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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Inventory2
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
import gr.peptidetracker.app.ui.StoreVialImage
import gr.peptidetracker.app.ui.premiumButtonColors
import gr.peptidetracker.app.ui.premiumTextButtonColors

private data class OnboardingPage(
    val title: String,
    val text: String,
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
                t("Ανασύσταση χωρίς μπέρδεμα"),
                t("Βάλε τι γράφει το φιαλίδιο, πόσο διαλύτη πρόσθεσες και την ποσότητα που θέλεις να μετατρέψεις. Το app κάνει μόνο τη μαθηματική μετατροπή."),
                Icons.Rounded.Calculate,
                ElectricBlue
            ),
            OnboardingPage(
                t("Καθαρό ημερολόγιο χρήσεων"),
                t("Κράτα ημερομηνία, ώρα, ποσότητα, μονάδα και σημειώσεις ώστε να ξέρεις τι έχεις ήδη καταγράψει."),
                Icons.AutoMirrored.Rounded.EventNote,
                ElectricCyan
            ),
            OnboardingPage(
                t("Απόθεμα και ενεργό φιαλίδιο"),
                t("Παρακολούθησε πόσα φιαλίδια έχεις, ποιο είναι ενεργό και πόση ποσότητα απομένει σε αυτό."),
                Icons.Rounded.Inventory2,
                ElectricViolet
            )
        )
    }
    var page by remember { mutableIntStateOf(0) }
    val current = pages[page]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                "Peptide Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                t("Όλα τα βασικά εργαλεία σε μία καθαρή εφαρμογή που λειτουργεί τοπικά στη συσκευή."),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                StoreVialImage(
                    productKey = "retatrutide",
                    imageIndex = imageIndex,
                    modifier = Modifier.size(width = 118.dp, height = 176.dp)
                )
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(current.accent.copy(alpha = 0.14f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(current.icon, contentDescription = null, tint = current.accent)
                }
                Text(
                    current.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    current.text,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                if (index == page) ElectricCyan else Color.White.copy(alpha = 0.20f),
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
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}
