package gr.peptidetracker.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import gr.peptidetracker.app.i18n.t
import gr.peptidetracker.app.ui.premiumTextFieldColors

@Composable
fun PeptidePickerDialog(
    selectedPeptide: String,
    peptides: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val sortedPeptides = remember(peptides) {
        peptides.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
    }
    val filteredPeptides = remember(query, sortedPeptides) {
        val normalized = query.trim()
        if (normalized.isBlank()) {
            sortedPeptides
        } else {
            sortedPeptides.filter { it.contains(normalized, ignoreCase = true) }
        }
    }
    val selectedIndex = remember(selectedPeptide, sortedPeptides) {
        sortedPeptides.indexOfFirst { it.equals(selectedPeptide, ignoreCase = true) }
            .coerceAtLeast(0)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

    LaunchedEffect(query, filteredPeptides.size) {
        if (filteredPeptides.isNotEmpty()) {
            listState.scrollToItem(
                if (query.isBlank()) {
                    selectedIndex.coerceAtMost(filteredPeptides.lastIndex)
                } else {
                    0
                }
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 18.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            t("Επίλεξε πεπτίδιο"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            filteredPeptides.size.toString() + " " + t("πεπτίδια"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = t("Κλείσιμο")
                        )
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    placeholder = { Text(t("Αναζήτηση πεπτιδίου")) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = t("Καθαρισμός")
                                )
                            }
                        }
                    },
                    colors = premiumTextFieldColors()
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
                    )
                ) {
                    if (filteredPeptides.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    t("Δεν βρέθηκαν πεπτίδια."),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(
                                items = filteredPeptides,
                                key = { it }
                            ) { name ->
                                val selected = name.equals(
                                    selectedPeptide,
                                    ignoreCase = true
                                )
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSelect(name) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color.Transparent
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = 14.dp,
                                                vertical = 12.dp
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            name,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (selected) {
                                                FontWeight.ExtraBold
                                            } else {
                                                FontWeight.SemiBold
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (selected) {
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
