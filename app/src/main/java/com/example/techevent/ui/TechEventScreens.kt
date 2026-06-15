package com.example.techevent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.techevent.data.EventoNetwork

// Colores base del diseño
val DarkBlue = Color(0xFF0B132B)
val PillGreenBg = Color(0xFFE8F5E9)
val PillGreenText = Color(0xFF2E7D32)
val BackgroundApp = Color(0xFFF4F6F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationContainer(
    viewModel: EventViewModel,
    esTablet: Boolean,
    isDarkMode: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val favoritosIds by viewModel.idFavoritos.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Estado persistente para el modo offline manual
    var isModoOfflineManual by rememberSaveable { mutableStateOf(false) }
    val esOfflineReal = (state as? EventoUiState.Success)?.esOffline == true
    val mostrarSoloFavoritos = esOfflineReal || isModoOfflineManual

    val snackbarHostState = remember { SnackbarHostState() }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Mostrar teclado al activar búsqueda
    LaunchedEffect(isSearching) {
        if (isSearching) focusRequester.requestFocus()
    }

    if (esOfflineReal) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Error de red: Mostrando datos locales.")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!esTablet) {
                TopAppBar(
                    title = {
                        if (isSearching) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Buscar evento...", color = Color.White.copy(alpha = 0.7f)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        } else {
                            Text(
                                text = when (currentRoute) {
                                    "favoritos" -> "Mis Favoritos"
                                    "configuracion" -> "Ajustes"
                                    else -> "TechEvent"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        if (isSearching || (currentRoute != "catalogo" && currentRoute != null)) {
                            IconButton(onClick = {
                                if (isSearching) { isSearching = false; searchQuery = "" }
                                else { navController.navigate("catalogo") { popUpTo("catalogo") { inclusive = false } } }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                        }
                    },
                    actions = {
                        if (currentRoute == "catalogo" && !isSearching) {
                            IconButton(onClick = { isSearching = true }) { Icon(Icons.Default.Search, null, tint = Color.White) }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (currentRoute?.startsWith("detalle") == true) Color.Transparent else DarkBlue,
                        titleContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (!esTablet && currentRoute != null && !currentRoute.startsWith("detalle")) {
                NavigationBar(containerColor = if (isDarkMode) Color.Black else Color.White) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, null) },
                        label = { Text("Eventos") },
                        selected = currentRoute == "catalogo",
                        onClick = { navController.navigate("catalogo") { popUpTo("catalogo") { inclusive = true } } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, null) },
                        label = { Text("Favoritos") },
                        selected = currentRoute == "favoritos",
                        onClick = { navController.navigate("favoritos") { launchSingleTop = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Ajustes") },
                        selected = currentRoute == "configuracion",
                        onClick = { navController.navigate("configuracion") { launchSingleTop = true } }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(if (currentRoute != null && !currentRoute.startsWith("detalle")) padding else PaddingValues(0.dp)).fillMaxSize().background(if (isDarkMode) Color.Black else BackgroundApp)) {
            when (val result = state) {
                is EventoUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is EventoUiState.Success -> {
                    // Lógica de filtrado unificada
                    val listaFiltrada = result.lista.filter {
                        it.titulo.contains(searchQuery, ignoreCase = true)
                    }.let {
                        if (mostrarSoloFavoritos || currentRoute == "favoritos") it.filter { e -> favoritosIds.contains(e.id) }
                        else it
                    }

                    NavHost(navController, startDestination = "catalogo") {
                        composable("catalogo") {
                            CatalogoLista(listaFiltrada, favoritosIds, { navController.navigate("detalle/${it.id}") }, { viewModel.toggleFavorito(it) }, isDarkMode)
                        }
                        composable("favoritos") {
                            if (listaFiltrada.isEmpty()) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No hay favoritos guardados", color = Color.Gray) }
                            } else {
                                CatalogoLista(listaFiltrada, favoritosIds, { navController.navigate("detalle/${it.id}") }, { viewModel.toggleFavorito(it) }, isDarkMode)
                            }
                        }
                        composable("configuracion") {
                            SidebarConfiguracion(isDarkMode, onToggleTheme, isModoOfflineManual) { isModoOfflineManual = it }
                        }
                        composable("detalle/{eventId}", arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { backStack ->
                            val id = backStack.arguments?.getString("eventId")
                            result.lista.find { it.id == id }?.let {
                                PantallaDetalle(it, favoritosIds.contains(it.id)) { viewModel.toggleFavorito(it) }
                            }
                        }
                    }
                }
                is EventoUiState.Error -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error al cargar eventos", color = if(isDarkMode) Color.White else Color.Black)
                    Button(onClick = { viewModel.cargarEventos() }) { Text("Reintentar") }
                }
            }
        }
    }
}

@Composable
fun SidebarConfiguracion(isDarkMode: Boolean, onToggleTheme: (Boolean) -> Unit, isOffline: Boolean, onToggleOffline: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuración", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if(isDarkMode) Color.White else DarkBlue, modifier = Modifier.padding(bottom = 24.dp))

        Text("Apariencia", color = Color.Gray, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onToggleTheme(!isDarkMode) }.padding(vertical = 8.dp)) {
            Text(if (isDarkMode) "🌙 Tema Oscuro" else "☀️ Tema Claro", color = if(isDarkMode) Color.White else Color.Black, modifier = Modifier.weight(1f))
            Switch(checked = isDarkMode, onCheckedChange = onToggleTheme)
        }

        Spacer(Modifier.height(24.dp))
        Text("General", color = Color.Gray, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Modo sin conexión", fontWeight = FontWeight.Bold, color = if(isDarkMode) Color.White else Color.Black)
                Text("Mostrar solo eventos en Room", color = Color.Gray, fontSize = 11.sp)
            }
            Switch(checked = isOffline, onCheckedChange = onToggleOffline)
        }
    }
}

@Composable
fun CatalogoLista(lista: List<EventoNetwork>, favoritosIds: Set<String>, onEventoClick: (EventoNetwork) -> Unit, onFavClick: (EventoNetwork) -> Unit, isDarkMode: Boolean) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(lista) { evento ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onEventoClick(evento) },
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = evento.bannerUrl, contentDescription = null, Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop, placeholder = painterResource(android.R.drawable.ic_menu_gallery))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(evento.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDarkMode) Color.White else Color.Black)
                        Text(evento.fecha, fontSize = 12.sp, color = Color.Gray)
                        Surface(shape = RoundedCornerShape(8.dp), color = PillGreenBg,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(evento.estado, color = PillGreenText, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    IconButton(onClick = { onFavClick(evento) }) {
                        Icon(Icons.Default.Favorite, null, tint = if (favoritosIds.contains(evento.id)) Color.Red else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDetalle(evento: EventoNetwork, isFav: Boolean, onFavClick: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Color.White)) {
        Box {
            AsyncImage(model = evento.bannerUrl, contentDescription = null, Modifier.fillMaxWidth().height(250.dp), contentScale = ContentScale.Crop)
            IconButton(onClick = onFavClick, Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isFav) Color.Red else Color.White)
            }
        }
        Column(Modifier.padding(24.dp)) {
            Text(evento.titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Text("${evento.fecha} • ${evento.lugar}", color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Text("Descripción", fontWeight = FontWeight.Bold, color = DarkBlue)
            Text(evento.descripcion, color = Color.DarkGray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Text("Ponentes", fontWeight = FontWeight.Bold, color = DarkBlue)
            Text(evento.ponentes, color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}