package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import dev.icerock.moko.resources.ImageResource
import io.ktor.client.statement.readBytes
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.repository.FanboxRepository
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGDataProviderCopyData
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageCreateCopyWithColorSpace
import platform.CoreGraphics.CGImageGetAlphaInfo
import platform.CoreGraphics.CGImageGetBytesPerRow
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.Foundation.NSData
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.writeData
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum

@OptIn(ExperimentalForeignApi::class, ExperimentalCoilApi::class)
@Composable
actual fun ImageResource.asCoilImage(): coil3.Image {
    val skiaImage = toUIImage()?.toSkiaImage()
    val bitmap = skiaImage?.toComposeImageBitmap()?.asSkiaBitmap()

    return bitmap?.asCoilImage() ?: error("can't read UIImage of $this")
}

@OptIn(ExperimentalForeignApi::class)
internal fun UIImage.toSkiaImage(): Image? {
    val imageRef = CGImageCreateCopyWithColorSpace(this.CGImage, CGColorSpaceCreateDeviceRGB()) ?: return null

    val width = CGImageGetWidth(imageRef).toInt()
    val height = CGImageGetHeight(imageRef).toInt()

    val bytesPerRow = CGImageGetBytesPerRow(imageRef)
    val data = CGDataProviderCopyData(CGImageGetDataProvider(imageRef))
    val bytePointer = CFDataGetBytePtr(data)
    val length = CFDataGetLength(data)
    val alphaInfo = CGImageGetAlphaInfo(imageRef)

    val alphaType = when (alphaInfo) {
        CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst, CGImageAlphaInfo.kCGImageAlphaPremultipliedLast -> ColorAlphaType.PREMUL
        CGImageAlphaInfo.kCGImageAlphaFirst, CGImageAlphaInfo.kCGImageAlphaLast -> ColorAlphaType.UNPREMUL
        CGImageAlphaInfo.kCGImageAlphaNone, CGImageAlphaInfo.kCGImageAlphaNoneSkipFirst, CGImageAlphaInfo.kCGImageAlphaNoneSkipLast -> ColorAlphaType.OPAQUE
        else -> ColorAlphaType.UNKNOWN
    }

    val byteArray = ByteArray(length.toInt()) { index ->
        bytePointer!![index].toByte()
    }

    CFRelease(data)
    CFRelease(imageRef)

    return Image.makeRaster(
        imageInfo = ImageInfo(width = width, height = height, colorType = ColorType.RGBA_8888, alphaType = alphaType),
        bytes = byteArray,
        rowBytes = bytesPerRow.toInt(),
    )
}

class ImageDownloaderImpl(
    private val fanboxRepository: FanboxRepository,
): ImageDownloader {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun downloadImage(item: FanboxPostDetail.ImageItem): Boolean = suspendRunCatching {
        val bytes = fanboxRepository.download(item.originalUrl).readBytes()
        val nsData = memScoped { NSData.create(bytes = allocArrayOf(bytes), length = bytes.size.toULong()) }
        val uiImage = UIImage.imageWithData(nsData)!!

        UIImageWriteToSavedPhotosAlbum(uiImage, null, null, null)
    }.isSuccess

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun downloadFile(item: FanboxPostDetail.FileItem): Boolean = suspendRunCatching {
        val path = NSHomeDirectory() + "/Documents/FANBOX"
        val name = "illust-${item.postId}-${item.id}.${item.extension}"
        val fileManager = NSFileManager.defaultManager

        if (!fileManager.fileExistsAtPath(path)) {
            fileManager.createDirectoryAtPath(path, true, null, null)
        }

        val bytes = fanboxRepository.download(item.url).readBytes()
        val nsData = memScoped { NSData.create(bytes = allocArrayOf(bytes), length = bytes.size.toULong()) }

        NSFileHandle.fileHandleForWritingAtPath(path + name)!!.apply {
            writeData(nsData)
            closeFile()
        }
    }.isSuccess
}
