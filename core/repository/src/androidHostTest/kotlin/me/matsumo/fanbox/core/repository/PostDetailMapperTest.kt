package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.model.fanbox.PostDetail
import me.matsumo.fanbox.core.model.fanbox.PostDetail.Body
import me.matsumo.fanbox.core.model.fanbox.PostDetail.Body.Article.Block
import me.matsumo.fanbox.core.model.fanbox.PostDetail.Body.Article.Block.Text
import me.matsumo.fanbox.core.repository.mapper.toPostDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxUser
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostItemId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail.Body as FanktBody
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail.Body.Article.Block as FanktBlock
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail.Body.Article.Block.Text as FanktBlockText

/**
 * [FanboxPostDetail] から [PostDetail] への変換を検証するテスト。
 *
 * D5 の方針により、このモデルの変換は fankt → app の一方向のみを持つ。
 */
@OptIn(ExperimentalTime::class)
class PostDetailMapperTest {

    private val imageItem = FanboxPostDetail.ImageItem(
        id = FanboxPostItemId("item-1"),
        postId = FanboxPostId("post-1"),
        extension = "jpg",
        originalUrl = "https://example.com/original.jpg",
        thumbnailUrl = "https://example.com/thumbnail.jpg",
        aspectRatio = 1.5f,
    )

    private val fileItem = FanboxPostDetail.FileItem(
        id = FanboxPostItemId("item-2"),
        postId = FanboxPostId("post-1"),
        name = "document",
        extension = "pdf",
        size = 1024L,
        url = "https://example.com/document.pdf",
    )

    private val textBlock = FanktBlock.Text(
        text = "hello world",
        styles = listOf(FanktBlockText.StyleSpan(type = "bold", offset = 0, length = 5)),
        links = listOf(FanktBlockText.LinkSpan(offset = 6, length = 5, url = "https://example.com")),
        isHeader = true,
    )

    private val imageBlock = FanktBlock.Image(item = imageItem)
    private val fileBlock = FanktBlock.File(item = fileItem)

    private val linkBlock = FanktBlock.Link(
        html = "<p>html</p>",
        post = FanboxPost(
            id = FanboxPostId("linked-post"),
            title = "linked title",
            cover = null,
            user = null,
            excerpt = "excerpt",
            feeRequired = 0,
            hasAdultContent = false,
            isLiked = false,
            isRestricted = false,
            likeCount = 0,
            commentCount = 0,
            tags = emptyList(),
            publishedDatetime = Instant.parse("2024-01-01T00:00:00Z"),
            updatedDatetime = Instant.parse("2024-01-01T00:00:00Z"),
        ),
        type = "fanbox.post",
        url = "https://example.com/linked",
    )

    private val embedBlock = FanktBlock.Embed(
        serviceProvider = "youtube",
        contentId = "video-id",
    )

    private val unknownBlock = FanktBlock.Unknown(rawJson = """{"type":"mystery"}""")

    private val articleBody = FanktBody.Article(
        blocks = listOf(textBlock, imageBlock, fileBlock, linkBlock, embedBlock, unknownBlock),
    )

    private val fanboxPostDetail = FanboxPostDetail(
        id = FanboxPostId("post-1"),
        title = "title",
        body = articleBody,
        coverImageUrl = "https://example.com/cover.png",
        commentCount = 3,
        excerpt = "excerpt",
        feeRequired = 500,
        hasAdultContent = true,
        imageForShare = "https://example.com/share.png",
        isLiked = true,
        isRestricted = false,
        likeCount = 10,
        tags = listOf("tag1", "tag2"),
        updatedDatetime = Instant.parse("2024-01-02T00:00:00Z"),
        publishedDatetime = Instant.parse("2024-01-01T00:00:00Z"),
        nextPost = FanboxPostDetail.OtherPost(
            id = FanboxPostId("next-post"),
            title = "next title",
            publishedDatetime = Instant.parse("2024-01-03T00:00:00Z"),
        ),
        prevPost = FanboxPostDetail.OtherPost(
            id = FanboxPostId("prev-post"),
            title = "prev title",
            publishedDatetime = Instant.parse("2023-12-31T00:00:00Z"),
        ),
        user = FanboxUser(
            userId = FanboxUserId(100),
            creatorId = FanboxCreatorId("creator-1"),
            name = "creator name",
            iconUrl = "https://example.com/icon.png",
        ),
    )

    /** 記事本文の全フィールドが値を保ったまま変換されることを確認する。値が欠けていても検出できないため。 */
    @Test
    fun postDetailConversionPreservesAllFields() {
        val postDetail = fanboxPostDetail.toPostDetail()

        assertEquals(fanboxPostDetail.id.value, postDetail.id.value)
        assertEquals(fanboxPostDetail.title, postDetail.title)
        assertEquals(fanboxPostDetail.coverImageUrl, postDetail.coverImageUrl)
        assertEquals(fanboxPostDetail.commentCount, postDetail.commentCount)
        assertEquals(fanboxPostDetail.excerpt, postDetail.excerpt)
        assertEquals(fanboxPostDetail.feeRequired, postDetail.feeRequired)
        assertEquals(fanboxPostDetail.hasAdultContent, postDetail.hasAdultContent)
        assertEquals(fanboxPostDetail.imageForShare, postDetail.imageForShare)
        assertEquals(fanboxPostDetail.isLiked, postDetail.isLiked)
        assertEquals(fanboxPostDetail.isRestricted, postDetail.isRestricted)
        assertEquals(fanboxPostDetail.likeCount, postDetail.likeCount)
        assertEquals(fanboxPostDetail.tags, postDetail.tags)
        assertEquals(fanboxPostDetail.updatedDatetime, postDetail.updatedDatetime)
        assertEquals(fanboxPostDetail.publishedDatetime, postDetail.publishedDatetime)
        assertEquals(fanboxPostDetail.nextPost?.id?.value, postDetail.nextPost?.id?.value)
        assertEquals(fanboxPostDetail.nextPost?.title, postDetail.nextPost?.title)
        assertEquals(fanboxPostDetail.nextPost?.publishedDatetime, postDetail.nextPost?.publishedDatetime)
        assertEquals(fanboxPostDetail.prevPost?.id?.value, postDetail.prevPost?.id?.value)
        assertEquals(fanboxPostDetail.user?.userId?.value, postDetail.user?.userId?.value)
        assertEquals(fanboxPostDetail.user?.creatorId?.value, postDetail.user?.creatorId?.value)
    }

    /** 記事ブロックを構成する6種類すべてが変換できることを確認する。1種類でも欠けると記事が描画できないため。 */
    @Test
    fun allArticleBlockVariantsConvertSuccessfully() {
        val body = fanboxPostDetail.toPostDetail().body

        assertIs<Body.Article>(body)
        assertEquals(6, body.blocks.size)

        val text = assertIs<Block.Text>(body.blocks[0])
        assertEquals(textBlock.text, text.text)
        assertEquals(textBlock.isHeader, text.isHeader)
        assertEquals(textBlock.styles[0].type, text.styles[0].type)
        assertEquals(textBlock.links[0].url, text.links[0].url)

        val image = assertIs<Block.Image>(body.blocks[1])
        assertEquals(imageItem.id.value, image.item.id.value)
        assertEquals(imageItem.originalUrl, image.item.originalUrl)

        val file = assertIs<Block.File>(body.blocks[2])
        assertEquals(fileItem.id.value, file.item.id.value)
        assertEquals(fileItem.name, file.item.name)

        val link = assertIs<Block.Link>(body.blocks[3])
        assertEquals(linkBlock.html, link.html)
        assertEquals(linkBlock.post?.id?.value, link.post?.id?.value)
        assertEquals(linkBlock.type, link.type)
        assertEquals(linkBlock.url, link.url)

        val embed = assertIs<Block.Embed>(body.blocks[4])
        assertEquals(embedBlock.serviceProvider, embed.serviceProvider)
        assertEquals(embedBlock.contentId, embed.contentId)

        val unknown = assertIs<Block.Unknown>(body.blocks[5])
        assertEquals(unknownBlock.rawJson, unknown.rawJson)
    }

    /** 未知種別の記事ブロックが元の raw JSON を失わないことを確認する。フォールバック表示に必要なため。 */
    @Test
    fun unknownBlockVariantPreservesRawJson() {
        val body = fanboxPostDetail.toPostDetail().body

        assertIs<Body.Article>(body)

        val unknown = assertIs<Block.Unknown>(body.blocks.last())
        assertEquals("""{"type":"mystery"}""", unknown.rawJson)
    }

    /** 記事以外の6種類の本文がそれぞれ変換できることを確認する。 */
    @Test
    fun allBodyVariantsConvertSuccessfully() {
        val imageBody = fanboxPostDetail.copy(
            body = FanktBody.Image(text = "image text", images = listOf(imageItem)),
        ).toPostDetail().body
        val image = assertIs<Body.Image>(imageBody)
        assertEquals("image text", image.text)
        assertEquals(1, image.images.size)

        val fileBody = fanboxPostDetail.copy(
            body = FanktBody.File(text = "file text", files = listOf(fileItem)),
        ).toPostDetail().body
        val file = assertIs<Body.File>(fileBody)
        assertEquals("file text", file.text)
        assertEquals(1, file.files.size)

        val textBody = fanboxPostDetail.copy(
            body = FanktBody.Text(text = "plain text"),
        ).toPostDetail().body
        val text = assertIs<Body.Text>(textBody)
        assertEquals("plain text", text.text)

        val videoBody = fanboxPostDetail.copy(
            body = FanktBody.Video(serviceProvider = "youtube", videoId = "video-id"),
        ).toPostDetail().body
        val video = assertIs<Body.Video>(videoBody)
        assertEquals("youtube", video.serviceProvider)
        assertEquals("video-id", video.videoId)

        val htmlBody = fanboxPostDetail.copy(
            body = FanktBody.Html(html = "<p>html</p>"),
        ).toPostDetail().body
        val html = assertIs<Body.Html>(htmlBody)
        assertEquals("<p>html</p>", html.html)

        val unknownBody = fanboxPostDetail.copy(
            body = FanktBody.Unknown(type = "mystery", rawBodyJson = """{"foo":"bar"}"""),
        ).toPostDetail().body
        val unknown = assertIs<Body.Unknown>(unknownBody)
        assertEquals("mystery", unknown.type)
        assertEquals("""{"foo":"bar"}""", unknown.rawBodyJson)
    }

    /** 未知種別の本文が元の raw JSON を失わないことを確認する。フォールバック表示に必要なため。 */
    @Test
    fun unknownBodyVariantPreservesRawJson() {
        val postDetail = fanboxPostDetail.copy(
            body = FanktBody.Unknown(type = "mystery", rawBodyJson = """{"foo":"bar"}"""),
        ).toPostDetail()

        val unknown = assertIs<Body.Unknown>(postDetail.body)
        assertEquals("""{"foo":"bar"}""", unknown.rawBodyJson)
    }

    /**
     * fankt から移植した派生メンバーが、fankt の元実装と同じ値を返すことを確認する。
     * 独自ロジックで再実装すると計算式のズレに気付けないため、fankt 側の値と突き合わせる。
     */
    @Test
    fun derivedMembersMatchFanktValues() {
        val postDetail = fanboxPostDetail.toPostDetail()

        assertEquals(fanboxPostDetail.browserUrl, postDetail.browserUrl)
        assertEquals(
            fanboxPostDetail.body.imageItems.map { it.id.value },
            postDetail.body.imageItems.map { it.id.value },
        )
        assertEquals(
            fanboxPostDetail.body.fileItems.map { it.id.value },
            postDetail.body.fileItems.map { it.id.value },
        )

        val fanktArticleBody = assertIs<FanktBody.Article>(fanboxPostDetail.body)
        val articleBodyResult = assertIs<Body.Article>(postDetail.body)
        val fanktEmbed = assertIs<FanktBlock.Embed>(fanktArticleBody.blocks[4])
        val embed = assertIs<Block.Embed>(articleBodyResult.blocks[4])
        assertEquals(fanktEmbed.url, embed.url)

        val fanktVideoBody = FanktBody.Video(serviceProvider = "youtube", videoId = "video-id")
        val videoBody = assertIs<Body.Video>(fanboxPostDetail.copy(body = fanktVideoBody).toPostDetail().body)
        assertEquals(fanktVideoBody.url, videoBody.url)
    }

    /**
     * [FanboxPostDetail.FileItem.asImageItem] と [asVideoItem] が fankt の元実装と同じ結果を返すことを確認する。
     * 拡張子ごとの判定ロジックを独自に再実装しているため、fankt 側の値と突き合わせる。
     */
    @Test
    fun fileItemDerivedConversionMethodsMatchFanktValues() {
        val fanktImageExtensionFile = fileItem.copy(extension = "jpg")
        val fanktVideoExtensionFile = fileItem.copy(extension = "mp4")
        val fanktUnmatchedExtensionFile = fileItem.copy(extension = "pdf")

        val imageExtensionFile = fanboxPostDetail.copy(
            body = FanktBody.File(text = "file text", files = listOf(fanktImageExtensionFile)),
        ).toPostDetail().let { assertIs<Body.File>(it.body).files.single() }
        val videoExtensionFile = fanboxPostDetail.copy(
            body = FanktBody.File(text = "file text", files = listOf(fanktVideoExtensionFile)),
        ).toPostDetail().let { assertIs<Body.File>(it.body).files.single() }
        val unmatchedExtensionFile = fanboxPostDetail.copy(
            body = FanktBody.File(text = "file text", files = listOf(fanktUnmatchedExtensionFile)),
        ).toPostDetail().let { assertIs<Body.File>(it.body).files.single() }

        assertEquals(fanktImageExtensionFile.asImageItem()?.originalUrl, imageExtensionFile.asImageItem()?.originalUrl)
        assertEquals(fanktImageExtensionFile.asVideoItem(), null)
        assertEquals(imageExtensionFile.asVideoItem(), null)

        assertEquals(fanktVideoExtensionFile.asVideoItem()?.url, videoExtensionFile.asVideoItem()?.url)
        assertEquals(fanktVideoExtensionFile.asImageItem(), null)
        assertEquals(videoExtensionFile.asImageItem(), null)

        assertEquals(fanktUnmatchedExtensionFile.asImageItem(), null)
        assertEquals(fanktUnmatchedExtensionFile.asVideoItem(), null)
        assertEquals(unmatchedExtensionFile.asImageItem(), null)
        assertEquals(unmatchedExtensionFile.asVideoItem(), null)
    }
}
