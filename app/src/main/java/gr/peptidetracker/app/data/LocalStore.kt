package gr.peptidetracker.app.data

import android.content.Context

data class TrackerEntry(val id: Long, val peptide: String, val amount: String, val note: String, val createdAt: Long)
data class ProgressEntry(val id: Long, val weight: Double, val waist: Double?, val note: String, val createdAt: Long)
data class InventoryEntry(val id: Long, val peptide: String, val vialMg: Double, val quantity: Int, val batch: String)

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("peptide_tracker", Context.MODE_PRIVATE)
    private fun safe(s: String)=s.replace("|","/").replace("\n"," ")
    fun darkMode()=prefs.getBoolean("dark",false)
    fun setDarkMode(value:Boolean)=prefs.edit().putBoolean("dark",value).apply()
    fun favorites()=prefs.getStringSet("favorites",emptySet()).orEmpty()
    fun toggleFavorite(id:String){ val n=favorites().toMutableSet(); if(!n.add(id)) n.remove(id); prefs.edit().putStringSet("favorites",n).apply() }
    fun entries():List<TrackerEntry> = prefs.getStringSet("entries",emptySet()).orEmpty().mapNotNull{val p=it.split("|",limit=5);if(p.size==5)TrackerEntry(p[0].toLong(),p[1],p[2],p[3],p[4].toLong())else null}.sortedByDescending{it.createdAt}
    fun add(peptide:String,amount:String,note:String){val now=System.currentTimeMillis();val n=prefs.getStringSet("entries",emptySet()).orEmpty().toMutableSet();n+="$now|${safe(peptide)}|${safe(amount)}|${safe(note)}|$now";prefs.edit().putStringSet("entries",n).apply()}
    fun deleteEntry(id:Long){prefs.edit().putStringSet("entries",prefs.getStringSet("entries",emptySet()).orEmpty().filterNot{it.startsWith("$id|")}.toSet()).apply()}
    fun progress():List<ProgressEntry> = prefs.getStringSet("progress",emptySet()).orEmpty().mapNotNull{val p=it.split("|",limit=5);if(p.size==5)ProgressEntry(p[0].toLong(),p[1].toDouble(),p[2].takeIf(String::isNotBlank)?.toDouble(),p[3],p[4].toLong())else null}.sortedByDescending{it.createdAt}
    fun addProgress(weight:Double,waist:Double?,note:String){val now=System.currentTimeMillis();val n=prefs.getStringSet("progress",emptySet()).orEmpty().toMutableSet();n+="$now|$weight|${waist?:""}|${safe(note)}|$now";prefs.edit().putStringSet("progress",n).apply()}
    fun inventory():List<InventoryEntry> = prefs.getStringSet("inventory",emptySet()).orEmpty().mapNotNull{val p=it.split("|",limit=5);if(p.size>=5)InventoryEntry(p[0].toLong(),p[1],p[2].toDouble(),p[3].toInt(),p[4])else null}.sortedBy{it.peptide}
    fun addInventory(peptide:String,vial:Double,quantity:Int,batch:String){val now=System.currentTimeMillis();val n=prefs.getStringSet("inventory",emptySet()).orEmpty().toMutableSet();n+="$now|${safe(peptide)}|$vial|$quantity|${safe(batch)}";prefs.edit().putStringSet("inventory",n).apply()}
    fun deleteInventory(id:Long){prefs.edit().putStringSet("inventory",prefs.getStringSet("inventory",emptySet()).orEmpty().filterNot{it.startsWith("$id|")}.toSet()).apply()}
}
