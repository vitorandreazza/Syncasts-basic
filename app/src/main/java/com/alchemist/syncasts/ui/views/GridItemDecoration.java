package com.alchemist.syncasts.ui.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridItemDecoration extends RecyclerView.ItemDecoration {

    private int mHalfSpace;

    public GridItemDecoration(Context context, int space) {
        this.mHalfSpace = (int) (context.getResources().getDimension(space) / 2);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {

        if (parent.getPaddingLeft() != mHalfSpace) {
            parent.setPadding(mHalfSpace, mHalfSpace, mHalfSpace, mHalfSpace);
            parent.setClipToPadding(false);
        }

        outRect.left = mHalfSpace;
        outRect.right = mHalfSpace;
    }
}