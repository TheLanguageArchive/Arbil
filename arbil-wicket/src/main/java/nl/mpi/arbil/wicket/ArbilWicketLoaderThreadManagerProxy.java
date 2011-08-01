package nl.mpi.arbil.wicket;

import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.LoaderThreadManager;
import org.apache.wicket.Session;

/**
 * Proxy for the LoaderThreadManager that is contained in the session that makes
 * the request. To be injected Arbil core classes.
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketLoaderThreadManagerProxy implements LoaderThreadManager{

    private LoaderThreadManager getLoaderThreadManager(){
	return ArbilWicketSession.get().getLoaderThreadManager();
    }
    
    public void addNodeToQueue(ArbilDataNode nodeToAdd) {
	getLoaderThreadManager().addNodeToQueue(nodeToAdd);
    }

    public boolean isSchemaCheckLocalFiles() {
	return getLoaderThreadManager().isSchemaCheckLocalFiles();
    }

    public void setContinueThread(boolean continueThread) {
	getLoaderThreadManager().setContinueThread(continueThread);
    }

    public void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles) {
	getLoaderThreadManager().setSchemaCheckLocalFiles(schemaCheckLocalFiles);
    }

    public void startLoaderThreads() {
	getLoaderThreadManager().startLoaderThreads();
    }
    
}
