package com.example.myaddressbook;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

public class GroupItemDecoration extends RecyclerView.ItemDecoration {
    private final Paint backgroundPaint;
    private final Paint textPaint;
    private final int headerHeight;
    private Map<Integer, String> groupHeaders = new HashMap<>();

    public GroupItemDecoration() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xFFE0E0E0); // Light gray color for background

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF000000); // Black color for text
        textPaint.setTextSize(50);

        headerHeight = 100; // Assuming header height is 100 pixels
    }

    public void setGroupHeaders(Map<Integer, String> headers) {
        this.groupHeaders = new HashMap<>(headers);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (groupHeaders.containsKey(position)) {
                String headerTitle = groupHeaders.get(position);
                float top = child.getTop() - headerHeight;
                float bottom = child.getTop();
                // Draw the background for the header
                canvas.drawRect(left, top, right, bottom, backgroundPaint);
                // Draw the text for the header
                canvas.drawText(headerTitle, left + 20, bottom - 30, textPaint);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (groupHeaders.containsKey(position)) {
            outRect.set(0, headerHeight, 0, 0);
        } else {
            outRect.set(0, 0, 0, 0);
        }
    }
}
