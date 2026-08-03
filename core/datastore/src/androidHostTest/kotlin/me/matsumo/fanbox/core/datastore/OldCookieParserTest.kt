package me.matsumo.fanbox.core.datastore

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Room 導入より前の Cookie 保存形式から FANBOXSESSID を取り出す処理を検証するテスト。
 *
 * 元の実装は `split("=")` で全ての `=` を区切りに使い、`get(1)` で 2 番目の要素を取っていた。
 * 値に `=` を含むと切り詰められ、`=` を含まない要素があると例外になっていた。また要素の前後の
 * 空白を落としていなかったため、`; ` 区切りの 2 番目以降の要素は名前が一致しなかった。
 */
class OldCookieParserTest {

    @Test
    fun singleElementIsParsed() {
        assertEquals("abc123", parseOldSessionId("FANBOXSESSID=abc123"))
    }

    @Test
    fun valueContainingEqualsIsPreserved() {
        assertEquals("12345_aB+cD/eF=", parseOldSessionId("FANBOXSESSID=12345_aB+cD/eF="))
    }

    @Test
    fun valueContainingMultipleEqualsIsPreserved() {
        assertEquals("a=b=c", parseOldSessionId("FANBOXSESSID=a=b=c"))
    }

    @Test
    fun leadingWhitespaceOnLaterElementIsTrimmed() {
        assertEquals("abc123", parseOldSessionId("other=1; FANBOXSESSID=abc123"))
    }

    @Test
    fun elementWithoutEqualsIsIgnored() {
        assertEquals("abc123", parseOldSessionId("broken; FANBOXSESSID=abc123"))
    }

    @Test
    fun elementWithoutEqualsAloneReturnsNull() {
        assertNull(parseOldSessionId("broken"))
    }

    @Test
    fun sessionIdIsFoundAmongManyCookies() {
        val header = "p_ab_id=7; privacy_policy_agreement=6; FANBOXSESSID=abc123; login_ever=yes"

        assertEquals("abc123", parseOldSessionId(header))
    }

    @Test
    fun trailingSemicolonIsIgnored() {
        assertEquals("abc123", parseOldSessionId("FANBOXSESSID=abc123;"))
    }

    @Test
    fun otherCookieWithSimilarNameIsNotMatched() {
        assertNull(parseOldSessionId("FANBOXSESSID_OLD=abc123"))
    }

    @Test
    fun emptyValueReturnsNull() {
        assertNull(parseOldSessionId("FANBOXSESSID="))
    }

    @Test
    fun emptyValueAmongOthersReturnsNull() {
        assertNull(parseOldSessionId("other=1; FANBOXSESSID=; another=2"))
    }

    @Test
    fun missingSessionCookieReturnsNull() {
        assertNull(parseOldSessionId("other=1; another=2"))
    }

    @Test
    fun blankInputReturnsNull() {
        assertNull(parseOldSessionId(""))
        assertNull(parseOldSessionId("   "))
    }

    @Test
    fun nullInputReturnsNull() {
        assertNull(parseOldSessionId(null))
    }

    @Test
    fun elementStartingWithEqualsIsIgnored() {
        assertEquals("abc123", parseOldSessionId("=orphan; FANBOXSESSID=abc123"))
    }

    @Test
    fun firstMatchWins() {
        assertEquals("first", parseOldSessionId("FANBOXSESSID=first; FANBOXSESSID=second"))
    }
}
