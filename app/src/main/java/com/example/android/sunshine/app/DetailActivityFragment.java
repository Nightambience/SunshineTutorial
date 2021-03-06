package com.example.android.sunshine.app;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private  String mForecastStr;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        String mForecastStr = this.getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
        TextView textView = (TextView) rootView.findViewById(R.id.detail_textView);
        textView.setText(mForecastStr);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem  = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider =
                //(ShareActionProvider)  menuItem.getActionProvider();
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mShareActionProvider != null)
        {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
        else
        {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent()
    {
        /*Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr+FORECAST_SHARE_HASHTAG);
        return shareIntent;
*/

        int zipCode = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_location_key),
                        getString(R.string.pref_location_default)));

        String uri = "geo:0,0?q="+zipCode;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        return intent;
    }
}
