package com.openclassrooms.go4lunch.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.openclassrooms.go4lunch.models.Message;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface MessageDao {

    @Insert
    Completable insertMessage(Message message);

    @Query("SELECT * FROM message WHERE messageReceiver = :messageReceiver")
    Flowable<List<Message>> getMessagesConf(String messageReceiver);

}
