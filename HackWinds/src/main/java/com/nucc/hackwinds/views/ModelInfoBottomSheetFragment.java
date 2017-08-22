package com.nucc.hackwinds.views;

import android.app.Dialog;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.TextView;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.ForecastModel;

import org.w3c.dom.Text;


public class ModelInfoBottomSheetFragment extends BottomSheetDialogFragment {

    private TextView mWaveModelInfoTextView;
    private TextView mWindModelInfoTextView;
    private TextView mUpdateTimeTextView;

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
        View contentView = View.inflate(getContext(), R.layout.model_info_bottom_sheet_fragment, null);
        dialog.setContentView(contentView);

        mWaveModelInfoTextView = (TextView) contentView.findViewById(R.id.model_info_wave);
        mWindModelInfoTextView = (TextView) contentView.findViewById(R.id.model_info_wind);
        mUpdateTimeTextView = (TextView) contentView.findViewById(R.id.model_info_update_time);

        updateModelInfo();

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    public void updateModelInfo() {
        ForecastModel forecastModel = ForecastModel.getInstance(getActivity());

        mWaveModelInfoTextView.setText("Wave Model: " + forecastModel.waveModelName);
        mWindModelInfoTextView.setText("Wind Model: " + forecastModel.windModelName);
        mUpdateTimeTextView.setText("Updated: " + forecastModel.waveModelRun);
    }

}