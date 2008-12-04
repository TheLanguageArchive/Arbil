/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author petwit
 */
class ImageBoxRenderer extends JLabel implements ListCellRenderer {

    int outputWidth = 200;
    int outputHeight = 130;

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
                targetFile = imdiObject.getResource();
            } else if (imdiObject.isArchivableFile()) {
                targetFile = imdiObject.getUrlString();
            }

            ImageIcon icon = new ImageIcon(targetFile.replace("file://", ""));
            if (icon != null) {
//                        int outputWidth = 32;
//                        int outputHeight = 32;
//                        int outputWidth = getPreferredSize().width;
//                        int outputHeight = getPreferredSize().height - 100;
                BufferedImage resizedImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = resizedImg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(icon.getImage(), 0, 0, outputWidth, outputHeight, null);
                g2.dispose();
                ImageIcon thumbnailIcon = new ImageIcon(resizedImg);
                setIcon(thumbnailIcon);
            }

            setFont(list.getFont());
        } else {
            setText(value.toString() + " (no image available)");
        }
        return this;
    }
}
