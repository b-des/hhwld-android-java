package com.hh.wld.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.google.common.net.InternetDomainName;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    /**
     * Create image file in public directory
     * @return
     * @throws IOException
     */
    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        File imageStorageDir = new File(context.getFilesDir().getAbsolutePath());
        if (!imageStorageDir.exists()) {
            // Create folder at sdcard
            imageStorageDir.mkdirs();
        }
        return new File(imageStorageDir + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg");

    }

    public static boolean isHostsEqual(String url1, String url2) {
        if(url1 ==null || url2 == null){
            return true;
        }
        Uri uri1 = Uri.parse(url1)
                .buildUpon()
                .build();

        Uri uri2 = Uri.parse(url2)
                .buildUpon()
                .build();

        String host1 = InternetDomainName.from(uri1.getHost()).topDomainUnderRegistrySuffix().toString();
        String host2 = InternetDomainName.from(uri2.getHost()).topDomainUnderRegistrySuffix().toString();
        return  host1.equals(host2);
    }

}
