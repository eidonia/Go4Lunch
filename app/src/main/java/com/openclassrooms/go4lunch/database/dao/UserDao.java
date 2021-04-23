package com.openclassrooms.go4lunch.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.openclassrooms.go4lunch.models.User;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(User user);

    @Query("SELECT * FROM user")
    Flowable<List<User>> getAllUsers();

    @Query("UPDATE user SET status = :status WHERE ejabberdName = :ejabberdName")
    Completable updateStatus(boolean status, String ejabberdName);
}
