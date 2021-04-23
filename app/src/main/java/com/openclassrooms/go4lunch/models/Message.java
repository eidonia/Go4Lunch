package com.openclassrooms.go4lunch.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.openclassrooms.go4lunch.chatui.models.ChatMessage;

@Entity
public class Message extends ChatMessage {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;
    private String messageReceiver;

    public Message(String message, long timestamp, Type type, String messageReceiver) {
        super(message, timestamp, type);
        this.messageReceiver = messageReceiver;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessageReceiver() {
        return messageReceiver;
    }

    public void setMessageReceiver(String messageReceiver) {
        this.messageReceiver = messageReceiver;
    }
}
