/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package lab.tknv.gpslogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.MaterialDialog;
import lab.tknv.gpslogger.R;
import lab.tknv.gpslogger.common.IntentConstants;
import lab.tknv.gpslogger.common.slf4j.Logs;
import lab.tknv.gpslogger.ui.Dialogs;

import org.slf4j.Logger;

public class NotificationAnnotationActivity extends AppCompatActivity {

    //Called from the 'annotate' button in the Notification
    //This in turn captures user input and sends the input to the GPS Logging Service

    private static final Logger LOG = Logs.of(NotificationAnnotationActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dialogs.autoCompleteText(NotificationAnnotationActivity.this, "annotations",
                getString(R.string.add_description), getString(R.string.letters_numbers), "",
                new Dialogs.AutoCompleteCallback() {
                    @Override
                    public void messageBoxResult(int which, MaterialDialog dialog, String enteredText) {
                        if(which == Dialogs.AutoCompleteCallback.OK){
                            LOG.info("Notification Annotation entered : " + enteredText);
                            Intent serviceIntent = new Intent(getApplicationContext(), GpsLoggingService.class);
                            serviceIntent.putExtra(IntentConstants.SET_DESCRIPTION, enteredText);
                            ContextCompat.startForegroundService(getApplicationContext(),  serviceIntent);
                        }

                        finish();
                    }
                });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            super.finish();
        }

        return super.onKeyDown(keyCode, event);
    }
}
