package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;

/**
 * Document   : ImdiChildCellEditor
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiChildCellEditor extends AbstractCellEditor implements TableCellEditor {

    ImdiTable parentTable = null;
    Rectangle parentCellRect = null;
    ImdiTreeObject registeredOwner = null;
    JPanel editorPanel;
    JLabel button;
    String fieldName;
    Component editorComponent = null;
//    boolean receivedKeyDown = false;
    Object[] cellValue;
    int languageSelectWidth = 100;
    int selectedField = -1;
    JTextArea fieldEditors[] = null;
    JTextField keyEditorFields[] = null;
    JComboBox fieldLanguageBoxs[] = null;
    Vector<Component> componentsWithFocusListners = new Vector();
    private String defaultLanguageDropDownValue = "<select>";

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
                if (!cellHasControlledVocabulary() && isStartLongFieldKey(evt)) {// prevent ctrl key events getting through etc.
                    startEditorMode(true, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
                } else if (!evt.isActionKey() &&
                        !evt.isMetaDown() && // these key down checks will not catch a key up event hence the key codes below which work for up and down events
                        !evt.isAltDown() &&
                        !evt.isAltGraphDown() &&
                        !evt.isControlDown() &&
                        evt.getKeyCode() != 16 && /* 16 is a shift key up or down event */
                        evt.getKeyCode() != 17 && /* 17 is the ctrl key*/
                        evt.getKeyCode() != 18 && /* 18 is the alt key*/
                        evt.getKeyCode() != 157 &&/* 157 is the meta key*/
                        evt.getKeyCode() != 27 /* 27 is the esc key*/) {
                    startEditorMode(false, evt.getKeyCode(), evt.getKeyChar());
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

    private boolean requiresLongFieldEditor() {
        boolean requiresLongFieldEditor = false;
        if (cellValue instanceof ImdiField[]) {
            FontMetrics fontMetrics = button.getGraphics().getFontMetrics();
            double availableWidth = parentCellRect.getWidth() + 20; // let a few chars over be ok for the short editor
            ImdiField[] iterableFields;
            if (selectedField == -1) { // when a single filed is edited only check that field otherwise check all fields
                iterableFields = (ImdiField[]) cellValue;
            } else {
                iterableFields = new ImdiField[]{((ImdiField[]) cellValue)[selectedField]};
            }
            for (ImdiField currentField : iterableFields) {
//                if (!currentField.hasVocabulary()) { // the vocabulary type field should not get here
                String fieldValue = currentField.getFieldValue();
                // calculate length and look for line breaks
                if (fieldValue.contains("\n")) {
                    requiresLongFieldEditor = true;
                    break;
                } else {
                    int requiredWidth = fontMetrics.stringWidth(fieldValue);
                    System.out.println("requiredWidth: " + requiredWidth + " availableWidth: " + availableWidth);
                    String fieldLanguageId = currentField.getLanguageId();
                    if (fieldLanguageId != null) {
                        requiredWidth += languageSelectWidth;
                    }
                    if (requiredWidth > availableWidth) {
                        requiresLongFieldEditor = true;
                        break;
                    }
                }
//                }
            }
        }
        return requiresLongFieldEditor;
    }

    private boolean isCellEditable() {
        boolean returnValue = false;
        if (cellValue instanceof ImdiField[]) {
            ImdiTreeObject parentObject = ((ImdiField[]) cellValue)[0].parentImdi;
            // check that the field id exists and that the file is in the local cache or in the favourites not loose on a drive, as the determinator of editability
            returnValue = parentObject.isLocal() && parentObject.isImdi() && ((ImdiField[]) cellValue)[0].fieldID != null;
        }
        return (returnValue);
    }

    private boolean isStartLongFieldKey(KeyEvent evt) {
        return (isStartLongFieldModifier(evt) && (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER || evt.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE));
    }

    private boolean isStartLongFieldModifier(InputEvent evt) {
        return ((evt.isAltDown() || evt.isControlDown()));
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
            cellValue = parentImdiObject.getFields().get(fieldName);
            for (int cellFieldCounter = 0; cellFieldCounter < cellValue.length; cellFieldCounter++) {
                fieldEditors[cellFieldCounter].setText(((ImdiField[]) cellValue)[cellFieldCounter].getFieldValue());
                if (fieldLanguageBoxs[cellFieldCounter] != null) {
                    // set the language id selection in the dropdown
                    boolean selectedValueFound = false;
                    for (int itemCounter = 0; itemCounter < fieldLanguageBoxs[cellFieldCounter].getItemCount(); itemCounter++) {
                        Object currentObject = (ImdiVocabularies.VocabularyItem) fieldLanguageBoxs[cellFieldCounter].getItemAt(itemCounter);
                        if (currentObject instanceof ImdiVocabularies.VocabularyItem) {
                            ImdiVocabularies.VocabularyItem currentItem = (ImdiVocabularies.VocabularyItem) currentObject;
//                        System.out.println(((ImdiField[]) cellValue)[cellFieldCounter].getLanguageId());
                            System.out.println(currentItem.languageCode);
                            if (currentItem.languageCode.equals(((ImdiField[]) cellValue)[cellFieldCounter].getLanguageId())) {
//                            System.out.println("setting as current");
                                fieldLanguageBoxs[cellFieldCounter].setSelectedIndex(itemCounter);
                                selectedValueFound = true;
                            }
                        }
                    }
                    if (selectedValueFound == false) {
                        fieldLanguageBoxs[cellFieldCounter].addItem(defaultLanguageDropDownValue);
                        fieldLanguageBoxs[cellFieldCounter].setSelectedItem(defaultLanguageDropDownValue);
                    }
//                    System.out.println("field language: " + ((ImdiField[]) cellValue)[cellFieldCounter].getLanguageId());
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
            ImdiVocabularies.VocabularyItem selectedItem = null;
            comboBox.setEditable(false);
            ImdiVocabularies.VocabularyItem[] languageItemArray = cellField.getLanguageList();
            Arrays.sort(languageItemArray);
            for (ImdiVocabularies.VocabularyItem currentItem : languageItemArray) {
                comboBox.addItem(currentItem);
                if (fieldLanguageId.equals(currentItem.languageCode)) {
                    selectedItem = currentItem;
                }
            }
            if (selectedItem != null) {
                System.out.println("selectedItem: " + selectedItem);
                comboBox.setSelectedItem(selectedItem);
            } else {
                comboBox.addItem(defaultLanguageDropDownValue);
                comboBox.setSelectedItem(defaultLanguageDropDownValue);
            }
            comboBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        ImdiField cellField = (ImdiField) cellValue[cellFieldIndex];
                        cellField.setLanguageId(((ImdiVocabularies.VocabularyItem) comboBox.getSelectedItem()).languageCode, true, false);
                        comboBox.removeItem(defaultLanguageDropDownValue);
                    } catch (Exception ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
                }
            });
            if (parentCellRect != null) {
                comboBox.setPreferredSize(new Dimension(languageSelectWidth, parentCellRect.height));
            }
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
            case KeyEvent.VK_ESCAPE:
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

    private boolean cellHasControlledVocabulary() {
        return ((ImdiField) cellValue[0]).hasVocabulary();
    }

    public void startLongfieldEditor(JTable table, Object value, boolean isSelected, int row, int column) {
        getTableCellEditorComponent(table, value, isSelected, row, column);
        startEditorMode(true, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
    }

    private void startEditorMode(boolean ctrlDown, int lastKeyInt, char lastKeyChar) {
        System.out.println("startEditorMode: " + selectedField + " lastKeyInt: " + lastKeyInt + " lastKeyChar: " + lastKeyChar);
        removeAllFocusListners();
        if (cellValue instanceof ImdiField[]) {
            if (cellHasControlledVocabulary()) {
                if (isCellEditable()) {
                    // if the cell has a vocabulary then prevent the long field editor
                    System.out.println("Has Vocabulary");
                    ControlledVocabularyComboBox cvComboBox = new ControlledVocabularyComboBox((ImdiField) cellValue[selectedField]);
                    editorPanel.remove(button);
                    editorPanel.add(cvComboBox);
                    editorPanel.doLayout();
                    cvComboBox.setPopupVisible(true);
                    cvComboBox.addPopupMenuListener(new PopupMenuListener() {

                        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        }

                        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                            fireEditingStopped();
                        }

                        public void popupMenuCanceled(PopupMenuEvent e) {
                            fireEditingStopped();
                        }
                    });
                    editorComponent = cvComboBox;
                    addFocusListener(cvComboBox);
                    addFocusListener(cvComboBox.getEditor().getEditorComponent());
                    cvComboBox.getEditor().getEditorComponent().requestFocusInWindow();
                }
            } else if (!ctrlDown && selectedField != -1 && (!requiresLongFieldEditor() || getEditorText(lastKeyInt, lastKeyChar, "anystring").length() == 0)) { // make sure the long field editor is not shown when the contents of the field are being deleted
                if (isCellEditable()) {
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
                    editorTextField.addFocusListener(new FocusListener() {

                        public void focusGained(FocusEvent e) {
                            // the caret position must be set here so that the mac version does not loose the last typed char when entering edit mode
                            ((JTextField) editorComponent).setCaretPosition(((JTextField) editorComponent).getText().length());
                        }

                        public void focusLost(FocusEvent e) {
                        }
                    });
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
                                    cellField.setKeyName(keyEditorFields[cellFieldIndex].getText(), true, false);
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
                    JPanel tabTitlePanel = new JPanel();
                    tabPanel.setLayout(new BorderLayout());
                    tabTitlePanel.setLayout(new BoxLayout(tabTitlePanel, BoxLayout.PAGE_AXIS));
                    fieldLanguageBoxs[cellFieldIndex] = getLanguageIdBox(cellFieldIndex);
                    if (fieldLanguageBoxs[cellFieldIndex] != null) {
                        JPanel languagePanel = new JPanel(new BorderLayout());
                        languagePanel.add(new JLabel("Language:"), BorderLayout.LINE_START);
                        languagePanel.add(fieldLanguageBoxs[cellFieldIndex], BorderLayout.CENTER);
                        tabTitlePanel.add(languagePanel);
                    }
                    String keyName = ((ImdiField) cellValue[cellFieldIndex]).getKeyName();
                    if (keyName != null) { // if this is a key type field then show the editing options
                        JPanel keyNamePanel = new JPanel(new BorderLayout());
                        keyEditorFields[cellFieldIndex] = new JTextField(((ImdiField[]) cellValue)[cellFieldCounter].getKeyName());
                        keyEditorFields[cellFieldIndex].addFocusListener(editorFocusListener);
                        keyEditorFields[cellFieldIndex].setEditable(((ImdiField) cellValue[cellFieldIndex]).parentImdi.getParentDomNode().isLocal());
                        keyNamePanel.add(new JLabel("Key Name:"), BorderLayout.LINE_START);
                        keyNamePanel.add(keyEditorFields[cellFieldIndex], BorderLayout.CENTER);
                        tabTitlePanel.add(keyNamePanel);
                    }
//                    tabTitlePanel.add(new JLabel("Field Value"));
//                    int layoutRowCount = tabTitlePanel.getComponentCount() / 2;
//                    tabTitlePanel.setLayout(new GridLayout(layoutRowCount, 2));
                    tabPanel.add(tabTitlePanel, BorderLayout.PAGE_START);
                    tabPanel.add(new JScrollPane(fieldEditors[cellFieldIndex]), BorderLayout.CENTER);
                    tabPane.add(fieldName + " " + titleCount++, tabPanel);
                }
                registeredOwner.registerContainer(this);
                JInternalFrame editorFrame = LinorgWindowManager.getSingleInstance().createWindow(fieldName + " in " + registeredOwner, tabPane);
                editorFrame.addInternalFrameListener(new InternalFrameAdapter() {

                    @Override
                    public void internalFrameClosed(InternalFrameEvent e) {
                        // deregister component from imditreenode
                        registeredOwner.removeContainer(this);
                        super.internalFrameClosed(e);
                        parentTable.requestFocusInWindow();
                    }
                });
                if (selectedField != -1) {
                    tabPane.setSelectedIndex(selectedField);
                } else {
                    tabPane.setSelectedIndex(0);
                }
                focusedTabTextArea.requestFocusInWindow();
                fireEditingStopped();
//                ImdiChildCellEditor.this.stopCellEditing();
            }
        } else if (cellValue instanceof ImdiTreeObject[]) {
            LinorgWindowManager.getSingleInstance().openFloatingTableOnce((ImdiTreeObject[]) cellValue, fieldName + " in " + registeredOwner);
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
                if (editorComponent instanceof ControlledVocabularyComboBox) {
                    ((ImdiField[]) cellValue)[selectedField].setFieldValue(((ControlledVocabularyComboBox) editorComponent).getCurrentValue(), true, false);
                } else if (editorComponent instanceof JTextField) {
                    ((ImdiField[]) cellValue)[selectedField].setFieldValue(((JTextField) editorComponent).getText(), true, false);
                }
            }
            return cellValue[selectedField];
        } else {
            return cellValue;
        }
    }

    private void convertCellValue(Object value) {
        if (value instanceof ImdiField) {
            // TODO: get the whole array from the parent and select the correct tab for editing
            fieldName = ((ImdiField) value).getTranslateFieldName();
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
            fieldName = ((ImdiField[]) cellValue)[0].getTranslateFieldName();
        }
        registeredOwner = ((ImdiField) cellValue[0]).parentImdi;
    }

    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
//         TODO: something in this area is preventing the table selection change listener firing ergo the tree selection does not get updated (this symptom can also be caused by a node not being loaded into the tree)
//        receivedKeyDown = true;
        parentTable = (ImdiTable) table;
        parentCellRect = parentTable.getCellRect(row, column, false);
        ImdiTableCellRenderer cellRenderer = new ImdiTableCellRenderer(value);
        convertCellValue(value);
//        columnName = table.getColumnName(column);
//        rowImdi = table.getValueAt(row, 0);
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
