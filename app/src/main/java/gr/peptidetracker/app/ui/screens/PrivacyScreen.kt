package gr.peptidetracker.app.ui.screens

import gr.peptidetracker.app.i18n.t

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.ui.ElectricBlue
import gr.peptidetracker.app.ui.ElectricCyan
import gr.peptidetracker.app.ui.GlassCard
import gr.peptidetracker.app.ui.PremiumTopBar

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumTopBar(
                title = t("Ιδιωτικότητα & ασφάλεια"),
                subtitle = t("Πώς χειρίζεται η εφαρμογή τα δεδομένα σου."),
                onBack = onBack
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = ElectricCyan)
                    Text(t("Τοπική αποθήκευση"), fontWeight = FontWeight.ExtraBold)
                    Text(
                        t("Οι καταγραφές χρήσης, το απόθεμα, οι μετρήσεις, οι υπενθυμίσεις και οι αποθηκευμένοι υπολογισμοί τηρούνται στη συσκευή. Η εφαρμογή δεν απαιτεί λογαριασμό και δεν διαθέτει δικό της cloud sync."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = ElectricBlue)
                    Text("Backup", fontWeight = FontWeight.ExtraBold)
                    Text(
                        t("Το Android system cloud backup για τα δεδομένα της εφαρμογής είναι απενεργοποιημένο. Backup δημιουργείται μόνο όταν το επιλέξεις εσύ. Υπάρχει απλή JSON εξαγωγή και κρυπτογραφημένη εξαγωγή με κωδικό· για προσωπικές καταγραφές προτείνεται η κρυπτογραφημένη επιλογή."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = ElectricCyan)
                    Text(t("Χρήση εφαρμογής"), fontWeight = FontWeight.ExtraBold)
                    Text(
                        t("Το Peptide Tracker είναι εργαλείο καταγραφής, οργάνωσης και μαθηματικών μετατροπών. Δεν αποτελεί ιατροτεχνολογικό προϊόν, δεν κάνει διάγνωση και δεν παρέχει εξατομικευμένη ιατρική συμβουλή, θεραπευτικό πρωτόκολλο ή σύσταση δοσολογίας."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                t("Για δημοσίευση στο Google Play απαιτείται επιπλέον δημόσια προσβάσιμη πολιτική απορρήτου με στοιχεία υπευθύνου και επικοινωνίας."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
