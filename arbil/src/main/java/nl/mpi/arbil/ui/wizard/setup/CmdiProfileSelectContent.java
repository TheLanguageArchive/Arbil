/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.CmdiProfilesPanel;

/**
 * ArbilWizard content that lets the user select CMDI profiles
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiProfileSelectContent extends TextInstructionWizardContent {

    private CmdiProfilesPanel profilesPanel;

    public CmdiProfileSelectContent(ArbilSetupWizardModel model, JDialog wizardDialog) {
	super("/nl/mpi/arbil/resources/html/wizard/CmdiProfileSelect.html");

	profilesPanel = new CmdiProfilesPanel(wizardDialog);
	profilesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Clarin Profiles"));
	profilesPanel.setPreferredSize(new Dimension(200, 300));
	profilesPanel.setInstructionsVisible(false); // don't show instructions in addition to the one in the wizard
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
	for (String template : ArbilTemplateManager.getSingleInstance().getSelectedTemplates()) {
	    if (template.startsWith(ArbilTemplateManager.CLARIN_PREFIX)) {
		return true;
	    }
	}
	return false;
    }
}
