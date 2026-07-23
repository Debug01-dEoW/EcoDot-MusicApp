package com.example.ecodot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ecodot.ui.components.MiniPlayer
import com.example.ecodot.ui.components.liquidGlass
import com.example.ecodot.ui.screens.HomeScreen
import com.example.ecodot.ui.screens.LibraryScreen
import com.example.ecodot.ui.screens.NowPlayingScreen
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.EcoDotTheme
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.util.RequestNotificationPermission
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay

val LocalHazeState = compositionLocalOf { HazeState() }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MusicViewModel = viewModel()
            
            // Sync progress updater with activity lifecycle (stops background polling to save CPU)
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    when (event) {
                        androidx.lifecycle.Lifecycle.Event.ON_START -> {
                            viewModel.startPositionUpdater()
                        }
                        androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                            viewModel.stopPositionUpdater()
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val dominantColor by viewModel.dominantColor.collectAsState()
            val accentColor by viewModel.accentColor.collectAsState()

            EcoDotTheme(dominantColor = dominantColor, accentColor = accentColor) {
                val navController = rememberNavController()
                val currentTrack by viewModel.currentTrack.collectAsState()
                val isPlaying by viewModel.isPlaying.collectAsState()
                val hasTrackLoaded by viewModel.hasTrackLoaded.collectAsState()
                val position by viewModel.position.collectAsState()
                val duration by viewModel.duration.collectAsState()
                val isCurrentTrackLiked by viewModel.isCurrentTrackLiked.collectAsState()

                // Smoothly animate ambient haze color as song changes
                val animatedAmbient by animateColorAsState(
                    targetValue = dominantColor.copy(alpha = 0.13f),
                    animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                    label = "ambient_haze"
                )

                val progress = remember(position, duration) {
                    if (duration > 0) position.toFloat() / duration.toFloat() else 0f
                }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route

                var isNowPlayingVisible by remember { mutableStateOf(false) }
                var isEqualizerVisible by remember { mutableStateOf(false) }
                var isVideoPlayerVisible by remember { mutableStateOf(false) }
                val appHazeState = remember { HazeState() }

                // Hardware back button handling
                BackHandler(enabled = isNowPlayingVisible || isEqualizerVisible || isVideoPlayerVisible) {
                    when {
                        isVideoPlayerVisible -> isVideoPlayerVisible = false
                        isEqualizerVisible -> isEqualizerVisible = false
                        isNowPlayingVisible -> isNowPlayingVisible = false
                    }
                }

                val networkState by viewModel.networkState.collectAsState()
                var showOfflineBanner by remember { mutableStateOf(false) }
                var bannerMessage by remember { mutableStateOf("") }
                var isBannerSuccess by remember { mutableStateOf(false) }

                LaunchedEffect(networkState.isConnected) {
                    if (!networkState.isConnected) {
                        bannerMessage = "No Internet Connection. Playing downloads only."
                        isBannerSuccess = false
                        showOfflineBanner = true
                    } else {
                        if (showOfflineBanner && !isBannerSuccess) {
                            bannerMessage = "Back Online!"
                            isBannerSuccess = true
                            delay(3000)
                            showOfflineBanner = false
                        }
                    }
                }

                RequestNotificationPermission()

                CompositionLocalProvider(LocalHazeState provides appHazeState) {
                    Scaffold(
                        containerColor = Color(0xFF000000),
                    ) { _ ->
                        Box(modifier = Modifier.fillMaxSize().hazeSource(appHazeState)) {

                            // ── Global Ambient Haze (album art color bleeds into all screens) ──
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                animatedAmbient,
                                                Color.Transparent
                                            ),
                                            center = androidx.compose.ui.geometry.Offset(540f, -200f),
                                            radius = 1400f
                                        )
                                    )
                            )

                            // ── Page Content ──────────────────────────────────
                            NavHost(
                                navController = navController,
                                startDestination = "home",
                                modifier = Modifier.fillMaxSize(),
                                enterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                                    ) + fadeIn(animationSpec = tween(300))
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                                    ) + fadeOut(animationSpec = tween(300))
                                },
                                popEnterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                                    ) + fadeIn(animationSpec = tween(300))
                                },
                                popExitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                                    ) + fadeOut(animationSpec = tween(300))
                                }
                            ) {
                                composable("home") {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        navController = navController,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                composable("library") {
                                    LibraryScreen(
                                        viewModel = viewModel,
                                        navController = navController,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                composable("daily_mix") {
                                    com.example.ecodot.ui.screens.DailyMixGeneratorScreen(
                                        viewModel = viewModel,
                                        navController = navController,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                composable("search") {
                                    com.example.ecodot.ui.screens.SearchScreen(
                                        viewModel = viewModel,
                                        navController = navController,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                composable(
                                    route = "artist/{artistId}",
                                    arguments = listOf(navArgument("artistId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                                    com.example.ecodot.ui.screens.ArtistProfileScreen(
                                        artistId = artistId,
                                        musicViewModel = viewModel,
                                        navController = navController
                                    )
                                }
                                composable(
                                    route = "album/{albumId}",
                                    arguments = listOf(navArgument("albumId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
                                    com.example.ecodot.ui.screens.AlbumDetailScreen(
                                        albumId = albumId,
                                        musicViewModel = viewModel,
                                        navController = navController
                                    )
                                }
                                composable("profile") {
                                    com.example.ecodot.ui.screens.ProfileScreen(
                                        viewModel = viewModel,
                                        navController = navController,
                                        onEqualizerClick = { isEqualizerVisible = true },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // ── Dynamic Island Nav Bar ────────────────────────
                            val showBottomBar = currentDestination in listOf("home", "library", "search", "artist/{artistId}")
                            AnimatedVisibility(
                                visible = showBottomBar && !isNowPlayingVisible,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 16.dp)
                            ) {
                                DynamicIslandNavBar(
                                    currentRoute = currentDestination,
                                    onTabSelected = { route ->
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    hazeState = appHazeState
                                )
                            }

                            // ── Mini Player (floats above island nav) ─────────
                            AnimatedVisibility(
                                visible = hasTrackLoaded && currentTrack != null && !isNowPlayingVisible,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 80.dp, start = 12.dp, end = 12.dp)
                            ) {
                                MiniPlayer(
                                    track = currentTrack,
                                    isPlaying = isPlaying,
                                    progress = progress,
                                    onTogglePlayback = { viewModel.togglePlayback() },
                                    onSkipNext = { viewModel.skipNext() },
                                    onSkipPrevious = { viewModel.skipPrevious() },
                                    onLike = { viewModel.likeCurrentTrack() },
                                    isLiked = isCurrentTrackLiked,
                                    onClick = { isNowPlayingVisible = true },
                                    hazeState = appHazeState
                                )
                            }

                            // ── Full Screen Now Playing overlay ───────────────
                            // Sync canvas lifecycle: release video player when screen is dismissed
                            LaunchedEffect(isNowPlayingVisible) {
                                viewModel.setNowPlayingVisible(isNowPlayingVisible)
                            }

                            AnimatedVisibility(
                                visible = isNowPlayingVisible,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                NowPlayingScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    onBackClick = { isNowPlayingVisible = false },
                                    onEqualizerClick = { isEqualizerVisible = true },
                                    onWatchVideoClick = { isVideoPlayerVisible = true },
                                    isFullScreenVisible = isVideoPlayerVisible
                                )
                            }

                            // ── Video Player Overlay ──────────────────────────
                            AnimatedVisibility(
                                visible = isVideoPlayerVisible,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                com.example.ecodot.ui.screens.VideoPlayerScreen(
                                    viewModel = viewModel,
                                    onClose = { isVideoPlayerVisible = false }
                                )
                            }

                            // ── Equalizer Overlay ─────────────────────────────
                            AnimatedVisibility(
                                visible = isEqualizerVisible,
                                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                com.example.ecodot.ui.screens.EqualizerScreen(
                                    viewModel = viewModel,
                                    onBack = { isEqualizerVisible = false }
                                )
                            }

                            // ── Floating Offline/Online Banner ───────────────
                            AnimatedVisibility(
                                visible = showOfflineBanner,
                                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .statusBarsPadding()
                                    .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                                    .zIndex(999f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isBannerSuccess) {
                                                Color(0xFF0D2C1D).copy(alpha = 0.9f)
                                            } else {
                                                Color(0xFF2E1216).copy(alpha = 0.9f)
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.horizontalGradient(
                                                colors = if (isBannerSuccess) {
                                                    listOf(Color(0xFF1DB954).copy(alpha = 0.6f), Color(0xFF1DB954).copy(alpha = 0.1f))
                                                } else {
                                                    listOf(Color(0xFFFF3B30).copy(alpha = 0.6f), Color(0xFFFF3B30).copy(alpha = 0.1f))
                                                }
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = if (isBannerSuccess) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                                            contentDescription = null,
                                            tint = if (isBannerSuccess) Color(0xFF1DB954) else Color(0xFFFF3B30),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = bannerMessage,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                        } // end Box
                    } // end Scaffold
                } // end CompositionLocalProvider
            } // end EcoDotTheme
        } // end setContent
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dynamic Island Nav Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DynamicIslandNavBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    hazeState: HazeState? = null
) {
    val navItems = listOf(
        Triple("home",    Icons.Rounded.Home,         "Home"),
        Triple("search",  Icons.Rounded.Search,       "Search"),
        Triple("library", Icons.Rounded.LibraryMusic, "Library"),
    )

    val selectedIndex = when (currentRoute) {
        "home" -> 0
        "search" -> 1
        "library" -> 2
        else -> -1
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .liquidGlass(
                shape = RoundedCornerShape(percent = 50),
                specularAlpha = 0.38f,
                elevation = 16.dp,
                hazeState = hazeState,
                tintColor = Color(0xFF10111A),
                blurRadius = 24.dp
            )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // Sliding indicator pill background
            if (selectedIndex != -1) {
                val itemWidth = 60.dp
                val spacing = 6.dp
                val indicatorOffset by animateDpAsState(
                    targetValue = (selectedIndex * (60 + 6)).dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "nav_indicator_offset"
                )

                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .size(width = itemWidth, height = 44.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color(0xFF4A1A1A)) // Premium dark reddish-brown burgundy tone
                        .border(
                            width = 1.dp,
                            color = Color(0xFFFF4D4D).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(percent = 50)
                        )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { (route, icon, label) ->
                    val isSelected = currentRoute == route
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "island_scale"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFFFF4D4D) else Color.White.copy(alpha = 0.55f),
                        animationSpec = tween(220),
                        label = "island_icon_color"
                    )

                    Box(
                        modifier = Modifier
                            .size(width = 60.dp, height = 44.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(route) }
                            )
                            .scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
