/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.mpi.arbil.MetadataFile;

import java.io.File;
import java.net.URI;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : CmdiUtils
 *  Created on : May 22, 2010, 10:30:36 AM
 *  Author     : Peter Withers
 */
public class CmdiUtils implements MetadataUtils {

    public boolean addCorpusLink(ImdiTreeObject targetImdiNodes, ImdiTreeObject[] childImdiNodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addCorpusLink(URI nodeURL, URI linkURL) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean copyImdiFile(URI sourceURI, File destinationFile, URI[] linksToUpdate, boolean updateLinks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean moveImdiFile(URI sourceURL, File destinationFile, boolean updateLinks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeCorpusLink(ImdiTreeObject targetImdiNodes, ImdiTreeObject[] childImdiNodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeCorpusLink(URI nodeURL, URI linkURL) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI[] getCorpusLinks(URI nodeURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
