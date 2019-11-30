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

package lab.tknv.gpslogger.senders.ftp;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import lab.tknv.gpslogger.common.AppSettings;
import lab.tknv.gpslogger.common.network.Networks;
import lab.tknv.gpslogger.common.Strings;
import lab.tknv.gpslogger.common.events.UploadEvents;
import lab.tknv.gpslogger.common.slf4j.LoggingOutputStream;
import lab.tknv.gpslogger.common.slf4j.Logs;

import de.greenrobot.event.EventBus;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


public class FtpJob extends Job {

    private static final Logger LOG = Logs.of(FtpJob.class);

    String server;
    int port;
    String username;
    String password;
    boolean useFtps;
    String protocol;
    boolean implicit;
    File gpxFile;
    String fileName;
    String directory;

    static UploadEvents.Ftp jobResult;
    static ArrayList<String> ftpServerResponses;

    protected FtpJob(String server, int port, String username,
                     String password, String directory, boolean useFtps, String protocol, boolean implicit,
                     File gpxFile, String fileName) {
        super(new Params(1).requireNetwork().persist().addTags(getJobTag(gpxFile)));

        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.useFtps = useFtps;
        this.protocol = protocol;
        this.implicit = implicit;
        this.gpxFile = gpxFile;
        this.fileName = fileName;
        this.directory = directory;

        ftpServerResponses = new ArrayList<>();
        jobResult = null;

    }

    public synchronized static boolean upload(String server, String username, String password, String directory, int port,
                                              boolean useFtps, String protocol, boolean implicit,
                                              File gpxFile, String fileName) {
        FTPClient client;

        try {
            if (useFtps) {
                client = new FTPSClient(protocol, implicit);

                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(Networks.getKnownServersStore(AppSettings.getInstance()), null);
                KeyManager km = kmf.getKeyManagers()[0];

                ((FTPSClient) client).setKeyManager(km);
                ((FTPSClient) client).setTrustManager(Networks.getTrustManager(AppSettings.getInstance()));

            } else {
                client = new FTPClient();
            }

        } catch (Exception e) {
            jobResult = new UploadEvents.Ftp().failed( "Could not create FTP Client" , e);
            LOG.error("Could not create FTP Client", e);
            return false;
        }


        try {

            client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(new LoggingOutputStream(LOG))));
            client.setDefaultTimeout(60000);
            client.setConnectTimeout(60000);
            client.connect(server, port);
            client.setSoTimeout(60000);
            client.setDataTimeout(60000);
            logServerReply(client);


            if (client.login(username, password)) {

                if(useFtps){
                    ((FTPSClient)client).execPBSZ(0);
                    logServerReply(client);
                    ((FTPSClient)client).execPROT("P");
                    logServerReply(client);
                }


                client.enterLocalPassiveMode();
                logServerReply(client);

                LOG.debug("Uploading file to FTP server " + server);
                LOG.debug("Checking for FTP directory " + directory);
                FTPFile[] existingDirectory = client.listFiles(directory);
                logServerReply(client);

                if (existingDirectory.length <= 0) {
                    LOG.debug("Attempting to create FTP directory " + directory);
                    ftpCreateDirectoryTree(client, directory);
                    logServerReply(client);
                }

                FileInputStream inputStream = new FileInputStream(gpxFile);
                client.changeWorkingDirectory(directory);
                client.setFileType(FTP.BINARY_FILE_TYPE);
                boolean result = client.storeFile(fileName, inputStream);
                inputStream.close();
                logServerReply(client);
                if (result) {
                    LOG.debug("Successfully FTPd file " + fileName);
                } else {
                    jobResult = new UploadEvents.Ftp().failed( "Failed to FTP file " + fileName , null);
                    LOG.debug("Failed to FTP file " + fileName);
                    return false;
                }

            } else {
                logServerReply(client);
                jobResult = new UploadEvents.Ftp().failed( "Could not log in to FTP server" , null);
                LOG.debug("Could not log in to FTP server");
                return false;
            }

        } catch (Exception e) {
            logServerReply(client);
            jobResult = new UploadEvents.Ftp().failed( "Could not connect or upload to FTP server.", e);
            LOG.error("Could not connect or upload to FTP server.", e);
            return false;
        } finally {
            try {
                client.logout();
                logServerReply(client);

                client.disconnect();
                logServerReply(client);
            } catch (Exception e) {
                if(jobResult == null){
                    jobResult = new UploadEvents.Ftp().failed( "Could not logout or disconnect", e);
                }

                LOG.error("Could not logout or disconnect", e);
                return false;
            }
        }

        return true;
    }


    private static void ftpCreateDirectoryTree(FTPClient client, String dirTree) throws IOException {

        boolean dirExists = true;

        //tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
        String[] directories = dirTree.split("/");
        for (String dir : directories) {
            if (dir.length() > 0) {
                if (dirExists) {
                    dirExists = client.changeWorkingDirectory(dir);
                    logServerReply(client);
                }
                if (!dirExists) {
                    client.makeDirectory(dir);
                    logServerReply(client);
                    client.changeWorkingDirectory(dir);
                    logServerReply(client);
                }
            }
        }
    }


    private static void logServerReply(FTPClient client) {
        String singleReply = client.getReplyString();
        if(!Strings.isNullOrEmpty(singleReply)){
            ftpServerResponses.add(singleReply);
        }

        String[] replies = client.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                if(!Strings.isNullOrEmpty(aReply)){
                    ftpServerResponses.add(aReply);
                }
            }
        }
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        if (upload(server, username, password, directory, port, useFtps, protocol, implicit, gpxFile, fileName)) {
            LOG.info("FTP - file uploaded");
            EventBus.getDefault().post(new UploadEvents.Ftp().succeeded());
        } else {
            jobResult.ftpMessages = ftpServerResponses;
            EventBus.getDefault().post(jobResult);
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        EventBus.getDefault().post(new UploadEvents.Ftp().failed("Could not FTP file", throwable));
        LOG.error("Could not FTP file", throwable);
        return RetryConstraint.CANCEL;
    }

    public static String getJobTag(File testFile) {
        return "FTP"+testFile.getName();
    }
}
