
#include <cstring>
#include <jni.h>
#include "sk_tr369_jni.h"
#include "sk_tr369_log.h"
#include "sk_jni_callback.h"
#include "sk_jni_network.h"
#include "vendor_defs.h"
#include "usp_err_codes.h"

#define JNI_REG_CLASS "com/sdt/opentr369/OpenTR369Native"

static JavaVM *mJavaVm = nullptr;
static jclass mClass = nullptr;

const struct {
    const char *name;
    const char *type;
} sFuncScript[] = {
        {.name="OpenTR369CallbackGet",
         .type="(ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;"},
        {.name="OpenTR369CallbackSet",
         .type="(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)I"},
        {.name="OpenTR369CallbackGetAttr",
         .type="(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"},
        {.name="OpenTR369CallbackSetAttr",
         .type="(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I"},
};
static jmethodID sJavaFunction[ARRAY_SIZE(sFuncScript)];

#define funcGet sJavaFunction[0]
#define funcSet sJavaFunction[1]
#define funcGetAttr sJavaFunction[2]
#define funcSetAttr sJavaFunction[3]

#ifdef __cplusplus
extern "C" {
#endif

static inline JNIEnv *getJNIEnv(bool *needsDetach) {
    *needsDetach = false;
    JNIEnv *env = nullptr;
    int status = mJavaVm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_4);
    if (status < 0) {
        JavaVMAttachArgs args = {JNI_VERSION_1_4, nullptr, nullptr};
        int result = mJavaVm->AttachCurrentThread(&env, (void *) &args);
        if (result != JNI_OK) {
            TX_ERR("Thread attach failed: %#x", result);
            return nullptr;
        }
        *needsDetach = true;
    }
    return env;
}

static inline void detachJNI() {
    int result = mJavaVm->DetachCurrentThread();
    if (result != JNI_OK) {
        TX_ERR("Thread detach failed: %#x", result);
    }
}

int SK_TR369_Callback_Get(const int what, char *dst, int size, const char *str1, const char *str2) {
    TX_ERR("==>: %d, %s, %s", what, str1, str2);
    do {
        bool needsDetach;
        const char *pStr = nullptr;
        JNIEnv *env = getJNIEnv(&needsDetach);
        CHECK_BREAK(env != nullptr);
        CHECK_BREAK(str1 != nullptr);
        CHECK_BREAK(dst != nullptr);
        CHECK_BREAK(size > 0);
        jstring req1, req2 = nullptr;
        if ((req1 = env->NewStringUTF(str1)) == nullptr)
            env->ExceptionClear();
        if (str2 && ((req2 = env->NewStringUTF(str2)) == nullptr))
            env->ExceptionClear();
        auto reply = (jstring) env->CallStaticObjectMethod(mClass, funcGet, what, req1, req2);
        if (reply) pStr = env->GetStringUTFChars(reply, nullptr);
        if (pStr) {
            memset(dst, 0, size);
            size_t len = strlen(pStr);
            TX_ERR("Outis: SK_TR369_Callback_Get Len: %d, Size: %d", len, size);
            len = (len > size) ? size : len;
            memcpy(dst, pStr, len);
            env->ReleaseStringUTFChars(reply, pStr);
        }
        if (req1) env->DeleteLocalRef(req1);
        if (req2) env->DeleteLocalRef(req2);

        if (env->ExceptionCheck()) env->ExceptionClear();
        if (needsDetach) detachJNI();
    } while (false);
    TX_ERR("<==: %s, %s", str1, dst);
    return 0;
}

int SK_TR369_Callback_Set(const int what, const char *str1, const char *str2, const char *str3) {
    TX_ERR("==>: %d, %s, %s, %s", what, str1, str2, str3);
    int ret = -1;
    do {
        bool needsDetach;
        JNIEnv *env = getJNIEnv(&needsDetach);
        CHECK_BREAK(env != nullptr);
        CHECK_BREAK(str1 != nullptr);
        jstring req1, req2 = nullptr, req3 = nullptr;
        if ((req1 = env->NewStringUTF(str1)) == nullptr) env->ExceptionClear();
        if (str2 && ((req2 = env->NewStringUTF(str2)) == nullptr)) env->ExceptionClear();
        if (str3 && ((req3 = env->NewStringUTF(str3)) == nullptr)) env->ExceptionClear();
        ret = env->CallStaticIntMethod(mClass, funcSet, what, req1, req2, req3);
        if (req1) env->DeleteLocalRef(req1);
        if (req2) env->DeleteLocalRef(req2);
        if (req3) env->DeleteLocalRef(req3);
        if (env->ExceptionCheck()) env->ExceptionClear();
        if (needsDetach) detachJNI();
    } while (false);
    TX_ERR("<==: %s, %s, %s", str1, str2, str3);
    return ret;
}

int SK_TR369_Callback_GetAttr(const char *path, const char *method, const char **value, unsigned int *len) {
    TX_ERR("path ==>: %s", path);
    do {
        bool needsDetach;
        const char *pStr = nullptr;
        JNIEnv *env = getJNIEnv(&needsDetach);
        jstring req1 = nullptr, req2 = nullptr;
        if (path && ((req1 = env->NewStringUTF(path)) == nullptr)) env->ExceptionClear();
        if (method && ((req2 = env->NewStringUTF(method)) == nullptr)) env->ExceptionClear();
        auto reply = (jstring) env->CallStaticObjectMethod(mClass, funcGetAttr, req1, req2);
        if (reply) pStr = env->GetStringUTFChars(reply, nullptr);
        if (pStr) {
            *value = strdup(pStr);
            *len = strlen(pStr);
            env->ReleaseStringUTFChars(reply, pStr);
        }
        if (req1) env->DeleteLocalRef(req1);
        if (req2) env->DeleteLocalRef(req2);
        if (env->ExceptionCheck()) env->ExceptionClear();
        if (needsDetach) detachJNI();
    } while (false);
    TX_ERR("path, value, len <==: %s, %s, %d", path, *value, *len);
    return 0;
}

int SK_TR369_Callback_SetAttr(const char *path, const char *method, const char *value, unsigned int len) {
    TX_ERR("==>:%s->%s", path, method);
    int ret = -1;
    do {
        bool needsDetach;
        JNIEnv *env = getJNIEnv(&needsDetach);
        jstring req1 = nullptr, req2 = nullptr, req3 = nullptr;
        if (path && ((req1 = env->NewStringUTF(path)) == nullptr)) env->ExceptionClear();
        if (method && ((req2 = env->NewStringUTF(method)) == nullptr)) env->ExceptionClear();
        if (value && ((req3 = env->NewStringUTF(value)) == nullptr)) env->ExceptionClear();
        ret = env->CallStaticIntMethod(mClass, funcSetAttr, req1, req2, req3);
        if (req1) env->DeleteLocalRef(req1);
        if (req2) env->DeleteLocalRef(req2);
        if (req3) env->DeleteLocalRef(req3);
        if (env->ExceptionCheck()) env->ExceptionClear();
        if (needsDetach) detachJNI();
    } while (false);
    TX_ERR("<==:%d-%s-ret=%d", len, value, ret);
    return ret;
}

static void SK_TR369_JniConfig(JNIEnv *env) {
    jclass clazz;
    env->GetJavaVM(&mJavaVm);

    if ((clazz = env->FindClass(JNI_REG_CLASS)) == nullptr) {
        TX_ERR("Call FindClass(%s) failed", JNI_REG_CLASS);
        return;
    }
    mClass = reinterpret_cast<jclass> (env->NewGlobalRef(clazz));

    for (int i = 0; i < ARRAY_SIZE(sFuncScript); i++) {
        if ((sJavaFunction[i] =
                env->GetStaticMethodID(mClass,
                                       sFuncScript[i].name,
                                       sFuncScript[i].type)) == nullptr) {
            TX_ERR("Call GetStaticMethodID %s(%s) failed", sFuncScript[i].name, sFuncScript[i].type);
            return;
        }
    }
}

#ifdef __cplusplus
}
#endif

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_stringFromJNI(JNIEnv* env, jclass thiz) {
    return env->NewStringUTF("Hello Test NDK !");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_OpenTR369Init(JNIEnv *env, jclass clazz, const jstring path) {

    SK_TR369_JniConfig(env);
    static skJniCallback_t jniCallFuncion = {
            .SK_TR369_Callback_Get = SK_TR369_Callback_Get,
            .SK_TR369_Callback_Set = SK_TR369_Callback_Set};
    skSetJniCallback(&jniCallFuncion);

//    SK_TR369_Register_Setter_Getter(SK_TR369_Callback_SetAttr, SK_TR369_Callback_GetAttr);

    const char *const filePath = env->GetStringUTFChars(path, nullptr);
    int ret = SK_TR369_Start(filePath);
    env->ReleaseStringUTFChars(path, filePath);
    return ret;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_SetInitFilePath(JNIEnv *env, jclass clazz, const jstring default_path) {
    const char *const defaultFilePath = env->GetStringUTFChars(default_path, nullptr);
    int ret = SK_TR369_SetInitFilePath(defaultFilePath);
    env->ReleaseStringUTFChars(default_path, defaultFilePath);
    return ret;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_GetDefaultFilePath(JNIEnv *env, jclass clazz) {
    char *filePath = SK_TR369_GetDefaultFilePath();
    if (filePath != nullptr) {
        return env->NewStringUTF(filePath);
    }
    return env->NewStringUTF("");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_GetDBParam(JNIEnv *env, jclass clazz, const jstring path) {
    const char *param = env->GetStringUTFChars(path, nullptr);
    char value[MAX_DM_VALUE_LEN];
    int ret = SK_TR369_GetDBParam(param, value);
    env->ReleaseStringUTFChars(path, param);

    if (ret == USP_ERR_OK) {
        return env->NewStringUTF(value);
    }
    return env->NewStringUTF("");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_SetDBParam(JNIEnv *env, jclass clazz, const jstring path, const jstring value) {
    const char *param = env->GetStringUTFChars(path, nullptr);
    const char *paramValue = env->GetStringUTFChars(value, nullptr);

    int ret = SK_TR369_SetDBParam(param, paramValue);
    env->ReleaseStringUTFChars(path, param);
    env->ReleaseStringUTFChars(value, paramValue);
    return ret;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_ShowData(JNIEnv *env, jclass clazz, const jstring cmd) {
    const char *command = env->GetStringUTFChars(cmd, nullptr);
    int ret = SK_TR369_ShowData(command);
    env->ReleaseStringUTFChars(cmd, command);
    return ret;
}

/* Network */

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_GetCACertString(JNIEnv *env, jclass clazz) {
    // TODO: implement GetCACertString()
    jstring strRet = env->NewStringUTF("");
    char *ca = SK_TR369_API_GetCACertString();
    if (ca) {
        strRet = env->NewStringUTF(ca);
        free(ca);
    }
    return strRet;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_GetDevCertString(JNIEnv *env, jclass clazz) {
    // TODO: implement GetDevCertString()
    jstring strRet = env->NewStringUTF("");
    char *cert = SK_TR369_API_GetDevCertString();
    if (cert) {
        strRet = env->NewStringUTF(cert);
        free(cert);
    }
    return strRet;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_GetDevKeyString(JNIEnv *env, jclass clazz) {
    // TODO: implement GetDevKeyString()
    jstring strRet = env->NewStringUTF("");
    char *key = SK_TR369_API_GetDevKeyString();
    if (key) {
        strRet = env->NewStringUTF(key);
        free(key);
    }
    return strRet;
}