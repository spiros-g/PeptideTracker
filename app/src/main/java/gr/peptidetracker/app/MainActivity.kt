package gr.peptidetracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.LocalStore
import gr.peptidetracker.app.domain.PeptideCalculator
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        setContent { App(LocalStore(this)) }
    }
}

private val Green = Color(0xFF087F5B)
private data class Destination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable private fun App(store: LocalStore) {
    var selected by remember { mutableIntStateOf(0) }
    val destinations = listOf(Destination("Αρχική", Icons.Default.Home), Destination("Πεπτίδια", Icons.Default.Search), Destination("Υπολογιστής", Icons.Default.Calculate), Destination("Tracker", Icons.Default.History), Destination("Περισσότερα", Icons.Default.MoreHoriz))
    MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = Color(0xFF0B7285))) {
        Scaffold(bottomBar = { NavigationBar { destinations.forEachIndexed { i, d -> NavigationBarItem(selected = selected == i, onClick = { selected = i }, icon = { Icon(d.icon, null) }, label = { Text(d.label, maxLines = 1) }) } } }) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding)) { when(selected) { 0 -> Home { selected = 2 }; 1 -> Library(); 2 -> Calculators(); 3 -> Tracker(store); else -> More() } }
        }
    }
}

@Composable private fun Page(title: String, content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); content() }

@Composable private fun Home(openCalculator: () -> Unit) = Page("Peptide Tracker GR") {
    Text("Οι υπολογισμοί και οι καταγραφές σου, οργανωμένα και διαθέσιμα offline.")
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Γρήγορος υπολογισμός", fontWeight = FontWeight.Bold); Text("Μετέτρεψε ποσότητα, όγκο και μονάδες σύριγγας χωρίς προτεινόμενα πρωτόκολλα."); Button(onClick = openCalculator) { Text("Άνοιγμα υπολογιστή") } } }
    Text("Ιατρική επισήμανση", fontWeight = FontWeight.Bold)
    Text("Η εφαρμογή παρέχει πληροφορίες και μαθηματικές μετατροπές. Δεν αποτελεί ιατρική συμβουλή και δεν προτείνει δοσολογίες ή θεραπεία.")
}

private val peptides = listOf("Retatrutide" to "Υπό κλινική διερεύνηση", "Tirzepatide" to "Εγκεκριμένο φάρμακο για συγκεκριμένες ενδείξεις", "Semaglutide" to "Εγκεκριμένο φάρμακο για συγκεκριμένες ενδείξεις", "BPC-157" to "Μη εγκεκριμένη ερευνητική ένωση", "TB-500" to "Μη εγκεκριμένη ερευνητική ένωση", "GHK-Cu" to "Πεπτίδιο χαλκού", "Tesamorelin" to "Εγκεκριμένο για συγκεκριμένη ένδειξη στις ΗΠΑ", "Ipamorelin" to "Μη εγκεκριμένη ερευνητική ένωση", "CJC-1295" to "Μη εγκεκριμένη ερευνητική ένωση", "Sermorelin" to "Ιστορική/περιορισμένη ρυθμιστική χρήση", "AOD-9604" to "Μη εγκεκριμένη ερευνητική ένωση", "MOTS-c" to "Υπό έρευνα", "SS-31" to "Υπό κλινική διερεύνηση", "KPV" to "Προκλινική έρευνα", "Epitalon" to "Περιορισμένα κλινικά δεδομένα")

@Composable private fun Library() = Page("Βιβλιοθήκη πεπτιδίων") {
    var query by remember { mutableStateOf("") }; OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Αναζήτηση") }, leadingIcon = { Icon(Icons.Default.Search, null) })
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(peptides.filter { it.first.contains(query, true) }) { (name, status) -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(name, fontWeight = FontWeight.Bold); Text(status, style = MaterialTheme.typography.bodySmall) } } } }
}

@Composable private fun NumberField(label: String, value: String, onValue: (String)->Unit) = OutlinedTextField(value, { onValue(it.replace(',', '.')) }, Modifier.fillMaxWidth(), label = { Text(label) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Calculators() = Page("Υπολογιστής") {
    var mode by remember { mutableIntStateOf(0) }; SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) { listOf("Ανασύσταση", "Δόση", "Αντίστροφος").forEachIndexed { i, s -> SegmentedButton(selected = mode == i, onClick = { mode = i }, shape = SegmentedButtonDefaults.itemShape(i, 3)) { Text(s) } } }
    var vial by remember { mutableStateOf("10") }; var diluent by remember { mutableStateOf("2") }; var target by remember { mutableStateOf("250") }; var units by remember { mutableStateOf("5") }; var syringe by remember { mutableIntStateOf(100) }; var result by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }
    NumberField("Ποσότητα φιαλιδίου (mg)", vial) { vial = it }; NumberField("Διαλύτης (ml)", diluent) { diluent = it }
    if (mode == 1) NumberField("Ποσότητα στόχος (mcg)", target) { target = it }; if (mode == 2) NumberField("Μονάδες σύριγγας", units) { units = it }
    Row(verticalAlignment = Alignment.CenterVertically) { Text("Σύριγγα:"); Spacer(Modifier.width(12.dp)); FilterChip(syringe == 100, { syringe = 100 }, { Text("U-100") }); Spacer(Modifier.width(8.dp)); FilterChip(syringe == 40, { syringe = 40 }, { Text("U-40") }) }
    Button(modifier = Modifier.fillMaxWidth(), onClick = { try { val v=vial.toDouble(); val d=diluent.toDouble(); result = when(mode) { 0 -> PeptideCalculator.concentration(v,false,d,syringe).let { "${PeptideCalculator.format(it.mgPerMl)} mg/ml\n${PeptideCalculator.format(it.mcgPerMl)} mcg/ml\n${PeptideCalculator.format(it.mcgPerUnit)} mcg ανά μονάδα" }; 1 -> PeptideCalculator.dose(v,d,target.toDouble(),false,syringe).let { "${PeptideCalculator.format(it.concentrationMgPerMl)} mg/ml\n${PeptideCalculator.format(it.volumeMl)} ml\n${PeptideCalculator.format(it.syringeUnits)} μονάδες U-$syringe" }; else -> PeptideCalculator.reverse(v,d,units.toDouble(),syringe).let { "${PeptideCalculator.format(it.volumeMl)} ml\n${PeptideCalculator.format(it.amountMcg)} mcg\n${PeptideCalculator.format(it.amountMg)} mg" } }; error="" } catch(_: Exception) { error="Συμπλήρωσε έγκυρες θετικές τιμές." } }) { Text("Υπολογισμός") }
    if(error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error); if(result.isNotEmpty()) Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)) { Text(result, Modifier.padding(18.dp), style=MaterialTheme.typography.titleMedium) }
    Text("Μαθηματική μετατροπή μόνο — η ποσότητα στόχος ορίζεται αποκλειστικά από τον χρήστη.", style=MaterialTheme.typography.bodySmall)
}

@Composable private fun Tracker(store: LocalStore) = Page("Tracker") {
    var peptide by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }; var entries by remember { mutableStateOf(store.entries()) }
    OutlinedTextField(peptide,{peptide=it},Modifier.fillMaxWidth(),label={Text("Πεπτίδιο")}); OutlinedTextField(amount,{amount=it},Modifier.fillMaxWidth(),label={Text("Ποσότητα και μονάδα")}); OutlinedTextField(note,{note=it},Modifier.fillMaxWidth(),label={Text("Σημείωση (προαιρετική)")})
    Button(enabled=peptide.isNotBlank()&&amount.isNotBlank(),onClick={store.add(peptide,amount,note); entries=store.entries(); peptide="";amount="";note=""}) { Text("Αποθήκευση καταγραφής") }
    if(entries.isEmpty()) Text("Δεν υπάρχουν καταγραφές ακόμη.") else LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) { items(entries,key={it.id}) { e -> Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(e.peptide,fontWeight=FontWeight.Bold); Text(e.amount); if(e.note.isNotBlank()) Text(e.note); Text(DateFormat.getDateTimeInstance().format(Date(e.createdAt)),style=MaterialTheme.typography.bodySmall) }; IconButton({store.delete(e.id);entries=store.entries()}) { Icon(Icons.Default.Delete,"Διαγραφή") } } } } }
}

@Composable private fun More() = Page("Περισσότερα") { Text("Απόρρητο",fontWeight=FontWeight.Bold); Text("Οι καταγραφές αποθηκεύονται μόνο στη συσκευή. Δεν αποστέλλονται σε διακομιστή ή σε διαφημιστικά συστήματα."); HorizontalDivider(); Text("Έκδοση 1.0.0"); Text("Θέμα: ακολουθεί τις βασικές ρυθμίσεις της συσκευής σε επόμενη έκδοση.") }
