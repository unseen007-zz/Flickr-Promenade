/**
 *  Copyright 2014 Steven Perraudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.unseen.flickr.promenade;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.unseen.flickr.promenade.loader.FlickPhotoLoader;
import com.unseen.flickr.promenade.network.HttpManager;
import com.unseen.flickr.promenade.ui.dialog.DialogFragmentBuilder;
import com.unseen.flickr.promenade.utils.ConnectivityUtils;

import retrofit.RestAdapter;

public class MainActivity extends Activity {

    private final static int RQS_GMS = 3993;

    private final static float LAT_EXAMPLE = -37.8140000f;
    private final static float LON_EXAMPLE = 144.9633200f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ConnectivityUtils.isConnected(this)){
            // Init data
            initData();
        }
        else{
            showErrorDialog(getString(R.string.information), getString(R.string.error_no_internet));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        checkGooglePayServices();
    }

    private void initData(){
        Bundle bundle = new Bundle();
        bundle.putFloat(FlickPhotoLoader.PARAM_LATITUDE, LAT_EXAMPLE);
        bundle.putFloat(FlickPhotoLoader.PARAM_LONGITUDE, LON_EXAMPLE);

        getLoaderManager().restartLoader(0, bundle, new LoaderManager.LoaderCallbacks<HttpManager.PhotoResponse>() {
            @Override
            public Loader<HttpManager.PhotoResponse> onCreateLoader(int id, Bundle b) {
                return new FlickPhotoLoader(MainActivity.this, b);
            }

            @Override
            public void onLoadFinished(Loader<HttpManager.PhotoResponse> loader, HttpManager.PhotoResponse data) {
                System.out.println("======onLoadFinished");
                if(data == null){
                    System.out.println("======error loading photos");
                }
                else{
                    System.out.println("======data : " + data.photos.photo.size());
                }
            }

            @Override
            public void onLoaderReset(Loader<HttpManager.PhotoResponse> loader) {

            }
        });
    }

    private void showErrorDialog(String title, String message){
        System.out.println("===========showErrorDialog");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        final DialogFragmentBuilder newFragment = DialogFragmentBuilder.newInstance();
        newFragment.setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(android.R.string.ok));
                //.setNegativeButton(getString(android.R.string.cancel));
        newFragment.show(ft, "dialog");
    }

    private void checkGooglePayServices(){
        // Check status of Google Play Services
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // Check Google Play Service Available
        try {
            if (status != ConnectionResult.SUCCESS) {
                GooglePlayServicesUtil.getErrorDialog(status, this, RQS_GMS).show();
            }
        } catch (Exception e) {
            Log.e("Error: GooglePlayServiceUtil: ", "" + e);
        }
    }


}
