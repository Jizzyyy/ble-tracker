# Proguard rules for BleTracker
-keepattributes *Annotation*
-dontwarn java.lang.invoke.**

# Keep Domain & Room Database entities
-keep class com.bletracker.domain.model.** { *; }
-keep class com.bletracker.data.local.entity.** { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
