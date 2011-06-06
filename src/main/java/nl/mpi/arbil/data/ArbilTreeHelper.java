package nl.mpi.arbil.data;

import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Singleton instance of TreeHelper, for use with Arbil desktop application
 * Document   : ArbilTreeHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreeHelper extends AbstractTreeHelper {

    static private ArbilTreeHelper singleInstance = null;

    static synchronized public ArbilTreeHelper getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilTreeHelper();
	}
	return singleInstance;
    }

    private ArbilTreeHelper() {
	super();
	initTrees();
	// load any locations from the previous file formats
	//LinorgFavourites.getSingleInstance().convertOldFormatLocationLists();
	loadLocationsList();
    }

    @Override
    protected SessionStorage getSessionStorage() {
	// Hardwired to work with ArbilSessionStorage (as is the other way around)
	return ArbilSessionStorage.getSingleInstance();
    }
}
