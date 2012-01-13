package nl.mpi.arbil.data.metadatafile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader.CmdiResourceLink;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.BugCatcherManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *  Document   : CmdiUtils
 *  Created on : May 22, 2010, 10:30:36 AM
 *  Author     : Peter Withers
 */
public class CmdiUtils implements MetadataUtils {

    public boolean addCorpusLink(URI nodeURI, URI[] linkURI) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean copyMetadataFile(URI sourceURI, File destinationFile, URI[][] linksToUpdate, boolean updateLinks) {
	try {
	    final ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
	    final Document document = ArbilComponentBuilder.getDocument(sourceURI);
	    copySchemaFile(sourceURI, destinationFile, document);

	    // Update all links that need updating in document
	    CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
	    ArrayList<CmdiResourceLink> links = cmdiComponentLinkReader.readLinks(sourceURI);
	    if (links != null && updateLinks) {
		for (CmdiResourceLink link : links) {
		    String ref = link.resourceRef;
		    // Compare to links to update
		    if (linksToUpdate != null) {
			for (URI[] updatableLink : linksToUpdate) {
			    if (updatableLink[0].toString().equals(ref)) {
				// Found: try to update. First relativize reference URI.
				URI newReferenceURi = destinationFile.getParentFile().toURI().relativize(updatableLink[1]);
				if (!componentBuilder.updateResourceProxyReference(document, link.resourceProxyId, newReferenceURi)) {
				    BugCatcherManager.getBugCatcher().logError("Could not update resource proxy with id" + link.resourceProxyId + " in " + sourceURI.toString(), null);
				}
				break;
			    }
			}
		    }
		}
	    }
	    // Write to disk
	    ArbilComponentBuilder.savePrettyFormatting(document, destinationFile);
	    return true;
	} catch (IOException e) {
	    BugCatcherManager.getBugCatcher().logError(e);
	} catch (ParserConfigurationException e) {
	    BugCatcherManager.getBugCatcher().logError(e);
	} catch (SAXException e) {
	    BugCatcherManager.getBugCatcher().logError(e);
	}
	return false;
    }

    /**
     * Returns all ResourceLinks in the specified file that are CMDI metadata instances
     * @param nodeURI
     * @return 
     */
    public URI[] getCorpusLinks(URI nodeURI) {
	ArrayList<URI> returnUriList = new ArrayList<URI>();
	// Get resource links in file
	CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
	ArrayList<CmdiResourceLink> links = cmdiComponentLinkReader.readLinks(nodeURI);
	if (links != null) {
	    // Traverse links
	    for (CmdiResourceLink link : links) {
		try {
		    URI linkUri = link.getLinkUri();
		    if (linkUri != null && ArbilDataNode.isPathCmdi(linkUri.toString())) {
			// Link is CMDI metadata, include in result
			if (!linkUri.isAbsolute()) {
			    // Resolve to absolute path
			    linkUri = nodeURI.resolve(linkUri);
			}
			returnUriList.add(linkUri);
		    }
		} catch (URISyntaxException ex) {
		    BugCatcherManager.getBugCatcher().logError("Invalid link URI found in " + nodeURI.toString(), ex);
		}
	    }
	}
	return returnUriList.toArray(new URI[]{});
    }

    public boolean moveMetadataFile(URI sourceURI, File destinationFile, boolean updateLinks) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeCorpusLink(URI nodeURI, URI[] linkURI) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Copies all schema files found in the specified document that are local or relative to the destination of the MD copy action
     * @param mdSourceURI Source location of the metadata file copy action
     * @param mdDestinationFile Destination file for the metadata file copy action
     * @param mdDocument Metadata document that is being copied
     */
    private void copySchemaFile(final URI mdSourceURI, final File mdDestinationFile, final Document mdDocument) {
	// Get CMD root element
	Node rootNode = mdDocument.getFirstChild();
	if (rootNode instanceof Element) {
	    Element cmdElement = (Element) rootNode;
	    Attr schemaAttr = cmdElement.getAttributeNode("xsi:schemaLocation");
	    if (schemaAttr != null) {
		String[] schemaLocationValues = schemaAttr.getValue().split("\\s+");
		String[] newSchemaLocationValues = new String[schemaLocationValues.length];
		for (int i = 0; i + 1 < schemaLocationValues.length; i += 2) {
		    final String nameSpace = schemaLocationValues[i];
		    final String originalLocation = schemaLocationValues[i + 1];

		    // Keep original name space
		    newSchemaLocationValues[i] = nameSpace;
		    // Check schema location URI
		    try {
			URI locationURI = new URI(originalLocation);
			// Relative and local URIs should be copied over
			if (!locationURI.isAbsolute() || locationURI.getScheme().equals("file")) {
			    // Determine copy destination
			    locationURI = mdSourceURI.resolve(locationURI);
			    File originalLocationFile = new File(locationURI);
			    File exportDirectory = mdDestinationFile.getParentFile();
			    File newLocationFile = new File(exportDirectory, originalLocationFile.getName());

			    // Try to copy schema file to export directory
			    if (copySchemaFile(originalLocationFile, newLocationFile)) {
				// Insert into document as relative path
				URI newLocationURI = exportDirectory.toURI().relativize(newLocationFile.toURI());
				newSchemaLocationValues[i + 1] = newLocationURI.toString();
			    } else {
				// In case of failure to copy, keep referring to original location as fallback
				newSchemaLocationValues[i + 1] = originalLocation;
			    }
			} else {
			    newSchemaLocationValues[i + 1] = originalLocation;
			}
		    } catch (URISyntaxException ex) {
			BugCatcherManager.getBugCatcher().logError("Problematic URI in xsi:schemaLocation: " + schemaLocationValues[i], ex);
		    }
		}
		// Concat new schema location values..
		StringBuilder schemaLocationsBuilder = new StringBuilder();
		for (String locationValue : newSchemaLocationValues) {
		    schemaLocationsBuilder.append(locationValue).append(" ");
		}
		// .. and set as new value for the schemaLocation attribute
		schemaAttr.setValue(schemaLocationsBuilder.toString());
	    }
	}
    }

    private boolean copySchemaFile(File originalLocation, File targetLocation) {
	try {
	    FileInputStream inStream = new FileInputStream(originalLocation);
	    try {
		return 0 != copyFile(inStream, targetLocation);
	    } finally {
		inStream.close();
	    }
	} catch (FileNotFoundException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	return false;
    }

    /**
     * 
     * @param inStream
     * @param outFile
     * @return Number of bytes read
     * @throws FileNotFoundException When output file not found
     * @throws IOException 
     */
    private static int copyFile(InputStream inStream, File outFile) throws FileNotFoundException, IOException {
	final int bufferLength = 1024 * 3;
	final byte[] buffer = new byte[bufferLength];
	final FileOutputStream outStream = new FileOutputStream(outFile); //targetUrlString
	int totalRead = 0;
	try {
	    int bytesread = 0;
	    while (bytesread >= 0) {
		bytesread = inStream.read(buffer);
		totalRead += bytesread;
		if (bytesread == -1) {
		    break;
		}
		outStream.write(buffer, 0, bytesread);
	    }
	} finally {
	    outStream.close();
	}
	return totalRead;
    }
}
