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

package lab.tknv.gpslogger.senders.owncloud;

import com.birbit.android.jobqueue.CancelResult;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import lab.tknv.gpslogger.common.AppSettings;
import lab.tknv.gpslogger.common.PreferenceHelper;
import lab.tknv.gpslogger.common.Strings;
import lab.tknv.gpslogger.common.events.UploadEvents;
import lab.tknv.gpslogger.common.slf4j.Logs;
import lab.tknv.gpslogger.loggers.Files;
import lab.tknv.gpslogger.senders.FileSender;
import lab.tknv.gpslogger.ui.fragments.settings.OwnCloudSettingsFragment;
import de.greenrobot.event.EventBus;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;

public class OwnCloudManager extends FileSender
{
    private static final Logger LOG = Logs.of(OwnCloudSettingsFragment.class);
    private PreferenceHelper preferenceHelper;

    public OwnCloudManager(PreferenceHelper preferenceHelper) {
        this.preferenceHelper = preferenceHelper;
    }

    public void testOwnCloud(final String servername, final String username, final String password, final String directory) {



        try {
            final File testFile = Files.createTestFile();
            final JobManager jobManager = AppSettings.getJobManager();
            jobManager.cancelJobsInBackground(new CancelResult.AsyncCancelCallback() {
                @Override
                public void onCancelled(CancelResult cancelResult) {
                    jobManager.addJobInBackground(new OwnCloudJob(servername, username, password, directory,
                            testFile, testFile.getName()));
                }
            }, TagConstraint.ANY, OwnCloudJob.getJobTag(testFile));

        } catch (Exception ex) {
            EventBus.getDefault().post(new UploadEvents.Ftp().failed());
            LOG.error("Error while testing ownCloud upload: " + ex.getMessage());
        }



        LOG.debug("Added background ownCloud upload job");
    }

    public static boolean validSettings(
            String servername,
            String username,
            String password,
            String directory) {
        return !Strings.isNullOrEmpty(servername);

    }

    @Override
    public void uploadFile(List<File> files)
    {
        for (File f : files) {
            uploadFile(f);
        }
    }

    @Override
    public boolean isAvailable() {
        return validSettings(preferenceHelper.getOwnCloudServerName(),
                preferenceHelper.getOwnCloudUsername(),
                preferenceHelper.getOwnCloudPassword(),
                preferenceHelper.getOwnCloudDirectory());
    }

    @Override
    public boolean hasUserAllowedAutoSending() {
        return preferenceHelper.isOwnCloudAutoSendEnabled();
    }

    public void uploadFile(final File f)
    {
        final JobManager jobManager = AppSettings.getJobManager();
        jobManager.cancelJobsInBackground(new CancelResult.AsyncCancelCallback() {
            @Override
            public void onCancelled(CancelResult cancelResult) {
                jobManager.addJobInBackground(new OwnCloudJob(
                        preferenceHelper.getOwnCloudServerName(),
                        preferenceHelper.getOwnCloudUsername(),
                        preferenceHelper.getOwnCloudPassword(),
                        preferenceHelper.getOwnCloudDirectory(),
                        f, f.getName()));
            }
        }, TagConstraint.ANY, OwnCloudJob.getJobTag(f));

    }

    @Override
    public boolean accept(File dir, String name) {
        return true;
    }


}