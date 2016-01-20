package jp.co.crowdworks.android.nasulog.model.migration;

import io.realm.DynamicRealm;
import io.realm.RealmSchema;
import jp.co.crowdworks.android.nasulog.model.Poem;

public class Migration3 extends Migration2 {
    @Override
    public void migrate(DynamicRealm dynamicRealm, long oldVersion, long newVersion) {
        super.migrate(dynamicRealm, oldVersion, newVersion);

        RealmSchema schema = dynamicRealm.getSchema();
        if (oldVersion == 2) {
            migrateTo3(schema);
            oldVersion++;
        }
    }

    private void migrateTo3(RealmSchema schema) {
        schema.get("Poem").addField("syncstate", Integer.class).transform(obj -> {
            obj.setInt("syncstate", Poem.SYNCSTATE_SYNCED);
        });
    }
}
