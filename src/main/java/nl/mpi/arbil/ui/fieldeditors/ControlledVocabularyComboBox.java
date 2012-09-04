package nl.mpi.arbil.ui.fieldeditors;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilVocabulary;
import nl.mpi.arbil.data.ArbilVocabularyItem;

/**
 * Editable combo box that has the items of a controlled vocabulary in it.
 * Use with ControlledVocabularyComboBoxEditor
 *
 * Document : ControlledVocabularyComboBox
 * Created on : Wed Oct 07 11:07:30 CET 2009
 *
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 * @see ControlledVocabularyComboBoxEditor
 */
public class ControlledVocabularyComboBox extends JComboBox {

    public ControlledVocabularyComboBox(ArbilField targetField) {
	ArbilVocabulary fieldsVocabulary = targetField.getVocabulary();
	if (null == fieldsVocabulary || null == fieldsVocabulary.findVocabularyItem(targetField.getFieldValue())) {
	    this.addItem(targetField.getFieldValue());
	}
	if (null != fieldsVocabulary) {
	    for (ArbilVocabularyItem vocabularyListItem : fieldsVocabulary.getVocabularyItems()) {
		this.addItem(vocabularyListItem);
	    }
	}

	this.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());

	this.setRenderer(new BasicComboBoxRenderer() {
	    @Override
	    public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
		// TODO: Create a more distinct separation of display value and code; this will probably require
		// a panel rather than a label which requires custom initialization (for the reuse).
		// Display value on the left, code on the right in a lighter shade would look nice.
		
		// Call super, which initializes the renderer
		super.getListCellRendererComponent(jlist, o, i, bln, bln1);
		if (o instanceof ArbilVocabularyItem) {
		    final ArbilVocabularyItem item = (ArbilVocabularyItem) o;
		    if (item.hasItemCode()) {
			// Show code and diplay value seperately
			setText(String.format("%1$s [%2$s]", item.getDisplayValue(), item.getValue()));
		    } else {
			// Only show display value (there is no code, so value and display are the same)
			setText(item.getDisplayValue());
		    }
		}
		// The renderer is the component (same object gets reused, see implementation of BasicComboBoxRenderer)
		return this;
	    }
	});
    }

    public String getCurrentValue() {
	if (getEditor() != null) {
	    if (getEditor() instanceof ControlledVocabularyComboBoxEditor) {
		return ((ControlledVocabularyComboBoxEditor) getEditor()).getCurrentValue();
	    } else {
		return getEditor().getItem().toString();
	    }
	} else {
	    return getSelectedItem().toString();
	}
    }
}
