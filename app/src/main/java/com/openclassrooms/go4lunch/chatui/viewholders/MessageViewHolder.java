package com.openclassrooms.go4lunch.chatui.viewholders;

import android.content.Context;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.chatui.views.MessageView;

/**
 * View Holder for the Chat UI. Interfaces with the Received and Sent views and sets them up
 * with any messages required.
 * <p>
 * Original Code by Timi
 * Extended by James Lendrem, Michael Obi, Samuel Ojo
 */

public class MessageViewHolder {

    public final int STATUS_SENT = 0;
    public final int STATUS_RECEIVED = 1;
    private final MessageView messageView;
    private final int backgroundRcv;
    private final int backgroundSend;
    private final int bubbleBackgroundRcv;
    private final int bubbleBackgroundSend;
    View row;
    Context context;

    public MessageViewHolder(View convertView, int backgroundRcv, int backgroundSend, int bubbleBackgroundRcv, int bubbleBackgroundSend) {
        row = convertView;
        context = row.getContext();
        messageView = (MessageView) convertView;
        this.backgroundRcv = backgroundRcv;
        this.backgroundSend = backgroundSend;
        this.bubbleBackgroundSend = bubbleBackgroundSend;
        this.bubbleBackgroundRcv = bubbleBackgroundRcv;
    }

    public void setMessage(String message) {

        messageView.setMessage(message);

    }

    public void setTimestamp(String timestamp) {

        messageView.setTimestamp(timestamp);

    }

    public void setElevation(float elevation) {

        messageView.setElevation(elevation);

    }

    public void setSender(String sender) {
        messageView.setSender(sender);
    }

    public void setBackground(int messageType) {

        int chatMessageBackground = ContextCompat.getColor(context, R.color.cardview_light_background);
        int bubbleBackground = ContextCompat.getColor(context, R.color.cardview_light_background);

        switch (messageType) {
            case STATUS_RECEIVED:
                chatMessageBackground = backgroundRcv;
                bubbleBackground = bubbleBackgroundRcv;
                break;
            case STATUS_SENT:
                chatMessageBackground = backgroundSend;
                bubbleBackground = bubbleBackgroundSend;
                break;
        }

        messageView.setBackgroundColor(chatMessageBackground);
        messageView.setBackground(bubbleBackground);

    }

}