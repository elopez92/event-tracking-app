package us.elopez.projecttwo.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import us.elopez.projecttwo.AppDatabase;
import us.elopez.projecttwo.RegistrationCallback;
import us.elopez.projecttwo.UserDao;
import us.elopez.projecttwo.data.model.UserEntity;
import us.elopez.projecttwo.util.SecurityUtil;

public class UserViewModel extends ViewModel {
    private final UserDao userDao;

    public UserViewModel(AppDatabase database) {
        userDao = database.userDao();
    }

    public LiveData<UserEntity> login(String username, String password) {
        return userDao.login(username, SecurityUtil.hashPassword(password));
    }

    public void registerUser(UserEntity user, RegistrationCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity existingUser = userDao.getUserByUsername(user.username).getValue(); // Use a direct (non-LiveData) query

            if (existingUser == null) {
                user.password = SecurityUtil.hashPassword(user.password);
                userDao.insertUser(user);

                // Run success callback on the main thread
                new Handler(Looper.getMainLooper()).post(callback::onSuccess);
            } else {
                // Run failure callback on the main thread
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Username already taken"));
            }
        });
    }

    public LiveData<UserEntity> getUser(String username) {
        return userDao.getUserByUsername(username);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AppDatabase database;

        public Factory(AppDatabase db) {
            database = db;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new UserViewModel(database);
        }
    }

    public void observeOnce(LiveData<UserEntity> liveData, Observer<UserEntity> observer) {
        liveData.observeForever(new Observer<UserEntity>() {
            @Override
            public void onChanged(UserEntity user) {
                liveData.removeObserver(this); // Remove observer immediately after receiving response
                observer.onChanged(user); // Pass result to caller
            }
        });
    }

}
