<div align="center">

# 🎵 EcoDot 1.27v

**A fast 120Hz Android music & video player combining crystal-clear Liquid Glass optics, Spotify-inspired playlists, Apple Music / Vinyl / Spotify player styles, seamless 1080p video playback, AutoMix DJ crossfade, and ad-free YouTube Music streaming.**

**Made by [Debug01-dEoW aka Deep](https://github.com/Debug01-dEoW)**

[![Release](https://img.shields.io/badge/Version-EcoDot%201.27v-10B981?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Debug01-dEoW/EcoDot-MusicApp/releases)
[![Made By](https://img.shields.io/badge/Made%20By-Debug01--dEoW%20aka%20Deep-6366F1?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Debug01-dEoW)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-F59E0B?style=for-the-badge)](LICENSE)

[**Download APK (`EcoDot 1.27v.apk`)**](releases/EcoDot%201.27v.apk) · [**GitHub Releases**](https://github.com/Debug01-dEoW/EcoDot-MusicApp/releases) · [**Developer Profile**](https://github.com/Debug01-dEoW)

</div>

---

## 📣 What's New in `1.27v`

EcoDot `1.27v` delivers a major polish pass — **120Hz fluidity**, **RSA-2048 release signing**, **Spotify-inspired playlists**, **reliable Queue + Remove from Queue controls**, **synced Song Info & live telemetry**, **unified Lyrics**, and **crash-free Profile custom tags**:

- **Fixed `Play Next`, `Add Queue` & New `Remove` Queue Action**: `Play Next` and `Add Queue` now insert ahead of auto-generated Endless Queue tracks, start playback cleanly when idle, sync with AutoMix crossfade decks (`EcoDotSessionService`), and showcase a **Remove** quick tile + full row option whenever a track is in the queue.
- **Synced Song Info & Live Stream Telemetry**: Eliminated fake `0 Views / 1 Likes / 0 Dislikes` and `BITRATE: 0 / ITAG: -` cards plus `1 kbps · 22.0 kHz` badges by merging live NewPipeExtractor + keyed InnerTube + Media3 telemetry.
- **Unified Inline & Full Lyrics Experience**: Shared `SyncedLyricsList` engine ensures inline Apple Music Lyrics and `FullScreenLyricsView` share consistent scaling, centered auto-scrolling, blur focus, LRCLIB attribution, and non-overlapping `Share` / `Full screen` buttons.
- **Fixed Edit Profile Custom Tag Crash**: Replaced unstable nested sheet/IME/FlowRow wrappers in `ProfileScreen` with a crash-safe dialog editor supporting `Done` submit and comma-separated custom tags.

## Highlights (`1.27v`)

EcoDot `1.27v` delivers a major polish pass focused on **120Hz butter-smooth performance**, **crystal-clear Liquid Glass lens refraction**, **authentic Spotify-style playlists**, **a complete scrollable 3-dots Track Options menu**, **interactive circular profile picture adjustment**, and **over-the-air update notifications for all `v1.26v` users**.

### 🎧 Spotify-Style Playlists & Full Track Actions
- **Authentic Playlist Detail Screen (`PlaylistDetailScreen`)**: Centered rounded artwork (featuring Spotify's purple gradient Heart cover on *Liked Songs* and dynamic `2x2` album mosaics on playlists), smooth `Palette` ambient hero gradients, one-tap Download All, track reordering, Shuffle, and a green circular Play/Pause FAB.
- **Complete Scrollable Track Options Sheet (`TrackOptionsBottomSheet`)**: Grouped matte cards with Quick Actions (`Play Next`, `Add Queue`, `Playlist`) plus every action cleanly accessible across Home, Search, Library, and Playlists — `Play Now`, `Save/Remove Liked Songs`, `Download`, `Remove from Playlist`, `Switch to Music Video/Audio`, `Sleep Timer`, `Looping Canvas`, `Now Playing Style`, `Equalizer`, `Set as Ringtone`, `Go to Album`, and `Go to Artist`.

### ✨ Crystal-Clear Liquid Glass & 120Hz Smoothness
- **Clear Optical Lens Glass**: Restored crisp low-haze lens refraction across Home header pills (`Profile`, `All`, `Mood`, `+ | Settings`), Search Bar, `SimpChip` filters, `MiniPlayer`, and `SimpBottomNavigationBar`, while removing dark sticky box artifacts behind Home chips and `"Recent searches | Clear search history"`.
- **120Hz Display Engine & R8 Minification**: Selects hardware 120Hz display mode (`preferredDisplayModeId`), isolates per-frame playback progress state reads across seekbars/lyrics, uses single-pass sibling backdrop captures, and ships R8-minified release APKs (`~8.3 MB`).

### 👤 Minimal Profile & Interactive Photo Cropper
- **Circular Avatar Cropper (`ProfileScreen` + `AvatarCropMath`)**: Upload any gallery photo and adjust it inside a circular crop overlay with pinch/drag pan, zoom slider (`1x–5x`), and 90° rotation before saving cleanly.
- **Clutter-Free Profile & Themes**: Removed extra analytics clutter from Profile and removed the redundant `Song / Video` toggle bar from **Spotify** and **Vinyl** player themes (Video switching remains one tap away inside the 3-dots menu).

### 🎨 Four Signature Now Playing Styles
Switch seamlessly from the Player Style sheet (`Track Options → Now Playing Style` or Player Header):

| Style | Highlights |
| :--- | :--- |
| **Apple Music** | `6x6` dynamic artwork mesh backdrop + frosted Haze overlay, bare white header/controls, inline synced lyric teaser, double-arrow transport, interactive system volume bar, real-time **`kbps · kHz`** audio telemetry, 3-pill dock, device model badge (`Build.MODEL`), and dedicated **Queue** & **Lyrics** tabs |
| **Vinyl Turntable** | 33⅓ RPM spinning tonearm & vinyl platter artwork (`VinylArtworkView`), analog groove lighting, speed-reactive rotation, and full queue controls |
| **Spotify** | High-contrast obsidian surface cards, bold typography, inline lyrics card preview, and tactile playback bar |
| **EcoDot Classic** | Frosted Liquid Glass centerpiece, continuous seekbar (`ContinuousSeekBar`), fullscreen & lockscreen synced lyrics, and ambient video support |

### 🔊 Audio Engine, AutoMix & Over-the-Air Updater
- **Live Stream Telemetry**: Real-time bitrate (`kbps`) and sample rate (`kHz`) read directly from active stream & Media3 session state.
- **AutoMix & DJ Crossfade**: Equal-power dual-deck ExoPlayer crossfade engine with **`CrossfadeFilterAudioProcessor`** delivering low-pass / high-pass filter sweeps and beat-aware `Auto` scaling, plus **High AAC (`256 kbps`)** and **SponsorBlock** skipping.
- **Upgraded In-App Update Screen (`UpdateDialog` & `AppUpdateManager`)**: Displays Release News & Highlights, `v1.26v -> v1.27v` version delta badges, APK size chips, categorized change cards, and one-tap APK download & installation so `v1.26v` users are automatically notified when `1.27v` ships.
- **Home Page Credit Footer**: Features clickable attribution (**`Made by Debug01-dEoW aka Deep`**) linking directly to GitHub alongside `© 2026 EcoDot v1.27v`.

---

## 🏗️ Architecture

EcoDot follows **Clean Architecture + MVVM** with **Koin** dependency injection, **Room** persistence, **Media3** background media session management, and **Jetpack Compose + Liquid Glass + Haze** rendering:

```mermaid
graph TB
    UI["Jetpack Compose UI · Clear Liquid Glass & Haze<br/>(Home, Search, Browse, Spotify Playlists, NowPlaying Styles, Profile, Settings)"] --> VM["ViewModels (Koin)<br/>MusicViewModel · HomeViewModel · SearchViewModel · AlbumViewModel · ArtistViewModel"]
    VM --> UC["Domain Use Cases<br/>GetHomeFeed · SearchMusic · GetLyrics · GetSponsorSegments"]
    UC --> REPO["MusicRepository (IMusicRepository)"]
    REPO --> REMOTE["Remote Sources<br/>YouTubeParser · YouTubeStreamResolver · LyricsRepository · SponsorBlockService"]
    REPO --> LOCAL["Room & DataStore<br/>EcoDotDatabase · UserPreferencesManager"]
    VM --> SVC["EcoDotSessionService (Media3)<br/>Dual-Deck AutoMix · CrossfadeFilterAudioProcessor · Telemetry · CacheManager"]
    UI --> REC["Acoustic Recognition & Utils<br/>SongRecognizer · ShazamSignature · AvatarCropMath · AppUpdateManager"]
```

---

## 📁 Project Structure

```text
EcoDot-MusicApp/
├── releases/
│   ├── EcoDot 1.27v.apk                 # Latest R8-optimized 120Hz release APK
│   ├── EcoDot 1.26v.apk                 # Previous v1.26 release build
│   ├── EcoDot-v1.2-liquid-glass.apk     # Previous v1.2 build
│   └── EcoDot-v1.0-beta.apk             # Initial beta release
├── version.json                         # Remote OTA update manifest (polled by AppUpdateManager)
├── gradle/libs.versions.toml            # Version catalog (AGP 9.2.1, Kotlin 2.2.10, Media3 1.5.1)
└── app/src/main/
    ├── assets/version.json              # Offline fallback update manifest
    ├── res/layout/responsive_player_video_view.xml
    └── java/com/example/ecodot/
        ├── EcoDotApplication.kt & MainActivity.kt
        ├── data/
        │   ├── local/{dao,database,entities,prefs}/
        │   ├── remote/                  # YouTubeParser, StreamResolver, RealHomeData, Lyrics, SponsorBlock
        │   └── repository/MusicRepository.kt
        ├── di/                          # App, Database, Network, Repository, UseCase, ViewModel modules
        ├── domain/{model,repository,usecase}/
        ├── playback/                    # EcoDotSessionService, AudioEffectManager, CrossfadeFilterAudioProcessor, CacheManager
        ├── recognition/                 # ShazamSignature, SongRecognizer
        ├── ui/
        │   ├── components/              # LiquidGlass, SimpBottomNavigationBar, MiniPlayer, TrackOptionsMenu, UpdateDialog, etc.
        │   ├── components/player/       # AppleMusicStyleContent, VinylStyleContent, SpotifyStyleContent, ResponsivePlayerVideoSurface, etc.
        │   ├── screens/                 # HomeScreen, SearchScreen, BrowseScreen, PlaylistDetailScreen, NowPlayingScreen, ProfileScreen, SettingsScreen, etc.
        │   ├── theme/                   # Color, SimpIcons, Theme, Type
        │   └── viewmodel/               # MusicViewModel, HomeViewModel, SearchViewModel, AlbumViewModel, ArtistViewModel
        └── util/                        # AppUpdateManager, AppUpdateModel, AvatarCropMath, NetworkMonitor, PermissionHandler, PlaylistCustomization
```

---

## 📥 Installation & Build

### Install the APK
1. Download [**`EcoDot 1.27v.apk`**](releases/EcoDot%201.27v.apk) from [**Releases**](https://github.com/Debug01-dEoW/EcoDot-MusicApp/releases).
2. Enable **"Install from unknown sources"** on your Android device (**Android 7.0 / API 24+** required, targets API 37).
3. If Android Play Protect shows **"Scan app before install"**, tap **Scan app** → once it reports **No threats found**, tap **Install** (normal for sideloaded APKs until Google's cloud database builds enough install reputation for the signing certificate).
4. **Upgrading from `EcoDot 1.26v`**: `EcoDot 1.27v` switches from a generic debug certificate to EcoDot's official release certificate (`SHA-256: 0B:8A:F1:49:63:A1:71:7E:F0:50:2A:CC:B1:6A:9E:80:50:F6:5D:11:11:0B:33:63:E5:9E:6A:5B:03:D4:9D:9E`). If Android blocks updating over `v1.26v` due to the key change, uninstall `v1.26v` once before installing `1.27v` — all future releases will update cleanly in-place.

### Build from Source
```bash
git clone https://github.com/Debug01-dEoW/EcoDot-MusicApp.git
cd EcoDot-MusicApp

# Run unit test suite
./gradlew testDebugUnitTest

# Build R8-optimized 120Hz EcoDot 1.27v release APK (uses keystore.properties if present, else debug fallback)
./gradlew assembleRelease
```

---

## 👨‍💻 Author & Credits

<div align="center">

### **Made by Debug01-dEoW aka Deep**

[![GitHub Profile](https://img.shields.io/badge/GitHub-Debug01--dEoW-181717?style=for-the-badge&logo=github)](https://github.com/Debug01-dEoW)
[![Repository](https://img.shields.io/badge/Repo-EcoDot--MusicApp-0EA5E9?style=for-the-badge&logo=git&logoColor=white)](https://github.com/Debug01-dEoW/EcoDot-MusicApp)
[![Releases](https://img.shields.io/badge/Releases-Download%20APKs-10B981?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Debug01-dEoW/EcoDot-MusicApp/releases)

UI/UX inspiration from **SimpMusic**, **Apple Music**, and **Spotify**, built with **Jetpack Compose**, **Media3 ExoPlayer**, **Haze Liquid Glass**, and **NewPipe Extractor**.

</div>

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
