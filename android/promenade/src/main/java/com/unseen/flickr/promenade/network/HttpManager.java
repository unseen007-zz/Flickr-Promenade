package com.unseen.flickr.promenade.network;

import com.unseen.flickr.promenade.utils.Config;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

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
public interface HttpManager {

    static final String API_URL = "http://api.flickr.com/services/rest";

    @GET("/?method=flickr.photos.search&api_key=" + Config.FLICKR_KEY + "&license=1%2C2%2C3%2C4%2C5%2C6%2C7&sort=+interestingness-desc&privacy_filter=1&safe_search=1&media=photos&has_geo=1&radius=30&radius_units=km&extras=description%2Cowner_name%2Cdate_taken%2Cgeo&per_page=100&page=1&format=json&nojsoncallback=1")
    PhotoResponse getPopularPhotos(
            @Query("lat") float latitude,
            @Query("lon") float longitude
    );

    public static class PhotoResponse{
        public PhotoList photos;
    }

    public static class PhotoList{
        public List<Photo> photo;
    }

    public static class Photo {
        public String id;
        public String title;
        public float lat;
        public float lon;
        private String datetime;
        private Description description;
    }

    public static class Description {
        public String _content;
    }

}
