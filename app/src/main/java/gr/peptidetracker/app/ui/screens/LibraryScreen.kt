package gr.peptidetracker.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.data.PeptideInfo
import gr.peptidetracker.app.data.peptideCatalog
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.ElectricViolet
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.NeonRose
import gr.peptidetracker.app.ui.PremiumTopBar
import gr.peptidetracker.app.ui.StoreVialImage
import gr.peptidetracker.app.ui.premiumButtonColors
import gr.peptidetracker.app.ui.premiumFilterChipColors
import gr.peptidetracker.app.ui.premiumTextFieldColors

@Composable
fun LibraryScreen(
    store: LocalStore,
    imageIndex: Map<String, String>,
    onOpenPeptide: (PeptideInfo) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Όλα") }
    var favorites by remember { mutableStateOf(store.favorites()) }

    val categories = remember {
        listOf("Όλα") + peptideCatalog.map { it.category }.distinct()
    }

    val visible = peptideCatalog.filter { peptide ->
        val matchesQuery =
            query.isBlank() ||
                peptide.name.contains(query, ignoreCase = true) ||
                peptide.alias.contains(query, ignoreCase = true) ||
                peptide.category.contains(query, ignoreCase = true)

        val matchesCategory = category == "Όλα" || peptide.category == category
        matchesQuery && matchesCategory
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 18.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PremiumTopBar(
                title = "Βιβλιοθήκη",
                subtitle = "Δες τι είναι κάθε πεπτίδιο, σε τι έχει μελετηθεί και πόσο ισχυρά είναι τα διαθέσιμα δεδομένα."
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                colors = premiumTextFieldColors(),
                placeholder = { Text("Αναζήτηση πεπτιδίου") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Καθαρισμός")
                        }
                    }
                }
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 18.dp)
            ) {
                items(categories) { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = { category = item },
                        label = { Text(item) },
                        colors = premiumFilterChipColors()
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (visible.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("Δεν βρέθηκε αποτέλεσμα", fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Δοκίμασε διαφορετικό όνομα ή κατηγορία.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visible, key = { it.id }) { peptide ->
                    PeptideGridCard(
                        peptide = peptide,
                        imageIndex = imageIndex,
                        favorite = peptide.id in favorites,
                        onFavorite = {
                            store.toggleFavorite(peptide.id)
                            favorites = store.favorites()
                        },
                        onClick = { onOpenPeptide(peptide) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeptideGridCard(
    peptide: PeptideInfo,
    imageIndex: Map<String, String>,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.height(252.dp),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ElectricBlue.copy(alpha = 0.10f),
                                ElectricViolet.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                StoreVialImage(
                    productKey = peptide.id,
                    imageIndex = imageIndex,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentDescription = peptide.name
                )
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Αγαπημένο",
                        tint = if (favorite) NeonRose else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    peptide.category.uppercase(),
                    color = ElectricCyan,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    peptide.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    peptide.status,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PeptideDetailScreen(
    peptide: PeptideInfo,
    store: LocalStore,
    imageIndex: Map<String, String>,
    onBack: () -> Unit,
    onOpenCalculator: (PeptideInfo) -> Unit = {},
    onAddInventory: (PeptideInfo) -> Unit = {}
) {
    val context = LocalContext.current
    var favorite by remember { mutableStateOf(peptide.id in store.favorites()) }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumTopBar(
                title = peptide.name,
                subtitle = peptide.alias,
                onBack = onBack
            )
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    ElectricViolet.copy(alpha = 0.20f),
                                    ElectricBlue.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    StoreVialImage(
                        productKey = peptide.id,
                        imageIndex = imageIndex,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp),
                        contentDescription = peptide.name
                    )

                    IconButton(
                        onClick = {
                            store.toggleFavorite(peptide.id)
                            favorite = !favorite
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Αγαπημένο",
                            tint = if (favorite) NeonRose else Color.White
                        )
                    }

                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.42f))
                            .padding(horizontal = 11.dp, vertical = 7.dp)
                    ) {
                        Text(
                            peptide.category,
                            color = ElectricCyan,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Κατάσταση",
                        color = ElectricBlue,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        peptide.status,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Τελευταίος έλεγχος περιεχομένου: " + peptide.lastReviewed,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onOpenCalculator(peptide) },
                    modifier = Modifier.weight(1f),
                    colors = premiumButtonColors()
                ) {
                    Icon(Icons.Rounded.Calculate, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Υπολογιστής", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { onAddInventory(peptide) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Inventory2, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Απόθεμα", maxLines = 1)
                }
            }
        }

        item {
            DetailInfoCard(
                title = "Τι είναι",
                text = peptide.overview,
                icon = Icons.Rounded.Science,
                accent = ElectricBlue
            )
        }

        item {
            DetailInfoCard(
                title = "Σε τι έχει μελετηθεί",
                text = peptide.research,
                icon = Icons.AutoMirrored.Rounded.ManageSearch,
                accent = ElectricCyan
            )
        }

        item {
            DetailInfoCard(
                title = "Πώς δρα",
                text = peptide.mechanism,
                icon = Icons.Rounded.Hub,
                accent = ElectricViolet
            )
        }

        item {
            DetailInfoCard(
                title = "Τι πρέπει να γνωρίζεις",
                text = peptide.warning,
                icon = Icons.Rounded.WarningAmber,
                accent = Color(0xFFFFBE6B)
            )
        }

        item {
            Text(
                "Πηγές & αναζητήσεις βιβλιογραφίας",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(peptide.studies) { study ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(study.url))
                    )
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElectricBlue.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Article,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            study.title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            study.source,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = "Άνοιγμα",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Οι πληροφορίες εξηγούν τα διαθέσιμα δεδομένα με απλό τρόπο. Δεν αποτελούν ιατρική συμβουλή ή εξατομικευμένη οδηγία χρήσης.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun DetailInfoCard(
    title: String,
    text: String,
    icon: ImageVector,
    accent: Color
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent
                )
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
