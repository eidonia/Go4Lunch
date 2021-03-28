package com.openclassrooms.go4lunch.broadcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.ui.MainActivity;

import static com.openclassrooms.go4lunch.utils.Constante.NOTIF_FCM;

public class BroadcastFCM extends BroadcastReceiver {

    private static final String CHANNEL_ID = "1";
    private FirebaseFirestore db;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(NOTIF_FCM)) {
            Log.d("testNotifFCM", "Blop");

            db = FirebaseFirestore.getInstance();
            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);

                        Intent notifIntent = new Intent(context, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifIntent, PendingIntent.FLAG_ONE_SHOT);

                        if (user.isRestauChoosen()) {
                            db.collection("restaurants").document(user.getThisDayRestau().getPlaceId())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot1 -> {
                                        Restaurant restaurant = documentSnapshot1.toObject(Restaurant.class);
                                        String restau = user.getThisDayRestau().getName();
                                        String textRestau = user.getThisDayRestau().getVicinity();
                                        String pplRestau = context.getString(R.string.inviteWorkmates);

                                        if (restaurant.getListUser().size() > 0) {
                                            for (User userEat : user.getThisDayRestau().getListUser()) {
                                                if (pplRestau.equals("")) {
                                                    pplRestau = userEat.getName();
                                                } else {
                                                    pplRestau += ", " + userEat.getName();
                                                }
                                            }
                                        }

                                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                                            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                            Notification.Builder builder = new Notification.Builder(context);
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
                                            notificationManager = context.getSystemService(NotificationManager.class);
                                            NotificationChannel notificationChannel;
                                            notificationChannel = new NotificationChannel(CHANNEL_ID, "Go4Lunch", NotificationManager.IMPORTANCE_HIGH);
                                            notificationChannel.setDescription(textRestau);
                                            notificationChannel.enableLights(true);
                                            notificationChannel.setLightColor(Color.GREEN);
                                            notificationChannel.enableVibration(false);
                                            notificationManager.createNotificationChannel(notificationChannel);

                                            Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
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
}
