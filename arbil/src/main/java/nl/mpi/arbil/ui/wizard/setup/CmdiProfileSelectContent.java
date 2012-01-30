package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.CmdiProfilesPanel;

/**
 * ArbilWizard content that lets the user select CMDI profiles
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiProfileSelectContent extends TextInstructionWizardContent {

    private final ArbilSetupWizardModel model;
    private CmdiProfilesPanel profilesPanel;

    public CmdiProfileSelectContent(ArbilSetupWizardModel model, JDialog wizardDialog) {
	super("/nl/mpi/arbil/resources/html/wizard/CmdiProfileSelect.html");
	this.model = model;

	profilesPanel = new CmdiProfilesPanel(wizardDialog);
	profilesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Clarin Profiles"));
	profilesPanel.setPreferredSize(new Dimension(200, 300));
	profilesPanel.setVisible(false); // do not show until content panel is visible

	add(profilesPanel, BorderLayout.CENTER);

    }

    @Override
    public void beforeShow() {
	profilesPanel.setVisible(true);
	profilesPanel.populateList();
	profilesPanel.loadProfileDescriptions(false);
    }

    @Override
    public boolean beforeNext() {
	boolean doNext;
	if (profilesSelected()) {
	    doNext = true;
	} else {
	    doNext = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
		    "You have not yet selected profiles to use. Are you sure you want to continue?",
		    "No profiles selected",
		    JOptionPane.OK_CANCEL_OPTION,
		    JOptionPane.WARNING_MESSAGE);
	}
	if (doNext) {
	    profilesPanel.setVisible(false);
	}
	return doNext;
    }

    @Override
    public boolean beforePrevious() {
	profilesPanel.setVisible(false);
	return true;
    }

    public Object getNext() {
	return ArbilSetupWizard.REMOTE_LOCATIONS;
    }

    public Object getPrevious() {
	return ArbilSetupWizard.METADATA_FORMAT_SELECT;
    }

    /**
     * 
     * @return Whether at least one CMDI profile has been selected
     */
    private boolean profilesSelected() {
	for (String template : ArbilTemplateManager.getSingleInstance().getSelectedTemplateArrayList()) {
	    if (template.startsWith(ArbilTemplateManager.CLARIN_PREFIX)) {
		return true;
	    }
	}
	return false;
    }
}
