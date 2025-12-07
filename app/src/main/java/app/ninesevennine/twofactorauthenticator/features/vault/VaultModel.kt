package app.ninesevennine.twofactorauthenticator.features.vault

import android.content.Context
import app.ninesevennine.twofactorauthenticator.features.otp.OtpHashFunctions
import app.ninesevennine.twofactorauthenticator.features.otp.OtpTypes
import app.ninesevennine.twofactorauthenticator.secureCryptoViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.OtpCardColors
import app.ninesevennine.twofactorauthenticator.utils.Argon2id
import app.ninesevennine.twofactorauthenticator.utils.ChaCha20Poly1305
import app.ninesevennine.twofactorauthenticator.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object VaultModel {
    private const val FILE_NAME = "vault.json"

    private fun vaultItemsAsJson(vaultItems: List<VaultItem>): String {
        val json = JSONArray().apply {
            vaultItems.forEach { item ->
                put(JSONObject().apply {
                    put("uuid", item.uuid.toString())
                    put("lastUpdated", item.lastUpdated)
                    if (item.name.isNotEmpty()) put("name", item.name)
                    if (item.issuer.isNotEmpty()) put("issuer", item.issuer)
                    if (item.note.isNotEmpty()) put("note", item.note)
                    put("secret", Base64.encode(item.secret))
                    if (item.otpType != OtpTypes.HOTP) put("period", item.period)
                    put("digits", item.digits)
                    if (item.otpType != OtpTypes.TOTP) put("counter", item.counter)
                    put("otpType", item.otpType.value)
                    put("otpHashFunction", item.otpHashFunction.value)
                    put("otpCardColor", item.otpCardColor.value)
                })
            }
        }.toString()

        return json
    }

    private fun encryptVault(context: Context, vaultItemsJson: String): ByteArray {
        context.secureCryptoViewModel.encrypt(vaultItemsJson.toByteArray(Charsets.UTF_8))?.let {
            return it
        }
        throw Exception("encrypt returned null")
    }

    fun saveVault(context: Context, vaultItems: List<VaultItem>) {
        Logger.i("VaultModel", "saveVault")

        runCatching {
            JSONObject().apply {
                put("version", 1)
                put("data", Base64.encode(encryptVault(context, vaultItemsAsJson(vaultItems))))
            }.toString().let { jsonString ->
                File(context.noBackupFilesDir, FILE_NAME).writeText(jsonString, Charsets.UTF_8)
            }
        }.onFailure { e ->
            Logger.e("VaultModel", "Error saving vault: ${e.stackTraceToString()}")
        }
    }

    fun backupVault(vaultItems: List<VaultItem>, password: String): String {
        Logger.i("VaultModel", "BackupVault")

        return runCatching {
            val salt = Argon2id.generateSalt(16)
            val hash = Argon2id.get(
                password = password.toByteArray(Charsets.UTF_8),
                salt = salt,
                outLength = ChaCha20Poly1305.KEY_SIZE + ChaCha20Poly1305.NONCE_SIZE
            )

            val key = hash.copyOfRange(0, ChaCha20Poly1305.KEY_SIZE)
            val nonce = hash.copyOfRange(ChaCha20Poly1305.KEY_SIZE, hash.size)

            val vaultsJsonAsByteArray = vaultItemsAsJson(vaultItems).toByteArray(Charsets.UTF_8)
            val encryptedData = ChaCha20Poly1305.encrypt(vaultsJsonAsByteArray, key, nonce)

            JSONObject().apply {
                put("version", 1)
                put("data", Base64.encode(encryptedData))
                put("salt", Base64.encode(salt))
                put("argon2id", JSONObject().apply {
                    put("m", Argon2id.DEFAULT_M)
                    put("t", Argon2id.DEFAULT_T)
                    put("p", Argon2id.DEFAULT_P)
                })
            }.toString()
        }.onFailure { e ->
            Logger.e("VaultModel", "Error backing up vault: ${e.stackTraceToString()}")
        }.getOrElse { "" }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Suppress("UNUSED_PARAMETER")
    private fun jsonAsVaultItems(version: Int, dataJson: String): List<VaultItem> {
        return runCatching {
            val jsonArray = try {
                JSONArray(dataJson)
            } catch (e: Exception) {
                Logger.e(
                    "VaultModel", "Failed to parse items JSON array: ${e.stackTraceToString()}"
                )
                return@runCatching emptyList<VaultItem>()
            }

            val list = mutableListOf<VaultItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue

                val item = VaultItem(
                    uuid = Uuid.parse(obj.getString("uuid")),
                    lastUpdated = obj.getLong("lastUpdated"),
                    name = obj.optString("name", ""),
                    issuer = obj.optString("issuer", ""),
                    note = obj.optString("note", ""),
                    secret = Base64.decode(obj.getString("secret")),
                    period = obj.optInt("period", 30),
                    digits = obj.getInt("digits"),
                    counter = obj.optLong("counter", 0),
                    otpType = OtpTypes.fromString(obj.getString("otpType")),
                    otpHashFunction = OtpHashFunctions.fromString(obj.getString("otpHashFunction")),
                    otpCardColor = OtpCardColors.fromString(obj.getString("otpCardColor"))
                )

                list.add(item)
            }

            list
        }.getOrElse { e ->
            Logger.e("VaultModel", "Error decrypting vault: ${e.stackTraceToString()}")
            emptyList()
        }
    }

    private fun decryptVault(context: Context, data: ByteArray): String {
        val rawData =
            context.secureCryptoViewModel.decrypt(data) ?: throw Exception("decrypt returned null")
        return String(rawData, Charsets.UTF_8)
    }

    fun readVault(context: Context): List<VaultItem>? {
        Logger.i("VaultModel", "readVault")

        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return emptyList()

        val json = file.readText(Charsets.UTF_8)
        if (json.isBlank()) return emptyList()

        return runCatching {
            val obj = JSONObject(json)

            val version = obj.getInt("version")
            val rawData = obj.getString("data").let { Base64.decode(it) }

            jsonAsVaultItems(version, decryptVault(context, rawData))
        }.getOrElse { e ->
            Logger.e("VaultModel", "Error reading vault: ${e.stackTraceToString()}")
            null
        }
    }

    fun restoreVault(password: String, json: String): List<VaultItem>? {
        Logger.i("VaultModel", "restoreVault")

        if (json.isBlank()) return emptyList()

        return runCatching {
            val obj = JSONObject(json)

            val version = obj.getInt("version")
            val rawData = obj.getString("data").let { Base64.decode(it) }
            val salt = obj.getString("salt").let { Base64.decode(it) }

            val argonObj = obj.getJSONObject("argon2id")
            val m = argonObj.getInt("m")
            val t = argonObj.getInt("t")
            val p = argonObj.getInt("p")

            val hash = Argon2id.get(
                password = password.toByteArray(Charsets.UTF_8),
                salt = salt,
                outLength = ChaCha20Poly1305.KEY_SIZE + ChaCha20Poly1305.NONCE_SIZE,
                m = m,
                t = t,
                p = p
            )

            val key = hash.copyOfRange(0, ChaCha20Poly1305.KEY_SIZE)
            val nonce = hash.copyOfRange(ChaCha20Poly1305.KEY_SIZE, hash.size)

            val data = ChaCha20Poly1305.decrypt(rawData, key, nonce) ?: return@runCatching null

            jsonAsVaultItems(version, String(data, Charsets.UTF_8))
        }.onFailure { e ->
            Logger.e("VaultModel", "Error restoring vault: ${e.stackTraceToString()}")
        }.getOrElse {
            emptyList()
        }
    }
}
