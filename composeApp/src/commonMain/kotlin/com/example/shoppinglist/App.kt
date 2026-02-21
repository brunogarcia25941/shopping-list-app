package com.example.shoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// 1. A NOSSA NOVA PALETA DE CORES (Premium Dark Blue)
val BackgroundNavy = Color(0xFF0B132B)    // Fundo da app (Azul muito escuro)
val CardSurfaceBlue = Color(0xFF1C2541)   // Fundo dos cartões
val PrimaryAccent = Color(0xFF5BC0BE)     // Ciano vibrante para botões e vistos
val TextWhite = Color(0xFFF0F6F6)         // Texto principal
val TextGray = Color(0xFFA0AAB2)          // Texto secundário (Quantidades)
val ErrorRed = Color(0xFF8B0000)          // Vermelho escuro para o fundo de apagar

// 2. CONFIGURAÇÃO DO TEMA
private val ModernDarkBlueScheme = darkColorScheme(
    primary = PrimaryAccent,
    background = BackgroundNavy,
    surface = BackgroundNavy,
    surfaceVariant = CardSurfaceBlue,
    onPrimary = BackgroundNavy,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextWhite,
    errorContainer = ErrorRed,
    onErrorContainer = Color.White
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val client = remember { ShoppingClient() }
    val items = remember { mutableStateListOf<ShoppingItem>() }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        try {
            items.clear()
            items.addAll(client.getItems())
        } catch (e: Exception) {
            println("Erro inicial: ${e.message}")
        }

        client.listenForUpdates().collect { command ->
            if (command == "REFRESH") {
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

    // 3. ENVOLVER A APP NO NOSSO TEMA PERSONALIZADO
    MaterialTheme(
        colorScheme = ModernDarkBlueScheme,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp), // Cartões bastante arredondados
            large = RoundedCornerShape(24.dp)   // Dialogs arredondados
        )
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Compras em Família",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BackgroundNavy
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = PrimaryAccent,
                    contentColor = BackgroundNavy,
                    shape = RoundedCornerShape(50) // Botão 100% circular
                ) {
                    // ÍCONES ROUNDED EM VEZ DE DEFAULT
                    Icon(Icons.Rounded.Add, contentDescription = "Adicionar", modifier = Modifier.size(28.dp))
                }
            },
            containerColor = BackgroundNavy // Fundo geral da app
        ) { innerPadding ->

            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Espaço extra entre cartões
            ) {
                items(items, key = { it.id }) { item ->

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                scope.launch {
                                    var jaDesfez = false
                                    try {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        client.deleteItem(item.id)

                                        val result = snackbarHostState.showSnackbar(
                                            message = "${item.name} apagado",
                                            actionLabel = "Desfazer",
                                            duration = SnackbarDuration.Short
                                        )

                                        if (result == SnackbarResult.ActionPerformed && !jaDesfez) {
                                            jaDesfez = true
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            client.addItem(item.copy(id = ""))
                                        }
                                    } catch (e: Exception) {
                                        println("Erro: ${e.message}")
                                    }
                                }
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                Color.Transparent

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    // O fundo do Swipe tem de ter o mesmo arredondamento do cartão
                                    .background(color, RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete, // Ícone arredondado
                                    contentDescription = "Apagar",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(end = 24.dp).size(28.dp)
                                )
                            }
                        },
                        content = {
                            // CARTÃO COM EFEITOS
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurfaceBlue),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Sombra/Efeito de flutuação
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.isBought,
                                        onCheckedChange = { isChecked ->
                                            scope.launch {
                                                client.updateItem(item.copy(isBought = isChecked))
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = PrimaryAccent,
                                            checkmarkColor = BackgroundNavy
                                        )
                                    )

                                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            textDecoration = if (item.isBought) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                            color = if (item.isBought) TextGray else TextWhite
                                        )
                                        if (item.quantity > 1) {
                                            Text(
                                                text = "Qtd: ${item.quantity}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextGray
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

        if (showDialog) {
            AddItemDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, quantity ->
                    scope.launch {
                        val newItem = ShoppingItem(name = name, quantity = quantity.toIntOrNull() ?: 1)
                        try {
                            client.addItem(newItem)
                            showDialog = false
                        } catch (e: Exception) {
                            println("Erro: ${e.message}")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurfaceBlue, // Fundo do pop-up
        titleContentColor = PrimaryAccent,
        textContentColor = TextWhite,
        title = { Text("Novo Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome (ex: Arroz)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent,
                        unfocusedTextColor = TextWhite,
                        focusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent,
                        unfocusedTextColor = TextWhite,
                        focusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, quantity) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = BackgroundNavy),
                shape = RoundedCornerShape(50) // Botão redondo
            ) {
                Text("Adicionar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextGray)
            ) {
                Text("Cancelar")
            }
        }
    )
}