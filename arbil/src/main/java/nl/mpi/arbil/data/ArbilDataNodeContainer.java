/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilDataNodeContainer {

    /**
     * Data node is to be removed from the container
     *
     * @param dataNode Data node that should be removed
     */
    void dataNodeRemoved(ArbilNode dataNode);

    /**
     * Data node is clearing its icon
     *
     * @param dataNode Data node that is clearing its icon
     */
    void dataNodeIconCleared(ArbilNode dataNode);

    /**
     * A new child node has been added to the destination node
     *
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    void dataNodeChildAdded(ArbilNode destination, ArbilNode newChildNode);

    /**
     * 
     * @return whether a {@link ArbilDataNode.LoadingState#LOADED} state is required. If false, this indicates a 
     * {@link ArbilDataNode.LoadingState#PARTIAL} suffices.
     */
    boolean isFulllyLoadedNodeRequired();
}
