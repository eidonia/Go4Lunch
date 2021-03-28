/**
 * This broadcast is called when user receives a message.
 * It saves message to Database and refresh ChatActivity
 * If application is in background, an intent send the message to the SnackBar.
 */

package com.openclassrooms.go4lunch.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;

import com.openclassrooms.go4lunch.chatui.models.ChatMessage;
import com.openclassrooms.go4lunch.models.Message;
import com.openclassrooms.go4lunch.repository.ChatRepo;
import com.openclassrooms.go4lunch.ui.restaurant.workmates.UserListAdapter;
import com.openclassrooms.go4lunch.worker.NotifMessage;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static com.openclassrooms.go4lunch.utils.Constante.LFRAG_ADA;
import static com.openclassrooms.go4lunch.utils.Constante.RECEIVED_MESSAGE;

@AndroidEntryPoint
public class ReceivedMessage extends BroadcastReceiver {

    @Inject
    public ChatRepo repo;
    private Long timeSent;
    private String bodyCut;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(RECEIVED_MESSAGE)) {
            Log.d("ReceivedMessage", "messageRecu");
            String jid = intent.getStringExtra("receiveJid");
            String name = jid.split("@")[0];
            String body = intent.getStringExtra("receivedMessage");
            timeSent = System.currentTimeMillis();
            bodyCut = body;

            Log.d("messageReceived", "name : " + name + "  " + body);

            if (body.contains("§§") && messageContainsTime(body)) {
                bodyCut = body.split("§§")[0];
                timeSent = Long.parseLong(body.split("§§")[1]);
            }

            Data.Builder data = new Data.Builder();
            data.putString("message", bodyCut);
            data.putString("idSender", name);


            repo.insertMessage(new Message(bodyCut, timeSent, ChatMessage.Type.RECEIVED, name));
            UserListAdapter userListAdapter = new UserListAdapter(context, LFRAG_ADA);
            Log.d("TestEvent", "broadcast " + name);

            repo.updateStatusMessage(true, name);
            NotifMessage.startWorker(context, data);
        }
    }

    private boolean messageContainsTime(String messagebody) {
        try {
            Long.parseLong(messagebody.split("§§")[1]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
