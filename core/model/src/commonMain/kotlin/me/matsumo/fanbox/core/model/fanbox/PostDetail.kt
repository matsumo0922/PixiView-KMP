package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 投稿の詳細。 */
@Immutable
@OptIn(ExperimentalTime::class)
data class PostDetail(
    val id: PostId,
    val title: String,
    val body: Body,
    val coverImageUrl: String?,
    val commentCount: Int,
    val excerpt: String,
    val feeRequired: Int,
    val hasAdultContent: Boolean,
    val imageForShare: String,
    val isLiked: Boolean,
    val isRestricted: Boolean,
    val likeCount: Int,
    val tags: List<String>,
    val updatedDatetime: Instant,
    val publishedDatetime: Instant,
    val nextPost: OtherPost?,
    val prevPost: OtherPost?,
    val user: User?,
) {
    /** 投稿のブラウザ表示 URL。 */
    val browserUrl get() = "https://www.fanbox.cc/@${user?.creatorId?.value}/posts/${id.value}"

    /** 投稿本文の種別。 */
    @Immutable
    sealed interface Body {
        /** 本文が含む画像アイテムの一覧。 */
        val imageItems
            get() = when (this) {
                is Article -> blocks.filterIsInstance<Article.Block.Image>().map {
                    it.item
                }

                is Image -> images
                is File -> files.mapNotNull {
                    it.asImageItem()
                }

                is Text, is Video, is Html, is Unknown -> emptyList()
            }

        /** 本文が含むファイルアイテムの一覧。 */
        val fileItems
            get() = when (this) {
                is Article -> blocks.filterIsInstance<Article.Block.File>()
                    .map {
                        it.item
                    }

                is Image -> emptyList()
                is File -> files
                is Text, is Video, is Html, is Unknown -> emptyList()
            }

        /** 記事本文。 */
        @Immutable
        data class Article(val blocks: List<Block>) : Body {
            /** 記事本文を構成するブロック。 */
            @Immutable
            sealed interface Block {
                /**
                 * 記事本文の段落・見出し。
                 *
                 * [isHeader] で段落表示か見出し表示かを選ぶ。スパンのオフセットと長さは FANBOX
                 * ペイロードの UTF-16 コード単位座標を使う未検証データであり、呼び出し側は適用前に
                 * [text] の範囲内であることを検証しなければならない。
                 */
                @Immutable
                data class Text(
                    val text: String,
                    val styles: List<StyleSpan> = emptyList(),
                    val links: List<LinkSpan> = emptyList(),
                    val isHeader: Boolean = false,
                ) : Block {
                    /**
                     * FANBOX の UTF-16 コード単位座標による未検証の装飾範囲。
                     *
                     * 呼び出し側は [offset] と [length] を所有する Text の文字列範囲に照らして検証して
                     * から装飾を適用すること。未知の [type] もそのまま保持する。
                     */
                    @Immutable
                    data class StyleSpan(
                        val type: String,
                        val offset: Int,
                        val length: Int,
                    )

                    /**
                     * FANBOX の UTF-16 コード単位座標による未検証のインラインリンク範囲。
                     *
                     * 呼び出し側は [offset] と [length] を所有する Text の文字列範囲に照らして検証し、
                     * ナビゲーション前に [url] をアプリの URL・スキームポリシーに照らして検証すること。
                     */
                    @Immutable
                    data class LinkSpan(
                        val offset: Int,
                        val length: Int,
                        val url: String,
                    )
                }

                /** 画像ブロック。 */
                @Immutable
                data class Image(val item: ImageItem) : Block

                /** ファイルブロック。 */
                @Immutable
                data class File(val item: FileItem) : Block

                /**
                 * 記事ブロックが参照する URL 埋め込み。
                 *
                 * [type] は FANBOX の埋め込み種別。`default` は単純な [url] を、`html` と `html.card` は
                 * 未検証の [html] を、`fanbox.post` は [post] を表す。未知の種別は種別固有の情報を作らず
                 * そのまま保持する。呼び出し側は [html] を描画前にサニタイズすること。
                 */
                @Immutable
                data class Link(
                    val html: String?,
                    val post: Post?,
                    val type: String = "unknown",
                    val url: String? = null,
                ) : Block

                /**
                 * サービス提供者に紐づく記事埋め込み。
                 *
                 * fanbox の場合 [url] はリダイレクト元 URL。リダイレクトの解決は呼び出し側のネットワーク
                 * 責務とする。
                 */
                @Immutable
                data class Embed(
                    val serviceProvider: String,
                    val contentId: String,
                ) : Block {
                    /** 対応済みサービス提供者の埋め込み URL。未対応の場合は null。 */
                    val url: String?
                        get() = when (serviceProvider) {
                            "twitter" -> "https://twitter.com/_/status/$contentId"
                            "youtube" -> "https://www.youtube.com/watch?v=$contentId"
                            "vimeo" -> "https://vimeo.com/$contentId"
                            "soundcloud" -> "https://soundcloud.com/$contentId"
                            "google_forms" -> {
                                "https://docs.google.com/forms/d/e/$contentId/viewform?usp=sf_link"
                            }

                            "fanbox" -> "https://www.pixiv.net/fanbox/$contentId"
                            else -> null
                        }
                }

                /**
                 * 既知の variant に当てはまらない記事ブロック。
                 *
                 * [rawJson] はブロック本体の JSON、またはそのエントリが原因でフォールバックした場合は
                 * 参照先マップエントリの JSON を保持する。
                 */
                @Immutable
                data class Unknown(val rawJson: String) : Block
            }
        }

        /** 画像本文。 */
        @Immutable
        data class Image(
            val text: String,
            val images: List<ImageItem>,
        ) : Body

        /** ファイル本文。 */
        @Immutable
        data class File(
            val text: String,
            val files: List<FileItem>,
        ) : Body

        /** テキスト本文。 */
        @Immutable
        data class Text(val text: String) : Body

        /** 動画本文。 */
        @Immutable
        data class Video(
            val serviceProvider: String,
            val videoId: String,
        ) : Body {
            /** 対応済みサービス提供者の動画 URL。未対応の場合は null。 */
            val url: String?
                get() = when (serviceProvider) {
                    "youtube" -> "https://www.youtube.com/watch?v=$videoId"
                    "vimeo" -> "https://vimeo.com/$videoId"
                    else -> null
                }
        }

        /**
         * FANBOX からサニタイズされずに届く HTML 本文。
         *
         * [html] は未検証のため、呼び出し側は描画前にサニタイズしなければならない。
         */
        @Immutable
        data class Html(val html: String) : Body

        /** 既知の variant に当てはまらない本文。 */
        @Immutable
        data class Unknown(
            val type: String = "unknown",
            val rawBodyJson: String? = null,
        ) : Body
    }

    /** 前後の投稿。 */
    @Immutable
    @OptIn(ExperimentalTime::class)
    data class OtherPost(
        val id: PostId,
        val title: String,
        val publishedDatetime: Instant,
    )

    /** 画像アイテム。 */
    @Immutable
    data class ImageItem(
        val id: PostItemId,
        val postId: PostId,
        val extension: String,
        val originalUrl: String,
        val thumbnailUrl: String,
        val aspectRatio: Float,
    )

    /** 動画アイテム。 */
    @Immutable
    data class VideoItem(
        val id: PostItemId,
        val postId: PostId,
        val extension: String,
        val url: String,
    )

    /** ファイルアイテム。 */
    @Immutable
    data class FileItem(
        val id: PostItemId,
        val postId: PostId,
        val name: String,
        val extension: String,
        val size: Long,
        val url: String,
    ) {
        /** 画像として扱える拡張子であれば [ImageItem] へ読み替える。 */
        fun asImageItem(): ImageItem? {
            return if (!extension.lowercase().contains(Regex("""(jpg|jpeg|png|gif)"""))) {
                null
            } else {
                ImageItem(
                    id = id,
                    postId = postId,
                    extension = extension,
                    originalUrl = url,
                    thumbnailUrl = url,
                    aspectRatio = 1f,
                )
            }
        }

        /** 動画として扱える拡張子であれば [VideoItem] へ読み替える。 */
        fun asVideoItem(): VideoItem? {
            return if (!extension.lowercase().contains(Regex("""(mp4|webm)"""))) {
                null
            } else {
                VideoItem(
                    id = id,
                    postId = postId,
                    extension = extension,
                    url = url,
                )
            }
        }
    }
}
