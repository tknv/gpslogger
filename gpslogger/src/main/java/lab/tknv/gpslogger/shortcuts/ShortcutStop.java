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

package lab.tknv.gpslogger.shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import lab.tknv.gpslogger.GpsLoggingService;
import lab.tknv.gpslogger.common.IntentConstants;
import lab.tknv.gpslogger.common.events.CommandEvents;
import lab.tknv.gpslogger.common.slf4j.Logs;

import de.greenrobot.event.EventBus;

import org.slf4j.Logger;


public class ShortcutStop extends Activity {

    private static final Logger LOG = Logs.of(ShortcutStop.class);

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LOG.info("Shortcut - stop logging");
        EventBus.getDefault().post(new CommandEvents.RequestStartStop(false));

        Intent serviceIntent = new Intent(getApplicationContext(), GpsLoggingService.class);
        serviceIntent.putExtra(IntentConstants.IMMEDIATE_STOP, true);
        getApplicationContext().startService(serviceIntent);

        finish();

    }


}