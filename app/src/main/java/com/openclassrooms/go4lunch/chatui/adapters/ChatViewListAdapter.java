package com.openclassrooms.go4lunch.chatui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.openclassrooms.go4lunch.chatui.models.ChatMessage;
import com.openclassrooms.go4lunch.chatui.viewholders.MessageViewHolder;
import com.openclassrooms.go4lunch.chatui.views.ViewBuilder;
import com.openclassrooms.go4lunch.chatui.views.ViewBuilderInterface;

import java.util.ArrayList;

/**
 * List Adapter for use in the recycler view to display messages using the Message View Holder
 * <p>
 * Created by Timi
 * Extended by James Lendrem, Samuel Ojo
 */

public class ChatViewListAdapter extends BaseAdapter {

    public final int STATUS_SENT = 0;
    public final int STATUS_RECEIVED = 1;
    private final int backgroundRcv;
    private final int backgroundSend;
    private final int bubbleBackgroundRcv;
    private final int bubbleBackgroundSend;
    private final float bubbleElevation;
    ArrayList<ChatMessage> chatMessages;
    Context context;
    LayoutInflater inflater;
    private ViewBuilderInterface viewBuilder = new ViewBuilder();

    public ChatViewListAdapter(Context context, ViewBuilderInterface viewBuilder, int backgroundRcv, int backgroundSend, int bubbleBackgroundRcv, int bubbleBackgroundSend, float bubbleElevation) {
        this.chatMessages = new ArrayList<>();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.backgroundRcv = backgroundRcv;
        this.backgroundSend = backgroundSend;
        this.bubbleBackgroundRcv = bubbleBackgroundRcv;
        this.bubbleBackgroundSend = bubbleBackgroundSend;
        this.bubbleElevation = bubbleElevation;
        this.viewBuilder = viewBuilder;
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getType().ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case STATUS_SENT:
                    convertView = viewBuilder.buildSentView(context);
                    break;
                case STATUS_RECEIVED:
                    convertView = viewBuilder.buildRecvView(context);
                    break;
            }

            holder = new MessageViewHolder(convertView, backgroundRcv, backgroundSend, bubbleBackgroundRcv, bubbleBackgroundSend);
            convertView.setTag(holder);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
        }

        holder.setTimestamp(chatMessages.get(position).getFormattedTime());
        holder.setElevation(bubbleElevation);
        holder.setMessage(chatMessages.get(position).getMessage());
        holder.setBackground(type);
        String sender = chatMessages.get(position).getSender();
        if (sender != null && type == STATUS_RECEIVED) {
            Log.d("sender", "" + sender);
            holder.setSender(sender.split("@")[0].toUpperCase());
        }

        return convertView;
    }

    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyDataSetChanged();
    }

    public void addMessages(ArrayList<ChatMessage> chatMessages) {
        this.chatMessages.addAll(chatMessages);
        notifyDataSetChanged();
    }

    public void removeMessage(int position) {
        if (this.chatMessages.size() > position) {
            this.chatMessages.remove(position);
        }
    }

    public void clearMessages() {
        this.chatMessages.clear();
        notifyDataSetChanged();
    }
}