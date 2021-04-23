/**
 * This worker is used for reconnecting to the server when user is reconnected.
 */

package com.openclassrooms.go4lunch.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.openclassrooms.go4lunch.utils.ConnectionService;
import com.openclassrooms.go4lunch.utils.XmppConnection;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class ReconnectionWorker extends Worker {

    private final Context context;
    private final XmppConnection xmppConnection;

    @AssistedInject
    public ReconnectionWorker(@Assisted Context context, @Assisted WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        xmppConnection = new XmppConnection(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!ConnectionService.getState().equals(XmppConnection.ConnectionState.CONNECTED)) {
            try {
                xmppConnection.connect();
            } catch (InterruptedException | SmackException | XMPPException | IOException e) {
                e.printStackTrace();
            }
            if (!ConnectionService.getState().equals(XmppConnection.ConnectionState.CONNECTED)) {
                return Result.retry();
            }

            return Result.success();
        }
        return Result.success();
    }
}
