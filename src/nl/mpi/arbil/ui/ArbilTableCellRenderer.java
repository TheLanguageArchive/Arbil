/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilDataNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import nl.mpi.arbil.ArbilIcons;

/**
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableCellRenderer extends DefaultTableCellRenderer {

    Object cellObject;
    boolean isCellSelected = false;

    public int getRequiredWidth(FontMetrics fontMetrics) {
        String currentCellString = getText();
        // Calculate width of text
        int width = fontMetrics.stringWidth(currentCellString);
        // ArbilField might have an icon
        if (cellObject instanceof ArbilField) {
            Icon icon = ArbilIcons.getSingleInstance().getIconForField((ArbilField) cellObject);
            // If there's an icon, add its width
            if (icon != null) {
                width += icon.getIconWidth();
            }
        }
        return width;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // TODO: this might be a better place to set the backgound and text colours
        isCellSelected = isSelected;
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // ArbilField may have to be decorated with an icon
        if (cellObject instanceof ArbilField) {
            Icon icon = ArbilIcons.getSingleInstance().getIconForField((ArbilField) cellObject);
            if (icon != null) {
                // An icon exists for this field, so wrap component with icon panel
                return new ArbilIconCellPanel(component, icon);
            }
        }
        return component;
    }

    public ArbilTableCellRenderer() {
    }

    // this is used when getting a string value for the object
    public ArbilTableCellRenderer(Object cellObject) {
        setValue(cellObject);
    }

    @Override
    protected final void setValue(Object value) {
        cellObject = value;
        super.setValue(value);
        if (cellObject instanceof ArbilField[]) {
            int greyTone = 150;
            super.setForeground(new Color(greyTone, greyTone, greyTone));
        }
        if (!isCellSelected) {
            if (cellObject instanceof String && cellObject.equals("")) {
                super.setBackground(new Color(230, 230, 230)/*Color.lightGray*/);
            }
            if (cellObject instanceof ArbilField) {
                if (((ArbilField) cellObject).fieldNeedsSaveToDisk()) {
                    super.setForeground(Color.blue);
                }
            }
        }
    }

    @Override
    public Icon getIcon() {
        if (cellObject instanceof ArbilDataNode) {
            return (((ArbilDataNode) cellObject).getIcon());
        } else if (cellObject instanceof ArbilDataNode[]) {
            return (ArbilIcons.getSingleInstance().getIconForNode((ArbilDataNode[]) cellObject));
        } else if (cellObject instanceof ArbilField[]) {
            return null;
//        } else if (cellObject instanceof ArbilField) {
//            return ArbilIcons.getSingleInstance().getIconForVocabulary((ArbilField) cellObject);
        } else {
            return (null);
        }
    }

    @Override
    public String getText() {
        if (cellObject instanceof ArbilDataNode) {
            return (((ArbilDataNode) cellObject).toString());
        } else if (cellObject instanceof ArbilDataNode[]) {
            String cellText = "";
            Arrays.sort((ArbilDataNode[]) cellObject, new Comparator() {

                public int compare(Object o1, Object o2) {
                    String value1 = o1.toString();
                    String value2 = o2.toString();
                    return value1.compareToIgnoreCase(value2);
                }
            });
            boolean hasAddedValues = false;
            for (ArbilDataNode currentArbilDataNode : (ArbilDataNode[]) cellObject) {
                cellText = cellText + "[" + currentArbilDataNode.toString() + "],";
                hasAddedValues = true;
            }
            if (hasAddedValues) {
                cellText = cellText.substring(0, cellText.length() - 1);
            }
            return (cellText);
        } else if (cellObject instanceof ArbilField[]) {
            return "<multiple values>";
        } else if (cellObject instanceof ArbilField && ((ArbilField) cellObject).isRequiredField() && ((ArbilField) cellObject).toString().length() == 0) {
            super.setForeground(Color.RED);
            return "<required field>";
        } else if (cellObject instanceof ArbilField && !((ArbilField) cellObject).fieldValueValidates()) {
            super.setForeground(Color.RED);
            return super.getText();
        } else {
            return super.getText();
        }
    }
}
