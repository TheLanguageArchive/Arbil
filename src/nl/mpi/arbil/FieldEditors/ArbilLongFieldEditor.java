package nl.mpi.arbil.FieldEditors;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.ImdiTable;
import nl.mpi.arbil.ImdiVocabularies;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : ArbilLongFieldEditor
 *  Created on : Sep 14, 2010, 1:53:15 PM
 *  Author     : Peter Withers
 */
public class ArbilLongFieldEditor {

    ImdiTable parentTable = null;
    int selectedField = -1;
    Object[] cellValue;
    JTextField keyEditorFields[] = null;
    JTextArea fieldEditors[] = null;
    JComboBox fieldLanguageBoxs[] = null;
    JLabel fieldDescription = null;
    JInternalFrame editorFrame = null;

    public ArbilLongFieldEditor(ImdiTable parentTableLocal) {
        parentTable = parentTableLocal;
    }

    public void showEditor(Object[] cellValueLocal, String currentEditorText) {
        cellValue = cellValueLocal;
        int titleCount = 1;
        JTextArea focusedTabTextArea = null;
        JTabbedPane tabPane = new JTabbedPane();

        fieldEditors = new JTextArea[cellValue.length];
        keyEditorFields = new JTextField[cellValue.length];
        fieldLanguageBoxs = new JComboBox[cellValue.length];
        fieldDescription = new JLabel();


        String parentNodeName = "unknown";
        String fieldName = "unknown";
        if (cellValue[0] instanceof ImdiField) {
            ((ImdiField) cellValue[0]).parentImdi.getParentDomNode().registerContainer(this);
            parentNodeName = ((ImdiField) cellValue[0]).parentImdi.toString();
            fieldName = ((ImdiField[]) cellValue)[0].getTranslateFieldName();
        }

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
                fieldEditors[cellFieldIndex].setText(currentEditorText);
            } else {
                fieldEditors[cellFieldIndex].setText(((ImdiField) cellValue[cellFieldIndex]).getFieldValue());
            }
            fieldEditors[cellFieldIndex].setLineWrap(true);
            fieldEditors[cellFieldIndex].setWrapStyleWord(true);

            JPanel tabPanel = new JPanel();
            JPanel tabTitlePanel = new JPanel();
            tabPanel.setLayout(new BorderLayout());
            tabTitlePanel.setLayout(new BoxLayout(tabTitlePanel, BoxLayout.PAGE_AXIS));
            fieldLanguageBoxs[cellFieldIndex] = null;
            if (cellValue[cellFieldIndex] instanceof ImdiField) {
                if (((ImdiField) cellValue[cellFieldIndex]).getLanguageId() != null) {
                    fieldLanguageBoxs[cellFieldIndex] = new LanguageIdBox((ImdiField) cellValue[cellFieldIndex], null);
                    JPanel languagePanel = new JPanel(new BorderLayout());
                    languagePanel.add(new JLabel("Language:"), BorderLayout.LINE_START);
                    languagePanel.add(fieldLanguageBoxs[cellFieldIndex], BorderLayout.CENTER);
                    tabTitlePanel.add(languagePanel);
                }
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
        JPanel outerPanel = new JPanel(new BorderLayout());
        fieldDescription.setText(((ImdiField) cellValue[0]).parentImdi.getNodeTemplate().getHelpStringForField(((ImdiField) cellValue[0]).getFullXmlPath()));
        outerPanel.add(fieldDescription, BorderLayout.PAGE_START);
        outerPanel.add(tabPane, BorderLayout.CENTER);
        // todo: add next and previous buttons for the current file
        // todo: add all unused attributes as editable text
        editorFrame = LinorgWindowManager.getSingleInstance().createWindow(fieldName + " in " + parentNodeName, outerPanel);
        editorFrame.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                // deregister component from imditreenode
                if (cellValue[0] instanceof ImdiField) {
                    ((ImdiField) cellValue[0]).parentImdi.getParentDomNode().removeContainer(this);
                }
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
    }

    public void updateEditor(ImdiTreeObject parentImdiObject) {
        // this will only be called when the long field editor is shown
        // when an imdi node is edited or saved or reloaded this will be called to update the displayed values
        if (cellValue instanceof ImdiField[]) {
            fieldDescription.setText(((ImdiField) cellValue[0]).parentImdi.getNodeTemplate().getHelpStringForField(((ImdiField) cellValue[0]).getFullXmlPath()));
            String fieldName = ((ImdiField[]) cellValue)[0].getTranslateFieldName();
            cellValue = parentImdiObject.getFields().get(fieldName);
            for (int cellFieldCounter = 0; cellFieldCounter < cellValue.length; cellFieldCounter++) {
                fieldEditors[cellFieldCounter].setText(((ImdiField[]) cellValue)[cellFieldCounter].getFieldValue());
                if (fieldLanguageBoxs[cellFieldCounter] != null) {
                    // set the language id selection in the dropdown
                    boolean selectedValueFound = false;
                    for (int itemCounter = 0; itemCounter < fieldLanguageBoxs[cellFieldCounter].getItemCount(); itemCounter++) {
                        Object currentObject = fieldLanguageBoxs[cellFieldCounter].getItemAt(itemCounter);
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
                        fieldLanguageBoxs[cellFieldCounter].addItem(LanguageIdBox.defaultLanguageDropDownValue);
                        fieldLanguageBoxs[cellFieldCounter].setSelectedItem(LanguageIdBox.defaultLanguageDropDownValue);
                    }
//                    System.out.println("field language: " + ((ImdiField[]) cellValue)[cellFieldCounter].getLanguageId());
                }
                if (keyEditorFields[cellFieldCounter] != null) {
                    keyEditorFields[cellFieldCounter].setText(((ImdiField[]) cellValue)[cellFieldCounter].getKeyName());
                }
            }
        }
    }

    public void closeWindow() {
        editorFrame.setVisible(false);
    }
}
