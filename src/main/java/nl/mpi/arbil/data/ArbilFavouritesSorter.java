package nl.mpi.arbil.data;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFavouritesSorter extends ArbilNodeSorter {

    @Override
    protected int getChildNodeTypeIndex(ArbilNode targetDataNode) {
        if (targetDataNode instanceof ArbilDataNode) {
            return Math.abs(((ArbilDataNode) targetDataNode).getURI().getFragment().replaceAll("\\(.*\\)", "").hashCode()) % 100;
        } else {
            return Math.abs(targetDataNode.hashCode()) % 100;
        }
    }
}
