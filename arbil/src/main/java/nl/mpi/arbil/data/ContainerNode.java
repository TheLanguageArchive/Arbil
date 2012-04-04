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
    private String labelString;
    private ImageIcon imageIcon;

    public ContainerNode(String labelString, ImageIcon imageIcon, ArbilNode[] childNodes) {
        this.childNodes = childNodes;
        this.labelString = labelString + " (" + childNodes.length + ")";
        this.imageIcon = imageIcon;
    }

    public int compareTo(Object o) {
        return labelString.compareTo(o.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 17;
//        hash = 19 * hash + Arrays.deepHashCode(this.childNodes);
        hash = 19 * hash + (this.labelString != null ? this.labelString.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return labelString;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCatalogue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isChildNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCmdiMetaDataNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCorpus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDataLoaded() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDirectory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEditable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmptyMetaNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isFavorite() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLoading() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLocal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMetaDataNode() {
        throw new UnsupportedOperationException("Not supported yet.");
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
