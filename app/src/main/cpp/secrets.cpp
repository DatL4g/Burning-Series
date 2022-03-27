#include "secrets.hpp"

#include <jni.h>

#include "sha256.hpp"
#include "sha256.cpp"

/* Copyright (c) 2020-present Klaxit SAS
*
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
*
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
* OTHER DEALINGS IN THE SOFTWARE.
*/
char *customDecode(char *str, int s) {
    /* Add your own logic here
    * To improve your key security you can encode it before to integrate it in the app.
    * And then decode it with your own logic in this function.
    */
    return str;
}

jstring getOriginalKey(
        const char *obfuscatedSecret,
        int obfuscatedSecretSize,
        jstring obfuscatingJStr,
        JNIEnv *pEnv) {

    // Get the obfuscating string SHA256 as the obfuscator
    const char *obfuscatingStr = pEnv->GetStringUTFChars(obfuscatingJStr, nullptr);
    char buffer[2 * SHA256::DIGEST_SIZE + 1];

    sha256(obfuscatingStr, buffer);
    const char *obfuscator = buffer;

    // Apply a XOR between the obfuscated key and the obfuscating string to get original string
    char out[obfuscatedSecretSize + 1];
    for (int i = 0; i < obfuscatedSecretSize; i++) {
        out[i] = obfuscatedSecret[i] ^ obfuscator[i % strlen(obfuscator)];
    }

    // Add string terminal delimiter
    out[obfuscatedSecretSize] = 0x0;

    //(Optional) To improve key security
    return pEnv->NewStringUTF(customDecode(out, strlen(out)/2));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_de_datlag_burningseries_Secrets_getWrapAPIToken(
        JNIEnv* pEnv,
        jobject pThis,
        jstring packageName) {
     char obfuscatedSecret[] = { 0x5b, 0x1c, 0x4f, 0x5e, 0x2c, 0x3, 0x51, 0x68, 0x55, 0x78, 0xe, 0xd, 0x5c, 0x55, 0x79, 0x57, 0x5f, 0x2, 0x67, 0x0, 0xc, 0x2e, 0x52, 0x71, 0x59, 0x62, 0x46, 0x52, 0x5, 0x53, 0xb, 0x2f };
     return getOriginalKey(obfuscatedSecret, sizeof(obfuscatedSecret), packageName, pEnv);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_de_datlag_burningseries_Secrets_getM3OToken(
        JNIEnv* pEnv,
        jobject pThis,
        jstring packageName) {
     char obfuscatedSecret[] = { 0x78, 0x22, 0x61, 0x3, 0x3f, 0x35, 0x79, 0x52, 0x7d, 0x65, 0x3, 0x10, 0x7a, 0x51, 0x7f, 0x56, 0x78, 0x63, 0x3, 0x6, 0x7a, 0x33, 0x69, 0x58, 0x78, 0x64, 0x72, 0x42, 0x7d, 0x1f, 0xf, 0x11, 0x29, 0x65, 0x60, 0x59, 0x29, 0x35, 0x2, 0x4f, 0x2c, 0x72, 0x33, 0x4c, 0x2d, 0x6, 0x73, 0x2 };
     return getOriginalKey(obfuscatedSecret, sizeof(obfuscatedSecret), packageName, pEnv);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_de_datlag_burningseries_Secrets_getMALClientId(
        JNIEnv* pEnv,
        jobject pThis,
        jstring packageName) {
     char obfuscatedSecret[] = { 0x53, 0x5, 0x5a, 0x50, 0x7, 0x50, 0x52, 0xa, 0x5, 0x8, 0x1, 0x54, 0x4, 0x50, 0x7, 0x0, 0x3, 0x56, 0x57, 0x3, 0xc, 0x0, 0x6, 0x56, 0x7, 0x57, 0x5, 0x5e, 0x8, 0x3, 0x7, 0x54 };
     return getOriginalKey(obfuscatedSecret, sizeof(obfuscatedSecret), packageName, pEnv);
}
