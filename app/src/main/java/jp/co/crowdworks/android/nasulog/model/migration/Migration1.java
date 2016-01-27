package jp.co.crowdworks.android.nasulog.model.migration;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class Migration1 implements RealmMigration {
    @Override
    public void migrate(DynamicRealm dynamicRealm, long oldVersion, long newVersion) {
        RealmSchema schema = dynamicRealm.getSchema();
        if (oldVersion==0) {
            migrateTo1(schema);
            oldVersion++;
        }
    }

    private void migrateTo1(RealmSchema schema) {
        // do nothing
    }
}
