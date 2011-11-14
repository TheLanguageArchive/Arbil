/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.wizard.setup;

import javax.swing.JCheckBox;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MetadataFormatSelectContent extends TextInstructionWizardContent {

    private ArbilSetupWizardModel model;

    public MetadataFormatSelectContent(ArbilSetupWizardModel model) {
	super("/nl/mpi/arbil/resources/html/wizard/MetadataFormatSelect.html");
	this.model = model;

	add(new JCheckBox("IDMI"));
	add(new JCheckBox("CDMI"));
    }

    public Object getNext() {
	return null;
    }

    public Object getPrevious() {
	return ArbilSetupWizard.INTRODUCTION;
    }
}
