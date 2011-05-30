package nl.mpi.arbil.data;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFavouritesSorter extends ArbilNodeSorter {

    @Override
    protected int getTypeIndex(ArbilDataNode targetDataNode) {
        if (targetDataNode.isInfoLink) {
            return 1;
        }
        if (targetDataNode.isDataLoaded()) { // caution: this sort can cause the tree to collapse when nodes reload because the nodes will be removed if not in order
            if (targetDataNode.isCorpus()) {
                return 3;
            } else if (targetDataNode.isCatalogue()) {
                return 4;
            } else if (targetDataNode.isSession()) {
                return 5;
            } else if (targetDataNode.isChildNode()) {
                return 2;
            } else if (targetDataNode.isDirectory()) {
                return 6;
            } else {
                return 7;
            }
        } else {
            // put the loading nodes at the end to help the tree sorting and rendering process
            return 10;
        }
    }
}
