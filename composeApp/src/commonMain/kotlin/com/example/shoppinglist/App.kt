package com.example.shoppinglist

import androidx.compose.foundation.background
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

    // NOVO: O controlador da Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

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
        // NOVO: Adicionamos a SnackbarHost ao Scaffold para ele saber onde a desenhar
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
            items(items, key = { it.id }) { item -> // Importante: usar o ID como chave para evitar bugs visuais

                // 1. Estado do Swipe
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            scope.launch {
                                // Evita o bug de clicar 2 vezes rápido
                                var jaDesfez = false

                                try {
                                    // Limpa qualquer Snackbar que já estivesse no ecrã para não criar "fila"
                                    snackbarHostState.currentSnackbarData?.dismiss()

                                    // A. Apaga imediatamente no servidor
                                    client.deleteItem(item.id)

                                    // B. Mostra a Snackbar e espera pela resposta
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${item.name} apagado",
                                        actionLabel = "Desfazer",
                                        duration = SnackbarDuration.Short
                                    )

                                    // C. Se o utilizador clicou no botão e ainda não tinha desfeito
                                    if (result == SnackbarResult.ActionPerformed && !jaDesfez) {
                                        jaDesfez = true // Bloqueia cliques duplos

                                        // Força o pop-up a desaparecer instantaneamente
                                        snackbarHostState.currentSnackbarData?.dismiss()

                                        // Voltamos a adicionar o mesmo item à base de dados
                                        client.addItem(item.copy(id = ""))
                                    }
                                } catch (e: Exception) {
                                    println("Erro ao apagar: ${e.message}")
                                }
                            }
                            true // Confirma que o swipe aconteceu
                        } else {
                            false // Ignora outros movimentos
                        }
                    }
                )

                // 2. O Componente de Deslizar
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        // O que aparece por trás quando deslizar (Fundo Vermelho com Lixo)
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.background

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp) // Igual ao padding do Card
                                .background(color),
                            contentAlignment = Alignment.CenterEnd // Ícone alinhado à direita
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Apagar",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(end = 24.dp)
                            )
                        }
                    },
                    content = {
                        // 3. O Cartão Original (agora sem o botão de lixo)
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isBought,
                                    onCheckedChange = { isChecked ->
                                        scope.launch {
                                            client.updateItem(item.copy(isBought = isChecked))
                                        }
                                    }
                                )

                                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        textDecoration = if (item.isBought) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                        color = if (item.isBought) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.quantity > 1) {
                                        Text(
                                            text = "Qtd: ${item.quantity}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (item.isBought) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                            }
                        }
                    }
                )
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