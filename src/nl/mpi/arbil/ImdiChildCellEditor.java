package nl.mpi.arbil;

import nl.mpi.arbil.FieldEditors.ControlledVocabularyComboBox;
import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.FieldEditors.ArbilFieldEditor;
import nl.mpi.arbil.FieldEditors.ArbilLongFieldEditor;
import nl.mpi.arbil.FieldEditors.LanguageIdBox;

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
    int selectedField = -1;
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
                if (!cellHasControlledVocabulary() && isStartLongFieldKey(evt)) {// prevent ctrl key events getting through etc.
                    startEditorMode(true, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
                } else if (!evt.isActionKey()
                        && !evt.isMetaDown() && // these key down checks will not catch a key up event hence the key codes below which work for up and down events
                        !evt.isAltDown()
                        && !evt.isAltGraphDown()
                        && !evt.isControlDown()
                        && evt.getKeyCode() != 16
                        && /* 16 is a shift key up or down event */ evt.getKeyCode() != 17
                        && /* 17 is the ctrl key*/ evt.getKeyCode() != 18
                        && /* 18 is the alt key*/ evt.getKeyCode() != 157
                        &&/* 157 is the meta key*/ evt.getKeyCode() != 27 /* 27 is the esc key*/) {
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
                        requiredWidth += LanguageIdBox.languageSelectWidth;
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
            returnValue = !parentObject.isLoading() && parentObject.isEditable() && parentObject.isMetaDataNode(); // todo: consider limiting editing to files withing the cache only
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
//        System.out.println("startEditorMode: " + selectedField + " lastKeyInt: " + lastKeyInt + " lastKeyChar: " + lastKeyChar);
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
                    ArbilFieldEditor editorTextField = new ArbilFieldEditor(getEditorText(lastKeyInt, lastKeyChar, currentCellString));
                    editorTextField.addKeyListener(new java.awt.event.KeyListener() {

                        public void keyTyped(KeyEvent evt) {
                            if (isStartLongFieldKey(evt)) {
                                // if this is a long start long field event the we don't want that key appended so it is not passed on here
                                new ArbilLongFieldEditor(parentTable).showEditor(cellValue, getEditorText(KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED, ((ImdiField) cellValue[selectedField]).getFieldValue()));
                            }
                        }

                        public void keyPressed(KeyEvent evt) {
                        }

                        public void keyReleased(KeyEvent evt) {
                        }
                    });
                    editorPanel.setBorder(null);
                    editorPanel.add(editorTextField);

                    addFocusListener(editorTextField);
                    if (cellValue[selectedField] instanceof ImdiField) {
                        if (((ImdiField) cellValue[selectedField]).getLanguageId() != null) {
                            // this is an ImdiField that has a fieldLanguageId
                            JComboBox fieldLanguageBox = new LanguageIdBox((ImdiField) cellValue[selectedField], parentCellRect);
                            editorPanel.add(fieldLanguageBox);
                            addFocusListener(fieldLanguageBox);
                        }
                    }
                    editorPanel.doLayout();
                    editorTextField.requestFocusInWindow();
                    editorComponent = editorTextField;
                }
            } else {
                fireEditingStopped();
                new ArbilLongFieldEditor(parentTable).showEditor(cellValue, getEditorText(lastKeyInt, lastKeyChar, ((ImdiField) cellValue[selectedField]).getFieldValue()));
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
                } else if (editorComponent instanceof ArbilFieldEditor) {
                    ((ImdiField[]) cellValue)[selectedField].setFieldValue(((ArbilFieldEditor) editorComponent).getText(), true, false);
                }
            }
            return cellValue[selectedField];
        } else {
            return cellValue;
        }
    }

    private void convertCellValue(Object value) {
        if (value != null) {
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
                if (cellValue[0] instanceof ImdiField) {
                    fieldName = ((ImdiField[]) cellValue)[0].getTranslateFieldName();
                }
            }
            if (cellValue[0] instanceof ImdiField) {
                registeredOwner = ((ImdiField) cellValue[0]).parentImdi;
            }
        } else {
            GuiHelper.linorgBugCatcher.logError(new Exception("value is null in convertCellValue"));
        }
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
