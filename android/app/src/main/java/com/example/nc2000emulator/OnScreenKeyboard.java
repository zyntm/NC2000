package com.example.nc2000emulator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.HashMap;
import java.util.Map;

public class OnScreenKeyboard extends View {
    private static final int ROWS = 5;
    private static final int COLS = 7;
    private static final int PADDING = 10;
    private static final int KEY_CORNER_RADIUS = 8;

    // NC1020 真实键盘布局
    private static final String[][] KEY_LAYOUT = {
            {"ON", "学", "习", "查", "看", "★", "DEL"},
            {"Aa", "1", "2", "3", "4", "5", "6"},
            {"Bb", "7", "8", "9", "0", ".", "?"},
            {"Cc", "、", "；", "'", "，", "\"", "!"},
            {"ENT", "空格", "◄", "▲", "▼", "►", "OFF"}
    };

    private Map<String, Integer> keyToSDLKey = new HashMap<>();
    private Map<String, Rect> keyRects = new HashMap<>();
    private Map<String, Boolean> keyPressed = new HashMap<>();
    private Paint keyPaint;
    private Paint textPaint;
    private Paint pressedPaint;
    private Paint borderPaint;

    public OnScreenKeyboard(Context context) {
        super(context);
        init();
    }

    public OnScreenKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OnScreenKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initializePaints();
        initializeKeyMapping();
        setWillNotDraw(false);
    }

    private void initializePaints() {
        keyPaint = new Paint();
        keyPaint.setColor(Color.parseColor("#E0E0E0"));
        keyPaint.setStyle(Paint.Style.FILL);
        keyPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(16);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        pressedPaint = new Paint();
        pressedPaint.setColor(Color.parseColor("#909090"));
        pressedPaint.setStyle(Paint.Style.FILL);
        pressedPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#808080"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        borderPaint.setAntiAlias(true);
    }

    private void initializeKeyMapping() {
        // Map key labels to SDL keycodes
        keyToSDLKey.put("1", 0x31); // '1'
        keyToSDLKey.put("2", 0x32); // '2'
        keyToSDLKey.put("3", 0x33); // '3'
        keyToSDLKey.put("4", 0x34); // '4'
        keyToSDLKey.put("5", 0x35); // '5'
        keyToSDLKey.put("6", 0x36); // '6'
        keyToSDLKey.put("7", 0x37); // '7'
        keyToSDLKey.put("8", 0x38); // '8'
        keyToSDLKey.put("9", 0x39); // '9'
        keyToSDLKey.put("0", 0x30); // '0'
        keyToSDLKey.put(".", 0x2E); // '.'
        keyToSDLKey.put("?", 0x3F); // '?'
        keyToSDLKey.put("!", 0x21); // '!'
        keyToSDLKey.put("ON", 0x1073); // Custom key code for ON
        keyToSDLKey.put("OFF", 0x1074); // Custom key code for OFF
        keyToSDLKey.put("DEL", 0x7F); // Delete/Backspace
        keyToSDLKey.put("ENT", 0x0D); // Enter
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int keyWidth = (width - PADDING * 2 - PADDING * (COLS - 1)) / COLS;
        int keyHeight = (height - PADDING * 2 - PADDING * (ROWS - 1)) / ROWS;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = PADDING + col * (keyWidth + PADDING);
                int y = PADDING + row * (keyHeight + PADDING);
                String key = KEY_LAYOUT[row][col];
                Rect rect = new Rect(x, y, x + keyWidth, y + keyHeight);
                keyRects.put(key, rect);

                Paint fillPaint = (keyPressed.getOrDefault(key, false)) ? pressedPaint : keyPaint;
                
                RectF rectF = new RectF(x, y, x + keyWidth, y + keyHeight);
                canvas.drawRoundRect(rectF, KEY_CORNER_RADIUS, KEY_CORNER_RADIUS, fillPaint);
                canvas.drawRoundRect(rectF, KEY_CORNER_RADIUS, KEY_CORNER_RADIUS, borderPaint);

                // Draw text
                float textX = x + keyWidth / 2f;
                float textY = y + keyHeight / 2f + textPaint.getTextSize() / 3;
                canvas.drawText(key, textX, textY, textPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        String key = getKeyAtPosition(x, y);
        if (key != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    keyPressed.put(key, true);
                    onKeyDown(key);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    keyPressed.put(key, false);
                    onKeyUp(key);
                    invalidate();
                    break;
            }
        }
        return true;
    }

    private String getKeyAtPosition(float x, float y) {
        for (Map.Entry<String, Rect> entry : keyRects.entrySet()) {
            if (entry.getValue().contains((int) x, (int) y)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void onKeyDown(String key) {
        Integer sdlKey = keyToSDLKey.get(key);
        if (sdlKey != null) {
            nativeOnKeyDown(sdlKey);
        }
    }

    private void onKeyUp(String key) {
        Integer sdlKey = keyToSDLKey.get(key);
        if (sdlKey != null) {
            nativeOnKeyUp(sdlKey);
        }
    }

    // JNI methods to communicate with C++ code
    private native void nativeOnKeyDown(int keyCode);
    private native void nativeOnKeyUp(int keyCode);
}
