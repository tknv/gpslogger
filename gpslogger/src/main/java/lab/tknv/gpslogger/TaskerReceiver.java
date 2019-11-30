package lab.tknv.gpslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

import lab.tknv.gpslogger.common.slf4j.Logs;

import org.slf4j.Logger;

public class TaskerReceiver extends BroadcastReceiver {

    private static final Logger LOG = Logs.of(TaskerReceiver.class);
    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.info("Tasker Command Received");

        Intent serviceIntent = new Intent(context, GpsLoggingService.class);
        serviceIntent.putExtras(intent);
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
