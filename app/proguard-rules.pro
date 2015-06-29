# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Damien\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

### Remove logging ###
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

### Otto ###
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

### Okio, OkHttp, Retrofit ###
-dontwarn okio.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-dontwarn retrofit.**

### Gson models ###
-keep class com.ddiehl.reddit.** { *; }

### Flurry ###
-dontwarn com.flurry.**

### RxJava ###
#-dontwarn rx.**

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

### Gson ###
# Gson uses generic type information stored in a class file when working with
# fields. Proguard removes such information by default, so configure it to keep
# all of it.
#-keepattributes Signature

# For using GSON @Expose annotation
#-keepattributes *Annotation*

# Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
#-keep class com.google.gson.examples.android.model.** { *; }