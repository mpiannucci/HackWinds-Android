package com.nucc.hackwinds.views;

import android.app.Dialog;
import android.support.annotation.IdRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.RadioGroup;

import com.nucc.hackwinds.R;


public class BuoyPickerBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(View bottomSheet, int newState) {
            switch (newState) {

                case BottomSheetBehavior.STATE_COLLAPSED:{
                }
                case BottomSheetBehavior.STATE_SETTLING:{
                }
                case BottomSheetBehavior.STATE_EXPANDED:{
                }
                case BottomSheetBehavior.STATE_HIDDEN: {
                }
                case BottomSheetBehavior.STATE_DRAGGING: {
                }
            }

        }

        @Override
        public void onSlide(View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.buoy_picker_bottom_sheet_fragment, null);
        dialog.setContentView(contentView);

        RadioGroup buoyLocationRadioGroup = (RadioGroup) contentView.findViewById(R.id.buoy_location_radio_group);
        buoyLocationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                // TODO
            }
        });

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }
}
