package nl.mpi.arbil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

/**
 * Document   : ImdiChildCellEditor
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
class ImdiChildCellEditor extends AbstractCellEditor implements TableCellEditor {

    ImdiTable parentTable = null;
    ImdiTreeObject registeredOwner = null;
    JPanel editorPanel;
    JLabel button;
    String columnName;
    Object rowImdi;
    Component editorComponent = null;
    boolean receivedKeyDown = false;
    Object[] cellValue;
    int selectedField = -1;
    JTextArea fieldEditors[] = null;
    JComboBox fieldLanguageBoxs[] = null;

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

            @Override
            public void mouseReleased(MouseEvent evt) {
                parentTable.checkPopup(evt, false);
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                parentTable.checkPopup(evt, false);
            }
        
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                boolean ctrlDown = evt.isControlDown();
                if (evt.getButton() == MouseEvent.BUTTON1) {
                    startEditorMode(ctrlDown);
                } else {
                    parentTable.checkPopup(evt, false);
                    //super.mousePressed(evt);
                }
            }
        });
    }

    private void addFocusListener(Component editorComponent) {
        editorComponent.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                if (e.getComponent().getParent() != null) {
                    if (!e.getOppositeComponent().getParent().equals(editorPanel)) {
                        ImdiChildCellEditor.this.stopCellEditing();
                    }
                }
            }
        });
    }

    public void updateEditor(ImdiTreeObject parentImdiObject) {
        // this will only be called when the long field editor is shown
        // when an imdi node is edited or saved or reloaded this will be called to update the displayed values
        if (cellValue instanceof ImdiField[]) {
            String fieldName = ((ImdiField[]) cellValue)[0].getTranslateFieldName();
            cellValue = parentImdiObject.getFields().get(fieldName);
            for (int cellFieldCounter = 0; cellFieldCounter < cellValue.length; cellFieldCounter++) {
                fieldEditors[cellFieldCounter].setText(((ImdiField[]) cellValue)[cellFieldCounter].getFieldValue());
                if (fieldLanguageBoxs[cellFieldCounter] != null) {
                    fieldLanguageBoxs[cellFieldCounter].setSelectedItem(((ImdiField[]) cellValue)[cellFieldCounter].getLanguageId());
                    ;
                }
            }
        }
    }
    private JComboBox getLanguageIdBox(final int cellFieldIndex) {
        ImdiField cellField = (ImdiField) cellValue[cellFieldIndex];
        String fieldLanguageId = cellField.getLanguageId();
        if (fieldLanguageId != null) {
            System.out.println("Has LanguageId");
            final JComboBox comboBox = new JComboBox();
            final String defaultValue = "<select>";
            ImdiVocabularies.VocabularyItem selectedItem = null;
            comboBox.setEditable(false);
            for (Enumeration<ImdiVocabularies.VocabularyItem> vocabularyList = cellField.getLanguageList(); vocabularyList.hasMoreElements();) {
                ImdiVocabularies.VocabularyItem currentItem = vocabularyList.nextElement();
                comboBox.addItem(currentItem);
                if (fieldLanguageId.equals(currentItem.languageCode)) {
                    selectedItem = currentItem;
                }
            }
            if (selectedItem != null) {
                System.out.println("selectedItem: " + selectedItem);
                comboBox.setSelectedItem(selectedItem);
            } else {
                comboBox.addItem(defaultValue);
                comboBox.setSelectedItem(defaultValue);
            }
            comboBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ImdiField cellField = (ImdiField) cellValue[cellFieldIndex];
                    cellField.setLanguageId(((ImdiVocabularies.VocabularyItem) comboBox.getSelectedItem()).languageCode, true);
                    comboBox.removeItem(defaultValue);
                }
            });
            return comboBox;
        } else {
            return null;
        }
    }
    private void startEditorMode(boolean ctrlDown) {
        System.out.println("startEditorMode: " + selectedField);
        if (cellValue instanceof ImdiField[]) {
            if (!ctrlDown && selectedField != -1 && !((ImdiField) cellValue[selectedField]).isLongField()) {
                if (((ImdiField) cellValue[selectedField]).hasVocabulary()) {
                    System.out.println("Has Vocabulary");
                    JComboBox comboBox = new JComboBox();
                    comboBox.setEditable(((ImdiField) cellValue[selectedField]).vocabularyIsOpen);
                    for (Enumeration<ImdiVocabularies.VocabularyItem> vocabularyList = ((ImdiField) cellValue[selectedField]).getVocabulary(); vocabularyList.hasMoreElements();) {
                        comboBox.addItem(vocabularyList.nextElement().languageName);
                    }
                    // TODO: enable multiple selection for vocabulary lists
                    comboBox.setSelectedItem(cellValue[selectedField].toString());
                    editorPanel.remove(button);
                    editorPanel.add(comboBox);
                    addFocusListener(comboBox);
                    editorPanel.doLayout();
                    comboBox.setPopupVisible(true);
                    comboBox.requestFocusInWindow();
                    editorComponent = comboBox;
                } else {
                    editorPanel.remove(button);
                    editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
                    JTextField editorTextField = new JTextField(cellValue[selectedField].toString());
                    editorTextField.setMinimumSize(new Dimension(50, (int) editorTextField.getMinimumSize().getHeight()));
                    editorPanel.add(editorTextField);
                    addFocusListener(editorTextField);
                    JComboBox fieldLanguageBox = getLanguageIdBox(selectedField);
                    if (fieldLanguageBox != null) {
                        editorPanel.add(fieldLanguageBox);
                        addFocusListener(fieldLanguageBox);
                    }
                    editorPanel.doLayout();
                    editorTextField.requestFocusInWindow();
                    editorComponent = editorTextField;
                }
            } else {
                int titleCount = 1;
                JTextArea focusedTabTextArea = null;
                JTabbedPane tabPane = new JTabbedPane();

                fieldEditors = new JTextArea[cellValue.length];
                fieldLanguageBoxs = new JComboBox[cellValue.length];

                for (int cellFieldCounter = 0; cellFieldCounter < cellValue.length; cellFieldCounter++) {
                    final int cellFieldIndex = cellFieldCounter;
//                    ImdiField cellField = ((ImdiField) cellValue[cellFieldIndex]);
//                    final ImdiField sourceField = cellValueItem;
                    fieldEditors[cellFieldIndex] = new JTextArea();
                    if (focusedTabTextArea == null || selectedField == cellFieldCounter) {
                        // set the selected field as the first one or in the case of a single node being selected tab to its pane
                        focusedTabTextArea = fieldEditors[cellFieldIndex];
                    }
                    fieldEditors[cellFieldIndex].setEditable(((ImdiField) cellValue[cellFieldIndex]).parentImdi.getParentDomNode().isLocal());
                    fieldEditors[cellFieldIndex].addFocusListener(new FocusListener() {

                        public void focusGained(FocusEvent e) {
                        }

                        public void focusLost(FocusEvent e) {
                            ImdiField cellField = (ImdiField) cellValue[cellFieldIndex];
                            if (cellField.parentImdi.getParentDomNode().isLocal()) {
                                cellField.setFieldValue(fieldEditors[cellFieldIndex].getText(), true);
                            }
                        }
                    });
                    fieldEditors[cellFieldIndex].setText(((ImdiField) cellValue[cellFieldIndex]).getFieldValue());
                    fieldEditors[cellFieldIndex].setLineWrap(true);
                    fieldEditors[cellFieldIndex].setWrapStyleWord(true);

                    JPanel tabPanel = new JPanel();
                    tabPanel.setLayout(new BorderLayout());
                    fieldLanguageBoxs[cellFieldIndex] = getLanguageIdBox(cellFieldIndex);
                    if (fieldLanguageBoxs[cellFieldIndex] != null) {
                        tabPanel.add(fieldLanguageBoxs[cellFieldIndex], BorderLayout.PAGE_START);
                    }
                    tabPanel.add(new JScrollPane(fieldEditors[cellFieldIndex]), BorderLayout.CENTER);
                    tabPane.add(((ImdiField) cellValue[cellFieldIndex]).getTranslateFieldName() + " " + titleCount++, tabPanel);
                }
                registeredOwner = ((ImdiField) cellValue[0]).parentImdi;
                registeredOwner.registerContainer(this);
                JInternalFrame editorFrame = LinorgWindowManager.getSingleInstance().createWindow(columnName + " in " + rowImdi, tabPane);
                editorFrame.addInternalFrameListener(new InternalFrameAdapter() {

                    @Override
                    public void internalFrameClosed(InternalFrameEvent e) {
                        // deregister component from imditreenode
                        registeredOwner.removeContainer(this);
                        super.internalFrameClosed(e);
                    }
                });
                if (selectedField != -1) {
                    tabPane.setSelectedIndex(selectedField);
                }
                focusedTabTextArea.requestFocusInWindow();
                fireEditingStopped();
            }
        } else {
            LinorgWindowManager.getSingleInstance().openFloatingTable((new Vector(Arrays.asList((Object[]) cellValue))).elements(), columnName + " in " + rowImdi);
        }
    }

    public Object getCellEditorValue() {
//        System.out.println("getCellEditorValue");
        if (selectedField != -1) {
            if (editorComponent != null) {
                if (editorComponent instanceof JComboBox) {
                    ((ImdiField[]) cellValue)[selectedField].setFieldValue(((JComboBox) editorComponent).getSelectedItem().toString(), true);
                } else if (editorComponent instanceof JTextField) {
                    ((ImdiField[]) cellValue)[selectedField].setFieldValue(((JTextField) editorComponent).getText(), true);
                }
            }
            return cellValue[selectedField];
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
        parentTable = (ImdiTable)table;
        if (value instanceof ImdiField) {
            // TODO: get the whole array from the parent and select the correct tab for editing
            String fieldName = ((ImdiField) value).getTranslateFieldName();
            cellValue = ((ImdiField) value).parentImdi.getFields().get(fieldName);
            // TODO: find the chosen fields index in the array and store
            for (int cellFieldCounter = 0; cellFieldCounter < cellValue.length; cellFieldCounter++) {
                System.out.println("selectedField: " + cellValue[cellFieldCounter] + " : " + value);
                if (cellValue[cellFieldCounter].equals(value)) {
                    System.out.println("selectedField found");
                    selectedField = cellFieldCounter;
                }
            }
        } else {
            cellValue = (Object[]) value;
        }
        columnName = table.getColumnName(column);
        rowImdi = table.getValueAt(row, 0);
        if (cellValue instanceof ImdiField[]) {
            button.setText(((DefaultTableCellRenderer) table.getCellRenderer(row, column)).getText());
            button.setForeground(((DefaultTableCellRenderer) table.getCellRenderer(row, column)).getForeground());
        } else {
            button.setIcon(ImdiIcons.getSingleInstance().getIconForImdi((ImdiTreeObject[]) cellValue));
            button.setText("");
        }
        editorPanel.setBackground(table.getSelectionBackground());
        editorPanel.setLayout(new BorderLayout());
        editorPanel.add(button);
        addFocusListener(button);
        //table.requestFocusInWindow();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                button.requestFocusInWindow();
            }
        });
        return editorPanel;
    }
}
