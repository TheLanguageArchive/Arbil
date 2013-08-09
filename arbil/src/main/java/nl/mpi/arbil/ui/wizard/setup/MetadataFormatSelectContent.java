/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ResourceBundle;
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
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    private final ArbilSetupWizardModel model;
    private final JCheckBox imdiCheckBox;
    private final JCheckBox cmdiCheckBox;

    public MetadataFormatSelectContent(ArbilSetupWizardModel model) {
	super("/nl/mpi/arbil/resources/html/wizard/MetadataFormatSelect.html");
	this.model = model;
	
	JPanel checkBoxPanel = new JPanel();
	checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.PAGE_AXIS));
	checkBoxPanel.setBackground(Color.WHITE);
	
	imdiCheckBox = new JCheckBox("IMDI");
        imdiCheckBox.setBackground(Color.WHITE);
	imdiCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
	checkBoxPanel.add(imdiCheckBox);
	cmdiCheckBox = new JCheckBox("CMDI");
        cmdiCheckBox.setBackground(Color.WHITE);
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
	    JOptionPane.showMessageDialog(this, widgets.getString("SETUP_SELECT AT LEAST ONE OF THE OPTIONS BEFORE CONTINUING"), widgets.getString("SETUP_SELECT A METADATA FORMAT"), JOptionPane.WARNING_MESSAGE);
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
	    return ArbilSetupWizard.CMDI_PROFILE_SELECT;
	} else {
	    return ArbilSetupWizard.REMOTE_LOCATIONS;
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
