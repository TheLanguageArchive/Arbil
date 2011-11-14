/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.wizard.setup;

import java.awt.Color;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.ui.wizard.ArbilWizardContent;

/**
 * Abstract Wizard content that has vertical box layout with JTextPane on top
 * Contents of JTextPane come from resource with specified location
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class TextInstructionWizardContent extends JPanel implements ArbilWizardContent {

    /**
     * Location of text (optionally HTML) resource to show as introduction
     * @param resourceLocation 
     */
    public TextInstructionWizardContent(String resourceLocation) {
	super();
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	setBackground(Color.WHITE);
	
	JTextPane textPane = new JTextPane();
	textPane.setBackground(Color.WHITE);
	textPane.setMargin(new Insets(5, 10, 5, 10));
	try {
	    textPane.setPage(JTextPane.class.getResource(resourceLocation));
	} catch (IOException ex) {
	    textPane.setText("Error while getting wizard text. Please check the error log.");
	    GuiHelper.linorgBugCatcher.logError("I/O exception while getting wizard text", ex);
	}
	add(textPane);
    }
    
    public JComponent getContent() {
	return this;
    }
    
    public void onNext() {
    }
    
    public void onPrevious() {
    }
}
