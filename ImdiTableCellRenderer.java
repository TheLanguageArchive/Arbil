/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.Icon;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Peter.Withers@mpi.nl
 */
public class ImdiTableCellRenderer extends DefaultTableCellRenderer {

    Object cellObject;

    public ImdiTableCellRenderer() {
    }

    // this is used when getting a string value for the object
    public ImdiTableCellRenderer(Object cellObject) {
        setValue(cellObject);
    }

    @Override
    protected void setValue(Object value) {
        cellObject = value;
        super.setValue(value);
        if (cellObject instanceof ImdiField[]) {
            int greyTone = 150;
            super.setForeground(new Color(greyTone, greyTone, greyTone));
        }
        if (cellObject instanceof String && cellObject.equals("")) {
            super.setBackground(new Color(230, 230, 230)/*Color.lightGray*/);
        }
        if (cellObject instanceof ImdiField) {
            if (((ImdiField) cellObject).fieldNeedsSaveToDisk) {
                super.setForeground(Color.blue);
            }
        }
    }

    @Override
    public Icon getIcon() {
        if (cellObject instanceof ImdiTreeObject) {
            return (((ImdiTreeObject) cellObject).getIcon());
        } else if (cellObject instanceof ImdiTreeObject[]) {
            return (ImdiIcons.getSingleInstance().getIconForImdi((ImdiTreeObject[]) cellObject));
        } else if (cellObject instanceof ImdiField[]) {
            return null;
        } else {
            return (null);
        }
    }

    @Override
    public String getText() {
        if (cellObject instanceof ImdiTreeObject) {
            return (((ImdiTreeObject) cellObject).toString());
        } else if (cellObject instanceof ImdiTreeObject[]) {
            String cellText = "";
            Arrays.sort((ImdiTreeObject[]) cellObject, new Comparator() {

                public int compare(Object o1, Object o2) {
                    String value1 = o1.toString();
                    String value2 = o2.toString();
                    return value1.compareToIgnoreCase(value2);
                }
            });
            boolean hasAddedValues = false;
            for (ImdiTreeObject currentImdiTreeObject : (ImdiTreeObject[]) cellObject) {
                cellText = cellText + "[" + currentImdiTreeObject.toString() + "],";
                hasAddedValues = true;
            }
            if (hasAddedValues) {
                cellText = cellText.substring(0, cellText.length() - 1);
            }
            return (cellText);
        } else if (cellObject instanceof ImdiField[]) {
            return "<multiple values>";
        } else if (cellObject instanceof ImdiField && ((ImdiField) cellObject).isRequiredField() && ((ImdiField) cellObject).toString().length() == 0) {
            super.setForeground(Color.RED);
            return "<required field>";
        } else {
            return super.getText();
        }
    }
}
