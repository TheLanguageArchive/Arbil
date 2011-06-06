package nl.mpi.arbil.wicket;

import nl.mpi.arbil.data.AbstractTreeHelper;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * TreeHelper that is tied to a specific session storage
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTreeHelper extends AbstractTreeHelper {

    private SessionStorage sessionStorage;
    
    /**
     * 
     * @param sessionStorage Storage to tie treehelper to
     */
    public ArbilWicketTreeHelper(SessionStorage sessionStorage) {
	super();
	this.sessionStorage = sessionStorage;
	initTrees();
	loadLocationsList();
    }

    @Override
    protected synchronized SessionStorage getSessionStorage() {
	return sessionStorage;
    }
}
