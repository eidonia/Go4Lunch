package com.openclassrooms.go4lunch.utils;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.openclassrooms.go4lunch.utils.Constante.NOTIF_FCM;

public class FirebaseCloudMessage extends FirebaseMessagingService {

    private Boolean isNotifActiv;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("testNotifFCM", "Blop");

        Intent intent = new Intent();
        intent.setAction(NOTIF_FCM);
        sendBroadcast(intent);
    }
}
