package nl.mpi.arbil.ui.wizard.setup;

import java.io.Serializable;

/**
 * Model for storing the user's selected options and entered values in an ArbilSetupWizard session
 * @see ArbilWizard
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSetupWizardModel implements Serializable {

    private boolean imdiSelected;
    private boolean cmdiSelected;
    private transient String remoteLocations;

    public boolean isImdiSelected() {
	return imdiSelected;
    }

    public void setImdiSelected(boolean imdiSelected) {
	this.imdiSelected = imdiSelected;
    }

    public boolean isCmdiSelected() {
	return cmdiSelected;
    }

    public void setCmdiSelected(boolean cmdiSelected) {
	this.cmdiSelected = cmdiSelected;
    }

    public String getRemoteLocations() {
	return remoteLocations;
    }

    public void setRemoteLocations(String remoteLocations) {
	this.remoteLocations = remoteLocations;
    }
}
