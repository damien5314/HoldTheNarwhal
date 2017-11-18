# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Damien\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# OkHttp3, Retrofit2
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * { # Do we need this one?
  @retrofit2.http.* <methods>;
}

### Gson models ###
# TODO remove this
-keep class rxreddit.model.** { *; }

### ??? ###
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }

# Bypass
-keep class in.uncod.android.bypass.** { *; }

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
#-printmapping mapping.txt # This must not be present for Fabric plugin to upload mapping file
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Google Play Services; random files that are giving us trouble, but not sure why
-dontwarn android.content.ServiceConnection$$CC
-keep class android.content.ServiceConnection$$CC
