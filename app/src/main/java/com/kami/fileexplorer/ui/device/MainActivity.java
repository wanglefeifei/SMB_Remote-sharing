package com.kami.fileexplorer.ui.device;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.kami.fileexplorer.R;
import com.kami.fileexplorer.ui.BaseActivity;
import com.kami.fileexplorer.ui.device.cifs.CIFSFragment;
import com.kami.fileexplorer.ui.device.cifs.CIFSPresenter;
import com.kami.fileexplorer.widget.FSToolbar;

import butterknife.BindView;


public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    FSToolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        setSupportActionBar(mToolbar);
        switchFragment();
    }

    private void switchFragment() {
        CIFSFragment cifsFragment = createCIFSFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, cifsFragment);
        fragmentTransaction.commit();
    }

    private CIFSFragment createCIFSFragment() {
        CIFSFragment cifsFragment = new CIFSFragment();
        CIFSPresenter presenter = new CIFSPresenter(cifsFragment);
        cifsFragment.setPresenter(presenter);
        return cifsFragment;
    }
}
