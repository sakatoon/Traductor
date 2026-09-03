# ML Kit rules
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }

# Text to Speech and Speech Recognizer
-keep class android.speech.** { *; }

# ViewModel and LiveData/Flow if needed (usually handled by default rules)
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
