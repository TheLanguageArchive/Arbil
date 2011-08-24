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
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilNode dataNode);
    /**
     * Data node is clearing its icon
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilNode dataNode);
}
