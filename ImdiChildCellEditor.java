/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Component;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author petwit
 */
class ImdiChildCellEditor extends AbstractCellEditor implements TableCellEditor {

    JLabel button;
    Object cellValue;
    String columnName;
    Object rowImdi;

    public ImdiChildCellEditor() {
        button = new JLabel("...");
        button.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() > 1) {
                    GuiHelper.linorgWindowManager.openFloatingTable((new Vector(Arrays.asList((Object[]) cellValue))).elements(), columnName + " in " + rowImdi);
                }
            }
        });
    }

    public Object getCellEditorValue() {
        return cellValue;
    }

    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
        columnName = table.getColumnName(column);
        rowImdi = table.getValueAt(row, 0);
        cellValue = value;
        return button;
    }
}
