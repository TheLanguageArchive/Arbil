package nl.mpi.arbil.data.metadatafile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilConstants;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader.CmdiResourceLink;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeService;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Document : ArbilMetadataReader
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMetadataReader implements MetadataReader {

    private final MessageDialogHandler messageDialogHandler;
    private final SessionStorage sessionStorage;
    private final DataNodeLoader dataNodeLoader;

    public ArbilMetadataReader(MessageDialogHandler messageDialogHandler, SessionStorage sessionStorage, DataNodeLoader dataNodeLoader) {
	this.messageDialogHandler = messageDialogHandler;
	this.sessionStorage = sessionStorage;
	this.dataNodeLoader = dataNodeLoader;
    }

    private String getNamedAttributeValue(NamedNodeMap namedNodeMap, String attributeName) {
	Node nameNode = namedNodeMap.getNamedItem(attributeName);
	if (nameNode != null) {
	    return nameNode.getNodeValue();
	} else {
	    return null;
	}
    }

    @Override
    public URI correctLinkPath(URI parentPath, String linkString) {
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
    @Override
    public int iterateChildNodes(ArbilDataNode parentNode, Vector<String[]> childLinks, Node startNode, final String nodePath, String fullNodePath,
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
			ArbilDataNode metaNode = null;
			String pathUrlXpathSeparator = "";
			if (!parentNode.getUrlString().contains("#")) {
			    pathUrlXpathSeparator = "#";
			}
			StringBuilder siblingSpacer;
			boolean isSingleton = false;
			// Build URI for metaNode or subNode
			final StringBuilder nodeURIStringBuilder = new StringBuilder(4).append(parentNode.getURI().toString()).append(pathUrlXpathSeparator).append(siblingNodePath);
			if (maxOccurs > 1 || maxOccurs == -1 || !(parentDomNode.nodeTemplate instanceof CmdiTemplate) /* this version of the metanode creation should always be run for imdi files */) {
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
	} else {
	    // for a branch, check if there are referenced resources to add
	    addReferencedResources(parentNode, parentChildTree, childNodeAttributes, childLinks, destinationNode);
	    // and add all editable component attributes as field
	    if (childNodeAttributes != null) {
		if (parentNode.isCmdiMetaDataNode()) {
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
		if (!parentNode.isCmdiMetaDataNode()) {
		    // only if this is an imdi file we will require the node to be saved which will remove the dom id attributes
		    parentNode.hasDomIdAttribute = true;
		    showDomIdFoundMessage();
		    parentNode.setDataNodeNeedsSaveToDisk(null, false);
		}
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
	// For CMDI nodes, get field attribute paths from schema and values from document before creating arbil field
	if (destinationNode.isCmdiMetaDataNode()) {
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
	    allowsLanguageId = template.pathAllowsLanguageId(nodePath); //CMDI case where language id is optional as specified by schema
	} else {
	    allowsLanguageId = languageId != null; //IMDI case where language id comes from template
	}

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
	    addReferencedResources(parentNode, parentChildTree, childNodeAttributes, childLinks, destinationNode);
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

    private void addReferencedResources(ArbilDataNode parentNode, Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, NamedNodeMap childNodeAttributes, Vector<String[]> childLinks, ArbilDataNode destinationNode) {
	String clarinRefIds = getNamedAttributeValue(childNodeAttributes, "ref");
	if (clarinRefIds != null && clarinRefIds.length() > 0) {
	    CmdiComponentLinkReader cmdiComponentLinkReader = parentNode.getCmdiComponentLinkReader();
	    if (cmdiComponentLinkReader != null) {
		for (String refId : clarinRefIds.split(" ")) {
		    refId = refId.trim();
		    if (refId.length() > 0) {
			CmdiResourceLink clarinLink = cmdiComponentLinkReader.getResourceLink(refId);
			addResourceLinkNode(parentNode, destinationNode, parentChildTree, clarinLink, childLinks);
		    }
		}
	    }
	}
    }

    /**
     * Add all unreferenced resources in a document to the parent node
     *
     * @param parentNode Parent node, to which resources will be added
     * @param parentChildTree Parent-child tree that is constructed
     * @param childLinks Child links collection that is constructed
     */
    @Override
    public void addUnreferencedResources(ArbilDataNode parentNode, Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, Vector<String[]> childLinks) {
	CmdiComponentLinkReader cmdiComponentLinkReader = parentNode.getCmdiComponentLinkReader();
	if (cmdiComponentLinkReader != null) {
	    for (CmdiResourceLink link : cmdiComponentLinkReader.cmdiResourceLinkArray) {
		if (link.getReferencingNodesCount() == 0) {
		    addResourceLinkNode(parentNode, parentNode, parentChildTree, link, childLinks);
		}
	    }
	}
    }

    private void addResourceLinkNode(ArbilDataNode parentNode, ArbilDataNode destinationNode, Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, CmdiResourceLink clarinLink, Vector<String[]> childLinks) {
	if (clarinLink != null) {
	    try {
		URI linkURI = clarinLink.getLinkUri();
		if (linkURI != null) {
		    linkURI = parentNode.getURI().resolve(linkURI);
		    childLinks.add(new String[]{clarinLink.toString(), clarinLink.resourceProxyId});
		    parentChildTree.get(destinationNode).add(dataNodeLoader.getArbilDataNodeWithoutLoading(linkURI));
		    clarinLink.addReferencingNode();
		}
	    } catch (URISyntaxException ex) {
		BugCatcherManager.getBugCatcher().logError("Error while reading resource link. Link not added: " + clarinLink.resourceRef, ex);
	    }
	}
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
	if (!childNode.getLocalName().equals("METATRANSCRIPT")) {
	    // change made for clarin
	    try {
		// TODO: for some reason getNamespaceURI does not retrieve the uri so we are resorting to simply gettting the attribute
		//                    System.out.println("startNode.getNamespaceURI():" + startNode.getNamespaceURI());
		//                    System.out.println("childNode.getNamespaceURI():" + childNode.getNamespaceURI());
		//                    System.out.println("schemaLocation:" + childNode.getAttributes().getNamedItem("xsi:schemaLocation"));
		//                    System.out.println("noNamespaceSchemaLocation:" + childNode.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation"));
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
	} else {
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
