package com.kami.fileexplorer.ui.file;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.kami.fileexplorer.BasePresenter;
import com.kami.fileexplorer.BaseView;
import com.kami.fileexplorer.data.FileExplorer;

import java.util.List;


interface FileContract {
    interface View extends BaseView<Presenter> {

        void listFile(String dir, List<FileExplorer.File> fileList);

        void notifyError(String message);

        FragmentManager getFragmentManager();
    }

    interface Presenter extends BasePresenter {
        String getTitle();

        String getDeviceName();

        void listFiles(String path);
    }
}
