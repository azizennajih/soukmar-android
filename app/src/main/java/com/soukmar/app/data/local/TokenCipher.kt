package com.soukmar.app.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts the login token with an AES key that lives in the Android Keystore (never leaves the secure hardware /
 * system process), so the token is not readable from the app's DataStore file (rooted device, backup extraction,
 * file-system exploit). Stored form: "enc1:" + base64(iv) + ":" + base64(ciphertext).
 *
 * A value without the prefix is a token saved by an older app version; it is still accepted and gets encrypted the
 * next time the token is written. If decryption fails (key lost, e.g. after a restore), null is returned and the
 * user simply has to log in again.
 */
object TokenCipher {
    private const val KEY_ALIAS = "soukmar_token_key"
    private const val PREFIX = "enc1:"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    fun encrypt(plain: String): String = try {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val data = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        PREFIX + Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(data, Base64.NO_WRAP)
    } catch (e: Exception) {
        // Keystore unavailable on this device: keep the app usable rather than locking the user out.
        plain
    }

    fun decrypt(stored: String): String? {
        if (!stored.startsWith(PREFIX)) return stored
        return try {
            val parts = stored.removePrefix(PREFIX).split(":")
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val data = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv)) }
            String(cipher.doFinal(data), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
