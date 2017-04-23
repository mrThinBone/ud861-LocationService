package vinhtv.android.app.ud861;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Created by DELL-INSPIRON on 4/23/2017.
 */

public class ActivitiesRecognitionActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    TextView txtStatus;
    Button btnRequest, btnRemove;
    private GoogleApiClient googleApiClient;
    private ActivityDetectionBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        txtStatus = (TextView) findViewById(R.id.tv_status);
        btnRequest = (Button) findViewById(R.id.button_request);
        btnRemove = (Button) findViewById(R.id.button_remove);
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestActivityUpdates();
            }
        });
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeActivityUpdates();
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        broadcastReceiver = new ActivityDetectionBroadcastReceiver();
    }

    private void requestActivityUpdates() {
        if(!googleApiClient.isConnected()) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                googleApiClient,
                5000,
                getActivityPendingIntent()
        ).setResultCallback(this);
        btnRequest.setEnabled(false);
        btnRemove.setEnabled(true);
    }

    private void removeActivityUpdates() {
        if(!googleApiClient.isConnected()) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                googleApiClient,
                getActivityPendingIntent()
        ).setResultCallback(this);
        btnRequest.setEnabled(true);
        btnRemove.setEnabled(false);
    }

    private PendingIntent getActivityPendingIntent() {
        Intent intent = new Intent(this, ActivitiesRecognitionService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {}

    @Override
    public void onConnectionSuspended(int cause) {
//        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onResult(@NonNull Status status) {
        if(status.isSuccess()) {
            Log.d("", "Successfully added activity detection");
        } else {
            Log.d("", "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> updatedActivities = intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);

            StringBuilder strStatus = new StringBuilder("");
            for (DetectedActivity activity : updatedActivities) {
                strStatus.append(getActivityString(activity.getType())).append(activity.getConfidence()).append("%\n");
            }
            txtStatus.setText(strStatus.toString());
        }
    }

    String getActivityString(int detectedActivityType) {
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return getString(R.string.running);
            case DetectedActivity.STILL:
                return getString(R.string.still);
            case DetectedActivity.TILTING:
                return getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return getString(R.string.walking);
            default:
                return getString(R.string.unidentifiable_activity);
        }
    }
}
