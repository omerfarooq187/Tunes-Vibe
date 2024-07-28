# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Jetpack Compose
# Jetpack Compose
# Keep all annotations
-keepattributes *Annotation*

# Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Dagger Hilt
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Navigation
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**


# Keep classes annotated with @Serializable for Kotlinx Serialization
-keepnames class kotlinx.serialization.Serializable { *; }
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Ensure all classes with @Serializable are not obfuscated or removed
-keep class com.example.android.tunesvibe.** { *; }
-dontwarn com.example.android.tunesvibe.**

# Keep your specific data classes used in serialization/deserialization
-keep class com.example.android.tunesvibe.MainScreenRoute { *; }
-keep class com.example.android.tunesvibe.PlaybackScreenRoute { *; }

# Coil (if you use it)
-keep class coil.** { *; }
-dontwarn coil.**


# Prevent obfuscation of the ViewModel with Hilt
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep generated Hilt components
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
