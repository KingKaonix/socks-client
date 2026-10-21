#define main tun2socks_main
#include "tun2socks_patched.c"
#undef main

#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>

#define LOG_TAG "SocksClient"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

JNIEXPORT jint JNICALL
Java_com_example_socksclient_Tun2socks_startTun2socks(JNIEnv *env, jobject thiz,
    jstring tunDev, jstring socksServer, jstring udpgwServer) {

    const char *tun = (*env)->GetStringUTFChars(env, tunDev, NULL);
    const char *socks = (*env)->GetStringUTFChars(env, socksServer, NULL);
    const char *udpgw = (*env)->GetStringUTFChars(env, udpgwServer, NULL);

    char *argv[] = {
        "tun2socks",
        "--tundev", (char*)tun,
        "--netif-ipaddr", "10.0.0.2",
        "--netif-netmask", "255.255.255.0",
        "--socks-server-addr", (char*)socks,
        "--udpgw-remote-server-addr", (char*)udpgw,
        NULL
    };
    int argc = 11;

    LOGI("Starting tun2socks: tun=%s, socks=%s, udpgw=%s", tun, socks, udpgw);

    int result = tun2socks_main(argc, argv);

    (*env)->ReleaseStringUTFChars(env, tunDev, tun);
    (*env)->ReleaseStringUTFChars(env, socksServer, socks);
    (*env)->ReleaseStringUTFChars(env, udpgwServer, udpgw);

    return result;
}
