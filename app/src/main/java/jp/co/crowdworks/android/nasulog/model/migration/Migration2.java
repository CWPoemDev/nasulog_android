package jp.co.crowdworks.android.nasulog.model.migration;

import io.realm.DynamicRealm;
import io.realm.RealmSchema;

public class Migration2 extends Migration1 {
    @Override
    public void migrate(DynamicRealm dynamicRealm, long oldVersion, long newVersion) {
        super.migrate(dynamicRealm, oldVersion, newVersion);

        RealmSchema schema = dynamicRealm.getSchema();
        if (oldVersion == 1) {
            migrateTo2(schema);
            oldVersion++;
        }
    }

    private void migrateTo2(RealmSchema schema) {
        schema.get("User").addField("email", String.class);
    }
}
