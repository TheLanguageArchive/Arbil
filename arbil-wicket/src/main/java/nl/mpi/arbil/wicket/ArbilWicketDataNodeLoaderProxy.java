package nl.mpi.arbil.wicket;

import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.DataNodeLoader;

/**
 * Proxy for the DataNodeLoader that is contained in the session that makes
 * the request. To be injected into Arbil core classes.
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketDataNodeLoaderProxy implements DataNodeLoader{

    private DataNodeLoader getDataNodeLoader() {
	// Data node loader is retrieved from the session - each session has its own.
	return ArbilWicketSession.get().getDataNodeLoader();
    }
    
    public void addNodeNeedingSave(ArbilDataNode nodeToSave) {
	getDataNodeLoader().addNodeNeedingSave(nodeToSave);
    }

    public ArbilDataNode getArbilDataNode(Object registeringObject, URI localUri) {
	return getDataNodeLoader().getArbilDataNode(registeringObject, localUri);
    }

    public ArbilDataNode getArbilDataNodeOnlyIfLoaded(URI arbilUri) {
	return getDataNodeLoader().getArbilDataNodeOnlyIfLoaded(arbilUri);
    }

    public ArbilDataNode getArbilDataNodeWithoutLoading(URI localUri) {
	return getDataNodeLoader().getArbilDataNodeWithoutLoading(localUri);
    }

    public ArbilDataNode[] getNodesNeedSave() {
	return getDataNodeLoader().getNodesNeedSave();
    }

    public boolean isSchemaCheckLocalFiles() {
	return getDataNodeLoader().isSchemaCheckLocalFiles();
    }

    public boolean nodesNeedSave() {
	return getDataNodeLoader().nodesNeedSave();
    }

    public void removeNodesNeedingSave(ArbilDataNode savedNode) {
	getDataNodeLoader().removeNodesNeedingSave(savedNode);
    }

    public void requestReload(ArbilDataNode currentDataNode) {
	getDataNodeLoader().requestReload(currentDataNode);
    }

    public void requestReloadAllNodes() {
	getDataNodeLoader().requestReloadAllNodes();
    }

    public void requestReloadOnlyIfLoaded(URI arbilUri) {
	getDataNodeLoader().requestReloadOnlyIfLoaded(arbilUri);
    }

    public void saveNodesNeedingSave(boolean updateIcons) {
	getDataNodeLoader().saveNodesNeedingSave(updateIcons);
    }

    public void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles) {
	getDataNodeLoader().setSchemaCheckLocalFiles(schemaCheckLocalFiles);
    }

    public void startLoaderThreads() {
	getDataNodeLoader().startLoaderThreads();
    }

    public void stopLoaderThreads() {
	getDataNodeLoader().stopLoaderThreads();
    }

    public ArbilDataNode createNewDataNode(URI uri) {
	return getDataNodeLoader().createNewDataNode(uri);
    }

    public void requestShallowReload(ArbilDataNode adn) {
	getDataNodeLoader().requestShallowReload(adn);
    }
    
}
