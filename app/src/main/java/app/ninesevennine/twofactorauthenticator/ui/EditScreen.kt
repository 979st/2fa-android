package app.ninesevennine.twofactorauthenticator.ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
import app.ninesevennine.twofactorauthenticator.features.otp.OtpHashFunctions
import app.ninesevennine.twofactorauthenticator.features.otp.OtpTypes
import app.ninesevennine.twofactorauthenticator.features.qrscanner.QRScannerView
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.ItemColorOption
import app.ninesevennine.twofactorauthenticator.ui.elements.WideTitle
import app.ninesevennine.twofactorauthenticator.ui.elements.bottomappbar.EditAppBar
import app.ninesevennine.twofactorauthenticator.ui.elements.dropdown.DropDownSingleChoice
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.OtpCard
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.OtpCardColors
import app.ninesevennine.twofactorauthenticator.ui.elements.textfields.NumbersOnlyTextField
import app.ninesevennine.twofactorauthenticator.ui.elements.textfields.SingleLineTextField
import app.ninesevennine.twofactorauthenticator.ui.elements.textfields.TextField2fa
import app.ninesevennine.twofactorauthenticator.ui.elements.widebutton.WideButton
import app.ninesevennine.twofactorauthenticator.ui.elements.widebutton.WideButtonError
import app.ninesevennine.twofactorauthenticator.utils.Base32
import app.ninesevennine.twofactorauthenticator.utils.Constants
import app.ninesevennine.twofactorauthenticator.vaultViewModel
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

    var item by remember {
        mutableStateOf(
            when (uuidString) {
                Constants.NILUUIDSTR -> {
                    VaultItem()
                }

                Constants.ONEUUIDSTR -> {
                    VaultItem(uuid = Uuid.random())
                }

                else -> {
                    vaultViewModel.getItemByUuid(Uuid.parse(uuidString)) ?: VaultItem()
                }
            }
        )
    }

    if (item.uuid == Constants.NILUUID) {
        QRScannerView(
            onEnterManually = {
                item = item.copy(uuid = Uuid.random())
            },
            onVaultItemChange = { newItem ->
                item = newItem
            }
        )

        return
    }

    val navController = LocalNavController.current
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
            OtpCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                item = item,
                dragging = false,
                enableEditing = false
            )

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
                            if (digits < 4 || digits > 10) {
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