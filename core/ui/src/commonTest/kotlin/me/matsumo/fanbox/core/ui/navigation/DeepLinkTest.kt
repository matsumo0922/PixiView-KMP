package me.matsumo.fanbox.core.ui.navigation

import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.PostId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** FANBOX の URL から遷移先を求める処理を検証する。 */
class DeepLinkTest {

    @Test
    fun postUrlOnWwwHostOpensPostDetail() {
        val destination = deepLinkDestinationOf("https://www.fanbox.cc/@riosi1126/posts/8683683")

        assertEquals(Destination.PostDetail(PostId("8683683")), destination)
    }

    @Test
    fun postUrlOnCreatorHostOpensPostDetail() {
        val destination = deepLinkDestinationOf("https://riosi1126.fanbox.cc/posts/8683683?utm_source=x")

        assertEquals(Destination.PostDetail(PostId("8683683")), destination)
    }

    @Test
    fun creatorUrlOnWwwHostOpensCreatorTop() {
        val destination = deepLinkDestinationOf("https://www.fanbox.cc/@riosi1126")

        assertEquals(Destination.CreatorTop(CreatorId("riosi1126")), destination)
    }

    @Test
    fun creatorHostWithAndWithoutTrailingSlashOpensCreatorTop() {
        val expected = Destination.CreatorTop(CreatorId("riosi1126"))

        assertEquals(expected, deepLinkDestinationOf("https://riosi1126.fanbox.cc/"))
        assertEquals(expected, deepLinkDestinationOf("https://riosi1126.fanbox.cc"))
    }

    @Test
    fun unsupportedUrlsReturnNull() {
        assertNull(deepLinkDestinationOf("https://riosi1126.fanbox.cc/plans"))
        assertNull(deepLinkDestinationOf("https://www.fanbox.cc/@riosi1126/posts/8683683/comments"))
        assertNull(deepLinkDestinationOf("http://riosi1126.fanbox.cc/posts/8683683"))
        assertNull(deepLinkDestinationOf("https://matsumo0922.github.io/"))
    }
}
