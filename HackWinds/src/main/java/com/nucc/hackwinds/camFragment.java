package com.nucc.hackwinds;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.koushikdutta.ion.Ion;


public class camFragment extends Fragment {
    ImageView img;
    int cacheDuration = 3000;
    static String urlBase = "http://www.warmwinds.com/wp-content/uploads/surf-cam-stills/image0000";
    static String urlExt = ".jpg";

    private SwipeRefreshLayout mSwipeRefreshLayout;

    int[] resIds = {
            R.id.imageView1,
            R.id.imageView2,
            R.id.imageView3,
            R.id.imageView4,
            R.id.imageView5,
            R.id.imageView6,
            R.id.imageView7,
            R.id.imageView8,
            R.id.imageView9
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.cam_fragment, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) V.findViewById(R.id.swipe_layout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                for (int i=0; i<resIds.length; i++) {
                img = (ImageView) getActivity().findViewById(resIds[i]);
                Ion.with(getActivity()).load(urlBase + Integer.toString(i+1) + urlExt).intoImageView(img);
                img.getLayoutParams().width = LayoutParams.MATCH_PARENT;
                img.setScaleType(ScaleType.FIT_XY);
                img.setAdjustViewBounds(true);
                }
                // Set the refresh state to false
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        // Set the color scheme of the SwipeRefreshLayout by providing 4 color resource ids
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.jblue, R.color.swipe_color_4,
                R.color.swipe_color_2, R.color.swipe_color_3);

        loadImages(V);
        return V;
    }

    // Function to update the images on call
    public void loadImages(View rootView) {
        for (int i=0; i<resIds.length; i++) {
            img = (ImageView) rootView.findViewById(resIds[i]);
            Ion.with(getActivity()).load(urlBase + Integer.toString(i+1) + urlExt).intoImageView(img);
            img.getLayoutParams().width = LayoutParams.MATCH_PARENT;
            img.setScaleType(ScaleType.FIT_XY);
            img.setAdjustViewBounds(true);
        }
    }
}
