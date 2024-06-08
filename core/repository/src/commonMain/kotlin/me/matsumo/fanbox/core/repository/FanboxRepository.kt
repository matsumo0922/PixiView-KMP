package me.matsumo.fanbox.core.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fleeksoft.ksoup.Ksoup
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMessageBuilder
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.matsumo.fanbox.core.datastore.BlockDataStore
import me.matsumo.fanbox.core.datastore.BookmarkDataStore
import me.matsumo.fanbox.core.datastore.FanboxCookieDataStore
import me.matsumo.fanbox.core.model.FanboxTag
import me.matsumo.fanbox.core.model.PageCursorInfo
import me.matsumo.fanbox.core.model.PageNumberInfo
import me.matsumo.fanbox.core.model.PageOffsetInfo
import me.matsumo.fanbox.core.model.fanbox.FanboxBell
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlan
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlanDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorTag
import me.matsumo.fanbox.core.model.fanbox.FanboxCursor
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.model.fanbox.FanboxNewsLetter
import me.matsumo.fanbox.core.model.fanbox.FanboxPaidRecord
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxBellItemsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorItemsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorPlanEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorPlansEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorPostsPaginateEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorSearchEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxCreatorTagsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxMetaDataEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxNewsLettersEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPaidRecordEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPostCommentItemsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPostDetailEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPostItemsEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxPostSearchEntity
import me.matsumo.fanbox.core.model.fanbox.entity.FanboxTagsEntity
import me.matsumo.fanbox.core.model.fanbox.id.CommentId
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.repository.paging.CreatorPostsPagingSource
import me.matsumo.fanbox.core.repository.paging.HomePostsPagingSource
import me.matsumo.fanbox.core.repository.paging.SearchCreatorsPagingSource
import me.matsumo.fanbox.core.repository.paging.SearchPostsPagingSource
import me.matsumo.fanbox.core.repository.paging.SupportedPostsPagingSource
import me.matsumo.fanbox.core.repository.utils.parse
import me.matsumo.fanbox.core.repository.utils.requireSuccess
import me.matsumo.fanbox.core.repository.utils.translate
import kotlin.random.Random

interface FanboxRepository {
    val metaData: StateFlow<FanboxMetaData>
    val bookmarkedPosts: SharedFlow<List<PostId>>
    val blockedCreators: SharedFlow<Set<CreatorId>>
    val cookie: Flow<String>
    val logoutTrigger: Flow<Long>

    suspend fun logout()

    suspend fun isCookieValid(): Boolean
    suspend fun updateCookie(cookie: String)
    suspend fun updateCsrfToken()

    suspend fun getHomePosts(cursor: FanboxCursor?, loadSize: Int = cursor?.limit ?: 10): PageCursorInfo<FanboxPost>
    suspend fun getSupportedPosts(cursor: FanboxCursor?, loadSize: Int = cursor?.limit ?: 10): PageCursorInfo<FanboxPost>
    suspend fun getCreatorPosts(creatorId: CreatorId, cursor: FanboxCursor?, loadSize: Int = cursor?.limit ?: 10): PageCursorInfo<FanboxPost>
    suspend fun getCreatorPostsPaginate(creatorId: CreatorId): List<FanboxCursor>
    suspend fun getPost(postId: PostId): FanboxPostDetail
    suspend fun getPostCached(postId: PostId): FanboxPostDetail
    suspend fun getPostComment(postId: PostId, offset: Int = 0): PageOffsetInfo<FanboxPostDetail.Comment.CommentItem>
    suspend fun getPostFromQuery(query: String, creatorId: CreatorId? = null, page: Int = 0): PageNumberInfo<FanboxPost>
    suspend fun getCreatorFromQuery(query: String, page: Int = 0): PageNumberInfo<FanboxCreatorDetail>
    suspend fun getTagFromQuery(query: String): List<FanboxTag>

    fun getHomePostsPager(loadSize: Int, isHideRestricted: Boolean): Flow<PagingData<FanboxPost>>
    fun getHomePostsPagerCache(loadSize: Int, isHideRestricted: Boolean): Flow<PagingData<FanboxPost>>
    fun getSupportedPostsPager(loadSize: Int, isHideRestricted: Boolean): Flow<PagingData<FanboxPost>>
    fun getSupportedPostsPagerCache(loadSize: Int, isHideRestricted: Boolean): Flow<PagingData<FanboxPost>>
    fun getCreatorPostsPager(creatorId: CreatorId, loadSize: Int): Flow<PagingData<FanboxPost>>
    fun getCreatorPostsPagerCache(): Flow<PagingData<FanboxPost>>?
    fun getPostsFromQueryPager(query: String, creatorId: CreatorId? = null): Flow<PagingData<FanboxPost>>
    fun getPostsFromQueryPagerCache(): Flow<PagingData<FanboxPost>>?
    fun getCreatorsFromQueryPager(query: String): Flow<PagingData<FanboxCreatorDetail>>

    suspend fun getFollowingCreators(): List<FanboxCreatorDetail>
    suspend fun getFollowingPixivCreators(): List<FanboxCreatorDetail>
    suspend fun getRecommendedCreators(): List<FanboxCreatorDetail>

    suspend fun getCreator(creatorId: CreatorId): FanboxCreatorDetail
    suspend fun getCreatorCached(creatorId: CreatorId): FanboxCreatorDetail
    suspend fun getCreatorTags(creatorId: CreatorId): List<FanboxCreatorTag>

    suspend fun getSupportedPlans(): List<FanboxCreatorPlan>
    suspend fun getCreatorPlans(creatorId: CreatorId): List<FanboxCreatorPlan>
    suspend fun getCreatorPlan(creatorId: CreatorId): FanboxCreatorPlanDetail

    suspend fun getPaidRecords(): List<FanboxPaidRecord>
    suspend fun getUnpaidRecords(): List<FanboxPaidRecord>

    suspend fun getNewsLetters(): List<FanboxNewsLetter>
    suspend fun getBells(page: Int = 0): PageNumberInfo<FanboxBell>

    suspend fun likePost(postId: PostId)
    suspend fun likeComment(commentId: CommentId)

    suspend fun addComment(postId: PostId, comment: String, rootCommentId: CommentId? = null, parentCommentId: CommentId? = null)
    suspend fun deleteComment(commentId: CommentId)

    suspend fun followCreator(creatorUserId: String)
    suspend fun unfollowCreator(creatorUserId: String)

    suspend fun blockCreator(creatorId: CreatorId)
    suspend fun unblockCreator(creatorId: CreatorId)

    suspend fun getBookmarkedPosts(): List<FanboxPost>
    suspend fun bookmarkPost(post: FanboxPost)
    suspend fun unbookmarkPost(post: FanboxPost)

    suspend fun download(url: String, updateCallback: (Float) -> Unit): HttpResponse
}

class FanboxRepositoryImpl(
    private val client: HttpClient,
    private val formatter: Json,
    private val fanboxCookieDataStore: FanboxCookieDataStore,
    private val bookmarkDataStore: BookmarkDataStore,
    private val blockDataStore: BlockDataStore,
    private val ioDispatcher: CoroutineDispatcher,
) : FanboxRepository {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val creatorCache = mutableMapOf<CreatorId, FanboxCreatorDetail>()
    private val postCache = mutableMapOf<PostId, FanboxPostDetail>()
    private var homePostsPager: Flow<PagingData<FanboxPost>>? = null
    private var supportedPostsPager: Flow<PagingData<FanboxPost>>? = null
    private var creatorPostsPager: Flow<PagingData<FanboxPost>>? = null
    private var searchPostsPager: Flow<PagingData<FanboxPost>>? = null

    private val _metaData = MutableStateFlow(FanboxMetaData.dummy())
    private val _logoutTrigger = Channel<Long>()

    override val metaData: StateFlow<FanboxMetaData> = _metaData.asStateFlow()
    override val cookie: Flow<String> = fanboxCookieDataStore.data
    override val logoutTrigger: Flow<Long> = _logoutTrigger.receiveAsFlow()

    override val bookmarkedPosts: SharedFlow<List<PostId>> = bookmarkDataStore.data
    override val blockedCreators: SharedFlow<Set<CreatorId>> = blockDataStore.data

    override suspend fun logout() {
        CoroutineScope(ioDispatcher).launch {
            fanboxCookieDataStore.save("")
            bookmarkDataStore.clear()
            blockDataStore.clear()
            _logoutTrigger.send(Random.nextLong())
        }
    }

    override suspend fun isCookieValid(): Boolean {
        return !fanboxCookieDataStore.data.firstOrNull().isNullOrBlank()
    }

    override suspend fun updateCookie(cookie: String) {
        fanboxCookieDataStore.save(cookie)
    }

    override suspend fun updateCsrfToken() = withContext(ioDispatcher) {
        Napier.d { "updateCsrfToken" }

        val response = client.get("https://www.fanbox.cc/")
        val html = response.bodyAsText()
        val doc = Ksoup.parse(html)
        val meta = doc.select("meta[name=metadata]").first()?.attr("content")
        val data = formatter.decodeFromString(FanboxMetaDataEntity.serializer(), meta!!).translate()

        Napier.d { "updateCsrfToken: ${data.csrfToken}" }

        _metaData.emit(data)
    }

    override suspend fun getHomePosts(cursor: FanboxCursor?, loadSize: Int): PageCursorInfo<FanboxPost> = withContext(ioDispatcher) {
        buildMap {
            put("limit", loadSize.toString())

            if (cursor != null) {
                put("maxPublishedDatetime", cursor.maxPublishedDatetime)
                put("maxId", cursor.maxId)
            }
        }.let {
            get("post.listHome", it).parse<FanboxPostItemsEntity>()!!.translate(bookmarkedPosts.first())
        }
    }

    override suspend fun getSupportedPosts(cursor: FanboxCursor?, loadSize: Int): PageCursorInfo<FanboxPost> = withContext(ioDispatcher) {
        buildMap {
            put("limit", loadSize.toString())

            if (cursor != null) {
                put("maxPublishedDatetime", cursor.maxPublishedDatetime)
                put("maxId", cursor.maxId)
            }
        }.let {
            get("post.listSupporting", it).parse<FanboxPostItemsEntity>()!!.translate(bookmarkedPosts.first())
        }
    }

    override suspend fun getCreatorPosts(creatorId: CreatorId, cursor: FanboxCursor?, loadSize: Int): PageCursorInfo<FanboxPost> =
        withContext(ioDispatcher) {
            buildMap {
                put("creatorId", creatorId.value)
                put("limit", loadSize.toString())

                if (cursor != null) {
                    put("maxPublishedDatetime", cursor.maxPublishedDatetime)
                    put("maxId", cursor.maxId)
                }
            }.let {
                get("post.listCreator", it).parse<FanboxPostItemsEntity>()!!.translate(bookmarkedPosts.first())
            }
        }

    override suspend fun getPostFromQuery(query: String, creatorId: CreatorId?, page: Int): PageNumberInfo<FanboxPost> = withContext(ioDispatcher) {
        buildMap {
            put("tag", query)
            put("page", page.toString())

            if (creatorId != null) {
                put("creatorId", creatorId.value)
            }
        }.let {
            get("post.listTagged", it).parse<FanboxPostSearchEntity>()!!.translate(bookmarkedPosts.first())
        }
    }

    override suspend fun getCreatorPostsPaginate(creatorId: CreatorId): List<FanboxCursor> = withContext(ioDispatcher) {
        get("post.paginateCreator", mapOf("creatorId" to creatorId.value)).parse<FanboxCreatorPostsPaginateEntity>()!!.translate()
    }

    override suspend fun getCreatorFromQuery(query: String, page: Int): PageNumberInfo<FanboxCreatorDetail> = withContext(ioDispatcher) {
        get("creator.search", mapOf("q" to query, "page" to page.toString())).parse<FanboxCreatorSearchEntity>()!!.translate()
    }

    override suspend fun getTagFromQuery(query: String): List<FanboxTag> = withContext(ioDispatcher) {
        get("tag.search", mapOf("q" to query)).parse<FanboxTagsEntity>()!!.translate()
    }

    override suspend fun getPost(postId: PostId): FanboxPostDetail = withContext(ioDispatcher) {
        get("post.info", mapOf("postId" to postId.value)).parse<FanboxPostDetailEntity>()!!.translate(bookmarkedPosts.first()).also {
            postCache[postId] = it
        }
    }

    override suspend fun getPostCached(postId: PostId): FanboxPostDetail = withContext(ioDispatcher) {
        postCache.getOrPut(postId) { getPost(postId) }
    }

    override suspend fun getPostComment(postId: PostId, offset: Int): PageOffsetInfo<FanboxPostDetail.Comment.CommentItem> =
        withContext(ioDispatcher) {
            get("post.listComments", mapOf("postId" to postId.value, "offset" to offset.toString(), "limit" to "10"),).parse<FanboxPostCommentItemsEntity>()!!.translate()
        }

    override fun getHomePostsPager(loadSize: Int, isHideRestricted: Boolean): Flow<PagingData<FanboxPost>> {
        return Pager(
            config = PagingConfig(pageSize = loadSize),
            initialKey = null,
            pagingSourceFactory = {
                HomePostsPagingSource(this, isHideRestricted)
            },
        )
            .flow
            .cachedIn(scope)
            .also { homePostsPager = it }
    }

    override fun getHomePostsPagerCache(loadSize: Int, isHideRestricted: Boolean): Flow<PagingData<FanboxPost>> {
        return homePostsPager ?: getHomePostsPager(loadSize, isHideRestricted)
    }

    override fun getSupportedPostsPager(loadSize: Int, isHideRestricted: Boolean): Flow<PagingData<FanboxPost>> {
        return Pager(
            config = PagingConfig(pageSize = loadSize),
            initialKey = null,
            pagingSourceFactory = {
                SupportedPostsPagingSource(this, isHideRestricted)
            },
        )
            .flow
            .cachedIn(scope)
            .also { supportedPostsPager = it }
    }

    override fun getSupportedPostsPagerCache(loadSize: Int, isHideRestricted: Boolean): Flow<PagingData<FanboxPost>> {
        return supportedPostsPager ?: getSupportedPostsPager(loadSize, isHideRestricted)
    }

    override fun getCreatorPostsPager(creatorId: CreatorId, loadSize: Int): Flow<PagingData<FanboxPost>> {
        return Pager(
            config = PagingConfig(pageSize = loadSize),
            initialKey = null,
            pagingSourceFactory = {
                CreatorPostsPagingSource(creatorId, this)
            },
        )
            .flow
            .cachedIn(scope)
            .also { creatorPostsPager = it }
    }

    override fun getCreatorPostsPagerCache(): Flow<PagingData<FanboxPost>>? {
        return creatorPostsPager
    }

    override fun getPostsFromQueryPager(query: String, creatorId: CreatorId?): Flow<PagingData<FanboxPost>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            initialKey = null,
            pagingSourceFactory = {
                SearchPostsPagingSource(this, creatorId, query)
            },
        )
            .flow
            .cachedIn(scope)
            .also { searchPostsPager = it }
    }

    override fun getPostsFromQueryPagerCache(): Flow<PagingData<FanboxPost>>? {
        return searchPostsPager
    }

    override fun getCreatorsFromQueryPager(query: String): Flow<PagingData<FanboxCreatorDetail>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            initialKey = null,
            pagingSourceFactory = {
                SearchCreatorsPagingSource(this, query)
            },
        ).flow
    }

    override suspend fun getFollowingCreators(): List<FanboxCreatorDetail> = withContext(ioDispatcher) {
        get("creator.listFollowing").parse<FanboxCreatorItemsEntity>()!!.translate()
    }

    override suspend fun getFollowingPixivCreators(): List<FanboxCreatorDetail> = withContext(ioDispatcher) {
        get("creator.listPixiv").parse<FanboxCreatorItemsEntity>()!!.translate()
    }

    override suspend fun getRecommendedCreators(): List<FanboxCreatorDetail> = withContext(ioDispatcher) {
        get("creator.listRecommended", mapOf("limit" to PAGE_LIMIT)).parse<FanboxCreatorItemsEntity>()!!.translate()
    }

    override suspend fun getCreator(creatorId: CreatorId): FanboxCreatorDetail = withContext(ioDispatcher) {
        get("creator.get", mapOf("creatorId" to creatorId.value)).parse<FanboxCreatorEntity>()!!.translate().also {
            creatorCache[creatorId] = it
        }
    }

    override suspend fun getCreatorCached(creatorId: CreatorId): FanboxCreatorDetail = withContext(ioDispatcher) {
        creatorCache.getOrPut(creatorId) { getCreator(creatorId) }
    }

    override suspend fun getCreatorTags(creatorId: CreatorId): List<FanboxCreatorTag> = withContext(ioDispatcher) {
        get("tag.getFeatured", mapOf("creatorId" to creatorId.value)).parse<FanboxCreatorTagsEntity>()!!.translate()
    }

    override suspend fun getSupportedPlans(): List<FanboxCreatorPlan> = withContext(ioDispatcher) {
        get("plan.listSupporting").parse<FanboxCreatorPlansEntity>()!!.translate()
    }

    override suspend fun getCreatorPlans(creatorId: CreatorId): List<FanboxCreatorPlan> = withContext(ioDispatcher) {
        get("plan.listCreator", mapOf("creatorId" to creatorId.value)).parse<FanboxCreatorPlansEntity>()!!.translate()
    }

    override suspend fun getCreatorPlan(creatorId: CreatorId): FanboxCreatorPlanDetail = withContext(ioDispatcher) {
        get("legacy/support/creator", mapOf("creatorId" to creatorId.value)).parse<FanboxCreatorPlanEntity>()!!.translate()
    }

    override suspend fun getPaidRecords(): List<FanboxPaidRecord> = withContext(ioDispatcher) {
        get("payment.listPaid").parse<FanboxPaidRecordEntity>()!!.translate()
    }

    override suspend fun getUnpaidRecords(): List<FanboxPaidRecord> = withContext(ioDispatcher) {
        get("payment.listUnpaid").parse<FanboxPaidRecordEntity>()!!.translate()
    }

    override suspend fun getNewsLetters(): List<FanboxNewsLetter> = withContext(ioDispatcher) {
        val data = get("newsletter.list")
        data.parse<FanboxNewsLettersEntity>()!!.translate()
    }

    override suspend fun getBells(page: Int): PageNumberInfo<FanboxBell> = withContext(ioDispatcher) {
        buildMap {
            put("page", page.toString())
            put("skipConvertUnreadNotification", "0")
            put("commentOnly", "0")
        }.let {
            get("bell.list", it).parse<FanboxBellItemsEntity>()!!.translate()
        }
    }

    override suspend fun likePost(postId: PostId): Unit = withContext(ioDispatcher) {
        post("post.likePost", mapOf("postId" to postId.value)).requireSuccess()
    }

    override suspend fun likeComment(commentId: CommentId): Unit = withContext(ioDispatcher) {
        post("post.likeComment", mapOf("commentId" to commentId.value)).requireSuccess()
    }

    override suspend fun addComment(postId: PostId, comment: String, rootCommentId: CommentId?, parentCommentId: CommentId?): Unit =
        withContext(ioDispatcher) {
            post(
                dir = "post.addComment",
                parameters = mapOf(
                    "postId" to postId.value,
                    "rootCommentId" to rootCommentId?.value.orEmpty(),
                    "parentCommentId" to parentCommentId?.value.orEmpty(),
                    "body" to comment,
                ),
            ).requireSuccess()
        }

    override suspend fun deleteComment(commentId: CommentId): Unit = withContext(ioDispatcher) {
        post("post.deleteComment", mapOf("commentId" to commentId.value)).requireSuccess()
    }

    override suspend fun followCreator(creatorUserId: String): Unit = withContext(ioDispatcher) {
        post("follow.create", mapOf("creatorUserId" to creatorUserId)).requireSuccess()
    }

    override suspend fun unfollowCreator(creatorUserId: String): Unit = withContext(ioDispatcher) {
        post("follow.delete", mapOf("creatorUserId" to creatorUserId)).requireSuccess()
    }

    override suspend fun blockCreator(creatorId: CreatorId) {
        blockDataStore.blockCreator(creatorId)
    }

    override suspend fun unblockCreator(creatorId: CreatorId) {
        blockDataStore.unblockCreator(creatorId)
    }

    override suspend fun getBookmarkedPosts(): List<FanboxPost> = withContext(ioDispatcher) {
        bookmarkDataStore.get()
    }

    override suspend fun bookmarkPost(post: FanboxPost) = withContext(ioDispatcher) {
        bookmarkDataStore.save(post)
    }

    override suspend fun unbookmarkPost(post: FanboxPost) {
        bookmarkDataStore.remove(post)
    }

    private suspend fun html(url: String): String {
        return client.get(url).bodyAsText()
    }

    private suspend fun get(dir: String, parameters: Map<String, String> = emptyMap()): HttpResponse {
        return client.get {
            url("$API/$dir")
            fanboxHeader()

            for ((key, value) in parameters) {
                parameter(key, value)
            }
        }
    }

    @OptIn(InternalAPI::class)
    private suspend fun post(dir: String, parameters: Map<String, String> = emptyMap()): HttpResponse {
        return client.post {
            url("$API/$dir")
            fanboxHeader()

            body = buildJsonObject {
                for ((key, value) in parameters) {
                    put(key, value)
                }
            }.toString()
        }
    }

    override suspend fun download(url: String, updateCallback: (Float) -> Unit): HttpResponse {
        return client.get {
            url(url)
            fanboxHeader()
            
            onDownload { bytesSentTotal, contentLength ->
                updateCallback.invoke(bytesSentTotal.toFloat() / contentLength.toFloat())
            }
        }
    }

    private suspend fun HttpMessageBuilder.fanboxHeader() {
        header("origin", "https://www.fanbox.cc")
        header("referer", "https://www.fanbox.cc")
        header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")
        header("Content-Type", "application/json")
        header("x-csrf-token", metaData.first().csrfToken)
        header("Cookie", cookie.first())
    }

    companion object {
        private const val API = "https://api.fanbox.cc"
        private const val PAGE_LIMIT = "10"
    }
}
