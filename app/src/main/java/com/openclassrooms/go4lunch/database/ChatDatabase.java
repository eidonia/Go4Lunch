package com.openclassrooms.go4lunch.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.openclassrooms.go4lunch.database.dao.MessageDao;
import com.openclassrooms.go4lunch.database.dao.UserDao;
import com.openclassrooms.go4lunch.models.Message;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.utils.Converters;

@Database(entities = {Message.class, User.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class ChatDatabase extends RoomDatabase {

    public abstract MessageDao messageDao();

    public abstract UserDao userDao();


}
