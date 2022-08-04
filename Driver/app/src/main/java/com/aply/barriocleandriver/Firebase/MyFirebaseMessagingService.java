package com.aply.barriocleandriver.Firebase;

import androidx.annotation.NonNull;

import com.aply.barriocleandriver.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    Utils utils = new Utils();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String body = remoteMessage.getData().get("body");
        String title = remoteMessage.getData().get("title");
        utils.showNotification(getApplicationContext(), body, title);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
