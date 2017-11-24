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


#-keep,allowobfuscation class rxreddit.model.** { *; }

#-keep class rxreddit.model.Listing** { *; }
#-keep class rxreddit.model.Link** { *; }
#-keep class rxreddit.model.Image** { *; }
#-keep class rxreddit.model.ListingData** { *; }
#-keep class rxreddit.model.ListingResponse** { *; }
#-keep class rxreddit.model.ListingResponseData** { *; }
#-keep class rxreddit.model.Media** { *; }
#-keep class rxreddit.model.MediaEmbed** { *; }
#-keep class rxreddit.model.ModReport** { *; }
#-keep class rxreddit.model.UserReport** { *; }
#-keep class rxreddit.model.AbsComment** { *; }
#-keep class rxreddit.model.AccessToken** { *; }
#-keep class rxreddit.model.AddCommentResponse** { *; }
#-keep class rxreddit.model.ApplicationAccessToken** { *; }
#-keep class rxreddit.model.Archivable** { *; }
#-keep class rxreddit.model.Comment** { *; }
#-keep class rxreddit.model.CommentStub** { *; }
#-keep class rxreddit.model.Friend** { *; }
#-keep class rxreddit.model.FriendInfo** { *; }
#-keep class rxreddit.model.Hideable** { *; }
#-keep class rxreddit.model.MoreChildrenResponse** { *; }
#-keep class rxreddit.model.PrivateMessage** { *; }
#-keep class rxreddit.model.ReportForm** { *; }
#-keep class rxreddit.model.Subreddit** { *; }
#-keep class rxreddit.model.Savable** { *; }
#-keep class rxreddit.model.SubmitPostResponse** { *; }
#-keep class rxreddit.model.SubredditRule** { *; }
#-keep class rxreddit.model.SubredditRules** { *; }
#-keep class rxreddit.model.SubredditSidebar** { *; }
#-keep class rxreddit.model.Trophy** { *; }
#-keep class rxreddit.model.TrophyResponse** { *; }
#-keep class rxreddit.model.TrophyResponseData** { *; }
#-keep class rxreddit.model.UserAccessToken** { *; }
#-keep class rxreddit.model.UserIdentity** { *; }
#-keep class rxreddit.model.UserIdentityListing** { *; }
#-keep class rxreddit.model.UserSettings** { *; }
#-keep class rxreddit.model.Votable** { *; }

### Trial zone

### Safe zone

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
