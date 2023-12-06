
#include <cstring>
#include <cerrno>
#include <jni.h>
#include <unistd.h>
#include <net/if.h>
#include <sys/ioctl.h>
#include "sk_tr369_jni.h"
#include "sk_tr369_log.h"
#include "sk_jni_callback.h"
#include "sk_jni_network.h"
#include "vendor_defs.h"
#include "usp_err_codes.h"
#include "usp_log.h"

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
        {.name="OpenTR369CallbackStart", .type="()V"},
};
static jmethodID sJavaFunction[ARRAY_SIZE(sFuncScript)];

#define funcGet sJavaFunction[0]
#define funcSet sJavaFunction[1]
#define funcGetAttr sJavaFunction[2]
#define funcSetAttr sJavaFunction[3]
#define funcStart sJavaFunction[4]

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
            USP_LOG_Error("Thread attach failed: %#x", result);
            return nullptr;
        }
        *needsDetach = true;
    }
    return env;
}

static inline void detachJNI() {
    int result = mJavaVm->DetachCurrentThread();
    if (result != JNI_OK) {
        USP_LOG_Error("Thread detach failed: %#x", result);
    }
}

int SK_TR369_Callback_Get(const int what, char *dst, int size, const char *str1, const char *str2) {
    USP_LOG_Debug("%s ==>: %d, %s, %s", __FUNCTION__, what, str1, str2);
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
            len = (len > size) ? size : len;
            memcpy(dst, pStr, len);
            env->ReleaseStringUTFChars(reply, pStr);
        }
        if (req1) env->DeleteLocalRef(req1);
        if (req2) env->DeleteLocalRef(req2);

        if (env->ExceptionCheck()) env->ExceptionClear();
        if (needsDetach) detachJNI();
    } while (false);
    USP_LOG_Debug("%s <==: %s, %s", __FUNCTION__, str1, dst);
    return 0;
}

int SK_TR369_Callback_Set(const int what, const char *str1, const char *str2, const char *str3) {
    USP_LOG_Debug("%s ==>: %d, %s, %s, %s", __FUNCTION__, what, str1, str2, str3);
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
    USP_LOG_Debug("%s <==: %s, %s, %s", __FUNCTION__, str1, str2, str3);
    return ret;
}

int SK_TR369_Callback_GetAttr(const char *path, const char *method, const char **value,
                              unsigned int *len) {
    USP_LOG_Debug("%s ==>: %s", __FUNCTION__, path);
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
    USP_LOG_Debug("%s <==: %s, %s, %d", __FUNCTION__, path, *value, *len);
    return 0;
}

int SK_TR369_Callback_SetAttr(const char *path, const char *method, const char *value,
                              unsigned int len) {
    USP_LOG_Debug("%s ==>: %s->%s", __FUNCTION__, path, method);
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
    USP_LOG_Debug("%s <==: %d-%s-ret=%d", __FUNCTION__, len, value, ret);
    return ret;
}

void SK_TR369_Callback_Start() {
    USP_LOG_Debug("==>: %s", __FUNCTION__);
    do {
        bool needsDetach;
        JNIEnv *env = getJNIEnv(&needsDetach);
        CHECK_BREAK(env != nullptr);
        env->CallStaticVoidMethod(mClass, funcStart);
        if (env->ExceptionCheck()) env->ExceptionClear();
        if (needsDetach) detachJNI();
    } while (false);
    USP_LOG_Debug("<==: %s", __FUNCTION__);
}

static void SK_TR369_JniConfig(JNIEnv *env) {
    jclass clazz;
    env->GetJavaVM(&mJavaVm);

    if ((clazz = env->FindClass(JNI_REG_CLASS)) == nullptr) {
        USP_LOG_Error("Call FindClass(%s) failed", JNI_REG_CLASS);
        return;
    }
    mClass = reinterpret_cast<jclass> (env->NewGlobalRef(clazz));

    for (int i = 0; i < ARRAY_SIZE(sFuncScript); i++) {
        if ((sJavaFunction[i] =
                     env->GetStaticMethodID(mClass,
                                            sFuncScript[i].name,
                                            sFuncScript[i].type)) == nullptr) {
            USP_LOG_Error("Call GetStaticMethodID %s(%s) failed", sFuncScript[i].name,
                          sFuncScript[i].type);
            return;
        }
    }
}

#ifdef __cplusplus
}
#endif

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_stringFromJNI(JNIEnv *env, jclass thiz) {
    return env->NewStringUTF("Hello Test NDK !");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_OpenTR369Init(JNIEnv *env, jclass clazz,
                                                     const jstring path) {

    SK_TR369_JniConfig(env);
    static skJniCallback_t jniCallFuncion = {
            .SK_TR369_Callback_Get = SK_TR369_Callback_Get,
            .SK_TR369_Callback_Set = SK_TR369_Callback_Set,
            .SK_TR369_Callback_Start = SK_TR369_Callback_Start};
    skSetJniCallback(&jniCallFuncion);

    const char *const filePath = env->GetStringUTFChars(path, nullptr);
    int ret = SK_TR369_Start(filePath);
    env->ReleaseStringUTFChars(path, filePath);
    return ret;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_SetInitFilePath(JNIEnv *env, jclass clazz,
                                                       const jstring default_path) {
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
Java_com_sdt_opentr369_OpenTR369Native_SetDBParam(JNIEnv *env, jclass clazz, const jstring path,
                                                  const jstring value) {
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

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_GetNetDevInterfaceStatus(JNIEnv *env, jclass clazz,
                                                                jstring name) {
    // TODO: implement GetNetDevInterfaceStatus()
    struct ifreq ifr;
    int size = 50;
    int ifc_ctl_sock = -1;
    jstring strRet = env->NewStringUTF("");
    const char *paramName = env->GetStringUTFChars(name, nullptr);

    ALOGD("GetNetDevInterfaceStatus paramName1: %s", paramName);
    ifc_ctl_sock = socket(AF_INET, SOCK_DGRAM, 0);
    if (ifc_ctl_sock < 0) {
        ALOGE("GetNetDevInterfaceStatus socket() failed: %s", strerror(errno));
        return strRet;
    }
    memset(&ifr, 0, sizeof(struct ifreq));
    snprintf(ifr.ifr_name, IFNAMSIZ, "%s", paramName);

    if (ioctl(ifc_ctl_sock, SIOCGIFFLAGS, &ifr) < 0) {
        ALOGE("GetNetDevInterfaceStatus socket() failed: %s", strerror(errno));
        close(ifc_ctl_sock);
        return strRet;
    }
    ALOGD("GetNetDevInterfaceStatus ifr.ifr_flags: %08x", ifr.ifr_flags);

    char *cRet = (char *) malloc(size);
    if (cRet == nullptr) {
        ALOGE("GetNetDevInterfaceStatus malloc() failed: %s", strerror(errno));
        return strRet;
    }

#if 0
    if (IFF_UP == (ifr.ifr_flags & IFF_UP)) {
        snprintf(ret, size, "Up");
    }
    else if(IFF_RUNNING != (ifr.ifr_flags & IFF_RUNNING)){
        snprintf(ret, size, "Down");
    }
#else
    if ((ifr.ifr_flags == 0x00001043)) {
        snprintf(cRet, size, "Up");
    } else if (0x00001003 == ifr.ifr_flags || 0x00001002 == ifr.ifr_flags) {
        //ifconfig eth0 down --> 00001002
        //disable wif on setting --> 00001003
        //liaoqs 2020.9.20
        ALOGD("GetNetDevInterfaceStatus paramName2: %s, size = %d", paramName, size);
        snprintf(cRet, size, "Down");
    }
#endif
    else if (IFF_LOWER_UP == (ifr.ifr_flags & IFF_LOWER_UP)) {
        ALOGD("GetNetDevInterfaceStatus paramName3: %s", paramName);
        snprintf(cRet, size, "LowerLayerDown");
    } else if (IFF_DORMANT == (ifr.ifr_flags & IFF_DORMANT)) {
        ALOGD("GetNetDevInterfaceStatus paramName4: %s", paramName);
        snprintf(cRet, size, "Dormant");
    } else {
        ALOGD("GetNetDevInterfaceStatus paramName5: %s", paramName);
        snprintf(cRet, size, "Unknown");
    }

    ALOGD("GetNetDevInterfaceStatus status: %s", cRet);
    close(ifc_ctl_sock);

    strRet = env->NewStringUTF(cRet);
    free(cRet);
    env->ReleaseStringUTFChars(name, paramName);
    return strRet;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_GetWirelessNoise(JNIEnv *env, jclass clazz, jstring name) {
    // TODO: implement GetWirelessNoise()
    int ret = 0;
    char strName[16] = {0};
    int nElem[4] = {0};
    char strLine[512] = {0};

    FILE *fp = fopen("/proc/net/wireless", "r");
    if (fp == nullptr) {
        ALOGE("GetWirelessNoise fopen() failed: %s", strerror(errno));
        return ret;
    }

    if (!fgets(strLine, sizeof(strLine), fp)) {
        ALOGE("GetWirelessNoise fgets() 1 failed: %s", strerror(errno));
        fclose(fp);
        return ret;
    }

    if (!fgets(strLine, sizeof(strLine), fp)) {
        ALOGE("GetWirelessNoise fgets() 2 failed: %s", strerror(errno));
        fclose(fp);
        return ret;
    }

    const char *paramName = env->GetStringUTFChars(name, nullptr);
    while (nullptr != fgets(strLine, sizeof(strLine), fp)) {
        memset(strName, 0x00, sizeof(strName));
        memset(nElem, 0x00, sizeof(nElem));
        ALOGE("getWirelessNoise strLine:%s", strLine);
        sscanf(strLine, "%s%d%d%d%d", strName, &nElem[0], &nElem[1], &nElem[2], &nElem[3]);
        strName[strlen(strName) - 1] = 0;
        ALOGE("getWirelessNoise face:%s tus:%d link:%d level:%d noise:%d", strName, nElem[0],
              nElem[1], nElem[2], nElem[3]);

        if (!strcmp(paramName, strName)) {
            ret = nElem[3];
            break;
        }
    }
    fclose(fp);
    env->ReleaseStringUTFChars(name, paramName);
    return ret;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sdt_opentr369_OpenTR369Native_GetNetInterfaceStatus(JNIEnv *env, jclass clazz,
                                                             jstring name) {
    // TODO: implement GetNetInterfaceStatus()
    char strName[16] = {0};
    int nElem[10] = {0};
    char strLine[512] = {0};
    jstring strRet = env->NewStringUTF("");

    FILE *fp = fopen("/proc/net/dev", "r");
    if (fp == nullptr) {
        ALOGE("GetNetInterfaceStatus fopen() failed: %s", strerror(errno));
        return strRet;
    }

    if (!fgets(strLine, sizeof(strLine), fp)) {
        ALOGE("GetNetInterfaceStatus fgets() 1 failed: %s", strerror(errno));
        fclose(fp);
        return strRet;
    }

    if (!fgets(strLine, sizeof(strLine), fp)) {
        ALOGE("GetNetInterfaceStatus fgets() 1 failed: %s", strerror(errno));
        fclose(fp);
        return strRet;
    }

    int size = 128;
    char *cRet = (char *) malloc(size);
    if (cRet == nullptr) {
        ALOGE("GetNetInterfaceStatus malloc() failed: %s", strerror(errno));
        return strRet;
    }
    const char *paramName = env->GetStringUTFChars(name, nullptr);

    while (nullptr != fgets(strLine, sizeof(strLine), fp)) {
        memset(strName, 0x00, sizeof(strName));
        memset(nElem, 0x00, sizeof(nElem));
        ALOGE("getNetIfaceStats strLine: %s", strLine);
        sscanf(strLine, "%s%d%d%d%d%d%d%d%d%d%d", strName, &nElem[0], &nElem[1], &nElem[2],
               &nElem[3], &nElem[4], &nElem[5], &nElem[6], &nElem[7], &nElem[8], &nElem[9]);
        strName[strlen(strName) - 1] = 0;
        ALOGE("GetNetInterfaceStatus link[%s]: face:%s Receive[bytes:%d packets:%d errs:%d drop:%d fifo:%d frame:%d compressed:%d multicast:%d] Transmit[bytes:%d packets:%d",
              paramName, strName, nElem[0], nElem[1],
              nElem[2], nElem[3], nElem[4], nElem[5],
              nElem[6], nElem[7], nElem[8], nElem[9]);

        if (!strcmp(paramName, strName)) {
            snprintf(cRet, size, "%d;%d;%d;%d;%d;%d", nElem[8], nElem[0], nElem[9], nElem[1],
                     nElem[3], nElem[2]);
            ALOGE("GetNetInterfaceStatus ret = %s ", cRet);
            strRet = env->NewStringUTF(cRet);
            break;
        }
    }
    fclose(fp);
    free(cRet);
    env->ReleaseStringUTFChars(name, paramName);
    return strRet;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_AddMultiObject(JNIEnv *env, jclass clazz, jstring path,
                                                      jint num) {
    // TODO: implement AddInstance()
    const char *param = env->GetStringUTFChars(path, nullptr);
    int ret = SK_TR369_AddMultiObject(param, num);
    env->ReleaseStringUTFChars(path, param);
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_sdt_opentr369_OpenTR369Native_DelMultiObject(JNIEnv *env, jclass clazz, jstring path) {
    // TODO: implement DeleteInstance()
    const char *param = env->GetStringUTFChars(path, nullptr);
    int ret = SK_TR369_DelMultiObject(param);
    env->ReleaseStringUTFChars(path, param);
    return ret;
}
