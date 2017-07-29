package com.alchemist.syncasts.ui.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ColorDialog extends DialogFragment {

    private static final String NUM_COLUMNS_KEY = "num_columns";
    private static final String COLOR_CHOICES_KEY = "color_choices";
    private static final String SELECTED_COLOR_KEY = "selected_color";

    @BindView(R.id.color_grid) GridLayout colorGrid;

    private int[] mColorChoices;
    private int mSelectedColorValue;
    private int mNumColumns;
    private OnColorSelectedListener mColorSelectedListener;

    public ColorDialog() {
    }

    public static ColorDialog newInstance(int numColumns, int[] colorChoices, int selectedColorValue) {
        Bundle args = new Bundle();
        args.putInt(NUM_COLUMNS_KEY, numColumns);
        args.putIntArray(COLOR_CHOICES_KEY, colorChoices);
        args.putInt(SELECTED_COLOR_KEY, selectedColorValue);

        ColorDialog dialog = new ColorDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mNumColumns = args.getInt(NUM_COLUMNS_KEY);
        mColorChoices = args.getIntArray(COLOR_CHOICES_KEY);
        mSelectedColorValue = args.getInt(SELECTED_COLOR_KEY);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnColorSelectedListener) {
            setOnColorSelectedListener((OnColorSelectedListener) context);
        } else {
            repopulateItems();
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View rootView = layoutInflater.inflate(R.layout.dialog_colors, null);
        ButterKnife.bind(this, rootView);

        colorGrid.setColumnCount(mNumColumns);
        repopulateItems();

        return new AlertDialog.Builder(getActivity())
                .setView(rootView)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        sizeDialog();
    }


    private void sizeDialog() {
        if (mColorSelectedListener == null || colorGrid == null) {
            return;
        }

        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }

        final Resources res = colorGrid.getContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        colorGrid.measure(
                View.MeasureSpec.makeMeasureSpec(dm.widthPixels, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(dm.heightPixels, View.MeasureSpec.AT_MOST));
        int width = colorGrid.getMeasuredWidth();
        int height = colorGrid.getMeasuredHeight();

        int extraPadding = res.getDimensionPixelSize(R.dimen.color_grid_extra_padding);

        width += extraPadding;
        height += extraPadding;

        dialog.getWindow().setLayout(width, height);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener colorSelectedListener) {
        this.mColorSelectedListener = colorSelectedListener;
        repopulateItems();
    }

    private void repopulateItems() {
        if (mColorSelectedListener == null || colorGrid == null) {
            return;
        }

        Context context = colorGrid.getContext();
        colorGrid.removeAllViews();

        for (int i = 0; i < mColorChoices.length; i++) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.grid_item_color, colorGrid, false);

            ViewUtils.setColorViewValue((ImageView) itemView.findViewById(R.id.color_view),
                    mColorChoices[i], mColorChoices[i] == mSelectedColorValue);

            itemView.setClickable(true);
            itemView.setFocusable(true);
            final int colorIndex = i;
            itemView.setOnClickListener(view -> {
                if (mColorSelectedListener != null) {
                    mColorSelectedListener.onColorSelected(colorIndex, getTag());
                }
                dismiss();
            });

            colorGrid.addView(itemView);
        }

        sizeDialog();
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int newColorIndex, String tag);
    }
}
