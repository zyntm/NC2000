# NC2000 Emulator - Android Port Implementation Guide

## 📋 Project Overview

This is a complete Android port of the NC2000 6502-based retro computer emulator (文曲星 dictionary simulator for NC1020/NC2000/NC2600 models).

**Repository**: [zyntm/NC2000](https://github.com/zyntm/NC2000)  
**Branch**: `android-port`  
**Platform**: Android 5.0+ (API 21+)

## 🏗️ Architecture

### Three-Layer Stack

```
┌─────────────────────────────────────────┐
│    Android Java UI Framework            │
│  (Activities, Permissions, Views)      │
├─────────────────────────────────────────┤
│         JNI Bridge Layer                │
│  (Java ↔ C++ Communication)            │
├─────────────────────────────────────────┤
│      SDL2 Abstraction Layer            │
│  (Audio, Video, Input, Threading)     │
├─────────────────────────────────────────┤
│     Original Emulator Core (C++)       │
│  (CPU, Memory, I/O, Display, Sound)   │
└─────────────────────────────────────────┘
```

## 📂 File Structure

```
android/
├── build.gradle                          # Gradle configuration
├── CMakeLists.txt                        # NDK compilation setup
├── app/
│   ├── build.gradle                      # App-specific build config
│   └── src/main/
│       ├── AndroidManifest.xml           # Permissions & Activities
│       ├── res/
│       │   ├── layout/
│       │   │   ├── activity_main.xml     # Main emulator UI layout
│       │   │   └── activity_permission.xml # Permission request UI
│       │   └── values/
│       │       └── strings.xml           # Localized strings (Chinese)
│       ├── java/com/example/nc2000emulator/
│       │   ├── MainActivity.java         # Main activity
│       │   ├── PermissionActivity.java   # File access permission handler
│       │   ├── PermissionManager.java    # Permission management logic
│       │   ├── EmulatorView.java         # SDL surface view wrapper
│       │   └── OnScreenKeyboard.java     # NC1020 keyboard UI
│       └── cpp/
│           ├── native-lib.cpp            # JNI bridge (Display/Audio init)
│           └── android_io.cpp            # Android-specific I/O & JNI
```

## 🔑 Key Features

### 1️⃣ File Access Permission System

**Location**: `PermissionActivity.java` & `PermissionManager.java`

- **Android 11+ (API 30+)**: Uses `MANAGE_EXTERNAL_STORAGE` permission
- **Android 6-10 (API 23-29)**: Uses `READ/WRITE_EXTERNAL_STORAGE` permissions
- **Pre-Android 6**: Automatic grant (manifest-only)

**ROM Path**: `/sdcard/wqx/`

```java
// Check permission
boolean hasPermission = permissionManager.hasFileAccessPermission();

// Get ROM directory
String romPath = permissionManager.getRomPath();
```

### 2️⃣ On-Screen Keyboard (NC1020 Layout)

**Location**: `OnScreenKeyboard.java`

- **Layout**: 5 rows × 7 columns
- **Keys**: ON, 学习查看, Numbers 0-9, Function keys, DEL, OFF, ENT
- **Visual Feedback**: 
  - Color change when pressed (#E0E0E0 → #909090)
  - Touch event handling with key mapping

```
[ON] [学] [习] [查] [看] [★] [DEL]
[Aa] [1 ] [2 ] [3 ] [4 ] [5 ] [6 ]
[Bb] [7 ] [8 ] [9 ] [0 ] [. ] [? ]
[Cc] [、] [；] [' ] [，] [" ] [! ]
[ENT][空格][◄ ] [▲ ] [▼ ] [► ] [OFF]
```

### 3️⃣ Display & Rendering

**Location**: `EmulatorView.java` + `android_io.cpp`

- Uses SDL2 SurfaceView integration
- Renders to Android's native window
- Supports fullscreen landscape orientation
- Framebuffer streaming via SDL2 Texture

### 4️⃣ Audio System

**Location**: `sound.cpp` (existing) + JNI bridge

- SDL2 audio callback mixed with Android AudioTrack
- Beeper (44.1 kHz) + DSP audio (8 kHz) resampling
- Thread-safe audio queueing

### 5️⃣ Input System

**Location**: `OnScreenKeyboard.java` + JNI bridge

- Touch events → SDL key events via `nativeOnKeyDown/nativeOnKeyUp`
- Key codes mapped to emulator's input handler
- Real-time visual feedback

## 🔗 JNI Bridge

### Core Methods

```cpp
// Emulator initialization
JNIEXPORT jint JNICALL Java_com_example_nc2000emulator_EmulatorView_nativeInitEmulator()

// Main loop execution
JNIEXPORT void JNICALL Java_com_example_nc2000emulator_EmulatorView_RenderThread_nativeRun()

// Display management
JNIEXPORT void JNICALL Java_com_example_nc2000emulator_EmulatorView_nativeInitDisplay(jobject surface)
JNIEXPORT void JNICALL Java_com_example_nc2000emulator_EmulatorView_nativeOnDisplayChanged(int w, int h)

// Input events
JNIEXPORT void JNICALL Java_com_example_nc2000emulator_OnScreenKeyboard_nativeOnKeyDown(int keyCode)
JNIEXPORT void JNICALL Java_com_example_nc2000emulator_OnScreenKeyboard_nativeOnKeyUp(int keyCode)
```

## 🛠️ Build Configuration

### CMakeLists.txt Setup

```cmake
# SDK version: API 21 (Android 5.0)
add_executable(nc2000-emulator ...)

# Links SDL2, libc++, Android NDK libs
target_link_libraries(nc2000-emulator
    SDL2::SDL2
    log
    android
)
```

### Gradle Configuration

```gradle
android {
    compileSdk 33
    minSdk 21
    targetSdk 33
    
    externalNativeBuild {
        cmake { path "CMakeLists.txt" }
    }
}
```

## 🚀 Compilation & Deployment

### Prerequisites

- Android Studio Giraffe (2022.3.1) or later
- Android NDK r21 or later
- SDL2 Android port (included in CMake)

### Build Steps

```bash
# 1. Clone repository
git clone https://github.com/zyntm/NC2000.git
cd NC2000
git checkout android-port

# 2. Build Android app
cd android
./gradlew build

# 3. Install APK
./gradlew installDebug
```

### Key Build Flags

```
-DANDROID_PLATFORM=android-21
-std=c++11
-O3 -pthread -DHANDYPSP
```

## 📝 Integration Steps for Future Development

### 1. **Update main.cpp**
   - Replace desktop-specific SDL window initialization
   - Use Android surface from JNI instead
   - Remove UDP server initialization on Android

### 2. **Enhance OnScreenKeyboard**
   - Add all NC1020 character mappings
   - Implement long-press and key repeat
   - Add haptic feedback support

### 3. **Add Save/Load UI**
   - Create activity for save state management
   - Implement game save browsing UI
   - Add auto-save on app pause

### 4. **Performance Optimization**
   - Profile JIT compilation
   - Implement ART optimization hints
   - Add CPU frequency scaling interface

### 5. **Testing**
   - Test on API 21, 28, 31, 33 devices
   - Verify audio sync with display
   - Check permission flow on all Android versions

## 🔄 Migration from Windows Build

### Differences to Handle

| Aspect | Windows | Android |
|--------|---------|---------|
| Window Creation | SDL_CreateWindow | ANativeWindow (JNI) |
| File I/O | Direct filesystem | Content Provider URI |
| Audio | SDL_AudioDevice | SDL + AudioTrack |
| Input | Keyboard events | Touch + JNI |
| Storage | User Documents | /sdcard/wqx |
| Permission | Administrator | Runtime permissions |

### Removed Features (Per Requirements)

- ✅ UDP server (`udp_server.cpp`) - Disabled on Android
- ✅ Command-line interface - Uses GUI only
- ✅ Serial port communication - Not needed on mobile

## 📦 Dependencies

### External Libraries

- **SDL2** (2.0.20+): Audio, video, input, threading
- **Android NDK** (r21+): C++ compilation, JNI, native APIs
- **Gradle** (7.0+): Build automation

### No Additional Libraries Needed

- Emulator core already supports Linux/POSIX
- File I/O adapted for Android storage access
- Threading via SDL2

## 🎯 Next Steps

1. **Compile** the Android app using provided CMakeLists.txt
2. **Test ROM loading** from `/sdcard/wqx/` directory
3. **Verify display rendering** and audio playback
4. **Test keyboard input** on physical device
5. **Optimize performance** based on profiling results

## 📞 Troubleshooting

### Common Issues

**Issue**: Permission denied on ROM loading  
**Solution**: Ensure `/sdcard/wqx/` directory exists with proper permissions

**Issue**: App crashes on startup  
**Solution**: Check logcat logs: `adb logcat | grep NC2000`

**Issue**: Black screen / no display  
**Solution**: Verify SDL2 initialization in `android_io.cpp`

**Issue**: No sound  
**Solution**: Check audio device permissions and SDL audio initialization

## 📄 License

GNU General Public License v3.0 (GPL-3.0)

---

**Last Updated**: June 1, 2026  
**Status**: Code porting complete, ready for compilation testing
