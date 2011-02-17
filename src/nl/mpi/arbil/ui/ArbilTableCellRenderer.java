/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilNodeObject;
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
        return fontMetrics.stringWidth(currentCellString)
                + ControlledVocabularyCellPanel.getAddedWidth(cellObject);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // TODO: this might be a better place to set the backgound and text colours
        isCellSelected = isSelected;
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (cellObject instanceof ArbilField && ((ArbilField) cellObject).hasVocabulary()) {
            return new ControlledVocabularyCellPanel(component, (ArbilField) cellObject);
        } else {
            return component;
        }
    }

    public ArbilTableCellRenderer() {
    }

    // this is used when getting a string value for the object
    public ArbilTableCellRenderer(Object cellObject) {
        setValue(cellObject);
    }

    @Override
    protected void setValue(Object value) {
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
        if (cellObject instanceof ArbilNodeObject) {
            return (((ArbilNodeObject) cellObject).getIcon());
        } else if (cellObject instanceof ArbilNodeObject[]) {
            return (ArbilIcons.getSingleInstance().getIconForImdi((ArbilNodeObject[]) cellObject));
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
        if (cellObject instanceof ArbilNodeObject) {
            return (((ArbilNodeObject) cellObject).toString());
        } else if (cellObject instanceof ArbilNodeObject[]) {
            String cellText = "";
            Arrays.sort((ArbilNodeObject[]) cellObject, new Comparator() {

                public int compare(Object o1, Object o2) {
                    String value1 = o1.toString();
                    String value2 = o2.toString();
                    return value1.compareToIgnoreCase(value2);
                }
            });
            boolean hasAddedValues = false;
            for (ArbilNodeObject currentImdiTreeObject : (ArbilNodeObject[]) cellObject) {
                cellText = cellText + "[" + currentImdiTreeObject.toString() + "],";
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
