package com.alchemist.syncasts.ui.views;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.utils.ViewUtils;

public class ColorPreference extends Preference implements ColorDialog.OnColorSelectedListener {

    private int[] mColorChoices;
    private int mColorIndex;
    private int mNumColumns = 5;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ColorPreference, 0, 0);

        try {
            mNumColumns = a.getInteger(R.styleable.ColorPreference_numColumns, mNumColumns);
            int choicesResId = a.getResourceId(R.styleable.ColorPreference_colorChoices, 0);
            mColorChoices = getContext().getResources().getIntArray(choicesResId);
        } finally {
            a.recycle();
        }

        setWidgetLayoutResource(R.layout.pref_color_layout);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ImageView previewView = (ImageView) view.findViewById(R.id.color_view);
        ViewUtils.setColorViewValue(previewView, mColorChoices[mColorIndex], false);
    }

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();
        Activity activity = resolveContext(getContext());
        ColorDialog fragment = (ColorDialog) activity.getFragmentManager().findFragmentByTag(getFragmentTag());
        if (fragment != null) {
            fragment.setOnColorSelectedListener(this);
        }
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(Integer.parseInt(value));
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        int defaultIntReturnValue;

        if (defaultReturnValue == null) {
            defaultIntReturnValue = 0;
        } else {
            defaultIntReturnValue = Integer.parseInt(defaultReturnValue);
        }

        return Integer.toString(getPersistedInt(defaultIntReturnValue));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(0) : (Integer) defaultValue);
    }

    @Override
    protected void onClick() {
        super.onClick();
        ColorDialog fragment = ColorDialog.newInstance(mNumColumns, mColorChoices, mColorChoices[getColorIndex()]);
        fragment.setOnColorSelectedListener(this);

        resolveContext(getContext()).getFragmentManager().beginTransaction()
                .add(fragment, getFragmentTag())
                .commit();
    }

    @Override
    public void onColorSelected(int newColorIndex, String tag) {
        setValue(newColorIndex);
    }

    public int getColorIndex() {
        return mColorIndex;
    }

    public void setValue(int value) {
        if (callChangeListener(value)) {
            mColorIndex = value;
            persistInt(value);
            notifyChanged();
        }
    }

    private String getFragmentTag() {
        return "color_" + getKey();
    }

    private Activity resolveContext(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return resolveContext(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }
}
