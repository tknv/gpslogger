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

package lab.tknv.gpslogger.senders;

import lab.tknv.gpslogger.common.PreferenceHelper;
import lab.tknv.gpslogger.common.slf4j.Logs;
import lab.tknv.gpslogger.loggers.Files;
import lab.tknv.gpslogger.senders.dropbox.DropBoxManager;
import lab.tknv.gpslogger.senders.email.AutoEmailManager;
import lab.tknv.gpslogger.senders.ftp.FtpManager;
import lab.tknv.gpslogger.senders.googledrive.GoogleDriveManager;
import lab.tknv.gpslogger.senders.opengts.OpenGTSManager;
import lab.tknv.gpslogger.senders.osm.OpenStreetMapManager;
import lab.tknv.gpslogger.senders.owncloud.OwnCloudManager;
import lab.tknv.gpslogger.senders.sftp.SFTPManager;

import org.slf4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileSenderFactory {

    private static final Logger LOG = Logs.of(FileSenderFactory.class);


    public static FileSender getOsmSender() {
        return new OpenStreetMapManager(PreferenceHelper.getInstance());
    }

    public static FileSender getDropBoxSender() {
        return new DropBoxManager(PreferenceHelper.getInstance());
    }

    public static FileSender getGoogleDriveSender() {
        return new GoogleDriveManager(PreferenceHelper.getInstance());
    }

    public static FileSender getEmailSender() {
        return new AutoEmailManager(PreferenceHelper.getInstance());
    }

    public static FileSender getOpenGTSSender() {
        return new OpenGTSManager(PreferenceHelper.getInstance());
    }

    public static FileSender getFtpSender() {
        return new FtpManager(PreferenceHelper.getInstance());
    }

    public static FileSender getOwnCloudSender() {
        return new OwnCloudManager(PreferenceHelper.getInstance());
    }

    public static FileSender getSFTPSender() {
        return new SFTPManager(PreferenceHelper.getInstance());
    }

    public static void autoSendFiles(final String fileToSend) {

        PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();
        LOG.info("Auto-sending file " + fileToSend);

        File gpxFolder = new File(preferenceHelper.getGpsLoggerFolder());

        if (Files.fromFolder(gpxFolder).length < 1) {
            LOG.warn("No files found to send.");
            return;
        }

        List<File> files = new ArrayList<>(Arrays.asList(Files.fromFolder(gpxFolder, new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.contains(fileToSend) && !s.contains("zip");
            }
        })));

        List<File> zipFiles = new ArrayList<>();

        if (files.size() == 0) {
            LOG.warn("No files found to send after filtering.");
            return;
        }

        List<FileSender> senders = getFileAutosenders();

        if (!senders.isEmpty() && preferenceHelper.shouldSendZipFile()) {
            File zipFile = new File(gpxFolder.getPath(), fileToSend + ".zip");
            ArrayList<String> filePaths = new ArrayList<>();

            for (File f : files) {
                filePaths.add(f.getAbsolutePath());
            }

            LOG.info("Zipping file");
            ZipHelper zh = new ZipHelper(filePaths.toArray(new String[filePaths.size()]), zipFile.getAbsolutePath());
            zh.zipFiles();

            zipFiles.clear();
            zipFiles.add(zipFile);
        }


        for (FileSender sender : senders) {
            LOG.debug("Sender: " + sender.getClass().getName());
            //Special case for OSM Uploader
            if(!sender.accept(null, ".zip")){
                sender.uploadFile(files);
                continue;
            }

            if(preferenceHelper.shouldSendZipFile()){
                sender.uploadFile(zipFiles);
            } else {
                sender.uploadFile(files);
            }

        }
    }


    private static List<FileSender> getFileAutosenders() {

        List<FileSender> senders = new ArrayList<>();


        if(getGoogleDriveSender().isAutoSendAvailable()){
            senders.add(getGoogleDriveSender());
        }

        if(getOsmSender().isAutoSendAvailable()){
            senders.add(getOsmSender());
        }

        if(getEmailSender().isAutoSendAvailable()){
            senders.add(getEmailSender());
        }

        if(getDropBoxSender().isAutoSendAvailable()){
            senders.add(getDropBoxSender());
        }

        if(getOpenGTSSender().isAutoSendAvailable()){
            senders.add(getOpenGTSSender());
        }

        if(getFtpSender().isAutoSendAvailable()){
            senders.add(getFtpSender());
        }

        if(getOwnCloudSender().isAutoSendAvailable()){
            senders.add(getOwnCloudSender());
        }

        if(getSFTPSender().isAutoSendAvailable()){
            senders.add(getSFTPSender());
        }

        return senders;

    }
}
