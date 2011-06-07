/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.util;

import java.net.URI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface TreeHelper {

    int addDefaultCorpusLocations();

    boolean addLocation(URI addedLocation);

    void applyRootLocations();

    void deleteNodes(Object sourceObject);

    /**
     * @return the favouriteNodes
     */
    ArbilDataNode[] getFavouriteNodes();

    /**
     * @return the favouritesTreeModel
     */
    DefaultTreeModel getFavouritesTreeModel();

    /**
     * @return the localCorpusNodes
     */
    ArbilDataNode[] getLocalCorpusNodes();

    /**
     * @return the localCorpusTreeModel
     */
    DefaultTreeModel getLocalCorpusTreeModel();

    /**
     * @return the localDirectoryTreeModel
     */
    DefaultTreeModel getLocalDirectoryTreeModel();

    /**
     * @return the localFileNodes
     */
    ArbilDataNode[] getLocalFileNodes();

    DefaultTreeModel getModelForNode(DefaultMutableTreeNode nodeToTest);

    /**
     * @return the remoteCorpusNodes
     */
    ArbilDataNode[] getRemoteCorpusNodes();

    /**
     * @return the remoteCorpusTreeModel
     */
    DefaultTreeModel getRemoteCorpusTreeModel();

    boolean isInFavouritesNodes(ArbilDataNode dataNode);

    /**
     * @return the showHiddenFilesInTree
     */
    boolean isShowHiddenFilesInTree();

    void jumpToSelectionInTree(boolean silent, ArbilDataNode cellDataNode);

    void loadLocationsList();

    boolean locationsHaveBeenAdded();

    void removeLocation(ArbilDataNode removeObject);

    void removeLocation(URI removeLocation);

    void saveLocations(ArbilDataNode[] nodesToAdd, ArbilDataNode[] nodesToRemove);

    void setShowHiddenFilesInTree(boolean showState);
    
}
