package com.mehmetbozkurt.questlog

import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.mehmetbozkurt.questlog.core.designsystem.theme.QuestLogTheme
import com.mehmetbozkurt.questlog.core.designsystem.theme.Spacing
import com.mehmetbozkurt.questlog.core.designsystem.theme.extendedColors
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            QuestLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ThemePreviewScreen()
                }
            }
        }
        Log.d("QuestLog", "Auth hazir: ${FirebaseAuth.getInstance().app.name}")
        FirebaseFirestore.getInstance()
            .collection("connectionTest")
            .document("ping")
            .set(mapOf("at" to System.currentTimeMillis()))
            .addOnSuccessListener { Log.d("QuestLog", "Firestore yazma BASARILI") }
            .addOnFailureListener { Log.e("QuestLog", "Firestore HATA: ${it.message}") }
    }
}

@Composable
fun ThemePreviewScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Spacer(Modifier.height(Spacing.xxl))

        Text("QuestLog", style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary)
        Text("Curse of Strahd", style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        LogCardPreview(
            type = "QUEST",
            typeColor = MaterialTheme.extendedColors.typeQuest,
            title = "Barovia'ya Giriş",
            body = "Sisler dağıldığında köyün kapıları önündeydik. Kimse bizi beklemiyordu.",
            priority = "Yüksek",
            priorityColor = MaterialTheme.extendedColors.priorityHigh
        )
        LogCardPreview(
            type = "NPC",
            typeColor = MaterialTheme.extendedColors.typeNpc,
            title = "Madam Eva",
            body = "Vistani kampındaki kâhin. Tarokka destesiyle kaderi okuyor.",
            priority = "Orta",
            priorityColor = MaterialTheme.extendedColors.priorityMedium
        )
        LogCardPreview(
            type = "LORE",
            typeColor = MaterialTheme.extendedColors.typeLore,
            title = "Ravenloft'un Sisleri",
            body = "Sisler bir hapishane. İçeri girmek kolay, çıkmak imkansız.",
            priority = "Düşük",
            priorityColor = MaterialTheme.extendedColors.priorityLow
        )

        Spacer(Modifier.weight(1f))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Yeni Kayıt", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun LogCardPreview(
    type: String, typeColor: Color,
    title: String, body: String,
    priority: String, priorityColor: Color,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(8.dp).background(typeColor, CircleShape)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(type, style = MaterialTheme.typography.labelMedium, color = typeColor)
                Spacer(Modifier.weight(1f))
                Text(priority, style = MaterialTheme.typography.labelMedium, color = priorityColor)
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(title, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(Spacing.xs))
            Text(body, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Preview(showBackground = true, name = "Karanlık")
@Composable
private fun DarkPreview() {
    QuestLogTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) { ThemePreviewScreen() }
    }
}

@Preview(showBackground = true, name = "Aydınlık")
@Composable
private fun LightPreview() {
    QuestLogTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) { ThemePreviewScreen() }
    }
}