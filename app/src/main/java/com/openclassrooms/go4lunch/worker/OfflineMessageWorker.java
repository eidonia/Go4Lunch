/**
 * This worker is used for sending message when user is reconnected.
 */

package com.openclassrooms.go4lunch.worker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.openclassrooms.go4lunch.utils.ConnectionService;
import com.openclassrooms.go4lunch.utils.XmppConnection;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

import static com.openclassrooms.go4lunch.utils.Constante.SENT_MESSAGE_XMPP;

@HiltWorker
public class OfflineMessageWorker extends Worker {

    private final Context context;

    @AssistedInject
    public OfflineMessageWorker(@Assisted Context context, @Assisted WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        String message = getInputData().getString("message");
        String idSender = getInputData().getString("idSender");

        Log.d("workercHAT", "Pas connecté");

        if (ConnectionService.getState().equals(XmppConnection.ConnectionState.CONNECTED)) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(SENT_MESSAGE_XMPP);
            sendIntent.putExtra("messageToXmpp", message);
            sendIntent.putExtra("jidToXmpp", idSender);
            context.sendBroadcast(sendIntent);
            return Result.success();
        }
        return Result.retry();
    }
}
