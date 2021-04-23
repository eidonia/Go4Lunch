/**
 * This broadcast is used for sending message.
 * It saves message to Database and send him to XmppConnection
 * There is a Worker to manage reconnection
 */

package com.openclassrooms.go4lunch.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.openclassrooms.go4lunch.chatui.models.ChatMessage;
import com.openclassrooms.go4lunch.models.Message;
import com.openclassrooms.go4lunch.repository.ChatRepo;
import com.openclassrooms.go4lunch.utils.ConnectionService;
import com.openclassrooms.go4lunch.utils.XmppConnection;
import com.openclassrooms.go4lunch.worker.OfflineMessageWorker;
import com.openclassrooms.go4lunch.worker.ReconnectionWorker;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static androidx.work.WorkRequest.MIN_BACKOFF_MILLIS;
import static com.openclassrooms.go4lunch.utils.Constante.HOST;
import static com.openclassrooms.go4lunch.utils.Constante.SENT_MESSAGE;
import static com.openclassrooms.go4lunch.utils.Constante.SENT_MESSAGE_XMPP;

@AndroidEntryPoint
public class BroadcastMessage extends BroadcastReceiver {

    @Inject
    public ChatRepo repo;
    private WorkManager workmanager;

    @Override
    public void onReceive(Context context, Intent intent) {
        workmanager = WorkManager.getInstance(context);
        String action = intent.getAction();
        if (action.equals(SENT_MESSAGE)) {
            String body = intent.getStringExtra("sendMessage");
            String jid = intent.getStringExtra("sendJid");
            Log.d("messageSent", "" + body);
            long timeSent = intent.getLongExtra("timeSent", System.currentTimeMillis());
            repo.insertMessage(new Message(body, timeSent, ChatMessage.Type.SENT, jid));

            Log.d("broad", "" + ConnectionService.getState());
            if (ConnectionService.getState().equals(XmppConnection.ConnectionState.CONNECTED)) {
                String jidEjab = jid + "@" + HOST;
                Log.d("broad", "PasseLa");
                Intent sendIntent = new Intent();
                sendIntent.setAction(SENT_MESSAGE_XMPP);
                sendIntent.putExtra("messageToXmpp", body);
                sendIntent.putExtra("jidToXmpp", jidEjab);
                sendIntent.putExtra("timeSent", timeSent);
                context.sendBroadcast(sendIntent);
            } else {
                Log.d("broad", "Coucou");
                Data.Builder data = new Data.Builder();
                data.putString("message", body + "§§" + System.currentTimeMillis());
                data.putString("idSender", jid);

                OneTimeWorkRequest reconnection = new OneTimeWorkRequest.Builder(ReconnectionWorker.class)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .build();

                WorkContinuation continuation = workmanager.beginWith(reconnection);

                OneTimeWorkRequest tryToSendMessage = new OneTimeWorkRequest.Builder(OfflineMessageWorker.class)
                        .setInputData(data.build())
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .build();

                continuation = continuation.then(tryToSendMessage);

                continuation.enqueue();
            }
        }
    }
}

