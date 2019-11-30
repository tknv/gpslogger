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

package lab.tknv.gpslogger.ui.fragments.settings;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.afollestad.materialdialogs.prefs.MaterialEditTextPreference;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import lab.tknv.gpslogger.GpsMainActivity;
import lab.tknv.gpslogger.R;
import lab.tknv.gpslogger.common.EventBusHook;
import lab.tknv.gpslogger.common.PreferenceHelper;
import lab.tknv.gpslogger.common.events.UploadEvents;
import lab.tknv.gpslogger.common.slf4j.Logs;
import lab.tknv.gpslogger.loggers.Files;
import lab.tknv.gpslogger.senders.googledrive.GoogleDriveManager;
import lab.tknv.gpslogger.ui.Dialogs;

import de.greenrobot.event.EventBus;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;


public class GoogleDriveSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {

    private static final Logger LOG = Logs.of(GoogleDriveSettingsFragment.class);
    private static PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();
    boolean messageShown = false;

    static final int REQUEST_CODE_MISSING_GPSF = 1;
    static final int REQUEST_CODE_ACCOUNT_PICKER = 2;
    static final int REQUEST_CODE_RECOVERED = 3;

    GoogleDriveManager manager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.gdocssettings);

        manager = new GoogleDriveManager(preferenceHelper);

        verifyGooglePlayServices();
        registerEventBus();
    }

    @Override
    public void onDestroy() {

        unregisterEventBus();
        super.onDestroy();
    }

    private void unregisterEventBus(){
        try {
            EventBus.getDefault().unregister(this);
        } catch (Throwable t){
            //this may crash if registration did not go through. just be safe
        }
    }
    private void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    private void verifyGooglePlayServices() {
        Preference resetPref = findPreference("gdocs_resetauth");
        Preference testPref = findPreference("gdocs_test");
        Preference folderPref = findPreference("gdocs_foldername");

        int availability = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        if (availability != ConnectionResult.SUCCESS) {
            resetPref.setEnabled(false);
            testPref.setEnabled(false);
            folderPref.setEnabled(false);

            if (!messageShown) {
                Dialog d = GooglePlayServicesUtil.getErrorDialog(availability, getActivity(), REQUEST_CODE_MISSING_GPSF);
                if (d != null) {
                    d.show();
                } else {
                    Dialogs.alert(getString(R.string.gpsf_missing), getString(R.string.gpsf_missing_description), getActivity());
                }
                messageShown = true;
            }

        } else {
            resetPreferenceAppearance(resetPref, testPref, folderPref);

            testPref.setOnPreferenceClickListener(this);
            resetPref.setOnPreferenceClickListener(this);
        }

    }


    public void onResume() {
        super.onResume();
        verifyGooglePlayServices();

    }

    private void resetPreferenceAppearance(Preference resetPref, Preference testPref, Preference folderPref) {
        if (manager.isLinked()) {
            resetPref.setTitle(R.string.osm_resetauth);
            resetPref.setSummary(R.string.gdocs_clearauthorization_summary);
            testPref.setEnabled(true);
            folderPref.setEnabled(true);
        } else {
            testPref.setEnabled(false);
            folderPref.setEnabled(false);
        }

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equalsIgnoreCase("gdocs_test")) {
            uploadTestFileToGoogleDocs();
        } else {
            if (manager.isLinked()) {
                new GoogleDriveClearAuth().execute();
            } else {
                //Re-authorize
                authorize();
            }
        }

        return true;
    }

    private void authorize() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE},
                false, null, null, null, null);

        startActivityForResult(intent, REQUEST_CODE_ACCOUNT_PICKER);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_CODE_ACCOUNT_PICKER:
                if (resultCode == getActivity().RESULT_OK) {
                    String accountName = data.getStringExtra(
                            AccountManager.KEY_ACCOUNT_NAME);

                    preferenceHelper.setGoogleDriveAccountName(accountName);

                    LOG.debug("Account:" + accountName);
                    getAndUseAuthTokenInAsyncTask();
                }
                break;
            case REQUEST_CODE_RECOVERED:
                if (resultCode == getActivity().RESULT_OK) {
                    getAndUseAuthTokenInAsyncTask();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // Example of how to use the GoogleAuthUtil in a blocking, non-main thread context
    String getAndUseAuthTokenBlocking() {
        try {
            // Retrieve a token for the given account and scope. It will always return either
            // a non-empty String or throw an exception.

            return GoogleAuthUtil.getToken(getActivity(), preferenceHelper.getGoogleDriveAccountName(), manager.getOauth2Scope());
        } catch (GooglePlayServicesAvailabilityException playEx) {
            Dialog alert = GooglePlayServicesUtil.getErrorDialog(
                    playEx.getConnectionStatusCode(),
                    getActivity(),
                    REQUEST_CODE_RECOVERED);
            alert.show();

        } catch (UserRecoverableAuthException userAuthEx) {
            // Start the user recoverable action using the intent returned by
            // getIntent()
            startActivityForResult(
                    userAuthEx.getIntent(),
                    REQUEST_CODE_RECOVERED);

        } catch (IOException transientEx) {
            LOG.error("Temporary failure", transientEx);
            // network or server error, the call is expected to succeed if you try again later.
            // Don't attempt to call again immediately - the request is likely to
            // fail, you'll hit quotas or back-off.


        } catch (GoogleAuthException authEx) {
            LOG.error("Authentication failure", authEx);
            // Failure. The call is not expected to ever succeed so it should not be
            // retried.

        }
        return null;
    }


    void getAndUseAuthTokenInAsyncTask() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return getAndUseAuthTokenBlocking();

            }

            @Override
            protected void onPostExecute(String authToken) {
                if (authToken != null) {
                    preferenceHelper.setGoogleDriveAuthToken(authToken);
                    LOG.debug("Auth token:" + authToken);
                    verifyGooglePlayServices();
                }

            }
        };
        task.execute();
    }


    private void uploadTestFileToGoogleDocs() {

        Dialogs.progress(getActivity(), getString(R.string.please_wait), getString(R.string.please_wait));

        try {
            File testFile = Files.createTestFile();
            MaterialEditTextPreference folderPref = (MaterialEditTextPreference)findPreference("gdocs_foldername");
            manager.uploadTestFile(testFile, folderPref.getText());

        } catch (Exception ex) {
            LOG.error("Could not create local test file", ex);
            EventBus.getDefault().post(new UploadEvents.GDrive().failed("Could not create local test file", ex));
        }
    }



    @EventBusHook
    public void onEventMainThread(UploadEvents.GDrive o){
        LOG.debug("GDrive Event completed, success: " + o.success);
        Dialogs.hideProgress();
        if(!o.success){
            Dialogs.error(getString(R.string.sorry), getString(R.string.upload_failure), o.message , o.throwable, getActivity());

        }
        else {
            Dialogs.alert(getString(R.string.success), getString(R.string.gdocs_testupload_success), getActivity());
        }
    }

    private class GoogleDriveClearAuth extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            //Clear authorization
            try {
                GoogleAuthUtil.clearToken(getActivity(), preferenceHelper.getGoogleDriveAuthToken());
            }
            catch(Exception e){
                LOG.error("Could not clear token", e);
            }
            preferenceHelper.setGoogleDriveAuthToken("");
            preferenceHelper.setGoogleDriveAccountName("");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            startActivity(new Intent(getActivity(), GpsMainActivity.class));
            getActivity().finish();

        }
    }

}