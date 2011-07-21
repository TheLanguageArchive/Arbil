package nl.mpi.arbil.data;

import java.util.Arrays;
import java.util.Vector;
import javax.swing.ImageIcon;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilRootNode implements ArbilNode {

    private String name;
    private ImageIcon icon;
    private boolean local;
    private Vector<ArbilDataNodeContainer> /*<Component>*/ containersOfThisNode;

    protected ArbilRootNode(String name, ImageIcon icon, boolean local) {
	this.name = name;
	this.icon = icon;
	this.local = local;
    }

    @Override
    public String toString() {
	return name;
    }

    public ImageIcon getIcon() {
	return icon;
    }

    public ArbilNode[] getAllChildren() {
	return getChildArray();
    }

    public void getAllChildren(Vector<ArbilNode> allChildren) {
	allChildren.addAll(Arrays.asList(getChildArray()));
    }
    
    /**
     * Gets the second level child nodes from the fist level child node matching the child type string.
     * Used to populate the child nodes in the table cell.
     * @param childType The name of the first level child to query.
     * @return An object array of all second level child nodes in the first level node.
     */
    public ArbilNode[] getChildNodesArray(String childType) {
	for (ArbilNode currentNode : getChildArray()) {
	    if (currentNode.toString().equals(childType)) {
		return currentNode.getChildArray();
	    }
	}
	return null;
    }

    public int getChildCount() {
	return getChildArray().length;
    }

    public boolean hasCatalogue() {
	return false;
    }

    public boolean hasHistory() {
	return false;
    }

    public boolean hasLocalResource() {
	return false;
    }

    public boolean hasResource() {
	return false;
    }

    public boolean isArchivableFile() {
	return false;
    }

    public boolean isCatalogue() {
	return false;
    }

    public boolean isChildNode() {
	return false;
    }

    public boolean isCmdiMetaDataNode() {
	return false;
    }

    public boolean isCorpus() {
	return false;
    }

    public boolean isDirectory() {
	return false;
    }

    public boolean isEditable() {
	return false;
    }

    public boolean isEmptyMetaNode() {
	return false;
    }

    public boolean isFavorite() {
	return false;
    }

    public boolean isLocal() {
	return this.local;
    }

    public boolean isMetaDataNode() {
	return false;
    }

    public boolean isResourceSet() {
	return false;
    }

    public boolean isSession() {
	return false;
    }

    public boolean isLoading() {
	return false;
    }

    public boolean isDataLoaded() {
	return true;
    }

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

    public synchronized void removeFromAllContainers() {
	// todo: this should also scan all child nodes and also remove them in the same way
	for (ArbilNode currentChildNode : this.getAllChildren()) {
	    currentChildNode.removeFromAllContainers();
	}
	for (ArbilDataNodeContainer currentContainer : containersOfThisNode.toArray(new ArbilDataNodeContainer[]{})) {
	    try {
		//ArbilDataNodeContainer currentContainer = containersIterator.nextElement();
		currentContainer.dataNodeRemoved(this);
	    } catch (java.util.NoSuchElementException ex) {
	    }
	}
    }
}
