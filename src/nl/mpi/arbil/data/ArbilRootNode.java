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

    protected ArbilRootNode(String name, ImageIcon icon) {
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String toString() {
        return name;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public ArbilDataNode[] getAllChildren() {
        return getChildArray();
    }

    public void getAllChildren(Vector<ArbilDataNode> allChildren) {
        allChildren.addAll(Arrays.asList(getChildArray()));
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
        return false;
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
}
