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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.techevent.data.EventoNetwork


val UmaRed = Color(0xFFC62828)
val UmaBlue = Color(0xFF1565C0)
val UmaGold = Color(0xFFFFD54F)
val UmaWhite = Color(0xFFFFFFFF)
val BackgroundApp = Color(0xFFF4F6F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationContainer(
    eventViewModel: EventViewModel,
    loginViewModel: LoginViewModel,
    esTablet: Boolean,
    isDarkMode: Boolean,
    isLoggedIn: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val state by eventViewModel.uiState.collectAsState()
    val favoritosIds by eventViewModel.idFavoritos.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoginScreen = currentRoute == "login"

    var isModoOfflineManual by rememberSaveable { mutableStateOf(false) }
    val esOfflineReal = (state as? EventoUiState.Success)?.esOffline == true
    val mostrarSoloFavoritos = esOfflineReal || isModoOfflineManual

    val snackbarHostState = remember { SnackbarHostState() }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

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
            if (!esTablet && !isLoginScreen) {
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
                                    else -> "UMAevent"
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
                        containerColor = if (currentRoute?.startsWith("detalle") == true) Color.Transparent else UmaBlue, // Color UMA
                        titleContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (!esTablet && currentRoute != null && !currentRoute.startsWith("detalle") && !isLoginScreen) {
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
        val innerPadding = if (isLoginScreen || currentRoute?.startsWith("detalle") == true) PaddingValues(0.dp) else padding

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(if (isDarkMode) Color.Black else BackgroundApp)) {

            val startDest = if (isLoggedIn) "catalogo" else "login"

            NavHost(navController, startDestination = startDest) {

                composable("login") {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            navController.navigate("catalogo") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                composable("catalogo") {
                    when (val result = state) {
                        is EventoUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        is EventoUiState.Success -> {
                            val listaFiltrada = result.lista.filter {
                                it.titulo.contains(searchQuery, ignoreCase = true)
                            }.let {
                                if (mostrarSoloFavoritos) it.filter { e -> favoritosIds.contains(e.id) } else it
                            }
                            CatalogoLista(listaFiltrada, favoritosIds, { navController.navigate("detalle/${it.id}") }, { eventViewModel.toggleFavorito(it) }, isDarkMode)
                        }
                        is EventoUiState.Error -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error al cargar eventos", color = if(isDarkMode) Color.White else Color.Black)
                            Button(onClick = { eventViewModel.cargarEventos() }, colors = ButtonDefaults.buttonColors(containerColor = UmaRed)) { Text("Reintentar") }
                        }
                    }
                }

                composable("favoritos") {
                    when (val result = state) {
                        is EventoUiState.Success -> {
                            val listaFav = result.lista.filter { favoritosIds.contains(it.id) && it.titulo.contains(searchQuery, ignoreCase = true) }
                            if (listaFav.isEmpty()) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No hay favoritos guardados", color = Color.Gray) }
                            } else {
                                CatalogoLista(listaFav, favoritosIds, { navController.navigate("detalle/${it.id}") }, { eventViewModel.toggleFavorito(it) }, isDarkMode)
                            }
                        }
                        else -> {}
                    }
                }

                // 🛠️ RUTA ACTUALIZADA: Le pasamos la acción de Cerrar Sesión a la configuración
                composable("configuracion") {
                    SidebarConfiguracion(
                        isDarkMode = isDarkMode,
                        onToggleTheme = onToggleTheme,
                        isOffline = isModoOfflineManual,
                        onToggleOffline = { isModoOfflineManual = it },
                        onLogout = {
                            loginViewModel.logout() // Actualiza DataStore y ViewModel
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true } // Borra todo el historial de pantallas
                            }
                        }
                    )
                }

                composable("detalle/{eventId}", arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { backStack ->
                    val id = backStack.arguments?.getString("eventId")
                    if (state is EventoUiState.Success) {
                        (state as EventoUiState.Success).lista.find { it.id == id }?.let { evento ->
                            PantallaDetalle(evento, favoritosIds.contains(evento.id)) { eventViewModel.toggleFavorito(evento) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(UmaBlue), contentAlignment = Alignment.Center) {
        ElevatedCard(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = UmaWhite)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = "https://cdn-icons-png.flaticon.com/512/2776/2776067.png",
                    contentDescription = "Logo UMA",
                    modifier = Modifier.size(100.dp).padding(bottom = 16.dp)
                )
                Text("UMAevent App", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = UmaRed)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = username, onValueChange = { username = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation()
                )
                if (uiState.isError) {
                    Text(text = uiState.errorMessage, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.login(username, password) }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = UmaRed), enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(color = UmaWhite, modifier = Modifier.size(24.dp))
                    else Text("Ingresar", color = UmaWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// 🛠️ FUNCIÓN ACTUALIZADA: Recibe onLogout y dibuja el botón
@Composable
fun SidebarConfiguracion(
    isDarkMode: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    isOffline: Boolean,
    onToggleOffline: (Boolean) -> Unit,
    onLogout: () -> Unit // Parámetro añadido
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuración", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if(isDarkMode) UmaWhite else UmaBlue, modifier = Modifier.padding(bottom = 24.dp))

        Text("Apariencia", color = Color.Gray, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onToggleTheme(!isDarkMode) }.padding(vertical = 8.dp)) {
            Text(if (isDarkMode) " Tema Oscuro" else "  Tema Claro", color = if(isDarkMode) UmaWhite else Color.Black, modifier = Modifier.weight(1f))
            Switch(checked = isDarkMode, onCheckedChange = onToggleTheme, colors = SwitchDefaults.colors(checkedThumbColor = UmaRed, checkedTrackColor = UmaGold.copy(alpha = 0.5f)))
        }

        Spacer(Modifier.height(24.dp))
        Text("General", color = Color.Gray, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Modo sin conexión", fontWeight = FontWeight.Bold, color = if(isDarkMode) UmaWhite else Color.Black)
                Text("Mostrar solo eventos en Room", color = Color.Gray, fontSize = 11.sp)
            }
            Switch(checked = isOffline, onCheckedChange = onToggleOffline, colors = SwitchDefaults.colors(checkedThumbColor = UmaRed, checkedTrackColor = UmaGold.copy(alpha = 0.5f)))
        }

        // 🎨 NUEVO: Botón de Cerrar Sesión empujado hacia abajo
        Spacer(Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UmaRed)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = UmaWhite)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar Sesión", color = UmaWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun CatalogoLista(lista: List<EventoNetwork>, favoritosIds: Set<String>, onEventoClick: (EventoNetwork) -> Unit, onFavClick: (EventoNetwork) -> Unit, isDarkMode: Boolean) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(lista) { evento ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onEventoClick(evento) },
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1E1E) else UmaWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = evento.bannerUrl, contentDescription = null, Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop, placeholder = painterResource(android.R.drawable.ic_menu_gallery))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(evento.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDarkMode) UmaWhite else Color.Black)
                        Text(evento.fecha, fontSize = 12.sp, color = Color.Gray)
                        Surface(shape = RoundedCornerShape(8.dp), color = UmaGold.copy(alpha = 0.2f), modifier = Modifier.padding(top = 4.dp)) {
                            Text(evento.estado, color = UmaBlue, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    IconButton(onClick = { onFavClick(evento) }) {
                        Icon(Icons.Default.Favorite, null, tint = if (favoritosIds.contains(evento.id)) UmaRed else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDetalle(evento: EventoNetwork, isFav: Boolean, onFavClick: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(UmaWhite)) {
        Box {
            AsyncImage(model = evento.bannerUrl, contentDescription = null, Modifier.fillMaxWidth().height(250.dp), contentScale = ContentScale.Crop)
            IconButton(onClick = onFavClick, Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isFav) UmaRed else UmaWhite)
            }
        }
        Column(Modifier.padding(24.dp)) {
            Text(evento.titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = UmaBlue)
            Text("${evento.fecha} • ${evento.lugar}", color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Text("Descripción", fontWeight = FontWeight.Bold, color = UmaBlue)
            Text(evento.descripcion, color = Color.DarkGray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Text("Ponentes", fontWeight = FontWeight.Bold, color = UmaBlue)
            Text(evento.ponentes, color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}