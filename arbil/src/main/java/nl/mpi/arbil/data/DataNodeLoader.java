
package nl.mpi.arbil.data;

import java.net.URI;

/**
 * @author Peter.Withers@mpi.nl 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface DataNodeLoader {

    void addNodeNeedingSave(ArbilDataNode nodeToSave);

    ArbilDataNode getArbilDataNode(Object registeringObject, URI localUri);

    ArbilDataNode getArbilDataNodeOnlyIfLoaded(URI arbilUri);

    ArbilDataNode getArbilDataNodeWithoutLoading(URI localUri);

    ArbilDataNode[] getNodesNeedSave();

    /**
     * @return the schemaCheckLocalFiles
     */
    boolean isSchemaCheckLocalFiles();

    boolean nodesNeedSave();

    void removeNodesNeedingSave(ArbilDataNode savedNode);

    void requestReload(ArbilDataNode currentDataNode);

    void requestReloadAllNodes();

    void requestReloadOnlyIfLoaded(URI arbilUri);

    void saveNodesNeedingSave(boolean updateIcons);

    /**
     * @param schemaCheckLocalFiles the schemaCheckLocalFiles to set
     */
    void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles);

    void startLoaderThreads();
    
    void stopLoaderThreads();
}
