package com.hh.wld.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebView;
import com.preference.PowerPreference;
import com.preference.Preference;

public class WebViewClient extends android.webkit.WebViewClient {

    private Context context;
    private ProgressDialog progressDialog;
    private Preference preference;

    public WebViewClient(Context context) {
        this.context = context;
        preference = PowerPreference.getDefaultFile();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // If url don't contains query param "did"
        // and last saved url is empty
        // then save this url as main domain
        String savedUrl = preference.getString(Constants.LAST_SAVED_URL, null);
        if (!url.contains("did=") && savedUrl == null) {
            preference.setString(Constants.LAST_SAVED_URL, url);
        }

        // if last host is the same as new one
        // the open this url in app
        // else open in default browser
        if(Utils.isHostsEqual(savedUrl, url)){
            view.loadUrl(url);
        }else{
            view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }

        return true;
    }

    //Show loader on url load
    @Override
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
    @Override
    public void onPageFinished(WebView view, String url) {
        try {
            // Close progressDialog
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
