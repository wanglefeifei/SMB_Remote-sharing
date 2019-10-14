package com.kami.fileexplorer.ui.device.cifs;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import com.kami.fileexplorer.bean.CIFSDevice;
import com.kami.fileexplorer.data.cifs.CIFSSearcher;
import com.kami.fileexplorer.util.NetUtils;
import com.kami.fileexplorer.util.schedulers.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class CIFSPresenter implements CIFSContract.Presenter {
    private CompositeDisposable mDisposable;
    private CIFSContract.View mView;

    public CIFSPresenter(@NonNull CIFSContract.View view) {
        mDisposable = new CompositeDisposable();
        this.mView = view;
    }

    @Override
    public void subscribe() {
        Context context = mView.getViewContext();
        if (!NetUtils.isWifiConnected(context)) {
            return;
        }
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        final List<CIFSDevice> deviceList = new ArrayList<>();
        Disposable disposable = CIFSSearcher.getInstance()
                .searchDevices(dhcpInfo.ipAddress)
                .subscribeOn(SchedulerProvider.getInstance().computation())
                .observeOn(SchedulerProvider.getInstance().ui())
                .subscribe(deviceList::add,
                        throwable -> mView.notifyError(throwable.getMessage()),
                        () -> mView.setCifsList(deviceList));
        mDisposable.add(disposable);
    }


    @Override
    public void unSubscribe() {
        mDisposable.clear();
    }
}
