package jp.co.crowdworks.android.nasulog.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import hugo.weaving.DebugLog;
import jp.co.crowdworks.android.nasulog.Prefs;
import jp.co.crowdworks.android.nasulog.service.observer.LoadPoemReceiver;
import jp.co.crowdworks.android.nasulog.service.observer.LoadUserReceiver;
import jp.co.crowdworks.android.nasulog.service.observer.Registerable;
import jp.co.crowdworks.android.nasulog.service.observer.CreatePoemObserver;

public class NasulogAPIService extends Service {
    private static final String TAG = NasulogAPIService.class.getName();

    private NasulogAPI mAPI;

    public static void keepalive(Context context) {
        context.startService(new Intent(context, NasulogAPIService.class));
    }
    public static void kill(Context context) {
        context.stopService(new Intent(context, NasulogAPIService.class));
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();

        String server = Prefs.get(getBaseContext()).getString(Prefs.KEY_SERVER,"");
        String token = Prefs.get(getBaseContext()).getString(Prefs.KEY_TOKEN,"");
        if(TextUtils.isEmpty(server) || TextUtils.isEmpty(token)) {
            stopSelf();
            return;
        }

        mAPI = new NasulogAPI(server, token);

        registerListeners();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterListeners();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private static final Class[] REGISTERABLE_CLASSES = {
            LoadPoemReceiver.class
            , LoadUserReceiver.class
            , CreatePoemObserver.class
    };

    private final ArrayList<Registerable> mListeners = new ArrayList<>();

    private void registerListeners(){
        final Context context = getApplicationContext();
        for(Class clazz: REGISTERABLE_CLASSES){
            try {
                Constructor ctor = clazz.getConstructor(Context.class, NasulogAPI.class);
                Object obj = ctor.newInstance(context, mAPI);

                if(obj instanceof Registerable) {
                    Registerable l = (Registerable) obj;
                    l.register();
                    mListeners.add(l);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            }
        }
    }

    private void unregisterListeners(){
        Iterator<Registerable> it = mListeners.iterator();
        while(it.hasNext()){
            Registerable l = it.next();
            l.unregister();
            it.remove();
        }
    }
}
