package jp.co.crowdworks.android.nasulog.activity;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import jp.co.crowdworks.android.nasulog.R;
import jp.co.crowdworks.android.nasulog.helper.OnBackPressListener;

abstract class AbstractActivity extends AppCompatActivity {

    @Override
    public void onBackPressed(){
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.simple_framelayout_container);
        if(f instanceof OnBackPressListener &&
                ((OnBackPressListener) f).onBackPressed()){
            //consumed. do nothing.
        }
        else super.onBackPressed();
    }
}
