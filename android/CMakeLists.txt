cmake_minimum_required(VERSION 3.15)
project(NC2000Emulator)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -g3 -pthread -Wno-deprecated-declarations")

# SDL2 configuration for Android
set(SDL2_DIR "${ANDROID_ABI}")
find_package(SDL2 REQUIRED CONFIG)

include_directories(${CMAKE_CURRENT_SOURCE_DIR})
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../..)

# Core emulator source files
set(EMULATOR_SOURCES
    ../../main.cpp
    ../../nc2000.cpp
    ../../disassembler.cpp
    ../../cpu_loop.cpp
    ../../comm.cpp
    ../../mem.cpp
    ../../io.cpp
    ../../rom.cpp
    ../../nor.cpp
    ../../nand.cpp
    ../../ram.cpp
    ../../dsp/dsp.cpp
    ../../sound.cpp
    ../../cmd.cpp
    ../../ansi/w65c02cpu.cpp
    ../../compare/c6502.cpp
    ../../console.cpp
    ../../display.cpp
    ../../settings.cpp
    ../../iv_uart.cpp
    ../../NekoDriverIO.cpp
    ../../lcdstripe/lcdpainter.cpp
    android_main.cpp
    android_io.cpp
    android_permission_manager.cpp
)

# JNI bridge
add_library(nc2000-emulator SHARED
    ${EMULATOR_SOURCES}
)

target_link_libraries(nc2000-emulator
    SDL2::SDL2
    log
    android
)
