package com.kami.fileexplorer.SmbStreamer;

import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import jcifs.smb.SmbFile;

import static android.content.ContentValues.TAG;

/**
 * Created by Arpit on 06-07-2015.
 */
public class Streamer extends StreamServer {

    public static final String TAG = "Streamer";
    public static final int PORT = 7871;
    public static final String URL = "http://127.0.0.1:" + PORT;
    private SmbFile file;
    long length = 0;
    //protected List<SmbFile> extras; //those can be subtitles
    // private InputStream stream;
    // private long length;
    private static Streamer instance;
    private static Pattern pattern = Pattern.compile("^.*\\.(?i)(mp3|wma|wav|aac|ogg|m4a|flac|mp4|avi|mpg|mpeg|3gp|3gpp|mkv|flv|rmvb)$");

    // private CBItem source;
    // private String mime;

    protected Streamer(int port) throws IOException {
        super(port, new File("."));
    }

    public static Streamer getInstance() {
        if (instance == null)
            try {
                instance = new Streamer(PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        return instance;
    }

    public static boolean isStreamMedia(SmbFile file) {
        return pattern.matcher(file.getName()).matches();
    }

    public void setStreamSrc(SmbFile file, long len) {
        Log.d(TAG, "wlf setStreamSrc: file = " +file);
        this.file = file;
        //this.extras = extraFiles;
        this.length = len;
    }

    @Override
    public void stop() {
        super.stop();
        instance = null;
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        Log.d("wlf ", "serve: uri = " + uri + "method = " + method + "header = " +header + " parms = "
                +parms+" files =  " +files);
        Response res;
        SmbFile sourceFile = null;
        String name = getNameFromPath(uri);
        if (file != null && file.getName().equals(name))
            sourceFile = file;
        /*else if(extras!=null){
            for(SmbFile i : extras){
                if(i!=null && i.getName().equals(name)){
                    sourceFile = i;
                    break;
                }
            }
        }*/

        Log.d("wlf ", "serve: sourceFile = " + sourceFile);
        if (sourceFile == null)
            res = new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, null);
        else {
            long startFrom = 0;
            long endAt = -1;
            String range = header.getProperty("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
            Log.d(TAG, "wlf Request: " + range + " from: " + startFrom + ", to: " + endAt);

            // Change return code and add Content-Range header when skipping
            // is requested
            //source.open();
            final StreamSource source = new StreamSource(sourceFile, length);
            long fileLen = source.length();
            if (range != null && startFrom > 0) {
                if (startFrom >= fileLen) {
                    res = new Response(HTTP_RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, null);
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                } else {
                    if (endAt < 0)
                        endAt = fileLen - 1;
                    long newLen = fileLen - startFrom;
                    if (newLen < 0)
                        newLen = 0;
                    Log.d(TAG, "wlf start=" + startFrom + ", endAt=" + endAt + ", newLen=" + newLen);
                    final long dataLen = newLen;
                    source.moveTo(startFrom);
                    Log.d(TAG, "wlf Skipped " + startFrom + " bytes");

                    res = new Response(HTTP_PARTIALCONTENT, source.getMimeType(), source);
                    res.addHeader("Content-length", "" + dataLen);
                }
            } else {
                source.reset();
                Log.d("wlf ", "serve: source.getMimeType() = " + source.getMimeType() + "source = " +source);
                res = new Response(HTTP_OK, source.getMimeType(), source);
                res.addHeader("Content-Length", "" + fileLen);
            }
        }

        res.addHeader("Accept-Ranges", "bytes"); // Announce that the file
        // server accepts partial
        // content requestes
        return res;
    }

    private static String getNameFromPath(String path) {
        if (path == null || path.length() < 2)
            return null;
        int slash = path.lastIndexOf('/');
        if (slash == -1)
            return path;
        else
            return path.substring(slash + 1);
    }

}

