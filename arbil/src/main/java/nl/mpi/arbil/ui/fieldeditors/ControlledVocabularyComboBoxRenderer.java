package nl.mpi.arbil.ui.fieldeditors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import nl.mpi.arbil.data.ArbilVocabularyItem;

/**
 * ListCellRenderer intended for the {@link ControlledVocabularyComboBox} that shows the {@link ArbilVocabularyItem}'s display value
 * like the default {@link BasicComboBoxEditor}, and, if available, shows the actual value at the line's end in a lighter shade.
 *
 * Implemented as a resolution for {@link https://trac.mpi.nl/ticket/2283}
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ControlledVocabularyComboBoxRenderer extends JPanel implements ListCellRenderer {

    public static final Color CODE_LABEL_FOREGROUND = Color.GRAY;
    public static final Color CODE_LABEL_SELECTED_FOREGROUND = Color.DARK_GRAY;
    private final JLabel displayValueLabel;
    private final JLabel codeLabel;

    public ControlledVocabularyComboBoxRenderer() {
	setLayout(new BorderLayout());
	setOpaque(true);
	setBorder(new EmptyBorder(1, 1, 1, 1));

	displayValueLabel = new JLabel("value");
	displayValueLabel.setOpaque(false);
	add(displayValueLabel, BorderLayout.CENTER);

	codeLabel = new JLabel("code");
	codeLabel.setOpaque(false);
	// Add empty border for left/right padding
	codeLabel.setBorder(new EmptyBorder(0, 2, 0, 2));
	// Align trailing and add to line end so that the code opposes the display value
	codeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
	add(codeLabel, BorderLayout.LINE_END);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	setPreferredSize(new Dimension(list.getWidth(), getPreferredSize().height));

	if (value instanceof ArbilVocabularyItem) {
	    final ArbilVocabularyItem item = (ArbilVocabularyItem) value;
	    displayValueLabel.setText(item.getDisplayValue());
	    if (item.hasItemCode()) {
		// Show code and diplay value seperately
		codeLabel.setText(item.getValue());
	    }
	    codeLabel.setVisible(item.hasItemCode());
	}

	if (isSelected) {
	    setBackground(list.getSelectionBackground());
	    displayValueLabel.setForeground(list.getSelectionForeground());
	    codeLabel.setForeground(CODE_LABEL_SELECTED_FOREGROUND);
	} else {
	    setBackground(list.getBackground());
	    displayValueLabel.setForeground(list.getForeground());
	    codeLabel.setForeground(CODE_LABEL_FOREGROUND);
	}

	displayValueLabel.setFont(list.getFont());
	codeLabel.setFont(list.getFont());

	// The renderer is the component (same object gets reused, as in BasicComboBoxRenderer)
	return this;
    }
}
