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

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;
import com.unseen.flickr.promenade.loader.FlickPhotoLoader;
import com.unseen.flickr.promenade.network.HttpManager;
import com.unseen.flickr.promenade.ui.dialog.DialogFragmentBuilder;
import com.unseen.flickr.promenade.ui.transformation.CircleMarkerTransformation;
import com.unseen.flickr.promenade.utils.ConnectivityUtils;
import com.unseen.flickr.promenade.utils.LocationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity implements LocationListener, View.OnClickListener {
    private final static String TAG = "MA";
    private final static int RQS_GMS = 3993;

    private GoogleMap mMap;

    private ViewPager mViewPager;
    private PhotoDetailsPagerAdapter mAdapter;
    private ProgressBar mProgressBar;

    private boolean mShouldReloadData = true;

    private HttpManager.Photo mCurrentPhoto = null;
    private List<HttpManager.Photo> mPhotoList;
    private HashMap<Marker, HttpManager.Photo> mMarkerPhoto = new HashMap<Marker, HttpManager.Photo>();

    private Polyline mPromenadePolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init view elements
        mViewPager = (ViewPager) findViewById(R.id.infowindow_container);
        mViewPager.setOnClickListener(this);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int i) {
                if(mCurrentPhoto != mPhotoList.get(i)){
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(
                            new LatLng( mPhotoList.get(i).latitude,  mPhotoList.get(i).longitude)));
                }
                mCurrentPhoto = mPhotoList.get(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

        ObjectAnimator slideOutAnimator = ObjectAnimator.ofFloat(mViewPager, "translationY", getResources().getDimension(R.dimen.infowindow_footer_height));
        slideOutAnimator.setDuration(1);
        slideOutAnimator.start();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if(ConnectivityUtils.isConnected(this)){
            // Init data
            initLocation();
        }
        else{
            showErrorDialog(getString(R.string.information), getString(R.string.error_no_internet));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkGooglePayServices();
        if (!checkReady()) {
            return;
        }
    }

    private void initData(double lat, double lon){
        Bundle bundle = new Bundle();
        bundle.putDouble(FlickPhotoLoader.PARAM_LATITUDE, lat);
        bundle.putDouble(FlickPhotoLoader.PARAM_LONGITUDE, lon);

        getLoaderManager().restartLoader(0, bundle, new LoaderManager.LoaderCallbacks<HttpManager.PhotoResponse>() {
            @Override
            public Loader<HttpManager.PhotoResponse> onCreateLoader(int id, Bundle b) {
                return new FlickPhotoLoader(MainActivity.this, b);
            }

            @Override
            public void onLoadFinished(Loader<HttpManager.PhotoResponse> loader, HttpManager.PhotoResponse data) {
                if(data == null){
                    Toast.makeText(MainActivity.this, R.string.error_loading_data, Toast.LENGTH_SHORT).show();
                }
                else{
                    if(checkReady()){
                        mMap.clear();
                        mMarkerPhoto.clear();
                        mPhotoList = data.photos.photo;
                        for(HttpManager.Photo photo : data.photos.photo){
                            Marker m = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(photo.latitude, photo.longitude))
                                    .title(photo.title)
                                    .snippet(photo.description._content));
                            mMarkerPhoto.put(m, photo);

                            new SquareImageMarkerDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, m, photo.getImageSqUrl());
                        }

                        mAdapter = new PhotoDetailsPagerAdapter(data.photos.photo);
                        mViewPager.setAdapter(mAdapter);
                    }
                }
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoaderReset(Loader<HttpManager.PhotoResponse> loader) {}
        });
    }

    private void showErrorDialog(String title, String message){
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
            else{
                setUpMapIfNeeded();
            }
        } catch (Exception e) {
            Log.e("Error: GooglePlayServiceUtil: ", "" + e);
        }
    }

    private boolean checkReady() {
        if (mMap == null) {
            return false;
        }
        return true;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        HttpManager.Photo p = mMarkerPhoto.get(marker);
                        toggleInfoWindow(p);
                        animateMarker(marker, 360);
                        return true;
                    }
                });

                Location lastLocation = LocationUtils.getBestLastKnownLocation(this);
                if(lastLocation != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
                }
            }
        }
    }

    /**
     * Just some simple rotation for fun
     * @param marker
     * @param endScale
     */
    static void animateMarker(Marker marker, float endScale) {
        ObjectAnimator.ofFloat(marker, "rotation", marker.getRotation() + endScale)
                .setDuration(200)
                .start();
    }


    private void initLocation(){
        String bestAvailableProvider = LocationUtils.getBestProvider(this);
        if (bestAvailableProvider == null || bestAvailableProvider.equals(LocationManager.PASSIVE_PROVIDER)) {
            showGpsSettingsDialog();
        }
        else{
            Location lastLocation = LocationUtils.getBestLastKnownLocation(this);
            if(lastLocation == null){
                LocationUtils.enableLocationUpdates(this, this);
            }
            else{
                onLocationChanged(lastLocation);
            }
        }
    }

    private void showGpsSettingsDialog(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        final DialogFragmentBuilder newFragment = DialogFragmentBuilder.newInstance();
        newFragment.setTitle(getString(R.string.information))
                .setMessage(getString(R.string.error_no_gps))
                .setPositiveButton(getString(android.R.string.ok))
                .setNegativeButton(getString(android.R.string.cancel))
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.dialog_ok:
                                newFragment.dismiss();
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                        }
                    }
                });

        newFragment.show(ft, "dialog");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "xx onLocationChanged" + location);
        if(mShouldReloadData){
            if(checkReady()){
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            }

            initData(location.getLatitude(), location.getLongitude());
            mShouldReloadData = false;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void toggleInfoWindow(HttpManager.Photo p){
        if(p != null){
            if(mCurrentPhoto == null){
                ObjectAnimator slideOutAnimator = ObjectAnimator.ofFloat(mViewPager, "translationY", 0);
                slideOutAnimator.setDuration(300);
                slideOutAnimator.start();
            }
            mCurrentPhoto = p;

            mViewPager.setCurrentItem(mPhotoList.indexOf(mCurrentPhoto), true);
        }
    }

    @Override
    public void onClick(View v) {
        HttpManager.Photo photo = null;
        switch (v.getId()){
            case R.id.photo_promenade_button:
                photo = (HttpManager.Photo) v.getTag();
                if(photo != null){
                    new PromenadeCalculatorTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCurrentPhoto);
                }
                break;
            case R.id.photo_promenade_container:
                photo = (HttpManager.Photo) v.getTag();
                if(photo != null){
                    Intent intent = new Intent(this, PreviewPhotoActivity.class);
                    intent.putExtra(PreviewPhotoActivity.EXTRA_TITLE, photo.title);
                    intent.putExtra(PreviewPhotoActivity.EXTRA_TITLE, photo.ownername);
                    intent.putExtra(PreviewPhotoActivity.EXTRA_DATE, photo.datetaken);
                    intent.putExtra(PreviewPhotoActivity.EXTRA_DESCRIPTION, photo.description._content);
                    intent.putExtra(PreviewPhotoActivity.EXTRA_URL, photo.getImageLUrl());
                    startActivity(intent);
                }
                break;
        }
    }

    private class SquareImageMarkerDownloader extends AsyncTask<Object, Void, Bitmap>{

        private Marker mMarker;

        @Override
        protected void onPostExecute(Bitmap b) {
            super.onPostExecute(b);

            if(b != null){
                mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(b));
            }
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            mMarker = (Marker)params[0];
            String url = (String)params[1];

            if(TextUtils.isEmpty(url)){
                // TODO
            }
            else{
                try {
                    return Picasso.with(MainActivity.this).load(url).transform(new CircleMarkerTransformation()).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    private class PromenadeCalculatorTask extends AsyncTask<HttpManager.Photo, Void, List<HttpManager.Photo>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            if(mPromenadePolyline != null){
                mPromenadePolyline.remove();
                mPromenadePolyline = null;
            }

        }

        @Override
        protected List<HttpManager.Photo> doInBackground(HttpManager.Photo... params) {

            HttpManager.Photo lastPoint = params[0];
            ArrayList<HttpManager.Photo> promenade = new ArrayList<HttpManager.Photo>(mPhotoList.size());

            ArrayList<HttpManager.Photo> unsettledPhotos = new ArrayList<HttpManager.Photo>(mPhotoList);


            promenade.add(lastPoint);
            unsettledPhotos.remove(lastPoint);

            while (unsettledPhotos.size() > 0){
                lastPoint = getMinimum(unsettledPhotos, lastPoint);
                promenade.add(lastPoint);
                unsettledPhotos.remove(lastPoint);
            }

            promenade.add(promenade.get(0));

            return promenade;
        }

        private HttpManager.Photo getMinimum(ArrayList<HttpManager.Photo> unsettledPhotos, HttpManager.Photo lastPoint) {

            float minDistance = Float.MAX_VALUE;
            float[] results = new float[3];
            HttpManager.Photo closestPhoto = null;

            for(HttpManager.Photo p : unsettledPhotos){
                Location.distanceBetween(lastPoint.latitude, lastPoint.longitude,
                        p.latitude, p.longitude,
                        results);
                if(results[0] < minDistance){
                    closestPhoto = p;
                    minDistance = results[0];
                }
            }
            return closestPhoto;
        }

        private ArrayList<LatLng> getCoordinateListFromPhotoList(List<HttpManager.Photo> photos){
            ArrayList<LatLng> result = new ArrayList<LatLng>(photos.size());

            for(HttpManager.Photo photo : photos){
                result.add(new LatLng(photo.latitude, photo.longitude));
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<HttpManager.Photo> promenade) {
            super.onPostExecute(promenade);
            mProgressBar.setVisibility(View.GONE);
            mPromenadePolyline = mMap.addPolyline((new PolylineOptions())
                    .addAll(getCoordinateListFromPhotoList(promenade))
                    .width(5)
                    .geodesic(true)
                    .color(Color.BLUE));

            mPhotoList = promenade;

            mAdapter = new PhotoDetailsPagerAdapter(promenade);
            mViewPager.setCurrentItem(0, false);
            mViewPager.setAdapter(mAdapter);
        }
    }

    private class PhotoDetailsPagerAdapter extends PagerAdapter {

        private List<HttpManager.Photo> mPromenade;

        public PhotoDetailsPagerAdapter(List<HttpManager.Photo> promenade){
            this.mPromenade = promenade;
        }

        @Override
        public int getCount() {
            if(mPromenade != null){
                return mPromenade.size();
            }
            return 0;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            HttpManager.Photo current = mPromenade.get(position);

            View rootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_photo_details, null);
            ((TextView)rootView.findViewById(R.id.photo_title)).setText(current.title + "");
            ((TextView)rootView.findViewById(R.id.photo_owner)).setText( "@" + current.ownername);

            rootView.setOnClickListener(MainActivity.this);
            rootView.setTag(current);
            rootView.findViewById(R.id.photo_promenade_button).setOnClickListener(MainActivity.this);
            rootView.findViewById(R.id.photo_promenade_button).setTag(current);

            collection.addView(rootView, 0);
            return rootView;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((RelativeLayout) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view==object);
        }

        @Override
        public void finishUpdate(ViewGroup arg0) {}

        @Override
        public void startUpdate(ViewGroup arg0) {}


        public void setPromenade(List<HttpManager.Photo> promenade) {
            this.mPromenade = promenade;
        }
    }

}
