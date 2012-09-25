package nl.mpi.arbil.ui.fieldeditors;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import nl.mpi.arbil.data.ArbilVocabularyItem;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ControlledVocabularyComboBoxRenderer extends BasicComboBoxRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	// TODO: Create a more distinct separation of display value and code; this will probably require
	// a panel rather than a label which requires custom initialization (for the reuse).
	// Display value on the left, code on the right in a lighter shade would look nice.
	// Call super, which initializes the renderer
	super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	if (value instanceof ArbilVocabularyItem) {
	    final ArbilVocabularyItem item = (ArbilVocabularyItem) value;
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

}
