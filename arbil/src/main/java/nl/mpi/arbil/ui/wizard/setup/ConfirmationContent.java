package nl.mpi.arbil.ui.wizard.setup;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ConfirmationContent extends TextInstructionWizardContent {

    public ConfirmationContent() {
	super("/nl/mpi/arbil/resources/html/wizard/Confirmation.html");
    }

    public Object getNext() {
	return null; // After this, finish wizard
    }

    public Object getPrevious() {
	return ArbilSetupWizard.REMOTE_LOCATIONS;
    }
}
