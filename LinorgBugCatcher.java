/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

/**
 * Document   : LinorgBugCatcher
 * Created on : Dec 17, 2008, 10:35:56 AM
 * @author petwit
 */
public class LinorgBugCatcher {

    private int captureCount = 0;

    public void grabApplicationShot() {
        try {
            Robot robot = new Robot();
            //BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            BufferedImage screenShot = robot.createScreenCapture(GuiHelper.linorgWindowManager.linorgFrame.getBounds());
            DecimalFormat myFormat = new DecimalFormat("000");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String formattedDate = formatter.format(new Date());
            String formattedCount = myFormat.format(new Integer(captureCount));
            ImageIO.write(screenShot, "JPG", new File(GuiHelper.linorgSessionStorage.storageDirectory + "screenshots" + File.separatorChar + formattedDate + "-" + formattedCount + ".jpg"));
            captureCount++;
        } catch (Exception e) {
        }
    }

    public void logError(/*String messageString,*/Exception exception) {
        try {
            FileWriter errorLogFile = new FileWriter(GuiHelper.linorgSessionStorage.storageDirectory + "linorgerror.log", true);
//            System.out.println("logCatch: " + messageString);
//            journalFile.append(messageString + "\n");
            errorLogFile.append("Error Date: " + new Date().toString() + "\n");
            errorLogFile.append("Compile Date: " + new LinorgVersion().compileDate + "\n");
            errorLogFile.append("Current Revision: " + new LinorgVersion().currentRevision + "\n");
            StackTraceElement[] stackTraceElements = exception.getStackTrace();
            for (StackTraceElement element : stackTraceElements) {
                errorLogFile.append(element.toString() + "\n");
            }
            errorLogFile.append("======================================================================\n");
            errorLogFile.close();
        } catch (Exception ex) {
            System.err.println("failed to write to the journal: " + ex.getMessage());
        }
    }
}
