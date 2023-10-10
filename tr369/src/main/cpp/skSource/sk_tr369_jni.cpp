
#include <cstring>
#include <jni.h>
#include "sk_tr369.h"


extern "C" JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_stringFromJNI(JNIEnv* env, jclass thiz) {
    return env->NewStringUTF("Hello Test NDK !");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_OpenTR369Init(JNIEnv *env, jclass clazz, const jstring path) {
    const char *const filePath = env->GetStringUTFChars(path, nullptr);
    int ret = sk_tr369_start(filePath);
    env->ReleaseStringUTFChars(path, filePath);
    return ret;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_SetDBFilePath(JNIEnv *env, jclass clazz, const jstring path) {
    const char *const filePath = env->GetStringUTFChars(path, nullptr);
    int ret = sk_set_db_file_path(filePath);
    env->ReleaseStringUTFChars(path, filePath);
    return ret;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_GetDBFilePath(JNIEnv *env, jclass clazz) {
    char *filePath = sk_get_db_file_path();
    if (filePath != nullptr) {
        return env->NewStringUTF(filePath);
    }
    return env->NewStringUTF("");
}
