package com.example.shoppinglist

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.shoppinglist.ShoppingClient
import com.example.shoppinglist.ShoppingItem

@Composable
fun App() {
    // Estado para guardar a lista
    var items by remember { mutableStateOf(listOf<ShoppingItem>()) }
    val client = remember { ShoppingClient() }

    // LaunchedEffect corre quando a app abre (tipo o onCreate)
    LaunchedEffect(Unit) {
        try {
            // Vai buscar ao servidor
            items = client.getItems()
        } catch (e: Exception) {
            println("ERRO: ${e.message}")
        }
    }

    // UI Simples: Uma lista
    LazyColumn {
        item {
            Text("Lista de Compras (${items.size} itens)")
        }
        items(items) { item ->
            Text("- ${item.name} (${item.quantity})")
        }
    }
}