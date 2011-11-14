/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.wizard;

import java.awt.Insets;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import nl.mpi.arbil.ui.GuiHelper;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class IntroductionContent extends JTextPane implements ArbilWizardContent {

    public IntroductionContent() {
	setMargin(new Insets(5, 10, 5, 10));
	
	try {
	    setPage(JTextPane.class.getResource("/nl/mpi/arbil/resources/html/wizard/introduction.html"));
	} catch (IOException ex) {
	    setText("Error while getting wizard text. Please check the error log.");
	    GuiHelper.linorgBugCatcher.logError("I/O exception while getting wizard text", ex);
	}
    }

    public JComponent getContent() {
	return this;
    }

    public Object getNext() {
	return ArbilSetupWizard.METADATA_FORMAT_SELECT;
    }

    public Object getPrevious() {
	return null;
    }
}
