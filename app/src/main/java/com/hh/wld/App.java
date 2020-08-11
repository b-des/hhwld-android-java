package com.hh.wld;

import android.app.Application;

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

        PowerPreference.init(this);
        Preference preference = PowerPreference.getDefaultFile();

        AF_DEV_KEY = getString(R.string.appslyer_key);
        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {

            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) { }

            @Override
            public void onConversionDataFail(String errorMessage) { }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) { }

            @Override
            public void onAttributionFailure(String errorMessage) { }
        };

        AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, this);

        if(!preference.contains(Constants.APPS_FLYER_ID)){
            String appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(this);
            preference.setString(Constants.APPS_FLYER_ID, appsFlyerId);
        }

    }



}
