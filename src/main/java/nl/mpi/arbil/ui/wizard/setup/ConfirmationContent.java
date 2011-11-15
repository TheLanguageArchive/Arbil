/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.wizard.setup;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ConfirmationContent extends TextInstructionWizardContent {

    private final ArbilSetupWizardModel model;

    public ConfirmationContent(ArbilSetupWizardModel model) {
	super("/nl/mpi/arbil/resources/html/wizard/Confirmation.html");
	this.model = model;
    }

    public Object getNext() {
	return null; // After this, finish wizard
    }

    public Object getPrevious() {
	if (model.isCmdiSelected()) {
	    return CmdiProfileSelectContent.class;
	} else {
	    return MetadataFormatSelectContent.class;
	}
    }
}
