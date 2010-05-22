package nl.mpi.arbil.MetadataFile;

import java.io.File;
import java.net.URI;

/**
 *  Document   : CmdiUtils
 *  Created on : May 22, 2010, 10:30:36 AM
 *  Author     : Peter Withers
 */
public class CmdiUtils implements MetadataUtils {

    public boolean addCorpusLink(URI nodeURI, URI[] linkURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean copyMetadataFile(URI sourceURI, File destinationFile, URI[] linksToUpdate, boolean updateLinks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI[] getCorpusLinks(URI nodeURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean moveMetadataFile(URI sourceURI, File destinationFile, boolean updateLinks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeCorpusLink(URI nodeURI, URI[] linkURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
