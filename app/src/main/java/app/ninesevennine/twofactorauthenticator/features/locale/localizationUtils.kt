package app.ninesevennine.twofactorauthenticator.features.locale

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.ninesevennine.twofactorauthenticator.localeViewModel

@Composable
fun localizedString(@StringRes resourceId: Int): String {
    val context = LocalContext.current
    val localeViewModel = context.localeViewModel

    val locale = localeViewModel.effectiveLocale

    return remember(locale) {
        localeViewModel.getLocalizedString(context, resourceId)
    }
}

//@Composable
//fun localizedStringFormatted(@StringRes resourceId: Int, vararg formatArgs: Any): String {
//    val context = LocalContext.current
//    val localeViewModel = context.localeViewModel
//
//    val locale = localeViewModel.effectiveLocale
//
//    return remember(locale) {
//        localeViewModel.getLocalizedString(context, resourceId, formatArgs)
//    }
//}
//
//@Composable
//fun localizedPluralString(@PluralsRes resourceId: Int, count: Int): String {
//    val context = LocalContext.current
//    val localeViewModel = context.localeViewModel
//
//    val locale = localeViewModel.effectiveLocale
//
//    return remember(locale) {
//        localeViewModel.getQuantityString(context, resourceId, count)
//    }
//}
//
//@Composable
//fun localizedPluralStringFormatted(@PluralsRes resourceId: Int, count: Int, vararg formatArgs: Any): String {
//    val context = LocalContext.current
//    val localeViewModel = context.localeViewModel
//
//    val locale = localeViewModel.effectiveLocale
//
//    return remember(locale) {
//        localeViewModel.getQuantityString(context, resourceId, count, *formatArgs)
//    }
//}