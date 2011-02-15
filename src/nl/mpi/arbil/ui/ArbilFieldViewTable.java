package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ImdiTableModel;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Document   : ImdiFieldViewTable
 * Used to edit the field view of an imdi table
 * Created on : Jan 5, 2009, 12:40:37 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilFieldViewTable extends JTable {

    public ArbilFieldViewTable(ImdiTableModel imdiTableModel) {
        TableModel tableModel = new ArbilFieldViewTableModel(imdiTableModel);
        setModel(tableModel);
        this.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {
                System.out.println("mousePressed");
                checkPopup(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                System.out.println("mouseReleased");
                checkPopup(evt);
            }

//            @Override
//            public void mouseClicked(java.awt.event.MouseEvent evt) {
//                System.out.println("mouseClicked");
//                checkPopup(evt);
//            }

            private void checkPopup(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()*/) {
                    // set the clicked cell selected
                    java.awt.Point p = evt.getPoint();
                    int clickedColumn = columnAtPoint(p);
                    System.out.println("clickedColumn: " + clickedColumn);
                    if (clickedColumn == 2 || clickedColumn == 3) {
                        JPopupMenu viewPopupMenu = new javax.swing.JPopupMenu();
                        viewPopupMenu.setName("viewPopupMenu");

                        JMenuItem selectedAllMenuItem = new javax.swing.JMenuItem();
                        selectedAllMenuItem.setText("Selected All");
                        viewPopupMenu.setInvoker(evt.getComponent());
                        selectedAllMenuItem.setActionCommand("" + clickedColumn);
                        selectedAllMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                try {
                                    int targetColumn = Integer.parseInt(evt.getActionCommand());
                                    ArbilFieldViewTableModel fieldViewTableModel = (ArbilFieldViewTableModel) ((JTable) ((JPopupMenu) ((JMenuItem) evt.getSource()).getComponent().getParent()).getInvoker()).getModel();
                                    System.out.println("targetColumn: " + targetColumn + ":" + evt.getActionCommand());
                                    for (int rowCounter = 0; rowCounter < fieldViewTableModel.getRowCount(); rowCounter++) {
                                        fieldViewTableModel.setValueAt(true, rowCounter, targetColumn);
                                    }
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(ex);
                                }
                            }
                        });
                        viewPopupMenu.add(selectedAllMenuItem);

                        JMenuItem selectetNodeMenuItem = new javax.swing.JMenuItem();
                        selectetNodeMenuItem.setText("Selected None");
                        selectetNodeMenuItem.setActionCommand("" + clickedColumn);
                        selectetNodeMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                try {
                                    int targetColumn = Integer.parseInt(evt.getActionCommand());
                                    ArbilFieldViewTableModel fieldViewTableModel = (ArbilFieldViewTableModel) ((JTable) ((JPopupMenu) ((JMenuItem) evt.getSource()).getComponent().getParent()).getInvoker()).getModel();
                                    System.out.println("targetColumn: " + targetColumn + ":" + evt.getActionCommand());
                                    for (int rowCounter = 0; rowCounter < fieldViewTableModel.getRowCount(); rowCounter++) {
                                        fieldViewTableModel.setValueAt(false, rowCounter, targetColumn);
                                    }
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(ex);
                                }
                            }
                        });
                        viewPopupMenu.add(selectetNodeMenuItem);

                        viewPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
            }
        });
    }

    @Override
    //Implement table cell tool tips.
    public String getToolTipText(MouseEvent e) {
        java.awt.Point p = e.getPoint();
        switch (columnAtPoint(p)) {
            case 2:
                return "Show only checked fields (hides all others and overrides hide fields)";
            case 3:
                return "Hide checked fields (only active when no 'show only' selection is made)";
        }
        return null;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        TableCellRenderer tableCellRenderer = super.getCellRenderer(row, column);
        if (tableCellRenderer instanceof JCheckBox) {
            ((JCheckBox) tableCellRenderer).setEnabled(getModel().isCellEditable(row, column));
        }
        return tableCellRenderer;
    }
}
