//
// Created by liwentian on 17/6/1.
//

#include <stdio.h>
#include <stdlib.h>
#include <jni.h>

#include <sys/time.h>

#include <android/log.h>

#define TAG "bush"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)

static int mSize;
static jbyte *copyBytes;

#define CLASS "com/inuker/datacopy/NativeCopy"

jmethodID onCallback;

static unsigned long long currentTime() {
    struct timeval curtime;
    gettimeofday(&curtime, NULL);
    return curtime.tv_sec * 1000 + curtime.tv_usec / 1000;
}

static void nativeInit(JNIEnv *env, jobject clazz, jint size) {
    mSize = size;
    copyBytes = (jbyte *) calloc(1, mSize);

    jclass nativeCopyClaz = env->FindClass(CLASS);
    onCallback = env->GetStaticMethodID(nativeCopyClaz, "onCallback", "(IJ)V");

    LOGV("copyBytes size = %d", mSize);
}

static void nativeWrite(JNIEnv *env, jclass clazz, jbyteArray data) {
    long start = currentTime();

    jbyte * bytes = env->GetByteArrayElements(data, NULL);
    memcpy(copyBytes, bytes, mSize);

    long time = currentTime() - start;

    LOGV("Write buffer takes %dms", time);
    env->CallStaticVoidMethod(clazz, onCallback, 1, time);
}

static void nativeRead(JNIEnv *env, jclass clazz, jbyteArray data) {
    long start = currentTime();

    jbyte * bytes = env->GetByteArrayElements(data, NULL);
    memcpy(bytes, copyBytes, mSize);

    long time = currentTime() - start;

    LOGV("Read buffer takes %dms", time);
    env->CallStaticVoidMethod(clazz, onCallback, 2, time);
}

static JNINativeMethod gMethods[] = {
        {"nativeInit", "(I)V",             (void *) nativeInit},
        {"nativeWrite", "([B)V", (void *) nativeWrite},
        {"nativeRead", "([B)V", (void *) nativeRead},
};

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env) {
    return registerNativeMethods(env, CLASS, gMethods,
                                 sizeof(gMethods) / sizeof(gMethods[0]));
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    if (!registerNatives(env)) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}