package jp.co.crowdworks.android.nasulog.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {
    @PrimaryKey
    private long id;
    private String name;
    private String image;

    public long getId(){ return id; }
    public String getName() { return name; }
    public String getImage() { return image; }

    public void setId(long id){ this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setImage(String image) { this.image = image; }
}
