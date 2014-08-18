/*
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.HandleUtils;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.util.BugCatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @since Jul 15, 2014 11:18:58 AM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class BlockingDataNodeService extends AbstractDataNodeService implements DataNodeService {

    private final static Logger logger = LoggerFactory.getLogger(BlockingDataNodeService.class);
    private final MetadataFormat metadataFormat = new MetadataFormat();
    private final MetadataReader metadataReader = MetadataReader.getSingleInstance();
    // todo: the ResourceBundle widgets is not relevant to this usage and it would be better to throw from this class
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    public boolean addCorpusLink(ArbilDataNode dataNode, ArbilDataNode targetNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void addField(ArbilDataNode dataNode, ArbilField fieldToAdd) {
        //        System.addField:out.println("addField: " + this.getUrlString() + " : " + fieldToAdd.xmlPath + " : " + fieldToAdd.getFieldValue());
        ArbilField[] currentFieldsArray = dataNode.getFieldArray(fieldToAdd.getTranslateFieldName());
        if (currentFieldsArray == null) {
            currentFieldsArray = new ArbilField[]{fieldToAdd};
        } else {
            //            logger.debug("appendingField: " + fieldToAdd);
            ArbilField[] appendedFieldsArray = new ArbilField[currentFieldsArray.length + 1];
            System.arraycopy(currentFieldsArray, 0, appendedFieldsArray, 0, currentFieldsArray.length);
            appendedFieldsArray[appendedFieldsArray.length - 1] = fieldToAdd;
            currentFieldsArray = appendedFieldsArray;

            //            for (ImdiField tempField : currentFieldsArray) {
            //                logger.debug("appended fields: " + tempField);
            //            }
        }
        dataNode.addFieldArray(fieldToAdd.getTranslateFieldName(), currentFieldsArray);

        if (fieldToAdd.xmlPath.endsWith(".ResourceLink") && fieldToAdd.getParentDataNode().isChildNode()/* && fieldToAdd.parentImdi.getUrlString().contains("MediaFile") */) {
            dataNode.resourceUrlField = fieldToAdd;
        }
    }

    public void bumpHistory(File dataNodeFile) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void copyLastHistoryToCurrent(ArbilDataNode dataNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void deleteCorpusLink(ArbilDataNode dataNode, ArbilDataNode[] targetImdiNodes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ArbilDataNode getParentOfNode(ArbilDataNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isEditable(ArbilDataNode dataNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isFavorite(ArbilDataNode dataNode) {
        return false;
    }

    public ArbilDataNode loadArbilDataNode(Object registeringObject, URI localUri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void loadArbilDom(ArbilDataNode dataNode) {
        if (dataNode.getParentDomNode() != dataNode) {
            dataNode.getParentDomNode().loadArbilDom();
        } else {
            // we reduce the times the file type is checked by only checking when the type is unset, this is because for difficult files a deep check is required which requires downloading a small portion of the file
            if (dataNode.getFormatType() == MetadataFormat.FileType.UNKNOWN) {
                dataNode.setFormatType(metadataFormat.deepCheck(dataNode.getUri()));
            }
            synchronized (dataNode.getParentDomLockObject()) {
                dataNode.initNodeVariables(); // this might be run too often here but it must be done in the loading thread and it also must be done when the object is created
                if (!dataNode.isMetaDataNode() && !dataNode.isDirectory() && dataNode.isLocal()) {
                    // if it is an not imdi or a loose file but not a direcotry then get the md5sum
                    dataNode.setLoadingState(ArbilDataNode.LoadingState.LOADED);
                }
                if (dataNode.isDirectory()) {
//		    getDirectoryLinks(dataNode);
//		    dataNode.setLoadingState(ArbilDataNode.LoadingState.LOADED);
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                if (dataNode.isMetaDataNode()) {
                    try {
                        //set the string name to unknown, it will be updated in the tostring function
                        dataNode.nodeText = "unknown";
                        initComponentLinkReader(dataNode);
                        updateMetadataChildNodes(dataNode);
                    } catch (Exception mue) {
                        logger.info(mue.getMessage());
//                        BugCatcherManager.getBugCatcher().logError(dataNode.getUrlString(), mue);
                        //            logger.debug("Invalid input URL: " + mue);
                        File nodeFile = dataNode.getFile();
                        if (nodeFile != null && nodeFile.exists()) {
                            dataNode.nodeText = widgets.getString("COULD NOT LOAD DATA");
                        } else {
                            dataNode.nodeText = widgets.getString("FILE NOT FOUND");
                            dataNode.fileNotFound = true;
                        }
                    }

                    ArbilDataNode.LoadingState requestedLoadingState = dataNode.getRequestedLoadingState();
                    if (requestedLoadingState == null) {
                        requestedLoadingState = ArbilDataNode.LoadingState.LOADED;
                    }

                    dataNode.setLoadingState(requestedLoadingState);
                } else if (!dataNode.isLocal()) {
                    //todo: move me and or make me optional
                    try {
                        final URLConnection uRLConnection = dataNode.getUri().toURL().openConnection();
                        if (uRLConnection instanceof HttpURLConnection) {
                            // For HTTP connections, we want to follow redirects
                            ((HttpURLConnection) uRLConnection).setInstanceFollowRedirects(true);
                        }
                        // add http headers as data node fields
                        final Map<String, List<String>> headerFields = uRLConnection.getHeaderFields();
                        for (String fieldName : headerFields.keySet()) {
                            final List<String> values = headerFields.get(fieldName);
                            for (String value : values) {
                                final String fieldNameNonNull = (fieldName != null) ? fieldName : "";
                                final String valueNonNull = (value != null) ? value : "";
                                dataNode.addField(new ArbilField(0, dataNode, fieldNameNonNull, valueNonNull, 0, false));
                            }
                        }
                        dataNode.setLoadingState(ArbilDataNode.LoadingState.LOADED);
                    } catch (IOException exception) {
                        logger.debug("location header check on URL {} failed", exception.getMessage());
//                    } catch (MalformedURLException exception) {
//                        logger.debug("location header check on URL {} failed", exception.getMessage());
                    }
                }
            }
        }
    }

    // todo: these innner methods are sared with thte other datanode services and really should be moved to a utility class
    private void initComponentLinkReader(ArbilDataNode dataNode) {
        if (dataNode.isCmdiMetaDataNode()) {
            final URI matadataUri = new HandleUtils().resolveHandle(dataNode.getUri());
            // load the links from the cmdi file
            // the links will be hooked to the relevent nodes when the rest of the xml is read
            dataNode.getCmdiComponentLinkReader().readLinks(matadataUri);
        }
    }

    private void updateMetadataChildNodes(ArbilDataNode dataNode) throws ParserConfigurationException, SAXException, IOException, TransformerException, ArbilMetadataException {
        final URI matadataUri = new HandleUtils().resolveHandle(dataNode.getUri());
        Document nodDom = ArbilComponentBuilder.getDocument(matadataUri);
        Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree = new HashMap<ArbilDataNode, Set<ArbilDataNode>>();
        dataNode.childLinks = loadMetadataChildNodes(dataNode, nodDom, parentChildTree);
        checkRemovedChildNodes(parentChildTree);
    }

    private List<String[]> loadMetadataChildNodes(ArbilDataNode dataNode, Document nodDom, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) throws TransformerException, ArbilMetadataException {
        final List<String[]> childLinks = new ArrayList<String[]>();
        final Map<String, Integer> siblingNodePathCounter = new HashMap<String, Integer>();
        // get the metadata format information required to read this nodes metadata
//        final String metadataStartPath = MetadataFormat.getMetadataStartPath(nodeUri.getPath());
        final String fullNodePath = "";
        final Node startNode = nodDom.getFirstChild();
//	if (metadataStartPath.length() > 0) {
//	    fullNodePath = metadataStartPath.substring(0, metadataStartPath.lastIndexOf("."));
//	    final String metadataXpath = metadataStartPath.replaceAll("\\.", "/:"); //"/:Kinnate/:Entity";
//	    final Node metadataNode = org.apache.xpath.XPathAPI.selectSingleNode(startNode, metadataXpath);
        // if this fails then we probably want to fail the reading of the node
//	    if (metadataNode == null) {
//		throw new ArbilMetadataException("Failed to find the start node for the metadata to read: " + fullNodePath);
//	    }
//	    startNode = metadataNode;
//	}
        // load the fields from the imdi file
        final boolean shallowLoading = ArbilDataNode.LoadingState.PARTIAL.equals(dataNode.getRequestedLoadingState());
        metadataReader.iterateChildNodes(dataNode, childLinks, startNode, fullNodePath, fullNodePath, parentChildTree, siblingNodePathCounter, 0, shallowLoading);
        if (dataNode.isCmdiMetaDataNode()) {
            // Add all links that have no references to the root node (might confuse users but at least it will show what's going on)
            metadataReader.addUnreferencedResources(dataNode, parentChildTree, childLinks);
        }
        return childLinks;
    }
    // endtodo: these innner methods are sared with thte other datanode services and really should be moved to a utility class

    public void loadFullArbilDom(ArbilDataNode dataNode) {
        dataNode.setLoadingState(ArbilDataNode.LoadingState.UNLOADED);
        dataNode.setRequestedLoadingState(ArbilDataNode.LoadingState.LOADED);
        dataNode.updateLoadingState(+1);
        try {
            dataNode.loadArbilDom();
        } finally {
            dataNode.updateLoadingState(-1);
        }
    }

    public void reloadNode(ArbilDataNode dataNode) {
        dataNode.loadArbilDom();
        dataNode.updateLoadingState(-1);
        dataNode.clearIcon();
        dataNode.clearChildIcons();
    }

    public void reloadNodeShallowly(ArbilDataNode dataNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean resurrectHistory(ArbilDataNode dataNode, String historyVersion) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void saveChangesToCache(ArbilDataNode datanode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDataNodeNeedsSaveToDisk(ArbilDataNode dataNode, ArbilField originatingField, boolean updateUI) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String urlEncodePath(String inputPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void checkRemovedChildNodes(Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) {
        for (Map.Entry<ArbilDataNode, Set<ArbilDataNode>> entry : parentChildTree.entrySet()) {
            ArbilDataNode currentNode = entry.getKey();
            // logger.debug("setting childArray on: " + currentNode.getUrlString());
            // save the old child array
            ArbilDataNode[] oldChildArray = currentNode.childArray;
            // set the new child array
            final Set<ArbilDataNode> newChildren = entry.getValue();
            currentNode.childArray = newChildren.toArray(new ArbilDataNode[newChildren.size()]);
            // check the old child array and for each that is no longer in the child array make sure they are removed from any containers (tables or trees)
            final List currentChildList = Arrays.asList(currentNode.childArray);
            for (ArbilDataNode currentOldChild : oldChildArray) {
                if (!currentChildList.contains(currentOldChild)) {
                    // remove from any containers that its found in
                    for (ArbilDataNodeContainer currentContainer : currentOldChild.getRegisteredContainers()) {
                        currentContainer.dataNodeRemoved(currentOldChild);
                    }
                }
            }
        }
    }
}
