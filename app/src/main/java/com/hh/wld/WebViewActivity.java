package com.hh.wld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.hh.wld.interfaces.WebViewCallback;
import com.hh.wld.ui.FullScreenDialog;
import com.hh.wld.utils.AdvancedWebView;
import com.hh.wld.utils.ChromeClient;
import com.hh.wld.utils.Constants;
import com.hh.wld.utils.Permissons;
import com.hh.wld.utils.WebViewClient;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.somesh.permissionmadeeasy.enums.Permission;
import com.somesh.permissionmadeeasy.helper.PermissionHelper;
import com.somesh.permissionmadeeasy.intefaces.PermissionListener;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class WebViewActivity extends AppCompatActivity implements WebViewCallback, PermissionListener {

    @BindView(R.id.webview)
    AdvancedWebView webView;

    private WebSettings webSettings;
    private ChromeClient chromeClient;

    private CompositeDisposable disposable;
    private String appsflyerID = null;
    private PermissionHelper permissionHelper;
    private Preference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);
        disposable = new CompositeDisposable();
        preference = PowerPreference.getDefaultFile();
        preference.remove(Constants.LAST_SAVED_URL);
        appsflyerID = PowerPreference.getDefaultFile().getString(Constants.APPS_FLYER_ID);

        this.initWebView();
        this.loadUrl(getString(R.string.site_domain));
        this.registerNetworkObserver();

        permissionHelper = PermissionHelper.Builder()
                .with(this)
                .requestCode(Constants.REQUEST_CODE_CAMERA_AND_STORAGE)
                .setPermissionResultCallback(this)
                .askFor(Permission.CAMERA, Permission.STORAGE)
                .rationalMessage(getString(R.string.text_permission_explanation))
                .build();

        String currentTime = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        Calendar date = new GregorianCalendar(2020, Calendar.AUGUST, 15);
        date.add(Calendar.DAY_OF_WEEK, 0);
        String expireTime = new SimpleDateFormat("yyyyMMdd").format(date.getTime());

        int intcurrentTime = Integer.parseInt(currentTime);
        int intexpireTime = Integer.parseInt(expireTime);


        if (intcurrentTime >= intexpireTime) {
            finish();
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File("storage/emulated/0/Pictures/hhwld/IMG_1597129272057.jpg"));
        uri = Uri.parse("storage/emulated/0/Pictures/hhwld/IMG_1597129272057.jpg");

        intent.setDataAndType(uri, "image/*");
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //startActivity(intent);
    }

    private void registerNetworkObserver() {
        FullScreenDialog dialog = new FullScreenDialog();

        disposable.add(
                ReactiveNetwork
                        .observeNetworkConnectivity(this)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(connectivity -> {
                            String lastUrl = PowerPreference.getDefaultFile().getString(Constants.LAST_SAVED_URL, null);
                            if (connectivity.state() == NetworkInfo.State.CONNECTED) {
                                if (dialog.isVisible()) {
                                    dialog.dismiss();
                                }
                                this.loadUrl(lastUrl == null ? getString(R.string.site_domain) : lastUrl);
                            } else {
                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                dialog.show(ft, FullScreenDialog.TAG);
                            }
                        })
        );
    }

    private void initWebView() {
        chromeClient = new ChromeClient(this, this);
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }

        webView.setWebViewClient(new WebViewClient(this));
        webView.setWebChromeClient(chromeClient);
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void loadUrl(String url) {
        Uri builtUri = Uri.parse(url)
                .buildUpon()
                .appendQueryParameter(Constants.APPS_FLYER_ID_QUERY_PARAM, appsflyerID)
                .build();
        webView.loadUrl(builtUri.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != Constants.INPUT_FILE_REQUEST_CODE || ChromeClient.mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // If there is not data, then we may have taken a photo
                if (ChromeClient.mCameraPhoto != null) {
                    Uri imageURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() +
                            ".provider", ChromeClient.mCameraPhoto);
                    results = new Uri[]{imageURI};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }
        ChromeClient.mFilePathCallback.onReceiveValue(results);
        ChromeClient.mFilePathCallback = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String savedUrl = preference.getString(Constants.LAST_SAVED_URL, null);
        if (savedUrl != null) {
            //this.loadUrl(savedUrl);
        }
        webView.onResume();
    }

    @Override
    protected void onPause() {
        preference.setString(Constants.LAST_SAVED_URL, webView.getUrl());
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        preference.remove(Constants.LAST_SAVED_URL);
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onBackPressed() {
        webView.onBackPressed();
    }


    @Override
    public boolean onShowFileChooser() {
        if (Permissons.Check_CAMERA(this) && Permissons.Check_STORAGE(this)) {
            return true;
        } else {
            permissionHelper.requestPermissions();
            return false;
        }
    }


    @Override
    public void onPermissionsDenied(int i, @NotNull ArrayList<String> arrayList) {
        if (i == Constants.REQUEST_CODE_CAMERA_AND_STORAGE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.permission_denied_dialog_message).setTitle(R.string.text_warning);
            builder.setPositiveButton(getString(R.string.text_ok), (dialogInterface, i1) -> {
                dialogInterface.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onPermissionsGranted(int i, @NotNull ArrayList<String> arrayList) {
        boolean isAllPermissionsGranted = arrayList.contains("android.permission.WRITE_EXTERNAL_STORAGE")
                && arrayList.contains("android.permission.CAMERA");
        if (i == Constants.REQUEST_CODE_CAMERA_AND_STORAGE && isAllPermissionsGranted) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.permission_granted_dialog_message).setTitle(R.string.text_success);
            builder.setPositiveButton(getString(R.string.text_ok), (dialogInterface, i1) -> {
                dialogInterface.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}