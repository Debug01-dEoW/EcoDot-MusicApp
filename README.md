<div align="center">

# 🎵 EcoDot
### The Vibrant Hybrid Android Music Player

[![Android](https://img.shields.io/badge/Platform-Android%207.0%2B-brightgreen?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blueviolet?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0--beta-orange)]()
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-yellow)]()

> **EcoDot** bridges the gap between your **local music library** and the entire **YouTube Music catalog** — offering seamless discovery, beautiful playback, and smart offline management in one app.

</div>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Screenshots](#-screenshots)
- [Features](#-features)
- [User Guide](#-user-guide)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Developer Guide](#-developer-guide)
- [Project Structure](#-project-structure)
- [Database Schema](#-database-schema)
- [API Reference](#-api-reference)
- [Build & Run](#-build--run)
- [Testing](#-testing)
- [Known Limitations](#-known-limitations)
- [Contributing](#-contributing)
- [Changelog](#-changelog)

---

## 🌟 Overview

EcoDot is a **full-featured, Material Design 3** Android music app built entirely in Kotlin + Jetpack Compose. It combines:

- 🎙 **YouTube Music integration** — stream any song from YouTube Music's catalogue
- 📂 **Local library management** — play audio files already on your device
- 🎨 **Stunning UI** — dynamic colour extraction, glassmorphism, smooth animations
- 🎚 **Audio tools** — built-in equaliser, audio effects, ringtone clipper
- 🔍 **Unified search** — search across both local files and YouTube Music simultaneously

---

## ✨ Features

### 🏠 Home Screen
| Feature | Description |
|---------|-------------|
| Personalised greeting | Time-aware greeting with user avatar |
| Quick picks | Fast access to recently-played songs |
| Trending tracks | Dynamically updated trending music |
| Recommended albums | Based on your listening history |
| Featured artists | Artists you follow |
| Recently played | Full scrollable history |

### 🔍 Search
- Real-time search across **YouTube Music** and **local files** simultaneously
- Results categorised into Songs, Albums, Artists, Playlists
- Recent search history with quick-tap reuse
- Long-press any result for context options

### 📚 Library
| Filter Tab | Shows |
|------------|-------|
| **All** | Everything — playlists, albums, artists, downloads, liked |
| **Playlists** | User-created playlists (saved + downloaded) |
| **Albums** | Albums the user has saved |
| **Downloads** | Offline-cached tracks |
| **Artists** | Followed artists |

- Long-press any item to reveal the Spotify-style context bottom sheet
- Create, rename, reorder, and delete playlists
- Add songs from anywhere directly into a playlist

### 🎵 Now Playing Screen
- Full-screen album art with **dynamic colour palette**
- Real-time **synced lyrics** (fetched automatically)
- Lyrics card view & lockscreen lyrics overlay
- Shuffle, repeat (one / all / off), crossfade
- **Like / dislike** toggle stored locally
- Swipe gestures for next/previous track
- Queue management panel

### 👤 Profile
- Custom display name & profile photo (camera or gallery)
- Listening stats: total plays, favourite genres, listening time
- Edit profile inline with live preview

### 🎛 Equaliser
- 5-band graphic equaliser
- Preset modes: Flat, Bass Boost, Pop, Rock, Jazz, Classical, Hip-Hop
- Bass Boost & Virtualizer effect sliders
- Loudness enhancement toggle

### 🎬 Video Player
- Embedded YouTube video playback
- Gesture-based brightness & volume control
- Full-screen landscape support

### 🎼 Daily Mix Generator
- Mood-based mix creation (Happy, Chill, Energetic, Focus, Sleep)
- Genre and era selectors
- AI-curated playlist generation from YouTube Music

### ✂️ Ringtone Clipper
- Trim any track to create a custom ringtone
- Waveform visualiser
- Set directly as device ringtone, notification, or alarm tone

### 🔔 Background Playback & Notifications
- Persistent media notification with playback controls
- Lock screen integration
- Bluetooth / headset button support
- Android Auto ready (via Media3 session)

---

## 📱 User Guide

### Installation
1. Download `EcoDot-v1.0-beta.apk`
2. On your Android device go to **Settings → Apps → Special App Access → Install Unknown Apps**
3. Enable installation from your file manager / browser
4. Tap the APK file and tap **Install**
5. Open **EcoDot** from your home screen

> **Requirements:** Android 7.0 (Nougat) or later. Internet connection required for YouTube streaming.

### First Launch
1. Grant **Storage** and **Notification** permissions when prompted
2. The app scans your local music library automatically
3. Your Home screen populates with local tracks and trending music from YouTube

### Playing Music
- **Tap** any song to start playback
- **Long-press** any song, album, or playlist anywhere in the app for a context menu
- The **mini player** at the bottom gives you quick controls from any screen
- Tap the mini player to open the full **Now Playing** screen

### Building a Playlist
1. Open **Library** → tap **+** (create playlist)
2. Give it a name and optional colour/emoji
3. Navigate to any song → **long-press** → **Add to Playlist**
4. Repeat for as many songs as you like

### Going Offline
Songs are automatically cached when played. For manual downloading:
- **Long-press** any track → **Download**
- View downloads in **Library → Downloads** tab

### Searching YouTube Music
- Open **Search** tab → type any song, artist, or album
- YouTube Music results appear alongside local results
- Tap any YouTube result to stream it instantly

---

## 🏗 Architecture

EcoDot follows the **MVVM (Model-View-ViewModel)** architecture with a clean **Repository Pattern** and reactive data flow using Kotlin **Coroutines + StateFlow**.

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                │
│  HomeScreen  SearchScreen  LibraryScreen  NowPlaying │
│  ProfileScreen  EqualizerScreen  VideoPlayerScreen   │
└───────────────────────┬─────────────────────────────┘
                        │ observes StateFlow
┌───────────────────────▼─────────────────────────────┐
│               ViewModel Layer                        │
│    MusicViewModel   AlbumViewModel   ArtistViewModel │
└──────────┬────────────────────────┬─────────────────┘
           │ calls                  │ calls
┌──────────▼──────────┐  ┌─────────▼──────────────────┐
│  MusicRepository    │  │  YouTubeParser              │
│  (single source     │  │  (InnerTube API scraper)    │
│   of truth)         │  │  LyricsRepository           │
└──────────┬──────────┘  └─────────┬──────────────────┘
           │                       │ HTTP via Retrofit
┌──────────▼──────────┐  ┌─────────▼──────────────────┐
│  Room Database      │  │  YouTube InnerTube API      │
│  (local SQLite)     │  │  (web-based music source)   │
└─────────────────────┘  └────────────────────────────┘
           │
┌──────────▼──────────────────────────────────────────┐
│        Playback Layer (Media3 / ExoPlayer)           │
│  EcoDotSessionService (MediaSessionService)          │
│  AudioEffectManager  CacheManager                   │
└─────────────────────────────────────────────────────┘
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Single ViewModel** (`MusicViewModel`) | Avoids repeated database queries and keeps playback state centralised |
| **Room + StateFlow** | Reactive UI updates without manual refresh triggers |
| **Media3 SessionService** | Native Android background playback, lock screen, and Android Auto support |
| **YouTube InnerTube API** | No API key required; uses the same endpoint as the YouTube Music web app |
| **Coil** for image loading | Lightweight, Compose-native, with built-in disk caching |
| **KMPalette** for colour extraction | Dynamic theming based on album art |

---

## 🛠 Tech Stack

### Language & Core
| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 2.x | Primary language |
| **Jetpack Compose** | BOM latest | Declarative UI framework |
| **Kotlin Coroutines** | 1.8+ | Async/concurrent operations |
| **StateFlow / Flow** | — | Reactive state management |

### UI & Design
| Library | Purpose |
|---------|---------|
| **Material 3** | Design system components |
| **Material Icons Extended** | Full icon set |
| **Coil Compose** | Async image loading & caching |
| **Haze** | Glassmorphism / blur effects |
| **KMPalette** | Album art colour extraction |
| **Accompanist Permissions** | Runtime permission handling |

### Data & Persistence
| Library | Purpose |
|---------|---------|
| **Room (KSP)** | Local SQLite ORM with reactive queries |
| **DataStore Preferences** | Lightweight key-value storage (settings, theme) |
| **Gson** | JSON serialisation for playlist data |

### Networking
| Library | Purpose |
|---------|---------|
| **Retrofit 2** | Type-safe HTTP client |
| **OkHttp 4** | HTTP engine with interceptors |
| **Moshi** | Fast JSON parsing (with Kotlin codegen) |
| **Logging Interceptor** | Debug HTTP request/response logging |

### Media Playback
| Library | Purpose |
|---------|---------|
| **Media3 ExoPlayer** | Core audio/video playback engine |
| **Media3 HLS** | HTTP Live Streaming support |
| **Media3 DASH** | MPEG-DASH adaptive streaming |
| **Media3 Session** | Background playback + system media controls |
| **AndroidX Palette** | Legacy palette extraction fallback |

### Build Tools
| Tool | Purpose |
|------|---------|
| **Gradle (Kotlin DSL)** | Build system |
| **KSP (Kotlin Symbol Processing)** | Code generation for Room & Moshi |
| **Android SDK 37** | Target/compile SDK |

---

## 👩‍💻 Developer Guide

### Prerequisites

| Tool | Minimum Version | Recommended |
|------|----------------|-------------|
| **Android Studio** | Hedgehog (2023.1) | Meerkat (2024.3) |
| **JDK** | 11 | 17 |
| **Android SDK** | API 24 | API 37 |
| **Gradle** | 9.x | Latest stable |
| **Kotlin** | 2.0 | Latest stable |

### Cloning & Setup

```bash
# Clone the repository
git clone https://github.com/your-username/EcoDot.git
cd EcoDot

# Open in Android Studio
# File → Open → select the EcoDot folder
```

### Building

```bash
# Debug APK (for development & testing)
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease

# Compile Kotlin only (quick syntax check)
./gradlew compileDebugKotlin

# Clean build
./gradlew clean assembleDebug
```

### Running on a Device

```bash
# List connected devices
adb devices

# Install debug APK directly
adb install app/build/outputs/apk/debug/app-debug.apk

# View live logs
adb logcat -s "EcoDot" "ExoPlayer" "MusicViewModel"
```

### Signing a Release APK

1. Generate a keystore:
   ```bash
   keytool -genkey -v -keystore ecodot-release.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias ecodot
   ```

2. Add signing config to `app/build.gradle.kts`:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("ecodot-release.jks")
               storePassword = "YOUR_STORE_PASSWORD"
               keyAlias = "ecodot"
               keyPassword = "YOUR_KEY_PASSWORD"
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
               isMinifyEnabled = true
           }
       }
   }
   ```

3. Build:
   ```bash
   ./gradlew assembleRelease
   ```

### Adding a New Screen

1. Create `YourScreen.kt` in `ui/screens/`
2. Add composable with `@Composable` annotation
3. Register the route in `MainActivity.kt` inside `NavHost`
4. Add the bottom nav item in the navigation bar composable (if needed)

**Template:**
```kotlin
@Composable
fun YourScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    val uiState by viewModel.someState.collectAsState()

    Scaffold(
        topBar = { /* ... */ }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Content
        }
    }
}
```

### Adding a New Room Entity

1. Create the entity in `data/local/entities/`
2. Create the DAO in `data/local/dao/`
3. Register both in `EcoDotDatabase.kt`
4. Increment the `version` in `@Database` and add a `Migration`
5. Expose repository methods in `MusicRepository.kt`
6. Add ViewModel functions in `MusicViewModel.kt`

### Adding a New API Endpoint

1. Define the Retrofit interface in `data/remote/YouTubeApiService.kt`
2. Add models to `YouTubeModels.kt`
3. Add parsing logic to `YouTubeParser.kt`
4. Expose via `MusicRepository.kt`

### Code Style Guidelines

- **Kotlin idioms**: Prefer `let`, `also`, `apply`, `run` scope functions where appropriate
- **Composables**: Keep composable functions small and single-purpose; extract sub-composables
- **State**: Use `StateFlow` / `collectAsStateWithLifecycle()` — never store Compose state in the ViewModel
- **Coroutines**: All repository/database operations must run on `Dispatchers.IO`
- **Naming**: Screens end in `Screen`, ViewModels in `ViewModel`, DAOs in `Dao`, Entities are plain noun names

---

## 📁 Project Structure

```
EcoDot/
├── app/
│   └── src/main/
│       ├── java/com/example/ecodot/
│       │   ├── MainActivity.kt               # App entry, NavHost, BottomNav
│       │   │
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── entities/             # Room entities (DB tables)
│       │   │   │   │   ├── Track.kt
│       │   │   │   │   ├── Playlist.kt
│       │   │   │   │   ├── PlaylistTrack.kt
│       │   │   │   │   ├── PlaybackHistory.kt
│       │   │   │   │   ├── FollowedArtist.kt
│       │   │   │   │   ├── RecentSearchItem.kt
│       │   │   │   │   └── UserProfile.kt
│       │   │   │   ├── dao/                  # Room DAOs (DB queries)
│       │   │   │   │   ├── TrackDao.kt
│       │   │   │   │   ├── PlaylistDao.kt
│       │   │   │   │   ├── FollowedArtistDao.kt
│       │   │   │   │   ├── PlaybackHistoryDao.kt
│       │   │   │   │   ├── RecentSearchDao.kt
│       │   │   │   │   └── UserProfileDao.kt
│       │   │   │   ├── database/
│       │   │   │   │   └── EcoDotDatabase.kt # Room DB singleton
│       │   │   │   └── prefs/                # DataStore preferences
│       │   │   ├── remote/
│       │   │   │   ├── YouTubeApiService.kt  # Retrofit interface
│       │   │   │   ├── YouTubeModels.kt      # API response models
│       │   │   │   ├── YouTubeParser.kt      # InnerTube response parser
│       │   │   │   └── LyricsRepository.kt   # Lyrics fetching
│       │   │   └── repository/
│       │   │       └── MusicRepository.kt    # Single source of truth
│       │   │
│       │   ├── playback/
│       │   │   ├── EcoDotSessionService.kt   # MediaSessionService (background)
│       │   │   ├── AudioEffectManager.kt     # EQ, bass, virtualizer
│       │   │   └── CacheManager.kt           # Stream caching logic
│       │   │
│       │   ├── ui/
│       │   │   ├── components/               # Reusable composables
│       │   │   │   ├── MiniPlayer.kt
│       │   │   │   ├── TrackOptionsMenu.kt   # Long-press bottom sheet
│       │   │   │   ├── PlayingIndicator.kt
│       │   │   │   └── RingtoneClipperBottomSheet.kt
│       │   │   ├── screens/                  # Full screens
│       │   │   │   ├── HomeScreen.kt
│       │   │   │   ├── SearchScreen.kt
│       │   │   │   ├── LibraryScreen.kt
│       │   │   │   ├── NowPlayingScreen.kt
│       │   │   │   ├── ProfileScreen.kt
│       │   │   │   ├── EqualizerScreen.kt
│       │   │   │   ├── AlbumDetailScreen.kt
│       │   │   │   ├── ArtistProfileScreen.kt
│       │   │   │   ├── PlaylistDetailScreen.kt
│       │   │   │   ├── HistoryScreen.kt
│       │   │   │   ├── DailyMixGeneratorScreen.kt
│       │   │   │   ├── VideoPlayerScreen.kt
│       │   │   │   ├── LyricsCardView.kt
│       │   │   │   └── LockscreenLyricsActivity.kt
│       │   │   ├── theme/
│       │   │   │   ├── Theme.kt              # Material3 theme setup
│       │   │   │   ├── Color.kt              # Colour palette
│       │   │   │   └── Type.kt               # Typography
│       │   │   └── viewmodel/
│       │   │       ├── MusicViewModel.kt     # Main app ViewModel (~3000 lines)
│       │   │       ├── AlbumViewModel.kt     # Album detail ViewModel
│       │   │       └── ArtistViewModel.kt    # Artist profile ViewModel
│       │   │
│       │   └── util/
│       │       ├── NetworkMonitor.kt         # Connectivity observer
│       │       ├── PermissionHandler.kt      # Runtime permissions helper
│       │       └── PlaylistCustomization.kt  # Playlist colour/emoji utils
│       │
│       └── res/
│           ├── drawable/                     # Icons and graphics
│           ├── xml/
│           │   └── file_paths.xml            # FileProvider paths
│           └── values/                       # Strings, colours
│
├── KotlinYTMusicScraper/                     # YouTube Music scraper module
├── build.gradle.kts                          # Project-level build config
├── app/build.gradle.kts                      # App-level dependencies
├── gradle/libs.versions.toml                 # Centralised dependency versions
└── settings.gradle.kts                       # Module settings
```

---

## 🗄 Database Schema

EcoDot uses a **Room SQLite** database (`ecodot_database`) with the following tables:

### `tracks`
| Column | Type | Description |
|--------|------|-------------|
| `id` | TEXT (PK) | Unique track ID (YouTube video ID or local path hash) |
| `title` | TEXT | Track title |
| `artist` | TEXT | Artist name |
| `album` | TEXT | Album name |
| `albumArtUri` | TEXT | Thumbnail / album art URL |
| `streamUrl` | TEXT | Playback URL (local path or YouTube stream) |
| `duration` | LONG | Duration in milliseconds |
| `isLocal` | BOOLEAN | Whether the file is a local audio file |
| `isLiked` | BOOLEAN | User favourite flag |
| `isDownloaded` | BOOLEAN | Whether it has been cached offline |
| `playCount` | INTEGER | Total play count |
| `addedAt` | LONG | Timestamp when added to library |

### `playlists`
| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER (PK auto) | Playlist ID |
| `name` | TEXT | Playlist name |
| `description` | TEXT | Optional description |
| `coverUri` | TEXT | Cover image URL or local URI |
| `createdAt` | LONG | Creation timestamp |

### `playlist_tracks`
| Column | Type | Description |
|--------|------|-------------|
| `playlistId` | INTEGER (FK → playlists) | Parent playlist |
| `trackId` | TEXT (FK → tracks) | Linked track |
| `position` | INTEGER | Order within playlist |

### `playback_history`
| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER (PK auto) | History entry ID |
| `trackId` | TEXT | Track that was played |
| `playedAt` | LONG | Timestamp of playback |

### `followed_artists`
| Column | Type | Description |
|--------|------|-------------|
| `artistId` | TEXT (PK) | YouTube channel / artist ID |
| `name` | TEXT | Artist display name |
| `thumbnailUrl` | TEXT | Artist image URL |
| `followedAt` | LONG | When the user followed |

### `recent_searches`
| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER (PK auto) | Entry ID |
| `query` | TEXT | Search term |
| `searchedAt` | LONG | Timestamp |

### `user_profile`
| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER (PK) | Always `1` (single row) |
| `displayName` | TEXT | User's chosen name |
| `photoUri` | TEXT | URI to profile photo |

---

## 🌐 API Reference

### YouTube InnerTube (via `YouTubeParser.kt`)

EcoDot uses YouTube's internal **InnerTube API** — the same JSON API used by the YouTube Music web app. No API key is required.

**Base URL:** `https://music.youtube.com/`

| Endpoint | Purpose |
|----------|---------|
| `youtubei/v1/search` | Search songs, albums, artists |
| `youtubei/v1/browse` | Fetch artist pages, album details, home feed |
| `youtubei/v1/next` | Get related tracks / queue |
| `youtubei/v1/player` | Get stream URLs for a video |

**Headers sent with every request:**
```
User-Agent: Mozilla/5.0 ...
X-Goog-Api-Key: AIza...
Content-Type: application/json
Origin: https://music.youtube.com
Referer: https://music.youtube.com/
```

> ⚠️ **Note:** InnerTube is an unofficial API. It is not guaranteed to remain stable. Changes to YouTube's backend may require updates to `YouTubeParser.kt`.

### Lyrics (`LyricsRepository.kt`)

Lyrics are fetched from a third-party lyrics API based on song title + artist. The response is parsed into timed lines for synced display.

---

## 🔨 Build & Run

### Requirements
- Android Studio **Meerkat** or newer
- JDK **11** (bundled with Android Studio)
- Android device / emulator running **API 24+**

### Steps

```bash
# 1. Clone
git clone https://github.com/your-username/EcoDot.git

# 2. Open Android Studio → File → Open → EcoDot/

# 3. Let Gradle sync (first time may take 3–5 minutes)

# 4. Run on device/emulator
#    Click the green ▶ Run button, or:
./gradlew installDebug
```

### Environment Variables / Secrets
Currently, EcoDot requires **no API keys**. All YouTube communication uses the public InnerTube endpoint.

---

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests (requires connected device)
```bash
./gradlew connectedAndroidTest
```

### Manual Test Checklist
- [ ] Local music scan and display
- [ ] YouTube search returning results
- [ ] Tapping a result and audio playing
- [ ] Background playback + notification controls
- [ ] Playlist create / add song / delete
- [ ] Equaliser presets affecting audio
- [ ] Download / offline playback
- [ ] Profile photo change
- [ ] Lockscreen lyrics overlay

---

## ⚠️ Known Limitations

| Limitation | Details |
|------------|---------|
| YouTube stream URLs expire | Stream URLs have a TTL (~6 hours). Offline downloads work; streams re-fetched on replay |
| InnerTube API stability | Any YouTube backend change could break search/streaming |
| No Play Store distribution | Currently distributed as a sideload-only APK |
| Min API 24 | Devices below Android 7.0 are not supported |
| No explicit DRM support | DRM-protected content is not playable |

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'feat: add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Commit Message Convention

Use [Conventional Commits](https://www.conventionalcommits.org/):
```
feat: add new feature
fix: resolve a bug
refactor: code refactoring
docs: documentation update
style: formatting, no logic change
test: add or update tests
chore: build process, tooling
```

---

## 📝 Changelog

### v1.0-beta (July 2026)
- ✅ Initial release
- ✅ YouTube Music search & streaming
- ✅ Local library management
- ✅ Room database persistence
- ✅ Background playback with Media3
- ✅ Synced lyrics display
- ✅ 5-band equaliser
- ✅ Ringtone clipper
- ✅ Daily Mix generator
- ✅ Artist & album detail pages
- ✅ Spotify-style long-press context menus
- ✅ Playlist creation and management
- ✅ Profile customisation
- ✅ Video player screen
- ✅ Lockscreen lyrics activity
- ✅ History screen

---

<div align="center">

Made with ❤️ using **Kotlin** and **Jetpack Compose**

**EcoDot** — *Music Without Boundaries*

</div>
