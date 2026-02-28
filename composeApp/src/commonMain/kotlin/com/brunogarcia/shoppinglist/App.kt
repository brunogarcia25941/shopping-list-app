package com.brunogarcia.shoppinglist

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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.CameraAlt
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import kotlinx.serialization.json.Json
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Language
import com.brunogarcia.shoppinglist.t
import kotlinx.serialization.encodeToString
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PickerMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

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


    var themeColorName by remember { mutableStateOf(settings.getString("THEME_COLOR", "Ocean")) }

    // Variável do Idioma
    var isPortuguese by remember { mutableStateOf(settings.getBoolean("IS_PT", true)) }

    // Descobre qual é a cor que está escolhida
    val selectedPrimaryColor = when (themeColorName) {
        "Forest" -> Color(0xFF4CAF50)    // Verde Natureza
        "Sunset" -> Color(0xFFFF9800)    // Laranja Pôr do Sol
        "Amethyst" -> Color(0xFF9C27B0)  // Roxo Ametista
        "Rose" -> Color(0xFFE91E63)      // Rosa Choque
        else -> Color(0xFF5BC0BE)        // Ocean (O defeito)
    }

    // 3. Escolhe o esquema de cores com base na variável
    val baseScheme = if (isDarkTheme) ModernDarkBlueScheme else ModernLightScheme
    val currentScheme = baseScheme.copy(primary = selectedPrimaryColor)

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
                isPt = isPortuguese,
                onLanguageChange = { newState ->
                    isPortuguese = newState
                    settings.putBoolean("IS_PT", newState) // Guarda o idioma
                },
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
                isPortuguese = isPortuguese,
                onLanguageChange = { newState ->
                    isPortuguese = newState // Muda a variável
                    settings.putBoolean("IS_PT", newState) // Guarda na memória local
                },
                themeColorName = themeColorName,
                onThemeColorChange = { newColor ->
                    themeColorName = newColor
                    settings.putString("THEME_COLOR", newColor) // Guarda na memória local
                },
                onLogout = {
                    settings.remove("CACHE_ITEMS_$loggedFamilyCode")
                    settings.remove("CACHE_SUGS_$loggedFamilyCode")
                    settings.remove("FAMILY_CODE")
                    loggedFamilyCode = ""
                }
            )
        }
    }
}

@Composable
fun LoginScreen(isPt: Boolean, onLanguageChange: (Boolean) -> Unit, onEnter: (String) -> Unit) {
    var codeInput by remember { mutableStateOf("") }
    // Traz o gestor de links do Android/iOS/Web
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. BOTÃO DE MUDAR IDIOMA NO CANTO SUPERIOR ---
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { onLanguageChange(!isPt) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Rounded.Language, contentDescription = "Language")
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isPt) "PT" else "EN", fontWeight = FontWeight.Bold)
            }
        }

        // --- 2. O CARTÃO PRINCIPAL (Centrado) ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        t("Bem-vindo", "Welcome", isPt),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // --- 3. CAIXA DE INSTRUÇÕES ELEGANTE ---

                    Surface(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f), // Fundo translúcido
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            // Envolvemos os textos numa Coluna com "weight"
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    t(
                                        "O Código da Casa é uma chave secreta partilhada. Inventa um código (ex: SILVA2026) e partilha-o com a tua família. Todos os que usarem este código verão a mesma lista!\n\n🍏 Dica: Familiares com iPhone ou PC não precisam instalar a app, basta acederem ao nosso site e usar o mesmo código.",
                                        "The Family Code is a shared secret key. Invent a code (e.g., SMITH2026) and share it with your family. Everyone using this code will see the same list!\n\n🍏 Tip: Family members with an iPhone or PC don't need to install the app, they can just access our website and use the same code.",
                                        isPt
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray
                                )
                                Text(
                                    text = "https://family-shopping-list-maoz.onrender.com/",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clickable {
                                            uriHandler.openUri("https://family-shopping-list-maoz.onrender.com/")
                                        }
                                )
                            }
                        }
                    }


                    // --- 4. O CAMPO DE TEXTO ---
                    OutlinedTextField(
                        value = codeInput,
                        // Tira espaços e põe tudo em maiúsculas automaticamente
                        onValueChange = { codeInput = it.uppercase().replace(" ", "") },
                        label = { Text(t("Código da Casa", "Family Code", isPt)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- 5. O BOTÃO DE ENTRAR ---
                    Button(
                        onClick = {
                            if (codeInput.isNotBlank()) onEnter(codeInput)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(t("Entrar", "Login", isPt), fontWeight = FontWeight.Bold)
                    }
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
fun ShoppingListScreen(familyCode: String, isDarkTheme: Boolean, isPortuguese: Boolean, onLanguageChange: (Boolean) -> Unit, onToggleTheme: () -> Unit, themeColorName: String, onThemeColorChange: (String) -> Unit, onLogout: () -> Unit) {
    // ------------------------------------------------------------------------
    // GESTÃO DE ESTADO (State Management)
    // O 'remember' faz com que a variável não perca o seu valor quando o ecrã se redesenha (recomposição).
    // ------------------------------------------------------------------------
    val client = remember { ShoppingClient(familyCode) }

    val settings = remember { Settings() }

    // mutableStateListOf é uma lista especial: se adicionares ou removeres algo, o ecrã atualiza sozinho!
    val items = remember { mutableStateListOf<ShoppingItem>() }

    // Variável que controla se o pop-up de Adicionar está visível (true) ou escondido (false)
    var showDialog by remember { mutableStateOf(false) }

    // Guarda o item que estamos a editar no momento. Se for null, não estamos a editar nada.
    var itemToEdit by remember { mutableStateOf<ShoppingItem?>(null) }

    var itemToShowDetails by remember { mutableStateOf<ShoppingItem?>(null) }

    // Começa como 'true' porque a primeira coisa que a app faz é carregar dados
    var isLoading by remember { mutableStateOf(true) }

    // Guarda as sugestoes da casa
    val suggestions = remember { mutableStateListOf<QuickSuggestion>() }

    // Controla o pop-up de limpar tudo
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // O 'scope' serve para podermos lançar rotinas assíncronas (como ir à internet) sem bloquear a interface da app.
    val scope = rememberCoroutineScope()

    // motor de vibração (Haptics)
    val vibrator = rememberNativeVibrator()

    val shareManager = rememberShareManager()

    // Controlador daquela barra preta que aparece no fundo (Snackbar) com o botão "Desfazer"
    val snackbarHostState = remember { SnackbarHostState() }

    var showSettingsDialog by remember { mutableStateOf(false) }

    var showManageSuggestionsDialog by remember { mutableStateOf(false) }

    val categorias = listOf("Supermercado", "Farmácia", "Outros")
    var selectedCategory by remember { mutableStateOf(categorias.first()) }

    // CÉREBRO DO PULL-TO-REFRESH
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val screenManager = rememberScreenManager()

    // Assim que este ecrã aparecer (LaunchedEffect), ele diz "fica ligado"
    LaunchedEffect(Unit) {
        screenManager.keepScreenOn(true)
    }

    //Quando a pessoa sair deste ecra o compose corre a seccao "onDispose" e diz que o ecrã ja pode apagar
    DisposableEffect(Unit) {
        onDispose {
            screenManager.keepScreenOn(false)
        }
    }

    val widgetUpdater = rememberWidgetUpdater()

    val saveToCache = {
        try {
            // Transforma a lista em texto e guarda-a
            val itemsJson = Json.encodeToString(items.toList())
            val sugJson = Json.encodeToString(suggestions.toList())
            settings.putString("CACHE_ITEMS_$familyCode", itemsJson)
            settings.putString("CACHE_SUGS_$familyCode", sugJson)
            widgetUpdater.update()
        } catch (e: Exception) { println("Erro ao guardar cache: ${e.message}") }
    }

    // ------------------------------------------------------------------------
    // CICLO DE VIDA E WEBSOCKETS
    // ------------------------------------------------------------------------
    // O LaunchedEffect(Unit) é o código que corre UMA ÚNICA VEZ quando a App abre.
    LaunchedEffect(Unit) {
        // -------------------------------------------------------------
        // FASE 1: LEITURA OFFLINE INSTANTÂNEA
        // -------------------------------------------------------------
        val cachedItems = settings.getString("CACHE_ITEMS_$familyCode", "")
        val cachedSugs = settings.getString("CACHE_SUGS_$familyCode", "")

        if (cachedItems.isNotEmpty()) {
            try {
                // Se houver memória, mostra logo no ecrã!
                items.clear()
                items.addAll(Json.decodeFromString<List<ShoppingItem>>(cachedItems))

                if (cachedSugs.isNotEmpty()) {
                    suggestions.clear()
                    suggestions.addAll(Json.decodeFromString<List<QuickSuggestion>>(cachedSugs))
                }

                // Como já temos dados para mostrar, desligamos a rodinha logo!
                isLoading = false
            } catch (e: Exception) { println("Erro na cache, vamos ignorar.") }
        } else {
            // Se for a primeira vez que abrimos a app e a cache estiver vazia, liga a rodinha.
            isLoading = true
        }

        // -------------------------------------------------------------
        // FASE 2: ATUALIZAÇÃO SILENCIOSA PELA INTERNET
        // -------------------------------------------------------------
        try {
            val netItems = client.getItems()
            val netSugs = client.getSuggestions()

            // Atualiza o ecrã com as novidades da net
            items.clear()
            items.addAll(netItems)
            suggestions.clear()
            suggestions.addAll(netSugs)

            // Tira a "fotografia" para guardar esta versão atualizada para amanhã!
            saveToCache()
        } catch (e: Exception) {
            println("Sem internet! O Modo Offline está a aguentar a app: ${e.message}")
            // Avisa o utilizador que falhou a sincronização inicial
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = t("A mostrar lista offline. A sincronizar...", "Showing offline list. Syncing...", isPortuguese),
                    duration = SnackbarDuration.Short
                )
            }
        } finally {
            isLoading = false
        }


        // -------------------------------------------------------------
        // FASE 3: WEBSOCKET A LÓGICA INTELIGENTE (COM RECONEXÃO)
        // -------------------------------------------------------------
        while (true) {
            try {
                client.listenForUpdates().collect { jsonText ->
                    try {
                        val message = Json.decodeFromString<WsMessage>(jsonText)
                        var houveAlteracao = false

                        when (message.action) {
                            "ADD" -> {
                                message.item?.let { newItem ->
                                    if (items.none { it.id == newItem.id }) {
                                        items.add(newItem)
                                        houveAlteracao = true
                                    }
                                }
                            }
                            "UPDATE" -> {
                                message.item?.let { updatedItem ->
                                    val index = items.indexOfFirst { it.id == updatedItem.id }
                                    if (index != -1) {
                                        items[index] = updatedItem
                                        houveAlteracao = true
                                    }
                                }
                            }
                            "DELETE" -> {
                                message.itemId?.let { idToApagar ->
                                    val apagou = items.removeAll { it.id == idToApagar }
                                    if (apagou) houveAlteracao = true
                                }
                            }
                            "DELETE_BOUGHT" -> {
                                val apagou = items.removeAll { it.isBought }
                                if (apagou) houveAlteracao = true
                            }
                        }

                        if (houveAlteracao) saveToCache()

                    } catch (e: Exception) {
                        println("Erro ao decifrar evento: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("A ligação WebSocket caiu. A tentar religar... Erro: ${e.message}")
            }

            // Se o collect for interrompido (falha de rede), esperamos 3 segundos e o while(true) tenta de novo
            kotlinx.coroutines.delay(3000)
        }
    }

    // ------------------------------------------------------------------------
    // FASE 4: O "DESPERTADOR" (Voltar do Segundo Plano)
    // Este bloco corre sempre que a app volta a estar visível no ecrã!
    // ------------------------------------------------------------------------
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch {
            try {
                // A app acordou, vamos ver o que perdemos enquanto dormía
                val netItems = client.getItems()
                val netSugs = client.getSuggestions()

                // Atualizamos a interface silenciosamente
                items.clear()
                items.addAll(netItems)

                suggestions.clear()
                suggestions.addAll(netSugs)

                // Tiramos uma nova "fotografia" para o Modo Offline
                saveToCache()
            } catch (e: Exception) {
                println("Erro ao recarregar após despertar: ${e.message}")
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
                        Text(t(familyCode, familyCode, isPortuguese), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    actions = {

                        IconButton(onClick = {
                            // 1. Cria a mensagem
                            val mensagem = formatShoppingList(items, isPortuguese, familyCode)
                            // 2. Chama o ecrã de partilha do telemóvel
                            shareManager.shareText(mensagem)
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = t("Partilhar", "Share", isPortuguese),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        //botao de definicoes
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = t("Definições", "Settings", isPortuguese),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Botão de Limpar
                        if (items.any { it.isBought }) {
                            IconButton(onClick = { showClearConfirmDialog = true }) {
                                Icon(Icons.Rounded.DeleteSweep, contentDescription = "Limpar Comprados", tint = MaterialTheme.colorScheme.primary)
                            }
                        }


                        // Botão de Sair
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Rounded.ExitToApp, contentDescription = "Sair", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )

                val itemsDaCategoria = items.filter { it.category == selectedCategory }

                // BARRA DE PROGRESSO
                // Só mostramos a barra se a lista não estiver vazia
                if (itemsDaCategoria.isNotEmpty()) {
                    val totalItems = itemsDaCategoria.size
                    val boughtItems = itemsDaCategoria.count { it.isBought }
                    val progress = boughtItems.toFloat() / totalItems.toFloat()

                    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(500))
                    val progressColor by animateColorAsState(
                        targetValue = if (boughtItems == totalItems) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        animationSpec = tween(500)
                    )

                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t("Progresso", "Progress", isPortuguese), color = TextGray, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "$boughtItems/$totalItems (${(progress * 100).toInt()}%)",
                                color = if (boughtItems == totalItems) progressColor else TextGray,
                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Box(modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().background(progressColor))
                        }
                    }
                }

                // OS SEPARADORES DAS LISTAS (Tabs)
                ScrollableTabRow(
                    selectedTabIndex = categorias.indexOf(selectedCategory),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp, // Dá espaco nas bordas
                    divider = {} // Remove a linha divisória padrão que fica por baixo
                ) {
                    categorias.forEach { categoria ->
                        Tab(
                            selected = selectedCategory == categoria,
                            onClick = { selectedCategory = categoria },
                            text = {
                                Text(
                                    text = tCategory(categoria, isPortuguese),
                                    fontWeight = if (selectedCategory == categoria) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCategory == categoria) MaterialTheme.colorScheme.primary else TextGray
                                )
                            }
                        )
                    }

                }
            }
                 },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // Diz ao Scaffold onde deve desenhar os pop-ups de "Desfazer"
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }, // Ao clicar, muda a variável para abrir o pop-up
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50) // Faz com que o botão seja uma bola perfeita
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Adicionar", modifier = Modifier.size(28.dp))
            } },

        // barra de publicidade
        bottomBar = {
            AdBanner()
        },

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
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp) // Fica com um tamanho simpático
                )
            }
        }
        // "Empty State"
        else if (items.filter { it.category == selectedCategory }.isEmpty()) {
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

                        val catTranslated = tCategory(selectedCategory, isPortuguese)

                        Text(
                            t("A lista de $catTranslated está vazia", "The $catTranslated list is empty", isPortuguese),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextGray
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botão que faz a mesma coisa que o '+' flutuante
                        Button(
                            onClick = { showDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(t("Adicionar o primeiro item", "Add the first item", isPortuguese), fontWeight = FontWeight.Bold)
                        }
                    }
                }
        // SE JÁ NÃO ESTIVER A CARREGAR: Mostra a tua lista
        else {

            @OptIn(ExperimentalMaterial3Api::class)
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    coroutineScope.launch {
                        try {
                            // Vai buscar tudo de novo à Base de Dados
                            val netItems = client.getItems()
                            val netSugs = client.getSuggestions()
                            items.clear()
                            items.addAll(netItems)
                            suggestions.clear()
                            suggestions.addAll(netSugs)
                            saveToCache()
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(
                                message = t("Erro ao atualizar a lista. Verifica a internet.", "Error updating list. Check your connection.", isPortuguese),
                                duration = SnackbarDuration.Short
                            )
                        } finally {
                            // Pára a rodinha independentemente de dar erro ou sucesso
                            isRefreshing = false
                        }
                    }
                },
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.padding().fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // A dica visual no topo da lista
                    item {
                        Text(

                            t(
                                "Desliza para a esquerda para apagar",
                                "Slide to the left to delete",
                                isPortuguese
                            ),
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
                    items(
                        items.filter { it.category == selectedCategory }.sortedBy { it.isBought },
                        key = { it.id }) { item ->

                        // ANIMAÇÃO DE VISIBILIDADE ---
                        var isVisible by remember { mutableStateOf(false) }

                        // Assim que o cartão entra no ecrã pela primeira vez, ele aparece suavemente
                        LaunchedEffect(Unit) {
                            isVisible = true
                        }

                        // Envolvemos o cartão nesta "Caixa" que sabe encolher e esticar
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(500)) + expandVertically(tween(500)),
                            exit = fadeOut(tween(500)) + shrinkVertically(tween(500))
                        ) {
                            // ----------------------------------------------------------------
                            // LÓGICA DO SWIPE (Deslizar para apagar)
                            // ----------------------------------------------------------------
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {

                                        vibrator.vibrateHeavy()

                                        scope.launch {
                                            var jaDesfez = false
                                            try {
                                                snackbarHostState.currentSnackbarData?.dismiss()

                                                // 1. MAGIA VISUAL: Em vez de apagar logo, fechamos a "cortina"!
                                                isVisible = false

                                                // 2. Esperamos que o cartão termine de encolher (300ms)
                                                kotlinx.coroutines.delay(500)

                                                // 3. AGORA SIM, apagamos da base de dados local
                                                items.removeAll { it.id == item.id }
                                                saveToCache()

                                                // 4. Manda apagar no servidor às escondidas
                                                client.deleteItem(item.id)


                                                // 4. Mostra a mensagem e fica à espera de uma ação (Suspende aqui)
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "${item.name} apagado",
                                                    actionLabel = "Desfazer",
                                                    duration = SnackbarDuration.Short
                                                )

                                                // 5. Se o utilizador clicou no botão "Desfazer"
                                                if (result == SnackbarResult.ActionPerformed && !jaDesfez) {
                                                    jaDesfez = true
                                                    snackbarHostState.currentSnackbarData?.dismiss() // Fecha logo o pop-up

                                                    // Restaura com um novo ID único (e otimista!)
                                                    val uniqueId =
                                                        kotlin.random.Random.nextLong().toString()
                                                    val itemRestaurado = item.copy(id = uniqueId)

                                                    items.add(itemRestaurado) // Volta a aparecer na lista!
                                                    saveToCache()

                                                    // Mandamos para o servidor a nova versão
                                                    client.addItem(itemRestaurado)
                                                }
                                            } catch (e: Exception) {
                                                println("Erro: ${e.message}")
                                                // Se der erro (ex: sem internet), devolvemos o item à lista para não o perder
                                                if (items.none { it.id == item.id }) {
                                                    items.add(item)
                                                }
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
                            val isDismissing =
                                dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
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
                                    val color =
                                        if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
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
                                            modifier = Modifier.padding(end = 24.dp).size(28.dp)
                                                .scale(trashScale) // Aplica aqui a animação de tamanho do lixo
                                        )
                                    }
                                },
                                content = {
                                    // CARTÃO VISUAL PRINCIPAL DO ITEM
                                    Card(
                                        modifier = Modifier.fillMaxWidth().shadow(
                                            elevation = if (item.isBought) 0.dp else 16.dp, // 16.dp cria uma sombra bem mais alta e larga
                                            shape = RoundedCornerShape(20.dp),
                                            spotColor = Color.Black,    // Força a sombra a ser preta pura
                                            ambientColor = Color.Black  // Reforça a escuridão à volta do cartão
                                        ),

                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = animatedCardColor), // Cor animada
                                        // Se estiver comprado (isBought), remove a sombra (0.dp), senão tem sombra (8.dp)
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                        border = if (item.isBought) null else BorderStroke(
                                            1.dp,
                                            Color.Gray.copy(alpha = 0.2f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                vertical = 8.dp,
                                                horizontal = 12.dp
                                            ),
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
                                                targetValue = if (item.isBought) MaterialTheme.colorScheme.primary else TextGray
                                            )
                                            // Usamos uma Box clicável e redonda em vez de uma Checkbox aborrecida
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .clickable {

                                                        // vibra ao fazer check
                                                        vibrator.vibrateHeavy()

                                                        // 1. Criamos a versão atualizada do item com o visto trocado
                                                        val newState = !item.isBought
                                                        val updatedItem =
                                                            item.copy(isBought = newState)

                                                        // 2. MAGIA OTIMISTA: Atualizamos a lista local IMEDIATAMENTE!
                                                        // (A animação vai disparar logo no ecrã, sem atrasos)
                                                        val index =
                                                            items.indexOfFirst { it.id == item.id }
                                                        if (index != -1) {
                                                            items[index] = updatedItem
                                                        }

                                                        // 3. Enviamos para o servidor em background
                                                        scope.launch {
                                                            try {
                                                                client.updateItem(updatedItem)
                                                                saveToCache()
                                                                // Quando o servidor responder com REFRESH, a lista vai
                                                                // recarregar, mas o utilizador nem nota porque já está igual!
                                                            } catch (e: Exception) {
                                                                println("Erro de rede: ${e.message}")
                                                                // 4. Se falhar (sem rede), revertemos a animação
                                                                if (index != -1) {
                                                                    items[index] =
                                                                        item // volta a colocar o antigo
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
                                                    modifier = Modifier.scale(checkScale)
                                                        .size(28.dp) // Tamanho animado
                                                )
                                            }
                                            // TEXTOS (Nome e Quantidade)

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp)) // Para o efeito de clique ser redondinho
                                                    .clickable {
                                                        itemToEdit =
                                                            item // Ao clicar, dizemos à app qual é o item a editar!
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = item.name,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    textDecoration = if (item.isBought) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = animatedAlpha
                                                    )
                                                )
                                                if (item.quantity > 1) {
                                                    Text(
                                                        t(
                                                            "Quantidade: ${item.quantity}",
                                                            "Ammount: $item.quantity",
                                                            isPortuguese
                                                        ),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = animatedAlpha
                                                        )
                                                    )
                                                }
                                            }
                                            // Botão de Informação à direita
                                            IconButton(onClick = { itemToShowDetails = item }) {
                                                Icon(
                                                    Icons.Rounded.Info,
                                                    contentDescription = t(
                                                        "Detalhes",
                                                        "Details",
                                                        isPortuguese
                                                    ),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
    }
    }

    // Se a variável 'showDialog' for verdadeira, desenha o nosso ecrã de adicionar item por cima de tudo
    if (showDialog) {
        AddItemDialog(
            isPt = isPortuguese,
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
                    // 1. geramos um id unico e aleatorio no telemovel
                    val uniqueId = kotlin.random.Random.nextLong().toString()

                    val newItem = ShoppingItem(
                        id = uniqueId,
                        name = name,
                        quantity = quantity.toIntOrNull() ?: 1,
                        category = selectedCategory
                    )

                    // 2. MAGIA OTIMISTA: Adiciona logo ao ecrã com o ID
                    items.add(newItem)
                    showDialog = false

                    // 3. Tira a foto "Cache"
                    saveToCache()

                    // 4. Tenta mandar para o servidor em background
                    try {
                        client.addItem(newItem)
                    } catch (e: Exception) { println("Erro: ${e.message}") }
                }
            }
        )
    }

    /// Se houver um item selecionado para editar, mostra o pop-up
    itemToEdit?.let { item ->
        EditItemDialog(
            isPt = isPortuguese,
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
                saveToCache()

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
    // Pop-up dos Detalhes do Item
    itemToShowDetails?.let { item ->
        ItemDetailsDialog(
            isPt = isPortuguese,
            item = item,
            onFetchFullItem = { id ->
                try { client.getItem(id) } catch (e: Exception) { null }
            },
            onDismiss = { itemToShowDetails = null },
            onConfirm = { updatedItem ->

                // 1. Magia Otimista: Atualizamos no ecrã logo!
                val index = items.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    items[index] = updatedItem
                }
                itemToShowDetails = null
                saveToCache()

                // 2. Manda para a nossa Base de Dados no Render
                scope.launch {
                    try {
                        client.updateItem(updatedItem)
                    } catch (e: Exception) {
                        println("Erro ao atualizar detalhes: ${e.message}")
                        // Se falhar a net, desfaz a alteração local
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
            titleContentColor = MaterialTheme.colorScheme.primary,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text(t("Limpar Comprados", "Clear Bought", isPortuguese), fontWeight = FontWeight.Bold) },
            text = { Text(t("Tens a certeza que queres apagar todos os itens que já foram comprados? Esta ação não pode ser desfeita.", "Are you sure you want to clear all bought items?", isPortuguese)) },
            confirmButton = {
                Button(
                    onClick = {
                        // MAGIA OTIMISTA: Apagamos do ecrã instantaneamente
                        items.removeAll { it.isBought }
                        showClearConfirmDialog = false // Fecha o pop-up

                        saveToCache()
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
                    Text(t("Apagar Tudo", "Clear All", isPortuguese), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(t("Cancelar", "Cancel", isPortuguese), color = TextGray)
                }
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            isDarkTheme = isDarkTheme,
            onThemeChange = { onToggleTheme() }, // Chama a função que veio por parâmetro
            isPt = isPortuguese,
            onLanguageChange = onLanguageChange, // Passa a função que veio por parâmetro!
            themeColorName = themeColorName,
            onThemeColorChange = onThemeColorChange,
            onOpenManageSuggestions = {
                // Fecha as definições e abre as sugestões!
                showSettingsDialog = false
                showManageSuggestionsDialog = true
            },

            onDismiss = { showSettingsDialog = false } // Aqui podes mudar, porque é local
        )
    }



    // Abre o Gestor de Sugestões
    if (showManageSuggestionsDialog) {
        ManageSuggestionsDialog(
            isPt = isPortuguese,
            suggestions = suggestions,
            onAddSuggestion = { nomeSugestao ->
                scope.launch {
                    try {
                        val nova = client.addSuggestion(nomeSugestao)
                        suggestions.add(nova) // Adiciona localmente
                        saveToCache()
                    } catch (e: Exception) { println("Erro ao guardar sugestão") }
                }
            },
            onDeleteSuggestion = { idSugestao ->
                scope.launch {
                    try {
                        client.deleteSuggestion(idSugestao)
                        suggestions.removeAll { it.id == idSugestao } // Apaga localmente
                        saveToCache()
                    } catch (e: Exception) { println("Erro ao apagar sugestão") }
                }
            },
            onDismiss = { showManageSuggestionsDialog = false }
        )
    }

}

// ============================================================================
// COMPONENTE EXTRA: O Pop-up (Dialog) de Adicionar Item
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddItemDialog(
    isPt: Boolean,
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
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text(t("Novo Item", "New Item", isPt), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // --- ÁREA DAS SUGESTÕES RÁPIDAS ---
                if (suggestions.isNotEmpty()) {
                    Text(t("Sugestões Rápidas (pressiona para apagar):", "Quick Suggestions (long press to delete):", isPt), style = MaterialTheme.typography.labelMedium, color = TextGray)
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
                                    color = MaterialTheme.colorScheme.primary,
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
                    label = { Text(t("Nome (ex: Arroz)", "Name (ex: Rice)", isPt),) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                // SELETOR DE QUANTIDADE (+ e -)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        t("Quantidade", "Quantity", isPt),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Botão Menos (-)
                        IconButton(
                            onClick = {
                                val current = quantity.toIntOrNull() ?: 1
                                if (current > 1) quantity = (current - 1).toString()
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Menos", tint = MaterialTheme.colorScheme.primary)
                        }

                        // O Número no meio
                        Text(
                            text = quantity,
                            modifier = Modifier.padding(horizontal = 20.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Botão Mais (+)
                        IconButton(
                            onClick = {
                                val current = quantity.toIntOrNull() ?: 1
                                quantity = (current + 1).toString()
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Mais", tint = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }



                // Checkbox para guardar nos favoritos
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { saveAsSuggestion = !saveAsSuggestion }) {
                    Checkbox(
                        checked = saveAsSuggestion,
                        onCheckedChange = { saveAsSuggestion = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(t("Guardar como sugestão rápida", "Save as quick suggestion", isPt), style = MaterialTheme.typography.bodyMedium)
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onBackground),
                shape = RoundedCornerShape(50)
            ) {
                Text(t("Adicionar", "Add", isPt), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextGray)) {
                Text(t("Cancelar", "Cancel", isPt))
            }
        }
    )
}

@Composable
fun EditItemDialog(
    isPt: Boolean,
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
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text(t("Editar Item", "Edit Item", isPt), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(t("Nome", "Name", isPt)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                // SELETOR DE QUANTIDADE (+ e -)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        t("Quantidade", "Quantity", isPt),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Botão Menos (-)
                        IconButton(
                            onClick = {
                                val current = quantity.toIntOrNull() ?: 1
                                if (current > 1) quantity = (current - 1).toString()
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Menos", tint = MaterialTheme.colorScheme.primary)
                        }

                        // O Número no meio
                        Text(
                            text = quantity,
                            modifier = Modifier.padding(horizontal = 20.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Botão Mais (+)
                        IconButton(
                            onClick = {
                                val current = quantity.toIntOrNull() ?: 1
                                quantity = (current + 1).toString()
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Mais", tint = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, quantity) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onBackground),
                shape = RoundedCornerShape(50)
            ) {
                Text(t("Guardar", "Save", isPt), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextGray)
            ) {
                Text(t("Cancelar", "Cancel", isPt))
            }
        }
    )
}

@OptIn(ExperimentalEncodingApi::class) // Dizemos ao Kotlin que queremos usar o Base64 novo
@Composable
fun ItemDetailsDialog(
    isPt: Boolean,
    item: ShoppingItem,
    onFetchFullItem: suspend (String) -> ShoppingItem?,
    onDismiss: () -> Unit,
    onConfirm: (ShoppingItem) -> Unit
) {
    var notes by remember { mutableStateOf(item.notes ?: "") }
    var photoBase64 by remember { mutableStateOf(item.photoBase64) }
    var isLoadingPhoto by remember { mutableStateOf(true) } // Rodinha ligada por defeito

    val scope = rememberCoroutineScope()

    // Assim que o pop-up abre, vai sacar a foto verdadeira
    LaunchedEffect(item.id) {
        val fullItem = onFetchFullItem(item.id)
        photoBase64 = fullItem?.photoBase64
        isLoadingPhoto = false // Desliga a rodinha
    }


    // "apanhador" de imagens
    val singleImagePicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Single,
        title = "Escolher Fotografia"
    ) { file ->
        scope.launch {
            file?.let {
                // 1. Lê os bytes originais do telemóvel (pesados)
                val rawBytes = it.readBytes()

                // 2. Passamos pelo compressor
                val compressedBytes = rawBytes.compressImage()

                // 3. Transformamos a imagem miniatura em texto para o servidor
                photoBase64 = Base64.Default.encode(compressedBytes)
            }
        }
    }

    // Lemos os bytes fora da interface para evitar o erro do "Try-Catch" no Compose
    val imageBitmap = remember(photoBase64) {
        if (photoBase64 != null) {
            try {
                // Descodificamos o Base64 e transformamos em Imagem com segurança
                val bytes = Base64.Default.decode(photoBase64!!)
                bytes.toImageBitmap()
            } catch (e: Exception) {
                null // Se falhar, fica nulo (ex: imagem corrompida)
            }
        } else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text(t("Detalhes: ${item.name}", "Details: ${item.name}", isPt), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 1. Campo para as Notas Específicas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(t("Notas (ex: Marca Mimosa)", "Details (ex: Brand)", isPt)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // 2. Área da Imagem (Galeria/Câmara)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { singleImagePicker.launch() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingPhoto) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else if (photoBase64 != null) {
                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "Foto do Produto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(t("Erro ao carregar imagem", "Error loading image", isPt), color = ErrorRed)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = TextGray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(t("Tocar para adicionar foto", "Tap to add photo", isPt), color = TextGray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedItem = item.copy(
                        notes = notes.takeIf { it.isNotBlank() },
                        photoBase64 = photoBase64
                    )
                    onConfirm(updatedItem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onBackground),
                shape = RoundedCornerShape(50)
            ) {
                Text(t("Guardar", "Save", isPt), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextGray)) {
                Text(t("Cancelar", "Cancel", isPt))
            }
        }
    )
}

// Função auxiliar para traduzir o texto
fun t(pt: String, en: String, isPt: Boolean): String {
    return if (isPt) pt else en
}

// Tradutor específico de categorias
fun tCategory(categoria: String, isPt: Boolean): String {
    return when (categoria) {
        "Supermercado" -> if (isPt) "Supermercado" else "Supermarket"
        "Farmácia" -> if (isPt) "Farmácia" else "Pharmacy"
        "Outros" -> if (isPt) "Outros" else "Others"
        else -> categoria // Previne erros se houver outras categorias antigas
    }
}

@Composable
fun SettingsDialog(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    isPt: Boolean,
    onLanguageChange: (Boolean) -> Unit,

    // Recebe as variáveis
    themeColorName: String,
    onThemeColorChange: (String) -> Unit,
    onOpenManageSuggestions: () -> Unit,
    onDismiss: () -> Unit
) {
    // Traz o gestor de links do Android/iOS/Web
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.primary, // <--- Atenção aqui, já usa a cor nova!
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(t("Definições", "Settings", isPt), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 1. Linha do Tema Claro/Escuro

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onThemeChange(!isDarkTheme) }) {
                    Icon(if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode, contentDescription = null, tint = TextGray)
                    Spacer(Modifier.width(12.dp))
                    Text(t("Tema Escuro", "Dark Theme", isPt), modifier = Modifier.weight(1f))
                    Switch(checked = isDarkTheme, onCheckedChange = onThemeChange)
                }

                Divider(color = TextGray.copy(alpha = 0.2f))

                // 2. Linha do Idioma

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onLanguageChange(!isPt) }) {
                    Icon(Icons.Rounded.Language, contentDescription = null, tint = TextGray)
                    Spacer(Modifier.width(12.dp))
                    Text(t("Português (PT)", "English (EN)", isPt), modifier = Modifier.weight(1f))
                    Switch(checked = !isPt, onCheckedChange = { onLanguageChange(!it) })
                }

                Divider(color = TextGray.copy(alpha = 0.2f))

                // -------------------------------------------------------------
                // 3. A GALERIA DE CORES ("O Camaleão")
                // -------------------------------------------------------------
                Text(t("Cor de Destaque", "Accent Color", isPt), color = TextGray, style = MaterialTheme.typography.labelMedium)

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    // A nossa lista de cores com os respetivos códigos
                    val colorOptions = listOf(
                        "Ocean" to Color(0xFF5BC0BE),
                        "Forest" to Color(0xFF4CAF50),
                        "Sunset" to Color(0xFFFF9800),
                        "Amethyst" to Color(0xFF9C27B0),
                        "Rose" to Color(0xFFE91E63)
                    )

                    colorOptions.forEach { (name, color) ->
                        val isSelected = themeColorName == name

                        Box(
                            modifier = Modifier
                                .size(40.dp) // Tamanho da bolinha
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onThemeColorChange(name) }
                                // Desenha um anel branco à volta se estiver selecionado
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Põe um vistozinho na cor que está ativa
                            if (isSelected) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                Divider(color = TextGray.copy(alpha = 0.2f))

                // 4. Abrir Gestão de Sugestões
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onOpenManageSuggestions() }.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = TextGray)
                    Spacer(Modifier.width(12.dp))
                    Text(t("Gerir Sugestões Rápidas", "Manage Quick Suggestions", isPt), modifier = Modifier.weight(1f))
                }
                Divider(color = TextGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                // INSTRUÇÕES PARA IPHONE / WEB
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), // Fundo suave a combinar com o tema
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Trocamos o Row por Column aqui para os dois textos ficarem empilhados!
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            t(
                                "Tens familiares com iPhone ou PC?\nPartilha este link para eles acederem à lista sem precisarem da app:",
                                "Have family members with an iPhone or PC?\nShare this link so they can access the list without the app:",
                                isPt
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "https://family-shopping-list-maoz.onrender.com/",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                uriHandler.openUri("https://family-shopping-list-maoz.onrender.com/")
                            }
                        )
                    }
                }

                // ANÚNCIO DO WIDGET (Só aparece em Android)
                if (isWidgetSupported) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Widgets, // Adiciona o import se necessário
                                contentDescription = "Widget",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = t(
                                    "Dica: Vai ao teu ecrã principal e adiciona o nosso Widget para veres a lista sem abrir a app!",
                                    "Tip: Add our Widget to your home screen to see the list without opening the app!",
                                    isPt
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onBackground),
                shape = RoundedCornerShape(50)
            ) {
                Text(t("Fechar", "Close", isPt), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ManageSuggestionsDialog(
    isPt: Boolean,
    suggestions: List<QuickSuggestion>,
    onAddSuggestion: (String) -> Unit,
    onDeleteSuggestion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newSuggestionName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text(t("Gerir Sugestões", "Manage Suggestions", isPt), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 1. ÁREA DE ADICIONAR NOVA SUGESTÃO
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newSuggestionName,
                        onValueChange = { newSuggestionName = it },
                        label = { Text(t("Nova sugestão...", "New suggestion...", isPt)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newSuggestionName.isNotBlank()) {
                                onAddSuggestion(newSuggestionName)
                                newSuggestionName = "" // Limpa o campo depois de adicionar
                            }
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Adicionar", tint = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }

                Divider(color = TextGray.copy(alpha = 0.2f))

                // 2. LISTA DAS SUGESTÕES EXISTENTES
                if (suggestions.isEmpty()) {
                    Text(t("Sem sugestões guardadas.", "No saved suggestions.", isPt), color = TextGray)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                        items(suggestions, key = { it.id }) { sug ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(sug.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                IconButton(onClick = { onDeleteSuggestion(sug.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Apagar", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(50)
            ) {
                Text(t("Fechar", "Close", isPt), fontWeight = FontWeight.Bold)
            }
        }
    )
}

// Função para desenhar a mensagem do WhatsApp!
fun formatShoppingList(items: List<ShoppingItem>, isPt: Boolean, familyCode: String): String {
    val sb = StringBuilder()

    // Título
    val title = t("🛒 Lista de Compras da Casa ($familyCode)", "🛒 Shopping List ($familyCode)", isPt)
    sb.append("$title\n")
    sb.append("--------------------------\n")

    // Separamos o que falta comprar do que já está comprado
    val toBuy = items.filter { !it.isBought }
    val bought = items.filter { it.isBought }

    if (toBuy.isNotEmpty()) {
        sb.append(t("\n📝 Falta comprar:\n", "\n📝 To buy:\n", isPt))
        toBuy.forEach { item ->
            val notes = if (!item.notes.isNullOrBlank()) " (${item.notes})" else ""
            sb.append("- ${item.quantity}x ${item.name}$notes\n")
        }
    }

    if (bought.isNotEmpty()) {
        sb.append(t("\n✅ Já no carrinho:\n", "\n✅ Already in cart:\n", isPt))
        bought.forEach { item ->
            sb.append("- ${item.name}\n")
        }
    }

    return sb.toString()
}


expect @Composable fun AdBanner()

expect fun ByteArray.toImageBitmap(): ImageBitmap

expect fun ByteArray.compressImage(): ByteArray


// 1. "Comando" que as plataformas têm de obedecer
interface NativeVibrator {
    fun vibrateHeavy()
}

// 2. Prometemos que sabemos criar este comando em qualquer sistema
@Composable
expect fun rememberNativeVibrator(): NativeVibrator

// Interface do Gestor de Partilha
interface ShareManager {
    fun shareText(text: String)
}
@Composable
expect fun rememberShareManager(): ShareManager

