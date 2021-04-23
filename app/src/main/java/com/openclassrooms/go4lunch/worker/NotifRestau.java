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
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.ui.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static void startWorker(Context context) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String thisTime = sdf.format(new Date());
        long oneDaySeconds = 86400;

        Date dateNow = sdf.parse(thisTime);
        Date dateNotif = sdf.parse("13:16:45");
        Log.d("testHourNotif", "dateNow : " + dateNow.getTime() + " -  dateNotif : " + dateNotif.getTime());
        long diff = dateNotif.getTime() - dateNow.getTime();
        diff = TimeUnit.MILLISECONDS.toSeconds(diff);

        if (diff < 0) {
            diff = oneDaySeconds + diff;
        }
        Log.d("testHourNotif", " " + diff);

        /*OneTimeWorkRequest test = new OneTimeWorkRequest.Builder(NotifRestau.class)
                .setInitialDelay(30, TimeUnit.SECONDS)
                .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(test);*/

        PeriodicWorkRequest Worker = new PeriodicWorkRequest.Builder(NotifRestau.class, 24, TimeUnit.HOURS)
                .setInitialDelay(diff, TimeUnit.SECONDS)
                .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(Worker);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("testHourNotif", "doWork");
        isNotifActiv = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("isNotifActiv", true);
        if (isNotifActiv) {
            createNotification();
        }
        return Result.success();
    }

    private void createNotification() {
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

                    if (user.isRestauChoosen()) {
                        db.collection("restaurants").document(user.getThisDayRestau().getPlaceId())
                                .get()
                                .addOnSuccessListener(documentSnapshot1 -> {
                                    Restaurant restaurant = documentSnapshot1.toObject(Restaurant.class);
                                    String restau = user.getThisDayRestau().getName();
                                    String textRestau = user.getThisDayRestau().getVicinity();
                                    String pplRestau = getApplicationContext().getString(R.string.inviteWorkmates);

                                    if (restaurant.getListUser().size() > 1) {
                                        pplRestau = getApplicationContext().getString(R.string.eatWith);
                                        for (User userEat : restaurant.getListUser()) {
                                            Log.d("notifEat", "la");
                                            if (userEat.getEmail().equals(user.getEmail())) {
                                                continue;
                                            }
                                            if (pplRestau.equals(getApplicationContext().getString(R.string.eatWith))) {
                                                pplRestau += userEat.getName();
                                            } else {
                                                pplRestau += ", " + userEat.getName();
                                            }
                                        }
                                    }

                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                                        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                                        Notification.Builder builder = new Notification.Builder(getApplicationContext());
                                        builder.setContentTitle(restau)
                                                .setContentText(textRestau)
                                                .setStyle(new Notification.InboxStyle()
                                                        .addLine(textRestau)
                                                        .addLine(pplRestau)
                                                        .setBigContentTitle(restau)
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
                                        notificationChannel.setDescription(textRestau);
                                        notificationChannel.enableLights(true);
                                        notificationChannel.setLightColor(Color.GREEN);
                                        notificationChannel.enableVibration(false);
                                        notificationManager.createNotificationChannel(notificationChannel);

                                        Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
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
                                    }
                                });

                    }


                });
    }
}
