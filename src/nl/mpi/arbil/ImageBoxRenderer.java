package nl.mpi.arbil;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
    Hashtable<String, ImageIcon> thumbNailHash = new Hashtable<String, ImageIcon>();

    public ImageBoxRenderer() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        setPreferredSize(new Dimension(outputWidth + 10, outputHeight + 50));
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
            ImdiTreeObject imdiObject = (ImdiTreeObject) value;
            setText(imdiObject.toString());
            String targetFile = "";
            if (imdiObject.hasResource()) {
                targetFile = imdiObject.getFullResourcePath();
            } else if (imdiObject.isArchivableFile()) {
                targetFile = imdiObject.getUrlString();
            }

            if (targetFile != null && targetFile.length() > 0) {
                ImageIcon thumbnailIcon = thumbNailHash.get(targetFile);
                if (thumbnailIcon == null) {
                    try {
//                    System.out.println("targetFile: " + targetFile);
                        ImageIcon nodeImage = new ImageIcon(new URL(targetFile).getFile());
                        if (nodeImage != null) {
//                        int outputWidth = 32;
//                        int outputHeight = 32;
//                        int outputWidth = getPreferredSize().width;
//                        int outputHeight = getPreferredSize().height - 100;
                            BufferedImage resizedImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
                            Graphics2D g2 = resizedImg.createGraphics();
                            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g2.drawImage(nodeImage.getImage(), 0, 0, outputWidth, outputHeight, null);
                            g2.dispose();
                            thumbnailIcon = new ImageIcon(resizedImg);
                            thumbNailHash.put(targetFile, thumbnailIcon);
                        }
                        setFont(list.getFont());
                    } catch (Exception ex) {
                        setText(value.toString() + " (failed to render image)");
                        GuiHelper.linorgBugCatcher.logError(ex);
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
}
