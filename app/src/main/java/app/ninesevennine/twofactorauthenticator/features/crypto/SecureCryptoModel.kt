package app.ninesevennine.twofactorauthenticator.features.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import app.ninesevennine.twofactorauthenticator.utils.Logger
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SecureCryptoModel {
    private const val KEY_STORE_PROVIDER = "AndroidKeyStore"
    private const val IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128 // default

    private const val TRANSFORMATION_ALGORITHM = "AES/GCM/NoPadding"

    fun getKeyStore(): KeyStore {
        return KeyStore.getInstance(KEY_STORE_PROVIDER).apply {
            load(null)
        }
    }

    fun generateKey(id: String) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEY_STORE_PROVIDER
        )

        val builder = KeyGenParameterSpec.Builder(
            id,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setUserAuthenticationRequired(false)
            setInvalidatedByBiometricEnrollment(false)
            setRandomizedEncryptionRequired(true)
            setUnlockedDeviceRequired(true)
            setKeySize(256)
            setIsStrongBoxBacked(true)
        }

        try {
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        } catch (_: StrongBoxUnavailableException) {
            Logger.w("SecureCryptoModel", "StrongBox unavailable")

            builder.setIsStrongBoxBacked(false)
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
    }

    fun isKeyPermanentlyInvalidated(key: SecretKey): Boolean {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
        } catch (e: Exception) {
            Logger.w("SecureCryptoModel", "isKeyPermanentlyInvalidated $e")
            return true
        }

        return false
    }

    fun encrypt(key: SecretKey, data: ByteArray): ByteArray? {
        if (data.isEmpty()) return null

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val iv = cipher.iv
            val ciphertext = cipher.doFinal(data)

            iv + ciphertext
        } catch (e: Exception) {
            Logger.e("SecureCryptoModel", "Error encrypting data: ${e.stackTraceToString()}")
            null
        }
    }

    fun decrypt(key: SecretKey, data: ByteArray): ByteArray? {
        if (data.size < IV_LENGTH + (GCM_TAG_LENGTH / 8)) return null

        return try {
            val iv = data.copyOfRange(0, IV_LENGTH)
            val ciphertext = data.copyOfRange(IV_LENGTH, data.size)

            val cipher = Cipher.getInstance(TRANSFORMATION_ALGORITHM)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            Logger.e("SecureCryptoModel", "Error decrypting data: ${e.stackTraceToString()}")
            null
        }
    }
}