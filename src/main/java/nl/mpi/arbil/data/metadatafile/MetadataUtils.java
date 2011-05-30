package nl.mpi.arbil.data.metadatafile;

import java.io.File;
import java.net.URI;
import nl.mpi.arbil.ArbilMetadataException;

/**
 *  Document   : MetadataUtils
 *  Created on : May 22, 2010, 7:56:46 AM
 *  Author     : Peter Withers
 */
public interface MetadataUtils {

    public boolean addCorpusLink(URI nodeURI, URI linkURI[]);

    public boolean removeCorpusLink(URI nodeURI, URI linkURI[]);

    public boolean moveMetadataFile(URI sourceURI, File destinationFile, boolean updateLinks);

    public boolean copyMetadataFile(URI sourceURI, File destinationFile, URI[][] linksToUpdate, boolean updateLinks) throws ArbilMetadataException;

    public URI[] getCorpusLinks(URI nodeURI) throws ArbilMetadataException;
}
