package gr.peptidetracker.app.ui.screens

import gr.peptidetracker.app.i18n.t

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.ElectricViolet
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.NeonRose
import gr.peptidetracker.app.ui.premiumButtonColors
import gr.peptidetracker.app.ui.premiumTextButtonColors

private data class OnboardingPage(
    val title: String,
    val summary: String,
    val features: List<String>,
    val icon: ImageVector,
    val accent: Color
)

@Composable
fun OnboardingScreen(
    imageIndex: Map<String, String>,
    onFinish: () -> Unit
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                title = t("Υπολογιστής ανασύστασης & μονάδων"),
                summary = t("Κάνε γρήγορα τις μαθηματικές μετατροπές που χρειάζεσαι από τα στοιχεία που βάζεις εσύ."),
                features = listOf(
                    t("Μετατροπή ποσότητας σε mL και μονάδες σύριγγας, αλλά και αντίστροφα."),
                    t("Επιλογές χωρητικότητας σύριγγας και έλεγχος αν το αποτέλεσμα χωράει στη σύριγγα."),
                    t("Αποθήκευση υπολογισμών για να τους ξαναφορτώνεις αργότερα.")
                ),
                icon = Icons.Rounded.Calculate,
                accent = ElectricBlue
            ),
            OnboardingPage(
                title = t("Βιβλιοθήκη πεπτιδίων"),
                summary = t("Βρες γρήγορα πληροφορίες για τα πεπτίδια που σε ενδιαφέρουν χωρίς να ψάχνεις σε διαφορετικά σημεία."),
                features = listOf(
                    t("Αναζήτηση στη βιβλιοθήκη και γρήγορη πρόσβαση στα προφίλ."),
                    t("Αγαπημένα για να κρατάς μπροστά όσα χρησιμοποιείς συχνότερα."),
                    t("Πηγές και σύνδεσμοι βιβλιογραφίας μέσα από κάθε προφίλ.")
                ),
                icon = Icons.Rounded.Search,
                accent = ElectricCyan
            ),
            OnboardingPage(
                title = t("Ημερολόγιο χρήσεων"),
                summary = t("Κατέγραψε τι έχεις ήδη χρησιμοποιήσει ώστε να έχεις καθαρό ιστορικό."),
                features = listOf(
                    t("Ημερομηνία, ώρα, ποσότητα, μονάδα, σημείο και προσωπικές σημειώσεις."),
                    t("Προβολή των καταγραφών σου σε ιστορικό και ημερολόγιο."),
                    t("Σύνδεση μιας καταγραφής με απόθεμα ώστε να αφαιρείται αυτόματα η αντίστοιχη ποσότητα.")
                ),
                icon = Icons.AutoMirrored.Rounded.EventNote,
                accent = ElectricBlue
            ),
            OnboardingPage(
                title = t("Απόθεμα & ενεργό φιαλίδιο"),
                summary = t("Οργάνωσε τα φιαλίδιά σου και δες άμεσα τι έχεις διαθέσιμο και τι χρησιμοποιείς τώρα."),
                features = listOf(
                    t("Ποσότητα αποθέματος, ενεργό φιαλίδιο και υπόλοιπο περιεχόμενο."),
                    t("Lot/batch, προμηθευτής, ημερομηνία αγοράς και ημερομηνία λήξης."),
                    t("Προειδοποιήσεις λήξης και άνοιγμα του υπολογιστή απευθείας από το φιαλίδιο.")
                ),
                icon = Icons.Rounded.Inventory2,
                accent = ElectricViolet
            ),
            OnboardingPage(
                title = t("Μετρήσεις, στατιστικά & υπενθυμίσεις"),
                summary = t("Παρακολούθησε την πορεία των προσωπικών σου καταγραφών και οργάνωσε τις δικές σου υπενθυμίσεις."),
                features = listOf(
                    t("Αποθήκευση βάρους, μέσης και σημειώσεων με ιστορικό μετρήσεων."),
                    t("Στατιστικά για χρήσεις, απόθεμα, ενεργά φιαλίδια και μεταβολές μετρήσεων."),
                    t("Υπενθυμίσεις με ημερομηνία, ώρα και επανάληψη που ορίζεις εσύ.")
                ),
                icon = Icons.Rounded.QueryStats,
                accent = NeonRose
            ),
            OnboardingPage(
                title = t("Δεδομένα, ιδιωτικότητα & προσαρμογή"),
                summary = t("Κράτα τον έλεγχο των δεδομένων και της εμφάνισης της εφαρμογής."),
                features = listOf(
                    t("Τοπική αποθήκευση, απλό ή κρυπτογραφημένο backup, επαναφορά και εξαγωγή CSV."),
                    t("Προαιρετικό κλείδωμα εφαρμογής, ρυθμίσεις ιδιωτικότητας ειδοποιήσεων και ασφαλείς επίσημες ενημερώσεις."),
                    t("Ελληνικά ή Αγγλικά, φωτεινό ή σκοτεινό θέμα και δικά σου προσαρμοσμένα πεπτίδια.")
                ),
                icon = Icons.Rounded.Backup,
                accent = ElectricCyan
            )
        )
    }
    var page by remember { mutableIntStateOf(0) }
    val current = pages[page]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Peptide Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                t("Όλα τα βασικά εργαλεία του Peptide Tracker, εξηγημένα σε λίγα βήματα πριν ξεκινήσεις."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(current.accent.copy(alpha = 0.14f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            current.icon,
                            contentDescription = null,
                            tint = current.accent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            t("Βήμα ") + (page + 1) + " / " + pages.size,
                            color = current.accent,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            current.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    current.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    current.features.forEach { feature ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 7.dp)
                                    .size(7.dp)
                                    .background(current.accent, CircleShape)
                            )
                            Text(
                                feature,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.indices.forEach { index ->
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == page) 10.dp else 7.dp)
                            .background(
                                if (index == page) ElectricCyan
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
                                RoundedCornerShape(99.dp)
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (page < pages.lastIndex) page++ else onFinish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = premiumButtonColors()
            ) {
                Text(
                    if (page == pages.lastIndex) t("Ξεκίνα") else t("Συνέχεια"),
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (page < pages.lastIndex) {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    colors = premiumTextButtonColors()
                ) {
                    Text(t("Παράλειψη"))
                }
            } else {
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}
