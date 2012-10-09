package nl.mpi.arbil.ui.wizard;

import java.util.HashMap;

/**
 * Model for the ArbilWizard. Hold the wizard's contents and keeps track of the wizard state
 * @see ArbilWizard
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWizardModel {

    private Object currentId = null;
    private final HashMap<Object, ArbilWizardContent> contents = new HashMap<Object, ArbilWizardContent>();

    public void addContent(Object id, ArbilWizardContent content) {
	contents.put(id, content);
    }

    public void setCurrent(Object id) {
	currentId = id;
    }

    public Object getCurrentId() {
	return currentId;
    }

    public ArbilWizardContent getCurrent() {
	return contents.get(currentId);
    }

    public ArbilWizardContent next() {
	final ArbilWizardContent current = getCurrent();
	if (current != null) {
	    currentId = current.getNext();
	    return getCurrent();
	} else {
	    return null;
	}
    }

    public ArbilWizardContent previous() {
	final ArbilWizardContent current = getCurrent();
	if (current != null) {
	    currentId = current.getPrevious();
	    return getCurrent();
	} else {
	    return null;
	}
    }
}
