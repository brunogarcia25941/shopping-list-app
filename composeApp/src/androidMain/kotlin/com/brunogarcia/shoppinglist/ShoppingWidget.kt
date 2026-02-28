package com.brunogarcia.shoppinglist

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import android.annotation.SuppressLint

// 1. O Recetor
class ShoppingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ShoppingWidget()
}

// 2. Desenho Inteligente
@SuppressLint("RestrictedApi")
class ShoppingWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // --- 1. LER DADOS DA MEMÓRIA ---
        val settings = Settings()
        val familyCode = settings.getString("FAMILY_CODE", "")

        var itemsToBuy = emptyList<ShoppingItem>()
        if (familyCode.isNotEmpty()) {
            val cachedItems = settings.getString("CACHE_ITEMS_$familyCode", "")
            if (cachedItems.isNotEmpty()) {
                try {
                    // Transforma o texto guardado de volta em Lista de Objetos
                    val allItems = Json.decodeFromString<List<ShoppingItem>>(cachedItems)
                    // Filtra: Queremos apenas os que AINDA NÃO foram comprados!
                    itemsToBuy = allItems.filter { !it.isBought }
                } catch (e: Exception) {
                    // Erro silencioso (se a cache estiver vazia ou corrompida)
                }
            }
        }

        // --- 2. DESENHAR O WIDGET ---
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1C2541)) // A cor 'CardSurfaceBlue'
                    .padding(16.dp)
                    // Clicar em qualquer parte vazia do widget abre a App
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                // CABEÇALHO DO WIDGET
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (familyCode.isEmpty()) "🛒 Lista" else "🛒 $familyCode",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        )
                    )
                }

                // CORPO DO WIDGET
                if (familyCode.isEmpty()) {
                    Text(
                        text = "Abre a app para iniciar sessão.",
                        style = TextStyle(color = ColorProvider(Color.Gray))
                    )
                } else if (itemsToBuy.isEmpty()) {
                    Text(
                        text = "Tudo comprado! 🎉",
                        style = TextStyle(color = ColorProvider(Color.White))
                    )
                } else {
                    // Lista das coisas que faltam comprar
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {

                        // Pegamos apenas nos primeiros 8 para não encher demasiado o widget
                        val displayItems = itemsToBuy.take(8)

                        items(displayItems) { item ->
                            val quantidade = if (item.quantity > 1) "${item.quantity}x " else ""
                            Text(
                                text = "• $quantidade${item.name}",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = ColorProvider(Color.White)
                                ),
                                modifier = GlanceModifier.padding(vertical = 4.dp)
                            )
                        }

                        // Se houver mais coisas, mostramos o "+ X itens" no fundo
                        if (itemsToBuy.size > 8) {
                            item {
                                Text(
                                    text = "+ ${itemsToBuy.size - 8} itens",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(Color(0xFF5BC0BE)) // 'PrimaryAccent'
                                    ),
                                    modifier = GlanceModifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}