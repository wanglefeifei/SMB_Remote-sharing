package com.kami.fileexplorer.data;


import com.kami.fileexplorer.exception.AuthException;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.List;

import jcifs.smb.SmbFile;


public interface FileExplorer extends Serializable{
    public static final String CIFS = "cifs";
    List<File> getFiles(String path) throws IOException;
    String getDeviceName();
    String getTitle();
    void setAuth(String[] auth);
    interface File{
        String getName();
        SmbFile getSmbFile();
        boolean isDirectory();
        boolean isFilie();
        String  getpath();
        String  getpath2();
        long length();
        long lastModified();
    }
}
