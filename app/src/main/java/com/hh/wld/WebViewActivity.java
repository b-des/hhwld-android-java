package com.hh.wld;

import android.app.Activity;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.hh.wld.interfaces.ChromeClientCallback;
import com.hh.wld.ui.FullScreenDialog;
import com.hh.wld.utils.ChromeClient;
import com.hh.wld.utils.Constants;
import com.hh.wld.utils.Permissons;
import com.hh.wld.utils.WebViewClient;
import com.preference.PowerPreference;
import com.somesh.permissionmadeeasy.enums.Permission;
import com.somesh.permissionmadeeasy.helper.PermissionHelper;
import com.somesh.permissionmadeeasy.intefaces.PermissionListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class WebViewActivity extends AppCompatActivity implements ChromeClientCallback, PermissionListener  {

    private static final String TAG = WebViewActivity.class.getSimpleName();
    @BindView(R.id.webview) WebView webView;

    private WebSettings webSettings;

    private ValueCallback<Uri[]> filePath;
    private WebChromeClient.FileChooserParams fileChooserParams;
    private ChromeClient chromeClient;

    private CompositeDisposable disposable;
    private String appsflyerID = null;
    PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        disposable = new CompositeDisposable();

        appsflyerID = PowerPreference.getDefaultFile().getString(Constants.APPS_FLYER_ID);

        this.initWebView();
        this.loadUrl(getString(R.string.site_domain));
        this.registerNetworkObserver();

        permissionHelper = PermissionHelper.Builder()
                .with(this)
                .requestCode(Constants.REQUEST_CODE_CAMERA_AND_STORAGE)
                .setPermissionResultCallback(this)
                .askFor(Permission.CAMERA, Permission.STORAGE)
                .rationalMessage("Permissions are required for app to work properly")
                .build();
    }

    private void registerNetworkObserver() {
        FullScreenDialog dialog = new FullScreenDialog();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        disposable.add(
                ReactiveNetwork
                        .observeNetworkConnectivity(this)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(connectivity -> {
                            Timber.d("NETWORK: %s", connectivity.toString());
                            if (connectivity.state() == NetworkInfo.State.CONNECTED) {
                                try {
                                    dialog.dismiss();
                                } catch (Exception e) {
                                }

                            } else {
                                dialog.show(ft, FullScreenDialog.TAG);
                            }
                        })
        );
    }

    private void initWebView(){
         chromeClient = new ChromeClient(this, this);
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient(this));
        webView.setWebChromeClient(chromeClient);
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void loadUrl(String url){
        Uri builtUri = Uri.parse(url)
                .buildUpon()
                .appendQueryParameter("did", appsflyerID)
                .build();
        Timber.d("URL IS: %s", builtUri.toString());
        webView.loadUrl(builtUri.toString());
    }


    /*public class ChromeClient extends WebChromeClient {
        // For Android 5.0
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            Timber.d("onShowFileChooser");
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = Utils.createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Timber.d(ex, "Unable to create Image File");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }
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
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, Constants.INPUT_FILE_REQUEST_CODE);
            return true;
        }

        // openFileChooser for Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            // Create AndroidExampleFolder at sdcard
            // Create AndroidExampleFolder at sdcard
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidExampleFolder");
            if (!imageStorageDir.exists()) {
                // Create AndroidExampleFolder at sdcard
                imageStorageDir.mkdirs();
            }
            // Create camera captured image file path and name
            File file = new File(
                    imageStorageDir + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg");
            mCapturedImageURI = Uri.fromFile(file);
            // Camera capture image intent
            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            // Create file chooser intent
            Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
            // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, Constants.FILECHOOSER_RESULTCODE);
        }

        // openFileChooser for Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        //openFileChooser for other Android versions
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType,
                                    String capture) {
            openFileChooser(uploadMsg, acceptType);
        }
    }*/

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != Constants.INPUT_FILE_REQUEST_CODE || ChromeClient.mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (ChromeClient.mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(ChromeClient.mCameraPhotoPath)};
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onShowFileChooser(ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
        Timber.d("onShowFileChooser");
        this.filePath = filePath;
        this.fileChooserParams = fileChooserParams;
        if(Permissons.Check_CAMERA(this) && Permissons.Check_STORAGE(this)){
            return true;
        }else{
            permissionHelper.requestPermissions();
            return false;
        }
    }


    @Override
    public void onPermissionsDenied(int i, @NotNull ArrayList<String> arrayList) {
        Timber.d("onPermissionsDenied, %d", i);
    }

    @Override
    public void onPermissionsGranted(int i, @NotNull ArrayList<String> arrayList) {
        Timber.d("onPermissionsGranted, %d", i);
        if(i == Constants.REQUEST_CODE_CAMERA_AND_STORAGE){
           // chromeClient.onShowFileChooser(webView, filePath, fileChooserParams);
        }
    }
}