# Keep stack trace line numbers legible for release crash reports
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Reflection-serialized remote, UI, and persistence models (Moshi / Gson / Room)
-keep class com.example.ecodot.data.remote.** { *; }
-keep class com.example.ecodot.data.local.entities.** { *; }
-keep class com.example.ecodot.util.CustomCover { *; }
-keep class com.example.ecodot.ui.viewmodel.Album { *; }
-keep class com.example.ecodot.ui.viewmodel.Artist { *; }
-keep class com.example.ecodot.ui.viewmodel.InstantSearchItem { *; }

# Moshi & Gson
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keepclassmembers class * extends com.squareup.moshi.JsonAdapter { *; }
-dontwarn com.squareup.moshi.**
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

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
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.**
-dontwarn org.mozilla.javascript.**
-dontwarn org.jsoup.**
-dontwarn org.w3c.**
-dontwarn javax.naming.**

# Koin dependency injection
-keep class org.koin.** { *; }
-keep class com.example.ecodot.di.** { *; }

# Media3 ExoPlayer & Notification session
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Kyant Backdrop / Haze GPU shaders
-keep class com.kyant.backdrop.** { *; }
-keep class dev.chrisbanes.haze.** { *; }
-dontwarn com.kyant.backdrop.**
-dontwarn dev.chrisbanes.haze.**
