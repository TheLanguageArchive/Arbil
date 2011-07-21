package nl.mpi.arbil.data;

import java.util.Vector;
import javax.swing.ImageIcon;

/**
 * Interface for nodes, either data nodes, root nodes or potentially other
 * kinds of nodes
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilNode {
    /**
     * Calls getAllChildren(Vector<ArbilDataNode> allChildren) and returns the result as an array
     * @return an array of all the child nodes
     */
    ArbilNode[] getAllChildren();

    /**
     * Used to get all the Arbil child nodes (all levels) of a session or all the nodes contained in a corpus (one level only).
     * @param An empty vector, to which all the child nodes will be added.
     */
    void getAllChildren(Vector<ArbilNode> allChildren);

    /**
     * Gets an array of the children of this node.
     * @return An array of the next level child nodes.
     */
    ArbilNode[] getChildArray();

    ArbilNode[] getChildNodesArray(String childType);
    
    /**
     * Count the next level of child nodes. (non recursive)
     * @return An integer of the next level of child nodes including corpus links and Arbil child nodes.
     */
    int getChildCount();


    /**
     * If not already done calculates the required icon for this node in its current state.
     * Once calculated the stored icon will be returned.
     * To clear the icon and recalculate it "clearIcon()" should be called.
     * @return The icon for this node.
     */
    ImageIcon getIcon();

    boolean hasCatalogue();

    boolean hasHistory();

    /**
     * Tests if a local resource file is associated with this node.
     * @return boolean
     */
    boolean hasLocalResource();

    /**
     * Tests if a resource file (local or remote) is associated with this node.
     * @return boolean
     */
    boolean hasResource();

    /**
     * Tests if there is file associated with this node and if it is an archivable type.
     * The file could be either a resource file (getResource) or a loose file (getUrlString).
     * @return boolean
     */
    boolean isArchivableFile();

    boolean isCatalogue();

    /**
     * Tests if this node represents an imdi file or if if it represents a child node from an imdi file (created by adding fields with child nodes).
     * @return boolean
     */
    boolean isChildNode();

    boolean isCmdiMetaDataNode();

    boolean isCorpus();

    boolean isDirectory();

    boolean isEditable();

    /**
     * Tests if this node is a meta node that contains no fields and only child nodes, such as the Languages, Actors, MediaFiles nodes etc..
     * @return boolean
     */
    boolean isEmptyMetaNode();

    boolean isFavorite();

    boolean isLocal();

    boolean isMetaDataNode();

    /**
     * @return Whether a resource URI has been set for this node
     */
    boolean isResourceSet();

    boolean isSession();

    boolean isLoading();
    boolean isDataLoaded();
    
    void removeFromAllContainers();
    void registerContainer(ArbilDataNodeContainer containerToAdd);
    void removeContainer(ArbilDataNodeContainer containerToRemove);
}
