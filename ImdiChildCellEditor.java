package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
import javax.swing.table.TableCellEditor;

/**
 * Document   : ImdiChildCellEditor
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
class ImdiChildCellEditor extends AbstractCellEditor implements TableCellEditor {

    ImdiTable parentTable = null;
    Rectangle parentCellRect = null;
    ImdiTreeObject registeredOwner = null;
    JPanel editorPanel;
    JLabel button;
    String columnName;
    Object rowImdi;
    Component editorComponent = null;
//    boolean receivedKeyDown = false;
    Object[] cellValue;
    int selectedField = -1;
    JTextArea fieldEditors[] = null;
    JTextField keyEditorFields[] = null;
    JComboBox fieldLanguageBoxs[] = null;
    Vector<Component> componentsWithFocusListners = new Vector();

    public ImdiChildCellEditor() {
        button = new JLabel("...");
        editorPanel = new JPanel();
        button.addKeyListener(new java.awt.event.KeyListener() {

            public void keyTyped(KeyEvent evt) {
            }

            public void keyPressed(KeyEvent evt) {
//                receivedKeyDown = true; // this is to prevent reopening the editor after a ctrl_w has closed the editor
            }

            public void keyReleased(KeyEvent evt) {
                if (isStartLongFieldModifier(evt)) {
                    // prevent ctrl key events getting through etc.
                    startEditorMode(isStartLongFieldKey(evt), KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
                } else if (evt.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                    startEditorMode(isStartLongFieldKey(evt), evt.getKeyCode(), evt.getKeyChar());
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
                if (evt.getButton() == MouseEvent.BUTTON1) {
                    startEditorMode(isStartLongFieldModifier(evt), KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
                } else {
                    parentTable.checkPopup(evt, false);
                //super.mousePressed(evt);
                }
            }
        });
    }

    private boolean isStartLongFieldKey(KeyEvent evt) {
        return (isStartLongFieldModifier(evt) && evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER);
    }

    private boolean isStartLongFieldModifier(InputEvent evt) {
        return ((evt.isShiftDown() || evt.isControlDown()));
    }

    private void removeAllFocusListners() {
        while (componentsWithFocusListners.size() > 0) {
            Component currentComponent = componentsWithFocusListners.remove(0);
            if (currentComponent != null) {
                System.out.println("removeAllFocusListners:currentComponent:" + currentComponent.getClass());
                for (FocusListener currentListner : currentComponent.getFocusListeners()) {
                    currentComponent.removeFocusListener(currentListner);
                }
            }
        }
    }

    private void addFocusListener(Component targetComponent) {
        componentsWithFocusListners.add(targetComponent);
        targetComponent.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                boolean oppositeIsParent = false;
                try {
                    oppositeIsParent = (e.getComponent().equals(e.getOppositeComponent().getParent()) || e.getComponent().getParent().equals(e.getOppositeComponent()));
                    if (!oppositeIsParent && e.getComponent().getParent() != null) {
                        if (!e.getOppositeComponent().getParent().equals(editorPanel)) {
                            ImdiChildCellEditor.this.stopCellEditing();
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("OppositeComponent or parent container not set");
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
                }
                if (keyEditorFields[cellFieldCounter] != null) {
                    keyEditorFields[cellFieldCounter].setText(((ImdiField[]) cellValue)[cellFieldCounter].getKeyName());
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
            comboBox.setPreferredSize(new Dimension(80, parentCellRect.height));
            return comboBox;
        } else {
            return null;
        }
    }

    private String getEditorText(int lastKeyInt, char lastKeyChar, String currentCellString) {
        switch (lastKeyInt) {
            case KeyEvent.VK_DELETE:
                currentCellString = "";
                break;
            case KeyEvent.VK_BACK_SPACE:
                currentCellString = "";
                break;
            case KeyEvent.VK_INSERT:
                break;
            case KeyEvent.VK_NUM_LOCK:
                break;
            case KeyEvent.VK_CAPS_LOCK:
                break;
            case KeyEvent.CHAR_UNDEFINED:
                break;
            default:
                if (lastKeyChar != KeyEvent.CHAR_UNDEFINED) {
                    currentCellString = currentCellString + lastKeyChar;
                }
                break;
        }
        return currentCellString;
    }

    private void startEditorMode(boolean ctrlDown, int lastKeyInt, char lastKeyChar) {
        System.out.println("startEditorMode: " + selectedField);
        removeAllFocusListners();
        if (cellValue instanceof ImdiField[]) {
            if (!ctrlDown && selectedField != -1 && !((ImdiField) cellValue[selectedField]).isLongField()) {
                if (((ImdiField) cellValue[selectedField]).hasVocabulary()) {
                    System.out.println("Has Vocabulary");
                    JComboBox comboBox = new JComboBox();
                    ImdiVocabularies.Vocabulary fieldsVocabulary = ((ImdiField) cellValue[selectedField]).getVocabulary();
                    if (null == fieldsVocabulary || null == fieldsVocabulary.findVocabularyItem(((ImdiField) cellValue[selectedField]).fieldValue)) {
                        comboBox.addItem(((ImdiField) cellValue[selectedField]).fieldValue);
                    }
                    if (null != fieldsVocabulary) {
                        for (Enumeration<ImdiVocabularies.VocabularyItem> vocabularyList = fieldsVocabulary.vocabularyItems.elements(); vocabularyList.hasMoreElements();) {
                            comboBox.addItem(vocabularyList.nextElement().languageName);
                        }
                    }
                    // TODO: enable multiple selection for vocabulary lists
                    comboBox.setSelectedItem(cellValue[selectedField].toString());
                    editorPanel.remove(button);
                    editorPanel.add(comboBox);
                    editorPanel.doLayout();
                    comboBox.setPopupVisible(true);
                    comboBox.setEditable(((ImdiField) cellValue[selectedField]).vocabularyIsOpen);
                    if (((ImdiField) cellValue[selectedField]).vocabularyIsOpen) {
                        comboBox.getEditor().getEditorComponent().requestFocusInWindow();
                    } else {
                        comboBox.requestFocusInWindow();
                    }
                    editorComponent = comboBox;
                    addFocusListener(comboBox);
                    addFocusListener(comboBox.getEditor().getEditorComponent());
                } else {
                    editorPanel.remove(button);
                    editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
                    String currentCellString = cellValue[selectedField].toString();
                    JTextField editorTextField = new JTextField(getEditorText(lastKeyInt, lastKeyChar, currentCellString));
                    editorPanel.setBorder(null);
                    editorTextField.setBorder(null);
                    editorTextField.setMinimumSize(new Dimension(50, (int) editorTextField.getMinimumSize().getHeight()));
                    editorPanel.add(editorTextField);

                    editorTextField.addKeyListener(new java.awt.event.KeyListener() {

                        public void keyTyped(KeyEvent evt) {
                            if (isStartLongFieldKey(evt)) {
                                // if this is a long start long field event the we don't want that key appended so it is not passed on here
                                startEditorMode(true, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
                            }
                        }

                        public void keyPressed(KeyEvent evt) {
                        }

                        public void keyReleased(KeyEvent evt) {
                        }
                    });
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
                fireEditingStopped();
                int titleCount = 1;
                JTextArea focusedTabTextArea = null;
                JTabbedPane tabPane = new JTabbedPane();

                fieldEditors = new JTextArea[cellValue.length];
                keyEditorFields = new JTextField[cellValue.length];
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
                    FocusListener editorFocusListener = new FocusListener() {

                        public void focusGained(FocusEvent e) {
                        }

                        public void focusLost(FocusEvent e) {
                            ImdiField cellField = (ImdiField) cellValue[cellFieldIndex];
                            if (cellField.parentImdi.getParentDomNode().isLocal()) {
                                cellField.setFieldValue(fieldEditors[cellFieldIndex].getText(), true, false);
                                if (keyEditorFields[cellFieldIndex] != null) {
                                    cellField.setKeyName(keyEditorFields[cellFieldIndex].getText(), true);
                                }
                            }
                        }
                    };
                    fieldEditors[cellFieldIndex].addFocusListener(editorFocusListener);
                    // insert the last key for only the selected field
                    if (selectedField == cellFieldIndex || (selectedField == -1 && cellFieldIndex == 0)) {
                        fieldEditors[cellFieldIndex].setText(getEditorText(lastKeyInt, lastKeyChar, ((ImdiField) cellValue[cellFieldIndex]).getFieldValue()));
                    } else {
                        fieldEditors[cellFieldIndex].setText(((ImdiField) cellValue[cellFieldIndex]).getFieldValue());
                    }
                    fieldEditors[cellFieldIndex].setLineWrap(true);
                    fieldEditors[cellFieldIndex].setWrapStyleWord(true);

                    JPanel tabPanel = new JPanel();
                    tabPanel.setLayout(new BorderLayout());
                    fieldLanguageBoxs[cellFieldIndex] = getLanguageIdBox(cellFieldIndex);
                    if (fieldLanguageBoxs[cellFieldIndex] != null) {
                        tabPanel.add(fieldLanguageBoxs[cellFieldIndex], BorderLayout.PAGE_START);
                    }
                    String keyName = ((ImdiField) cellValue[cellFieldIndex]).getKeyName();
                    if (keyName != null) { // if this is a key type field then show the editing options
                        keyEditorFields[cellFieldIndex] = new JTextField(((ImdiField[]) cellValue)[cellFieldCounter].getKeyName());
                        keyEditorFields[cellFieldIndex].addFocusListener(editorFocusListener);
                        keyEditorFields[cellFieldIndex].setEditable(((ImdiField) cellValue[cellFieldIndex]).parentImdi.getParentDomNode().isLocal());
                        tabPanel.add(keyEditorFields[cellFieldIndex], BorderLayout.PAGE_START);
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
                } else {
                    tabPane.setSelectedIndex(0);
                }
                focusedTabTextArea.requestFocusInWindow();
                fireEditingStopped();
            }
        } else if (cellValue instanceof ImdiTreeObject[]) {
            LinorgWindowManager.getSingleInstance().openFloatingTableOnce((ImdiTreeObject[]) cellValue, columnName + " in " + rowImdi);
        } else {
            try {
                throw new Exception("Edit cell type not supported");
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
    }

    public Object getCellEditorValue() {
//        System.out.println("getCellEditorValue");
        if (selectedField != -1) {
            if (editorComponent != null) {
                if (editorComponent instanceof JComboBox) {
                    ((ImdiField[]) cellValue)[selectedField].setFieldValue(((JComboBox) editorComponent).getSelectedItem().toString(), true, false);
                } else if (editorComponent instanceof JTextField) {
                    ((ImdiField[]) cellValue)[selectedField].setFieldValue(((JTextField) editorComponent).getText(), true, false);
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
//        receivedKeyDown = true;
        parentTable = (ImdiTable) table;
        parentCellRect = parentTable.getCellRect(row, column, false);
        ImdiTableCellRenderer cellRenderer = new ImdiTableCellRenderer(value);
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
        button.setText(cellRenderer.getText());
        button.setForeground(cellRenderer.getForeground());
        button.setIcon(cellRenderer.getIcon());
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
