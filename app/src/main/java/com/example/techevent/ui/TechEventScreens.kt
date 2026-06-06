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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.techevent.data.EventoNetwork
import com.example.techevent.data.ThemePreferences
import kotlinx.coroutines.launch

// Colores base para las etiquetas
val PillGreenBg = Color(0xFFE8F5E9)
val PillGreenText = Color(0xFF2E7D32)
val PillRedBg = Color(0xFFFFEBEE)
val PillRedText = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationContainer(viewModel: EventViewModel, esTablet: Boolean) {
    val state by viewModel.uiState.collectAsState()
    val favoritosIds by viewModel.idFavoritos.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var eventoSeleccionadoTablet by remember { mutableStateOf<EventoNetwork?>(null) }
    var tabSeleccionada by remember { mutableStateOf(0) }

    var isModoOfflineManual by remember { mutableStateOf(false) }
    val esOfflineReal = (state as? EventoUiState.Success)?.esOffline == true
    val mostrarOffline = esOfflineReal || isModoOfflineManual

    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val isDarkMode by themePreferences.esModoOscuro.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }


    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }


    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    if (esOfflineReal) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Sin internet. Auto-activando modo offline.")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!esTablet) {
                if (currentRoute == "catalogo") {
                    TopAppBar(
                        title = {
                            if (isSearching) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Buscar evento...", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                )
                            } else {
                                Text("TechEvent", fontWeight = FontWeight.Bold)
                            }
                        },
                        navigationIcon = {
                            if (isSearching) {
                                IconButton(onClick = { isSearching = false; searchQuery = "" }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        },
                        actions = {
                            if (isSearching) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            } else {
                                IconButton(onClick = { isSearching = true }) { 
                                    Icon(Icons.Default.Search, contentDescription = "Buscar") 
                                }
                                IconButton(onClick = { }) { 
                                    Icon(Icons.Default.Menu, contentDescription = "Filtrar") 
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                } else if (currentRoute?.startsWith("detalle") == true) {
                    TopAppBar(
                        title = { Text("") },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (!esTablet && currentRoute == "catalogo") {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(icon = { Icon(Icons.Default.DateRange, contentDescription = "Eventos") }, label = { Text("Eventos") }, selected = tabSeleccionada == 0, onClick = { tabSeleccionada = 0 })
                    NavigationBarItem(icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Favoritos") }, label = { Text("Favoritos") }, selected = tabSeleccionada == 1, onClick = { tabSeleccionada = 1 })
                    NavigationBarItem(icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") }, label = { Text("Configuración") }, selected = tabSeleccionada == 2, onClick = { tabSeleccionada = 2 })
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(if (!esTablet && currentRoute == "catalogo") padding else PaddingValues(0.dp)).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (val result = state) {
                is EventoUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is EventoUiState.Error -> Text("Error: ${result.mensaje}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is EventoUiState.Success -> {
                    val listaCompleta = result.lista
                    val listaAMostrar = if (mostrarOffline) listaCompleta.filter { favoritosIds.contains(it.id) } else listaCompleta

                    // busqueda
                    val listaFiltrada = if (searchQuery.isBlank()) {
                        listaAMostrar
                    } else {
                        listaAMostrar.filter {
                            it.titulo.contains(searchQuery, ignoreCase = true) ||
                                    it.ponentes.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (esTablet) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(0.25f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant)) {
                                SidebarConfiguracion(
                                    isOffline = mostrarOffline,
                                    onOfflineToggle = { isModoOfflineManual = it },
                                    isDarkMode = isDarkMode,
                                    onThemeChange = { scope.launch { themePreferences.guardarModoOscuro(it) } }
                                )
                            }
                            Box(modifier = Modifier.weight(0.35f).fillMaxHeight()) {
                                Column {
                                    Text(if (mostrarOffline) "Eventos Descargados" else "Próximos Eventos", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(16.dp))
                                    CatalogoLista(listaFiltrada, favoritosIds, { eventoSeleccionadoTablet = it }, { viewModel.toggleFavorito(it) })
                                }
                            }
                            Box(modifier = Modifier.weight(0.40f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant)) {
                                if (eventoSeleccionadoTablet != null) {
                                    PantallaDetalle(eventoSeleccionadoTablet!!, favoritosIds.contains(eventoSeleccionadoTablet!!.id), { viewModel.toggleFavorito(eventoSeleccionadoTablet!!) })
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Selecciona un evento", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    } else {
                        NavHost(navController = navController, startDestination = "catalogo") {
                            composable("catalogo") {
                                when (tabSeleccionada) {
                                    0 -> {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            if (!isSearching) {
                                                Text(if (mostrarOffline) "Eventos Descargados" else "Próximos Eventos", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(16.dp))
                                            }
                                            if (listaFiltrada.isEmpty()) {
                                                val msg = if (mostrarOffline && searchQuery.isBlank()) "No tienes eventos guardados." else "No se encontraron resultados."
                                                Text(msg, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                                            } else {
                                                CatalogoLista(listaFiltrada, favoritosIds, onEventoClick = { navController.navigate("detalle/${it.id}") }, onFavClick = { viewModel.toggleFavorito(it) })
                                            }
                                        }
                                    }
                                    1 -> {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Text("Mis Favoritos", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(16.dp))
                                            val listaFavoritos = listaFiltrada.filter { favoritosIds.contains(it.id) }
                                            if (listaFavoritos.isEmpty()) {
                                                Text("Aún no tienes favoritos guardados.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                                            } else {
                                                CatalogoLista(listaFavoritos, favoritosIds, onEventoClick = { navController.navigate("detalle/${it.id}") }, onFavClick = { viewModel.toggleFavorito(it) })
                                            }
                                        }
                                    }
                                    2 -> {
                                        SidebarConfiguracion(
                                            isOffline = mostrarOffline,
                                            onOfflineToggle = { isModoOfflineManual = it },
                                            isDarkMode = isDarkMode,
                                            onThemeChange = { scope.launch { themePreferences.guardarModoOscuro(it) } }
                                        )
                                    }
                                }
                            }
                            composable("detalle/{eventId}") { backStackEntry ->
                                val eventId = backStackEntry.arguments?.getString("eventId")
                                val evento = listaCompleta.find { it.id == eventId }
                                if (evento != null) {
                                    PantallaDetalle(evento, favoritosIds.contains(evento.id), { viewModel.toggleFavorito(evento) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SidebarConfiguracion(isOffline: Boolean, onOfflineToggle: (Boolean) -> Unit, isDarkMode: Boolean, onThemeChange: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuración", color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp, top = 16.dp))

        Text("Apariencia", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onThemeChange(false) }.padding(vertical = 8.dp)) {
            Text("", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Tema claro", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, modifier = Modifier.weight(1f))
            RadioButton(selected = !isDarkMode, onClick = { onThemeChange(false) })
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onThemeChange(true) }.padding(vertical = 8.dp)) {
            Text("", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Tema oscuro", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, modifier = Modifier.weight(1f))
            RadioButton(selected = isDarkMode, onClick = { onThemeChange(true) })
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("General", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Modo sin conexión", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Usar datos locales de Room", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 14.sp)
            }
            Switch(checked = isOffline, onCheckedChange = { onOfflineToggle(it) })
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Acerca de", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
        Text("TechEvent App\nVersión 1.0.0", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)

        Spacer(modifier = Modifier.weight(1f))
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("❤️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("para la comunidad tech", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun CatalogoLista(lista: List<EventoNetwork>, favoritosIds: Set<String>, onEventoClick: (EventoNetwork) -> Unit, onFavClick: (EventoNetwork) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        items(lista) { evento ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onEventoClick(evento) }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = evento.bannerUrl,
                        contentDescription = "Banner",
                        placeholder = painterResource(id = android.R.drawable.ic_menu_report_image),
                        error = painterResource(id = android.R.drawable.ic_delete),
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = evento.titulo, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${evento.fecha}\n${evento.lugar}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val isAvailable = evento.estado == "Cupos Disponibles"
                        Surface(shape = RoundedCornerShape(12.dp), color = if (isAvailable) PillGreenBg else PillRedBg) {
                            Text(
                                text = evento.estado,
                                color = if (isAvailable) PillGreenText else PillRedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    IconButton(onClick = { onFavClick(evento) }, modifier = Modifier.align(Alignment.Top)) {
                        Icon(
                            imageVector = if (favoritosIds.contains(evento.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (favoritosIds.contains(evento.id)) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDetalle(evento: EventoNetwork, isFav: Boolean = false, onFavClick: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            AsyncImage(
                model = evento.bannerUrl,
                contentDescription = "Banner",
                placeholder = painterResource(id = android.R.drawable.ic_menu_report_image),
                error = painterResource(id = android.R.drawable.ic_delete),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Row(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                IconButton(onClick = { onFavClick() }) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFav) Color.Red else Color.White
                    )
                }
                IconButton(onClick = {  }) { Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White) }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(text = evento.titulo, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = "Fecha", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = " ${evento.fecha}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.LocationOn, contentDescription = "Lugar", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = " ${evento.lugar}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))

            val isAvailable = evento.estado == "Cupos Disponibles"
            Surface(shape = RoundedCornerShape(12.dp), color = if (isAvailable) PillGreenBg else PillRedBg) {
                Text(evento.estado, color = if (isAvailable) PillGreenText else PillRedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Descripción", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(evento.descripcion, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)

            Spacer(modifier = Modifier.height(24.dp))
            Text("Ponentes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(evento.ponentes, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))
            Text("Agenda", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(evento.agenda, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
