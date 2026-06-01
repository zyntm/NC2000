#include <jni.h>
#include <android/log.h>
#include <android/native_window_jni.h>
#include <SDL2/SDL.h>

#define LOG_TAG "NC2000Emulator"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Forward declarations from main emulator
extern int main(int argc, char* argv[]);
extern void main_loop();
extern bool InitAudioVideo();

extern "C" {

// Called from MainActivity to initialize the emulator
JNIEXPORT jint JNICALL
Java_com_example_nc2000emulator_EmulatorView_nativeInitEmulator(
        JNIEnv* env, jobject obj) {
    LOGI("Initializing NC2000 emulator");
    
    if (!InitAudioVideo()) {
        LOGE("Failed to initialize audio/video");
        return -1;
    }
    
    LOGI("Audio/video initialized successfully");
    return 0;
}

// Called from EmulatorView to start the main loop
JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_EmulatorView_nativeRun(JNIEnv* env, jobject obj) {
    LOGI("Starting emulator main loop");
    main_loop();
    LOGI("Main loop exited");
}

// Initialize display with Android surface
JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_EmulatorView_nativeInitDisplay(
        JNIEnv* env, jobject obj, jobject surface) {
    if (surface == nullptr) {
        LOGE("Surface is null");
        return;
    }
    
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (window == nullptr) {
        LOGE("Failed to get native window from surface");
        return;
    }
    
    LOGI("Native window initialized: %p", window);
    
    // Set up SDL to use this native window
    // Store the window pointer for SDL initialization
    ANativeWindow_release(window);
}

// Handle display size changes
JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_EmulatorView_nativeOnDisplayChanged(
        JNIEnv* env, jobject obj, jint width, jint height) {
    LOGI("Display size changed: %d x %d", width, height);
}

// Cleanup display resources
JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_EmulatorView_nativeCleanupDisplay(
        JNIEnv* env, jobject obj) {
    LOGI("Cleaning up display");
}

// Key down event from on-screen keyboard
JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_OnScreenKeyboard_nativeOnKeyDown(
        JNIEnv* env, jobject obj, jint keyCode) {
    LOGI("Key down: %d", keyCode);
    
    SDL_Event event;
    event.type = SDL_KEYDOWN;
    event.key.keysym.sym = keyCode;
    event.key.state = SDL_PRESSED;
    SDL_PushEvent(&event);
}

// Key up event from on-screen keyboard
JNIEXPORT void JNICALL
Java_com_example_nc2000emulator_OnScreenKeyboard_nativeOnKeyUp(
        JNIEnv* env, jobject obj, jint keyCode) {
    LOGI("Key up: %d", keyCode);
    
    SDL_Event event;
    event.type = SDL_KEYUP;
    event.key.keysym.sym = keyCode;
    event.key.state = SDL_RELEASED;
    SDL_PushEvent(&event);
}

}
