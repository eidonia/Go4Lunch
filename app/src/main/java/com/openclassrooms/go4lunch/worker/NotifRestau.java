package com.openclassrooms.go4lunch.worker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.preference.PreferenceManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;


@HiltWorker
public class NotifRestau extends Worker {

    private static final String CHANNEL_ID = "1";
    private FirebaseFirestore db;
    private Boolean isNotifActiv;

    @AssistedInject
    public NotifRestau(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void startWorker(Context context) {
        Log.d("diffHour", "Coucou");
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String thisTime = sdf.format(cal.getTime());
        long oneDaySeconds = 86400;
        LocalTime start = LocalTime.parse(thisTime);
        LocalTime stop = LocalTime.parse("12:00:00");
        Duration d = Duration.between(start, stop);
        long diffHour = d.getSeconds();
        if (d.getSeconds() < 0) {
            diffHour = oneDaySeconds + d.getSeconds();
        }

        Log.d("diffHour", "" + diffHour);


        OneTimeWorkRequest test = new OneTimeWorkRequest.Builder(NotifRestau.class)
                .setInitialDelay(30, TimeUnit.SECONDS)
                .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(test);

        /*PeriodicWorkRequest test = new PeriodicWorkRequest.Builder(NotifRestau.class, 24, TimeUnit.HOURS)
                .setInitialDelay(diffHour, TimeUnit.SECONDS)
                .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(test);*/
    }

    @NonNull
    @Override
    public Result doWork() {
        isNotifActiv = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("isNotifActiv", true);
        Log.d("isNotifActiv", "" + isNotifActiv.toString());
        if (isNotifActiv) {
            createNotification();
        }
        return Result.success();
    }

    private void createNotification() {
        Log.d("testWorker", "Coucou test");
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

                    NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);

                    if (user.isRestauChoosen()) {
                        db.collection("restaurants").document(user.getThisDayRestau().getPlaceId())
                                .get()
                                .addOnSuccessListener(documentSnapshot1 -> {
                                    Restaurant restaurant = documentSnapshot1.toObject(Restaurant.class);
                                    Notification.Builder builder;
                                    String restau = user.getThisDayRestau().getName();
                                    String textRestau = user.getThisDayRestau().getVicinity();
                                    String pplRestau = "Invitez vos collègues à y manger !";

                                    if (restaurant.getListUser().size() > 0) {
                                        for (User userEat : user.getThisDayRestau().getListUser()) {
                                            if (pplRestau.equals("")) {
                                                pplRestau = userEat.getName();
                                            } else {
                                                pplRestau += ", " + userEat.getName();
                                            }
                                        }
                                    }

                                    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Go4Lunch", NotificationManager.IMPORTANCE_HIGH);

                                    notificationChannel.setDescription(textRestau);
                                    notificationChannel.enableLights(true);
                                    notificationChannel.setLightColor(Color.GREEN);
                                    notificationChannel.enableVibration(false);
                                    notificationManager.createNotificationChannel(notificationChannel);

                                    builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                                            .setContentTitle(restau)
                                            .setContentText(textRestau)
                                            .setStyle(new Notification.InboxStyle()
                                                    .addLine(textRestau)
                                                    .addLine(pplRestau)
                                                    .setBigContentTitle(restau)
                                            )
                                            .setSmallIcon(R.drawable.ic_launcher_background)
                                            .setAutoCancel(true)
                                            .setContentIntent(pendingIntent);

                                    notificationManager.notify(1234, builder.build());
                                });

                    }

                });
    }
}
