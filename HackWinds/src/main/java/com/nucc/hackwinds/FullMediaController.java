package com.nucc.hackwinds;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;

/**
 * FullMediaController - Custom Media Controller that adds a full screen button that for some weird
 * reason isn't included in the Android SDK...
 * Created by miannucci on 2/18/2015.
 */
public class FullMediaController extends MediaController {

    ImageButton mFullScreenButton;
    Context mContext;

    public FullMediaController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        frameParams.gravity = Gravity.RIGHT|Gravity.TOP;
        frameParams.topMargin = 23;

        View v = makeFullScreenButtonView();
        addView(v, frameParams);

    }

    private View makeFullScreenButtonView() {
        mFullScreenButton = new ImageButton(mContext);
        mFullScreenButton.setImageResource(R.drawable.ic_media_fullscreen_stretch);
        mFullScreenButton.setBackgroundColor(Color.TRANSPARENT);

        mFullScreenButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Log.d("hackwinds", "Full Screen Wave Cam Requested");
            }
        });

        return mFullScreenButton;
    }
}
