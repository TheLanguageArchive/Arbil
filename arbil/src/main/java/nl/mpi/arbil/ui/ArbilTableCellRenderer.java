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
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilTableCell;

/**
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableCellRenderer extends DefaultTableCellRenderer {

    private ArbilTableCell cellObject;
    boolean isCellSelected = false;

    public int getRequiredWidth(FontMetrics fontMetrics) {
	String currentCellString = getText();
	// Calculate width of text
	int width = fontMetrics.stringWidth(currentCellString);
	// ArbilField might have an icon
	if (getCellContent() instanceof ArbilField) {
	    Icon icon = ArbilIcons.getSingleInstance().getIconForField((ArbilField) getCellContent());
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
	if (getCellContent() instanceof ArbilField) {
	    Icon icon = ArbilIcons.getSingleInstance().getIconForField((ArbilField) getCellContent());
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
    public ArbilTableCellRenderer(ArbilTableCell cellObject) {
	setValue(cellObject);
    }

    @Override
    protected final void setValue(Object value) {
	super.setValue(value);
	cellObject = ((ArbilTableCell) value);
	if (getCellContent() instanceof ArbilField[]) {
	    // Multi-valued field
	    int greyTone = 150;
	    super.setForeground(new Color(greyTone, greyTone, greyTone));
	}
	if (!isCellSelected) {
	    if (getCellContent() instanceof ArbilFieldPlaceHolder || getCellContent() instanceof String && "".equals(getCellContent())) {
		// Field does not exist in node OR childs column and node has no children of this type
		super.setBackground(new Color(230, 230, 230)/*Color.lightGray*/);
	    } else if (getCellContent() instanceof ArbilField && ((ArbilField) getCellContent()).fieldNeedsSaveToDisk()) {
		// Value has changed since last save
		super.setForeground(Color.blue);
	    }
	}
    }

    @Override
    public Icon getIcon() {
	if (getCellContent() instanceof ArbilDataNode) {
	    return (((ArbilDataNode) getCellContent()).getIcon());
	} else if (getCellContent() instanceof ArbilDataNode[]) {
	    return (ArbilIcons.getSingleInstance().getIconForNode((ArbilDataNode[]) getCellContent()));
	} else if (getCellContent() instanceof ArbilField[]) {
	    return null;
//        } else if (cellObject instanceof ArbilField) {
//            return ArbilIcons.getSingleInstance().getIconForVocabulary((ArbilField) cellObject);
	} else {
	    return (null);
	}
    }

    @Override
    public String getText() {
	if (cellObject == null) {
	    return super.toString();
	} else {
	    if (getCellContent() instanceof ArbilField && ((ArbilField) getCellContent()).isRequiredField() && ((ArbilField) getCellContent()).toString().length() == 0) {
		super.setForeground(Color.RED);
	    } else if (getCellContent() instanceof ArbilField && !((ArbilField) getCellContent()).fieldValueValidates()) {
		super.setForeground(Color.RED);
	    }
	    return cellObject.toString();
	}
//	
//	if (getCellObject() instanceof ArbilDataNode) {
//	    return (((ArbilDataNode) getCellObject()).toString());
//	} else if (getCellObject() instanceof ArbilDataNode[]) {
//	    String cellText = "";
//	    Arrays.sort((ArbilDataNode[]) getCellObject(), new Comparator() {
//
//		public int compare(Object o1, Object o2) {
//		    String value1 = o1.toString();
//		    String value2 = o2.toString();
//		    return value1.compareToIgnoreCase(value2);
//		}
//	    });
//	    boolean hasAddedValues = false;
//	    for (ArbilDataNode currentArbilDataNode : (ArbilDataNode[]) getCellObject()) {
//		cellText = cellText + "[" + currentArbilDataNode.toString() + "],";
//		hasAddedValues = true;
//	    }
//	    if (hasAddedValues) {
//		cellText = cellText.substring(0, cellText.length() - 1);
//	    }
//	    return (cellText);
//	} else if (getCellObject() instanceof ArbilField[]) {
//	    return "<multiple values>";
//	} else if (getCellObject() instanceof ArbilField && ((ArbilField) getCellObject()).isRequiredField() && ((ArbilField) getCellObject()).toString().length() == 0) {
//	    super.setForeground(Color.RED);
//	    return "<required field>";
//	} else if (getCellObject() instanceof ArbilField && !((ArbilField) getCellObject()).fieldValueValidates()) {
//	    super.setForeground(Color.RED);
//	    return super.getText();
//	} else {
//	    return super.getText();
//	}
    }

    /**
     * @return the cellObject
     */
    private Object getCellContent() {
	if (cellObject == null) {
	    return null;
	} else {
	    return cellObject.getContent();
	}
    }
}
