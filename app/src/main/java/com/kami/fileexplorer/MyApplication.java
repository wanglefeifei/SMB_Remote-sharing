package com.kami.fileexplorer;

import android.app.Application;

import com.kami.fileexplorer.SmbStreamer.MimeTypes;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initConfig();
    }

    private void initConfig() {
//        MimeTypes.get().readData(this);
//        System.setProperty("jcifs.smb.client.dfs.disabled", "true");
//        System.setProperty("jcifs.smb.client.soTimeout", "60000");
//        System.setProperty("jcifs.smb.client.responseTimeout", "60000");
        //......
    }

}
