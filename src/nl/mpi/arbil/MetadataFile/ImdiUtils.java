package nl.mpi.arbil.MetadataFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.transform.TransformerException;
import mpi.imdi.api.IMDIDom;
import mpi.imdi.api.IMDILink;
import mpi.imdi.api.WSNodeType;
import mpi.util.OurURL;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgWindowManager;
import org.w3c.dom.Document;

/**
 *  Document   : ImdiUtils
 *  Created on : May 21, 2010, 9:30:03 PM
 *  Author     : Peter Withers
 */
public class ImdiUtils implements MetadataUtils {

    public static IMDIDom api = new IMDIDom();

    private boolean isCatalogue(URI sourceURI) {
        try {
            mpi.util.OurURL inUrlLocal = new mpi.util.OurURL(sourceURI.toURL());
            org.w3c.dom.Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
            return null != org.apache.xpath.XPathAPI.selectSingleNode(nodDom, "/:METATRANSCRIPT/:Catalogue");
        } catch (MalformedURLException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (TransformerException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
        return false;
    }

    private boolean isSession(URI sourceURI) {
        try {
            mpi.util.OurURL inUrlLocal = new mpi.util.OurURL(sourceURI.toURL());
            org.w3c.dom.Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
            return null != org.apache.xpath.XPathAPI.selectSingleNode(nodDom, "/:METATRANSCRIPT/:Session");
        } catch (MalformedURLException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (TransformerException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
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
//                GuiHelper.linorgBugCatcher.logError(new Exception(api.getMessage()));
//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Add Link");
//                return false;
//            } else {
//                api.writeDOM(nodDom, new File(nodeURI), false);
//                return true;
//            }
//        } catch (MalformedURLException ex) {
//            GuiHelper.linorgBugCatcher.logError(ex);
//        }
//        return true;
//    }

    public boolean addCorpusLink(URI nodeURI, URI linkURI[]) {
        try {
            Document nodDom;
            OurURL inUrlLocal = new OurURL(nodeURI.toURL());
            nodDom = api.loadIMDIDocument(inUrlLocal, false);
            if (nodDom == null) {
                GuiHelper.linorgBugCatcher.logError(new Exception(api.getMessage()));
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Add Link");
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
                    api.createIMDILink(nodDom, inUrlLocal, currentLinkUri.toString(), "", nodeType, "");
                }
                api.writeDOM(nodDom, new File(nodeURI), true);
                return true;
            }
        } catch (MalformedURLException ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return true;
    }

    public boolean copyMetadataFile(URI sourceURI, File destinationFile, URI[][] linksToUpdate, boolean updateLinks) {
        try {
            mpi.util.OurURL inUrlLocal = new mpi.util.OurURL(sourceURI.toURL());
            mpi.util.OurURL destinationUrl = new mpi.util.OurURL(destinationFile.toURI().toURL());

            org.w3c.dom.Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
            if (nodDom == null) {
                GuiHelper.linorgBugCatcher.logError(new Exception(api.getMessage()));
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Copy IMDI File");
                return false;
            } else {
                mpi.imdi.api.IMDILink[] links = api.getIMDILinks(nodDom, inUrlLocal, mpi.imdi.api.WSNodeType.UNKNOWN);
                if (links != null && updateLinks) {
                    for (mpi.imdi.api.IMDILink currentLink : links) {
                        URI linkUriToUpdate = null;
                        if (linksToUpdate != null) {
                            for (URI[] updatableLink : linksToUpdate) {
                                try {
                                    if (currentLink.getRawURL().toURL().toURI().equals(updatableLink[0])) {
                                        linkUriToUpdate = updatableLink[1];
                                        break;
                                    }
                                } catch (URISyntaxException exception) {
                                    GuiHelper.linorgBugCatcher.logError(exception);
                                }
                            }
                        }
                        System.out.println("currentLink: " + linkUriToUpdate + " : " + currentLink.getRawURL().toString());
                        if (linkUriToUpdate != null) {
                            // todo: this is not going to always work because the changeIMDILink is too limited, when a link points to a different domain for example
                            // todo: cont... or when a remote imdi is imported without its files then exported while copying its files, the files will be copied but the links not updated by the api
                            // todo: cont... this must instead take oldurl newurl and the new imdi file location
//                            api.changeIMDILink(nodDom, destinationUrl, currentLink);
                            // todo: check how removeIMDILink and createIMDILink handles info links compared to changeIMDILink
                            api.removeIMDILink(nodDom, currentLink);
                            api.createIMDILink(nodDom, destinationUrl, linkUriToUpdate.toString(), currentLink.getLinkName(), currentLink.getNodeType(), currentLink.getSpec());
                        }
                    }
                }
                boolean removeIdAttributes = true;
                api.writeDOM(nodDom, destinationFile, removeIdAttributes);
                return true;
            }
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
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
            if (nodDom == null) {
                GuiHelper.linorgBugCatcher.logError(new Exception(api.getMessage()));
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Remove IMDI Links");
                return false;
            }
            IMDILink[] allImdiLinks;
            allImdiLinks = api.getIMDILinks(nodDom, destinationUrl, WSNodeType.UNKNOWN);
            if (allImdiLinks != null) {
                for (IMDILink currentLink : allImdiLinks) {
                    for (URI currentUri : linkURI) {
                        try {
                            if (currentUri.equals(currentLink.getRawURL().toURL().toURI())) {
                                api.removeIMDILink(nodDom, currentLink);
                            }
                        } catch (URISyntaxException exception) {
                            GuiHelper.linorgBugCatcher.logError(exception);
                        }
                    }
                }
                boolean removeIdAttributes = true;
                api.writeDOM(nodDom, new File(nodeURI), removeIdAttributes);
                return true;
            }

        } catch (MalformedURLException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading links via the IMDI API", "Get Links");
        }
        return false;
    }

    public URI[] getCorpusLinks(URI nodeURI) {
        try {
            OurURL destinationUrl = new OurURL(nodeURI.toString());
            Document nodDom = api.loadIMDIDocument(destinationUrl, false);
            IMDILink[] allImdiLinks;
            allImdiLinks = api.getIMDILinks(nodDom, destinationUrl, WSNodeType.UNKNOWN);
            if (allImdiLinks != null) {
                URI[] returnUriArray = new URI[allImdiLinks.length];
                for (int linkCount = 0; linkCount
                        < allImdiLinks.length; linkCount++) {
                    try {
                        returnUriArray[linkCount] = allImdiLinks[linkCount].getRawURL().toURL().toURI();
                    } catch (URISyntaxException exception) {
                        GuiHelper.linorgBugCatcher.logError(exception);
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading one of the links via the IMDI API", "Get Links");
                    }
                }
                return returnUriArray;
            }

        } catch (MalformedURLException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading links via the IMDI API", "Get Links");
        }
        return null;
    }
}
