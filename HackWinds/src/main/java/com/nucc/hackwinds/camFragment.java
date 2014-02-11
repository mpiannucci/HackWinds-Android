package com.nucc.hackwinds;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.nucc.hackwinds.R;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class camFragment extends Fragment implements OnRefreshListener {
	ImageView img;
	int cacheDuration = 3000;
	static String urlBase = "http://www.warmwinds.com/wp-content/uploads/surf-cam-stills/image0000";
	static String urlExt = ".jpg";
	private PullToRefreshLayout mPullToRefreshLayout;

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
		mPullToRefreshLayout = (PullToRefreshLayout) V.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

		loadImages(V);
		return V;
	}

    @Override
	public
	void onRefreshStarted(View view) {
    	for (int i=0; i<resIds.length; i++) {
    		img = (ImageView) getActivity().findViewById(resIds[i]);
    		UrlImageViewHelper.setUrlDrawable(img, urlBase + Integer.toString(i) + urlExt, null, cacheDuration);
    		img.getLayoutParams().width = LayoutParams.MATCH_PARENT;
    		img.setScaleType(ScaleType.FIT_XY);
    		img.setAdjustViewBounds(true);
    	}
    	mPullToRefreshLayout.setRefreshComplete();
    }

    // Function to update the images on call
    public void loadImages(View rootView) {
    	for (int i=0; i<resIds.length; i++) {
    		img = (ImageView) rootView.findViewById(resIds[i]);
    		UrlImageViewHelper.setUrlDrawable(img, urlBase + Integer.toString(i) + urlExt, null, cacheDuration);
    		img.getLayoutParams().width = LayoutParams.MATCH_PARENT;
    		img.setScaleType(ScaleType.FIT_XY);
    		img.setAdjustViewBounds(true);
    	}
    }
}
