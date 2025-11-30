package app.ninesevennine.twofactorauthenticator.ui

import android.view.SoundEffectConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.configViewModel
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
import app.ninesevennine.twofactorauthenticator.features.otp.OtpHashFunctions
import app.ninesevennine.twofactorauthenticator.features.otp.OtpTypes
import app.ninesevennine.twofactorauthenticator.features.otp.otpParser
import app.ninesevennine.twofactorauthenticator.features.qrscanner.QRScannerView
import app.ninesevennine.twofactorauthenticator.features.qrscanner.ZXingQrUri
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.ItemColorOption
import app.ninesevennine.twofactorauthenticator.ui.elements.WideTitle
import app.ninesevennine.twofactorauthenticator.ui.elements.bottomappbar.EditAppBar
import app.ninesevennine.twofactorauthenticator.ui.elements.dropdown.DropDownSingleChoice
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.ClassicOtpCard
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.MinimalOtpCard
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.OtpCardColors
import app.ninesevennine.twofactorauthenticator.ui.elements.textfields.NumbersOnlyTextField
import app.ninesevennine.twofactorauthenticator.ui.elements.textfields.SingleLineTextField
import app.ninesevennine.twofactorauthenticator.ui.elements.textfields.TextField2fa
import app.ninesevennine.twofactorauthenticator.ui.elements.widebutton.WideButton
import app.ninesevennine.twofactorauthenticator.ui.elements.widebutton.WideButtonError
import app.ninesevennine.twofactorauthenticator.utils.Base32
import app.ninesevennine.twofactorauthenticator.utils.Constants
import app.ninesevennine.twofactorauthenticator.vaultViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class EditScreenRoute(
    val uuidString: String
)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun EditScreen(uuidString: String) {
    val context = LocalContext.current
    val vaultViewModel = context.vaultViewModel
    val configViewModel = context.configViewModel
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val navController = LocalNavController.current

    var item by remember {
        mutableStateOf(
            when (uuidString) {
                Constants.NILUUIDSTR -> {
                    VaultItem()
                }

                Constants.ONEUUIDSTR -> {
                    if (configViewModel.otpauthUrl.isEmpty()) {
                        VaultItem(uuid = Uuid.random())
                    } else {
                        val url = configViewModel.otpauthUrl
                        configViewModel.otpauthUrl = ""
                        otpParser(url) ?: VaultItem(uuid = Uuid.random())
                    }
                }

                else -> {
                    vaultViewModel.getItemByUuid(Uuid.parse(uuidString)) ?: VaultItem()
                }
            }
        )
    }

    val pickScope = rememberCoroutineScope()
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            pickScope.launch {
                val url = ZXingQrUri.decode(it, context.contentResolver)
                if (url != null) {
                    otpParser(url)?.let { vaultItem ->
                        item = vaultItem.copy()
                    }
                }
            }
        }
    }

    if (item.uuid == Constants.NILUUID) {
        QRScannerView(
            onQrCodeScanned = {
                otpParser(it)?.let { parsedItem ->
                    @Suppress("AssignedValueIsNeverRead") // piece of shit
                    item = parsedItem
                }
            },
            bottomBar = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0x99000000))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                navController.popBackStack()
                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0x99000000))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                item = item.copy(uuid = Uuid.random())
                            }
                            .padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = localizedString(R.string.qr_scanner_button_text_enter_manually),
                            fontFamily = InterVariable,
                            color = Color.White,
                            fontWeight = FontWeight.W700,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0x99000000))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                view.playSoundEffect(SoundEffectConstants.CLICK)

                                pickImage.launch("image/*")
                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ImageSearch,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        )

        return
    }

    val colors = context.themeViewModel.colors

    var secretInput by remember { mutableStateOf(Base32.encode(item.secret)) }
    var secretError by remember { mutableStateOf(item.secret.isEmpty()) }

    var periodInput by remember { mutableStateOf(item.period.toString()) }
    var periodError by remember { mutableStateOf(false) }

    var digitsInput by remember { mutableStateOf(item.digits.toString()) }
    var digitsError by remember { mutableStateOf(false) }

    var counterInput by remember { mutableStateOf(item.counter.toString()) }
    var counterError by remember { mutableStateOf(false) }

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val bottomPadding = if (imeBottom > 0.dp) imeBottom else navBottom

    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxHeight()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = bottomPadding
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (configViewModel.values.cardStyle) {
                    0 -> {
                        ClassicOtpCard(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            item = item,
                            dragging = false,
                            enableEditing = false
                        )
                    }
                    1 -> {
                        MinimalOtpCard(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .width(264.dp),
                            item = item,
                            dragging = false,
                            enableEditing = false
                        )
                    }
                }
            }

            WideTitle(text = localizedString(R.string.edit_screen_basic_info_title))

            SingleLineTextField(
                modifier = Modifier.fillMaxWidth(),
                value = item.name,
                onValueChange = { item = item.copy(name = it) },
                placeholder = localizedString(R.string.edit_field_name_hint)
            )

            SingleLineTextField(
                modifier = Modifier.fillMaxWidth(),
                value = item.issuer,
                onValueChange = { item = item.copy(issuer = it) },
                placeholder = localizedString(R.string.edit_field_issuer_hint)
            )

            SingleLineTextField(
                modifier = Modifier.fillMaxWidth(),
                value = item.note,
                onValueChange = { item = item.copy(note = it) },
                placeholder = localizedString(R.string.edit_field_note_hint)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ItemColorOption(item.otpCardColor, OtpCardColors.RED) {
                    item = item.copy(otpCardColor = OtpCardColors.RED)
                }

                ItemColorOption(item.otpCardColor, OtpCardColors.ORANGE) {
                    item = item.copy(otpCardColor = OtpCardColors.ORANGE)
                }

                ItemColorOption(item.otpCardColor, OtpCardColors.PINK) {
                    item = item.copy(otpCardColor = OtpCardColors.PINK)
                }

                ItemColorOption(item.otpCardColor, OtpCardColors.BLUE) {
                    item = item.copy(otpCardColor = OtpCardColors.BLUE)
                }

                ItemColorOption(item.otpCardColor, OtpCardColors.GREEN) {
                    item = item.copy(otpCardColor = OtpCardColors.GREEN)
                }

                ItemColorOption(item.otpCardColor, OtpCardColors.BROWN) {
                    item = item.copy(otpCardColor = OtpCardColors.BROWN)
                }
            }

            WideButtonError(
                modifier = Modifier.fillMaxWidth(),
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = colors.onErrorContainer
                    )
                },
                label = localizedString(R.string.edit_button_delete),
                onClick = {
                    showDeleteDialog = true
                }
            )

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = localizedString(R.string.edit_delete_alert_title),
                            fontFamily = InterVariable,
                            color = colors.onBackground,
                            fontWeight = FontWeight.W700,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = localizedString(R.string.edit_delete_alert_text),
                            fontFamily = InterVariable,
                            color = colors.onBackground,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    },
                    containerColor = colors.background,
                    confirmButton = {
                        WideButton(
                            label = localizedString(R.string.common_yes),
                            color = colors.primary,
                            textColor = colors.onPrimary,
                            onClick = {
                                showDeleteDialog = false
                                navController.popBackStack()
                                vaultViewModel.removeItemByUuid(item.uuid)
                            }
                        )
                    },
                    dismissButton = {
                        WideButton(
                            label = localizedString(R.string.common_cancel),
                            onClick = { showDeleteDialog = false }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            WideTitle(text = localizedString(R.string.edit_screen_advanced_title))

            TextField2fa(
                modifier = Modifier.fillMaxWidth(),
                value = secretInput,
                onValueChange = {
                    secretInput = it.trim()
                    val decoded = Base32.decode(it.trim())
                    if (decoded != null) {
                        secretError = false
                        item = item.copy(secret = decoded)
                    } else {
                        secretError = true
                    }
                },
                placeholder = localizedString(R.string.edit_field_secret_hint),
                isError = secretError
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropDownSingleChoice(
                    modifier = Modifier.weight(1f),
                    options = OtpTypes.entries.toTypedArray(),
                    selectedOption = item.otpType,
                    onSelectionChange = { item = item.copy(otpType = it) },
                    getDisplayText = { it.value }
                )

                Spacer(Modifier.width(8.dp))

                DropDownSingleChoice(
                    modifier = Modifier.weight(1f),
                    options = OtpHashFunctions.entries.toTypedArray(),
                    selectedOption = item.otpHashFunction,
                    onSelectionChange = { item = item.copy(otpHashFunction = it) },
                    getDisplayText = { it.value }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.otpType == OtpTypes.HOTP) {
                    NumbersOnlyTextField(
                        modifier = Modifier.weight(1f),
                        value = counterInput,
                        onValueChange = {
                            counterInput = it.trim()
                            val counter = it.trim().toLongOrNull()
                            if (counter != null) {
                                if (counter < 0) {
                                    counterError = true
                                } else {
                                    counterError = false
                                    item = item.copy(counter = counter)
                                }
                            } else {
                                counterError = true
                            }
                        },
                        placeholder = "0+",
                        trailingText = localizedString(R.string.edit_unit_counter),
                        isError = counterError
                    )
                } else {
                    NumbersOnlyTextField(
                        modifier = Modifier.weight(1f),
                        value = periodInput,
                        onValueChange = {
                            periodInput = it.trim()
                            val period = it.trim().toIntOrNull()
                            if (period != null) {
                                if (period < 10) {
                                    periodError = true
                                } else {
                                    periodError = false
                                    item = item.copy(period = period)
                                }
                            } else {
                                periodError = true
                            }
                        },
                        placeholder = "10+",
                        trailingText = localizedString(R.string.edit_unit_seconds),
                        isError = periodError
                    )
                }

                Spacer(Modifier.width(8.dp))

                NumbersOnlyTextField(
                    modifier = Modifier.weight(1f),
                    value = digitsInput,
                    onValueChange = {
                        digitsInput = it.trim()
                        val digits = it.trim().toIntOrNull()
                        if (digits != null) {
                            if (digits !in 4..10) {
                                digitsError = true
                            } else {
                                digitsError = false
                                item = item.copy(digits = digits)
                            }
                        } else {
                            digitsError = true
                        }
                    },
                    placeholder = "4-10",
                    trailingText = localizedString(R.string.edit_unit_digits),
                    isError = digitsError
                )
            }

            Spacer(Modifier.height(96.dp))
        }
    }

    EditAppBar(
        onCancel = {
            navController.popBackStack()
        },
        onDone = {
            if (secretError || periodError || digitsError) {
                return@EditAppBar
            }

            if (item.otpType == OtpTypes.HOTP && counterError) {
                return@EditAppBar
            }

            item.name = item.name.trim()
            item.issuer = item.issuer.trim()
            item.note = item.note.trim()

            @OptIn(ExperimentalTime::class)
            item.lastUpdated = Clock.System.now().epochSeconds

            vaultViewModel.updateItemOrAdd(item)
            vaultViewModel.save(context)
            navController.popBackStack()
        }
    )
}