package mpi.linorg;

import java.io.BufferedReader;
import java.io.FileReader;
import javax.swing.SwingUtilities;

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
            String currentVersionTxt = "arbil-" + linorgVersion.currentMajor + "-" + linorgVersion.currentMinor + "-current.txt";
            String cachePath = GuiHelper.linorgSessionStorage.updateCache("http://www.mpi.nl/tg/j2se/jnlp/linorg/" + currentVersionTxt, daysTillExpire);
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

    private boolean doUpdate() {
        // download then check that the new jar is correctly signed
        // then move the old version
        // then move the new version to the old location
        return true;
    }

    private void restartApplication() {
        //parentComponent.performCleanExit();
        //System.//spawn new updated applcation 
        System.exit(0);
    }

    public void checkForUpdate(final LinorgFrame parentComponentLocal) {
        parentComponent = parentComponentLocal;
        //if (last check date not today)
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (!isLatestVersion()) {
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("There is a new version available.\nPlease go to the website and update via the download link.", null);
//                    switch (JOptionPane.showConfirmDialog(parentComponent, "There is a new version available\nDo you want to update now?", "Arbil", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
//                        case JOptionPane.NO_OPTION:
//                            break;
//                        case JOptionPane.YES_OPTION:
//                            if (doUpdate()) {
//                                restartApplication();
//                            }
//                            break;
//                        default:
//                            return;
//                    }
                }
            }
        });
    }
}
