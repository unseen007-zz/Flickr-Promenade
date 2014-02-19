package com.unseen.flickr.promenade.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.unseen.flickr.promenade.network.HttpManager;

import retrofit.RestAdapter;

/**
 * Copyright 2014 Steven Perraudin
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class FlickPhotoLoader extends AsyncTaskLoader<HttpManager.PhotoResponse> {

    public final static String PARAM_LATITUDE = "com.unseen.flickr.promenade.loader.FlickPhotoLoader.PARAM_LATITUDE";
    public final static String PARAM_LONGITUDE = "com.unseen.flickr.promenade.loader.FlickPhotoLoaderPARAM_LONGITUDE";

    private HttpManager.PhotoResponse mResponse;

    private float mLatitude;
    private float mLongitude;

    public FlickPhotoLoader(Context context, Bundle bundle) {
        super(context);

        if(bundle != null){
            mLatitude = bundle.getFloat(PARAM_LATITUDE);
            mLongitude = bundle.getFloat(PARAM_LONGITUDE);
        }
    }

    @Override
    public HttpManager.PhotoResponse loadInBackground() {

        try{
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(HttpManager.API_URL)
                    .build();

            HttpManager http = restAdapter.create(HttpManager.class);

            mResponse = http.getPopularPhotos(mLatitude, mLongitude);
        }
        catch (Exception e){
            Log.e("xx", "xx", e);
            // TODO do some error handling
        }
        return mResponse;
    }

    @Override
    protected void onStartLoading() {
        if (mResponse != null) {
            deliverResult(mResponse);
        } else if (takeContentChanged() || mResponse == null) {
            forceLoad();
        }
    }


}
