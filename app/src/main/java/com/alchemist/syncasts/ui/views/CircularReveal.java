package com.alchemist.syncasts.ui.views;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.utils.AnimUtils;

/**
 * A transition which shows/hides a view with a circular clipping mask. Callers should provide the
 * mCenter point of the reveal either {@link #setCenter(Point) directly} or by
 * {@link #centerOn(View) specifying} another view to mCenter on; otherwise the target {@code view}
 * pivot point will be used.
 */
public class CircularReveal extends Visibility {

    private Point mCenter;
    private float mStartRadius;
    private float mEndRadius;
    @IdRes private int mCenterOnId = View.NO_ID;
    private View mCenterOn;

    public CircularReveal() {
        super();
    }

    public CircularReveal(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularReveal);
        mStartRadius = a.getDimension(R.styleable.CircularReveal_startRadius, 0f);
        mEndRadius = a.getDimension(R.styleable.CircularReveal_endRadius, 0f);
        mCenterOnId = a.getResourceId(R.styleable.CircularReveal_centerOn, View.NO_ID);
        a.recycle();
    }

    /**
     * The mCenter point of the reveal or conceal, relative to the target {@code view}.
     */
    public void setCenter(@NonNull Point center) {
        this.mCenter = center;
    }

    /**
     * Center the reveal or conceal on this view.
     */
    public void centerOn(@NonNull View source) {
        mCenterOn = source;
    }

    /**
     * Sets the radius that <strong>reveals</strong> start from.
     */
    public void setStartRadius(float startRadius) {
        this.mStartRadius = startRadius;
    }

    /**
     * Sets the radius that <strong>conceals</strong> end at.
     */
    public void setEndRadius(float endRadius) {
        this.mEndRadius = endRadius;
    }

    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view,
                             TransitionValues startValues,
                             TransitionValues endValues) {
        if (view == null || view.getHeight() == 0 || view.getWidth() == 0) return null;
        ensureCenterPoint(sceneRoot, view);
        return new AnimUtils.NoPauseAnimator(ViewAnimationUtils.createCircularReveal(
                view,
                mCenter.x,
                mCenter.y,
                mStartRadius,
                getFullyRevealedRadius(view)));
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view,
                                TransitionValues startValues,
                                TransitionValues endValues) {
        if (view == null || view.getHeight() == 0 || view.getWidth() == 0) return null;
        ensureCenterPoint(sceneRoot, view);
        return new AnimUtils.NoPauseAnimator(ViewAnimationUtils.createCircularReveal(
                view,
                mCenter.x,
                mCenter.y,
                getFullyRevealedRadius(view),
                mEndRadius));
    }

    private void ensureCenterPoint(ViewGroup sceneRoot, View view) {
        if (mCenter != null) return;
        if (mCenterOn != null || mCenterOnId != View.NO_ID) {
            View source;
            if (mCenterOn != null) {
                source = mCenterOn;
            } else {
                source = sceneRoot.findViewById(mCenterOnId);
            }
            if (source != null) {
                // use window location to allow views in diff hierarchies
                int[] loc = new int[2];
                source.getLocationInWindow(loc);
                int srcX = loc[0] + (source.getWidth() / 2);
                int srcY = loc[1] + (source.getHeight() / 2);
                view.getLocationInWindow(loc);
                mCenter = new Point(srcX - loc[0], srcY - loc[1]);
            }
        }
        // else use the pivot point
        if (mCenter == null) {
            mCenter = new Point(Math.round(view.getPivotX()), Math.round(view.getPivotY()));
        }
    }

    private float getFullyRevealedRadius(@NonNull View view) {
        return (float) Math.hypot(
                Math.max(mCenter.x, view.getWidth() - mCenter.x),
                Math.max(mCenter.y, view.getHeight() - mCenter.y));
    }
}
