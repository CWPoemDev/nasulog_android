package jp.co.crowdworks.android.nasulog;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import jp.co.crowdworks.android.nasulog.model.migration.Migration3;

public class NasulogApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name("default.realm")
                .migration(new Migration3())
                .schemaVersion(3)
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
