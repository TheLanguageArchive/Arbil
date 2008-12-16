/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.util.Enumeration;
import javax.swing.table.DefaultTableModel;

/**
 * This table model is used to edit the field view for a given imdi table model
 * Split from ImdiFieldViews on : Dec 16, 2008, 10:27:30 AM
 * @author petwit
 */
public class ImdiFieldViewTableModel extends DefaultTableModel {

    private ImdiTableModel imdiTableModel;

    public ImdiFieldViewTableModel(ImdiTableModel localImdiTableModel) {
        imdiTableModel = localImdiTableModel;
        //System.out.println("setting to: " + viewLabel);
        setColumnIdentifiers(new String[]{"Column Name", "Column Description", "Show Only", "Hide"}); //, "Always Display"
        // we want a table model even if it has no rows
        LinorgFieldView currentView = imdiTableModel.getFieldView();
        if (currentView != null) {
            // loop the known columns
            for (Enumeration knownColumnNames = ((LinorgFieldView) currentView).getKnownColumns(); knownColumnNames.hasMoreElements();) {
                String currentFieldName = knownColumnNames.nextElement().toString();
                this.addRow(new Object[]{currentFieldName, GuiHelper.imdiSchema.getHelpForField(currentFieldName),
                            // set the show only fields
                            ((LinorgFieldView) currentView).isShowOnlyColumn(currentFieldName),
                            // set the hidden fields
                            ((LinorgFieldView) currentView).isHiddenColumn(currentFieldName)//,
                        // set alays show fields
                        //((LinorgFieldView) currentView).isAlwaysShowColumn(currentFieldName)
                        });
            }
        }

    }
    Class[] types = new Class[]{
        java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
    };
    private int showOnlyEnabledCount = -1;
    private int showOnlyColumn = 2;
    private int fieldNameColumn = 0;
    private int hideColumn = 3;

    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
//        System.out.println("isCellEditable showOnlyEnabledCount: " + showOnlyEnabledCount);
        if (column == hideColumn) {
            if (showOnlyEnabledCount < 0) { // count the show only selection
                showOnlyEnabledCount = 0;
                for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
                    //if (((Boolean) getValueAt(rowCounter, showOnlyColumn)) == true) {
                    if (getValueAt(rowCounter, showOnlyColumn).equals(true)) {
                        showOnlyEnabledCount++;
                    }
                }

            }
            return showOnlyEnabledCount == 0;
//                    return (getValueAt(row, showOnlyColumn).equals(true) || showOnlyEnabledCount == 0);
        }
        return (column == showOnlyColumn || column == hideColumn);
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
//        System.out.println("setValueAt showOnlyEnabledCount reset");
        // clear the show only count to retrigger the checking process
        showOnlyEnabledCount = -1;
//                fireTableDataChanged();
        boolean booleanState = aValue.equals(true);
        String targetColumnName = getValueAt(row, fieldNameColumn).toString();
        switch (column) {
            case 2:
                if (booleanState) {
                    imdiTableModel.getFieldView().addShowOnlyColumn(targetColumnName);
                } else {
                    imdiTableModel.getFieldView().removeShowOnlyColumn(targetColumnName);
                }
                break;
            case 3:
                if (booleanState) {
                    imdiTableModel.getFieldView().addHiddenColumn(targetColumnName);
                } else {
                    imdiTableModel.getFieldView().removeHiddenColumn(targetColumnName);
                }
                break;
            case 4:
                if (booleanState) {
                    imdiTableModel.getFieldView().addAlwaysShowColumn(targetColumnName);
                } else {
                    imdiTableModel.getFieldView().removeAlwaysShowColumn(targetColumnName);
                }
                break;
        }
        imdiTableModel.reloadTableData();
        fireTableStructureChanged();
    }//returnTableModel.setRowCount(0);
}

