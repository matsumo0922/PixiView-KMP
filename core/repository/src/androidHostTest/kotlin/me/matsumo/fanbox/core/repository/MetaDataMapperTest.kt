package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.model.fanbox.MetaData
import me.matsumo.fanbox.core.repository.mapper.toMetaData
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData.Context
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [FanboxMetaData] からアプリ所有のメタデータへの変換を検証するテスト。
 *
 * 入れ子が 2 段あり、[Context.User] は同じ型の真偽値を 6 つ並べて持つ。取り違えても型が合うため
 * コンパイルでは捕まらない。ここが崩れると成人向け表示の可否や支援者判定が入れ替わる。
 */
class MetaDataMapperTest {

    private val fanboxMetaData = FanboxMetaData(
        apiUrl = "https://api.fanbox.cc",
        context = Context(
            privacyPolicy = Context.PrivacyPolicy(
                policyUrl = "https://example.com/policy",
                revisionHistoryUrl = "https://example.com/history",
                shouldShowNotice = true,
                updateDate = "2024-01-01",
            ),
            user = Context.User(
                creatorId = FanboxCreatorId("creator-1"),
                fanboxUserStatus = 2,
                hasAdultContent = true,
                hasUnpaidPayments = false,
                iconUrl = "https://example.com/icon.png",
                isCreator = true,
                isMailAddressOutdated = false,
                isSupporter = true,
                lang = "ja",
                name = "user name",
                planCount = 3,
                showAdultContent = false,
                userId = FanboxUserId(300),
            ),
        ),
        csrfToken = "token",
    )

    private val flagReaders: List<(MetaData.Context.User) -> Boolean> = listOf(
        { it.hasAdultContent },
        { it.hasUnpaidPayments },
        { it.isCreator },
        { it.isMailAddressOutdated },
        { it.isSupporter },
        { it.showAdultContent },
    )

    private val allFlagsDown: Context.User = requireNotNull(fanboxMetaData.context).user.copy(
        hasAdultContent = false,
        hasUnpaidPayments = false,
        isCreator = false,
        isMailAddressOutdated = false,
        isSupporter = false,
        showAdultContent = false,
    )

    /** 入れ子を含む全フィールドが写ることを確認する。真偽値の取り違えを含む。 */
    @Test
    fun metaDataConversionPreservesAllFields() {
        val metaData = fanboxMetaData.toMetaData()

        assertEquals("https://api.fanbox.cc", metaData.apiUrl)
        assertEquals("token", metaData.csrfToken)

        val privacyPolicy = metaData.context?.privacyPolicy
        assertEquals("https://example.com/policy", privacyPolicy?.policyUrl)
        assertEquals("https://example.com/history", privacyPolicy?.revisionHistoryUrl)
        assertEquals(true, privacyPolicy?.shouldShowNotice)
        assertEquals("2024-01-01", privacyPolicy?.updateDate)

        val user = metaData.context?.user
        assertEquals("creator-1", user?.creatorId?.value)
        assertEquals(2, user?.fanboxUserStatus)
        assertEquals(true, user?.hasAdultContent)
        assertEquals(false, user?.hasUnpaidPayments)
        assertEquals("https://example.com/icon.png", user?.iconUrl)
        assertEquals(true, user?.isCreator)
        assertEquals(false, user?.isMailAddressOutdated)
        assertEquals(true, user?.isSupporter)
        assertEquals("ja", user?.lang)
        assertEquals("user name", user?.name)
        assertEquals(3, user?.planCount)
        assertEquals(false, user?.showAdultContent)
        assertEquals(300L, user?.userId?.value)
    }

    /**
     * 6 つの真偽値がそれぞれ自分のフィールドへ写ることを確認する。
     *
     * 同じ値どうしの取り違えは、真偽値を並べた fixture では検出できない。1 つだけを立てた入力を
     * 6 通り作り、立った先が 1 つだけであることを見る。
     */
    @Test
    fun eachUserFlagIsMappedToItsOwnField() {
        assertOnlyThisFlagIsRaised({ it.copy(hasAdultContent = true) }, { it.hasAdultContent })
        assertOnlyThisFlagIsRaised({ it.copy(hasUnpaidPayments = true) }, { it.hasUnpaidPayments })
        assertOnlyThisFlagIsRaised({ it.copy(isCreator = true) }, { it.isCreator })
        assertOnlyThisFlagIsRaised({ it.copy(isMailAddressOutdated = true) }, { it.isMailAddressOutdated })
        assertOnlyThisFlagIsRaised({ it.copy(isSupporter = true) }, { it.isSupporter })
        assertOnlyThisFlagIsRaised({ it.copy(showAdultContent = true) }, { it.showAdultContent })
    }

    /** context が無い場合も安全に変換できることを確認する。 */
    @Test
    fun metaDataConversionHandlesMissingContext() {
        val metaData = fanboxMetaData.copy(apiUrl = null, context = null).toMetaData()

        assertNull(metaData.apiUrl)
        assertNull(metaData.context)
        assertEquals("token", metaData.csrfToken)
    }

    private fun assertOnlyThisFlagIsRaised(
        raiseOne: (Context.User) -> Context.User,
        readBack: (MetaData.Context.User) -> Boolean,
    ) {
        val context = requireNotNull(fanboxMetaData.context).copy(user = raiseOne(allFlagsDown))
        val user = requireNotNull(fanboxMetaData.copy(context = context).toMetaData().context).user

        assertEquals(1, flagReaders.count { reader -> reader(user) })
        assertEquals(true, readBack(user))
    }
}
