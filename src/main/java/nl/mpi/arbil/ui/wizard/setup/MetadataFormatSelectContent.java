package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
	
	JPanel checkBoxPanel = new JPanel();
	checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.PAGE_AXIS));
	checkBoxPanel.setOpaque(true);
	
	imdiCheckBox = new JCheckBox("IMDI");
	imdiCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
	checkBoxPanel.add(imdiCheckBox);
	cmdiCheckBox = new JCheckBox("CMDI");
	cmdiCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
	checkBoxPanel.add(cmdiCheckBox);
	add(checkBoxPanel, BorderLayout.WEST);
	
	refresh();
    }

    @Override
    public final void refresh() {
	super.refresh();
	imdiCheckBox.setSelected(model.isImdiSelected());
	cmdiCheckBox.setSelected(model.isCmdiSelected());
    }

    @Override
    public boolean beforeNext() {
	updateModel();
	if (model.isImdiSelected() || model.isCmdiSelected()) {
	    return true;
	} else {
	    JOptionPane.showMessageDialog(this, "Select at least one of the options before continuing", "Select a metadata format", JOptionPane.WARNING_MESSAGE);
	    return false;
	}
    }

    @Override
    public boolean beforePrevious() {
	updateModel();
	return true;
    }

    public Object getNext() {
	if (model.isCmdiSelected()) {
	    return CmdiProfileSelectContent.class;
	} else {
	    return ConfirmationContent.class;
	}
    }

    public Object getPrevious() {
	return ArbilSetupWizard.INTRODUCTION;
    }

    private void updateModel() {
	model.setImdiSelected(imdiCheckBox.isSelected());
	model.setCmdiSelected(cmdiCheckBox.isSelected());
    }
}
