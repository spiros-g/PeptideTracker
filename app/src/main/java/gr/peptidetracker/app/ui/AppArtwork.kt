package gr.peptidetracker.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

@Composable
fun VialArtwork(name: String, id: String, modifier: Modifier = Modifier) {
    val liquid = if (id == "ghkcu") Color(0xFF2587F4) else Color(0xFFF3F6FA)
    val colors = listOf(Color(0xFF276EF1), Color(0xFF7457E8), Color(0xFF0BA897), Color(0xFFE65A79))
    val accent = colors[kotlin.math.abs(id.hashCode()) % colors.size]
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width; val h = size.height
            drawRoundRect(Color(0xFF9AA9B8), Offset(w*.29f,h*.02f), Size(w*.42f,h*.17f), CornerRadius(w*.07f))
            drawRoundRect(Color(0xFFE8EEF4), Offset(w*.18f,h*.16f), Size(w*.64f,h*.78f), CornerRadius(w*.13f))
            drawRoundRect(Color.White.copy(.75f), Offset(w*.23f,h*.2f), Size(w*.11f,h*.68f), CornerRadius(w*.05f))
            drawRoundRect(liquid, Offset(w*.23f,h*.7f), Size(w*.54f,h*.17f), CornerRadius(w*.06f))
            drawRoundRect(accent, Offset(w*.22f,h*.35f), Size(w*.56f,h*.27f), CornerRadius(w*.035f))
        }
        Text(name.take(7).uppercase(), color=Color.White, fontSize=7.sp, fontWeight=FontWeight.ExtraBold, modifier=Modifier.align(Alignment.Center))
    }
}

@Composable
fun MoleculeArtwork(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier) {
        val p=listOf(Offset(size.width*.16f,size.height*.55f),Offset(size.width*.4f,size.height*.2f),Offset(size.width*.75f,size.height*.31f),Offset(size.width*.82f,size.height*.72f),Offset(size.width*.4f,size.height*.8f))
        listOf(0 to 1,1 to 2,2 to 3,3 to 4,4 to 0,1 to 4).forEach{(a,b)->drawLine(color.copy(.72f),p[a],p[b],size.width*.055f,StrokeCap.Round)}
        p.forEach{drawCircle(color,size.width*.1f,it)}
    }
}

@Composable
fun SyringeArtwork(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier) { rotate(-32f) {
        drawRoundRect(color.copy(.12f),Offset(size.width*.16f,size.height*.35f),Size(size.width*.61f,size.height*.29f),CornerRadius(16f),style=Stroke(size.width*.055f))
        drawLine(color,Offset(size.width*.07f,size.height*.5f),Offset(size.width*.16f,size.height*.5f),size.width*.055f,StrokeCap.Round)
        drawLine(color,Offset(size.width*.77f,size.height*.5f),Offset(size.width*.96f,size.height*.5f),size.width*.03f,StrokeCap.Round)
        repeat(4){i->drawLine(color.copy(.75f),Offset(size.width*(.29f+i*.11f),size.height*.38f),Offset(size.width*(.29f+i*.11f),size.height*.46f),size.width*.022f)}
    } }
}

@Composable
fun EmptyArtwork(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier) {
        drawCircle(color.copy(.12f),size.minDimension*.45f,center)
        drawCircle(color,size.minDimension*.08f,Offset(size.width*.3f,size.height*.55f))
        drawCircle(color,size.minDimension*.08f,Offset(size.width*.5f,size.height*.3f))
        drawCircle(color,size.minDimension*.08f,Offset(size.width*.72f,size.height*.58f))
        drawLine(color,Offset(size.width*.3f,size.height*.55f),Offset(size.width*.5f,size.height*.3f),size.width*.05f,StrokeCap.Round)
        drawLine(color,Offset(size.width*.5f,size.height*.3f),Offset(size.width*.72f,size.height*.58f),size.width*.05f,StrokeCap.Round)
    }
}
