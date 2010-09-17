package nl.mpi.arbil.FieldEditors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.ImdiVocabularies;

/**
 * Document   : ControlledVocabularyComboBox
 * Created on : Wed Oct 07 11:07:30 CET 2009
 * @author Peter.Withers@mpi.nl
 */
public class ControlledVocabularyComboBox extends JComboBox implements KeyListener, ActionListener {

//    ImdiField targetField;
    String currentValue;

    public ControlledVocabularyComboBox(ImdiField targetField) {
//        targetField = targetFieldLocal;
        currentValue = targetField.getFieldValue();
        ImdiVocabularies.Vocabulary fieldsVocabulary = targetField.getVocabulary();
        if (null == fieldsVocabulary || null == fieldsVocabulary.findVocabularyItem(targetField.getFieldValue())) {
            this.addItem(targetField.getFieldValue());
        }
        if (null != fieldsVocabulary) {
            for (ImdiVocabularies.VocabularyItem vocabularyListItem : fieldsVocabulary.getVocabularyItems()) {
                this.addItem(vocabularyListItem.languageName);
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
        this.addActionListener(this);
        this.getEditor().getEditorComponent().addKeyListener(this);
    }

    public String getCurrentValue() {
        return currentValue;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // handle item selections
        if (actionEvent.getSource() instanceof JComboBox) {
            JComboBox cb = (JComboBox) actionEvent.getSource();
            currentValue = (String) cb.getSelectedItem();
        }
        System.out.println("currentValue: " + currentValue);
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        // handle key typed
        this.getCurrentValue();
        currentValue = ((JTextComponent) this.getEditor().getEditorComponent()).getText();
        System.out.println("keyReleased: " + currentValue);
        // TODO: continue with the closed and list vocabularies
//        if (!targetField.vocabularyIsOpen) {
//            for (int itemCounter = 0; itemCounter< this.getItemCount(); itemCounter++){
//                if (this.)
//            }
//        }
    }

    public void keyTyped(KeyEvent e) {
    }
}
