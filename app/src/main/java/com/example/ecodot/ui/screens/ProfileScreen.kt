package com.example.ecodot.ui.screens
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Add
import com.example.ecodot.ui.theme.ArrowBackIos
import com.example.ecodot.ui.theme.CameraAlt
import com.example.ecodot.ui.theme.Close
import com.example.ecodot.ui.theme.Crop
import com.example.ecodot.ui.theme.DeleteOutline
import com.example.ecodot.ui.theme.Edit
import com.example.ecodot.ui.theme.Person
import com.example.ecodot.ui.theme.PhotoLibrary
import com.example.ecodot.ui.theme.Refresh
import com.example.ecodot.ui.theme.RotateRight
import com.example.ecodot.ui.theme.Settings
import com.example.ecodot.ui.theme.Verified
import com.example.ecodot.ui.theme.ZoomIn
import com.example.ecodot.ui.theme.ZoomOut

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.ui.components.player.PlayerStyleMath
import com.example.ecodot.ui.theme.poppinsFamily
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.util.AvatarCropMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

private val CURATED_PROFILE_GENRES = listOf(
    "Pop", "Lo-Fi", "Hip-Hop", "Indie", "Electronic", "Rock",
    "R&B", "Jazz", "Bollywood", "Phonk", "Classical", "Ambient"
)

private val ProfileBg = Color(0xFF090A0D)
private val ProfileSurface = Color(0xFF13141A)
private val ProfileTonalButton = Color(0xFF1C1E26)
private val ProfileHairline = Color.White.copy(alpha = 0.06f)

private const val AVATAR_SOURCE_FILE = "profile_avatar_source.jpg"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    onEqualizerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsState()

    var showEditProfile by remember { mutableStateOf(false) }
    var showPhotoActionSheet by remember { mutableStateOf(false) }
    var showResetProfileDialog by remember { mutableStateOf(false) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val cachedSource = withContext(Dispatchers.IO) {
                    copyUriToLocalSource(context, uri)
                }
                pendingCropUri = cachedSource ?: uri
            }
        }
    }

    val favoriteGenreList = remember(userProfile?.favoriteGenres) {
        PlayerStyleMath.parseFavoriteGenres(userProfile?.favoriteGenres.orEmpty())
    }
    val displayName = userProfile?.name?.ifBlank { "EcoDot Listener" } ?: "EcoDot Listener"
    val avatarUri = userProfile?.avatarUri

    val openAdjustCurrentPhoto: () -> Unit = {
        val localSource = File(context.filesDir, AVATAR_SOURCE_FILE)
        when {
            localSource.exists() && localSource.length() > 0L -> pendingCropUri = Uri.fromFile(localSource)
            !avatarUri.isNullOrBlank() -> pendingCropUri = Uri.parse(avatarUri)
            else -> imagePicker.launch("image/*")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ProfileBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── 1. Minimal Top Navigation Bar ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = SimpIcons.ArrowBackIos,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Text(
                        text = "Profile",
                        fontFamily = poppinsFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        imageVector = SimpIcons.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.86f),
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            // ── 2. Clean Centered Profile Content (Essential Items Only) ─────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Adjustable Profile Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            if (!avatarUri.isNullOrBlank()) {
                                showPhotoActionSheet = true
                            } else {
                                imagePicker.launch("image/*")
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(ProfileTonalButton),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatarUri.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(avatarUri)
                                    .size(320, 320)
                                    .crossfade(160)
                                    .build(),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val initial = displayName.firstOrNull()?.uppercaseChar()
                            if (initial != null && initial.isLetter()) {
                                Text(
                                    text = initial.toString(),
                                    fontFamily = poppinsFamily,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.86f)
                                )
                            } else {
                                Icon(
                                    imageVector = SimpIcons.Person,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.72f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF232530))
                            .border(2.5.dp, ProfileBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SimpIcons.CameraAlt,
                            contentDescription = "Adjust photo",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Display Name & Optional Verified Check
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = displayName,
                        fontFamily = poppinsFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (userProfile?.isPro == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = SimpIcons.Verified,
                            contentDescription = "Verified",
                            tint = Color.White.copy(alpha = 0.78f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Essential Profile Actions: Edit Profile + Add/Adjust Photo
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfilePillButton(
                        icon = SimpIcons.Edit,
                        label = "Edit profile",
                        onClick = { showEditProfile = true }
                    )
                    if (!avatarUri.isNullOrBlank()) {
                        ProfilePillButton(
                            icon = SimpIcons.Crop,
                            label = "Adjust photo",
                            onClick = openAdjustCurrentPhoto
                        )
                    } else {
                        ProfilePillButton(
                            icon = SimpIcons.PhotoLibrary,
                            label = "Add photo",
                            onClick = { imagePicker.launch("image/*") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Favorite Genres
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Favorite genres",
                        fontFamily = poppinsFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.52f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (favoriteGenreList.isNotEmpty()) {
                        CenteredWrappingTags(
                            horizontalSpacing = 8.dp,
                            verticalSpacing = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            favoriteGenreList.forEach { genre ->
                                Surface(
                                    shape = CircleShape,
                                    color = ProfileSurface
                                ) {
                                    Text(
                                        text = genre,
                                        fontFamily = poppinsFamily,
                                        fontSize = 12.5.sp,
                                        color = Color.White.copy(alpha = 0.82f),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = ProfileSurface,
                            modifier = Modifier.clickable { showEditProfile = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = SimpIcons.Add,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.65f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add favorite genres",
                                    fontFamily = poppinsFamily,
                                    fontSize = 12.5.sp,
                                    color = Color.White.copy(alpha = 0.68f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Minimal Account Reset Action
                TextButton(onClick = { showResetProfileDialog = true }) {
                    Text(
                        text = "Reset profile",
                        fontFamily = poppinsFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE5534B)
                    )
                }
            }
        }

        // Photo Action Menu when an avatar already exists
        if (showPhotoActionSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPhotoActionSheet = false },
                containerColor = Color(0xFF14151B),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp)
                ) {
                    PhotoMenuRow(
                        icon = SimpIcons.Crop,
                        label = "Adjust picture",
                        onClick = {
                            showPhotoActionSheet = false
                            openAdjustCurrentPhoto()
                        }
                    )
                    HorizontalDivider(
                        color = ProfileHairline,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 52.dp)
                    )
                    PhotoMenuRow(
                        icon = SimpIcons.PhotoLibrary,
                        label = "Choose new photo",
                        onClick = {
                            showPhotoActionSheet = false
                            imagePicker.launch("image/*")
                        }
                    )
                    HorizontalDivider(
                        color = ProfileHairline,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 52.dp)
                    )
                    PhotoMenuRow(
                        icon = SimpIcons.DeleteOutline,
                        label = "Remove photo",
                        tint = Color(0xFFE5534B),
                        onClick = {
                            showPhotoActionSheet = false
                            deleteSavedAvatarFiles(context, includeSource = true)
                            viewModel.updateProfile(
                                name = displayName,
                                genres = userProfile?.favoriteGenres.orEmpty(),
                                avatarUrl = null,
                                isPro = userProfile?.isPro ?: true
                            )
                        }
                    )
                }
            }
        }

        // Interactive Circle Crop / Pan / Zoom Picture Adjuster
        pendingCropUri?.let { uriToCrop ->
            AdjustProfilePictureDialog(
                sourceUri = uriToCrop,
                onApply = { croppedFile ->
                    pendingCropUri = null
                    viewModel.updateProfile(
                        name = displayName,
                        genres = userProfile?.favoriteGenres.orEmpty(),
                        avatarUrl = Uri.fromFile(croppedFile).toString(),
                        isPro = userProfile?.isPro ?: true
                    )
                },
                onDismiss = { pendingCropUri = null }
            )
        }

        if (showEditProfile) {
            EditProfileSheet(
                currentName = userProfile?.name ?: "EcoDot Listener",
                currentGenres = userProfile?.favoriteGenres ?: "",
                currentAvatarUri = avatarUri,
                currentIsPro = userProfile?.isPro ?: true,
                onPickAvatar = { imagePicker.launch("image/*") },
                onAdjustAvatar = openAdjustCurrentPhoto,
                onSave = { name, genres, isPro ->
                    viewModel.updateProfile(
                        name = name,
                        genres = genres,
                        avatarUrl = avatarUri,
                        isPro = isPro
                    )
                    showEditProfile = false
                },
                onDismiss = { showEditProfile = false }
            )
        }

        if (showResetProfileDialog) {
            AlertDialog(
                onDismissRequest = { showResetProfileDialog = false },
                title = {
                    Text(
                        text = "Reset profile?",
                        fontFamily = poppinsFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "This restores your default profile name and clears your profile photo and favorite genres.",
                        fontFamily = poppinsFamily,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            deleteSavedAvatarFiles(context, includeSource = true)
                            viewModel.updateProfile("EcoDot Listener", "", null, false)
                            showResetProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5534B))
                    ) {
                        Text("Reset", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetProfileDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                containerColor = Color(0xFF1A1B22),
                titleContentColor = Color.White
            )
        }
    }
}

@Composable
private fun ProfilePillButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = ProfileTonalButton,
        modifier = Modifier
            .height(38.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.84f),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontFamily = poppinsFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PhotoMenuRow(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontFamily = poppinsFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Interactive Circular Profile Picture Adjuster (Pan, Pinch-Zoom, Slider, Rotate)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdjustProfilePictureDialog(
    sourceUri: Uri,
    onApply: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var baseBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadError by remember { mutableStateOf(false) }
    var quarterTurns by remember { mutableIntStateOf(0) }
    var orientedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var boxWidth by remember { mutableFloatStateOf(0f) }
    var boxHeight by remember { mutableFloatStateOf(0f) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(sourceUri) {
        loadError = false
        baseBitmap = null
        val decoded = withContext(Dispatchers.IO) {
            decodeOrientedBitmap(context, sourceUri, maxDimension = 1600)
        }
        if (decoded == null) {
            loadError = true
        } else {
            baseBitmap = decoded
        }
    }

    LaunchedEffect(baseBitmap, quarterTurns) {
        val base = baseBitmap ?: return@LaunchedEffect
        orientedBitmap = withContext(Dispatchers.Default) {
            rotateBitmapQuarterTurns(base, quarterTurns)
        }
    }

    val previewImage = remember(orientedBitmap) {
        orientedBitmap?.asImageBitmap()
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ProfileBg)
                .statusBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(enabled = !isSaving, onClick = onDismiss) {
                    Icon(
                        imageVector = SimpIcons.Close,
                        contentDescription = "Cancel",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Adjust picture",
                    fontFamily = poppinsFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Button(
                    enabled = orientedBitmap != null && !isSaving,
                    onClick = {
                        val bmp = orientedBitmap ?: return@Button
                        if (boxWidth <= 0f || boxHeight <= 0f) return@Button
                        isSaving = true
                        scope.launch {
                            val file = withContext(Dispatchers.IO) {
                                saveCroppedAvatar(
                                    context = context,
                                    source = bmp,
                                    containerWidth = boxWidth,
                                    containerHeight = boxHeight,
                                    zoom = zoom,
                                    offsetX = panOffset.x,
                                    offsetY = panOffset.y
                                )
                            }
                            isSaving = false
                            if (file != null) {
                                onApply(file)
                            }
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = "Apply",
                            fontFamily = poppinsFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Square Gesture Crop Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF101116))
                    .onSizeChanged { size ->
                        boxWidth = size.width.toFloat()
                        boxHeight = size.height.toFloat()
                    }
                    .pointerInput(orientedBitmap) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            val bmp = orientedBitmap ?: return@detectTransformGestures
                            val newZoom = (zoom * gestureZoom).coerceIn(
                                AvatarCropMath.MIN_ZOOM,
                                AvatarCropMath.MAX_ZOOM
                            )
                            val layout = AvatarCropMath.computeLayout(
                                sourceWidth = bmp.width,
                                sourceHeight = bmp.height,
                                containerWidth = size.width.toFloat(),
                                containerHeight = size.height.toFloat(),
                                zoom = newZoom,
                                offsetX = panOffset.x + pan.x,
                                offsetY = panOffset.y + pan.y
                            )
                            zoom = newZoom
                            panOffset = Offset(layout.clampedOffsetX, layout.clampedOffsetY)
                        }
                    }
                    .pointerInput(orientedBitmap) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (zoom > 1.2f) {
                                    zoom = 1f
                                    panOffset = Offset.Zero
                                } else {
                                    zoom = 2.2f
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    loadError -> {
                        Text(
                            text = "Unable to load selected photo",
                            fontFamily = poppinsFamily,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    previewImage == null || orientedBitmap == null -> {
                        CircularProgressIndicator(color = Color.White.copy(alpha = 0.8f))
                    }
                    else -> {
                        val bmp = orientedBitmap!!
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        ) {
                            val layout = AvatarCropMath.computeLayout(
                                sourceWidth = bmp.width,
                                sourceHeight = bmp.height,
                                containerWidth = size.width,
                                containerHeight = size.height,
                                zoom = zoom,
                                offsetX = panOffset.x,
                                offsetY = panOffset.y
                            )

                            drawImage(
                                image = previewImage,
                                dstOffset = IntOffset(
                                    layout.imageLeft.roundToInt(),
                                    layout.imageTop.roundToInt()
                                ),
                                dstSize = IntSize(
                                    layout.drawnWidth.roundToInt().coerceAtLeast(1),
                                    layout.drawnHeight.roundToInt().coerceAtLeast(1)
                                )
                            )

                            // Outer circular crop vignette mask
                            val fullPath = Path().apply {
                                addRect(Rect(0f, 0f, size.width, size.height))
                            }
                            val radius = layout.cropDiameter / 2f
                            val circlePath = Path().apply {
                                addOval(
                                    Rect(
                                        layout.centerX - radius,
                                        layout.centerY - radius,
                                        layout.centerX + radius,
                                        layout.centerY + radius
                                    )
                                )
                            }
                            val maskPath = Path().apply {
                                op(fullPath, circlePath, PathOperation.Difference)
                            }
                            drawPath(maskPath, color = Color.Black.copy(alpha = 0.68f))

                            drawCircle(
                                color = Color.White.copy(alpha = 0.8f),
                                radius = radius,
                                center = Offset(layout.centerX, layout.centerY),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Pinch or drag to reframe · Double-tap to zoom",
                fontFamily = poppinsFamily,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Single-handed Zoom Slider & Rotate/Reset controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val bmp = orientedBitmap ?: return@IconButton
                            val newZoom = (zoom - 0.35f).coerceIn(
                                AvatarCropMath.MIN_ZOOM,
                                AvatarCropMath.MAX_ZOOM
                            )
                            val layout = AvatarCropMath.computeLayout(
                                bmp.width, bmp.height, boxWidth, boxHeight,
                                newZoom, panOffset.x, panOffset.y
                            )
                            zoom = newZoom
                            panOffset = Offset(layout.clampedOffsetX, layout.clampedOffsetY)
                        }
                    ) {
                        Icon(
                            imageVector = SimpIcons.ZoomOut,
                            contentDescription = "Zoom out",
                            tint = Color.White.copy(alpha = 0.72f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Slider(
                        value = zoom,
                        onValueChange = { nextZoom ->
                            val bmp = orientedBitmap
                            if (bmp != null && boxWidth > 0f) {
                                val layout = AvatarCropMath.computeLayout(
                                    bmp.width, bmp.height, boxWidth, boxHeight,
                                    nextZoom, panOffset.x, panOffset.y
                                )
                                panOffset = Offset(layout.clampedOffsetX, layout.clampedOffsetY)
                            }
                            zoom = nextZoom
                        },
                        valueRange = AvatarCropMath.MIN_ZOOM..AvatarCropMath.MAX_ZOOM,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.18f)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val bmp = orientedBitmap ?: return@IconButton
                            val newZoom = (zoom + 0.35f).coerceIn(
                                AvatarCropMath.MIN_ZOOM,
                                AvatarCropMath.MAX_ZOOM
                            )
                            val layout = AvatarCropMath.computeLayout(
                                bmp.width, bmp.height, boxWidth, boxHeight,
                                newZoom, panOffset.x, panOffset.y
                            )
                            zoom = newZoom
                            panOffset = Offset(layout.clampedOffsetX, layout.clampedOffsetY)
                        }
                    ) {
                        Icon(
                            imageVector = SimpIcons.ZoomIn,
                            contentDescription = "Zoom in",
                            tint = Color.White.copy(alpha = 0.72f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    ProfilePillButton(
                        icon = SimpIcons.RotateRight,
                        label = "Rotate 90°",
                        onClick = {
                            quarterTurns = (quarterTurns + 1) % 4
                            panOffset = Offset.Zero
                        }
                    )
                    ProfilePillButton(
                        icon = SimpIcons.Refresh,
                        label = "Reset",
                        onClick = {
                            zoom = 1f
                            panOffset = Offset.Zero
                            quarterTurns = 0
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Profile Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    currentName: String,
    currentGenres: String,
    currentAvatarUri: String?,
    currentIsPro: Boolean,
    onPickAvatar: () -> Unit,
    onAdjustAvatar: () -> Unit,
    onSave: (name: String, genres: String, isPro: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    val selectedGenres = remember {
        mutableStateListOf<String>().apply {
            addAll(PlayerStyleMath.parseFavoriteGenres(currentGenres))
        }
    }
    var customGenreInput by remember { mutableStateOf("") }
    var isPro by remember { mutableStateOf(currentIsPro) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF14151B),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Edit profile",
                fontFamily = poppinsFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ProfileTonalButton)
                        .clickable {
                            if (!currentAvatarUri.isNullOrBlank()) onAdjustAvatar() else onPickAvatar()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentAvatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = currentAvatarUri,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(SimpIcons.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.78f))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Profile photo",
                        fontFamily = poppinsFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Choose photo",
                            fontFamily = poppinsFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.76f),
                            modifier = Modifier.clickable(onClick = onPickAvatar)
                        )
                        if (!currentAvatarUri.isNullOrBlank()) {
                            Text(
                                text = "Adjust picture",
                                fontFamily = poppinsFamily,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.76f),
                                modifier = Modifier.clickable(onClick = onAdjustAvatar)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display name", fontFamily = poppinsFamily) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White.copy(alpha = 0.55f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White.copy(alpha = 0.78f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Favorite genres",
                fontFamily = poppinsFamily,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            val allDisplayGenres = remember(selectedGenres.toList()) {
                (CURATED_PROFILE_GENRES + selectedGenres).distinctBy { it.lowercase() }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                allDisplayGenres.chunked(3).forEach { rowGenres ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowGenres.forEach { genre ->
                            val isSelected = selectedGenres.any { it.equals(genre, ignoreCase = true) }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedGenres.removeAll { it.equals(genre, ignoreCase = true) }
                                    } else {
                                        selectedGenres.add(genre)
                                    }
                                },
                                label = {
                                    Text(
                                        text = genre,
                                        fontFamily = poppinsFamily,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = Color.Black,
                                    containerColor = ProfileTonalButton,
                                    labelColor = Color.White.copy(alpha = 0.76f)
                                ),
                                border = null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowGenres.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val commitCustomGenre = {
                val trimmed = customGenreInput.trim().replace(Regex("\\s+"), " ")
                if (trimmed.isNotEmpty() && selectedGenres.none { it.equals(trimmed, ignoreCase = true) }) {
                    selectedGenres.add(trimmed)
                }
                customGenreInput = ""
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customGenreInput,
                    onValueChange = { customGenreInput = it },
                    placeholder = { Text("Add custom genre...", fontSize = 12.5.sp) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { commitCustomGenre() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.55f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = commitCustomGenre) {
                    Text("Add", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Verified listener badge",
                        fontFamily = poppinsFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Show verified checkmark beside your name",
                        fontFamily = poppinsFamily,
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.52f)
                    )
                }
                Switch(
                    checked = isPro,
                    onCheckedChange = { isPro = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onSave(
                        name.trim().ifBlank { "EcoDot Listener" },
                        selectedGenres.joinToString(", "),
                        isPro
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(
                    text = "Save",
                    fontFamily = poppinsFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bitmap Loading, Orientation & Cropping Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun copyUriToLocalSource(context: Context, uri: Uri): Uri? {
    return runCatching {
        val bitmap = decodeOrientedBitmap(context, uri, maxDimension = 1600) ?: return null
        val destFile = File(context.filesDir, AVATAR_SOURCE_FILE)
        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        Uri.fromFile(destFile)
    }.getOrNull()
}

private fun deleteSavedAvatarFiles(context: Context, includeSource: Boolean) {
    context.filesDir.listFiles { file ->
        file.isFile && (
            (file.name.startsWith("user_avatar") && file.name.endsWith(".jpg")) ||
                (includeSource && file.name == AVATAR_SOURCE_FILE)
            )
    }?.forEach { file ->
        runCatching { file.delete() }
    }
}

private fun decodeOrientedBitmap(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
    return runCatching {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openUriStream(context, uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOpts)
        }
        val rawW = boundsOpts.outWidth
        val rawH = boundsOpts.outHeight
        if (rawW <= 0 || rawH <= 0) return null

        var sampleSize = 1
        while ((rawW / sampleSize) > maxDimension || (rawH / sampleSize) > maxDimension) {
            sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val sampled = openUriStream(context, uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
        } ?: return null

        val orientation = openUriStream(context, uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    postRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    postRotate(270f)
                    postScale(-1f, 1f)
                }
            }
        }
        if (matrix.isIdentity) sampled
        else Bitmap.createBitmap(sampled, 0, 0, sampled.width, sampled.height, matrix, true)
    }.getOrNull()
}

private fun openUriStream(context: Context, uri: Uri) =
    if (uri.scheme == "file") {
        uri.path?.let { File(it).takeIf(File::exists)?.inputStream() }
    } else {
        context.contentResolver.openInputStream(uri)
    }

private fun rotateBitmapQuarterTurns(source: Bitmap, quarterTurns: Int): Bitmap {
    val turns = ((quarterTurns % 4) + 4) % 4
    if (turns == 0) return source
    val matrix = Matrix().apply { postRotate(turns * 90f) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

private fun saveCroppedAvatar(
    context: Context,
    source: Bitmap,
    containerWidth: Float,
    containerHeight: Float,
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    outputSizePx: Int = 512
): File? {
    return runCatching {
        val cropRect = AvatarCropMath.computeSourceCropRect(
            sourceWidth = source.width,
            sourceHeight = source.height,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoom = zoom,
            offsetX = offsetX,
            offsetY = offsetY
        )
        val outBitmap = Bitmap.createBitmap(outputSizePx, outputSizePx, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(outBitmap)
        val srcRect = android.graphics.Rect(
            cropRect.left.roundToInt().coerceIn(0, source.width - 1),
            cropRect.top.roundToInt().coerceIn(0, source.height - 1),
            (cropRect.left + cropRect.size).roundToInt().coerceIn(1, source.width),
            (cropRect.top + cropRect.size).roundToInt().coerceIn(1, source.height)
        )
        val dstRect = RectF(0f, 0f, outputSizePx.toFloat(), outputSizePx.toFloat())
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, srcRect, dstRect, paint)

        deleteSavedAvatarFiles(context, includeSource = false)
        val outFile = File(context.filesDir, "user_avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { stream ->
            outBitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
        }
        outFile
    }.getOrNull()
}

/**
 * Custom centering flow layout for profile genre pills; avoids runtime
 * Compose foundation-layout FlowRow overload incompatibilities.
 */
@Composable
private fun CenteredWrappingTags(
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = horizontalSpacing.roundToPx()
        val vGapPx = verticalSpacing.roundToPx()
        val maxWidth = constraints.maxWidth
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentWidth = 0
        var currentHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(childConstraints)
            val neededWidth = if (currentRow.isEmpty()) placeable.width else currentWidth + hGapPx + placeable.width
            if (currentRow.isNotEmpty() && maxWidth != androidx.compose.ui.unit.Constraints.Infinity && neededWidth > maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentWidth)
                rowHeights.add(currentHeight)
                currentRow = mutableListOf(placeable)
                currentWidth = placeable.width
                currentHeight = placeable.height
            } else {
                currentRow.add(placeable)
                currentWidth = neededWidth
                currentHeight = currentHeight.coerceAtLeast(placeable.height)
            }
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentWidth)
            rowHeights.add(currentHeight)
        }

        val contentWidth = (rowWidths.maxOrNull() ?: 0).coerceIn(constraints.minWidth, maxWidth)
        val totalHeight = (rowHeights.sum() + (rows.size - 1).coerceAtLeast(0) * vGapPx)
            .coerceIn(constraints.minHeight, constraints.maxHeight)
        val layoutWidth = if (constraints.hasBoundedWidth && maxWidth != androidx.compose.ui.unit.Constraints.Infinity) {
            maxWidth
        } else {
            contentWidth
        }

        layout(layoutWidth, totalHeight) {
            var y = 0
            rows.forEachIndexed { rowIdx, row ->
                val rowWidth = rowWidths[rowIdx]
                val rowHeight = rowHeights[rowIdx]
                var x = ((layoutWidth - rowWidth) / 2).coerceAtLeast(0)
                row.forEach { placeable ->
                    val itemY = y + (rowHeight - placeable.height) / 2
                    placeable.placeRelative(x, itemY)
                    x += placeable.width + hGapPx
                }
                y += rowHeight + vGapPx
            }
        }
    }
}
