package com.example.myaddressbook;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;

public class AlphabetItemDecoration extends RecyclerView.ItemDecoration {
    private final Context context;
    private final Paint paint;
    private final int sectionHeaderHeight;
    private Map<Integer, String> sectionHeaders;

    public AlphabetItemDecoration(Context context) {
        this.context = context;
        paint = new Paint();
        paint.setColor(context.getResources().getColor(android.R.color.black));
        paint.setTextSize(40);
        paint.setAntiAlias(true);
        sectionHeaderHeight = (int) (context.getResources().getDisplayMetrics().density * 30); // 固定高度
    }

    public void setSectionHeaders(Map<Integer, String> sectionHeaders) {
        this.sectionHeaders = sectionHeaders;
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (sectionHeaders == null || sectionHeaders.isEmpty()) return;

        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (sectionHeaders.containsKey(position)) {
                String title = sectionHeaders.get(position);
                float titleHeight = paint.descent() - paint.ascent();
                float titleBaseline = Math.max(sectionHeaderHeight, child.getTop() - (titleHeight + sectionHeaderHeight));

                canvas.drawRect(left, child.getTop() - sectionHeaderHeight, right, child.getTop(), paint);
                canvas.drawText(title, left + 16, titleBaseline, paint);
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (sectionHeaders != null && sectionHeaders.containsKey(position)) {
            outRect.top = sectionHeaderHeight;
        }
    }
}