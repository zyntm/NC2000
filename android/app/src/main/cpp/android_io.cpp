#include <android/log.h>
#include <jni.h>
#include <android/native_window_jni.h>
#include <SDL2/SDL.h>
#include <unistd.h>
#include <sys/stat.h>

#define LOG_TAG "NC2000_Android"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Forward declarations from emulator core
extern void main_loop();
extern bool InitAudioVideo();

static ANativeWindow* g_native_window = nullptr;
static int g_display_width = 0;
static int g_display_height = 0;

extern "C" {

JNIEXPORT jint JNICALL
Java_com_example_nc2000emulator_EmulatorView_nativeInitEmulator(
        JNIEnv* env, jobject thiz) {
    LOGI("Initializing NC2000 emulator");
    
    if (!InitAudioVideo()) {
        LOGE("Failed to initialize audio/video");
        return -1;
    }
    
    LOGI("Emulator initialized successfully");
    return 0;
}

JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_EmulatorView_RenderThread_nativeRun(
        JNIEnv* env, jobject thiz) {
    LOGI("Starting emulator main loop");
    main_loop();
    LOGI("Main loop exited");
}

JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_EmulatorView_nativeInitDisplay(
        JNIEnv* env, jobject thiz, jobject surface) {
    if (surface == nullptr) {
        LOGE("Surface is null");
        return;
    }
    
    if (g_native_window != nullptr) {
        ANativeWindow_release(g_native_window);
    }
    
    g_native_window = ANativeWindow_fromSurface(env, surface);
    if (g_native_window == nullptr) {
        LOGE("Failed to get native window from surface");
        return;
    }
    
    LOGI("Native window initialized: %p", g_native_window);
}

JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_EmulatorView_nativeOnDisplayChanged(
        JNIEnv* env, jobject thiz, jint width, jint height) {
    LOGI("Display size changed: %d x %d", width, height);
    g_display_width = width;
    g_display_height = height;
}

JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_EmulatorView_nativeCleanupDisplay(
        JNIEnv* env, jobject thiz) {
    LOGI("Cleaning up display");
    if (g_native_window != nullptr) {
        ANativeWindow_release(g_native_window);
        g_native_window = nullptr;
    }
}

JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_OnScreenKeyboard_nativeOnKeyDown(
        JNIEnv* env, jobject thiz, jint keyCode) {
    LOGD("Key down: 0x%X", keyCode);
    
    SDL_Event event;
    SDL_zero(event);
    event.type = SDL_KEYDOWN;
    event.key.keysym.sym = keyCode;
    event.key.state = SDL_PRESSED;
    SDL_PushEvent(&event);
}

JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_OnScreenKeyboard_nativeOnKeyUp(
        JNIEnv* env, jobject thiz, jint keyCode) {
    LOGD("Key up: 0x%X", keyCode);
    
    SDL_Event event;
    SDL_zero(event);
    event.type = SDL_KEYUP;
    event.key.keysym.sym = keyCode;
    event.key.state = SDL_RELEASED;
    SDL_PushEvent(&event);
}

JNIEXPORT jstring JNICALL
Java_com_example_nc2000emulator_PermissionManager_getRomPath(
        JNIEnv* env, jobject thiz) {
    const char* rom_path = "/sdcard/wqx/";
    return env->NewStringUTF(rom_path);
}

}
