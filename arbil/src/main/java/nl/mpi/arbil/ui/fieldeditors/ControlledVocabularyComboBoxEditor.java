/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.fieldeditors;

import javax.swing.JComboBox;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilVocabulary;
import nl.mpi.arbil.ui.TypeAheadComboBoxEditor;

/**
 * Text editor intended for use with ControlledVocabularyComboBox
 * It has typeahead and can deal with open and closed vocabularies, and both
 * single valued vocabularies and lists.
 *
 * @see ControlledVocabularyComboBox
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ControlledVocabularyComboBoxEditor extends TypeAheadComboBoxEditor {

    public ControlledVocabularyComboBoxEditor(String initialValue, String originalValue, ArbilField arbilField, JComboBox comboBox) {
	super(new ArbilFieldEditor(initialValue), initialValue, originalValue, comboBox);

	this.targetField = arbilField;
	this.vocabulary = arbilField.getVocabulary();

	init();
    }

    /**
     * Local convenience method. Gets item from vocabulary
     *
     * @param index
     * @return
     */
    protected String getItemAt(int index) {
	return vocabulary.getVocabularyItems().get(index).itemDisplayName;
    }

    protected int getItemsCount() {
	return vocabulary.getVocabularyItems().size();
    }

    protected boolean isList() {
	return targetField.isVocabularyList();
    }

    protected boolean isOpen() {
	return targetField.isVocabularyOpen();
    }
    private ArbilVocabulary vocabulary;
    private ArbilField targetField;
}
