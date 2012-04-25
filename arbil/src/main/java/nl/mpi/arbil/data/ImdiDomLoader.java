/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ImdiDomLoader implements MetadataDomLoader {

    private final ArbilDataNodeService dataNodeService;
    private final MessageDialogHandler messageDialogHandler;

    public ImdiDomLoader(ArbilDataNodeService dataNodeService, MessageDialogHandler messageDialogHandler) {
	this.dataNodeService = dataNodeService;
	this.messageDialogHandler = messageDialogHandler;
    }
    
    public void loadMetadataDom(ArbilDataNode dataNode) {
	if (dataNode.isLocal() && !dataNode.getFile().exists() && new File(dataNode.getFile().getAbsolutePath() + ".0").exists()) {
	    // if the file is missing then try to find a valid history file
	    dataNodeService.copyLastHistoryToCurrent(dataNode);
	    messageDialogHandler.addMessageDialogToQueue("Missing file has been recovered from the last history item.", "Recover History");
	}
	try {
	    //set the string name to unknown, it will be updated in the tostring function
	    dataNode.nodeText = "unknown";
	    initComponentLinkReader(dataNode);
	    updateMetadataChildNodes(dataNode);
	} catch (Exception mue) {
	    BugCatcherManager.getBugCatcher().logError(dataNode.getUrlString(), mue);
	    //            System.out.println("Invalid input URL: " + mue);
	    File nodeFile = dataNode.getFile();
	    if (nodeFile != null && nodeFile.exists()) {
		dataNode.nodeText = "Could not load data";
	    } else {
		dataNode.nodeText = "File not found";
		dataNode.fileNotFound = true;
	    }
	}
    }

    private void initComponentLinkReader(ArbilDataNode dataNode) {
	if (dataNode.isCmdiMetaDataNode()) {
	    // load the links from the cmdi file
	    // the links will be hooked to the relevent nodes when the rest of the xml is read
	    dataNode.cmdiComponentLinkReader = new CmdiComponentLinkReader();
	    dataNode.cmdiComponentLinkReader.readLinks(dataNode.getURI());
	} else {
	    dataNode.cmdiComponentLinkReader = null;
	}
    }

    private void updateMetadataChildNodes(ArbilDataNode dataNode) throws ParserConfigurationException, SAXException, IOException, TransformerException, ArbilMetadataException {
	Document nodDom = ArbilComponentBuilder.getDocument(dataNode.getURI());
	Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree = new Hashtable<ArbilDataNode, HashSet<ArbilDataNode>>();
	dataNode.childLinks = loadMetadataChildNodes(dataNode, nodDom, parentChildTree);
	checkRemovedChildNodes(parentChildTree);
    }

    private String[][] loadMetadataChildNodes(ArbilDataNode dataNode, Document nodDom, Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) throws TransformerException, ArbilMetadataException {
	Vector<String[]> childLinks = new Vector<String[]>();
	Hashtable<String, Integer> siblingNodePathCounter = new Hashtable<String, Integer>();
	// get the metadata format information required to read this nodes metadata
//        final String metadataStartPath = MetadataFormat.getMetadataStartPath(nodeUri.getPath());
	String fullNodePath = "";
	Node startNode = nodDom.getFirstChild();
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
	dataNode.getMetadataReader().iterateChildNodes(dataNode, childLinks, startNode, fullNodePath, fullNodePath, parentChildTree, siblingNodePathCounter, 0);
	if (dataNode.isCmdiMetaDataNode()) {
	    // Add all links that have no references to the root node (might confuse users but at least it will show what's going on)
	    dataNode.getMetadataReader().addUnreferencedResources(dataNode, parentChildTree, childLinks);
	}
	return childLinks.toArray(new String[][]{});
    }

    private void checkRemovedChildNodes(Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	//ImdiTreeObject[] childArrayTemp = new ImdiTreeObject[childLinks.length];
	for (Map.Entry<ArbilDataNode, HashSet<ArbilDataNode>> entry : parentChildTree.entrySet()) {
	    ArbilDataNode currentNode = entry.getKey();
	    // System.out.println("setting childArray on: " + currentNode.getUrlString());
	    // save the old child array
	    ArbilDataNode[] oldChildArray = currentNode.childArray;
	    // set the new child array
	    currentNode.childArray = parentChildTree.get(currentNode).toArray(new ArbilDataNode[]{});
	    // check the old child array and for each that is no longer in the child array make sure they are removed from any containers (tables or trees)
	    List currentChildList = Arrays.asList(currentNode.childArray);
	    for (ArbilDataNode currentOldChild : oldChildArray) {
		if (currentChildList.indexOf(currentOldChild) == -1) {
		    // remove from any containers that its found in
		    for (ArbilDataNodeContainer currentContainer : currentOldChild.getRegisteredContainers()) {
			currentContainer.dataNodeRemoved(currentOldChild);
		    }
		}
	    }
	}
    }
}
