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


public class CamFragment extends Fragment {
    // Initialize variables
    ImageView img;

    // Constant cache duration
    final int cacheDuration = 3000;

    // This is the base URL for the static images, they are all jpegs
    final String urlBase = "http://www.warmwinds.com/wp-content/uploads/surf-cam-stills/image0000";
    final String urlExt = ".jpg";

    // Initialize the swipe refresh layout
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // An array holding the imageviews so they can be kept in a scrollview
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

        // Set the swipe to refresh listener
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // When the refresh is triggered, get the images and set them to the imageviews
                for (int i = 0; i < resIds.length; i++) {
                    // Get the imageview
                    img = (ImageView) getActivity().findViewById(resIds[i]);

                    // Get the image and scale it to fill the whole screen width
                    Ion.with(getActivity()).load(urlBase + Integer.toString(i + 1) + urlExt).intoImageView(img);
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

        // Initialize the view by forcing an image load
        loadImages(V);
        return V;
    }

    // Function to update the images on call
    public void loadImages(View rootView) {
        for (int i = 0; i < resIds.length; i++) {
            // Get the image view
            img = (ImageView) rootView.findViewById(resIds[i]);

            // Get the images and scale them to fit the width of the screen
            Ion.with(getActivity()).load(urlBase + Integer.toString(i + 1) + urlExt).intoImageView(img);
            img.getLayoutParams().width = LayoutParams.MATCH_PARENT;
            img.setScaleType(ScaleType.FIT_XY);
            img.setAdjustViewBounds(true);
        }
    }
}
