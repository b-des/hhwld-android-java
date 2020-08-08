package com.hh.wld.interfaces;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public interface ChromeClientCallback {
    boolean onShowFileChooser(ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams);
}
