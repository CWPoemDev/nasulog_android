package jp.co.crowdworks.android.nasulog.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Poem extends RealmObject {
    @PrimaryKey
    private long id;
    private String title;
    private String description;
    private User author;
    private RealmList<User> read_users;
    private Date created_at;

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public User getAuthor() { return author; }
    public RealmList<User> getRead_users() { return read_users; }
    public Date getCreated_at() { return created_at; }

    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAuthor(User author) { this.author = author; }
    public void setRead_users(RealmList<User> read_users) { this.read_users = read_users; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }
}
