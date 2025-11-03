package app.ninesevennine.twofactorauthenticator.ui.elements.bottomappbar

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.configViewModel
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.themeViewModel

@Composable
fun MainAppBar(
    onSearch: (String) -> Unit,
    onSettings: () -> Unit,
    onAdd: () -> Unit,
    onAddLongPress: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val layoutDirection = LocalLayoutDirection.current

    val colors = context.themeViewModel.colors
    val primaryColor = colors.onPrimaryContainer

    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val cutoutLeftPadding =
        WindowInsets.displayCutout.asPaddingValues().calculateLeftPadding(layoutDirection)
    val cutoutRightPadding =
        WindowInsets.displayCutout.asPaddingValues().calculateRightPadding(layoutDirection)

    val isKeyboardOpen = imeBottomPadding > navBottomPadding + 4.dp + 56.dp
    val bottomPadding = if (isKeyboardOpen) imeBottomPadding else navBottomPadding

    if (context.configViewModel.values.enableFocusSearch) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    fun triggerFeedback(type: HapticFeedbackType = HapticFeedbackType.TextHandleMove) {
        haptic.performHapticFeedback(type)
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingValues(bottom = if (!isKeyboardOpen) bottomPadding + 4.dp else 0.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = if (!isKeyboardOpen) 500.dp else Dp.Infinity)
                .fillMaxWidth()
                .padding(
                    start = if (isKeyboardOpen) cutoutLeftPadding else bottomPadding + 4.dp,
                    end = if (isKeyboardOpen) cutoutRightPadding else bottomPadding + 4.dp
                )
                .height(if (isKeyboardOpen) 56.dp + bottomPadding else 56.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                .clip(
                    if (isKeyboardOpen) {
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    } else {
                        RoundedCornerShape(28.dp)
                    }
                )
                .background(colors.primaryContainer),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchTextField(
                    query = query,
                    onQueryChange = { new ->
                        query = new
                        onSearch(new)
                    },
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        triggerFeedback()
                        onSearch(query)
                    },
                    focusRequester = focusRequester,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(primaryColor)

                SettingsButton(
                    onClick = {
                        triggerFeedback()
                        onSettings()
                    },
                    primaryColor = primaryColor,
                )

                VerticalDivider(primaryColor)

                AddButton(
                    onTap = {
                        triggerFeedback()
                        onAdd()
                    },
                    onLongPress = {
                        triggerFeedback(HapticFeedbackType.LongPress)
                        onAddLongPress()
                    },
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = InterVariable,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            color = primaryColor
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier
                    .size(26.dp)
                    .offset(x = 8.dp),
                tint = primaryColor
            )
        },
        placeholder = {
            Text(
                text = localizedString(R.string.main_bottom_bar_textfield_hint_text_search),
                fontFamily = InterVariable,
                letterSpacing = (-0.2).sp,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = primaryColor
            )
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = primaryColor,
            unfocusedTextColor = primaryColor,
            disabledTextColor = primaryColor.copy(alpha = 0.6f),
            cursorColor = primaryColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = primaryColor,
            unfocusedLeadingIconColor = primaryColor,
            disabledLeadingIconColor = primaryColor.copy(alpha = 0.6f),
            focusedPlaceholderColor = primaryColor.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = primaryColor.copy(alpha = 0.7f),
            disabledPlaceholderColor = primaryColor.copy(alpha = 0.5f)
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
private fun VerticalDivider(color: Color) {
    Spacer(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(color.copy(alpha = 0.1f))
    )
}

@Composable
private fun SettingsButton(
    onClick: () -> Unit,
    primaryColor: Color,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = primaryColor
        )
    }
}

@Composable
private fun AddButton(
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    primaryColor: Color
) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(48.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = primaryColor
        )
    }
}