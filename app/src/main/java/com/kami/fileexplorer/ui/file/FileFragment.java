package com.kami.fileexplorer.ui.file;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kami.fileexplorer.R;
import com.kami.fileexplorer.SmbStreamer.MimeTypes;
import com.kami.fileexplorer.SmbStreamer.Streamer;
import com.kami.fileexplorer.bean.FileRoute;
import com.kami.fileexplorer.comparable.DefaultFileComparator;
import com.kami.fileexplorer.data.FileExplorer;
import com.kami.fileexplorer.ui.BaseAdapter;
import com.kami.fileexplorer.ui.BaseFragment;
import com.kami.fileexplorer.widget.FSRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import butterknife.BindView;
import jcifs.smb.SmbFile;


public class FileFragment extends BaseFragment implements FileContract.View, BaseAdapter.OnItemClickListener {
    private FileContract.Presenter mPresenter;
    @BindView(R.id.file_list)
    FSRecyclerView mFileListView;
    private FileAdapter mFileAdapter;
    @BindView(R.id.file_route_list)
    FSRecyclerView mFileRouteListView;
    private FileRouteAdapter mFileRouteAdapter;
    private Stack<List<FileExplorer.File>> mFileListStack;
    private Comparator<FileExplorer.File> mFileComparator;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.frag_file, container, false);
    }

    @Override
    public void setPresenter(FileContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFileListStack = new Stack<>();
        mFileComparator = new DefaultFileComparator();


        initFileListView();
        initFileRouteListView();
        mPresenter.subscribe();
    }

    @Override
    public void onItemClick(BaseAdapter adapter, int position, View view) {
        if (adapter == mFileAdapter) {
            FileExplorer.File file = mFileAdapter.getList().get(position);
            if (file.isDirectory()) {
                final FileRoute fileRoute = mFileRouteAdapter.getList().get(mFileRouteAdapter.getItemCount() - 1);

                Log.d("wlf", "fileRoute" + fileRoute.getPath() + ("/".equals(fileRoute.getPath())));
                if ("/".equals(fileRoute.getPath())) {
                    mPresenter.listFiles(fileRoute.getPath() + file.getName());
                } else {
                    mPresenter.listFiles(fileRoute.getPath() + "/" + file.getName());
                }
            }
            if (file.isFilie()) {
                Log.d("wlf", "onItemClick: ++++" + file.isFilie());
                Log.d("wlf", "onItemClick: @@@" + file.getSmbFile());
                Log.d("wlf", "onItemClick: @@@" + file.length());
                launchSMB(file,file.length(),getFileActivity());
            }
        } else {
            if (!backTo(position)) {
                getActivity().finish();
            }
        }
    }

    boolean backTo() {
        int position = mFileRouteAdapter.getItemCount() - 2;
        return backTo(position);
    }

    boolean backTo(int position) {
        if (position < 1) {
            return false;
        }
        int removeSize = mFileRouteAdapter.getItemCount() - 1 - position;
        mFileRouteAdapter.removeTo(position);
        for (int i = 0; i < removeSize; i++) {
            mFileListStack.pop();
        }
        mFileAdapter.setList(mFileListStack.peek());
        return true;
    }

    private void initFileListView() {
        mFileListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mFileAdapter = new FileAdapter(getContext());
        mFileAdapter.setItemClickListener(this);
        mFileListView.setAdapter(mFileAdapter);
    }

    private void initFileRouteListView() {
        mFileRouteListView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager
                .HORIZONTAL, false));
        List<FileRoute> list = new ArrayList<>();
        list.add(new FileRoute(mPresenter.getTitle(), mPresenter.getTitle()));
        list.add(new FileRoute(mPresenter.getDeviceName(), "/"));
        mFileRouteAdapter = new FileRouteAdapter(this.getContext(), list);
        mFileRouteAdapter.setItemClickListener(this);
        mFileRouteListView.setAdapter(mFileRouteAdapter);
    }

    @Override
    public void listFile(String path, List<FileExplorer.File> fileList) {
        Collections.sort(fileList, mFileComparator);
        if (!path.equals("/")) {
            String name = path.substring(path.lastIndexOf("/") + 1);
            mFileRouteAdapter.add(new FileRoute(name, path));
        }
        mFileListStack.push(fileList);
        mFileAdapter.setList(fileList);
    }


    @Override
    public void notifyError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        mPresenter.unSubscribe();
        super.onDestroyView();
    }
    public FileActivity getFileActivity() {
        return (FileActivity) getActivity();
    }

    public static void launchSMB(final FileExplorer.File baseFile, long length, final Activity activity) {
        final Streamer s = Streamer.getInstance();
        new Thread() {
            public void run() {
                try {
                    s.setStreamSrc(new SmbFile(baseFile.getpath()), length);
                    Log.d("wlf", "run: 1 " + baseFile.getpath2() + length + " " +baseFile.getpath());
                    activity.runOnUiThread(() -> {
                        try {
                            Uri uri = Uri.parse(Streamer.URL + Uri.fromFile(new File(Uri.parse(baseFile.getpath()).getPath())).getEncodedPath());
                            Log.d("wlf", "run: uri = " +uri);
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setDataAndType(uri, MimeTypes.getMimeType(baseFile.getpath(), baseFile.isDirectory()));
//                            i.setDataAndType(uri,MimeType.get().getMimeType(baseFile.getpath()));
                            PackageManager packageManager = activity.getPackageManager();
                            List<ResolveInfo> resInfos = packageManager.queryIntentActivities(i, 0);
                            Log.d("wlf", "run: resinfos = " + resInfos.size());
                            if (resInfos != null && resInfos.size() > 0)
                                activity.startActivity(i);
                            else
                                Toast.makeText(activity,
                                        "您需要将此文件复制到存储空间中才能打开它",
                                        Toast.LENGTH_SHORT).show();
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
