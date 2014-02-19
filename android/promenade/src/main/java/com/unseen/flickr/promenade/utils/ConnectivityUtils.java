package com.unseen.flickr.promenade.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
public class ConnectivityUtils {

    /**
     * Checks if wifi connection is established and data can pass
     *
     * @return if wifi is enabled, false otherwise
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return  mWifi.isConnected();
    }

    /**
     * Checks if device has mobile connectivity, and if this mobile connectivity
     * is established and data can pass
     *
     * @return if mobile is enabled, false otherwise
     */
    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return ((connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) == null ? false
                : connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()));
    }

    /**
     * Checks if device has any connectivity
     *
     * @return true if any connection is established and data can pass
     */
    public static boolean isConnected(Context context) {
        return isMobileConnected(context) || isWifiConnected(context);
    }


}
