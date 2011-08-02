package nl.mpi.arbil.data;

/**
 * Document   : ArbilDataNodeLoader formerly known as ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author Peter.Withers@mpi.nl 
 */
public class ArbilDataNodeLoader extends DefaultDataNodeLoader {

    static private ArbilDataNodeLoader singleInstance = null;
    private static LoaderThreadManager threadManager;
        
    public static synchronized ArbilDataNodeLoader getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilDataNodeLoader();
	}
	return singleInstance;
    }

    private ArbilDataNodeLoader(){
	super(new ArbilDataNodeLoaderThreadManager());
    }

}
