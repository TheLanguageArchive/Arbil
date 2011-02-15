package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilNodeObject;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Document   : ImageBoxRenderer
 * Created on : Wed Dec 03 15:44:20
 * @author Peter.Withers@mpi.nl
 */
public class ImageBoxRenderer extends JLabel implements ListCellRenderer {

    int outputWidth = 200;
    int outputHeight = 130;
    int textStartX = 0;
    int textStartY = 0;
    boolean ffmpegFound = true;
    boolean imageMagickFound = true;
    String ffmpegPath = null;
    String imageMagickPath = null;
    String searchPathArray[] = {"ImageMagick\\", "/opt/local/bin/", ""};
    //    boolean loadedMfcDlls = false;
    // the thumbnail files are stored in a temp file on disk and the file location kept in the imditreeobject

    public ImageBoxRenderer() {
//        setBorder(new DashedBorder(Color.DARK_GRAY));
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        setPreferredSize(new Dimension(outputWidth + 10, outputHeight + 50));
    }

    // returns a boolean value indicating if the node has or can have a thumbnail
    // if it can but does not yet then a thumbnail will be made
    public boolean canDisplay(ArbilNodeObject testableObject) {
        if (testableObject.thumbnailFile != null) {
            return true;
        }
        if (!testableObject.hasLocalResource()) {
            return false;
        }
        if (testableObject.mpiMimeType == null) {
            return false;
        }
        if (testableObject.mpiMimeType.toLowerCase().contains("text")) {
            createThumbnail(testableObject);
        }
        if (testableObject.mpiMimeType.toLowerCase().contains("image")) {
            createImageThumbnail(testableObject);
            if (testableObject.thumbnailFile == null) {
                if (testableObject.mpiMimeType.toLowerCase().contains("jp") || testableObject.mpiMimeType.toLowerCase().contains("gif")) {
                    //  if we get here then resourt to creating the thumbnail in java
                    createThumbnail(testableObject);
                }
            }
        }
        if (testableObject.mpiMimeType.toLowerCase().contains("video")) {
            createVideoThumbnail(testableObject);
        }
        return testableObject.thumbnailFile != null;
    }

    /*
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        //Get the selected index. (The index param isn't
        //always valid, so just use the value.)
//            int selectedIndex = ((Integer) value).intValue();

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        //Set the icon and text.
        if (value instanceof ArbilNodeObject) {
            ArbilNodeObject imdiObject = (ArbilNodeObject) value;
            setFont(list.getFont());
            setText(imdiObject.toString());
            if (imdiObject.thumbnailFile != null) {
                try {
                    setIcon(new ImageIcon(imdiObject.thumbnailFile.toURL()));
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        } else {
            setText(value.toString() + " (no image available)");
        }
        return this;
    }

    private void drawFileText(Graphics2D targegGraphics, URL targetURL) {
        int linePosY = textStartY;
        targegGraphics.setBackground(Color.white);
        targegGraphics.clearRect(0, 0, outputWidth, outputHeight);
        targegGraphics.setColor(Color.BLACK);
        targegGraphics.drawRect(0, 0, outputWidth - 1, outputHeight - 1);
        targegGraphics.setColor(Color.DARK_GRAY);
        Font currentFont = targegGraphics.getFont();
        Font renderFont = new Font(currentFont.getFontName(), currentFont.getStyle(), 11);
        targegGraphics.setFont(renderFont);
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(targetURL.openStream()));
            String textToDraw;
            while ((textToDraw = bufferedReader.readLine()) != null && outputHeight > linePosY) {
                textToDraw = textToDraw.replaceAll("\\<.*?>", "");
                textToDraw = textToDraw.replaceAll("^\\\\[^ ]*", "");
                textToDraw = textToDraw.replaceAll("\\s+", " ");
                textToDraw = textToDraw.trim();
                if (textToDraw.length() > 0) {
                    double lineHeight = targegGraphics.getFont().getStringBounds(textToDraw, targegGraphics.getFontRenderContext()).getHeight();
                    linePosY = linePosY + (int) lineHeight;
                    targegGraphics.drawString(textToDraw, textStartX + 2, linePosY);
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    private File getTargetFile(ArbilNodeObject targetImdiObject) {
        if (targetImdiObject.hasResource()) {
            return new File(targetImdiObject.getFullResourceURI());
        } else if (targetImdiObject.isArchivableFile()) {
            return targetImdiObject.getFile();
        } else {
            return null;
        }
    }

    private void createVideoThumbnail(ArbilNodeObject targetImdiObject) {
        if (ffmpegPath == null) {
            // todo: replaces this with a parameter or a properties file
            for (String currentPath : searchPathArray) {
                for (String currentSuffix : new String[]{".exe", ""}) {
                    ffmpegPath = currentPath + "ffmpeg" + currentSuffix;
                    if (new File(ffmpegPath).exists()) {
                        break;
                    }
                }
                if (new File(ffmpegPath).exists()) {
                    break;
                }
            }
        }
        if (ffmpegFound) {
            try {
                File iconFile = File.createTempFile("arbil", ".jpg");
                iconFile.deleteOnExit();
                File targetFile = getTargetFile(targetImdiObject);
                String[] execString = new String[]{ffmpegPath, "-itsoffset", "-4", "-i", targetFile.getCanonicalPath(), "-vframes", "1", "-s", outputWidth + "x" + outputHeight, iconFile.getAbsolutePath()};
//                System.out.println(execString);
                Process launchedProcess = Runtime.getRuntime().exec(execString);
                BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream()));
                String line;
                while ((line = errorStreamReader.readLine()) != null) {
//                    ffmpegFound = false;
                    System.out.println("Launched process error stream: \"" + line + "\"");
                }
                iconFile.deleteOnExit();
                if (iconFile.exists()) {
                    targetImdiObject.thumbnailFile = iconFile;
                }
//        /data1/apps/ffmpeg-deb/usr/bin/ffmpeg
//            ffmpeg  -itsoffset -4  -i test.avi -vcodec mjpeg -vframes 1 -an -f rawvideo -s 320x240 test.jpg
            } catch (IOException ex) {
                ffmpegFound = false; //todo this is not getting hit when ffmpeg is not available
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
    }

    private void createImageThumbnail(ArbilNodeObject targetImdiObject) {
//        if (!loadedMfcDlls) {
//            loadedMfcDlls = true;
//            try {
//                // todo: this need not be done in a non windows environment or when imagemagick is installed
//                System.loadLibrary("CVCOMP90");
//            } catch (Exception ex) {
//                GuiHelper.linorgBugCatcher.logError(ex);
//            }
//        }
        if (imageMagickPath == null) {
            // todo: replaces this process with a parameter or a properties file so that the jnlp version can benifit from the installed version
            for (String currentPath : searchPathArray) {
                for (String currentSuffix : new String[]{".exe", ""}) {
                    imageMagickPath = currentPath + "convert" + currentSuffix;
                    if (new File(imageMagickPath).exists()) {
                        break;
                    }
                }
                if (new File(imageMagickPath).exists()) {
                    break;
                }
            }
        }
        if (imageMagickFound) {
            try {
                File iconFile = File.createTempFile("arbil", ".jpg");
                iconFile.deleteOnExit();
                File targetFile = getTargetFile(targetImdiObject);
                if (targetFile.exists()) {
                    String[] execString = new String[]{imageMagickPath, "-define", "jpeg:size=" + outputWidth * 2 + "x" + outputHeight * 2, targetFile.getCanonicalPath(), "-auto-orient", "-thumbnail", outputWidth + "x" + outputHeight, "-unsharp", "0x.5", iconFile.getAbsolutePath()};
                    System.out.println(execString);
                    Process launchedProcess = Runtime.getRuntime().exec(execString);
                    BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream()));
                    String line;
                    while ((line = errorStreamReader.readLine()) != null) {
//                    ffmpegFound = false;
                        System.out.println("Launched process error stream: \"" + line + "\"");
                    }
                    iconFile.deleteOnExit();
                    if (iconFile.exists()) {
                        targetImdiObject.thumbnailFile = iconFile;
                    }
                }
//        /data1/apps/ffmpeg-deb/usr/bin/ffmpeg
//            ffmpeg  -itsoffset -4  -i test.avi -vcodec mjpeg -vframes 1 -an -f rawvideo -s 320x240 test.jpg
            } catch (Exception ex) {
                imageMagickFound = false; //todo this is not getting hit when x is not available
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
    }

    private void createThumbnail(ArbilNodeObject targetImdiObject) {
        try {
            BufferedImage resizedImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = resizedImg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (((ArbilNodeObject) targetImdiObject).mpiMimeType.contains("image")) {
                ImageIcon nodeImage = new ImageIcon(getTargetFile(targetImdiObject).toURL());
                if (nodeImage != null) {
                    g2.drawImage(nodeImage.getImage(), 0, 0, outputWidth, outputHeight, null);
                }
            } else if (targetImdiObject.mpiMimeType.contains("text")) {
                drawFileText(g2, getTargetFile(targetImdiObject).toURL());
            }
            g2.dispose();
            File iconFile = File.createTempFile("arbil", ".jpg");
            iconFile.deleteOnExit();
            ImageIO.write(resizedImg, "JPEG", iconFile);
            if (iconFile.exists()) {
                targetImdiObject.thumbnailFile = iconFile;
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }
}


