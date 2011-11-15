package nl.mpi.arbil.ui.wizard.setup;

/**
 * Model for storing the user's selected options and entered values in an ArbilWizard session
 * @see ArbilWizard
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSetupWizardModel {
    private boolean imdiSelected;
    private boolean cmdiSelected;

    /**
     * @return the imdiSelected
     */
    public boolean isImdiSelected() {
	return imdiSelected;
    }

    /**
     * @param imdiSelected the imdiSelected to set
     */
    public void setImdiSelected(boolean imdiSelected) {
	this.imdiSelected = imdiSelected;
    }

    /**
     * @return the cmdiSelected
     */
    public boolean isCmdiSelected() {
	return cmdiSelected;
    }

    /**
     * @param cmdiSelected the cmdiSelected to set
     */
    public void setCmdiSelected(boolean cmdiSelected) {
	this.cmdiSelected = cmdiSelected;
    }
}
