package com.hh.wld;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.hh.wld.utils.Constants;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.util.Map;

import timber.log.Timber;

public class App extends Application {

    private String AF_DEV_KEY = "";


    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
        Timber.tag("TIMBER");

        PowerPreference.init(this);
        Preference preference = PowerPreference.getDefaultFile();

        AF_DEV_KEY = getString(R.string.appslyer_key);
        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {


            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {

                for (String attrName : conversionData.keySet()) {
                    Log.d(Constants.LOG_TAG, "attribute: " + attrName + " = " + conversionData.get(attrName));
                }
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d(Constants.LOG_TAG, "error getting conversion data: " + errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {

                for (String attrName : attributionData.keySet()) {
                    Log.d(Constants.LOG_TAG, "attribute: " + attrName + " = " + attributionData.get(attrName));
                }

            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d(Constants.LOG_TAG, "error onAttributionFailure : " + errorMessage);
            }
        };

        AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, this);

        //if(!preference.contains(Constants.APPS_FLYER_ID)){
            String appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(this);
            Log.d(Constants.LOG_TAG, "Appslyer ID: " + appsFlyerId);
            preference.setString(Constants.APPS_FLYER_ID, appsFlyerId);
        //}



    }


}
