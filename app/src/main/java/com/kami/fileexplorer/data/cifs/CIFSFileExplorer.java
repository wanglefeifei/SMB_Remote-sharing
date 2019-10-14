package com.kami.fileexplorer.data.cifs;

import android.util.Log;

import com.kami.fileexplorer.bean.CIFSDevice;
import com.kami.fileexplorer.data.FileExplorer;
import com.kami.fileexplorer.exception.AuthException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;


public class CIFSFileExplorer implements FileExplorer {
    private CIFSDevice mDevice;
    private NtlmPasswordAuthentication mAuth;

    public CIFSFileExplorer(CIFSDevice device) {
        mDevice = device;
    }

    @Override
    public void setAuth(String[] args) {
        if (args.length != 3) {
            throw new IllegalStateException("auth input count must be 3");
        }
        this.mAuth = new NtlmPasswordAuthentication(args[0], args[1], args[2]);
//        Log.d("wlf", "setAuth: mAuth = " + (mAuth.getName()+ mAuth.getPassword()),new Exception());
    }

    @Override
    public String getTitle() {
        return "网络邻居";
    }

    @Override
    public List<File> getFiles(String path) throws IOException {
        try {
            String path1 = "smb://"+(android.text.TextUtils.isEmpty(mAuth.getDomain()) ? ""
                    :( URLEncoder.encode(mAuth.getDomain() + ";","UTF-8")) )+ (false ? "" :
                    (URLEncoder.encode(mAuth.getName(), "UTF-8") + ":" + URLEncoder.encode(mAuth.getPassword(), "UTF-8") + "@")) + mDevice.getHostIp() + path + "/";
            Log.d("wlf", "getFiles: path1 = " + path1);
            SmbFile parent = new SmbFile(path1);
//            SmbFile parent = new SmbFile(String.format("smb://%s%s/", mDevice.getHostIp(), path), mAuth);
            Log.d("wlf", "getFiles: parent = " + parent);
            if (!parent.exists()) {
                throw new IOException(String.format("%s is not exists", path));
            }
            SmbFile[] children = parent.listFiles();
            List<File> fileList = new ArrayList<>();
            Log.d("wlf", "getFiles: children = " + children.toString());
            Log.d("wlf", "getFiles: fileList = " + fileList.toString());
            for (SmbFile child : children) {
                CIFSFile file = new CIFSFile();
                String name = child.getName();
                if (name.endsWith("/")) {
                    name = name.substring(0, name.length() - 1);
                }
                file.setName(name);
                boolean isDir = child.isDirectory();
                boolean isfile = child.isFile();
                String fiel_path = child.getPath();

                file.setSmbFile(child);
                file.setPath(fiel_path);
                file.setPath2(path1);
                file.setFile(isfile);
                file.setDir(isDir);
                if (!isDir) {
                    file.setLastModified(child.getLastModified());
                    file.setLength(child.getContentLength());
                }
                fileList.add(file);
            }
            return fileList;
        } catch (SmbAuthException e) {
            throw new AuthException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String getDeviceName() {
        return mDevice.getHostName();
    }

    public static class CIFSFile implements File {
        private String name;
        private boolean isDir;
        private boolean isFile;
        private String path;
        private String path2;
        private SmbFile smbFile;
        private int length;
        private long lastModified;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public SmbFile getSmbFile() {
            return smbFile;
        }

        @Override
        public boolean isDirectory() {
            return isDir;
        }

        @Override
        public boolean isFilie() {
            return isFile;
        }

        @Override
        public String getpath() {
            return path;
        }

        @Override
        public String getpath2() {
            return path2;
        }

        public void setSmbFile(SmbFile smbFile) {
            this.smbFile = smbFile;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setPath2(String path) {
            this.path2 = path;
        }

        public void setFile(boolean file) {
            this.isFile = file;
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public long lastModified() {
            return lastModified;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDir(boolean dir) {
            isDir = dir;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }
    }
}
