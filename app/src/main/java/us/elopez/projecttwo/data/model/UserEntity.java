package us.elopez.projecttwo.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String username;
    public String password;

    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
