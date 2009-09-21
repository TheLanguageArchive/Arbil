package nl.mpi.arbil;

import java.util.Enumeration;
import javax.swing.table.DefaultTableModel;

/**
 * Document   : ImdiFieldViewTableModel
 * This table model is used to edit the field view for a given imdi table model
 * Split from ImdiFieldViews on : Dec 16, 2008, 10:27:30 AM
 * @author Peter.Withers@mpi.nl
 */
public class ImdiFieldViewTableModel extends DefaultTableModel {

    private ImdiTableModel imdiTableModel;

    public ImdiFieldViewTableModel(ImdiTableModel localImdiTableModel) {
        imdiTableModel = localImdiTableModel;
        //System.out.println("setting to: " + viewLabel);
        // "Column Description",  is not relevant here because this is a list of column names not imdi fields
        setColumnIdentifiers(new String[]{"Column Name", "Show Only", "Hide"}); //, "Always Display"
        // we want a table model even if it has no rows
        LinorgFieldView currentView = imdiTableModel.getFieldView();
        if (currentView != null) {
            // loop the known columns
            for (Enumeration knownColumnNames = ((LinorgFieldView) currentView).getKnownColumns(); knownColumnNames.hasMoreElements();) {
                String currentFieldName = knownColumnNames.nextElement().toString();
                this.addRow(new Object[]{currentFieldName,
                            //GuiHelper.imdiSchema.getHelpForField(currentFieldName),
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
        java.lang.String.class, /*java.lang.String.class,*/ java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
    };
    private int showOnlyEnabledCount = -1;
    private final int showOnlyColumn = 1;
    private final int fieldNameColumn = 0;
    private final int hideColumn = 2;

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
            case showOnlyColumn:
                if (booleanState) {
                    imdiTableModel.getFieldView().addShowOnlyColumn(targetColumnName);
                } else {
                    imdiTableModel.getFieldView().removeShowOnlyColumn(targetColumnName);
                }
                break;
            case hideColumn:
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
        imdiTableModel.requestReloadTableData();
        fireTableStructureChanged();
    }//returnTableModel.setRowCount(0);
}

