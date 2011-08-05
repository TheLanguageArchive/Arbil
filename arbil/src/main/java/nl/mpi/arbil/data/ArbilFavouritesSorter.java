package nl.mpi.arbil.data;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFavouritesSorter extends ArbilNodeSorter {

    @Override
    protected int getTypeIndex(ArbilDataNode targetDataNode) {
	if (targetDataNode.isInfoLink) {
	    return 100;
	}
	if (targetDataNode.isDataLoaded()) { // caution: this sort can cause the tree to collapse when nodes reload because the nodes will be removed if not in order
	    if (targetDataNode.isCorpus()) {
		return 300;
	    } else if (targetDataNode.isCatalogue()) {
		return 400;
	    } else if (targetDataNode.isSession()) {
		return 500;
	    } else if (targetDataNode.isChildNode()) {
		return 200 + getChildNodeTypeIndex(targetDataNode);
	    } else if (targetDataNode.isDirectory()) {
		return 600;
	    } else {
		return 700;
	    }
	} else {
	    // put the loading nodes at the end to help the tree sorting and rendering process
	    return 1000;
	}
    }

    private int getChildNodeTypeIndex(ArbilDataNode targetDataNode) {
	return Math.abs(targetDataNode.getURI().getFragment().replaceAll("\\(.*\\)", "").hashCode()) % 100;
    }
}
