package app.ninesevennine.twofactorauthenticator.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.themeViewModel

@Composable
fun <T> SectionDropDownSingleChoice(
    title: String,
    options: Array<T>,
    selectedOption: T,
    onSelectionChange: (T) -> Unit,
    getDisplayText: (T) -> String,
    error: Boolean = false
) {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 4.dp, bottom = 6.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = colors.onBackground,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.W700,
                    fontFamily = InterVariable,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(color = colors.background, shape = RoundedCornerShape(10.dp))
                        .border(
                            width = 1.dp,
                            color = if (error) colors.error else colors.outline,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getDisplayText(selectedOption),
                            modifier = Modifier.weight(1f),
                            color = colors.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W700,
                            fontFamily = InterVariable,
                            maxLines = 1
                        )

                        Spacer(Modifier.width(8.dp))

                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = colors.onBackground
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(color = colors.background)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = getDisplayText(option),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W700,
                                    fontFamily = InterVariable,
                                    color = colors.onBackground
                                )
                            },
                            onClick = {
                                onSelectionChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}