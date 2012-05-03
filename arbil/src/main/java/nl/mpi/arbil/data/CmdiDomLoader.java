/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilConstants;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.MetadataContainer;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.MetadataField;
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
public class CmdiDomLoader implements MetadataDomLoader {

    private final DataNodeLoader dataNodeLoader;
    private final MessageDialogHandler messageDialogHandler;
    private final MetadataAPI metadataAPI;

    public CmdiDomLoader(DataNodeLoader dataNodeLoader, MessageDialogHandler messageDialogHandler, MetadataAPI metadataAPI) {
	this.dataNodeLoader = dataNodeLoader;
	this.messageDialogHandler = messageDialogHandler;
	this.metadataAPI = metadataAPI;
    }

    public void loadMetadataDom(ArbilDataNode dataNode) {
	try {
	    //set the string name to unknown, it will be updated in the tostring function
	    dataNode.nodeText = "unknown";
	    if (!dataNode.isChildNode()) {
		dataNode.setMetadataElement(metadataAPI.getMetadataDocument(dataNode.getURI().toURL()));
	    } else if (dataNode.getMetadataElement() == null) {
		throw new AssertionError("Child without element encountered");
	    }
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
	HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree = new HashMap<ArbilDataNode, HashSet<ArbilDataNode>>();
	loadMetadataChildNodes(dataNode, parentChildTree);
	checkRemovedChildNodes(parentChildTree);
    }

    private void loadMetadataChildNodes(ArbilDataNode parentNode, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	if (!parentChildTree.containsKey(parentNode)) {
	    parentChildTree.put(parentNode, new HashSet<ArbilDataNode>());
	}
	final MetadataElement metadataElement = parentNode.getMetadataElement();
	if (metadataElement instanceof MetadataContainer) {
	    final MetadataContainer<MetadataElement> container = (MetadataContainer) metadataElement;
	    iterateChildNodes(container, parentNode, parentChildTree);
	}
    }

    private void iterateChildNodes(final MetadataContainer<MetadataElement> container, ArbilDataNode parentNode, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	int fieldOrder = 0;
	for (MetadataElement child : container.getChildren()) {
	    if (child instanceof MetadataContainer) {
		final StringBuilder nodeURIStringBuilder = new StringBuilder(4).append(parentNode.getURI().toString());
		if (!parentNode.getUrlString().contains("#")) {
		    nodeURIStringBuilder.append("#");
		}
		nodeURIStringBuilder.append(ArbilConstants.imdiPathSeparator);
		nodeURIStringBuilder.append(child.getType().getName());
		nodeURIStringBuilder.append("(0)"); // TODO: make functional
		try {
		    ArbilDataNode subNode = dataNodeLoader.getArbilDataNodeWithoutLoading(new URI(nodeURIStringBuilder.toString()));
		    subNode.setMetadataElement(child);
		    parentChildTree.get(parentNode).add(subNode);
		    loadMetadataChildNodes(subNode, parentChildTree);
		} catch (URISyntaxException usEx) {
		    BugCatcherManager.getBugCatcher().logError("URISyntaxException while loading child nodes", usEx);
		}
	    } else if (child instanceof MetadataField) {
		final MetadataField metadataField = (MetadataField) child;
		ArbilField field = new ArbilField(fieldOrder++, parentNode, metadataField.getType().getPathString().replaceAll("/:", "."), metadataField.getValue().toString(), 0, false);
		field.setMetadataField(metadataField);
		parentNode.addField(field);
	    }
	}
    }

    private String[][] loadMetadataChildNodes(ArbilDataNode dataNode, Document nodDom, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) throws TransformerException, ArbilMetadataException {
	ArrayList<String[]> childLinks = new ArrayList<String[]>();
	HashMap<String, Integer> siblingNodePathCounter = new HashMap<String, Integer>();
	String fullNodePath = "";
	Node startNode = nodDom.getFirstChild();
	// load the fields from the imdi file
	iterateChildNodes(dataNode, childLinks, startNode, fullNodePath, fullNodePath, parentChildTree, siblingNodePathCounter, 0);
	// Add all links that have no references to the root node (might confuse users but at least it will show what's going on)
	addUnreferencedResources(dataNode, parentChildTree, childLinks);
	return childLinks.toArray(new String[][]{});
    }

    private void checkRemovedChildNodes(HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	for (Map.Entry<ArbilDataNode, HashSet<ArbilDataNode>> entry : parentChildTree.entrySet()) {
	    ArbilDataNode currentNode = entry.getKey();
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
	if (linkURI != null) {
	    linkURI = ArbilDataNodeService.normaliseURI(linkURI);
	}
	//        System.out.println("linkURI: " + linkURI.toString());
	return linkURI;
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
    private int iterateChildNodes(ArbilDataNode parentNode, ArrayList<String[]> childLinks, Node startNode, final String nodePath, String fullNodePath,
	    HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, HashMap<String, Integer> siblingNodePathCounter, int nodeOrderCounter) {
	if (!parentChildTree.containsKey(parentNode)) {
	    parentChildTree.put(parentNode, new HashSet<ArbilDataNode>());
	}
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

		if (fullNodePath.length() == 0) {
		    getTemplate(childNode, parentNode, childNodeAttributes);
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
			ArbilDataNode metaNode = null;
			String pathUrlXpathSeparator = "";
			if (!parentNode.getUrlString().contains("#")) {
			    pathUrlXpathSeparator = "#";
			}
			StringBuilder siblingSpacer;
			boolean isSingleton = false;
			// Build URI for metaNode or subNode
			final StringBuilder nodeURIStringBuilder = new StringBuilder(4).append(parentNode.getURI().toString()).append(pathUrlXpathSeparator).append(siblingNodePath);
			if (maxOccurs > 1 || maxOccurs == -1) {
			    isSingleton = maxOccurs == 1;
			    metaNode = dataNodeLoader.getArbilDataNodeWithoutLoading(new URI(nodeURIStringBuilder.toString()));
			    metaNode.setNodeText(childsMetaNode); // + "(" + localName + ")" + metaNodeImdiTreeObject.getURI().getFragment());
			    if (!parentChildTree.containsKey(metaNode)) {
				parentChildTree.put(metaNode, new HashSet<ArbilDataNode>());
			    }
			    if (!isSingleton) {
				// Add metanode to tree
				parentChildTree.get(parentNode).add(metaNode);
			    }
			    // add brackets to conform with the imdi api notation
			    siblingSpacer = new StringBuilder(3).append("(").append(parentChildTree.get(metaNode).size() + 1).append(")");
			} else {
			    int siblingCount = countSiblings(parentChildTree, parentNode, localName);
			    siblingSpacer = new StringBuilder(3).append("(").append(siblingCount).append(")");
//                            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(localName + " : " + childsMetaNode + " : " + maxOccurs, "filtered metanode");
			}
			fullSubNodePath.append(siblingSpacer);
			// For subnode URI 
			nodeURIStringBuilder.append(siblingSpacer);
			ArbilDataNode subNode = dataNodeLoader.getArbilDataNodeWithoutLoading(new URI(nodeURIStringBuilder.toString()));

			if (metaNode != null && !isSingleton) {
			    // Add subnode to metanode
			    parentChildTree.get(metaNode).add(subNode);
			    metaNode.setContainerNode(true);
			} else {
			    // Add subnode directly to parent
			    parentChildTree.get(parentNode).add(subNode);
			    subNode.setSingletonMetadataNode(isSingleton);
			}
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
    private int enterChildNodesRecursion(ArbilDataNode parentNode, ArrayList<String[]> childLinks, Node childNode, NamedNodeMap childNodeAttributes,
	    ArbilDataNode destinationNode, String localName, String parentNodePath, String siblingNodePath, String fullSubNodePath,
	    HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, HashMap<String, Integer> siblingNodePathCounter, int nodeOrderCounter) throws DOMException {
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
	} else {
	    // for a branch, check if there are referenced resources to add
	    addReferencedResources(parentNode, parentChildTree, childNodeAttributes, childLinks, destinationNode);
	    // and add all editable component attributes as field
	    if (childNodeAttributes != null) {
		for (int i = 0; i < childNodeAttributes.getLength(); i++) {
		    Node attrNode = childNodeAttributes.item(i);
		    String attrName = CmdiTemplate.getAttributePathSection(attrNode.getNamespaceURI(), attrNode.getLocalName());
		    String attrPath = siblingNodePath + ".@" + attrName;
		    String fullAttrPath = fullSubNodePath + ".@" + attrName;
		    if (!siblingNodePathCounter.containsKey(fullAttrPath)) {
			siblingNodePathCounter.put(fullAttrPath, 0);
		    }
		    if (parentNode.getNodeTemplate().pathIsEditableField(fullAttrPath.replaceAll("\\(\\d*?\\)", ""))) {
			nodeOrderCounter = addEditableField(nodeOrderCounter,
				destinationNode,
				attrPath, //siblingNodePath,
				attrNode.getNodeValue(),
				siblingNodePathCounter,
				fullAttrPath, //fullSubNodePath,
				parentNode,
				childLinks,
				parentChildTree,
				null, // don't pass childNodeAttributes as they're the parent's attributes 
				true);
		    }
		}
	    }
	}

	nodeOrderCounter = iterateChildNodes(destinationNode, childLinks, childNode.getFirstChild(), siblingNodePath, fullSubNodePath, parentChildTree, siblingNodePathCounter, nodeOrderCounter);

	return nodeOrderCounter;
    }

    private int countSiblings(HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, ArbilDataNode parentNode, String localName) {
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

    private int addEditableField(int nodeOrderCounter, ArbilDataNode destinationNode, String siblingNodePath, String fieldValue, HashMap<String, Integer> siblingNodePathCounter, String fullSubNodePath, ArbilDataNode parentNode, ArrayList<String[]> childLinks, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, NamedNodeMap childNodeAttributes, boolean shouldAddCurrent) {

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
	// For CMDI nodes, get field attribute paths from schema and values from document before creating arbil field
	final String nodePath = fullSubNodePath.replaceAll("\\(\\d+\\)", "");
	ArbilTemplate template = destinationNode.getNodeTemplate();
	attributePaths = template.getEditableAttributesForPath(nodePath);
	attributesValueMap = new HashMap<String, Object>();
	if (childNodeAttributes != null) {
	    for (int i = 0; i < childNodeAttributes.getLength(); i++) {
		final Node attrNode = childNodeAttributes.item(i);
		final String path = nodePath + ".@" + CmdiTemplate.getAttributePathSection(attrNode.getNamespaceURI(), attrNode.getLocalName());
		attributesValueMap.put(path, attrNode.getNodeValue());
	    }
	}
	allowsLanguageId = template.pathAllowsLanguageId(nodePath);

	// is a leaf not a branch
	ArbilField fieldToAdd = new ArbilField(nodeOrderCounter++, destinationNode, siblingNodePath, fieldValue, siblingNodePathCounter.get(fullSubNodePath), allowsLanguageId, attributePaths, attributesValueMap);

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
	    addReferencedResources(parentNode, parentChildTree, childNodeAttributes, childLinks, destinationNode);
	}

	if (shouldAddCurrent && fieldToAdd.isDisplayable()) {
	    destinationNode.addField(fieldToAdd);
	} else if (shouldAddCurrent && fieldToAdd.xmlPath.contains("CorpusLink") && fieldValue.length() > 0) {
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
	}
	fieldToAdd.finishLoading();
	return nodeOrderCounter;
    }

    private void addReferencedResources(ArbilDataNode parentNode, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, NamedNodeMap childNodeAttributes, ArrayList<String[]> childLinks, ArbilDataNode destinationNode) {
//	String clarinRefIds = getNamedAttributeValue(childNodeAttributes, "ref");
//	if (clarinRefIds != null && clarinRefIds.length() > 0) {
//	    CmdiComponentLinkReader cmdiComponentLinkReader = parentNode.getCmdiComponentLinkReader();
//	    if (cmdiComponentLinkReader != null) {
//		for (String refId : clarinRefIds.split(" ")) {
//		    refId = refId.trim();
//		    if (refId.length() > 0) {
//			CmdiComponentLinkReader.CmdiResourceLink clarinLink = cmdiComponentLinkReader.getResourceLink(refId);
//			addResourceLinkNode(parentNode, destinationNode, parentChildTree, clarinLink, childLinks);
//		    }
//		}
//	    }
//	}
    }

    private void addResourceLinkNode(ArbilDataNode parentNode, ArbilDataNode destinationNode, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree, CmdiComponentLinkReader.CmdiResourceLink clarinLink, ArrayList<String[]> childLinks) {
//	if (clarinLink != null) {
//	    try {
//		URI linkURI = clarinLink.getLinkUri();
//		if (linkURI != null) {
//		    linkURI = parentNode.getURI().resolve(linkURI);
//		    childLinks.add(new String[]{clarinLink.toString(), clarinLink.resourceProxyId});
//		    parentChildTree.get(destinationNode).add(dataNodeLoader.getArbilDataNodeWithoutLoading(linkURI));
//		    clarinLink.addReferencingNode();
//		}
//	    } catch (URISyntaxException ex) {
//		BugCatcherManager.getBugCatcher().logError("Error while reading resource link. Link not added: " + clarinLink.resourceRef, ex);
//	    }
//	}
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
	// if this is the first node and it is not metatranscript then it is not an imdi so get the clarin template
	// change made for clarin
	try {
	    // TODO: for some reason getNamespaceURI does not retrieve the uri so we are resorting to simply gettting the attribute
	    String schemaLocationString = null;
	    Node schemaLocationNode = childNode.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation");
	    if (schemaLocationNode == null) {
		schemaLocationNode = childNode.getAttributes().getNamedItem("xsi:schemaLocation");
	    }
	    if (schemaLocationNode != null) {
		schemaLocationString = schemaLocationNode.getNodeValue();
		String[] schemaLocation = schemaLocationString.split("\\s");
		schemaLocationString = schemaLocation[schemaLocation.length - 1];
		schemaLocationString = parentNode.getURI().resolve(schemaLocationString).toString();
	    } else {
		throw new Exception("Could not find the schema url: " + childNode.toString());
	    }
	    //if (schemaLocation != null && schemaLocation.length > 0) {
	    // this method of extracting the url has to accommadate many formatting variants such as \r\n or extra spaces
	    // this method also assumes that the xsd url is fully resolved
	    parentNode.nodeTemplate = ArbilTemplateManager.getSingleInstance().getCmdiTemplate(schemaLocationString);
	    /*
	     * // TODO: pass the resource node to a class to handle the resources
	     * childNode = childNode.getAttributes().getNamedItem("Components");
	     * nodeCounter = iterateChildNodes(parentNode, childLinks, childNode, nodePath, parentChildTree, nodeCounter);
	     * break;
	     */
	} catch (Exception exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	    messageDialogHandler.addMessageDialogToQueue("Could not find the schema url, some nodes will not display correctly.", "CMDI Schema Location");
	}
    }

    /**
     * Add all unreferenced resources in a document to the parent node
     *
     * @param parentNode Parent node, to which resources will be added
     * @param parentChildTree Parent-child tree that is constructed
     * @param childLinks Child links collection that is constructed
     */
    public void addUnreferencedResources(ArbilDataNode parentNode, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, ArrayList<String[]> childLinks) {
//	CmdiComponentLinkReader cmdiComponentLinkReader = parentNode.getCmdiComponentLinkReader();
//	if (cmdiComponentLinkReader != null) {
//	    for (CmdiComponentLinkReader.CmdiResourceLink link : cmdiComponentLinkReader.cmdiResourceLinkArray) {
//		if (link.getReferencingNodesCount() == 0) {
//		    addResourceLinkNode(parentNode, parentNode, parentChildTree, link, childLinks);
//		}
//	    }
//	}
    }
}
