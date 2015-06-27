#include <string.h>
#include <jni.h>

typedef enum { false, true } bool;

jstring Java_com_ddiehl_android_htn_utils_NUtils_getRedditClientId(JNIEnv* env, jobject javaThis) {
    return (*env)->NewStringUTF(env, "***REMOVED***");
}

jstring Java_com_ddiehl_android_htn_utils_NUtils_getFlurryApiKey(JNIEnv* env, jobject javaThis, bool debugMode) {
    if (debugMode) {
        return (*env)->NewStringUTF(env, "***REMOVED***");
    } else {
        return (*env)->NewStringUTF(env, "***REMOVED***");
//        return (*env)->NewStringUTF(env, "***REMOVED***");
    }
}

jstring Java_com_ddiehl_android_htn_utils_NUtils_getMoPubApiKey(JNIEnv* env, jobject javaThis, bool debugMode) {
    if (debugMode) {
        return (*env)->NewStringUTF(env, "***REMOVED***");
    } else {
        return (*env)->NewStringUTF(env, "***REMOVED***");
//        return (*env)->NewStringUTF(env, "***REMOVED***");
    }
}