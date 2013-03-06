/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
import javax.swing.JOptionPane;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document   : ApplicationVersionManager
 * Created on : Mar 11, 2009, 10:13:10 AM
 * @author Peter.Withers@mpi.nl
 */
public class ApplicationVersionManager {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationVersionManager.class);

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private ApplicationVersion applicationVersion;

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
	    messageDialogHandler.addMessageDialogToQueue("Could not write to storage directory. Update check failed!", "Error");
	}
	return this.checkForUpdate();
    }

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
	    if (serverVersionString == null || !serverVersionString.matches("[0-9]*")) {
		// ignore any strings that are not a number because it might be a 404 or other error page
		return true;
	    }
	    // either exact or greater version matches will be considered correct because there will be cases where the txt file is older than the jar
	    return (applicationVersion.currentRevision.compareTo(serverVersionString) >= 0);
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	} finally {
	    if (bufferedReader != null) {
		try {
		    bufferedReader.close();
		} catch (IOException ioe) {
		    BugCatcherManager.getBugCatcher().logError(ioe);
		}
	    }
	}
	return true;
    }

    private boolean doUpdate(String webstartUrlString) {
	BufferedReader errorStreamReader = null;
	try {
	    //TODO: check the version of javaws before calling this
	    Process launchedProcess = Runtime.getRuntime().exec(new String[]{"javaws", "-import", webstartUrlString});
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

    private void restartApplication(String webstartUrlString) {
	try {
	    Process restartProcess = Runtime.getRuntime().exec(new String[]{"javaws", webstartUrlString});
	    if (0 == restartProcess.waitFor()) {
		System.exit(0);
	    } else {
		messageDialogHandler.addMessageDialogToQueue("There was an error restarting the application.\nThe update will take effect next time the application is restarted.", null);
	    }
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError(e);
	}
    }

    public boolean checkForUpdate() {
	if (!isLatestVersion()) {
	    if (this.hasWebStartUrl()) {
		this.checkForAndUpdateViaJavaws();
	    } else {
		new Thread("checkForUpdate") {

		    @Override
		    public void run() {
			messageDialogHandler.addMessageDialogToQueue("There is a new version available.\nPlease go to the website and update via the download link.", null);
		    }
		}.start();
	    }
	    return true;
	} else {
	    return false;
	}
//        }
    }

    public boolean hasWebStartUrl() {
	logger.debug("hasWebStartUrl");
	String webstartUpdateUrl = System.getProperty("nl.mpi.webstartUpdateUrl");
	logger.debug("webstartUpdateUrl: {}", webstartUpdateUrl);
	return null != webstartUpdateUrl;
    }

    public void checkForAndUpdateViaJavaws() {
	//if (last check date not today)
	new Thread("checkForAndUpdateViaJavaws") {

	    @Override
	    public void run() {
		String webstartUrlString = System.getProperty("nl.mpi.webstartUpdateUrl");
//                logger.debug(webStartUrlString);
		{
		    if (webstartUrlString != null && !isLatestVersion()) {
//                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("There is a new version available.\nPlease go to the website and update via the download link.", null);
			switch (messageDialogHandler.showDialogBox("There is a new version available\nDo you want to update now?", applicationVersion.applicationTitle, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
			    case JOptionPane.NO_OPTION:
				break;
			    case JOptionPane.YES_OPTION:
				if (doUpdate(webstartUrlString)) {
				    restartApplication(webstartUrlString);
				} else {
				    messageDialogHandler.addMessageDialogToQueue("There was an error updating the application.\nPlease go to the website and update via the download link.", null);
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
