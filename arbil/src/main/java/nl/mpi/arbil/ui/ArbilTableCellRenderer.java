/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilTableCell;

/**
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableCellRenderer extends DefaultTableCellRenderer {

    public int getRequiredWidth(FontMetrics fontMetrics, ArbilTableCell cellObject) {
        Object cellContent = getCellContent(cellObject);
        String currentCellString = getText();
        // Calculate width of text
        int width = fontMetrics.stringWidth(currentCellString);
        // ArbilField might have an icon
        if (cellContent instanceof ArbilField) {
            Icon icon = ArbilIcons.getSingleInstance().getIconForField((ArbilField) cellContent);
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
//        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ArbilTableCell cellObject = ((ArbilTableCell) value);
        Object cellContent = getCellContent(cellObject);
        // ArbilField may have to be decorated with an icon
        if (cellContent instanceof ArbilField) {
            Icon icon = ArbilIcons.getSingleInstance().getIconForField((ArbilField) cellContent);
            if (icon != null) {
                // An icon exists for this field, so wrap component with icon panel
                return new ArbilIconCellPanel(this, icon);
            }
        }
        setColours(cellContent, isSelected);
        if (cellObject == null) {
            setText("");
        } else {
            setText(cellObject.toString());
        }
        setIcon(getIcon(cellContent));
        return this;
    }

    public ArbilTableCellRenderer() {
    }

    protected final void setColours(Object cellContent, boolean isSelected) {
        if (cellContent instanceof ArbilField[]) {
            // Multi-valued field
            int greyTone = 150;
            super.setForeground(new Color(greyTone, greyTone, greyTone));
        }
        if (!isSelected) {
            if (cellContent instanceof ArbilFieldPlaceHolder || cellContent instanceof String && "".equals(cellContent)) {
                // Field does not exist in node OR childs column and node has no children of this type
                super.setBackground(new Color(230, 230, 230)/* Color.lightGray */);
            } else if (cellContent instanceof ArbilField && ((ArbilField) cellContent).fieldNeedsSaveToDisk()) {
                // Value has changed since last save
                super.setForeground(Color.blue);
            }
        }
        if (cellContent instanceof ArbilField && ((ArbilField) cellContent).isRequiredField() && ((ArbilField) cellContent).getFieldValue().length() == 0) {
            super.setForeground(Color.RED);
        } else if (cellContent instanceof ArbilField && !((ArbilField) cellContent).fieldValueValidates()) {
            super.setForeground(Color.RED);
        }
    }

    public Icon getIcon(Object cellContent) {
        if (cellContent instanceof ArbilDataNode) {
            return (((ArbilDataNode) cellContent).getIcon());
        } else if (cellContent instanceof ArbilDataNode[]) {
            return (ArbilIcons.getSingleInstance().getIconForNode((ArbilDataNode[]) cellContent));
        } else if (cellContent instanceof ArbilField[]) {
            return null;
//        } else if (cellObject instanceof ArbilField) {
//            return ArbilIcons.getSingleInstance().getIconForVocabulary((ArbilField) cellObject);
        } else {
            return (null);
        }
    }

    /**
     * @return the cellObject
     */
    private Object getCellContent(ArbilTableCell cellObject) {
        if (cellObject == null) {
            return null;
        } else {
            return cellObject.getContent();
        }
    }
}