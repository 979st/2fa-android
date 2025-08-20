package app.ninesevennine.twofactorauthenticator.features.vault

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ninesevennine.twofactorauthenticator.BuildConfig
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
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class VaultViewModel(
    @Suppress("StaticFieldLeak")
    private val context: Context
) : ViewModel() {
    private val _items = mutableListOf<VaultItem>().toMutableStateList()
    val items: SnapshotStateList<VaultItem> = _items

    fun moveItem(from: Int, to: Int) {
        if (from == to) return
        val item = _items.removeAt(from)
        _items.add(to, item)
    }

    fun addItem(item: VaultItem) {
        _items.add(item.copy(id = Random.nextLong()))
    }

    fun removeItemById(id: Long) {
        Logger.i("VaultViewModel", "removeItemById")

        _items.removeAll { it.id == id }
        saveVault()
    }

    fun getItemById(id: Long): VaultItem? {
        return _items.find { it.id == id }
    }

    fun updateItemOrAdd(updatedItem: VaultItem) {
        Logger.i("VaultViewModel", "updateItemOrAdd")

        val index = _items.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            _items[index] = updatedItem
        } else {
            _items.add(updatedItem)
        }

        saveVault()
    }

    fun saveVault() {
        VaultModel.saveVault(context, _items.toList())
    }

    fun backupVault(password: String): String {
        return VaultModel.backupVault(_items.toList(), "979$password")
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
        _items.addAll(VaultModel.readVault(context))

        if (BuildConfig.DEBUG && _items.isEmpty()) {
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

        viewModelScope.launch {
            while (true) {
                @OptIn(ExperimentalTime::class)
                _currentTimeSeconds.value = Clock.System.now().epochSeconds

                delay(1000)
            }
        }
    }
}