# Keep stack trace line numbers legible for release crash reports
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Reflection-serialized remote, UI, and persistence models (Moshi / Room)
-keep class com.example.ecodot.data.remote.** { *; }
-keep class com.example.ecodot.data.local.entities.** { *; }
-keep class com.example.ecodot.data.backup.** { *; }
-keep class com.example.ecodot.domain.model.** { *; }
-keep class com.example.ecodot.util.CustomCover { *; }
-keep class com.example.ecodot.ui.viewmodel.InstantSearchItem { *; }

# Moshi reflection & generated adapters
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keepclassmembers class * extends com.squareup.moshi.JsonAdapter { *; }
-dontwarn com.squareup.moshi.**

# Room & SQLite
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Exceptions
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# NewPipeExtractor & Rhino JS engine
-keep class org.schabi.newpipe.extractor.ServiceList { *; }
-keep class org.schabi.newpipe.extractor.services.** { *; }
-dontwarn org.schabi.newpipe.**
-dontwarn org.mozilla.javascript.**
-dontwarn org.jsoup.**
-dontwarn org.w3c.**
-dontwarn javax.naming.**

# Koin dependency injection
-keep class com.example.ecodot.di.** { *; }
-keep class * extends org.koin.core.component.KoinComponent { *; }

# Media3 ExoPlayer & MediaSession entry points
-keep class androidx.media3.session.** { *; }
-keepclassmembers class * extends androidx.media3.common.Player { *; }
-dontwarn androidx.media3.**

# Kyant Backdrop / Haze GPU shaders
-dontwarn com.kyant.backdrop.**
-dontwarn dev.chrisbanes.haze.**

# Strip verbose/debug/info logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
