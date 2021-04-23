package com.openclassrooms.go4lunch.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

public class ConnectionService extends Service {

    public static final String UI_AUTHENTICATED = "com.openclass.go4unch.uiauthenticated";

    public static XmppConnection.ConnectionState sConnectionState;
    public static XmppConnection.LoggedInState sLoggedInState;
    private boolean mActive;
    private Thread mThread;
    private Handler mHandler;
    private XmppConnection mConnection;

    public ConnectionService() {
        //need an empty constructor
    }

    public static XmppConnection.ConnectionState getState() {
        if (sConnectionState == null) {
            return XmppConnection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static XmppConnection.LoggedInState getLoggedInState() {
        if (sLoggedInState == null) {
            return XmppConnection.LoggedInState.LOGGED_OUT;
        }
        return sLoggedInState;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void start() {
        if (!mActive) {
            mActive = true;
            if (mThread == null || !mThread.isAlive()) {
                mThread = new Thread(() -> {
                    Looper.prepare();
                    mHandler = new Handler();
                    initConnection();

                    Looper.loop();
                });
                mThread.start();
            }
        }
    }

    public void stop() {
        mActive = false;
        mHandler.post(() -> {
            if (mConnection != null) {
                mConnection.disconnect();
            } else {

            }
        });
    }


    private void initConnection() {
        if (mConnection == null) {
            mConnection = new XmppConnection(this);
        }

        try {
            Log.d("BootB", "connection");
            mConnection.connect();
        } catch (InterruptedException | IOException | SmackException | XMPPException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
        start();
        return Service.START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_NONE)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
}
