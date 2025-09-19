# MsgMates Proguard/R8 Rules
# =========================

# Keep Hilt / Dependency Injection
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class com.google.dagger.** { *; }
-keep class com.msgmates.app.di.** { *; }

# Keep Hilt generated classes
-keep class com.msgmates.app.**_HiltModules { *; }
-keep class com.msgmates.app.**_HiltModules$* { *; }
-keep class com.msgmates.app.**_GeneratedInjector { *; }

# Keep Navigation safe-args
-keepclassmembers class ** implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Navigation Component
-keep class androidx.navigation.** { *; }
-keep class com.msgmates.app.navigation.** { *; }

# Keep Kotlin metadata (for reflection-based features)
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# Keep Models used by serialization
-keep class com.msgmates.app.model.** { *; }
-keep class com.msgmates.app.data.model.** { *; }

# Keep ViewBinding classes
-keep class com.msgmates.app.databinding.** { *; }

# Keep EventLogger for crash reporting
-keep class com.msgmates.app.core.analytics.EventLogger { *; }

# Keep StatusCapsuleMapper
-keep class com.msgmates.app.ui.common.StatusCapsuleMapper { *; }

# Keep ThemeManager
-keep class com.msgmates.app.core.theme.ThemeManager { *; }

# Keep DataStore classes
-keep class androidx.datastore.** { *; }

# Keep Room Database
-keep class androidx.room.** { *; }
-keep class com.msgmates.app.data.database.** { *; }

# Keep Retrofit/Gson
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }

# Keep Lifecycle
-keep class androidx.lifecycle.** { *; }

# Keep Material Components
-keep class com.google.android.material.** { *; }

# Keep AppCompat
-keep class androidx.appcompat.** { *; }

# Keep Fragment classes
-keep class androidx.fragment.** { *; }
-keep class com.msgmates.app.ui.**.Fragment { *; }

# Keep Activity classes
-keep class com.msgmates.app.ui.**.Activity { *; }

# Keep ViewModel classes
-keep class com.msgmates.app.ui.**.ViewModel { *; }
-keep class com.msgmates.app.ui.**.ViewModel$* { *; }

# Keep Repository classes
-keep class com.msgmates.app.core.**.Repository { *; }
-keep class com.msgmates.app.data.repository.** { *; }

# Keep Adapter classes
-keep class com.msgmates.app.ui.**.Adapter { *; }
-keep class com.msgmates.app.ui.**.Adapter$* { *; }

# Keep DiffUtil classes
-keep class com.msgmates.app.ui.**.DiffCallback { *; }
-keep class com.msgmates.app.ui.**.ItemDiffCallback { *; }

# Keep BottomSheet classes
-keep class com.msgmates.app.ui.**.BottomSheet { *; }

# Keep Utility classes
-keep class com.msgmates.app.core.utils.** { *; }

# Keep Animation classes
-keep class com.msgmates.app.core.utils.AnimationUtils { *; }

# Keep Notification classes
-keep class com.msgmates.app.core.notifications.** { *; }

# Keep Disaster Mode classes
-keep class com.msgmates.app.core.disaster.** { *; }

# Keep Connectivity classes
-keep class com.msgmates.app.core.connectivity.** { *; }

# Keep DataStore classes
-keep class com.msgmates.app.core.datastore.** { *; }

# Keep BuildConfig fields
-keep class com.msgmates.app.BuildConfig { *; }

# Keep Application class
-keep class com.msgmates.app.App { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep crash reporting
-keep class com.msgmates.app.core.analytics.EventLogger {
    public static void log*(...);
}

# Optimize
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile