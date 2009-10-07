package nl.mpi.arbil;

import java.util.Enumeration;
import javax.swing.JComboBox;

/**
 * Document   : ControlledVocabularyComboBox
 * Created on : Wed Oct 07 11:07:30 CET 2009
 * @author Peter.Withers@mpi.nl
 */
public class ControlledVocabularyComboBox extends JComboBox {

    ImdiField targetField;

    public ControlledVocabularyComboBox(ImdiField targetFieldLocal) {
        targetField = targetFieldLocal;
        ImdiVocabularies.Vocabulary fieldsVocabulary = targetField.getVocabulary();
        if (null == fieldsVocabulary || null == fieldsVocabulary.findVocabularyItem(targetField.fieldValue)) {
            this.addItem(targetField.fieldValue);
        }
        if (null != fieldsVocabulary) {
            for (Enumeration<ImdiVocabularies.VocabularyItem> vocabularyList = fieldsVocabulary.vocabularyItems.elements(); vocabularyList.hasMoreElements();) {
                this.addItem(vocabularyList.nextElement().languageName);
            }
        }
        // TODO: enable multiple selection for vocabulary lists
        this.setSelectedItem(targetField.toString());
        this.setEditable(true);//targetField.vocabularyIsOpen);
//        if (targetField.vocabularyIsOpen) {
//            this.getEditor().getEditorComponent().requestFocusInWindow();
//        } else {
//            this.requestFocusInWindow();
//        }
//        this.setBorder(null);
        this.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
    }

    public String getCurrentValue() {
        // todo: ...
        return this.getSelectedItem().toString();
    }
}
