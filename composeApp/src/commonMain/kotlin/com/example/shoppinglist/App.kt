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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.ui.text.style.TextAlign


// ============================================================================
// 1. A NOSSA NOVA PALETA DE CORES (Premium Dark Blue)
// ============================================================================
// Definimos as nossas cores personalizadas usando os códigos HEX.
val BackgroundNavy = Color(0xFF0B132B)    // Fundo da app (Azul muito escuro)
val CardSurfaceBlue = Color(0xFF1C2541)   // Fundo dos cartões (Um azul ligeiramente mais claro para destacar do fundo)
val PrimaryAccent = Color(0xFF5BC0BE)     // Ciano vibrante para botões principais e vistos (chama a atenção)
val TextWhite = Color(0xFFF0F6F6)         // Texto principal (Quase branco, menos agressivo para os olhos)
val TextGray = Color(0xFFA0AAB2)          // Texto secundário (Usado nas quantidades ou itens já comprados)
val ErrorRed = Color(0xFF8B0000)          // Vermelho escuro para o fundo do Swipe (Apagar)


@Composable
fun App() {
    val settings = remember { Settings() }

    // 1. Ler Código da Casa
    var loggedFamilyCode by remember {
        mutableStateOf(settings.getString("FAMILY_CODE", ""))
    }

    // 2. Ler Preferência de Tema (Por defeito é TRUE/Escuro)
    var isDarkTheme by remember {
        mutableStateOf(settings.getBoolean("IS_DARK_MODE", true))
    }

    // 3. Escolhe o esquema de cores com base na variável
    val currentScheme = if (isDarkTheme) ModernDarkBlueScheme else ModernLightScheme

    MaterialTheme(
        colorScheme = currentScheme,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(24.dp)
        )
    ) {
        if (loggedFamilyCode.isEmpty()) {
            LoginScreen(
                onEnter = { code ->
                    loggedFamilyCode = code
                    settings.putString("FAMILY_CODE", code)
                }
            )
        } else {
            // Passamos o tema e a função de mudar o tema para o ecrã da lista
            ShoppingListScreen(
                familyCode = loggedFamilyCode,
                isDarkTheme = isDarkTheme,
                onToggleTheme = {
                    val newState = !isDarkTheme
                    isDarkTheme = newState
                    settings.putBoolean("IS_DARK_MODE", newState) // Guarda na memória
                },
                onLogout = {
                    settings.remove("FAMILY_CODE")
                    loggedFamilyCode = ""
                }
            )
        }
    }
}

@Composable
fun LoginScreen(onEnter: (String) -> Unit) {
    var codeInput by remember { mutableStateOf("") }

    // Ecrã centrado
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Bem-vindo",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryAccent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Insere o código da tua família para entrares na lista partilhada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )

                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it.uppercase() }, // Força a ficar tudo em maiúsculas
                    label = { Text("Código da Casa") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (codeInput.isNotBlank()) onEnter(codeInput)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Entrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================================
// 2. CONFIGURAÇÃO DO TEMA
// ============================================================================
// O Material 3 funciona através de um "Color Scheme" (Esquema de cores).
// Aqui "ensinamos" o Compose a usar as nossas cores nos locais certos por defeito.
private val ModernDarkBlueScheme = darkColorScheme(
    primary = PrimaryAccent,
    background = BackgroundNavy,
    surface = BackgroundNavy,
    surfaceVariant = CardSurfaceBlue,
    onPrimary = BackgroundNavy,      // Cor da letra/ícone quando está por cima da cor 'primary'
    onBackground = TextWhite,        // Cor da letra quando está no fundo da app
    onSurface = TextWhite,
    onSurfaceVariant = TextWhite,
    errorContainer = ErrorRed,
    onErrorContainer = Color.White
)

// paleta clara
val LightBackground = Color(0xFFF4F7F6)   // Um cinza muito clarinho (quase branco)
val LightSurface = Color(0xFFFFFFFF)      // Branco puro para os cartões
val LightTextPrimary = Color(0xFF0B132B)  // Azul escuro (o fundo do dark mode) para o texto
val LightTextSecondary = Color(0xFF64748B)// Cinzento para quantidades

private val ModernLightScheme = lightColorScheme(
    primary = PrimaryAccent,
    background = LightBackground,
    surface = LightBackground,
    surfaceVariant = LightSurface,
    onPrimary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextPrimary,
    errorContainer = ErrorRed,
    onErrorContainer = Color.White
)

// O @OptIn avisa o compilador que estamos a usar ferramentas "novas" do Compose que ainda estão em fase experimental.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingListScreen(familyCode: String, isDarkTheme: Boolean, onToggleTheme: () -> Unit, onLogout: () -> Unit) {
    // ------------------------------------------------------------------------
    // GESTÃO DE ESTADO (State Management)
    // O 'remember' faz com que a variável não perca o seu valor quando o ecrã se redesenha (recomposição).
    // ------------------------------------------------------------------------
    val client = remember { ShoppingClient(familyCode) }

    // mutableStateListOf é uma lista especial: se adicionares ou removeres algo, o ecrã atualiza sozinho!
    val items = remember { mutableStateListOf<ShoppingItem>() }

    // Variável que controla se o pop-up de Adicionar está visível (true) ou escondido (false)
    var showDialog by remember { mutableStateOf(false) }

    // Guarda o item que estamos a editar no momento. Se for null, não estamos a editar nada.
    var itemToEdit by remember { mutableStateOf<ShoppingItem?>(null) }

    // Começa como 'true' porque a primeira coisa que a app faz é carregar dados
    var isLoading by remember { mutableStateOf(true) }

    // Guarda as sugestoes da casa
    val suggestions = remember { mutableStateListOf<QuickSuggestion>() }

    // Controla o pop-up de limpar tudo
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // O 'scope' serve para podermos lançar rotinas assíncronas (como ir à internet) sem bloquear a interface da app.
    val scope = rememberCoroutineScope()

    // Controlador daquela barra preta que aparece no fundo (Snackbar) com o botão "Desfazer"
    val snackbarHostState = remember { SnackbarHostState() }

    // ------------------------------------------------------------------------
    // CICLO DE VIDA E WEBSOCKETS
    // ------------------------------------------------------------------------
    // O LaunchedEffect(Unit) é o código que corre UMA ÚNICA VEZ quando a App abre.
    LaunchedEffect(Unit) {
        // 1. Tenta carregar a lista inicial
        try {
            isLoading = true // Liga a rodinha
            items.clear()
            suggestions.clear()
            items.addAll(client.getItems())
            suggestions.addAll(client.getSuggestions())
        } catch (e: Exception) {
            println("Erro inicial: ${e.message}")
        } finally {
            isLoading = false // Desliga a rodinha (quer corra bem ou mal)
        }

        // 2. Depois, liga o "Túnel" (WebSocket) e fica à escuta para sempre.
        // O .collect() fica num loop à espera que o servidor mande mensagens.
        client.listenForUpdates().collect { command ->
            // Se alguém noutro telemóvel adicionar um item, o servidor grita "REFRESH".
            if (command == "REFRESH") {
                try {
                    val freshItems = client.getItems() // Vamos buscar a lista nova
                    items.clear()
                    items.addAll(freshItems)           // Atualizamos a lista no ecrã
                } catch (e: Exception) {
                    println("Erro ao recarregar: ${e.message}")
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // CONSTRUÇÃO DA INTERFACE (UI)
    // ------------------------------------------------------------------------
    // O Scaffold é o "esqueleto" de uma app clássica. Tem espaços (slots) pré-definidos para o topo, fundo, botões flutuantes, etc.
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Casa: $familyCode", fontWeight = FontWeight.Bold, color = PrimaryAccent)
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    actions = {
                        //Botão de Mudar Tema (Sol / Lua)
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = "Mudar Tema",
                                tint = PrimaryAccent
                            )
                        }
                        // Botão de Limpar
                        if (items.any { it.isBought }) {
                            IconButton(onClick = { showClearConfirmDialog = true }) {
                                Icon(Icons.Rounded.DeleteSweep, contentDescription = "Limpar Comprados", tint = PrimaryAccent)
                            }
                        }
                        // Botão de Sair
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Rounded.ExitToApp, contentDescription = "Sair", tint = PrimaryAccent)
                        }
                    }
                )

                // BARRA DE PROGRESSO
                // Só mostramos a barra se a lista não estiver vazia
                if (items.isNotEmpty()) {
                    val totalItems = items.size
                    val boughtItems = items.count { it.isBought }
                    val progress = boughtItems.toFloat() / totalItems.toFloat()

                    // Animação suave para a barra encher
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(durationMillis = 500)
                    )

                    // Quando chega aos 100%, a barra muda de Azul Ciano para Verde!
                    val progressColor by animateColorAsState(
                        targetValue = if (boughtItems == totalItems) Color(0xFF4CAF50) else PrimaryAccent,
                        animationSpec = tween(durationMillis = 500)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        // Os Textos por cima da barra
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progresso",
                                color = TextGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "$boughtItems/$totalItems (${(progress * 100).toInt()}%)",
                                color = if (boughtItems == totalItems) progressColor else TextGray,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // A barra visual

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)) // Arredonda o contentor todo (fundo e frente)
                                .background(MaterialTheme.colorScheme.surfaceVariant)   // A cor do fundo da barra (o trilho)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress) // A largura deste Box é a % do progresso!
                                    .fillMaxHeight()
                                    .background(progressColor)      // A cor do enchimento (Ciano ou Verde)
                            )
                        }
                    }
                }
            }
                 },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // Diz ao Scaffold onde deve desenhar os pop-ups de "Desfazer"
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }, // Ao clicar, muda a variável para abrir o pop-up
                containerColor = PrimaryAccent,
                contentColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50) // Faz com que o botão seja uma bola perfeita
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Adicionar", modifier = Modifier.size(28.dp))
            } },
        containerColor = MaterialTheme.colorScheme.surfaceVariant // Garante que a zona central atrás da lista é escura
    ) { innerPadding ->

        // SE ESTIVER A CARREGAR: Mostra a rodinha no centro do ecrã
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryAccent,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp) // Fica com um tamanho simpático
                )
            }
        }
        // "Empty State"
        else if (items.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Ícone gigante do carrinho
                        Icon(
                            imageVector = Icons.Rounded.ShoppingCart,
                            contentDescription = "Carrinho Vazio",
                            modifier = Modifier.size(100.dp),
                            tint = TextGray.copy(alpha = 0.5f) // Fica com um tom cinza meio transparente
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "A lista de compras está vazia",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextGray
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botão que faz a mesma coisa que o '+' flutuante
                        Button(
                            onClick = { showDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Adicionar o primeiro item", fontWeight = FontWeight.Bold)
                        }
                    }
                }
        // SE JÁ NÃO ESTIVER A CARREGAR: Mostra a tua lista
        else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // A dica visual no topo da lista
                item {
                    Text(
                        text = "Desliza para a esquerda para apagar",
                        color = TextGray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }


            // key = { it.id }: ISTO É MUITO IMPORTANTE!
            // Ajuda o Compose a saber quem é quem. Se apagares o item do meio, ele sabe exatamente qual cartão
            // remover, evitando bugs em que apaga o item errado visualmente.
                // Os itens com isBought = false ficam em cima, isBought = true vão para baixo.
            items(items.sortedBy { it.isBought }, key = { it.id }) { item ->
                // ----------------------------------------------------------------
                // LÓGICA DO SWIPE (Deslizar para apagar)
                // ----------------------------------------------------------------
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        // Só ativa se o movimento for da Direita para a Esquerda (EndToStart)
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            scope.launch {
                                var jaDesfez = false // Variável de controlo para evitar duplo-clique no Desfazer
                                try {
                                    // 1. Limpa pop-ups antigos e manda apagar no servidor
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    client.deleteItem(item.id)
                                    // 2. Mostra a mensagem e fica à espera de uma ação (Suspende aqui até a mensagem fechar)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${item.name} apagado",
                                        actionLabel = "Desfazer",
                                        duration = SnackbarDuration.Short
                                    )
                                    // 3. Se o utilizador clicou no botão "Desfazer" e ainda não tinha clicado
                                    if (result == SnackbarResult.ActionPerformed && !jaDesfez) {
                                        jaDesfez = true
                                        snackbarHostState.currentSnackbarData?.dismiss() // Fecha logo o pop-up
                                        // Voltamos a adicionar o item ao servidor usando uma cópia com ID limpo (para gerar um novo)
                                        client.addItem(item.copy(id = ""))
                                    }
                                } catch (e: Exception) {
                                    println("Erro: ${e.message}")
                                }
                            }
                            true // Diz ao SwipeBox: "Sim, podes avançar com o efeito visual de remover o cartão"
                        } else {
                            false // Se deslizar para o lado errado, volta a colocar o cartão no sítio
                        }
                    }
                )
                // ----------------------------------------------------------------
                // ANIMAÇÕES DE COR E TRANSPARÊNCIA (State-driven Animations)
                // Estas variáveis ficam "à escuta" do estado (isBought). Se o estado mudar,
                // em vez de saltarem para o novo valor instantaneamente, fazem uma transição suave.
                // ----------------------------------------------------------------
                val animatedCardColor by animateColorAsState(
                    targetValue = if (item.isBought) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceVariant,
                    animationSpec = tween(durationMillis = 500) // tween = animação com tempo fixo (meio segundo)
                )
                val animatedAlpha by animateFloatAsState(
                    targetValue = if (item.isBought) 0.4f else 1f, // 0.4f = 40% opaco (bastante transparente)
                    animationSpec = tween(durationMillis = 500)
                )
                // Animação para o ícone do lixo crescer.
                // Só cresce se a direção do "puxão" tiver ultrapassado o limite (isDismissing == true)
                val isDismissing = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                val trashScale by animateFloatAsState(
                    targetValue = if (isDismissing) 1.5f else 1f, // Cresce 50%
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy) // Efeito mola (dá um pequeno salto)
                )
                // O contentor que permite o deslize
                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.animateItem(), // Faz com que os cartões de baixo deslizem para cima suavemente quando este é apagado
                    backgroundContent = {
                        // Fundo vermelho que aparece por trás ao deslizar
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            Color.Transparent
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.CenterEnd // Encosta o lixo à direita
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Apagar",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(end = 24.dp).size(28.dp).scale(trashScale) // Aplica aqui a animação de tamanho do lixo
                            )
                        } },
                    content = {
                        // CARTÃO VISUAL PRINCIPAL DO ITEM
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = animatedCardColor), // Cor animada
                            // Se estiver comprado (isBought), remove a sombra (0.dp), senão tem sombra (8.dp)
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // ------------------------------------------------------------
                                // O NOSSO "VISTO" ANIMADO PERSONALIZADO
                                // ------------------------------------------------------------
                                val checkScale by animateFloatAsState(
                                    targetValue = if (item.isBought) 1.2f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioHighBouncy, // Mola muito elástica
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                val checkColor by animateColorAsState(
                                    targetValue = if (item.isBought) PrimaryAccent else TextGray
                                )
                                // Usamos uma Box clicável e redonda em vez de uma Checkbox aborrecida
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            // 1. Criamos a versão atualizada do item com o visto trocado
                                            val newState = !item.isBought
                                            val updatedItem = item.copy(isBought = newState)

                                            // 2. MAGIA OTIMISTA: Atualizamos a lista local IMEDIATAMENTE!
                                            // (A animação vai disparar logo no ecrã, sem atrasos)
                                            val index = items.indexOfFirst { it.id == item.id }
                                            if (index != -1) {
                                                items[index] = updatedItem
                                            }

                                            // 3. Enviamos para o servidor em background
                                            scope.launch {
                                                try {
                                                    client.updateItem(updatedItem)
                                                    // Quando o servidor responder com REFRESH, a lista vai
                                                    // recarregar, mas o utilizador nem nota porque já está igual!
                                                } catch (e: Exception) {
                                                    println("Erro de rede: ${e.message}")
                                                    // 4. Se falhar (sem rede), revertemos a animação
                                                    if (index != -1) {
                                                        items[index] = item // volta a colocar o antigo
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        // Se comprado mostra Visto, se não, mostra Círculo Vazio
                                        imageVector = if (item.isBought) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = "Comprado",
                                        tint = checkColor, // Cor animada
                                        modifier = Modifier.scale(checkScale).size(28.dp) // Tamanho animado
                                    )
                                }
                                // TEXTOS (Nome e Quantidade)

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                        .clip(RoundedCornerShape(8.dp)) // Para o efeito de clique ser redondinho
                                        .clickable {
                                            itemToEdit = item // Ao clicar, dizemos à app qual é o item a editar!
                                        }
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        textDecoration = if (item.isBought) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = animatedAlpha)
                                    )
                                    if (item.quantity > 1) {
                                        Text(
                                            text = "Qtd: ${item.quantity}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = animatedAlpha)
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
    }

    // Se a variável 'showDialog' for verdadeira, desenha o nosso ecrã de adicionar item por cima de tudo
    if (showDialog) {
        AddItemDialog(
            suggestions = suggestions,
            onAddSuggestion = { nomeSugestao ->
                scope.launch {
                    try {
                        val nova = client.addSuggestion(nomeSugestao)
                        suggestions.add(nova) // Adiciona à lista local imediatamente
                    } catch (e: Exception) { println("Erro ao guardar sugestão") }
                }
            },
            onDeleteSuggestion = { idSugestao ->
                scope.launch {
                    try {
                        client.deleteSuggestion(idSugestao)
                        suggestions.removeAll { it.id == idSugestao } // Remove visualmente
                    } catch (e: Exception) { println("Erro ao apagar sugestão") }
                }
            },
            onDismiss = { showDialog = false },
            onConfirm = { name, quantity ->
                scope.launch {
                    val newItem = ShoppingItem(name = name, quantity = quantity.toIntOrNull() ?: 1)
                    try {
                        client.addItem(newItem)
                        showDialog = false
                    } catch (e: Exception) { println("Erro: ${e.message}") }
                }
            }
        )
    }

    /// Se houver um item selecionado para editar, mostra o pop-up
    itemToEdit?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onConfirm = { novoNome, novaQuantidade ->

                // 1. Cria a nova versão do item
                val updatedItem = item.copy(
                    name = novoNome,
                    quantity = novaQuantidade.toIntOrNull() ?: 1
                )

                // 2. MAGIA OTIMISTA: Atualiza a lista e fecha o pop-up instantaneamente!
                val index = items.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    items[index] = updatedItem
                }
                itemToEdit = null // Fecha o pop-up sem esperar pelo servidor

                // 3. Envia para o servidor em background
                scope.launch {
                    try {
                        client.updateItem(updatedItem)
                    } catch (e: Exception) {
                        println("Erro ao editar: ${e.message}")
                        // Se falhar, reverte para o valor antigo
                        if (index != -1) items[index] = item
                    }
                }
            }
        )
    }

    // Pop-up de Confirmação para limpar tudo
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = PrimaryAccent,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("Limpar Comprados", fontWeight = FontWeight.Bold) },
            text = { Text("Tens a certeza que queres apagar todos os itens que já foram comprados? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        // MAGIA OTIMISTA: Apagamos do ecrã instantaneamente
                        items.removeAll { it.isBought }
                        showClearConfirmDialog = false // Fecha o pop-up

                        // Manda o comando para o servidor em background
                        scope.launch {
                            try {
                                client.clearBoughtItems()
                            } catch (e: Exception) {
                                println("Erro ao limpar: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Apagar Tudo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }


}

// ============================================================================
// COMPONENTE EXTRA: O Pop-up (Dialog) de Adicionar Item
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddItemDialog(
    suggestions: List<QuickSuggestion>,
    onAddSuggestion: (String) -> Unit,
    onDeleteSuggestion: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var saveAsSuggestion by remember { mutableStateOf(false) } // A caixinha de verificação

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = PrimaryAccent,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("Novo Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // --- ÁREA DAS SUGESTÕES RÁPIDAS ---
                if (suggestions.isNotEmpty()) {
                    Text("Sugestões Rápidas (Pressiona para apagar):", style = MaterialTheme.typography.labelMedium, color = TextGray)

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestions, key = { it.id }) { sug ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        // Clique Rápido: Adiciona logo o item com quantidade 1 e fecha!
                                        onConfirm(sug.name, "1")
                                    },
                                    onLongClick = {
                                        // Clique Longo: Apaga a sugestão
                                        onDeleteSuggestion(sug.id)
                                    }
                                )
                            ) {
                                Text(
                                    text = sug.name,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PrimaryAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Divider(color = TextGray.copy(alpha = 0.2f)) // Linha separadora
                }

                // --- ÁREA NORMAL DE TEXTO ---
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome (ex: Arroz)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Checkbox para guardar nos favoritos
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { saveAsSuggestion = !saveAsSuggestion }) {
                    Checkbox(
                        checked = saveAsSuggestion,
                        onCheckedChange = { saveAsSuggestion = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryAccent)
                    )
                    Text("Guardar como sugestão rápida", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (saveAsSuggestion && name.isNotBlank()) {
                        onAddSuggestion(name) // Guarda a sugestão na BD
                    }
                    onConfirm(name, quantity)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = MaterialTheme.colorScheme.onBackground),
                shape = RoundedCornerShape(50)
            ) {
                Text("Adicionar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextGray)) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditItemDialog(
    item: ShoppingItem,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    // Ao contrário do AddItem, aqui as variáveis já começam preenchidas com os valores do item!
    var name by remember { mutableStateOf(item.name) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = PrimaryAccent,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("Editar Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
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
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, quantity) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = MaterialTheme.colorScheme.onBackground),
                shape = RoundedCornerShape(50)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
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