package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import nl.mpi.arbil.ui.TemplateDialogue;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiProfileSelectContent extends TextInstructionWizardContent {

    private final ArbilSetupWizardModel model;
    private boolean dialogShown = false;

    public CmdiProfileSelectContent(ArbilSetupWizardModel model) {
	super("/nl/mpi/arbil/resources/html/wizard/CmdiProfileSelect.html");
	this.model = model;

	JPanel buttonPanel = new JPanel();
	buttonPanel.setBackground(Color.WHITE);
	JButton profileDialogButton = new JButton("Select profiles");
	profileDialogButton.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		dialogShown = true;
		TemplateDialogue.showTemplatesDialogue();
	    }
	});
	buttonPanel.add(profileDialogButton);
	add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    public boolean beforeNext() {
	if (dialogShown) {
	    return true;
	} else {
	    return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
		    "You have not yet selected profiles to use. Are you sure you want to continue?",
		    "No profiles selected",
		    JOptionPane.OK_CANCEL_OPTION,
		    JOptionPane.WARNING_MESSAGE);
	}
    }

    public Object getNext() {
	return ConfirmationContent.class;
    }

    public Object getPrevious() {
	return MetadataFormatSelectContent.class;
    }
}
