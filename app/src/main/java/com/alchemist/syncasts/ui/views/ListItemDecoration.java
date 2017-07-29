package com.alchemist.syncasts.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alchemist.syncasts.R;

/**
 * An {@link android.support.v7.widget.RecyclerView.ItemDecoration} that draws horizontal dividers
 * between entries in a {@link RecyclerView}
 */
public class ListItemDecoration extends RecyclerView.ItemDecoration {

    private static int measuredDividerHeight = 1; //1px
    private Context mContext;
    private Paint mPaint;
    private boolean mAddStandardMargin;

    public ListItemDecoration(Context context, boolean addStandardMargin) {
        mContext = context;
        mAddStandardMargin = addStandardMargin;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = (int) (parent.getPaddingLeft()
                + mContext.getResources().getDimension(R.dimen.content_standard_margin));
        if (mAddStandardMargin) {
            left += mContext.getResources().getDimension(R.dimen.standard_margin);
        }
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int endIndex = parent.getChildCount();
        for (int i = 0; i < endIndex; i++) {
            final View child = parent.getChildAt(i);

            final RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin
                    + (int) child.getTranslationY();
            final int bottom = top + measuredDividerHeight;

            mPaint.setColor(mContext.getResources().getColor(R.color.divider));
            c.drawRect(left, top, right, bottom, mPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.bottom = measuredDividerHeight;
    }
}
