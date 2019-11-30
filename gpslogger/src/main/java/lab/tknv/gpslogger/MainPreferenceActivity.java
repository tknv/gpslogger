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

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import lab.tknv.gpslogger.R;
import lab.tknv.gpslogger.common.PreferenceHelper;
import lab.tknv.gpslogger.common.Systems;
import lab.tknv.gpslogger.common.slf4j.Logs;
import lab.tknv.gpslogger.senders.PreferenceValidator;
import lab.tknv.gpslogger.ui.Dialogs;
import lab.tknv.gpslogger.ui.fragments.settings.*;
import lab.tknv.gpslogger.ui.fragments.settings.AutoEmailFragment;
import lab.tknv.gpslogger.ui.fragments.settings.CustomUrlFragment;
import lab.tknv.gpslogger.ui.fragments.settings.DropboxAuthorizationFragment;
import lab.tknv.gpslogger.ui.fragments.settings.FtpFragment;
import lab.tknv.gpslogger.ui.fragments.settings.GeneralSettingsFragment;
import lab.tknv.gpslogger.ui.fragments.settings.GoogleDriveSettingsFragment;
import lab.tknv.gpslogger.ui.fragments.settings.LoggingSettingsFragment;
import lab.tknv.gpslogger.ui.fragments.settings.OSMAuthorizationFragment;
import lab.tknv.gpslogger.ui.fragments.settings.OpenGTSFragment;
import lab.tknv.gpslogger.ui.fragments.settings.OwnCloudSettingsFragment;
import lab.tknv.gpslogger.ui.fragments.settings.PerformanceSettingsFragment;
import lab.tknv.gpslogger.ui.fragments.settings.SFTPSettingsFragment;
import lab.tknv.gpslogger.ui.fragments.settings.UploadSettingsFragment;

import org.slf4j.Logger;


public class MainPreferenceActivity extends AppCompatActivity {

    private static final Logger LOG = Logs.of(MainPreferenceActivity.class);

    PreferenceFragment preferenceFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Systems.setLocale(PreferenceHelper.getInstance().getUserSpecifiedLocale(),getBaseContext(),getResources());
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }





        String whichFragment = PREFERENCE_FRAGMENTS.GENERAL;

        if(getIntent().getExtras() != null) {
           whichFragment = getIntent().getExtras().getString("preference_fragment");
        }

        //If OpenStreetMap has returned with OAuth token
        if(getIntent().getData() != null) {
            LOG.debug("OSM Authorization returned: " + getIntent().getData().getQuery());
            whichFragment = PREFERENCE_FRAGMENTS.OSM;
        }

        switch(whichFragment){
            case PREFERENCE_FRAGMENTS.GENERAL:
                setTitle(R.string.settings_screen_name);
                preferenceFragment = new GeneralSettingsFragment();
                break;
            case PREFERENCE_FRAGMENTS.LOGGING:
                setTitle(R.string.pref_logging_title);
                preferenceFragment = new LoggingSettingsFragment();
                break;
            case PREFERENCE_FRAGMENTS.PERFORMANCE:
                setTitle(R.string.pref_performance_title);
                preferenceFragment = new PerformanceSettingsFragment();
                break;
            case PREFERENCE_FRAGMENTS.UPLOAD:
                setTitle(R.string.title_drawer_uploadsettings);
                preferenceFragment = new UploadSettingsFragment();
                break;
            case PREFERENCE_FRAGMENTS.FTP:
                setTitle(R.string.autoftp_setup_title);
                preferenceFragment = new FtpFragment();
                break;
            case PREFERENCE_FRAGMENTS.EMAIL:
                setTitle(R.string.autoemail_title);
                preferenceFragment = new AutoEmailFragment();
                break;
            case PREFERENCE_FRAGMENTS.OPENGTS:
                setTitle(R.string.opengts_setup_title);
                preferenceFragment = new OpenGTSFragment();
                break;
            case PREFERENCE_FRAGMENTS.CUSTOMURL:
                setTitle(R.string.log_customurl_title);
                preferenceFragment = new CustomUrlFragment();
                break;
            case PREFERENCE_FRAGMENTS.GDOCS:
                setTitle(R.string.gdocs_setup_title);
                preferenceFragment = new GoogleDriveSettingsFragment();
                break;
            case PREFERENCE_FRAGMENTS.DROPBOX:
                setTitle(R.string.dropbox_setup_title);
                preferenceFragment = new DropboxAuthorizationFragment();
                break;
            case PREFERENCE_FRAGMENTS.OSM:
                setTitle(R.string.osm_setup_title);
                preferenceFragment = new OSMAuthorizationFragment();
                break;
            case PREFERENCE_FRAGMENTS.OWNCLOUD:
                setTitle(R.string.owncloud_setup_title);
                preferenceFragment = new OwnCloudSettingsFragment();
                break;
            case PREFERENCE_FRAGMENTS.SFTP:
                setTitle(R.string.sftp_setup_title);
                preferenceFragment = new SFTPSettingsFragment();
                break;
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, preferenceFragment)
                .commit();

    }


    @Override
    public void onBackPressed() {

       if(isFormValid()){
           super.onBackPressed();
       }
    }

    private boolean isFormValid(){
        if(preferenceFragment instanceof PreferenceValidator){
            if( !((PreferenceValidator)preferenceFragment).isValid() ){
                Dialogs.alert(getString(R.string.autoftp_invalid_settings),
                        getString(R.string.autoftp_invalid_summary),
                        this);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();
        return id == android.R.id.home && !isFormValid();

    }

    public static class PREFERENCE_FRAGMENTS {
        public static final String GENERAL = "GeneralSettingsFragment";
        public static final String LOGGING = "LoggingSettingsFragment";
        public static final String PERFORMANCE = "PerformanceSettingsFragment";
        public static final String UPLOAD = "UploadSettingsFragment";
        public static final String FTP = "FtpFragment";
        public static final String EMAIL = "AutoEmailFragment";
        public static final String OPENGTS = "OpenGTSFragment";
        public static final String CUSTOMURL = "CustomUrlFragment";
        public static final String GDOCS = "GoogleDriveSettingsFragment";
        public static final String DROPBOX = "DropBoxAuthorizationFragment";
        public static final String OWNCLOUD = "OwnCloudAuthorizationFragment";
        public static final String OSM = "OSMAuthorizationFragment";
        public static final String SFTP = "SFTPSettingsFragment";
    }

}
