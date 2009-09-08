package nl.mpi.arbil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Document   : ImageBoxRenderer
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
class ImageBoxRenderer extends JLabel implements ListCellRenderer {

    int outputWidth = 200;
    int outputHeight = 130;
    int textStartX = 0;
    int textStartY = 0;
    Hashtable<String, ImageIcon> thumbNailHash = new Hashtable<String, ImageIcon>();

    public ImageBoxRenderer() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        setPreferredSize(new Dimension(outputWidth + 10, outputHeight + 50));
    }

    static public boolean canDisplay(ImdiTreeObject testableObject) {
        if (testableObject.mpiMimeType == null) {
            return false;
        }
        if (testableObject.mpiMimeType.toLowerCase().contains("text")) {
            return true;
        }
        if (testableObject.mpiMimeType.toLowerCase().contains("image")) {
            return true;
        }
        return false;
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

        //Set the icon and text.  If icon was null, say so.
        if (value instanceof ImdiTreeObject) {
            if (((ImdiTreeObject) value).mpiMimeType != null) {
                ImdiTreeObject imdiObject = (ImdiTreeObject) value;
                setText(imdiObject.toString());
                String targetFile = "";
                if (imdiObject.hasResource()) {
                    targetFile = imdiObject.getFullResourcePath();
                } else if (imdiObject.isArchivableFile()) {
                    targetFile = imdiObject.getUrlString();
                }
                ImageIcon thumbnailIcon = thumbNailHash.get(targetFile);
                if (thumbnailIcon == null) {
                    if (targetFile != null && targetFile.length() > 0) {
                        try {
                            if (((ImdiTreeObject) value).mpiMimeType.contains("video")) {
                            } else {
//                    System.out.println("targetFile: " + targetFile);
//                        int outputWidth = 32;
//                        int outputHeight = 32;
//                        int outputWidth = getPreferredSize().width;
//                        int outputHeight = getPreferredSize().height - 100;
                                BufferedImage resizedImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
                                Graphics2D g2 = resizedImg.createGraphics();
                                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                                if (((ImdiTreeObject) value).mpiMimeType.contains("image")) {
                                    ImageIcon nodeImage = new ImageIcon(new URL(targetFile).getFile());
                                    if (nodeImage != null) {
                                        g2.drawImage(nodeImage.getImage(), 0, 0, outputWidth, outputHeight, null);
                                    }
                                } else if (((ImdiTreeObject) value).mpiMimeType.contains("text")) {
                                    drawFileText(g2, new URL(targetFile));
                                }
                                g2.dispose();
                                thumbnailIcon = new ImageIcon(resizedImg);
                                thumbNailHash.put(targetFile, thumbnailIcon);
                                setFont(list.getFont());
                            }
                        } catch (Exception ex) {
                            setText(value.toString() + " (failed to render image)");
                            GuiHelper.linorgBugCatcher.logError(ex);
                        }
                    }
                }
                if (thumbnailIcon != null) {
                    setIcon(thumbnailIcon);
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

}

