package jp.co.crowdworks.android.nasulog.model.migration;

import io.realm.DynamicRealm;
import io.realm.RealmSchema;

public class Migration4 extends Migration3 {
    @Override
    public void migrate(DynamicRealm dynamicRealm, long oldVersion, long newVersion) {
        super.migrate(dynamicRealm, oldVersion, newVersion);

        RealmSchema schema = dynamicRealm.getSchema();
        if (oldVersion == 3) {
            migrateTo4(schema);
            oldVersion++;
        }
    }

    private void migrateTo4(RealmSchema schema) {
        schema.get("Poem").addField("original_poem_id", Long.class);
    }
}
