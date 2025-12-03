package app.ninesevennine.twofactorauthenticator.features.crypto

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import app.ninesevennine.twofactorauthenticator.BuildConfig
import app.ninesevennine.twofactorauthenticator.utils.Logger
import java.io.File
import javax.crypto.SecretKey

@SuppressLint("StaticFieldLeak")
class SecureCryptoViewModel(private val context: Context) : ViewModel() {
    private val keyAlias = "${BuildConfig.APPLICATION_ID}.secure.crypto.key"
    private var key: SecretKey? = null

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = SecureCryptoModel.getKeyStore()
            keyStore.getKey(keyAlias, null) as? SecretKey
        } catch (e: Exception) {
            Logger.e("SecureCryptoViewModel", "getSecretKey failed: ${e.stackTraceToString()}")
            null
        }
    }

    fun init() {
        try {
            key = getSecretKey()

            if (key == null) {
                SecureCryptoModel.generateKey(keyAlias)
                key = getSecretKey()
            }

            key?.let {
                if (SecureCryptoModel.isKeyPermanentlyInvalidated(it)) {
                    val keyStore = SecureCryptoModel.getKeyStore()
                    keyStore.deleteEntry(keyAlias)

                    val file = File(context.noBackupFilesDir, "vault.json")
                    file.delete()

                    SecureCryptoModel.generateKey(keyAlias)
                    key = getSecretKey()
                }
            }

        } catch (e: Exception) {
            Logger.e("SecureCryptoViewModel", "init failed: ${e.stackTraceToString()}")
        }
    }

    fun encrypt(data: ByteArray): ByteArray? = key?.let { SecureCryptoModel.encrypt(it, data) }

    fun decrypt(data: ByteArray): ByteArray? = key?.let { SecureCryptoModel.decrypt(it, data) }
}