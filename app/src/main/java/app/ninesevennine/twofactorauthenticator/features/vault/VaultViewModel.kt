package app.ninesevennine.twofactorauthenticator.features.vault

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ninesevennine.twofactorauthenticator.BuildConfig
import app.ninesevennine.twofactorauthenticator.features.externalvault.GoogleAuthenticator
import app.ninesevennine.twofactorauthenticator.features.otp.HOTP
import app.ninesevennine.twofactorauthenticator.features.otp.OtpHashFunctions
import app.ninesevennine.twofactorauthenticator.features.otp.OtpTypes
import app.ninesevennine.twofactorauthenticator.features.otp.otpParser
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.OtpCardColors
import app.ninesevennine.twofactorauthenticator.utils.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class VaultViewModel() : ViewModel() {
    private val _items = mutableListOf<VaultItem>().toMutableStateList()
    val items: SnapshotStateList<VaultItem> = _items

    var isVaultLoaded: Boolean = false
        private set

    fun save(context: Context) {
        if (!isVaultLoaded) {
            Logger.e("VaultViewModel", "Vault not loaded, skipping save to prevent data loss")
            return
        }

        VaultModel.saveVault(context, _items.toList())
    }

    fun load(context: Context) {
        if (isVaultLoaded) return

        val items = VaultModel.readVault(context)
        if (items != null) {
            _items.clear()
            _items.addAll(items)
            isVaultLoaded = true
        }

        if (BuildConfig.DEBUG && _items.isEmpty() && isVaultLoaded) {
            otpParser("otpauth://totp/Google:user%40gmail.com?issuer=Google&secret=FLBGI3IGK2CKXLRC&algorithm=SHA1&digits=6&period=30")
                ?.let { addItem(it) }
            otpParser("otpauth://totp/Cloudflare:user%40gmail.com?issuer=Cloudflare&secret=DXTBJDXEL7IC4MV2&algorithm=SHA1&digits=6&period=30")
                ?.let { addItem(it) }
            otpParser("otpauth://totp/Wise?issuer=Wise&secret=WXORKSJAVBV4TIWT&algorithm=SHA1&digits=6&period=30")
                ?.let { addItem(it) }
            otpParser("otpauth://totp/Discord:user%40gmail.com?issuer=Discord&secret=VDOJPD4SCRO4DFIT&algorithm=SHA1&digits=6&period=30")
                ?.let { addItem(it) }
            otpParser("otpauth://hotp/Amazon:yourusername?secret=6AYUMD6MZNAI2RD4&issuer=Amazon&counter=0")
                ?.let { addItem(it) }
            otpParser("otpauth://totp/Hetzner:user%40example.com?secret=WMRMYOEBCTBO6TDQ&issuer=Hetzner&digits=10&period=60&algorithm=SHA512")
                ?.let { addItem(it.copy(otpCardColor = OtpCardColors.RED)) }
            otpParser("otpauth://totp/Mega?secret=AOKFKHDC2LZMNIRZ&algorithm=SHA1&digits=6&period=30")
                ?.let { addItem(it.copy(otpCardColor = OtpCardColors.PINK)) }
            otpParser("otpauth://totp/Posteo.de:user%40posteo.com?issuer=Posteo.de&secret=PPOD7MUGB2GBV7FV&algorithm=SHA1&digits=6&period=30")
                ?.let { addItem(it) }
        }
    }

    fun moveItem(from: Int, to: Int) {
        if (from == to) return
        val item = _items.removeAt(from)
        _items.add(to, item)
    }

    fun addItem(item: VaultItem) {
        _items.add(item.copy(uuid = Uuid.random()))
    }

    fun removeItemByUuid(uuid: Uuid) {
        Logger.i("VaultViewModel", "removeItemById")

        _items.removeAll { it.uuid == uuid }
    }

    fun getItemByUuid(uuid: Uuid): VaultItem? {
        return _items.find { it.uuid == uuid }
    }

    fun updateItemOrAdd(updatedItem: VaultItem) {
        Logger.i("VaultViewModel", "updateItemOrAdd")

        val index = _items.indexOfFirst { it.uuid == updatedItem.uuid }
        if (index != -1) {
            _items[index] = updatedItem
        } else {
            _items.add(updatedItem)
        }
    }

    fun incrementItemCounter(uuid: Uuid) {
        val index = _items.indexOfFirst { it.uuid == uuid }
        if (index != -1) {
            _items[index] = _items[index].copy(counter = _items[index].counter + 1)
        }
    }

    fun restoreVaultItems(vaultItems: List<VaultItem>) {
        var addedCount = 0
        var updatedCount = 0

        vaultItems.forEach { newItem ->
            val index = _items.indexOfFirst { it.uuid == newItem.uuid }

            if (index != -1) {
                val existingItem = _items[index]

                if (newItem.lastUpdated > existingItem.lastUpdated) {
                    _items[index] = newItem
                    updatedCount++
                }
            } else {
                _items.add(newItem)
                addedCount++
            }
        }

        if (addedCount > 0 || updatedCount > 0) {
            Logger.i(
                "VaultViewModel",
                "restoreVaultItems: $updatedCount item(s) updated, $addedCount item(s) added"
            )
        }
    }

    fun backupVault(password: String): String {
        return VaultModel.backupVault(_items.toList(), "979$password")
    }

    fun restoreVault(password: String, json: String): Boolean {
        VaultModel.restoreVault("979$password", json)?.let {
            restoreVaultItems(it)
            return true
        }

        return false
    }

    fun exportToGoogleAuth(): List<Bitmap> {
        return GoogleAuthenticator.exportVaultItems(_items.toList())
    }

    private val _currentTimeSeconds = MutableStateFlow(0L)
    val currentTimeSeconds: StateFlow<Long> = _currentTimeSeconds

    fun generateOtp(
        otpType: OtpTypes,
        otpHashFunction: OtpHashFunctions,
        secret: ByteArray,
        digits: Int,
        period: Int,
        count: Long,
        currentTimeSeconds: Long
    ): String {
        return when (otpType) {
            OtpTypes.TOTP -> HOTP.generate(
                otpHashFunction = otpHashFunction,
                secret = secret,
                digits = digits,
                counter = currentTimeSeconds / period
            )

            OtpTypes.HOTP -> HOTP.generate(
                otpHashFunction = otpHashFunction,
                secret = secret,
                digits = digits,
                counter = count
            )
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                @OptIn(ExperimentalTime::class)
                _currentTimeSeconds.value = Clock.System.now().epochSeconds

                delay(1000)
            }
        }
    }
}
