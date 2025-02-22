package us.elopez.projecttwo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import us.elopez.projecttwo.data.model.UserEntity;
@Dao
public interface UserDao {
    @Insert
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    LiveData<UserEntity> login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username")
    LiveData<UserEntity> getUserByUsername(String username);
}
