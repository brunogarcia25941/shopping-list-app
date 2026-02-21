package com.example.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val client = remember { ShoppingClient() }
    // Usamos mutableStateList para ser mais fácil adicionar coisas dinamicamente
    val items = remember { mutableStateListOf<ShoppingItem>() }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 1. Carregar lista inicial E ligar o WebSocket
    LaunchedEffect(Unit) {
        // Carregar a primeira vez
        try {
            items.clear()
            items.addAll(client.getItems())
        } catch (e: Exception) {
            println("Erro ao carregar inicial: ${e.message}")
        }

        // Ficar à escuta de ordens do servidor
        client.listenForUpdates().collect { command ->
            if (command == "REFRESH") {
                println("O servidor mandou atualizar a lista!")
                try {
                    val freshItems = client.getItems()
                    items.clear()
                    items.addAll(freshItems)
                } catch (e: Exception) {
                    println("Erro ao recarregar: ${e.message}")
                }
            }
        }
    }
    // 3. Estrutura Visual (Material 3)
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lista de Compras") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { innerPadding ->

        // 4. A Lista
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(items.size) { index ->
                val item = items[index]
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Qtd: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                try {
                                    client.deleteItem(item.id)
                                    // Não precisamos de mexer na lista aqui,
                                    // porque o servidor vai responder com "REFRESH" e a app atualiza-se sozinha!
                                } catch (e: Exception) {
                                    println("Erro ao apagar: ${e.message}")
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // 5. O Pop-up para adicionar (Dialog)
    if (showDialog) {
        AddItemDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, quantity ->
                scope.launch {
                    val newItem = ShoppingItem(name = name, quantity = quantity.toIntOrNull() ?: 1)
                    try {
                        client.addItem(newItem) // Só envia. O servidor avisa de volta via WebSocket.
                        showDialog = false
                    } catch (e: Exception) {
                        println("Erro: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome (ex: Arroz)") }
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, quantity) }) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}