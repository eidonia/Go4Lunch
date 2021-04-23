package com.openclassrooms.go4lunch.utils;

import androidx.room.TypeConverter;

import com.openclassrooms.go4lunch.chatui.models.ChatMessage;

public class Converters {

    @TypeConverter
    public int fromType(ChatMessage.Type type) {
        return type.ordinal();
    }

    @TypeConverter
    public ChatMessage.Type toType(int type) {
        ChatMessage.Type finalType;
        if (type == 0) {
            finalType = ChatMessage.Type.SENT;
        } else {
            finalType = ChatMessage.Type.RECEIVED;
        }
        return finalType;
    }
}
