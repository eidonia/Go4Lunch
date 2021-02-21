package com.openclassrooms.go4lunch.worker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.User;

import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

public class ChosenRestWorker extends Worker {

    private static final String CHANNEL_ID = "1";
    private DatabaseReference refUser;


    @AssistedInject
    public ChosenRestWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void startWorker(Context context) {
        OneTimeWorkRequest test = new OneTimeWorkRequest.Builder(ChosenRestWorker.class)
                .setInitialDelay(15, TimeUnit.SECONDS)
                .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(test);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("testWorker", "Coucou test");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        refUser = FirebaseDatabase.getInstance().getReference().child("users");

        Query query = refUser.child(firebaseUser.getUid());

        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
                Notification.Builder builder;

                String restau = "Restaurant non choisi";
                String textRestau = "Veuillez choisir un restaurant";
                String pplRestau = "";

                if (user.getThisDayRestau() != null) {
                    restau = user.getThisDayRestau().getName();
                    textRestau = user.getThisDayRestau().getVicinity();
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
                                .addLine(pplRestau)
                                .setBigContentTitle(restau)
                        )
                        .setSmallIcon(R.drawable.ic_launcher_background);
                notificationManager.notify(1234, builder.build());
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

        return Result.success();
    }
}
