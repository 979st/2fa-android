package app.ninesevennine.twofactorauthenticator.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.configViewModel
import app.ninesevennine.twofactorauthenticator.features.locale.localizedPluralStringFormatted
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.bottomappbar.MainAppBar
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.OtpCard
import app.ninesevennine.twofactorauthenticator.utils.Constants
import app.ninesevennine.twofactorauthenticator.vaultViewModel
import kotlinx.serialization.Serializable
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import kotlin.math.max
import kotlin.uuid.ExperimentalUuidApi

@Serializable
object MainScreenRoute

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val navController = LocalNavController.current
    val configuration = LocalConfiguration.current
    val vaultViewModel = context.vaultViewModel

    if (context.configViewModel.otpauthUrl.isNotEmpty()) {
        navController.navigate(EditScreenRoute(Constants.ONEUUIDSTR))
    }

    val items = vaultViewModel.items

    var query by remember { mutableStateOf("") }

    val filteredItems by remember(items, query) {
        derivedStateOf {
            val q = query.trim()
            if (q.isEmpty()) {
                items
            } else {
                items.filter {
                    it.name.lowercase().contains(q) || it.issuer.lowercase().contains(q)
                }
            }
        }
    }

    val lazyGridState = rememberLazyGridState()

    val reorderState = rememberReorderableLazyGridState(
        lazyGridState = lazyGridState,
        onMove = { from, to -> vaultViewModel.moveItem(from.index, to.index) }
    )

    val isFiltering = query.trim().isNotEmpty()

    @SuppressLint("ConfigurationScreenWidthHeight")
    val columnCount = remember(configuration.screenWidthDp) {
        max(1, configuration.screenWidthDp / 400)
    }

    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Fixed(columnCount),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            start = 8.dp,
            end = 8.dp,
            bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding() + 96.dp
        ),
        horizontalArrangement = Arrangement.Center
    ) {
        @OptIn(ExperimentalUuidApi::class)
        itemsIndexed(filteredItems, key = { _, item -> item.uuid }) { _, item ->
            ReorderableItem(reorderState, key = item.uuid) { dragging ->
                OtpCard(
                    modifier = if (isFiltering) Modifier else Modifier.longPressDraggableHandle(),
                    item = item,
                    dragging = dragging
                )
            }
        }

        if (filteredItems.isNotEmpty()) {
            item(span = { GridItemSpan(columnCount) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = localizedPluralStringFormatted(
                            R.plurals.main_list_plural_text_showing_x_entries,
                            filteredItems.size,
                            filteredItems.size
                        ),
                        fontFamily = InterVariable,
                        color = colors.onBackground,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (items.isNotEmpty() && filteredItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = localizedString(R.string.main_list_text_no_entries_found),
                fontFamily = InterVariable,
                color = colors.onBackground,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )
        }
    }

    MainAppBar(
        onSearch = { q ->
            query = q.lowercase()
        },
        onSettings = {
            navController.navigate(SettingsScreenRoute)
        },
        onAdd = {
            navController.navigate(EditScreenRoute(Constants.NILUUIDSTR))
        },
        onAddLongPress = {
            navController.navigate(EditScreenRoute(Constants.ONEUUIDSTR))
        }
    )
}