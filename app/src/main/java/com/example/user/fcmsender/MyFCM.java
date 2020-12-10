package com.example.user.fcmsender;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.user.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFCM extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "notification";

        @Override
        public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Map<String, String> data = remoteMessage.getData();
            if (user.getUid().equals(data.get("for").toString())) {
                String version = remoteMessage.getData().get("value");


                createNotificationChannel();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_add)
                        .setContentTitle(data.get("title").toString())
                        .setContentText(data.get("body").toString())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
                managerCompat.notify(1, builder.build());
            }
            Log.e("LOG!", user.getUid());
            Log.e("LOG2",data.get("for").toString());
            Log.e("LOG3",data.get("title").toString());
            Log.e("LOG4",data.get("body").toString());

        }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
