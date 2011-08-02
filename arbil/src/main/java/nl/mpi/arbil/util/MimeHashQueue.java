/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.util;

import nl.mpi.arbil.data.ArbilDataNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface MimeHashQueue {

    /**
     * Adds a node to the queue for processing. Only nodes with resources will
     * actually be processed
     * @param dataNode Data node to be processed
     */
    void addToQueue(ArbilDataNode dataNode);

    /**
     * @return Whether resource permissions are checked
     */
    boolean isCheckResourcePermissions();

    /**
     * @param checkResourcePermissions Whether to check resource permissions
     */
    void setCheckResourcePermissions(boolean checkResourcePermissions);

    /**
     * Makes sure the mime hash queue thread is started
     */
    void startMimeHashQueueThread();
    
}
