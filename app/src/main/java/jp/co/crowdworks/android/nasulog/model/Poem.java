package jp.co.crowdworks.android.nasulog.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Poem extends RealmObject {
    public static final int SYNCSTATE_NOT_SYNCED = 0;
    public static final int SYNCSTATE_SYNCING = 1;
    public static final int SYNCSTATE_SYNCED = 2;
    public static final int SYNCSTATE_SYNC_FAILED = 3;

    @PrimaryKey
    private long id;
    private int syncstate = SYNCSTATE_SYNCED;
    private String title;
    private String description;
    private User author;
    private RealmList<User> read_users;
    private Date created_at;
    private long original_poem_id;

    public long getId() { return id; }
    public int getSyncstate() { return syncstate; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public User getAuthor() { return author; }
    public RealmList<User> getRead_users() { return read_users; }
    public Date getCreated_at() { return created_at; }
    public long getOriginal_poem_id() { return original_poem_id; }

    public void setId(long id) { this.id = id; }
    public void setSyncstate(int syncstate) { this.syncstate = syncstate; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAuthor(User author) { this.author = author; }
    public void setRead_users(RealmList<User> read_users) { this.read_users = read_users; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }
    public void setOriginal_poem_id(long original_poem_id) { this.original_poem_id = original_poem_id; }

    public static void setIdJson(JSONObject jsonPoem, long id) {
        try {
            jsonPoem.put("id", id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setOriginalPoemIdJson(JSONObject jsonPoem, long original_poem_id) {
        try {
            jsonPoem.put("original_poem_id", original_poem_id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setSyncstateJson(JSONObject jsonPoem, int syncstate) {
        try {
            jsonPoem.put("syncstate", syncstate);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
