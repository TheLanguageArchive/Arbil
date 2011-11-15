package nl.mpi.arbil.ui.wizard.setup;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import javax.swing.JOptionPane;
import nl.mpi.arbil.ui.wizard.ArbilWizard;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSetupWizard extends ArbilWizard {

    public final static Object INTRODUCTION = IntroductionContent.class;
    public final static Object METADATA_FORMAT_SELECT = MetadataFormatSelectContent.class;
    public final static Object CMDI_PROFILE_SELECT = CmdiProfileSelectContent.class;
    public final static Object CONFIRMATION = ConfirmationContent.class;
    private ArbilSetupWizardModel model;

    public ArbilSetupWizard() {
	this(null);
    }

    public ArbilSetupWizard(Frame owner) {
	super(owner);
	getWizardDialog().setBackground(Color.WHITE);
	getWizardDialog().getContentPane().setBackground(Color.WHITE);

	model = new ArbilSetupWizardModel();
	addContent(INTRODUCTION, new IntroductionContent());
	addContent(METADATA_FORMAT_SELECT, new MetadataFormatSelectContent(model));
	addContent(CMDI_PROFILE_SELECT, new CmdiProfileSelectContent(model));
	addContent(CONFIRMATION, new ConfirmationContent(model));
	setCurrent(INTRODUCTION);
    }

    @Override
    protected boolean onFinish() {
	//TODO: finish logic
	return true;
    }

    @Override
    protected boolean onCancel() {
	return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
		getWizardDialog(),
		"Do you really want to cancel the wizard?",
		"Cancel wizard",
		JOptionPane.YES_NO_OPTION,
		JOptionPane.QUESTION_MESSAGE);
    }

    public static void main(String args[]) {
	ArbilWizard wizard = new ArbilSetupWizard();
	wizard.showDialog(ModalityType.APPLICATION_MODAL);
    }
}
