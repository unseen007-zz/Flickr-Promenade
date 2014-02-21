package com.unseen.flickr.promenade;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
public class PreviewPhotoActivity extends Activity{

    public static final String EXTRA_TITLE = "com.unseen.flickr.promenade.PreviewPhotoActivity.EXTRA_TITLE";
    public static final String EXTRA_OWNERNAME = "com.unseen.flickr.promenade.PreviewPhotoActivity.EXTRA_OWNERNAME";
    public static final String EXTRA_DATE = "com.unseen.flickr.promenade.PreviewPhotoActivity.EXTRA_DATE";
    public static final String EXTRA_DESCRIPTION = "com.unseen.flickr.promenade.PreviewPhotoActivity.EXTRA_DESCRIPTION";
    public static final String EXTRA_URL = "com.unseen.flickr.promenade.PreviewPhotoActivity.EXTRA_URL";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_photo);


        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            finish();
            return;
        }

        String title = bundle.getString(EXTRA_TITLE);
        String owner = bundle.getString(EXTRA_OWNERNAME);
        String date = bundle.getString(EXTRA_DATE);
        String description = bundle.getString(EXTRA_DESCRIPTION);
        String url = bundle.getString(EXTRA_URL);

        if(TextUtils.isEmpty(url)){
            finish();
            return;
        }

        Picasso.with(this).load(url).placeholder(R.drawable.ic_launcher).into(((ImageView) findViewById(R.id.preview_photo)));

        ((TextView) findViewById(R.id.preview_title)).setText(title);
        ((TextView) findViewById(R.id.preview_owner)).setText(owner);
        ((TextView) findViewById(R.id.preview_date)).setText(date);
        ((TextView) findViewById(R.id.preview_description)).setText(description);
    }
}
