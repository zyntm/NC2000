package com.example.nc2000emulator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class EmulatorView extends SurfaceView {
    private RenderThread renderThread;

    public EmulatorView(Context context) {
        super(context);
        init();
    }

    public EmulatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setKeepScreenOn(true);
        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                nativeInitDisplay(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                nativeOnDisplayChanged(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                nativeCleanupDisplay();
            }
        });
    }

    public void initializeEmulator() {
        renderThread = new RenderThread();
        renderThread.start();
    }

    public void pauseEmulator() {
        if (renderThread != null) {
            renderThread.pause();
        }
    }

    public void resumeEmulator() {
        if (renderThread != null) {
            renderThread.resume();
        }
    }

    public void stopEmulator() {
        if (renderThread != null) {
            renderThread.stopRunning();
        }
    }

    private native void nativeInitDisplay(Object surface);
    private native void nativeOnDisplayChanged(int width, int height);
    private native void nativeCleanupDisplay();

    private class RenderThread extends Thread {
        private boolean running = true;
        private boolean paused = false;

        @Override
        public void run() {
            nativeRun();
        }

        public void pause() {
            paused = true;
        }

        public void resume() {
            paused = false;
        }

        public void stopRunning() {
            running = false;
        }

        private native void nativeRun();
    }
}
