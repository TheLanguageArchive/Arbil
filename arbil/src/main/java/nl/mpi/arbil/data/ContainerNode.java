package nl.mpi.arbil.data;

import java.util.Vector;
import javax.swing.ImageIcon;

/**
 * Document : ContainerNode
 * Created on : Mar 28, 2012, 1:04:22 PM
 * Author : Peter Withers
 */
public class ContainerNode extends ArbilNode implements Comparable {

    private ArbilNode[] childNodes;
    protected String labelString;
    private ImageIcon imageIcon;

    public ContainerNode(String labelString, ImageIcon imageIcon, ArbilNode[] childNodes) {
	this.childNodes = childNodes;
	this.labelString = labelString;
	this.imageIcon = imageIcon;
    }

    public int compareTo(Object o) {
	return labelString.compareTo(o.toString());
    }

    public void setChildNodes(ArbilNode[] childNodes) {
	this.childNodes = childNodes;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final ContainerNode other = (ContainerNode) obj;
	if ((this.labelString == null) ? (other.labelString != null) : !this.labelString.equals(other.labelString)) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int hash = 17;
//        hash = 19 * hash + Arrays.deepHashCode(this.childNodes);
//        hash = 19 * hash + this.childNodes.hashCode();
	hash = 19 * hash + (this.labelString != null ? this.labelString.hashCode() : 0);
	return hash;
    }

    @Override
    public String toString() {
	return labelString + " (" + childNodes.length + ")";
    }

    @Override
    public ArbilNode[] getChildArray() {
	return childNodes;
    }

    @Override
    public ImageIcon getIcon() {
	return imageIcon;
    }

    @Override
    public void registerContainer(ArbilDataNodeContainer containerToAdd) {
	for (ArbilNode currentChild : childNodes) {
	    currentChild.registerContainer(containerToAdd);
	}
    }

    @Override
    public void removeContainer(ArbilDataNodeContainer containerToRemove) {
	for (ArbilNode currentChild : childNodes) {
	    currentChild.removeContainer(containerToRemove);
	}
    }

    @Override
    public ArbilDataNode[] getAllChildren() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getAllChildren(Vector<ArbilDataNode> allChildren) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getChildCount() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasCatalogue() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasHistory() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasLocalResource() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasResource() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isArchivableFile() {
	return false;
    }

    @Override
    public boolean isCatalogue() {
	return false;
    }

    @Override
    public boolean isChildNode() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCmdiMetaDataNode() {
	return false;
    }

    @Override
    public boolean isCorpus() {
	return false;
    }

    @Override
    public boolean isDataLoaded() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDirectory() {
	return false;
    }

    @Override
    public boolean isEditable() {
	return false;
    }

    @Override
    public boolean isEmptyMetaNode() {
	return false;
    }

    @Override
    public boolean isFavorite() {
	return false;
    }

    @Override
    public boolean isLoading() {
	return false;
    }

    @Override
    public boolean isLocal() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMetaDataNode() {
	return false;
    }

    @Override
    public boolean isResourceSet() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSession() {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
