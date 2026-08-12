package me.matsumo.fanbox.core.repository

import org.junit.Test
import kotlin.test.assertEquals

/** 配信物の署名検証に使う公開鍵が、Ed25519 の鍵として受け付けられる形であることを検証するテスト。 */
class GuestDeliveryKeyTest {

    /**
     * 鍵長の誤りは fankt が `Fanbox` の生成時に弾くため、起動して初めて分かる。貼り付けの取りこぼし
     * のような長さの誤りをここで先に落とす。
     */
    @Test
    fun trustedPublicKeyDecodesToAnEd25519Key() {
        assertEquals(ED25519_PUBLIC_KEY_SIZE, GUEST_TRUSTED_ED25519_PUBLIC_KEY_HEX.hexToByteArray().size)
    }

    private companion object {
        const val ED25519_PUBLIC_KEY_SIZE = 32
    }
}
