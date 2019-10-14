package com.kami.fileexplorer.ui.device.cifs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kami.fileexplorer.R;
import com.kami.fileexplorer.bean.CIFSDevice;
import com.kami.fileexplorer.bean.Device;
import com.kami.fileexplorer.data.FileExplorer;
import com.kami.fileexplorer.data.cifs.CIFSFileExplorer;
import com.kami.fileexplorer.dialog.auth.AuthDialog;
import com.kami.fileexplorer.dialog.auth.CIFSAuthDialog;
import com.kami.fileexplorer.ui.BaseAdapter;
import com.kami.fileexplorer.ui.BaseFragment;
import com.kami.fileexplorer.ui.file.FileActivity;
import com.kami.fileexplorer.widget.DividerGridItemDecoration;
import com.kami.fileexplorer.widget.FSRecyclerView;
import com.kami.fileexplorer.widget.GridSpacingItemDecoration;

import java.util.List;

import butterknife.BindView;
import jcifs.smb.NtlmPasswordAuthentication;


public class CIFSFragment extends BaseFragment implements CIFSContract.View, BaseAdapter.OnItemClickListener {
    private CIFSContract.Presenter mPresenter;
    @BindView(R.id.cifs_list)
    FSRecyclerView mCifsListView;
    private CIFSListAdapter mAdapter;
    private ProgressDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.frag_cifs, container, false);
    }


    @Nullable
    @Override
    public Context getViewContext() {
        return getContext();
    }

    @Override
    public void setCifsList(List<CIFSDevice> deviceList) {
        mAdapter.setList(deviceList);
        if (dialog != null)
        dialog.dismiss();
    }


    @Override
    public void notifyError(String errorMessage) {
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemClick(BaseAdapter adapter, int position, View view) {
        final CIFSDevice device = mAdapter.getList().get(position);
        CIFSAuthDialog authDialog = new CIFSAuthDialog();
        authDialog.setAuthListener(args -> {
            CIFSFileExplorer explorer = new CIFSFileExplorer(device);
            explorer.setAuth(args);
            startActivity(FileActivity.getIntent(getContext(), explorer));
        });
        authDialog.show(getFragmentManager(), authDialog.getClass().getName());

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dialog = ProgressDialog.show(getContext(), "搜索", "正在搜索设备...");
        mAdapter = new CIFSListAdapter(getContext());
        mAdapter.setItemClickListener(this);
        mCifsListView.setAdapter(mAdapter);
        mCifsListView.setLayoutManager(new GridLayoutManager(getContext(), 5));
        mCifsListView.addItemDecoration(new GridSpacingItemDecoration(1));
        if (mPresenter != null)
        mPresenter.subscribe();
    }

    @Override
    public void setPresenter(CIFSContract.Presenter presenter) {
        this.mPresenter = presenter;
    }


    @Override
    public void onDestroyView() {
        if (mPresenter != null)
        mPresenter.unSubscribe();
        super.onDestroyView();
    }

}
