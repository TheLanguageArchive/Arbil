package nl.mpi.arbil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;

/**
 * Document   : LinorgVersionChecker
 * Created on : Mar 11, 2009, 10:13:10 AM
 * @author Peter.Withers@mpi.nl
 */
public class LinorgVersionChecker {

    LinorgFrame parentComponent;

    private boolean isLatestVersion() {
        try {
            LinorgVersion linorgVersion = new LinorgVersion();
            int daysTillExpire = 1;
            //String currentVersionTxt = "arbil-" + linorgVersion.currentMajor + "-" + linorgVersion.currentMinor + "-current.txt";
            File cachePath = LinorgSessionStorage.getSingleInstance().updateCache(linorgVersion.currentVersionFile, daysTillExpire);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(cachePath));
            String serverVersionString = bufferedReader.readLine();
//            String localVersionString = "linorg" + linorgVersion.currentRevision + ".jar"; // the server string has the full jar file name
//            System.out.println("currentRevision: " + localVersionString);
            System.out.println("currentRevision: " + linorgVersion.currentRevision);
            System.out.println("serverVersionString: " + serverVersionString);
            // either exact or greater version matches will be considered correct because there will be cases where the txt file is older than the jar
            return (linorgVersion.currentRevision.compareTo(serverVersionString) >= 0);
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return true;
    }

    private boolean doUpdate(String webstartUrlString) {
        try {
            //TODO: check the version of javaws before calling this
            Process launchedProcess = Runtime.getRuntime().exec(new String[]{"javaws", "-import", webstartUrlString});
            BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream()));
            String line;
            while ((line = errorStreamReader.readLine()) != null) {
                System.out.println("Launched process error stream: \"" + line + "\"");
            }
            return (0 == launchedProcess.waitFor());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void restartApplication(String webstartUrlString) {
        try {
            Process restartProcess = Runtime.getRuntime().exec(new String[]{"javaws", webstartUrlString});
            if (0 == restartProcess.waitFor()) {
                System.exit(0);
            } else {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("There was an error restarting the application.\nThe update will take effect next time the application is restarted.", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkForUpdate(final LinorgFrame parentComponentLocal) {
        parentComponent = parentComponentLocal;
        new Thread() {

            public void run() {
                if (!isLatestVersion()) {
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("There is a new version available.\nPlease go to the website and update via the download link.", null);
                }
            }
        }.start();
    }

    public boolean hasWebStartUrl() {
        System.out.println("hasWebStartUrl");
        //System.setProperty("nl.mpi.arbil.webstartUpdateUrl", "http://www.mpi.nl/tg/j2se/jnlp/arbil/arbil-testing.jnlp");
        String webstartUpdateUrl = System.getProperty("nl.mpi.arbil.webstartUpdateUrl");
        System.out.println("webstartUpdateUrl: " + webstartUpdateUrl);
        return null != webstartUpdateUrl;
    }

    public void checkForAndUpdateViaJavaws(final LinorgFrame parentComponentLocal) {
        parentComponent = parentComponentLocal;
        //if (last check date not today)
        new Thread() {

            public void run() {
                String webstartUrlString = System.getProperty("nl.mpi.arbil.webstartUpdateUrl");
                System.out.println("webstartUrlString: " + webstartUrlString);
                {
                    if (webstartUrlString != null && !isLatestVersion()) {
//                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("There is a new version available.\nPlease go to the website and update via the download link.", null);
                        switch (JOptionPane.showConfirmDialog(parentComponent, "There is a new version available\nDo you want to update now?", "Arbil", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                            case JOptionPane.NO_OPTION:
                                break;
                            case JOptionPane.YES_OPTION:
                                if (doUpdate(webstartUrlString)) {
                                    restartApplication(webstartUrlString);
                                } else {
                                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("There was an error updating the application.\nPlease go to the website and update via the download link.", null);
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
