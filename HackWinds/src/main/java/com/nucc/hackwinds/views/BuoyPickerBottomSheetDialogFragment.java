package com.nucc.hackwinds.views;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.RadioGroup;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.BuoyModel;


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
        buoyLocationRadioGroup.check(getBuoyLocation());
        buoyLocationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                setBuoyLocation(i);
                dismiss();
            }
        });

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    private int getBuoyLocation() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( getActivity().getApplicationContext() );
        String buoyLocation = sharedPrefs.getString( SettingsActivity.BUOY_LOCATION_KEY, BuoyModel.BLOCK_ISLAND_LOCATION );
        if (buoyLocation.equals(BuoyModel.BLOCK_ISLAND_LOCATION)) {
            return R.id.block_island_radio;
        } else if (buoyLocation.equals(BuoyModel.MONTAUK_LOCATION)) {
            return R.id.montauk_radio;
        } else if (buoyLocation.equals(BuoyModel.NANTUCKET_LOCATION)) {
            return R.id.nantucket_radio;
        } else {
            return R.id.texas_tower_radio;
        }
    }

    private void setBuoyLocation(int newLocationId) {
        String location = BuoyModel.BLOCK_ISLAND_LOCATION;

        switch(newLocationId) {
            case R.id.block_island_radio:
                location = BuoyModel.BLOCK_ISLAND_LOCATION;
                break;
            case R.id.montauk_radio:
                location = BuoyModel.MONTAUK_LOCATION;
                break;
            case R.id.nantucket_radio:
                location = BuoyModel.NANTUCKET_LOCATION;
                break;
            case R.id.texas_tower_radio:
                location = BuoyModel.TEXAS_TOWER_LOCATION;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( getActivity().getApplicationContext() );
        sharedPrefs.edit().putString ( SettingsActivity.BUOY_LOCATION_KEY, location ).apply();
    }
}
