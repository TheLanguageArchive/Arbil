package nl.mpi.arbil.ui;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Class that defines a collection of columns that should be shown or hidden
 * in a table view
 * Document   : ArbilFieldView
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilFieldView implements Serializable {

    private Vector hiddenColumns = new Vector();
    private Vector showOnlyColumns = new Vector();
    private Vector knownColumns = new Vector();
    private Vector alwaysShowColumns = new Vector();
    private HashMap<String, Integer> columnWidths = new HashMap<String, Integer>();

    public void showState() {
        System.out.println("knownColumns: " + knownColumns);
        System.out.println("hiddenColumns: " + hiddenColumns);
        System.out.println("showOnlyColumns: " + showOnlyColumns);
        System.out.println("alwaysShowColumns: " + alwaysShowColumns);
        System.out.println("columnWidths: " + columnWidths);
    }

    public void setAlwaysShowColumns(Vector alwaysShowColumns) {
        this.alwaysShowColumns = alwaysShowColumns;
    }

    public Enumeration getAlwaysShowColumns() {
        return this.alwaysShowColumns.elements();
    }

    public void setHiddenColumns(Vector hiddenColumns) {
        this.hiddenColumns = hiddenColumns;
    }

    private void setKnownColumns(Vector knownColumns) {
        this.knownColumns = knownColumns;
    }

    public void setShowOnlyColumns(Vector showOnlyColumns) {
        this.showOnlyColumns = showOnlyColumns;
    }

    public ArbilFieldView clone() {
        ArbilFieldView returnFieldView = new ArbilFieldView();
        returnFieldView.setAlwaysShowColumns((Vector) alwaysShowColumns.clone());
        returnFieldView.setHiddenColumns((Vector) hiddenColumns.clone());
        returnFieldView.setKnownColumns((Vector) knownColumns.clone());
        returnFieldView.setShowOnlyColumns((Vector) showOnlyColumns.clone());
        returnFieldView.columnWidths = (HashMap<String, Integer>) columnWidths.clone();
        return returnFieldView;
    }

    public void addKnownColumn(String columnName) {
        if (!knownColumns.contains(columnName)) {
            knownColumns.add(columnName);
        }
    }

    public void setShowOnlyColumns(String[] columnsToShow) {
        showOnlyColumns.clear();
        for (int columnCounter = 0; columnCounter < columnsToShow.length; columnCounter++) {
            showOnlyColumns.add(columnsToShow[columnCounter]);
        }
    }

    public void addAlwaysShowColumn(String columnName) {
        System.out.println("addAlwaysShowColumn");
        alwaysShowColumns.add(columnName);
        showState();
    }

    public void removeAlwaysShowColumn(String columnName) {
        System.out.println("removeAlwaysShowColumn");
        alwaysShowColumns.remove(columnName);
        showState();
    }

    public void addShowOnlyColumn(String columnName) {
        System.out.println("addShowOnlyColumn");
        showOnlyColumns.add(columnName);
        showState();
    }

    public void removeShowOnlyColumn(String columnName) {
        System.out.println("removeShowOnlyColumn");
        showOnlyColumns.remove(columnName);
        showState();
    }

    public void addHiddenColumn(String columnName) {
        System.out.println("addHiddenColumn");
        hiddenColumns.add(columnName);
        showOnlyColumns.remove(columnName);
        showState();
    }

    public void removeHiddenColumn(String columnName) {
        System.out.println("removeHiddenColumn");
        hiddenColumns.remove(columnName);
        showState();
    }

    public boolean viewShowsColumn(String currentColumnString) {
        boolean showColumn = true;
//    hiddenColumns, showOnlyColumns, knownColumns, alwaysShowColumns
        if (showOnlyColumns.size() > 0) {
            // set to true if it is in the show only list
            showColumn = showOnlyColumns.contains(currentColumnString);
        } else { // if (showColumn) { // this else makes the selection exclusive 
            // set to false if in the hidden list
            showColumn = !hiddenColumns.contains(currentColumnString);
        }
        if (!showColumn) {
            // set to true if in the always show list
            showColumn = alwaysShowColumns.contains(currentColumnString);
        }
        return showColumn;
    }

    public Enumeration getKnownColumns() {
        Collections.sort(knownColumns);
        return (knownColumns).elements();
    }

    public boolean isShowOnlyColumn(String columnString) {
        return showOnlyColumns.contains(columnString);
    }

    public boolean isHiddenColumn(String columnString) {
        return hiddenColumns.contains(columnString);
    }

    public boolean isAlwaysShowColumn(String columnString) {
        return alwaysShowColumns.contains(columnString);
    }

    /**
     * Sets the preferred column width for the specified column
     * @param columnString Name of column to set width for
     * @param width Preferred width for specified column. Null to remove preference
     */
    public void setColumnWidth(String columnString, Integer width) {
        if (width != null) {
            columnWidths.put(columnString, width);
        } else if (hasColumnWidthForColumn(columnString)) {
            columnWidths.remove(columnString);
        }
    }

    public boolean hasColumnWidthForColumn(String columnString) {
        return columnWidths.containsKey(columnString);
    }

    /**
     * @param columnString Name of the column to get the width for
     * @return Preferred width of column, if set. Otherwise null;
     */
    public Integer getColumnWidth(String columnString) {
        if (columnWidths.containsKey(columnString)) {
            return columnWidths.get(columnString);
        } else {
            return null;
        }
    }

    public void storeColumnWidths(TableColumnModel columnModel) {
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            if (column.getHeaderValue() instanceof String) {
                setColumnWidth((String) column.getHeaderValue(), column.getWidth());
            }
        }
    }
}
