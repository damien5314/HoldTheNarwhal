# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Damien\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

### Okio, OkHttp, Retrofit ###
-dontwarn okio.**
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-dontwarn com.squareup.okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * { # Do we need this one?
  @retrofit2.http.* <methods>;
}
-dontwarn com.squareup.picasso.**

### Gson models ###
-keep class rxreddit.model.** { *; }

### Flurry ###
-dontwarn com.flurry.**

### Debugging ###
#-renamesourcefileattribute SourceFile
#-keepattributes SourceFile, LineNumberTable

### ??? ###
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }

### Butter Knife ###
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-dontwarn butterknife.Views$InjectViewProcessor
-dontwarn com.gc.materialdesign.views.**

### Retrolambda and RxAndroid ###
-dontwarn java.lang.invoke.*

# Bypass
-keep class in.uncod.android.bypass.** { *; }

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
