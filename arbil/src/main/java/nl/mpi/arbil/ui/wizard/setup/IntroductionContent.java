/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.wizard.setup;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class IntroductionContent extends TextInstructionWizardContent {

    public IntroductionContent() {
	super("/nl/mpi/arbil/resources/html/wizard/Introduction.html");
    }

    public Object getNext() {
	return ArbilSetupWizard.METADATA_FORMAT_SELECT;
    }

    public Object getPrevious() {
	return null;
    }
}
