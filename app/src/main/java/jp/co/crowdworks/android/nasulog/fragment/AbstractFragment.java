package jp.co.crowdworks.android.nasulog.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.crowdworks.android.nasulog.Prefs;

abstract class AbstractFragment extends Fragment {

    protected View mRootView;
    protected abstract @LayoutRes int getLayout();
    protected abstract void onSetupRootView();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(getLayout(), container, false);
        onSetupRootView();
        return mRootView;
    }

    protected void finish(){
        if(getFragmentManager().getBackStackEntryCount()==0){
            getActivity().finish();
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    protected String getServer(){
        Context context = getContext();
        if (context!=null) return Prefs.get(context).getString(Prefs.KEY_SERVER,null);
        else return "";
    }

    protected String getBaseURL(){
        return "http://"+getServer();
    }

}
