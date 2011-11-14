/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.wizard.setup;

import java.awt.Dialog.ModalityType;
import nl.mpi.arbil.ui.wizard.ArbilWizard;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSetupWizard extends ArbilWizard {

    public final static Object INTRODUCTION = IntroductionContent.class;
    public final static Object METADATA_FORMAT_SELECT = MetadataFormatSelectContent.class;
    private ArbilSetupWizardModel model;

    public ArbilSetupWizard() {
	super();
	model = new ArbilSetupWizardModel();
	addContent(INTRODUCTION, new IntroductionContent());
	addContent(METADATA_FORMAT_SELECT, new MetadataFormatSelectContent(model));
	setCurrent(INTRODUCTION);
    }

    @Override
    protected boolean onFinish() {
	//TODO: finish logic
	return true;
    }

    @Override
    protected boolean onCancel() {
	// TODO: cancel logic (if needed..)
	return true;
    }

    public static void main(String args[]) {
	ArbilWizard wizard = new ArbilSetupWizard();
	wizard.showDialog(ModalityType.APPLICATION_MODAL);
    }
}
