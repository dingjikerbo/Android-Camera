//
// Created by dingjikerbo on 17/6/1.
//

#include <stdio.h>
#include <stdlib.h>
#include <jni.h>

#include <GLES3/gl3.h>

#define TAG "bush"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)

void native_glReadPixels(JNIEnv *env, jobject clazz, jint x, jint y, jint width, jint height, jint format, jint type) {
    glReadPixels(x, y, width, height, format, type, 0);
}

static JNINativeMethod gMethods[] = {
        {"glReadPixels", "(IIIIII)V",              (void *) native_glReadPixels},
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
    const char *kClassName = "com/inuker/library/utils/NativeUtils";     //指定要注册的类
    return registerNativeMethods(env, kClassName, gMethods,
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