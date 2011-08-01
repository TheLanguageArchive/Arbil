/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface LoaderThreadManager {

    void addNodeToQueue(ArbilDataNode nodeToAdd);

    /**
     * @return the schemaCheckLocalFiles
     */
    boolean isSchemaCheckLocalFiles();

    /**
     * @param continueThread the continueThread to set
     */
    void setContinueThread(boolean continueThread);

    /**
     * @param schemaCheckLocalFiles the schemaCheckLocalFiles to set
     */
    void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles);

    void startLoaderThreads();
    
}
