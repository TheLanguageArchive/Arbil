package nl.mpi.arbil.data.metadatafile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader.CmdiResourceLink;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.BugCatcher;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *  Document   : CmdiUtils
 *  Created on : May 22, 2010, 10:30:36 AM
 *  Author     : Peter Withers
 */
public class CmdiUtils implements MetadataUtils {

    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
    }

    public boolean addCorpusLink(URI nodeURI, URI[] linkURI) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean copyMetadataFile(URI sourceURI, File destinationFile, URI[][] linksToUpdate, boolean updateLinks) {
	try {
	    ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
	    Document document = ArbilComponentBuilder.getDocument(sourceURI);

	    // Update all links that need updating in document
	    CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
	    ArrayList<CmdiResourceLink> links = cmdiComponentLinkReader.readLinks(sourceURI);
	    if (links != null) {
		for (CmdiResourceLink link : links) {
		    String ref = link.resourceRef;
		    // Compare to links to update
		    for (URI[] updatableLink : linksToUpdate) {
			if (updatableLink[0].toString().equals(ref)) {
			    // Found: try to update. First relativize reference URI.
			    URI newReferenceURi = destinationFile.getParentFile().toURI().relativize(updatableLink[1]);
			    if (!componentBuilder.updateResourceProxyReference(document, link.resourceProxyId, newReferenceURi)) {
				bugCatcher.logError("Could not update resource proxy with id" + link.resourceProxyId + " in " + sourceURI.toString(), null);
			    }
			    break;
			}
		    }
		}
	    }
	    // Write to disk
	    ArbilComponentBuilder.savePrettyFormatting(document, destinationFile);
	    return true;
	} catch (IOException e) {
	    bugCatcher.logError(e);
	} catch (ParserConfigurationException e) {
	    bugCatcher.logError(e);
	} catch (SAXException e) {
	    bugCatcher.logError(e);
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
		    bugCatcher.logError("Invalid link URI found in " + nodeURI.toString(), ex);
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
}
