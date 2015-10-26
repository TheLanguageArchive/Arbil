/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ApplicationVersionManager Created on : Mar 11, 2009, 10:13:10 AM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ApplicationVersionManager {

    private final static Logger logger = LoggerFactory.getLogger(ApplicationVersionManager.class);
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }
    private static SessionStorage sessionStorage;
    private static final ResourceBundle services = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services");

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
        sessionStorage = sessionStorageInstance;
    }
    private ApplicationVersion applicationVersion;
    private final WebstartHelper webstartHelper = new WebstartHelper();

    /**
     *
     * @param appVersion Version information to use
     */
    public ApplicationVersionManager(ApplicationVersion appVersion) {
        this.applicationVersion = appVersion;
    }

    public ApplicationVersion getApplicationVersion() {
        return applicationVersion;
    }

    public boolean forceUpdateCheck() {
        File cachePath = sessionStorage.getSaveLocation(applicationVersion.currentVersionFile);
        if (cachePath.delete()) {
            logger.debug("Dropped old version file");
        } else {
            messageDialogHandler.addMessageDialogToQueue(services.getString("COULD NOT WRITE TO STORAGE DIRECTORY. UPDATE CHECK FAILED!"), services.getString("ERROR"));
        }
        return this.checkForUpdate();
    }

    private final Pattern VERSION_PATTERN = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)");

    private boolean isLatestVersion() {
        BufferedReader bufferedReader = null;
        try {
            int daysTillExpire = 1;
            File cachePath = sessionStorage.updateCache(applicationVersion.currentVersionFile, daysTillExpire, false);
            bufferedReader = new BufferedReader(new FileReader(cachePath));
            String serverVersionString = bufferedReader.readLine();
//            String localVersionString = "linorg" + linorgVersion.currentRevision + ".jar"; // the server string has the full jar file name
//            logger.debug("currentRevision: " + localVersionString);
            logger.debug("currentRevision: {}", applicationVersion.currentRevision);
            logger.debug("serverVersionString: {}", serverVersionString);
            if (serverVersionString == null) {
                return true;
            }
            final Matcher matcher = VERSION_PATTERN.matcher(serverVersionString);
            if (!matcher.matches()) {
                return true;
            }
            // ignore any strings that are not a version number because it might be a 404 or other error page
            final int major = Integer.parseInt(matcher.group(1));
            final int minor = Integer.parseInt(matcher.group(2));
            final int revision = Integer.parseInt(matcher.group(3));

            final int currentMajor = Integer.parseInt(applicationVersion.currentMajor);
            final int currentMinor = Integer.parseInt(applicationVersion.currentMinor);
            final int currentRevision = Integer.parseInt(applicationVersion.currentRevision);

            // either exact or greater version matches will be considered correct because there will be cases where the txt file is older than the jar
            return compareVersions(currentMajor, currentMinor, currentRevision, major, minor, revision);
        } catch (IOException ex) {
            // probably not connected
            logger.warn("Could not download current version information");
            logger.info("Failed to download current version information", ex);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    logger.warn("Could not close stream to version information");
                    logger.info("Failed to close stream to version information", ex);
                }
            }
        }
        return true;
    }

    /**
     * whether combined 'current' is greater than or equal to the specified
     * version
     *
     * @param currentMajor
     * @param currentMinor
     * @param currentRevision
     * @param major
     * @param minor
     * @param revision
     * @return
     */
    public static boolean compareVersions(final int currentMajor, final int currentMinor, final int currentRevision, final int major, final int minor, final int revision) {
        return currentMajor > major
                || currentMajor == major && currentMinor > minor
                //revision number gets reset for every branch (== major.minor, so check if branch equals before checking revision)
                || currentMajor == major && currentMinor == minor && currentRevision >= revision;
    }

    private boolean doUpdate(String updateUrl) {
        BufferedReader errorStreamReader = null;
        try {
            //TODO: check the version of javaws before calling this
            Process launchedProcess = Runtime.getRuntime().exec(new String[]{"javaws", "-import", updateUrl});
            errorStreamReader = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream()));
            String line;
            while ((line = errorStreamReader.readLine()) != null) {
                logger.debug("Launched process error stream: \"" + line + "\"");
            }
            return (0 == launchedProcess.waitFor());
        } catch (Exception e) {
            BugCatcherManager.getBugCatcher().logError(e);
        } finally { // close pipeline when lauched process is done
            if (errorStreamReader != null) {
                try {
                    errorStreamReader.close();
                } catch (IOException ioe) {
                    BugCatcherManager.getBugCatcher().logError(ioe);
                }
            }
        }
        return false;
    }

    private void restartApplication(String updateUrl) {
        try {
            Process restartProcess = Runtime.getRuntime().exec(new String[]{"javaws", updateUrl});
            if (0 == restartProcess.waitFor()) {
                System.exit(0);
            } else {
                messageDialogHandler.addMessageDialogToQueue(services.getString("THERE WAS AN ERROR RESTARTING THE APPLICATION.THE UPDATE WILL TAKE EFFECT NEXT TIME THE APPLICATION IS RESTARTED."), null);
            }
        } catch (Exception e) {
            BugCatcherManager.getBugCatcher().logError(e);
        }
    }

    public boolean checkForUpdate() {
        if (!isLatestVersion()) {
            if (webstartHelper.isWebStart()) {
                this.checkForAndUpdateViaJavaws();
            } else {
                new Thread("checkForUpdate") {
                    @Override
                    public void run() {
                        messageDialogHandler.addMessageDialogToQueue(services.getString("THERE IS A NEW VERSION AVAILABLE.PLEASE GO TO THE WEBSITE AND UPDATE VIA THE DOWNLOAD LINK."), null);
                    }
                }.start();
            }
            return true;
        } else {
            return false;
        }
//        }
    }

    /**
     * Update by re-starting webstart location
     */
    private void checkForAndUpdateViaJavaws() {
        //if (last check date not today)
        new Thread("checkForAndUpdateViaJavaws") {
            @Override
            public void run() {
                final String updateUrl = webstartHelper.getWebstartUrl();
                {
                    if (updateUrl != null && !isLatestVersion()) {
                        switch (messageDialogHandler.showDialogBox(services.getString("There is a new version available"), applicationVersion.applicationTitle, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                            case JOptionPane.NO_OPTION:
                                break;
                            case JOptionPane.YES_OPTION:
                                if (doUpdate(updateUrl)) {
                                    restartApplication(updateUrl);
                                } else {
                                    messageDialogHandler.addMessageDialogToQueue(services.getString("THERE WAS AN ERROR UPDATING THE APPLICATION.PLEASE GO TO THE WEBSITE AND UPDATE VIA THE DOWNLOAD LINK."), null);
                                }
                                break;
                            default:
                                return;
                        }
                    }
                }
            }
        }.start();
    }
}
