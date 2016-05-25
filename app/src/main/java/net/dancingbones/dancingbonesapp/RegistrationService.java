package net.dancingbones.dancingbonesapp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by Clif on 5/25/2016.
 */
public class RegistrationService extends IntentService {
    public RegistrationService() {
        super("RegistrationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID myID = InstanceID.getInstance(this);
        String registrationToken = null;
        try {
            registrationToken = myID.getToken(
                    getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                    null
            );
            Log.d("Registration Token", registrationToken);
        } catch (IOException e) {
            Log.d("Registration Token", registrationToken);
            e.printStackTrace();
        }
        GcmPubSub subscription = GcmPubSub.getInstance(this);
        try {
            subscription.subscribe(registrationToken, "/topics/dbnews_topic", null);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
