package nl.mpi.arbil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
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
class ImageBoxRenderer extends JLabel implements ListCellRenderer {

    int outputWidth = 200;
    int outputHeight = 130;
    int textStartX = 0;
    int textStartY = 0;
    boolean ffmpegFound = true;
    boolean imageMagickFound = true;
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
    public boolean canDisplay(ImdiTreeObject testableObject) {
        if (testableObject.thumbnailFile != null) {
            return true;
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
        if (value instanceof ImdiTreeObject) {
            ImdiTreeObject imdiObject = (ImdiTreeObject) value;
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

    private String getTargetFileString(ImdiTreeObject targetImdiObject) {
        if (targetImdiObject.hasResource()) {
            return targetImdiObject.getFullResourcePath();
        } else if (targetImdiObject.isArchivableFile()) {
            return targetImdiObject.getUrlString();
        } else {
            return null;
        }
    }

    private void createVideoThumbnail(ImdiTreeObject targetImdiObject) {
        if (ffmpegFound) {
            try {
                File iconFile = File.createTempFile("arbil", ".jpg");
                URL targetURL = new URL(getTargetFileString(targetImdiObject));
                String execString = "ffmpeg  -itsoffset -4  -i " + targetURL.getFile() + " -vframes 1 -s " + outputWidth + "x" + outputHeight + " " + iconFile.getAbsolutePath();
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
            } catch (Exception ex) {
                ffmpegFound = false; //todo this is not getting hit when ffmpeg is not available
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
    }

    private void createImageThumbnail(ImdiTreeObject targetImdiObject) {
        if (imageMagickFound) {
            try {
                File iconFile = File.createTempFile("arbil", ".jpg");
                URL targetURL = new URL(getTargetFileString(targetImdiObject));
                String execString = "convert -define jpeg:size=" + outputWidth * 2 + "x" + outputHeight * 2 + " " + targetURL.getFile() + " -auto-orient -thumbnail " + outputWidth + "x" + outputHeight + " -unsharp 0x.5 " + iconFile.getAbsolutePath();
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
//        /data1/apps/ffmpeg-deb/usr/bin/ffmpeg
//            ffmpeg  -itsoffset -4  -i test.avi -vcodec mjpeg -vframes 1 -an -f rawvideo -s 320x240 test.jpg
            } catch (Exception ex) {
                imageMagickFound = false; //todo this is not getting hit when x is not available
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
    }

    private void createThumbnail(ImdiTreeObject targetImdiObject) {
        try {
            BufferedImage resizedImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = resizedImg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (((ImdiTreeObject) targetImdiObject).mpiMimeType.contains("image")) {
                ImageIcon nodeImage = new ImageIcon(new URL(getTargetFileString(targetImdiObject)).getFile());
                if (nodeImage != null) {
                    g2.drawImage(nodeImage.getImage(), 0, 0, outputWidth, outputHeight, null);
                }
            } else if (targetImdiObject.mpiMimeType.contains("text")) {
                drawFileText(g2, new URL(getTargetFileString(targetImdiObject)));
            }
            g2.dispose();
            File iconFile = File.createTempFile("arbil", ".jpg");
            ImageIO.write(resizedImg, "JPEG", iconFile);
            if (iconFile.exists()) {
                targetImdiObject.thumbnailFile = iconFile;
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }
}


