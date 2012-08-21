package nl.mpi.arbil.util;

import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Document : ArbilBugCatcher Created on : Dec 17, 2008, 10:35:56 AM
 *
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 */
public class ArbilBugCatcher implements BugCatcher {

    private ApplicationVersionManager versionManager;
    private SessionStorage sessionStorage;

    public ArbilBugCatcher(SessionStorage sessionStorage, ApplicationVersionManager versionManager) {
        this.sessionStorage = sessionStorage;
        this.versionManager = versionManager;
        removeOldLogs();
    }

    private void removeOldLogs() {
        // remove all previous error logs for this version other than the one for this build number
        File errorLogFile = new File(sessionStorage.getApplicationSettingsDirectory(), "linorgerror.log");
        if (errorLogFile.exists()) {
            errorLogFile.delete();
        }
        // look for previous error logs for this version only
        ApplicationVersion appVersion = versionManager.getApplicationVersion();
        String currentApplicationVersionMatch = "error-" + appVersion.currentMajor + "-" + appVersion.currentMinor + "-";
        String currentLogFileMatch = getCurrentVersionLogFileName(appVersion);
        for (String currentFile : sessionStorage.getApplicationSettingsDirectory().list()) {
            if (currentFile.startsWith(currentApplicationVersionMatch)) {
                if (!currentFile.startsWith(currentLogFileMatch)) {
                    System.out.println("deleting old log file: " + currentFile);
                    if (!new File(sessionStorage.getApplicationSettingsDirectory(), currentFile).delete()) {
                        System.out.println("Did not delete old log file: " + currentFile);
                    }
                }
            }
        }
    }
    private int captureCount = 0;

    public static File getLogFile(SessionStorage sessionStorage, ApplicationVersion appVersion) {
        File file = new File(sessionStorage.getApplicationSettingsDirectory(), getCurrentVersionLogFileName(appVersion));
        if (!file.exists()) {
            startNewLogFile(file, sessionStorage, appVersion);
        }
        return file;
    }

    private static String getCurrentVersionLogFileName(ApplicationVersion appVersion) {
        return "error-" + appVersion.currentMajor + "-" + appVersion.currentMinor + "-" + appVersion.currentRevision + ".txt";
    }

    private static void startNewLogFile(File file, SessionStorage sessionStorage, ApplicationVersion appVersion) {
        try {
            FileWriter errorLogFile = new FileWriter(file, false);
            errorLogFile.append(appVersion.applicationTitle + " error log" + System.getProperty("line.separator")
                    + "Version: " + appVersion.currentMajor + "." + appVersion.currentMinor + "." + appVersion.currentRevision + System.getProperty("line.separator")
                    + appVersion.lastCommitDate + System.getProperty("line.separator")
                    + "Compile Date: " + appVersion.compileDate + System.getProperty("line.separator")
                    + "Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version") + System.getProperty("line.separator")
                    + "Java version: " + System.getProperty("java.version") + " by " + System.getProperty("java.vendor") + System.getProperty("line.separator")
                    + "User: " + System.getProperty("user.name") + System.getProperty("line.separator")
                    + "Storage directory: " + sessionStorage.getApplicationSettingsDirectory().toString() + System.getProperty("line.separator")
                    + "Project directory: " + sessionStorage.getProjectDirectory().toString() + System.getProperty("line.separator")
                    + "Project working directory: " + sessionStorage.getProjectWorkingDirectory().toString() + System.getProperty("line.separator")
                    + "Log started: " + new Date().toString() + System.getProperty("line.separator"));
            errorLogFile.append("======================================================================" + System.getProperty("line.separator"));
            errorLogFile.close();
        } catch (IOException ex) {
            System.err.println("failed to write to the error log: " + ex.getMessage());
        }
    }

    public void grabApplicationShot(WindowManager windowManager) {
        try {
            Robot robot = new Robot();
            BufferedImage screenShot = robot.createScreenCapture(windowManager.getMainFrame().getBounds());
            DecimalFormat myFormat = new DecimalFormat("000");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String formattedDate = formatter.format(new Date());
            String formattedCount = myFormat.format(Integer.valueOf(captureCount));
            ImageIO.write(screenShot, "JPG", new File(sessionStorage.getApplicationSettingsDirectory(), "screenshots" + File.separatorChar + formattedDate + "-" + formattedCount + ".jpg"));
            captureCount++;
        } catch (Exception e) {
            System.err.println("Exception when creating screenshot: " + e);
        }
    }

    public void logException(PluginException exception) {
        logError("plugin error: ", exception);
    }

    public void logError(Exception exception) {
        logError("", exception);
    }

    public void logError(String messageString, Exception exception) {
        try {
            System.err.println(messageString);
            if (exception != null) {
                System.err.println("Exception of type " + exception.getClass().getName() + ": " + exception.getMessage());
                exception.printStackTrace(System.err);
            }
            FileWriter errorLogFile = new FileWriter(getLogFile(sessionStorage, versionManager.getApplicationVersion()), true);
            errorLogFile.append(messageString).append(System.getProperty("line.separator"));
            errorLogFile.append("Error Date: ").append(new Date().toString()).append(System.getProperty("line.separator"));
            appendVersionInformation(errorLogFile);
            if (exception != null) {
                errorLogFile.append("Exception type: ").append(exception.getClass().getName()).append(System.getProperty("line.separator"));
                errorLogFile.append("Exception message: ").append(exception.getMessage()).append(System.getProperty("line.separator"));
                StackTraceElement[] stackTraceElements = exception.getStackTrace();
                for (StackTraceElement element : stackTraceElements) {
                    errorLogFile.append(element.toString()).append(System.getProperty("line.separator"));
                }
            }
            errorLogFile.append("======================================================================").append(System.getProperty("line.separator"));
            errorLogFile.close();
        } catch (Exception ex) {
            System.err.println("failed to write to the error log: " + ex.getMessage());
        }
    }

    protected void appendVersionInformation(FileWriter errorLogFile) throws IOException {
        ApplicationVersion appVersion = versionManager.getApplicationVersion();
        errorLogFile.append("Compile Date: " + appVersion.compileDate + System.getProperty("line.separator"));
        errorLogFile.append("Current Revision: " + appVersion.currentMajor + "-" + appVersion.currentMinor + "-" + appVersion.currentRevision + System.getProperty("line.separator"));
    }
}
