package nl.mpi.arbil.data;

/**
 * Document   : ArbilDataNodeLoader formerly known as ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author Peter.Withers@mpi.nl 
 */
public class ArbilDataNodeLoader extends AbstractDataNodeLoader {

    static private ArbilDataNodeLoader singleInstance = null;
    private static LoaderThreadManager threadManager;

    public static void setLoaderThreadManager(LoaderThreadManager loaderThreadManagerInstance) {
	threadManager = loaderThreadManagerInstance;
    }
        
    public static synchronized ArbilDataNodeLoader getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilDataNodeLoader();
	}
	return singleInstance;
    }

    private ArbilDataNodeLoader(){
	super(threadManager);
    }

}
