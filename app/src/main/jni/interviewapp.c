#include <jni.h>

// this file would normally not be included to hide the api key away but will be included
JNIEXPORT jstring JNICALL
Java_net_protect_interviewapp_BaseApplication_getApiKey(JNIEnv *env, jobject instance) {
    return (*env) -> NewStringUTF(env, "6b26cc7cacbb79c24cfab3bd97557e08");
}