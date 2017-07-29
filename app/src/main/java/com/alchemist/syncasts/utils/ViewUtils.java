package com.alchemist.syncasts.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.alchemist.syncasts.R;

public final class ViewUtils {

    public static float pxToDp(float px) {
        float densityDpi = Resources.getSystem().getDisplayMetrics().densityDpi;
        return px / (densityDpi / 160f);
    }

    public static int dpToPx(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static int screenWidthInPx(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setColorViewValue(ImageView imageView, int color, boolean selected) {
        Resources res = imageView.getContext().getResources();

        Drawable currentDrawable = imageView.getDrawable();
        GradientDrawable colorChoiceDrawable;
        if (currentDrawable instanceof GradientDrawable) {
            colorChoiceDrawable = (GradientDrawable) currentDrawable;
        } else {
            colorChoiceDrawable = new GradientDrawable();
            colorChoiceDrawable.setShape(GradientDrawable.OVAL);
        }

        // Set stroke to dark version of color
        int darkenedColor = Color.rgb(
                Color.red(color) * 192 / 256,
                Color.green(color) * 192 / 256,
                Color.blue(color) * 192 / 256);

        colorChoiceDrawable.setColor(color);
        colorChoiceDrawable.setStroke((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, res.getDisplayMetrics()), darkenedColor);

        Drawable drawable = colorChoiceDrawable;
        if (selected) {
            BitmapDrawable checkmark = (BitmapDrawable) res.getDrawable(R.drawable.ic_checkmark_white);
            checkmark.setGravity(Gravity.CENTER);
            drawable = new LayerDrawable(new Drawable[]{colorChoiceDrawable, checkmark});
        }

        imageView.setImageDrawable(drawable);
    }

    public static Drawable tintDrawable(Context context, Drawable drawable, @ColorRes int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(color));
        return wrapDrawable;
    }

    public static int getAttribute(Context context, int resid) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(resid, value, true);
        return value.data;
    }

    public static void setTextViewDrawableColor(Context context, TextView textView, @ColorRes int color) {
        setTextViewDrawableColor(textView, context.getResources().getColor(color));
    }

    public static void setTextViewDrawableColor(TextView textView, @ColorInt int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }
}
