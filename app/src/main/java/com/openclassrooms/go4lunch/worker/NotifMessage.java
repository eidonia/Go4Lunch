package com.openclassrooms.go4lunch.worker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.ui.restaurant.workmates.WorkmatesFragment;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;


@HiltWorker
public class NotifMessage extends Worker {

    private static final String CHANNEL_ID = "1";
    private FirebaseFirestore db;
    private Boolean isNotifActiv;
    private String mess;
    private String jid;

    @AssistedInject
    public NotifMessage(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void startWorker(Context context, Data.Builder data) {

        OneTimeWorkRequest test = new OneTimeWorkRequest.Builder(NotifMessage.class)
                .setInputData(data.build())
                .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(test);
    }

    @NonNull
    @Override
    public Result doWork() {
        isNotifActiv = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("isNotifActiv", true);
        if (isNotifActiv) {
            String message = getInputData().getString("message");
            String name = getInputData().getString("idSender");
            Log.d("messageReceived", "worker  name : " + name + "  " + message);
            createNotification(message, name);
        }
        return Result.success();
    }

    private void createNotification(String message, String name) {

        Intent intent = new Intent(getApplicationContext(), WorkmatesFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("ejabberdName", name)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        User snapUser = snapshot.toObject(User.class);

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                            NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                            Notification.Builder builder = new Notification.Builder(getApplicationContext());
                            builder.setContentTitle(snapUser.getName())
                                    .setContentText(message)
                                    .setStyle(new Notification.InboxStyle()
                                            .addLine(message)
                                            .setBigContentTitle(snapUser.getName())
                                    )
                                    .setSmallIcon(R.drawable.ic_launcher_background)
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent);
                            Notification n = builder.build();
                            nm.notify(1234, n);

                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationManager notificationManager;
                            notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
                            NotificationChannel notificationChannel;
                            notificationChannel = new NotificationChannel(CHANNEL_ID, "Go4Lunch", NotificationManager.IMPORTANCE_HIGH);
                            notificationChannel.setDescription(message);
                            notificationChannel.enableLights(true);
                            notificationChannel.setLightColor(Color.GREEN);
                            notificationChannel.enableVibration(false);
                            notificationManager.createNotificationChannel(notificationChannel);

                            Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                                    .setContentTitle(snapUser.getName())
                                    .setContentText(message)
                                    .setStyle(new Notification.InboxStyle()
                                            .addLine(message)
                                            .setBigContentTitle(snapUser.getName())
                                    )
                                    .setSmallIcon(R.drawable.ic_launcher_background)
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent);

                            notificationManager.notify(1234, builder.build());
                        }

                    }
                });
    }
}
