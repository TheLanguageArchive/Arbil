package nl.mpi.arbil.ui.fieldeditors;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 *  Document   : ArbilFieldEditor
 *  Created on : Sep 14, 2010, 1:52:57 PM
 *  Author     : Peter Withers
 */
public class ArbilFieldEditor extends JTextField {

    public ArbilFieldEditor(String initialValue) {
        super(initialValue);
        this.setBorder(null);
        this.setMinimumSize(new Dimension(50, (int) this.getMinimumSize().getHeight()));

        this.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                // the caret position must be set here so that the mac version does not loose the last typed char when entering edit mode
                ArbilFieldEditor.this.setCaretPosition(ArbilFieldEditor.this.getText().length());
            }

            public void focusLost(FocusEvent e) {
            }
        });
    }
}
