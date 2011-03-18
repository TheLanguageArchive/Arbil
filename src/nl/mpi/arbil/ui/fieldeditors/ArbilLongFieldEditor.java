package nl.mpi.arbil.ui.fieldeditors;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilFieldComparator;
import nl.mpi.arbil.util.ArrayComparator;

/**
 *  Document   : ArbilLongFieldEditor
 *  Created on : Sep 14, 2010, 1:53:15 PM
 *  Author     : Peter Withers
 */
public class ArbilLongFieldEditor extends JPanel implements ArbilDataNodeContainer {

    ArbilTable parentTable = null;
    ArbilDataNode parentArbilDataNode;
    ArbilField[] arbilFields;
    String fieldName = "unknown";
    JTabbedPane tabPane;
    int selectedField = -1;
//    Object[] cellValue;
    JTextField keyEditorFields[] = null;
    JTextArea fieldEditors[] = null;
    JComboBox fieldLanguageBoxs[] = null;
    JTextArea fieldDescription = null;
    JInternalFrame editorFrame = null;
    private JPanel contentPanel;
    private List<ArbilField[]> parentFieldList;
    private JButton prevButton;
    private JButton nextButton;

    public ArbilLongFieldEditor() {
        this(null);
    }

    public ArbilLongFieldEditor(ArbilTable parentTableLocal) {
        parentTable = parentTableLocal;

        setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new lfeInputMap(getInputMap()));
        setActionMap(new lfeActionMap(getActionMap()));

        // Window has two panels: content panel and panel with previous/next buttons
        contentPanel = new JPanel();
        setLayout(new BorderLayout());
        contentPanel.setLayout(new BorderLayout());

        this.add(contentPanel, BorderLayout.CENTER);

        this.add(createPreviousNextPanel(), BorderLayout.PAGE_END);
    }

    public void showEditor(ArbilField[] cellValueLocal, String currentEditorText, int selectedFieldLocal) {
        selectedField = selectedFieldLocal;
        arbilFields = cellValueLocal;
        parentArbilDataNode = arbilFields[0].parentDataNode;
        // todo: registerContainer should not be done on the parent node and the remove should scan all child nodes also, such that deleting a child like and actor would remove the correct nodes
        parentArbilDataNode.registerContainer(this);
        fieldName = arbilFields[0].getTranslateFieldName();

        setParentFieldList();

        tabPane = new JTabbedPane();
        final JComponent focusedTabTextArea = populateTabbedPane(currentEditorText);

        fieldDescription = new JTextArea();
        fieldDescription.setLineWrap(true);
        fieldDescription.setEditable(false);
        fieldDescription.setOpaque(false);
        fieldDescription.setText(parentArbilDataNode.getNodeTemplate().getHelpStringForField(arbilFields[0].getFullXmlPath()));

        contentPanel.removeAll();
        contentPanel.add(fieldDescription, BorderLayout.PAGE_START);
        contentPanel.add(tabPane, BorderLayout.CENTER);
        // todo: add next and previous buttons for the current file
        // todo: add all unused attributes as editable text
        editorFrame = ArbilWindowManager.getSingleInstance().createWindow(getWindowTitle(), this);
        editorFrame.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                // deregister component from imditreenode
                parentArbilDataNode.removeContainer(ArbilLongFieldEditor.this);
                super.internalFrameClosed(e);
                if (parentTable != null) {
                    parentTable.requestFocusInWindow();
                }
            }
        });
        if (selectedField != -1) {
            tabPane.setSelectedIndex(selectedField);
        } else {
            tabPane.setSelectedIndex(0);
        }
        setNavigationEnabled();
        requestFocusFor(focusedTabTextArea);
    }

    private JComponent populateTabbedPane(String currentEditorText) {
        int titleCount = 1;
        JTextArea focusedTabTextArea = null;

        fieldEditors = new JTextArea[arbilFields.length];
        keyEditorFields = new JTextField[arbilFields.length];
        fieldLanguageBoxs = new JComboBox[arbilFields.length];

        FocusListener editorFocusListener = new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                storeChanges();
            }
        };

        for (int cellFieldCounter = 0; cellFieldCounter < arbilFields.length; cellFieldCounter++) {
            final int cellFieldIndex = cellFieldCounter;
//                    ImdiField cellField = ((ImdiField) cellValue[cellFieldIndex]);
//                    final ImdiField sourceField = cellValueItem;
            fieldEditors[cellFieldIndex] = new JTextArea();
            if (focusedTabTextArea == null || selectedField == cellFieldCounter) {
                // set the selected field as the first one or in the case of a single node being selected tab to its pane
                focusedTabTextArea = fieldEditors[cellFieldIndex];
            }
            fieldEditors[cellFieldIndex].setEditable(parentArbilDataNode.getParentDomNode().isEditable());
            fieldEditors[cellFieldIndex].addFocusListener(editorFocusListener);
            // insert the last key for only the selected field
            if (currentEditorText != null && (selectedField == cellFieldIndex || (selectedField == -1 && cellFieldIndex == 0))) {
                fieldEditors[cellFieldIndex].setText(currentEditorText);
            } else {
                fieldEditors[cellFieldIndex].setText(arbilFields[cellFieldIndex].getFieldValue());
            }
            fieldEditors[cellFieldIndex].setLineWrap(true);
            fieldEditors[cellFieldIndex].setWrapStyleWord(true);
            fieldEditors[cellFieldIndex].setInputMap(JComponent.WHEN_FOCUSED, new lfeInputMap(fieldEditors[cellFieldIndex].getInputMap()));
            fieldEditors[cellFieldIndex].setActionMap(new lfeActionMap(fieldEditors[cellFieldIndex].getActionMap()));

            JPanel tabPanel = new JPanel();
            JPanel tabTitlePanel = new JPanel();
            tabPanel.setLayout(new BorderLayout());
            tabTitlePanel.setLayout(new BoxLayout(tabTitlePanel, BoxLayout.PAGE_AXIS));
            fieldLanguageBoxs[cellFieldIndex] = null;
            if (arbilFields[cellFieldIndex] instanceof ArbilField) {
                if (arbilFields[cellFieldIndex].getLanguageId() != null) {
                    fieldLanguageBoxs[cellFieldIndex] = new LanguageIdBox(arbilFields[cellFieldIndex], null);
                    JPanel languagePanel = new JPanel(new BorderLayout());
                    languagePanel.add(new JLabel("Language:"), BorderLayout.LINE_START);
                    if (parentArbilDataNode.getParentDomNode().isEditable()) {
                        languagePanel.add(fieldLanguageBoxs[cellFieldIndex], BorderLayout.CENTER);
                    } else {
                        languagePanel.add(new JLabel(fieldLanguageBoxs[cellFieldIndex].getSelectedItem().toString()), BorderLayout.CENTER);
                    }
                    tabTitlePanel.add(languagePanel);
                }
            }
            String keyName = arbilFields[cellFieldIndex].getKeyName();
            if (keyName != null) { // if this is a key type field then show the editing options
                JPanel keyNamePanel = new JPanel(new BorderLayout());
                keyEditorFields[cellFieldIndex] = new JTextField(arbilFields[cellFieldCounter].getKeyName());
                keyEditorFields[cellFieldIndex].addFocusListener(editorFocusListener);
                keyEditorFields[cellFieldIndex].setEditable(parentArbilDataNode.getParentDomNode().isEditable());
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

//        if (tabPane.getSelectedIndex() < 0 && tabPane.getTabCount() > 0) {
//            tabPane.setSelectedIndex(0);
//        }

        // If field has language attribute but no language has been chosen yet, request focus on the language select drop down
        if (arbilFields[selectedField].getLanguageId() != null && arbilFields[selectedField].getLanguageId().isEmpty()) {
            return fieldLanguageBoxs[selectedField];
        }

        return focusedTabTextArea;
    }

    private String getWindowTitle() {
        return fieldName + " in " + String.valueOf(parentArbilDataNode);
    }

    private JPanel createPreviousNextPanel() {
        prevButton = new JButton(previousAction);
        prevButton.setText("Previous");
        prevButton.setMnemonic('p');

        nextButton = new JButton(nextAction);
        nextButton.setText("Next");
        nextButton.setMnemonic('n');

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonsPanel.add(prevButton);
        buttonsPanel.add(nextButton);
        return buttonsPanel;
    }

    private void setParentFieldList() {
        parentFieldList = new ArrayList<ArbilField[]>(parentArbilDataNode.getFields().values());
        Collections.sort(parentFieldList, new ArrayComparator<ArbilField>(new ArbilFieldComparator(), 0));
    }

    private void setNavigationEnabled() {
        int index = parentFieldList.indexOf(arbilFields);
        nextButton.setEnabled(index < parentFieldList.size() - 1);
        prevButton.setEnabled(index > 0);
    }

    private void requestFocusFor(final JComponent component) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                component.requestFocusInWindow();
            }
        });
    }

    public void updateEditor() {
        arbilFields = parentArbilDataNode.getFields().get(fieldName);
        selectedField = Math.max(0, Math.min(tabPane.getTabCount(), tabPane.getSelectedIndex()));

        tabPane.removeAll();
        if (arbilFields != null && arbilFields.length > 0) {
            editorFrame.setTitle(getWindowTitle());
            JComponent focusedTabTextArea = populateTabbedPane(null);
            fieldDescription.setText(parentArbilDataNode.getNodeTemplate().getHelpStringForField(arbilFields[0].getFullXmlPath()));
            requestFocusFor(focusedTabTextArea);
            if (selectedField < tabPane.getTabCount()) {
                tabPane.setSelectedIndex(selectedField);
            }
        }

        // todo: this could be done more softly by using the below code when the fields have not been reloaded, but be carefull that the ImdiFields in the language boxes are not out of sync.
//        // todo: the number of fields might have changed so we really should update the tabs or re create the form
//        // this will only be called when the long field editor is shown
//        // when an imdi node is edited or saved or reloaded this will be called to update the displayed values
//        fieldDescription.setText(parentImdiTreeObject.getNodeTemplate().getHelpStringForField(imdiFields[0].getFullXmlPath()));
//        for (int cellFieldCounter = 0; cellFieldCounter < imdiFields.length; cellFieldCounter++) {
//            fieldEditors[cellFieldCounter].setText(imdiFields[cellFieldCounter].getFieldValue());
//            if (fieldLanguageBoxs[cellFieldCounter] != null) {
//                // set the language id selection in the dropdown
//                boolean selectedValueFound = false;
//                for (int itemCounter = 0; itemCounter < fieldLanguageBoxs[cellFieldCounter].getItemCount(); itemCounter++) {
//                    Object currentObject = fieldLanguageBoxs[cellFieldCounter].getItemAt(itemCounter);
//                    if (currentObject instanceof ImdiVocabularies.VocabularyItem) {
//                        ImdiVocabularies.VocabularyItem currentItem = (ImdiVocabularies.VocabularyItem) currentObject;
////                        System.out.println(((ImdiField[]) cellValue)[cellFieldCounter].getLanguageId());
////                            System.out.println(currentItem.languageCode);
//                        if (currentItem.languageCode.equals(imdiFields[cellFieldCounter].getLanguageId())) {
////                            System.out.println("setting as current");
////                            System.out.println(currentItem.languageCode);
////                            System.out.println(imdiFields[cellFieldCounter].getLanguageId());
//                            fieldLanguageBoxs[cellFieldCounter].setSelectedIndex(itemCounter);
//                            selectedValueFound = true;
//                        }
//                    }
//                }
//                if (selectedValueFound == false) {
//                    fieldLanguageBoxs[cellFieldCounter].addItem(LanguageIdBox.defaultLanguageDropDownValue);
//                    fieldLanguageBoxs[cellFieldCounter].setSelectedItem(LanguageIdBox.defaultLanguageDropDownValue);
//                }
////                    System.out.println("field language: " + ((ImdiField[]) cellValue)[cellFieldCounter].getLanguageId());
//            }
//            if (keyEditorFields[cellFieldCounter] != null) {
//                keyEditorFields[cellFieldCounter].setText(imdiFields[cellFieldCounter].getKeyName());
//            }
//        }
    }

    public void closeWindow() {
        editorFrame.doDefaultCloseAction();
    }

    public void storeChanges() {
        if (arbilFields != null) {
            for (int cellFieldCounter = 0; cellFieldCounter < arbilFields.length; cellFieldCounter++) {
                ArbilField cellField = (ArbilField) arbilFields[cellFieldCounter];
                if (cellField.parentDataNode.getParentDomNode().isEditable()) {
                    cellField.setFieldValue(fieldEditors[cellFieldCounter].getText(), true, false);
                    if (keyEditorFields[cellFieldCounter] != null) {
                        cellField.setKeyName(keyEditorFields[cellFieldCounter].getText(), true, false);
                    }
                }
            }
        }
    }

    /**
     * Data node is to be removed
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilDataNode dataNode) {
        closeWindow();
    }

    /**
     * Data node is clearing its icon
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilDataNode dataNode) {
        updateEditor();
        setParentFieldList();
        setNavigationEnabled();
    }

    private void changeTab(int d) {
        final int index;
        if (d > 0) {
            index = Math.min(tabPane.getSelectedIndex() + 1, tabPane.getTabCount() - 1);
        } else {
            index = Math.max(tabPane.getSelectedIndex() - 1, 0);
        }
        tabPane.setSelectedIndex(index);
        requestFocusFor(fieldEditors[index]);
    }

    private void moveAdjacent(int d) {
        int index = parentFieldList.indexOf(arbilFields);
        index += d;
        if (index < parentFieldList.size() && index >= 0) {
            storeChanges();
            fieldName = parentFieldList.get(index)[0].getTranslateFieldName();
            updateEditor();
            setNavigationEnabled();
        }
    }
    private Action nextAction = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            moveAdjacent(+1);
        }
    };
    private Action previousAction = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            moveAdjacent(-1);
        }
    };

    private class lfeActionMap extends ActionMap {

        public lfeActionMap(ActionMap parent) {
            super();
            if (parent != null) {
                setParent(parent);
            }

            put("nextField", nextAction);
            put("previousField", previousAction);

            // Action that switches to the next tab (if there is one)
            put("nextTab", new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    changeTab(+1);
                }
            });

            // Action that switches to the previous tab (if there is one)
            put("previousTab", new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    changeTab(-1);
                }
            });
        }
    }

    private class lfeInputMap extends InputMap {

        public lfeInputMap(InputMap parent) {
            super();
            if (parent != null) {
                setParent(parent);
            }

            // Next field keys
            put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.META_DOWN_MASK), "nextField");
            put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.META_DOWN_MASK), "nextField");
            put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), "nextField");
            put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK), "nextField");

            // Previous field keys
            put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.META_DOWN_MASK), "previousField");
            put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.META_DOWN_MASK), "previousField");
            put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), "previousField");
            put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK), "previousField");

            // Next tab keys
            put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.META_DOWN_MASK), "nextTab");
            put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), "nextTab");

            // Previous tab keys
            put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.META_DOWN_MASK), "previousTab");
            put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), "previousTab");
        }
    }
}
