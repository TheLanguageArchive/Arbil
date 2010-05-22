package nl.mpi.arbil.MetadataFile;

import java.io.File;
import java.net.URI;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : MetadataUtils
 *  Created on : May 22, 2010, 7:56:46 AM
 *  Author     : Peter Withers
 */
public interface MetadataUtils {

    public boolean addCorpusLink(ImdiTreeObject targetImdiNodes, ImdiTreeObject[] childImdiNodes);

    public boolean removeCorpusLink(ImdiTreeObject targetImdiNodes, ImdiTreeObject[] childImdiNodes);

    public boolean addCorpusLink(URI nodeURI, URI linkURI);

    public boolean removeCorpusLink(URI nodeURI, URI linkURI);

    public boolean moveImdiFile(URI sourceURI, File destinationFile, boolean updateLinks);

    public boolean copyImdiFile(URI sourceURI, File destinationFile, URI[] linksToUpdate, boolean updateLinks);

    public URI[] getCorpusLinks(URI nodeURI);
}
