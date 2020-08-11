package com.hh.wld.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.core.content.FileProvider;

import com.hh.wld.BuildConfig;
import com.hh.wld.R;
import com.hh.wld.interfaces.WebViewCallback;
import com.preference.PowerPreference;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

public class ChromeClient extends WebChromeClient {

    private static final String TAG = "WEBVIEW";

    public static ValueCallback<Uri> mUploadMessage;
    public static Uri mCapturedImageURI = null;
    public static ValueCallback<Uri[]> mFilePathCallback;
    public static File mCameraPhoto;
    private Activity activity;
    WebViewCallback callback;

    public ChromeClient(Activity activity, WebViewCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    // save current URL
    // because some sites don't use HTTP redirect(i.e. Angular, and other SPA)
    // need to observe some changes in different way
    // so onProgressChanged method is good place to handle that changes
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        //save url only when page loaded
        if(newProgress == 100){
            String savedUrl = PowerPreference.getDefaultFile().getString(Constants.LAST_SAVED_URL, null);
            if(savedUrl != null && Utils.isHostsEqual(savedUrl, view.getUrl())){
                PowerPreference.getDefaultFile().setString(Constants.LAST_SAVED_URL, view.getUrl());
            }
        }
    }

    @Override
    public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
        if(!this.callback.onShowFileChooser()){
            return false;
        }
        // Double check that we don't have any existing callbacks
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
        }
        mFilePathCallback = filePath;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = Utils.createImageFile(activity);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Timber.d(ex, "Unable to create Image File");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhoto = photoFile;
                Uri outputFileUri =  FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                takePictureIntent = null;
            }
        }
        // create intent to open image chooser
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");
        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, activity.getString(R.string.text_image_chooser));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        activity.startActivityForResult(chooserIntent, Constants.INPUT_FILE_REQUEST_CODE);
        return true;
    }

}
