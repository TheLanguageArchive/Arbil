package nl.mpi.arbil.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Document : ArbilComponentBuilder
 * Created on : Mar 18, 2010, 1:40:35 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilComponentBuilder {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }

    public static Document getDocument(URI inputUri) throws ParserConfigurationException, SAXException, IOException {
	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	documentBuilderFactory.setValidating(false);
	documentBuilderFactory.setNamespaceAware(true);
	DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	Document document;
	if (inputUri == null) {
	    document = documentBuilder.newDocument();
	} else {
	    String decodeUrlString = URLDecoder.decode(inputUri.toString(), "UTF-8");
	    document = documentBuilder.parse(decodeUrlString);
	}
	return document;
    }

//    private Document createDocument(File inputFile) throws ParserConfigurationException, SAXException, IOException {
//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//        documentBuilderFactory.setValidating(false);
//        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//        Document document = documentBuilder.newDocument();
//        return document;
//    }
    public static void savePrettyFormatting(Document document, File outputFile) {
	try {
	    if (outputFile.getPath().endsWith(".imdi")) {
		removeImdiDomIds(document);  // remove any dom id attributes left over by the imdi api
	    }
	    // set up input and output
	    DOMSource dOMSource = new DOMSource(document);
	    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
	    StreamResult xmlOutput = new StreamResult(fileOutputStream);
	    // configure transformer
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "testing.dtd");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    transformer.transform(dOMSource, xmlOutput);
	    xmlOutput.getOutputStream().close();

	    // todo: this maybe excessive to do every time
	    // this schema check has been moved to the point of loading the file rather than saving the file
//            XsdChecker xsdChecker = new XsdChecker();
//            String checkerResult;
//            checkerResult = xsdChecker.simpleCheck(outputFile, outputFile.toURI());
//            if (checkerResult != null) {
//                hasSchemaError = true;
////                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(checkerResult, "Schema Check");
//            }
	    //System.out.println(xmlOutput.getWriter().toString());
	} catch (IllegalArgumentException illegalArgumentException) {
	    BugCatcherManager.getBugCatcher().logError(illegalArgumentException);
	} catch (TransformerException transformerException) {
	    BugCatcherManager.getBugCatcher().logError(transformerException);
	} catch (TransformerFactoryConfigurationError transformerFactoryConfigurationError) {
	    System.out.println(transformerFactoryConfigurationError.getMessage());
	} catch (FileNotFoundException notFoundException) {
	    BugCatcherManager.getBugCatcher().logError(notFoundException);
	} catch (IOException iOException) {
	    BugCatcherManager.getBugCatcher().logError(iOException);
	}
    }

    public boolean removeChildNodes(ArbilDataNode arbilDataNode, String nodePaths[]) {
	if (arbilDataNode.getNeedsSaveToDisk(false)) {
	    arbilDataNode.saveChangesToCache(true);
	}
	synchronized (arbilDataNode.getParentDomLockObject()) {
	    System.out.println("remove from parent nodes: " + arbilDataNode);
	    File nodeFile = arbilDataNode.getFile();
	    try {
		Document targetDocument = getDocument(arbilDataNode.getURI());
		// collect up all the nodes to be deleted without changing the xpath
		ArrayList<Node> selectedNodes = new ArrayList<Node>();
		for (String currentNodePath : nodePaths) {
		    System.out.println("removeChildNodes: " + currentNodePath);
		    // todo: search for and remove any reource links referenced by this node or its sub nodes
		    Node documentNode = selectSingleNode(targetDocument, currentNodePath);
		    if (documentNode != null) {
			//System.out.println("documentNode: " + documentNode);
			System.out.println("documentNodeName: " + documentNode != null ? documentNode.getNodeName() : "<null>");
			selectedNodes.add(documentNode);
		    }

		}
		// delete all the nodes now that the xpath is no longer relevant
		System.out.println(selectedNodes.size());
		for (Node currentNode : selectedNodes) {
		    if (currentNode instanceof Attr) {
			Element parent = ((Attr) currentNode).getOwnerElement();
			if (parent != null) {
			    parent.removeAttributeNode((Attr) currentNode);
			}
		    } else {
			Node parentNode = currentNode.getParentNode();
			if (parentNode != null) {
			    parentNode.removeChild(currentNode);
			}
		    }
		}
		// bump the history
		arbilDataNode.bumpHistory();
		// save the dom
		savePrettyFormatting(targetDocument, nodeFile);
		for (String currentNodePath : nodePaths) {
		    // todo log to jornal file
		}
		return true;
	    } catch (ParserConfigurationException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    } catch (SAXException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    } catch (IOException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    } catch (TransformerException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    }
	    return false;
	}
    }

    public boolean setFieldValues(ArbilDataNode arbilDataNode, FieldUpdateRequest[] fieldUpdates) {
	synchronized (arbilDataNode.getParentDomLockObject()) {
	    //new ImdiUtils().addDomIds(imdiTreeObject.getURI()); // testing only
	    System.out.println("setFieldValues: " + arbilDataNode);
	    File nodeFile = arbilDataNode.getFile();
	    try {
		Document targetDocument = getDocument(arbilDataNode.getURI());
		for (FieldUpdateRequest currentFieldUpdate : fieldUpdates) {
		    System.out.println("currentFieldUpdate: " + currentFieldUpdate.fieldPath);
		    // todo: search for and remove any reource links referenced by this node or its sub nodes
		    Node documentNode = selectSingleNode(targetDocument, currentFieldUpdate.fieldPath);
		    if (currentFieldUpdate.fieldOldValue.equals(documentNode.getTextContent())) {
			documentNode.setTextContent(currentFieldUpdate.fieldNewValue);
		    } else {
			BugCatcherManager.getBugCatcher().logError(new Exception("expecting \'" + currentFieldUpdate.fieldOldValue + "\' not \'" + documentNode.getTextContent() + "\' in " + currentFieldUpdate.fieldPath));
			return false;
		    }
		    if (!(documentNode instanceof Attr)) { // Attributes obviously don't have an attributesMap
			NamedNodeMap attributesMap = documentNode.getAttributes();
			if (attributesMap != null) {
			    if (currentFieldUpdate.attributeValuesMap != null && documentNode instanceof Element) {
				// Traverse values from attribute map
				final Element element = (Element) documentNode;
				for (Map.Entry<String, Object> attributeEntry : currentFieldUpdate.attributeValuesMap.entrySet()) {
				    final String attrPath = attributeEntry.getKey();
				    final Object attrValue = attributeEntry.getValue();

				    final Attr attrNode = getAttributeNodeFromPath(element, attrPath);

				    if (attrValue == null || "".equals(attrValue)) {
					// Null or empty, remove if set
					if (attrNode != null) {
					    element.removeAttributeNode(attrNode);
					}
				    } else {
					// Value has been set, apply to document
					if (attrNode != null) {
					    // Modify on existing attribute
					    attrNode.setValue(attrValue.toString());
					} else {
					    // Set new attribute
					    addAttributeNodeFromPath(element, attrPath, attrValue.toString());
					}
				    }
				}
			    }

			    Node languageNode = attributesMap.getNamedItem("LanguageId");
			    if (languageNode == null) {
				languageNode = attributesMap.getNamedItem("xml:lang");
			    }
			    if (languageNode != null) {
				languageNode.setNodeValue(currentFieldUpdate.fieldLanguageId);
			    }

			    if (currentFieldUpdate.keyNameValue != null) {
				Node keyNameNode = attributesMap.getNamedItem("Name");
				if (keyNameNode != null) {
				    keyNameNode.setNodeValue(currentFieldUpdate.keyNameValue);
				}
			    }
			}
		    }
		}
		// bump the history
		arbilDataNode.bumpHistory();
		// save the dom
		savePrettyFormatting(targetDocument, nodeFile);
		for (FieldUpdateRequest currentFieldUpdate : fieldUpdates) {
		    // log to jornal file
		    ArbilJournal.getSingleInstance().saveJournalEntry(arbilDataNode.getUrlString(), currentFieldUpdate.fieldPath, currentFieldUpdate.fieldOldValue, currentFieldUpdate.fieldNewValue, "save");
		    if (currentFieldUpdate.fieldLanguageId != null) {
			ArbilJournal.getSingleInstance().saveJournalEntry(arbilDataNode.getUrlString(), currentFieldUpdate.fieldPath + ":LanguageId", currentFieldUpdate.fieldLanguageId, "", "save");
		    }
		    if (currentFieldUpdate.keyNameValue != null) {
			ArbilJournal.getSingleInstance().saveJournalEntry(arbilDataNode.getUrlString(), currentFieldUpdate.fieldPath + ":Name", currentFieldUpdate.keyNameValue, "", "save");
		    }
		}
		return true;
	    } catch (ParserConfigurationException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    } catch (SAXException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    } catch (IOException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    } catch (TransformerException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    }
	    return false;
	}
    }

    public URI insertFavouriteComponent(ArbilDataNode destinationArbilDataNode, ArbilDataNode favouriteArbilDataNode) throws ArbilMetadataException {
	URI returnUri = null;
	// this node has already been saved in the metadatabuilder which called this
	// but lets check this again in case this gets called elsewhere and to make things consistant
	String elementName = favouriteArbilDataNode.getURI().getFragment();
	//String insertBefore = destinationArbilDataNode.nodeTemplate.getInsertBeforeOfTemplate(elementName);
	String insertBefore = destinationArbilDataNode.getNodeTemplate().getInsertBeforeOfTemplate(elementName);
	System.out.println("insertBefore: " + insertBefore);
	int maxOccurs = destinationArbilDataNode.getNodeTemplate().getMaxOccursForTemplate(elementName);
	System.out.println("maxOccurs: " + maxOccurs);
	if (destinationArbilDataNode.getNeedsSaveToDisk(false)) {
	    destinationArbilDataNode.saveChangesToCache(true);
	}
	try {
	    Document favouriteDocument;
	    synchronized (favouriteArbilDataNode.getParentDomLockObject()) {
		favouriteDocument = getDocument(favouriteArbilDataNode.getURI());
	    }
	    synchronized (destinationArbilDataNode.getParentDomLockObject()) {
		Document destinationDocument = getDocument(destinationArbilDataNode.getURI());
		String favouriteXpath = favouriteArbilDataNode.getURI().getFragment();
		String favouriteXpathTrimmed = favouriteXpath.replaceFirst("\\.[^(^.]+$", "");
		boolean onlySubNodes = !favouriteXpathTrimmed.equals(favouriteXpath);
		System.out.println("favouriteXpath: " + favouriteXpathTrimmed);
		String destinationXpath;
		if (onlySubNodes) {
		    destinationXpath = favouriteXpathTrimmed;
		} else {
		    destinationXpath = favouriteXpathTrimmed.replaceFirst("\\.[^.]+$", "");
		}
		System.out.println("destinationXpath: " + destinationXpath);

		destinationXpath = alignDestinationPathWithTarget(destinationXpath, destinationArbilDataNode);

		Node destinationNode = selectSingleNode(destinationDocument, destinationXpath);
		Node selectedNode = selectSingleNode(favouriteDocument, favouriteXpathTrimmed);
		Node importedNode = destinationDocument.importNode(selectedNode, true);
		Node[] favouriteNodes;
		if (onlySubNodes) {
		    NodeList selectedNodeList = importedNode.getChildNodes();
		    favouriteNodes = new Node[selectedNodeList.getLength()];
		    for (int nodeCounter = 0; nodeCounter < selectedNodeList.getLength(); nodeCounter++) {
			favouriteNodes[nodeCounter] = selectedNodeList.item(nodeCounter);
		    }
		} else {
		    favouriteNodes = new Node[]{importedNode};
		}
		for (Node singleFavouriteNode : favouriteNodes) {
		    if (singleFavouriteNode.getNodeType() != Node.TEXT_NODE) {
			insertNodeInOrder(destinationNode, singleFavouriteNode, insertBefore, maxOccurs);
//                        destinationNode.appendChild(singleFavouriteNode);
			System.out.println("inserting favouriteNode: " + singleFavouriteNode.getLocalName());
		    }
		}
		savePrettyFormatting(destinationDocument, destinationArbilDataNode.getFile());
		try {
		    String nodeFragment;
		    if (favouriteNodes.length != 1) {
			nodeFragment = destinationXpath; // in this case show the target node
		    } else {
			nodeFragment = convertNodeToNodePath(destinationDocument, favouriteNodes[0], destinationXpath);
		    }
		    System.out.println("nodeFragment: " + nodeFragment);
		    // return the child node url and path in the xml
		    // first strip off any fragment then add the full node fragment
		    returnUri = new URI(destinationArbilDataNode.getURI().toString().split("#")[0] + "#" + nodeFragment);
		} catch (URISyntaxException exception) {
		    BugCatcherManager.getBugCatcher().logError(exception);
		}
	    }
	} catch (IOException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (ParserConfigurationException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (SAXException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (TransformerException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	}
	return returnUri;
    }

    /**
     * Aligns a destination path for a favourite with the target path (fragment) within the target node
     *
     * Fixes issue reported in https://trac.mpi.nl/ticket/1157
     *
     * @param destinationXpath Destination path as provided by the favourite
     * @param destinationArbilDataNode target node
     * @return Aligned XPath, or original if could not be aligned
     */
    private String alignDestinationPathWithTarget(String destinationXpath, ArbilDataNode destinationArbilDataNode) {
	String targetFragment = destinationArbilDataNode.getURI().getFragment();
	if (targetFragment != null) { // If null, it's the document root

	    // If container node, we want to know the actual level to add to, so remove container part from path
	    if (destinationArbilDataNode.isContainerNode()) {
		targetFragment = targetFragment.substring(0, targetFragment.lastIndexOf("."));
	    }

	    // Canonicalize both destination path and target fragment
	    String destinationXpathGeneric = destinationXpath.replaceAll("\\(\\d+\\)", "(x)");
	    String targetFragmentGeneric = targetFragment.replaceAll("\\(\\d+\\)", "(x)");

	    // Do they match up? Destination should begin with target
	    if (destinationXpathGeneric.startsWith(targetFragmentGeneric)) {
		// Convert target fragment back into regular expression for matching with destination path
		final String commonPartRegEx = targetFragmentGeneric.replaceAll("\\(x\\)", "\\\\(\\\\d+\\\\)");
		String[] destinationXpathParts = destinationXpath.split(commonPartRegEx);
		if (destinationXpathParts.length == 0) {
		    // Complete match
		    destinationXpath = targetFragment;
		} else if (destinationXpathParts.length == 2) {
		    // Combine targetFragment with remainder from destination path
		    destinationXpath = targetFragment + destinationXpathParts[1];
		} else {
		    // Should not happen, either exact match or remainder
		    messageDialogHandler.addMessageDialogToQueue("Unexpected relation between source and target paths. See error log for details.", "Insert node");
		    BugCatcherManager.getBugCatcher().logError("destinationXpath: " + destinationXpath + "\ntargetFragment: " + targetFragment, null);
		}
	    }
	}
	return destinationXpath;
    }

    public static boolean canInsertNode(Node destinationNode, Node addableNode, int maxOccurs) {
	if (maxOccurs > 0) {
	    String addableName = addableNode.getLocalName();
	    if (addableName == null) {
		addableName = addableNode.getNodeName();
	    }
	    NodeList childNodes = destinationNode.getChildNodes();
	    int duplicateNodeCounter = 0;
	    for (int childCounter = 0; childCounter < childNodes.getLength(); childCounter++) {
		String childsName = childNodes.item(childCounter).getLocalName();
		if (addableName.equals(childsName)) {
		    duplicateNodeCounter++;
		    if (duplicateNodeCounter >= maxOccurs) {
			return false;
		    }
		}
	    }
	}
	return true;
    }
    private final static Pattern attributePathPattern = Pattern.compile("^.*\\.@[^.]+$");
    private final static Pattern namespacePartPattern = Pattern.compile("\\{(.*)\\}");

    private boolean pathIsAttribute(String pathString) {
	return attributePathPattern.matcher(pathString).matches();
    }

    /**
     *
     * @param path Full path (e.g. .CMD.Component.Test.@myattr) of attribute
     * @return Attribute, if found
     */
    private Attr getAttributeNodeFromPath(Element parent, final String path) {
	try {
	    if (pathIsAttribute(path)) {
		final String attributePart = path.replaceAll("^.*@", ""); // remove path suffix (including @) so only attribute remains
		Matcher matcher = (namespacePartPattern.matcher(attributePart)); // look for namespace part
		if (matcher.find()) {
		    String nsPart = URLDecoder.decode(matcher.group(1), "UTF-8"); // extract namespace part and decode
		    String localName = attributePart.replaceAll("\\{.*\\}", "");
		    return parent.getAttributeNodeNS(nsPart, localName);
		} else {
		    return parent.getAttributeNode(attributePart);
		}
	    }
	} catch (UnsupportedEncodingException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	return null;
    }

    /**
     *
     * @param path Full path (e.g. .CMD.Component.Test.@myattr) of attribute
     * @return Successful creation
     */
    private boolean addAttributeNodeFromPath(Element parent, final String path, final String value) {
	try {
	    if (pathIsAttribute(path)) {
		final String attributePart = path.replaceAll("^.*@", ""); // remove path suffix (including @) so only attribute remains
		Matcher matcher = (namespacePartPattern.matcher(attributePart)); // look for namespace part
		if (matcher.find()) {
		    String nsPart = URLDecoder.decode(matcher.group(1), "UTF-8"); // extract namespace part and decode
		    String localName = attributePart.replaceAll("\\{.*\\}", "");
		    parent.setAttributeNS(nsPart, localName, value);
		} else {
		    parent.setAttribute(attributePart, value);
		}
		return true;
	    }
	} catch (UnsupportedEncodingException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	return false;
    }

    public static Node insertNodeInOrder(Node destinationNode, Node addableNode, String insertBefore, int maxOccurs) throws TransformerException, ArbilMetadataException {
	if (!canInsertNode(destinationNode, addableNode, maxOccurs)) {
	    throw new ArbilMetadataException("The maximum nodes of this type have already been added.\n");
	}

	// todo: read the template for max occurs values and use them here and for all inserts
	Node insertBeforeNode = null;
	if (insertBefore != null && insertBefore.length() > 0) {
	    String[] insertBeforeArray = insertBefore.split(",");
	    // find the node to add the new section before
	    NodeList childNodes = destinationNode.getChildNodes();
	    outerloop:
	    for (int childCounter = 0; childCounter < childNodes.getLength(); childCounter++) {
		String childsName = childNodes.item(childCounter).getLocalName();
		for (String currentInsertBefore : insertBeforeArray) {
		    if (currentInsertBefore.equals(childsName)) {
			System.out.println("insertbefore: " + childsName);
			insertBeforeNode = childNodes.item(childCounter);
			break outerloop;

		    }
		}
	    }
	}

	// find the node to add the new section to
	Node addedNode;
	if (insertBeforeNode != null) {
	    System.out.println("inserting before: " + insertBeforeNode.getNodeName());
	    addedNode = destinationNode.insertBefore(addableNode, insertBeforeNode);
	} else {
	    System.out.println("inserting");
	    if (addableNode instanceof Attr) {
		if (destinationNode instanceof Element) {
		    addedNode = ((Element) destinationNode).setAttributeNode((Attr) addableNode);
		} else {
		    throw new ArbilMetadataException("Cannot insert attribute in node of this type: " + destinationNode.getNodeName());
		}
	    } else {
		addedNode = destinationNode.appendChild(addableNode);
	    }
	}
	return addedNode;
    }

    public void removeArchiveHandles(ArbilDataNode arbilDataNode) {
	synchronized (arbilDataNode.getParentDomLockObject()) {
	    try {
		Document workingDocument = getDocument(arbilDataNode.getURI());
		removeArchiveHandles(workingDocument);
		savePrettyFormatting(workingDocument, arbilDataNode.getFile());
	    } catch (Exception exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    }
	}
    }

    private static void removeImdiDomIds(Document targetDocument) {
	String handleXpath = "/:METATRANSCRIPT[@id]|/:METATRANSCRIPT//*[@id]";
	try {
	    NodeList domIdNodeList = XPathAPI.selectNodeList(targetDocument, handleXpath);
	    for (int nodeCounter = 0; nodeCounter < domIdNodeList.getLength(); nodeCounter++) {
		Node domIdNode = domIdNodeList.item(nodeCounter);
		if (domIdNode != null) {
		    domIdNode.getAttributes().removeNamedItem("id");
		}
	    }
	} catch (TransformerException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	}
    }

    private void removeArchiveHandles(Document targetDocument) {
	String handleXpath = "/:METATRANSCRIPT[@ArchiveHandle]|/:METATRANSCRIPT//*[@ArchiveHandle]";
	try {
	    NodeList archiveHandleNodeList = XPathAPI.selectNodeList(targetDocument, handleXpath);
	    for (int nodeCounter = 0; nodeCounter < archiveHandleNodeList.getLength(); nodeCounter++) {
		Node archiveHandleNode = archiveHandleNodeList.item(nodeCounter);
		if (archiveHandleNode != null) {
		    archiveHandleNode.getAttributes().removeNamedItem("ArchiveHandle");
		}
	    }
	} catch (TransformerException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	}
    }

    private Node selectSingleNode(Document targetDocument, String targetXpath) throws TransformerException {
	String tempXpathArray[] = convertImdiPathToXPathOptions(targetXpath);
	if (tempXpathArray != null) {
	    for (String tempXpath : tempXpathArray) {
		tempXpath = tempXpath.replaceAll("\\(", "[");
		tempXpath = tempXpath.replaceAll("\\)", "]");
//            tempXpath = "/CMD/Components/Session/MDGroup/Actors";
		System.out.println("tempXpath: " + tempXpath);
		// find the target node of the xml
		Node returnNode = XPathAPI.selectSingleNode(targetDocument, tempXpath);
		if (returnNode != null) {
		    return returnNode;
		}
	    }
	}
	BugCatcherManager.getBugCatcher().logError(new Exception("Xpath issue, no node found for: " + targetXpath));
	return null;
    }

    private String[] convertImdiPathToXPathOptions(String targetXpath) {
	if (targetXpath == null) {
	    return null;
	} else {
	    // convert the syntax inherited from the imdi api into xpath
	    // Because most imdi files use a name space syntax we need to try both queries
	    return new String[]{
			targetXpath.replaceAll("\\.", "/"),
			targetXpath.replaceAll("\\.@([^.]*)$", "/@$1").replaceAll("\\.", "/:") // Attributes (.@) should not get colon hence the two steps
		    };
	}

    }

    public static String convertNodeToNodePath(Document targetDocument, Node documentNode, String targetXmlPath) {
	System.out.println("Calculating the added fragment");
	// count siblings to get the correct child index for the fragment
	int siblingCouter = 1;
	Node siblingNode = documentNode.getPreviousSibling();
	while (siblingNode != null) {
	    if (documentNode.getNodeName().equals(siblingNode.getNodeName())) {
		siblingCouter++;
	    }
	    siblingNode = siblingNode.getPreviousSibling();
	}
	// get the current node name
	String nodeFragment = documentNode.getNodeName();
	if (documentNode instanceof Attr) {
	    nodeFragment = "@" + nodeFragment;
	}
	String nodePathString = targetXmlPath + "." + nodeFragment + "(" + siblingCouter + ")";

//        String nodePathString = "";
//        Node parentNode = documentNode;
//        while (parentNode != null) {
//            // count siblings to get the correct child index for the fragment
//            int siblingCouter = 1;
//            Node siblingNode = parentNode.getPreviousSibling();
//            while (siblingNode != null) {
//                if (parentNode.getLocalName().equals(siblingNode.getLocalName())) {
//                    siblingCouter++;
//                }
//                siblingNode = siblingNode.getPreviousSibling();
//            }
//            // get the current node name
//            String nodeFragment = parentNode.getNodeName();
//
//            nodePathString = "." + nodeFragment + "(" + siblingCouter + ")" + nodePathString;
//            //System.out.println("nodePathString: " + nodePathString);
//            if (parentNode.isSameNode(targetDocument.getDocumentElement())) {
//                break;
//            }
//            parentNode = parentNode.getParentNode();
//        }
//        nodeFragment = "." + nodeFragment;
	System.out.println("nodeFragment: " + nodePathString);
	System.out.println("targetXmlPath: " + targetXmlPath);
	return nodePathString;
	//return childStartElement.
    }
}
