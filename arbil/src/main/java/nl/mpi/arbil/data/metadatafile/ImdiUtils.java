/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data.metadatafile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;
import javax.xml.transform.TransformerException;
import nl.mpi.imdi.api.IMDIDom;
import nl.mpi.imdi.api.IMDILink;
import nl.mpi.imdi.api.WSNodeType;
import nl.mpi.util.OurURL;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *  Document   : ImdiUtilsL
 *  Created on : May 21, 2010, 9:30:03 PM
 *  Author     : Peter Withers
 */
public class ImdiUtils implements MetadataUtils {
    private final static Logger logger = LoggerFactory.getLogger(ImdiUtils.class);

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    public final static IMDIDom api = new IMDIDom();

    private boolean isCatalogue(URI sourceURI) {
	try {
	    OurURL inUrlLocal = new OurURL(sourceURI.toURL());
	    org.w3c.dom.Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
	    checkImdiApiResult(nodDom, sourceURI);
	    return null != org.apache.xpath.XPathAPI.selectSingleNode(nodDom, "/:METATRANSCRIPT/:Catalogue");
	} catch (MalformedURLException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (TransformerException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	}
	return false;
    }

    private boolean isSession(URI sourceURI) {
	try {
	    OurURL inUrlLocal = new OurURL(sourceURI.toURL());
	    org.w3c.dom.Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
	    checkImdiApiResult(nodDom, sourceURI);
	    return null != org.apache.xpath.XPathAPI.selectSingleNode(nodDom, "/:METATRANSCRIPT/:Session");
	} catch (MalformedURLException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (TransformerException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	}
	return false;
    }

    // addDomIds is only used for testing
//    public boolean addDomIds(URI nodeURI) {
//        try {
//            Document nodDom;
//            OurURL inUrlLocal = new OurURL(nodeURI.toURL());
//            nodDom = api.loadIMDIDocument(inUrlLocal, false);
//            if (nodDom == null) {
//                BugCatcherManager.getBugCatcher().logError(new Exception(api.getMessage()));
//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Add Link");
//                return false;
//            } else {
//                api.writeDOM(nodDom, new File(nodeURI), false);
//                return true;
//            }
//        } catch (MalformedURLException ex) {
//            BugCatcherManager.getBugCatcher().logError(ex);
//        }
//        return true;
//    }
    public boolean addCorpusLink(URI nodeURI, URI linkURI[]) {
	try {
	    Document nodDom;
	    OurURL inUrlLocal = new OurURL(nodeURI.toURL());
	    nodDom = api.loadIMDIDocument(inUrlLocal, false);
	    checkImdiApiResult(nodDom, nodeURI);
	    if (nodDom == null) {
		BugCatcherManager.getBugCatcher().logError(new Exception(api.getMessage()));
		messageDialogHandler.addMessageDialogToQueue("Error reading via the IMDI API", "Add Link");
		return false;
	    } else {
		int nodeType = WSNodeType.CORPUS;
		for (URI currentLinkUri : linkURI) {
		    if (isCatalogue(currentLinkUri)) {
			nodeType = WSNodeType.CATALOGUE;
		    }
		    if (isSession(currentLinkUri)) {
			nodeType = WSNodeType.SESSION;
		    }
		    IMDILink createdLink = api.createIMDILink(nodDom, inUrlLocal, currentLinkUri.toString(), "", nodeType, "");
		    checkImdiApiResult(createdLink, nodeURI);
		    if (createdLink == null) {
			return false;
		    }
		}
		return api.writeDOM(nodDom, new File(nodeURI), true);
	    }
	} catch (MalformedURLException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	return true;
    }

    public boolean copyMetadataFile(URI sourceURI, File destinationFile, URI[][] linksToUpdate, boolean updateLinks) throws ArbilMetadataException {
	try {
	    OurURL inUrlLocal = new OurURL(sourceURI.toURL());
	    OurURL destinationUrl = new OurURL(destinationFile.toURI().toURL());

	    org.w3c.dom.Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
	    checkImdiApiResult(nodDom, sourceURI);
	    if (nodDom == null) {
		BugCatcherManager.getBugCatcher().logError(new Exception(api.getMessage()));
		messageDialogHandler.addMessageDialogToQueue("Error reading via the IMDI API", "Copy IMDI File");
		return false;
	    } else {
		IMDILink[] links = api.getIMDILinks(nodDom, inUrlLocal, WSNodeType.UNKNOWN);
		checkImdiApiResult(links, sourceURI);
		if (links != null && updateLinks) {
		    for (IMDILink currentLink : links) {
			URI linkUriToUpdate = null;
			if (linksToUpdate != null) {
			    for (URI[] updatableLink : linksToUpdate) {
				try {
				    if (currentLink.getRawURL().toURL().toURI().equals(updatableLink[0])) {
					linkUriToUpdate = updatableLink[1];
					break;
				    }
				} catch (URISyntaxException exception) {
				    BugCatcherManager.getBugCatcher().logError(exception);
				}
			    }
			    logger.debug("currentLink: " + linkUriToUpdate + " : " + currentLink.getRawURL().toString());
			    if (linkUriToUpdate != null) {
				// todo: this is not going to always work because the changeIMDILink is too limited, when a link points to a different domain for example
				// todo: cont... or when a remote imdi is imported without its files then exported while copying its files, the files will be copied but the links not updated by the api
				// todo: cont... this must instead take oldurl newurl and the new imdi file location
//                            boolean changeLinkResult = api.changeIMDILink(nodDom, new OurURL(linkUriToUpdate.toURL()), currentLink);
//                            if (!changeLinkResult) {
//                                checkImdiApiResult(null, sourceURI);
//                                return false;
//                            }
				// todo: check how removeIMDILink and createIMDILink handles info links compared to changeIMDILink
				// Changed this to use setURL that has now been suggested but was previously advised against, in the hope of resolving the numerous errors with the api such as info links issues and resource data issues and bad url construction in links.
				currentLink.setURL(new OurURL(linkUriToUpdate.toURL()));
				//logger.debug("currentLink.getURL: " + currentLink.getURL());
				boolean changeLinkResult = api.changeIMDILink(nodDom, destinationUrl, currentLink);
				if (!changeLinkResult) {
				    checkImdiApiResult(null, sourceURI);
				    // changeIMDILink appears to always return false, at very least for corpus nodes!
				    //return false;
				}

//                            String archiveHandle = currentLink.getURID();
//                            api.removeIMDILink(nodDom, currentLink);
//                            IMDILink replacementLink = api.createIMDILink(nodDom, destinationUrl, linkUriToUpdate.toString(), currentLink.getLinkName(), currentLink.getNodeType(), currentLink.getSpec());
//                            // preserve the archive handle so that LAMUS knows where it came from
//                            if (replacementLink != null) {
//                                replacementLink.setURID(archiveHandle);
//                            } else {
//                                checkImdiApiResult(null, sourceURI);
////                                throw new ArbilMetadataException("IMDI API returned null, no further information is available");
//                            }
//                            boolean changeLinkResult = api.changeIMDILink(nodDom, destinationUrl, replacementLink);
//                            if (!changeLinkResult) {
//                                checkImdiApiResult(null, sourceURI);
//                                return false;
//                            }
			    } else {
				BugCatcherManager.getBugCatcher().logError(new Exception(api.getMessage()));
				return false;
			    }
			}
		    }
		}
		boolean removeIdAttributes = true;
		return api.writeDOM(nodDom, destinationFile, removeIdAttributes);
	    }
	} catch (IOException e) {
	    BugCatcherManager.getBugCatcher().logError(e);
	    return false;
	}
    }

    public boolean moveMetadataFile(URI sourceURI, File destinationFile, boolean updateLinks) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeCorpusLink(URI nodeURI, URI linkURI[]) {
	try {
	    OurURL destinationUrl = new OurURL(nodeURI.toString());
	    Document nodDom = api.loadIMDIDocument(destinationUrl, false);
	    checkImdiApiResult(nodDom, nodeURI);
	    if (nodDom == null) {
		BugCatcherManager.getBugCatcher().logError(new Exception(api.getMessage()));
		messageDialogHandler.addMessageDialogToQueue("Error reading via the IMDI API", "Remove IMDI Links");
		return false;
	    }
	    IMDILink[] allImdiLinks;
	    allImdiLinks = api.getIMDILinks(nodDom, destinationUrl, WSNodeType.UNKNOWN);
	    checkImdiApiResult(allImdiLinks, nodeURI);
	    if (allImdiLinks != null) {
		for (IMDILink currentLink : allImdiLinks) {
		    for (URI currentUri : linkURI) {
			try {
			    if (currentUri.equals(currentLink.getRawURL().toURL().toURI())) {
				if (!api.removeIMDILink(nodDom, currentLink)) {
				    checkImdiApiResult(null, nodeURI);
				    return false;
				}
			    }
			} catch (URISyntaxException exception) {
			    BugCatcherManager.getBugCatcher().logError(exception);
			}
		    }
		}
		boolean removeIdAttributes = true;
		return api.writeDOM(nodDom, new File(nodeURI), removeIdAttributes);
	    }
	} catch (MalformedURLException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	    messageDialogHandler.addMessageDialogToQueue("Error reading links via the IMDI API", "Get Links");
	}
	return false;
    }

    public URI[] getCorpusLinks(URI nodeURI) throws ArbilMetadataException {
	try {
	    OurURL destinationUrl = new OurURL(nodeURI.toString());
	    Document nodDom = api.loadIMDIDocument(destinationUrl, false);
	    checkImdiApiResult(nodDom, nodeURI);
	    if (nodDom == null) {
		throw new ArbilMetadataException("could not load file");
	    }
	    IMDILink[] allImdiLinks;
	    allImdiLinks = api.getIMDILinks(nodDom, destinationUrl, WSNodeType.UNKNOWN);
	    checkImdiApiResult(allImdiLinks, nodeURI);
	    if (allImdiLinks != null) {
		URI[] returnUriArray = new URI[allImdiLinks.length];
		for (int linkCount = 0; linkCount < allImdiLinks.length; linkCount++) {
		    try {
			checkImdiApiResult(allImdiLinks[linkCount], nodeURI);
			returnUriArray[linkCount] = allImdiLinks[linkCount].getRawURL().toURL().toURI();
			checkImdiApiResult(returnUriArray[linkCount], nodeURI);
		    } catch (URISyntaxException exception) {
			BugCatcherManager.getBugCatcher().logError(exception);
			messageDialogHandler.addMessageDialogToQueue("Error reading one of the links via the IMDI API", "Get Links");
		    }
		}
		return returnUriArray;
	    }
	} catch (MalformedURLException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	    messageDialogHandler.addMessageDialogToQueue("Error reading links via the IMDI API", "Get Links");
	}
	return null;
    }

    private void checkImdiApiResult(Object resultUnknown, URI imdiURI) {
	if (resultUnknown == null) {
	    BugCatcherManager.getBugCatcher().logError(new Exception("The IMDI API returned null for: " + imdiURI.toString()));
	    BugCatcherManager.getBugCatcher().logError("The following is the last known error from the API: ", new Exception(api.getMessage()));
	}
    }

    /**
     * Lets the user override type checker decision about mimetype and archivability
     * @param selectedNodes
     * @return Whether action was carried out (not canceled)
     */
    public boolean overrideTypecheckerDecision(ArbilDataNode[] selectedNodes) {
	String titleString = "Override Type Checker Decision";
	String messageString = "The type checker does not recognise the selected file(s), which means that they\nare not an archivable type. This action will override that decision and allow you\nto add the file(s) to a session, as either media or written resources,\nhowever it might not be possible to import the result to the corpus server.";
	String[] optionStrings = {"WrittenResource", "MediaFile", "Cancel"};
	int userSelection = messageDialogHandler.showDialogBox(messageString, titleString, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, optionStrings, optionStrings[2]);
	if (optionStrings[userSelection].equals("WrittenResource") || optionStrings[userSelection].equals("MediaFile")) {
	    for (ArbilDataNode currentNode : selectedNodes) {
		if (currentNode.mpiMimeType == null) {
		    currentNode.mpiMimeType = "Manual/" + optionStrings[userSelection];
		    currentNode.typeCheckerMessage = "Manually overridden (might not be compatible with the archive)";
		    currentNode.clearIcon();
		}
	    }
	    return true;
	}
	return false;
    }
}
