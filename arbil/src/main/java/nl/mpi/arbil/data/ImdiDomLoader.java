/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilConstants;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ImdiDomLoader implements MetadataDomLoader {

    private final DataNodeLoader dataNodeLoader;
    private final MessageDialogHandler messageDialogHandler;

    public ImdiDomLoader(DataNodeLoader dataNodeLoader, MessageDialogHandler messageDialogHandler) {
	this.dataNodeLoader = dataNodeLoader;
	this.messageDialogHandler = messageDialogHandler;
    }

    public void loadMetadataDom(ArbilDataNode dataNode) {
	try {
	    //set the string name to unknown, it will be updated in the tostring function
	    dataNode.nodeText = "unknown";
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
	iterateChildNodes(dataNode, childLinks, startNode, fullNodePath, fullNodePath, parentChildTree, siblingNodePathCounter, 0);
	return childLinks.toArray(new String[][]{});
    }

    private void checkRemovedChildNodes(Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	for (Map.Entry<ArbilDataNode, HashSet<ArbilDataNode>> entry : parentChildTree.entrySet()) {
	    ArbilDataNode currentNode = entry.getKey();
	    // System.out.println("setting childArray on: " + currentNode.getUrlString());
	    // save the old child array
	    ArbilDataNode[] oldChildArray = currentNode.childArray;
	    // set the new child array
	    currentNode.childArray = entry.getValue().toArray(new ArbilDataNode[]{});
	    // check the old child array and for each that is no longer in the child array make sure they are removed from any containers (tables or trees)
	    List currentChildList = Arrays.asList(currentNode.childArray);
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

    private String getNamedAttributeValue(NamedNodeMap namedNodeMap, String attributeName) {
	Node nameNode = namedNodeMap.getNamedItem(attributeName);
	if (nameNode != null) {
	    return nameNode.getNodeValue();
	} else {
	    return null;
	}
    }

    private URI correctLinkPath(URI parentPath, String linkString) {
	URI linkURI = null;
	try {
	    if (!linkString.toLowerCase().startsWith("http:") && !linkString.toLowerCase().startsWith("file:")) {
		//                    linkPath = parentPath /*+ File.separatorChar*/ + fieldToAdd.getFieldValue();
		linkURI = parentPath.resolve(new URI(null, linkString, null));
	    } else if (linkString.toLowerCase().startsWith("&root;")) {
		// some imdi files contain "&root;" in its link paths
		linkURI = parentPath.resolve(new URI(null, linkString.substring(6), null));
	    } else {
		linkURI = parentPath.resolve(linkString);
	    }
	} catch (URISyntaxException exception) {
	    BugCatcherManager.getBugCatcher().logError(parentPath.toString() + " : " + linkString, exception);
	}
	//                    System.out.println("linkPath: " + linkPath);
	//                    linkPath = new URL(linkPath).getPath();
	// clean the path for the local file system
	//        linkURI = linkURI.replaceAll("/\\./", "/");
	//        linkURI = linkURI.substring(0, 6) + (linkURI.substring(6).replaceAll("[/]+/", "/"));
	//        while (linkURI.contains("/../")) {
	////                        System.out.println("linkPath: " + linkPath);
	//            linkURI = linkURI.replaceFirst("/[^/]+/\\.\\./", "/");
	//        }
	//                    System.out.println("linkPathCorrected: " + linkPath);
	if (linkURI != null) {
	    linkURI = ArbilDataNodeService.normaliseURI(linkURI);
	}
	//        System.out.println("linkURI: " + linkURI.toString());
	return linkURI;
    }

    private void showDomIdFoundMessage() {
	if (!dataNodeLoader.nodesNeedSave()) {
	    // Note TG: it may be good to add something like 'non-critical error' or something, so users feel safe to
	    // ignore this if they do not know what it means.
	    messageDialogHandler.addMessageDialogToQueue("A dom id attribute has been found in one or more files, these files will need to be saved to correct this.", "Load IMDI Files");
	}
    }

    /**
     * loop all nodes;
     * each end node becomes a field;
     * any node that passes pathIsChildNode becomes a subnode in a node named by the result string of pathIsChildNode;
     * the id of the node that passes pathIsChildNode is stored in the subnode to allow for deletion from the dom if needed
     *
     * @param parentNode
     * @param childLinks
     * @param startNode
     * @param nodePath
     * @param fullNodePath
     * @param parentChildTree
     * @param siblingNodePathCounter
     * @param nodeOrderCounter
     * @return
     */
    private int iterateChildNodes(ArbilDataNode parentNode, Vector<String[]> childLinks, Node startNode, final String nodePath, String fullNodePath,
	    Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree //, Hashtable<ImdiTreeObject, ImdiField[]> readFields
	    , Hashtable<String, Integer> siblingNodePathCounter, int nodeOrderCounter) {
	//        System.out.println("iterateChildNodes: " + nodePath);
	if (!parentChildTree.containsKey(parentNode)) {
	    parentChildTree.put(parentNode, new HashSet<ArbilDataNode>());
	}
	//        int nodeCounter = 0;
	// add the fields and nodes
	for (Node childNode = startNode; childNode != null; childNode = childNode.getNextSibling()) {
	    final String localName = childNode.getLocalName();
	    final NamedNodeMap childNodeAttributes = childNode.getAttributes();
	    if (localName != null) {
		final String childNodePath = new StringBuilder(3).append(nodePath).append(ArbilConstants.imdiPathSeparator).append(localName).toString();
		// todo: these filter strings should really be read from the metadata format
		if ((childNodePath).equals(".CMD.Header")) {
		    continue;
		}
		if ((childNodePath).equals(".CMD.Resources")) {
		    continue;
		}
		if ((childNodePath).equals(".Kinnate.Entity")) {
		    continue;
		}

		// get the xml node id
		if (childNodeAttributes != null) {
		    removeImdiNodeIds(childNodeAttributes, parentNode);
		}

		if (fullNodePath.length() == 0) {
		    getTemplate(childNode, parentNode, childNodeAttributes);
		}
		if (localName.equals("Corpus")) {
		    getImdiCatalogue(childNodeAttributes, parentNode, childLinks, parentChildTree);
		}

		final ArbilDataNode parentDomNode = parentNode.getParentDomNode();
		final ArbilTemplate parentNodeTemplate = parentDomNode.getNodeTemplate();

		final StringBuilder fullSubNodePath = new StringBuilder(fullNodePath).append(ArbilConstants.imdiPathSeparator).append(localName);
		final String parentNodePath = determineParentPath(parentNode);
		final String combinedPath = parentNodePath + childNodePath;
		final String childsMetaNode = parentNodeTemplate.pathIsChildNode(combinedPath);
		final int maxOccurs = parentNodeTemplate.getMaxOccursForTemplate(combinedPath);

		ArbilDataNode destinationNode;
		String siblingNodePath = childNodePath;
		if (localName != null && childsMetaNode != null) {
		    try {
			String pathUrlXpathSeparator = "";
			if (!parentNode.getUrlString().contains("#")) {
			    pathUrlXpathSeparator = "#";
			}
			StringBuilder siblingSpacer;
			// Build URI for metaNode or subNode
			final StringBuilder nodeURIStringBuilder = new StringBuilder(4).append(parentNode.getURI().toString()).append(pathUrlXpathSeparator).append(siblingNodePath);

			boolean isSingleton = maxOccurs == 1;
			ArbilDataNode metaNode = null;
			if (!isSingleton) {
			    metaNode = dataNodeLoader.getArbilDataNodeWithoutLoading(new URI(nodeURIStringBuilder.toString()));
			    metaNode.setNodeText(childsMetaNode); // + "(" + localName + ")" + metaNodeImdiTreeObject.getURI().getFragment());
			    if (!parentChildTree.containsKey(metaNode)) {
				parentChildTree.put(metaNode, new HashSet<ArbilDataNode>());
			    }
			    // Add metanode to tree
			    parentChildTree.get(parentNode).add(metaNode);
			    siblingSpacer = new StringBuilder(3).append("(").append(parentChildTree.get(metaNode).size() + 1).append(")");
			} else {
			    // add brackets to conform with the imdi api notation
			    siblingSpacer = new StringBuilder(3).append("(0)");
			}
			fullSubNodePath.append(siblingSpacer);
			// For subnode URI 
			nodeURIStringBuilder.append(siblingSpacer);
			ArbilDataNode subNode = dataNodeLoader.getArbilDataNodeWithoutLoading(new URI(nodeURIStringBuilder.toString()));

			if (!isSingleton) {
			    assert (metaNode != null);
			    // Add subnode to metanode
			    parentChildTree.get(metaNode).add(subNode);
			    metaNode.setContainerNode(true);
			} else {
			    // Add subnode directly to parent
			    parentChildTree.get(parentNode).add(subNode);
			    subNode.setSingletonMetadataNode(isSingleton);
			}
			//                parentNode.attachChildNode(metaNodeImdiTreeObject);
			//                metaNodeImdiTreeObject.attachChildNode(subNodeImdiTreeObject);
			if (!parentChildTree.containsKey(subNode)) {
			    parentChildTree.put(subNode, new HashSet<ArbilDataNode>());
			}
			destinationNode = subNode;
		    } catch (URISyntaxException ex) {
			destinationNode = parentNode;
			BugCatcherManager.getBugCatcher().logError(ex);
		    }
		    siblingNodePath = "";
		} else {
		    destinationNode = parentNode;
		}
		nodeOrderCounter = enterChildNodesRecursion(parentNode, childLinks, childNode, childNodeAttributes, destinationNode, localName,
			parentNodePath, siblingNodePath, fullSubNodePath.toString(), parentChildTree, siblingNodePathCounter, nodeOrderCounter);
	    }
	}
	return nodeOrderCounter;
    }

    /**
     * Updates counters and enters recursive iteration for child nodes.
     * Also adds referenced resources to the tree
     */
    private int enterChildNodesRecursion(ArbilDataNode parentNode, Vector<String[]> childLinks, Node childNode, NamedNodeMap childNodeAttributes,
	    ArbilDataNode destinationNode, String localName, String parentNodePath, String siblingNodePath, String fullSubNodePath,
	    Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, Hashtable<String, Integer> siblingNodePathCounter, int nodeOrderCounter) throws DOMException {
	NodeList childNodes = childNode.getChildNodes();
	boolean shouldAddCurrent = ((childNodes.getLength() == 0 && localName != null)
		|| (childNodes.getLength() == 1 && childNodes.item(0).getNodeType() == Node.TEXT_NODE));
	// calculate the xpath index for multiple fields like description
	if (!siblingNodePathCounter.containsKey(fullSubNodePath)) {
	    siblingNodePathCounter.put(fullSubNodePath, 0);
	} else {
	    siblingNodePathCounter.put(fullSubNodePath, siblingNodePathCounter.get(fullSubNodePath) + 1);
	}
	if (parentNode.getParentDomNode().getNodeTemplate().pathIsEditableField(parentNodePath + siblingNodePath)) {
	    // is a leaf not a branch
	    final String fieldValue = (childNodes.getLength() == 1) ? childNodes.item(0).getTextContent() : "";
	    nodeOrderCounter = addEditableField(nodeOrderCounter, destinationNode, siblingNodePath, fieldValue, siblingNodePathCounter, fullSubNodePath, parentNode, childLinks, parentChildTree, childNodeAttributes, shouldAddCurrent);
	}

	nodeOrderCounter = iterateChildNodes(destinationNode, childLinks, childNode.getFirstChild(), siblingNodePath, fullSubNodePath, parentChildTree, siblingNodePathCounter, nodeOrderCounter);

	return nodeOrderCounter;
    }

    private int countSiblings(Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, ArbilDataNode parentNode, String localName) {
	// todo: this might need to be revisited
	// this version of the metanode code is for cmdi nodes only and only when there can only be one node instance
	int siblingCount = 1;
	for (ArbilDataNode siblingNode : parentChildTree.get(parentNode)) {
	    String siblingPath = siblingNode.getURI().getFragment();
	    if (siblingPath != null) {
		siblingPath = siblingPath.substring(siblingPath.lastIndexOf(".") + 1);
		siblingPath = siblingPath.replaceAll("\\(\\d+\\)", "");
		if (localName.equals(siblingPath)) {
		    siblingCount++;
		}
	    }
	}
	return siblingCount;
    }

    private void removeImdiNodeIds(NamedNodeMap attributesMap, ArbilDataNode parentNode) {
	// look for node id attribites that should be removed from imdi files
	if (attributesMap.getNamedItem("id") != null) {
	    if (!parentNode.hasDomIdAttribute) {
		// only if this is an imdi file we will require the node to be saved which will remove the dom id attributes
		parentNode.hasDomIdAttribute = true;
		showDomIdFoundMessage();
		parentNode.setDataNodeNeedsSaveToDisk(null, false);
	    }
	} // end get the xml node id
    }

    private int addEditableField(int nodeOrderCounter, ArbilDataNode destinationNode, String siblingNodePath, String fieldValue, Hashtable<String, Integer> siblingNodePathCounter, String fullSubNodePath, ArbilDataNode parentNode, Vector<String[]> childLinks, Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, NamedNodeMap childNodeAttributes, boolean shouldAddCurrent) {

	// Handle special attributes

	String cvType = null;
	String cvUrlString = null;
	String keyName = null;
	String languageId = null;
	if (childNodeAttributes != null) {
	    cvType = getNamedAttributeValue(childNodeAttributes, "Type");
	    cvUrlString = getNamedAttributeValue(childNodeAttributes, "Link");
	    languageId = getNamedAttributeValue(childNodeAttributes, "LanguageId");
	    if (languageId == null) {
		languageId = getNamedAttributeValue(childNodeAttributes, "xml:lang");
	    }
	    keyName = getNamedAttributeValue(childNodeAttributes, "Name");
	}

	List<String[]> attributePaths = null;
	Map<String, Object> attributesValueMap = null;
	boolean allowsLanguageId = false;
	allowsLanguageId = languageId != null; //IMDI case where language id comes from template

	// is a leaf not a branch
	//            System.out.println("siblingNodePathCount: " + siblingNodePathCounter.get(siblingNodePath));
	ArbilField fieldToAdd = new ArbilField(nodeOrderCounter++, destinationNode, siblingNodePath, fieldValue, siblingNodePathCounter.get(fullSubNodePath), allowsLanguageId, attributePaths, attributesValueMap);
	// TODO: about to write this function
	//GuiHelper.imdiSchema.convertXmlPathToUiPath();
	// TODO: keep track of actual valid values here and only add to siblingCounter if siblings really exist
	// TODO: note that this method does not use any attributes without a node value
	//            if (childNode.getLocalName() != null) {
	//                nodeCounter++;
	//System.out.println("nodeCounter: " + nodeCounter + ":" + childNode.getLocalName());
	//            }
	if (childNodeAttributes != null) {
	    fieldToAdd.setFieldAttribute(cvType, cvUrlString, languageId, keyName);
	    if (fieldToAdd.xmlPath.endsWith("Description")) {
		if (cvUrlString != null && cvUrlString.length() > 0) {
		    // TODO: this field sould be put in the link node not the parent node
		    URI correcteLink = correctLinkPath(parentNode.getURI(), cvUrlString);
		    childLinks.add(new String[]{correcteLink.toString(), "Info Link"});
		    ArbilDataNode descriptionLinkNode = dataNodeLoader.getArbilDataNodeWithoutLoading(correcteLink);
		    descriptionLinkNode.isInfoLink = true;
		    descriptionLinkNode.setDataLoaded(true);
		    parentChildTree.get(parentNode).add(descriptionLinkNode);
		    descriptionLinkNode.addField(fieldToAdd);
		}
	    }
	}

	if (shouldAddCurrent && fieldToAdd.isDisplayable()) {
	    //                System.out.println("Adding: " + fieldToAdd);
	    //                debugOut("nextChild: " + fieldToAdd.xmlPath + siblingSpacer + " : " + fieldToAdd.fieldValue);
	    //                fieldToAdd.translateFieldName(siblingNodePath);
	    destinationNode.addField(fieldToAdd);
	} else if (shouldAddCurrent && fieldToAdd.xmlPath.contains("CorpusLink") && fieldValue.length() > 0) {
	    //                System.out.println("LinkValue: " + fieldValue);
	    //                System.out.println("ParentPath: " + parentPath);
	    //                System.out.println("Parent: " + this.getUrlString());
	    try {
		URI linkPath = correctLinkPath(parentNode.getURI(), fieldToAdd.getFieldValue());
		childLinks.add(new String[]{linkPath.toString(), "IMDI Link"});
		ArbilDataNode linkedNode = dataNodeLoader.getArbilDataNodeWithoutLoading(linkPath);
		linkedNode.setNodeText(fieldToAdd.getKeyName());
		parentChildTree.get(parentNode).add(linkedNode);
	    } catch (Exception ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
		System.out.println("Exception CorpusLink: " + ex.getMessage());
	    }
	} //                // the corpus link nodes are used but via the api.getlinks so dont log them here
	//                NamedNodeMap namedNodeMap = childNode.getParentNode().getAttributes();
	//            if (debugOn && !fieldToAdd.xmlPath.contains("CorpusLink")) {
	//                // the corpus link nodes are used but via the api.getlinks so dont log them here
	//                NamedNodeMap namedNodeMap = childNode.getParentNode().getAttributes();
	//                if (namedNodeMap != null) {
	//                    for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
	//                        String attributeName = fieldToAdd.xmlPath + ":" + namedNodeMap.item(attributeCounter).getNodeName();
	//                        // add all attributes even if they contain no value
	//                        // TODO: check if this should be removed yet
	//                        if (!listDiscardedOfAttributes.contains(attributeName) && !attributeName.endsWith(":id")) {
	//                            // also ignore any id attributes that would have been attached to blank fields
	//                            listDiscardedOfAttributes.add(attributeName);
	//                        }
	//                    }
	//                }
	//            }
	fieldToAdd.finishLoading();
	return nodeOrderCounter;
    }

    private String determineParentPath(ArbilDataNode parentNode) {
	String parentNodePath = parentNode.getURI().getFragment();
	if (parentNodePath == null) {
	    // pathIsChildNode needs to have the entire path of the node not just the local part
	    parentNodePath = "";
	} else {
	    parentNodePath = parentNodePath.replaceAll("\\(\\d+\\)", "");
	}
	return parentNodePath;
    }

    private void getTemplate(Node childNode, ArbilDataNode parentNode, NamedNodeMap attributesMap) throws DOMException {
	// this is an imdi file so get an imdi template etc
	if (attributesMap != null) {
	    // these attributes exist only in the metatranscript node
	    Node archiveHandleAtt = attributesMap.getNamedItem("ArchiveHandle");
	    if (archiveHandleAtt != null) {
		parentNode.archiveHandle = archiveHandleAtt.getNodeValue();
	    } else {
		parentNode.archiveHandle = null;
	    }
	    Node templateOriginatorAtt = attributesMap.getNamedItem("Originator");
	    if (templateOriginatorAtt != null) {
		String templateOriginator = templateOriginatorAtt.getNodeValue();
		int separatorIndex = templateOriginator.indexOf(":");
		if (separatorIndex > -1) {
		    parentNode.nodeTemplate = ArbilTemplateManager.getSingleInstance().getTemplate(templateOriginator.substring(separatorIndex + 1));
		} else {
		    // TODO: this is redundant but is here for backwards compatability
		    Node templateTypeAtt = attributesMap.getNamedItem("Type");
		    if (templateTypeAtt != null) {
			String templateType = templateTypeAtt.getNodeValue();
			// most of the time this will return the default template, but if the named template exixts it will be used
			parentNode.nodeTemplate = ArbilTemplateManager.getSingleInstance().getTemplate(templateType);
		    }
		}
	    }
	}
    }

    private void getImdiCatalogue(NamedNodeMap attributesMap, ArbilDataNode parentNode, Vector<String[]> childLinks, Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) throws DOMException {
	// get the imdi catalogue if it exists
	Node catalogueLinkAtt = attributesMap.getNamedItem("CatalogueLink");
	if (catalogueLinkAtt != null) {
	    String catalogueLink = catalogueLinkAtt.getNodeValue();
	    if (catalogueLink.length() > 0) {
		URI correcteLink = correctLinkPath(parentNode.getURI(), catalogueLink);
		childLinks.add(new String[]{correcteLink.toString(), "CatalogueLink"});
		parentChildTree.get(parentNode).add(dataNodeLoader.getArbilDataNodeWithoutLoading(correcteLink));
	    }
	}
    }
}
