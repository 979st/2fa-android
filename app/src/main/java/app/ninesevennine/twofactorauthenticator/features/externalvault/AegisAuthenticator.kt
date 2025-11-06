package app.ninesevennine.twofactorauthenticator.features.externalvault

import app.ninesevennine.twofactorauthenticator.features.otp.OtpHashFunctions
import app.ninesevennine.twofactorauthenticator.features.otp.OtpTypes
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.utils.Base32
import org.bouncycastle.crypto.generators.SCrypt
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex
import org.json.JSONArray
import org.json.JSONObject
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object AegisAuthenticator {
    fun importUnencrypted(vaultJson: String): List<VaultItem>? {
        return try {
            val vault = JSONObject(vaultJson)

            // slots will be null if it requires a password
            val header = vault.getJSONObject("header")
            if (!header.isNull("slots")) {
                return null
            }

            val db = vault.getJSONObject("db")
            val entries = db.getJSONArray("entries")

            parseEntries(entries)
        } catch (_: Exception) {
            null
        }
    }

    fun importEncrypted(vaultJson: String, password: String): List<VaultItem>? {
        return try {
            val vault = JSONObject(vaultJson)

            val header = vault.getJSONObject("header")
            val encryptedDb = vault.getString("db")

            val slots = header.getJSONArray("slots")
            var slot: JSONObject? = null

            for (i in 0 until slots.length()) {
                val s = slots.getJSONObject(i)
                val type = s.getInt("type")
                if (type == 1 || type == 2) {
                    slot = s
                    break
                }
            }

            if (slot == null) return null

            val salt = Hex.decode(slot.getString("salt"))
            val n = slot.getInt("n")
            val r = slot.getInt("r")
            val p = slot.getInt("p")

            val derivedKey = SCrypt.generate(
                password.toByteArray(Charsets.UTF_8),
                salt,
                n,
                r,
                p,
                32
            )

            val encryptedMasterKey = Hex.decode(slot.getString("key"))
            val keyParams = slot.getJSONObject("key_params")
            val keyNonce = Hex.decode(keyParams.getString("nonce"))
            val keyTag = Hex.decode(keyParams.getString("tag"))

            val masterKey = decryptAesGcm(
                encryptedMasterKey,
                derivedKey,
                keyNonce,
                keyTag
            ) ?: return null

            val encryptedDbBytes = Base64.decode(encryptedDb)
            val dbParams = header.getJSONObject("params")
            val dbNonce = Hex.decode(dbParams.getString("nonce"))
            val dbTag = Hex.decode(dbParams.getString("tag"))

            val decryptedDbBytes = decryptAesGcm(
                encryptedDbBytes,
                masterKey,
                dbNonce,
                dbTag
            ) ?: return null

            val dbString = String(decryptedDbBytes, Charsets.UTF_8)
            val db = JSONObject(dbString)
            val entries = db.getJSONArray("entries")

            parseEntries(entries)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseEntries(entries: JSONArray): List<VaultItem> {
        val items = mutableListOf<VaultItem>()

        for (i in 0 until entries.length()) {
            try {
                val entry = entries.getJSONObject(i)
                val info = entry.getJSONObject("info")

                val otpType = when (entry.getString("type")) {
                    "totp" -> OtpTypes.TOTP
                    "hotp" -> OtpTypes.HOTP
                    else -> continue
                }

                val hashFunction = when (info.getString("algo")) {
                    "SHA1" -> OtpHashFunctions.SHA1
                    "SHA256" -> OtpHashFunctions.SHA256
                    "SHA512" -> OtpHashFunctions.SHA512
                    else -> continue
                }

                val secret = Base32.decode(info.getString("secret")) ?: continue

                val item = VaultItem(
                    uuid = try {
                        Uuid.parse(entry.getString("uuid"))
                    } catch (_: Exception) {
                        Uuid.random()
                    },
                    name = entry.optString("name", ""),
                    issuer = entry.optString("issuer", ""),
                    note = entry.optString("note", ""),
                    secret = secret,
                    period = info.optInt("period", 30),
                    digits = info.optInt("digits", 6),
                    counter = info.optLong("counter", 0),
                    otpType = otpType,
                    otpHashFunction = hashFunction
                )

                items.add(item)
            } catch (_: Exception) {
                continue
            }
        }

        return items
    }

    private fun decryptAesGcm(
        ciphertext: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        tag: ByteArray
    ): ByteArray? {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(key, "AES")
            val gcmSpec = GCMParameterSpec(128, nonce)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

            val input = ciphertext + tag
            cipher.doFinal(input)
        } catch (_: Exception) {
            null
        }
    }
}