package nl.mpi.arbil.ui.wizard.setup;

import javax.swing.JCheckBox;

/**
 * ArbilWizard content that lets the user select which MD formats to use: IMDI and/or CMDI
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MetadataFormatSelectContent extends TextInstructionWizardContent {

    private final ArbilSetupWizardModel model;
    private final JCheckBox imdiCheckBox;
    private final JCheckBox cmdiCheckBox;

    public MetadataFormatSelectContent(ArbilSetupWizardModel model) {
	super("/nl/mpi/arbil/resources/html/wizard/MetadataFormatSelect.html");
	this.model = model;

	imdiCheckBox = new JCheckBox("IDMI");
	add(imdiCheckBox);
	cmdiCheckBox = new JCheckBox("CDMI");
	add(cmdiCheckBox);

	refresh();
    }

    @Override
    public final void refresh() {
	super.refresh();
	imdiCheckBox.setSelected(model.imdi);
	cmdiCheckBox.setSelected(model.cmdi);
    }

    @Override
    public void onNext() {
	updateModel();
    }

    @Override
    public void onPrevious() {
	updateModel();
    }

    public Object getNext() {
	return null;
    }

    public Object getPrevious() {
	return ArbilSetupWizard.INTRODUCTION;
    }

    private void updateModel() {
	model.imdi = imdiCheckBox.isSelected();
	model.cmdi = cmdiCheckBox.isSelected();
    }
}
