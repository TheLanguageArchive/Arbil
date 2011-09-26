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
import nl.mpi.arbil.userstorage.ArbilSessionStorage;

/**
 * Document   : ArbilBugCatcher
 * Created on : Dec 17, 2008, 10:35:56 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilBugCatcher implements BugCatcher {

//    static Logger log = Logger.getLogger(ImdiIcons.class.getName());
//            log.debug("debug message.");
//            log.info("info message.");
//            log.warn("warn message.");
//            log.error("error message.");
//            log.fatal("fatal message.");
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static ApplicationVersionManager versionManager;

    public static void setVersionManager(ApplicationVersionManager versionManagerInstance) {
	versionManager = versionManagerInstance;
    }

    public ArbilBugCatcher() {
	// remove all previous error logs for this version other than the one for this build number
	File errorLogFile = new File(ArbilSessionStorage.getSingleInstance().getStorageDirectory(), "linorgerror.log");
	if (errorLogFile.exists()) {
	    errorLogFile.delete();
	}
	// look for previous error logs for this version only
	ApplicationVersion appVersion = versionManager.getApplicationVersion();
	String currentApplicationVersionMatch = "error-" + appVersion.currentMajor + "-" + appVersion.currentMinor + "-";
	String currentLogFileMatch = "error-" + appVersion.currentMajor + "-" + appVersion.currentMinor + "-" + appVersion.currentRevision + ".log";
	for (String currentFile : ArbilSessionStorage.getSingleInstance().getStorageDirectory().list()) {
	    if (currentFile.startsWith(currentApplicationVersionMatch)) {
		if (currentFile.startsWith(currentLogFileMatch)) {
		    // keeping this builds log file
		    System.out.println("currentLogFileMatch: " + currentFile);
		} else {
		    System.out.println("deleting old log file: " + currentFile);
		    if (!new File(ArbilSessionStorage.getSingleInstance().getStorageDirectory(), currentFile).delete()) {
			System.out.println("Did not delete old log file: " + currentFile);
		    }
		}
	    }
	}
    }
    private int captureCount = 0;

    public File getLogFile() {
	ApplicationVersion appVersion = versionManager.getApplicationVersion();
	File file = new File(ArbilSessionStorage.getSingleInstance().getStorageDirectory(), "error-" + appVersion.currentMajor + "-" + appVersion.currentMinor + "-" + appVersion.currentRevision + ".log");
	if (!file.exists()) {
	    startNewLogFile(file);
	}
	return file;
    }

    private static void startNewLogFile(File file) {
	ApplicationVersion appVersion = versionManager.getApplicationVersion();
	try {
	    FileWriter errorLogFile = new FileWriter(file, false);
	    errorLogFile.append(appVersion.applicationTitle + " error log" + System.getProperty("line.separator")
		    + "Version: " + appVersion.currentMajor + "." + appVersion.currentMinor + "." + appVersion.currentRevision + System.getProperty("line.separator")
		    + appVersion.lastCommitDate + System.getProperty("line.separator")
		    + "Compile Date: " + appVersion.compileDate + System.getProperty("line.separator")
		    + "Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version") + System.getProperty("line.separator")
		    + "Java version: " + System.getProperty("java.version") + " by " + System.getProperty("java.vendor") + System.getProperty("line.separator")
		    + "User: " + System.getProperty("user.name") + System.getProperty("line.separator")
		    + "Log started: " + new Date().toString() + System.getProperty("line.separator"));
	    errorLogFile.append("======================================================================" + System.getProperty("line.separator"));
	    errorLogFile.close();
	} catch (IOException ex) {
	    System.err.println("failed to write to the error log: " + ex.getMessage());
	}
    }

    public void grabApplicationShot() {
	try {
	    Robot robot = new Robot();
	    BufferedImage screenShot = robot.createScreenCapture(windowManager.getMainFrame().getBounds());
	    DecimalFormat myFormat = new DecimalFormat("000");
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
	    String formattedDate = formatter.format(new Date());
	    String formattedCount = myFormat.format(Integer.valueOf(captureCount));
	    ImageIO.write(screenShot, "JPG", new File(ArbilSessionStorage.getSingleInstance().getStorageDirectory(), "screenshots" + File.separatorChar + formattedDate + "-" + formattedCount + ".jpg"));
	    captureCount++;
	} catch (Exception e) {
	    System.err.println("Exception when creating screenshot: " + e);
	}
    }

//    public void logMessage(String messageString) {
//        try {
//            FileWriter errorLogFile = new FileWriter(GuiHelper.linorgSessionStorage.storageDirectory, "linorgerror.log", true);
//            System.out.println("logCatch: " + messageString);
//            errorLogFile.append(messageString + System.getProperty("line.separator"));
//            errorLogFile.append("Message Date: " + new Date().toString() + System.getProperty("line.separator"));
//            errorLogFile.append("Compile Date: " + new LinorgVersion().compileDate + System.getProperty("line.separator"));
//            errorLogFile.append("Current Revision: " + new LinorgVersion().currentRevision + System.getProperty("line.separator"));
//            errorLogFile.append("======================================================================" + System.getProperty("line.separator"));
//            errorLogFile.close();
//        } catch (Exception ex) {
//            System.err.println("failed to write to the error log: " + ex.getMessage());
//        }
//    }
    public void logError(Exception exception) {
	logError("", exception);
    }

    public void logError(String messageString, Exception exception) {
	try {
	    ApplicationVersion appVersion = versionManager.getApplicationVersion();
	    System.err.println(messageString);
	    if (exception != null) {
		System.err.println("exception: " + exception.getMessage());
		exception.printStackTrace(System.err);
	    }
	    FileWriter errorLogFile = new FileWriter(getLogFile(), true);
//            System.out.println("logCatch: " + messageString);
	    errorLogFile.append(messageString + System.getProperty("line.separator"));
	    errorLogFile.append("Error Date: " + new Date().toString() + System.getProperty("line.separator"));
	    errorLogFile.append("Compile Date: " + appVersion.compileDate + System.getProperty("line.separator"));
	    errorLogFile.append("Current Revision: " + appVersion.currentMajor + "-" + appVersion.currentMinor + "-" + appVersion.currentRevision + System.getProperty("line.separator"));
	    if (exception != null) {
		errorLogFile.append("Exception Message: " + exception.getMessage() + System.getProperty("line.separator"));
		StackTraceElement[] stackTraceElements = exception.getStackTrace();
		for (StackTraceElement element : stackTraceElements) {
		    errorLogFile.append(element.toString() + System.getProperty("line.separator"));
		}
	    }
	    errorLogFile.append("======================================================================" + System.getProperty("line.separator"));
	    errorLogFile.close();
	} catch (Exception ex) {
	    System.err.println("failed to write to the error log: " + ex.getMessage());
	}
    }
}
