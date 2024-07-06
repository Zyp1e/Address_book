package com.example.myaddressbook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

public class LetterNavigationView extends View {
    private List<String> letters;
    private final Paint paint;
    private final int textHeight;
    private final int textPadding;
    private OnLetterSelectedListener listener;

    public interface OnLetterSelectedListener {
        void onLetterSelected(String letter);
    }

    public LetterNavigationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(context.getResources().getColor(android.R.color.black));
        paint.setTextSize(38);
        paint.setAntiAlias(true);
        textHeight = (int) (context.getResources().getDisplayMetrics().density * 20);
        textPadding = (int) (context.getResources().getDisplayMetrics().density * 6);
    }

    public void setLetters(List<String> letters) {
        this.letters = letters;
        invalidate();  // 重新绘制
    }

    public void setOnLetterSelectedListener(OnLetterSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (letters == null || letters.isEmpty()) return;

        int viewHeight = getHeight();
        int lettersHeight = letters.size() * (textHeight + textPadding);
        int top = (viewHeight - lettersHeight) / 2;

        for (int i = 0; i < letters.size(); i++) {
            String letter = letters.get(i);
            float x = getWidth() - textPadding - paint.measureText(letter);  // 调整字母位置，使其靠右
            float y = top + (i + 1) * (textHeight + textPadding);
            canvas.drawText(letter, x, y, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (letters == null || letters.isEmpty()) return false;

        int viewHeight = getHeight();
        int lettersHeight = letters.size() * (textHeight + textPadding);
        int top = (viewHeight - lettersHeight) / 2;
        float y = event.getY();
        if (y < top || y > top + lettersHeight) {
            return false;
        }

        int letterIndex = (int) ((y - top) / (textHeight + textPadding));
        if (letterIndex >= 0 && letterIndex < letters.size()) {
            String letter = letters.get(letterIndex);
            if (listener != null) {
                listener.onLetterSelected(letter);
            }
            return true;  // 返回 true 以表示事件已被处理
        }
        return false;
    }
}
