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
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.ecodot.ui.components.MiniPlayer
import com.example.ecodot.ui.components.SimpBottomNavigationBar
import com.example.ecodot.ui.components.liquidGlass
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.example.ecodot.ui.screens.HomeScreen
import com.example.ecodot.ui.screens.LibraryScreen
import com.example.ecodot.ui.screens.NowPlayingScreen
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.EcoDotTheme
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.util.RequestNotificationPermission
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
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
                val isCurrentTrackLiked by viewModel.isCurrentTrackLiked.collectAsState()

                // Smoothly animate ambient haze color as song changes
                val animatedAmbient by animateColorAsState(
                    targetValue = dominantColor.copy(alpha = 0.13f),
                    animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                    label = "ambient_haze"
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route

                var isNowPlayingVisible by remember { mutableStateOf(false) }
                var isEqualizerVisible by remember { mutableStateOf(false) }
                var isVideoPlayerVisible by remember { mutableStateOf(false) }
                val appHazeState = remember { HazeState() }
                val appBackdrop = rememberLayerBackdrop()

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

                val deviceTilt by com.example.ecodot.ui.components.rememberDeviceTilt()

                CompositionLocalProvider(
                    LocalHazeState provides appHazeState,
                    com.example.ecodot.ui.components.LocalBackdrop provides appBackdrop,
                    com.example.ecodot.ui.components.LocalTiltState provides deviceTilt
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF000000))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .layerBackdrop(appBackdrop)
                                .hazeSource(appHazeState)
                        ) {

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
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = "home",
                                    modifier = Modifier.fillMaxSize(),
                                    enterTransition = {
                                        val topLevel = setOf("home", "search", "library")
                                        val isBetweenTopLevel = initialState.destination.route in topLevel && targetState.destination.route in topLevel
                                        if (isBetweenTopLevel) {
                                            fadeIn(animationSpec = tween(160))
                                        } else {
                                            slideIntoContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Start,
                                                animationSpec = tween(250, easing = FastOutSlowInEasing)
                                            ) + fadeIn(animationSpec = tween(200))
                                        }
                                    },
                                    exitTransition = {
                                        val topLevel = setOf("home", "search", "library")
                                        val isBetweenTopLevel = initialState.destination.route in topLevel && targetState.destination.route in topLevel
                                        if (isBetweenTopLevel) {
                                            fadeOut(animationSpec = tween(160))
                                        } else {
                                            slideOutOfContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Start,
                                                animationSpec = tween(250, easing = FastOutSlowInEasing)
                                            ) + fadeOut(animationSpec = tween(200))
                                        }
                                    },
                                    popEnterTransition = {
                                        val topLevel = setOf("home", "search", "library")
                                        val isBetweenTopLevel = initialState.destination.route in topLevel && targetState.destination.route in topLevel
                                        if (isBetweenTopLevel) {
                                            fadeIn(animationSpec = tween(160))
                                        } else {
                                            slideIntoContainer(
                                                AnimatedContentTransitionScope.SlideDirection.End,
                                                animationSpec = tween(250, easing = FastOutSlowInEasing)
                                            ) + fadeIn(animationSpec = tween(200))
                                        }
                                    },
                                    popExitTransition = {
                                        val topLevel = setOf("home", "search", "library")
                                        val isBetweenTopLevel = initialState.destination.route in topLevel && targetState.destination.route in topLevel
                                        if (isBetweenTopLevel) {
                                            fadeOut(animationSpec = tween(160))
                                        } else {
                                            slideOutOfContainer(
                                                AnimatedContentTransitionScope.SlideDirection.End,
                                                animationSpec = tween(250, easing = FastOutSlowInEasing)
                                            ) + fadeOut(animationSpec = tween(200))
                                        }
                                    }
                                ) {
                                    composable("home") {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        navController = navController,
                                        onEqualizerClick = { isEqualizerVisible = true },
                                        onWatchVideo = {
                                            viewModel.setVideoMode(true)
                                            isVideoPlayerVisible = true
                                        },
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
                                composable("search") {
                                    com.example.ecodot.ui.screens.SearchScreen(
                                        viewModel = viewModel,
                                        navController = navController,
                                        modifier = Modifier.fillMaxSize(),
                                        onWatchVideo = {
                                            viewModel.setVideoMode(true)
                                            isVideoPlayerVisible = true
                                        }
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
                        } // end Box (Page Content)
                    } // end Box (layerBackdrop & hazeSource)

                        // ── Floating Controls Layer (No black container — true floating over content) ──
                        val showBottomBar = currentDestination in listOf("home", "library", "search", "artist/{artistId}", "album/{albumId}")
                        AnimatedVisibility(
                            visible = showBottomBar && !isNowPlayingVisible,
                            enter = fadeIn(animationSpec = tween(200)),
                            exit = fadeOut(animationSpec = tween(150)),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // MiniPlayer directly on top of floating squircle buttons
                                AnimatedVisibility(
                                    visible = hasTrackLoaded && currentTrack != null,
                                    enter = fadeIn() + slideInVertically { it },
                                    exit = fadeOut() + slideOutVertically { it }
                                ) {
                                    MiniPlayer(
                                        track = currentTrack,
                                        isPlaying = isPlaying,
                                        progressProvider = {
                                            val d = viewModel.duration.value
                                            if (d > 0) viewModel.position.value.toFloat() / d.toFloat() else 0f
                                        },
                                        dominantColor = dominantColor,
                                        onTogglePlayback = { viewModel.togglePlayback() },
                                        onSkipNext = { viewModel.skipNext() },
                                        onSkipPrevious = { viewModel.skipPrevious() },
                                        onLike = { viewModel.likeCurrentTrack() },
                                        isLiked = isCurrentTrackLiked,
                                        onClick = { isNowPlayingVisible = true },
                                        onClose = { viewModel.togglePlayback() },
                                        backdrop = appBackdrop,
                                        modifier = Modifier
                                            .height(56.dp)
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                            .padding(bottom = 6.dp)
                                    )
                                }

                                // Floating 3-Squircle Navbar (Completely transparent background, only the 3 squircles)
                                SimpBottomNavigationBar(
                                    currentRoute = currentDestination,
                                    backdrop = appBackdrop,
                                    onNavigate = { route: String ->
                                        if (route == currentDestination) {
                                            when (route) {
                                                "search" -> viewModel.triggerSearchFocus()
                                                "home" -> viewModel.triggerHomeReset()
                                                "library" -> viewModel.triggerLibraryReset()
                                            }
                                            return@SimpBottomNavigationBar
                                        }
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }

                // ── Full Screen Now Playing overlay ───────────────
                // Sync canvas lifecycle: release video player when screen is dismissed
                LaunchedEffect(isNowPlayingVisible) {
                    viewModel.setNowPlayingVisible(isNowPlayingVisible)
                }

                AnimatedVisibility(
                    visible = isNowPlayingVisible,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)) + fadeIn(tween(180)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200, easing = FastOutSlowInEasing)) + fadeOut(tween(150)),
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

            } // end Root Box
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

    val navBarShape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .wrapContentSize()
            .shadow(
                elevation = 20.dp,
                shape = navBarShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.7f),
                spotColor = Color.Black.copy(alpha = 0.95f)
            )
            .clip(navBarShape)
            .background(Color(0xFF0F1018))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Black.copy(alpha = 0.4f)
                    )
                ),
                shape = navBarShape
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
