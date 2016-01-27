package jp.co.crowdworks.android.nasulog.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import jp.co.crowdworks.android.nasulog.R;

abstract class AbstractFrameLayoutActivity extends AbstractActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.simple_framelayout);
    }

    protected void showFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout_container, f)
                .commit();
    }

    protected void showFragmentWithBackstack(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout_container, f)
                .addToBackStack(null)
                .commit();
    }
}
