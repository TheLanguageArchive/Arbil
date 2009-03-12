/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author petwit
 */
class ImdiChildCellEditor extends AbstractCellEditor implements TableCellEditor {

    JPanel editorPanel;
    JLabel button;
    Object[] cellValue;
    String columnName;
    Object rowImdi;
    Component editorComponent = null;
    boolean receivedKeyDown = false;

    public ImdiChildCellEditor() {
        button = new JLabel("...");
        editorPanel = new JPanel();
        button.addKeyListener(new java.awt.event.KeyListener() {

            public void keyTyped(KeyEvent evt) {
            }

            public void keyPressed(KeyEvent evt) {
                receivedKeyDown = true; // this is to prevent reopening the editor after a ctrl_w has closed the editor
            }

            public void keyReleased(KeyEvent evt) {
                if (receivedKeyDown) {
                    boolean ctrlDown = evt.isControlDown();
                    startEditorMode(ctrlDown);
                    receivedKeyDown = false;
                }
            }
        });
        button.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                boolean ctrlDown = evt.isControlDown();
                if (evt.getClickCount() > 1) {
                    startEditorMode(ctrlDown);
                }
            }
        });
    }

    private void startEditorMode(boolean ctrlDown) {
        System.out.println("startEditorMode");
        if (cellValue instanceof ImdiField[]) {
            if (!ctrlDown && !((ImdiField) cellValue[0]).isLongField() && cellValue.length == 1) {
                if (((ImdiField) cellValue[0]).hasVocabulary()) {
                    System.out.println("Has Vocabulary");
                    JComboBox comboBox = new JComboBox();
                    comboBox.setEditable(((ImdiField) cellValue[0]).vocabularyIsOpen);
                    for (Enumeration vocabularyList = ((ImdiField) cellValue[0]).getVocabulary(); vocabularyList.hasMoreElements();) {
                        comboBox.addItem(vocabularyList.nextElement());
                    }
                    // TODO: enable multiple selection for vocabulary lists
                    comboBox.setSelectedItem(cellValue[0].toString());
                    editorPanel.remove(button);
                    editorPanel.add(comboBox);
                    editorPanel.doLayout();
                    comboBox.setPopupVisible(true);
                    comboBox.requestFocusInWindow();
                    editorComponent = comboBox;
                } else {
                    editorPanel.remove(button);
                    JTextField editorTextField = new JTextField(cellValue[0].toString());
                    editorPanel.add(editorTextField);
                    editorPanel.doLayout();
                    editorTextField.requestFocusInWindow();
                    editorComponent = editorTextField;
                }
            } else {
                int titleCount = 1;
                JTextArea firstTabTextArea = null;
                JTabbedPane tabPane = new JTabbedPane();
                for (ImdiField cellValueItem : (ImdiField[]) cellValue) {
                    final ImdiField sourceField = cellValueItem;
                    final JTextArea fieldEditor = new JTextArea();
                    if (firstTabTextArea == null) {
                        firstTabTextArea = fieldEditor;
                    }
                    fieldEditor.setEditable(sourceField.parentImdi.getParentDomNode().isLocal());
                    fieldEditor.addFocusListener(new FocusListener() {

                        public void focusGained(FocusEvent e) {
                            fieldEditor.setText(sourceField.getFieldValue());
                        }

                        public void focusLost(FocusEvent e) {
                            if (sourceField.parentImdi.getParentDomNode().isLocal()) {
                                sourceField.setFieldValue(fieldEditor.getText());
                            }
                        }
                    });
                    fieldEditor.setText(cellValueItem.getFieldValue());
                    fieldEditor.setLineWrap(true);
                    fieldEditor.setWrapStyleWord(true);
                    tabPane.add(cellValueItem.getTranslateFieldName() + " " + titleCount++, new JScrollPane(fieldEditor));
                }
                GuiHelper.linorgWindowManager.createWindow(columnName + " in " + rowImdi, tabPane);
                firstTabTextArea.requestFocusInWindow();
            }
        } else {
            GuiHelper.linorgWindowManager.openFloatingTable((new Vector(Arrays.asList((Object[]) cellValue))).elements(), columnName + " in " + rowImdi);
        }
    }

    public Object getCellEditorValue() {
//        System.out.println("getCellEditorValue");
        if (((Object[]) cellValue).length == 1) {
            if (editorComponent != null) {
                if (editorComponent instanceof JComboBox) {
                    ((ImdiField[]) cellValue)[0].setFieldValue(((JComboBox) editorComponent).getSelectedItem().toString());
                } else if (editorComponent instanceof JTextField) {
                    ((ImdiField[]) cellValue)[0].setFieldValue(((JTextField) editorComponent).getText());
                }
            }
            return ((Object[]) cellValue)[0];
        } else {
            return cellValue;
        }
    }

    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
        receivedKeyDown = true;
        if (value instanceof ImdiField) {
            cellValue = new ImdiField[]{(ImdiField) value}; // TODO: something going funny saving after this edit
        } else {
            cellValue = (Object[]) value;
        }
        columnName = table.getColumnName(column);
        rowImdi = table.getValueAt(row, 0);
        if (cellValue instanceof ImdiField[]) {
            button.setText(((DefaultTableCellRenderer) table.getCellRenderer(row, column)).getText());
            button.setForeground(((DefaultTableCellRenderer) table.getCellRenderer(row, column)).getForeground());
        } else {
            button.setIcon(ImdiTreeObject.imdiIcons.getIconForImdi((ImdiTreeObject[]) cellValue));
            button.setText("");
        }
        editorPanel.setLayout(new BorderLayout());
        editorPanel.add(button);
        //table.requestFocusInWindow();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                button.requestFocusInWindow();
            }
        });
        return editorPanel;
    }
}
