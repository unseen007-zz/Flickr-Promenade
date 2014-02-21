package com.unseen.flickr.promenade.utils;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

/**
 * Device connectivity utility methods 
 */
public class LocationUtils {

	// ===========================================================
	// Constants
	// ===========================================================
	/**
	 * The log tag.
	 */
	private static final String TAG = LocationUtils.class.getSimpleName();

	/**
	 * The minimum time between 2 locations updates (in milliseconds)
	 */
	public static final long MIN_TIME = 2000; // in milliseconds
	/**
	 * The minimum distance between 2 updates (in meters)
	 */
	public static final float MIN_DISTANCE = 500; // in meters
	/**
	 * The validity of a last know location (in milliseconds)
	 */
	private static final long MAX_LAST_KNOW_LOCATION_TIME = 60000; // 10 minutes
	
	// ===========================================================
	// Fields
	// ===========================================================
    
	// ===========================================================
	// Constructors
	// ===========================================================
	private LocationUtils() {};
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	public static String getBestProvider(Activity activity) {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); //any accuracy
		criteria.setAltitudeRequired(false); // no altitude
		criteria.setBearingRequired(false); // no compass... for now ;)
		criteria.setSpeedRequired(false); // no speed required
		boolean enabledOnly = true; // only enabled location providers
		String bestProvider = getLocationManager(activity).getBestProvider(criteria, enabledOnly);
		Log.v(TAG, "nb location providers: " + bestProvider);
		return bestProvider;
	}
	
	/**
	 * @param activity the activity
	 * @return the providers matching the application requirement
	 */
	private static List<String> getBestProviders(Activity activity) {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); //any accuracy
		criteria.setAltitudeRequired(false); // no altitude
		criteria.setBearingRequired(false); // no compass... for now ;)
		criteria.setSpeedRequired(false); // no speed required
		boolean enabledOnly = true; // only enabled location providers
		List<String> providers = getLocationManager(activity).getProviders(criteria, enabledOnly);
        Log.v(TAG, "nb location providers: " + providers.size());
		return providers;
	}

	/**
	 * @param activity the activity
	 * @return the location manager service
	 */
	public static LocationManager getLocationManager(Activity activity) {
		return (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * @param activity the activity
	 * @return the best valid last know location or <b>NULL</b>
	 */
	public static Location getBestLastKnownLocation(Activity activity) {
		Location result = null;
		for (String provider : getBestProviders(activity)) {
			Location lastLocation = getLocationManager(activity).getLastKnownLocation(provider);
			// IF the last location is NOT NULL (= location provider disabled) DO
			if (lastLocation != null) {
				// IF no last location candidate DO
				if (result == null) {
					// IF this location candidate is not too old DO
					if (isNotTooOld(lastLocation)) {
						result = lastLocation;
					}
					else{
                        Log.v(TAG, "last location is too old : " + (System.currentTimeMillis() - lastLocation.getTime()) + " (ms)");
					}
				} else {
					// IF the new location candidate is more recent DO
					if (lastLocation.getTime() > result.getTime()) {
						result = lastLocation;
					}
					else{
                        Log.v(TAG, "new location candidate is more recent");
					}
				}
			}
		}
		if (result != null) {
            Log.v(TAG, "last know location:" + result.getProvider() + " > " + result.getLatitude() + ", "
			        + result.getLongitude() + "(" + result.getAccuracy() + ") "
			        + ((System.currentTimeMillis() - result.getTime()) / 1000) + " seconds ago.");
		} else {
            Log.v(TAG, "no valid last location found!");
		}
		return result;
	}

	/**
	 * @param location the location
	 * @return true if the location is not too "old"
	 */
	private static boolean isNotTooOld(Location location) {
		return System.currentTimeMillis() - location.getTime() < MAX_LAST_KNOW_LOCATION_TIME;
	}

	/**
	 * Enable updates for an activity and a listener
	 * @param activity the activity
	 * @param listener the listener
	 */
	public static void enableLocationUpdates(Activity activity, LocationListener listener) {
        Log.v(TAG, "enableLocationUpdates()");
		// enable location updates
		for (String provider : getBestProviders(activity)) {
            Log.v(TAG, "enableLocationUpdates() : " + provider);
			getLocationManager(activity).requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, listener);
		}
	}

	/**
	 * Disable updates for an activity and a listener
	 * @param activity the activity
	 * @param listener the listener
	 */
	public static void disableLocationUpdates(Activity activity, LocationListener listener) {
        Log.v(TAG, "disableLocationUpdates()");
		getLocationManager(activity).removeUpdates(listener);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
