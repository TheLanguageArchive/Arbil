package nl.mpi.arbil.data;

import java.util.Vector;
import javax.swing.ImageIcon;

/**
 * Interface for nodes, either data nodes, root nodes or potentially other
 * kinds of nodes
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilNode {

    protected Vector<ArbilDataNodeContainer> /*<Component>*/ containersOfThisNode;

    public ArbilNode() {
        containersOfThisNode = new Vector<ArbilDataNodeContainer>();
    }

    /**
     * Calls getAllChildren(Vector<ArbilDataNode> allChildren) and returns the result as an array
     * @return an array of all the child nodes
     */
    public abstract ArbilDataNode[] getAllChildren();

    /**
     * Used to get all the Arbil child nodes (all levels) of a session or all the nodes contained in a corpus (one level only).
     * @param An empty vector, to which all the child nodes will be added.
     */
    public abstract void getAllChildren(Vector<ArbilDataNode> allChildren);

    /**
     * Gets an array of the children of this node.
     * @return An array of the next level child nodes.
     */
    public abstract ArbilNode[] getChildArray();

    /**
     * Count the next level of child nodes. (non recursive)
     * @return An integer of the next level of child nodes including corpus links and Arbil child nodes.
     */
    public abstract int getChildCount();

    /**
     * If not already done calculates the required icon for this node in its current state.
     * Once calculated the stored icon will be returned.
     * To clear the icon and recalculate it "clearIcon()" should be called.
     * @return The icon for this node.
     */
    public abstract ImageIcon getIcon();

    /**
     * Register (add) a container for this node
     * @param containerToAdd Object that should be regarded as containing this node
     */
    public void registerContainer(ArbilDataNodeContainer containerToAdd) {
        // Add to collection of containers for future messaging
        if (containerToAdd != null) {
            if (!containersOfThisNode.contains(containerToAdd)) {
                containersOfThisNode.add(containerToAdd);
            }
        }
    }

    public ArbilDataNodeContainer[] getRegisteredContainers() {
        if (containersOfThisNode != null && containersOfThisNode.size() > 0) {
            return containersOfThisNode.toArray(new ArbilDataNodeContainer[0]);
        } else {
            return new ArbilDataNodeContainer[]{};
        }
    }

    /**
     * Removes a UI containers from the list of containers interested in this node.
     * @param containerToRemove The container to be removed from the list.
     */
    public void removeContainer(ArbilDataNodeContainer containerToRemove) {
        // TODO: make sure that containers are removed when a node is removed from the tree, otherwise memory will not get freed
        //        System.out.println("de registerContainer: " + containerToRemove);
        containersOfThisNode.remove(containerToRemove);
    }

    public abstract boolean hasCatalogue();

    public abstract boolean hasHistory();

    /**
     * Tests if a local resource file is associated with this node.
     * @return boolean
     */
    public abstract boolean hasLocalResource();

    /**
     * Tests if a resource file (local or remote) is associated with this node.
     * @return boolean
     */
    public abstract boolean hasResource();

    /**
     * Tests if there is file associated with this node and if it is an archivable type.
     * The file could be either a resource file (getResource) or a loose file (getUrlString).
     * @return boolean
     */
    public abstract boolean isArchivableFile();

    public abstract boolean isCatalogue();

    /**
     * Tests if this node represents an imdi file or if if it represents a child node from an imdi file (created by adding fields with child nodes).
     * @return boolean
     */
    public abstract boolean isChildNode();

    public abstract boolean isCmdiMetaDataNode();

    public abstract boolean isCorpus();

    public abstract boolean isDirectory();

    public abstract boolean isEditable();

    /**
     * Tests if this node is a meta node that contains no fields and only child nodes, such as the Languages, Actors, MediaFiles nodes etc..
     * @return boolean
     */
    public abstract boolean isEmptyMetaNode();

    public abstract boolean isFavorite();

    public abstract boolean isLocal();

    public abstract boolean isMetaDataNode();

    /**
     * @return Whether a resource URI has been set for this node
     */
    public abstract boolean isResourceSet();

    public abstract boolean isSession();

    public abstract boolean isLoading();

    public abstract boolean isDataLoaded();
}