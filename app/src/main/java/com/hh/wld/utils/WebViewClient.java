package com.hh.wld.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebView;

import com.hh.wld.WebViewActivity;
import com.preference.PowerPreference;
import com.preference.Preference;

import timber.log.Timber;

public class WebViewClient extends android.webkit.WebViewClient {
    private Context context;
    ProgressDialog progressDialog;
    Preference preference;
    public WebViewClient(Context context) {
        this.context = context;
        preference = PowerPreference.getDefaultFile();
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d("shouldOverrideUrlLoading: %s", url);
        // If url contains mailto link then open Mail Intent

        preference.setString(Constants.LAST_SAVED_URL, url);
        if (url.contains("mailto:")) {
            // Could be cleverer and use a regex
            view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }else {
            // Stay within this webview and load url
            view.loadUrl(url);
            return true;
        }

    }
    //Show loader on url load
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // Then show progress  Dialog
        // in standard case YourActivity.this
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this.context);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }
    }
    // Called when all page resources loaded
    public void onPageFinished(WebView view, String url) {
        try {
            // Close progressDialog
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void check(){

    }
}
