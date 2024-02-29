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
# General Android
-keep class com.android.** { *; }
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-dontwarn android.**

# Kotlin
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.** {
    <fields>;
}
-keep class kotlin.Metadata { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson

-keep class com.google.gson.stream.** { *; }



# Dagger
-dontwarn dagger.**
-keep class dagger.** { *; }
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* <init>(...);
    @dagger.* <methods>;
}
-keepattributes RuntimeVisibleAnnotations

# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Your app's specific classes (change to fit your package and classes)
-keep class com.example.** { *; }
