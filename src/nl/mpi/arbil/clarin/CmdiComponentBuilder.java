package nl.mpi.arbil.clarin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.arbil.ArbilEntityResolver;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgJournal;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Document   : CmdiComponentBuilder
 * Created on : Mar 18, 2010, 1:40:35 PM
 * @author Peter.Withers@mpi.nl
 */
public class CmdiComponentBuilder {

    public Document getDocument(URI inputUri) throws ParserConfigurationException, SAXException, IOException {
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
    public void savePrettyFormatting(Document document, File outputFile) {
        try {
            if (outputFile.toString().endsWith(".imdi")) {
                removeDomIds(document);  // remove any dom id attributes left over by the imdi api
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
            GuiHelper.linorgBugCatcher.logError(illegalArgumentException);
        } catch (TransformerException transformerException) {
            GuiHelper.linorgBugCatcher.logError(transformerException);
        } catch (TransformerFactoryConfigurationError transformerFactoryConfigurationError) {
            System.out.println(transformerFactoryConfigurationError.getMessage());
        } catch (FileNotFoundException notFoundException) {
            GuiHelper.linorgBugCatcher.logError(notFoundException);
        } catch (IOException iOException) {
            GuiHelper.linorgBugCatcher.logError(iOException);
        }
    }

    public URI insertResourceProxy(ImdiTreeObject imdiTreeObject, ImdiTreeObject resourceNode) {
        // there is no need to save the node at this point because metadatabuilder has already done so
        synchronized (imdiTreeObject.getParentDomLockObject()) {
//    <.CMD.Resources.ResourceProxyList.ResourceProxy>
//        <ResourceProxyList>
//            <ResourceProxy id="a_text">
//                <ResourceType>Resource</ResourceType>
//                <ResourceRef>bla.txt</ResourceRef>
//            </ResourceProxy>
            String targetXmlPath = imdiTreeObject.getURI().getFragment();
            if (targetXmlPath == null) {
                // todo: consider making sure that the dom parent node always has a path
                targetXmlPath = ".CMD.Components." + imdiTreeObject.getParentDomNode().nodeTemplate.loadedTemplateName;
            }
            System.out.println("insertResourceProxy: " + targetXmlPath);
//            File cmdiNodeFile = imdiTreeObject.getFile();
//            String nodeFragment = "";

            // geerate a uuid for new resource
            String resourceProxyId = UUID.randomUUID().toString();
            try {
                // load the schema
                SchemaType schemaType = getFirstSchemaType(imdiTreeObject.getNodeTemplate().templateFile);
                // load the dom
                Document targetDocument = getDocument(imdiTreeObject.getURI());
                // insert the new section
                try {
                    try {
//                    if (targetXmlPath == null) {
//                        targetXmlPath = ".CMD.Components";
//                    }

                        Node documentNode = selectSingleNode(targetDocument, targetXmlPath);
                        Node previousRefNode = documentNode.getAttributes().getNamedItem("ref");
                        if (previousRefNode != null) {
                            String previousRefValue = documentNode.getAttributes().getNamedItem("ref").getNodeValue();
                            // todo: remove old resource nodes that this one overwrites
                        }
                        ((Element) documentNode).setAttribute("ref", resourceProxyId);
                    } catch (TransformerException exception) {
                        GuiHelper.linorgBugCatcher.logError(exception);
                        return null;
                    }
//                printoutDocument(targetDocument);
                    Node addedResourceNode = insertSectionToXpath(targetDocument, targetDocument.getFirstChild(), schemaType, ".CMD.Resources.ResourceProxyList", ".CMD.Resources.ResourceProxyList.ResourceProxy");
                    addedResourceNode.getAttributes().getNamedItem("id").setNodeValue(resourceProxyId);
                    for (Node childNode = addedResourceNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
                        String localName = childNode.getNodeName();
                        if ("ResourceType".equals(localName)) {
                            childNode.setTextContent(resourceNode.mpiMimeType);
                        }
                        if ("ResourceRef".equals(localName)) {
                            childNode.setTextContent(resourceNode.getUrlString());
                        }
                    }
                } catch (Exception exception) {
                    GuiHelper.linorgBugCatcher.logError(exception);
                    return null;
                }
                // bump the history
                imdiTreeObject.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, imdiTreeObject.getFile()); // note that we want to make sure that this gets saved even without changes because we have bumped the history ant there will be no file otherwise
            } catch (IOException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            } catch (ParserConfigurationException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            } catch (SAXException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            }
            return imdiTreeObject.getURI();
        }
    }

    public boolean removeChildNodes(ImdiTreeObject imdiTreeObject, String nodePaths[]) {
        if (imdiTreeObject.getNeedsSaveToDisk()) {
            imdiTreeObject.saveChangesToCache(true);
        }
        synchronized (imdiTreeObject.getParentDomLockObject()) {
            System.out.println("removeChildNodes: " + imdiTreeObject);
            File cmdiNodeFile = imdiTreeObject.getFile();
            try {
                Document targetDocument = getDocument(imdiTreeObject.getURI());
                // collect up all the nodes to be deleted without changing the xpath
                ArrayList<Node> selectedNodes = new ArrayList<Node>();
                for (String currentNodePath : nodePaths) {
                    System.out.println("removeChildNodes: " + currentNodePath);
                    // todo: search for and remove any reource links referenced by this node or its sub nodes
                    Node documentNode = selectSingleNode(targetDocument, currentNodePath);
                    selectedNodes.add(documentNode);

                }
                // delete all the nodes now that the xpath is no longer relevant
                for (Node currentNode : selectedNodes) {
//                    todo: there may be an issue here when deleting a languages node with two languages within it
                    currentNode.getParentNode().removeChild(currentNode);
                }
                // bump the history
                imdiTreeObject.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, cmdiNodeFile);
                for (String currentNodePath : nodePaths) {
                    // todo log to jornal file
                }
                return true;
            } catch (ParserConfigurationException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (SAXException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (IOException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (TransformerException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            }
            return false;
        }
    }

    public boolean setFieldValues(ImdiTreeObject imdiTreeObject, FieldUpdateRequest[] fieldUpdates) {
        synchronized (imdiTreeObject.getParentDomLockObject()) {
            //new ImdiUtils().addDomIds(imdiTreeObject.getURI()); // testing only
            System.out.println("setFieldValues: " + imdiTreeObject);
            File cmdiNodeFile = imdiTreeObject.getFile();
            try {
                Document targetDocument = getDocument(imdiTreeObject.getURI());
                for (FieldUpdateRequest currentFieldUpdate : fieldUpdates) {
                    System.out.println("currentFieldUpdate: " + currentFieldUpdate.fieldPath);
                    // todo: search for and remove any reource links referenced by this node or its sub nodes
                    Node documentNode = selectSingleNode(targetDocument, currentFieldUpdate.fieldPath);
                    NamedNodeMap attributesMap = documentNode.getAttributes();
                    if (currentFieldUpdate.fieldOldValue.equals(documentNode.getTextContent())) {
                        documentNode.setTextContent(currentFieldUpdate.fieldNewValue);
                    } else {
                        GuiHelper.linorgBugCatcher.logError(new Exception("expecting \'" + currentFieldUpdate.fieldOldValue + "\' not \'" + documentNode.getTextContent() + "\' in " + currentFieldUpdate.fieldPath));
                        return false;
                    }
                    Node keyNameNode = attributesMap.getNamedItem("Name");
                    if (keyNameNode != null && currentFieldUpdate.keyNameValue != null) {
                        keyNameNode.setNodeValue(currentFieldUpdate.keyNameValue);
                    }
                    Node languageNode = attributesMap.getNamedItem("LanguageId");
                    if (languageNode == null) {
                        languageNode = attributesMap.getNamedItem("xml:lang");
                    }
                    if (languageNode != null && currentFieldUpdate.fieldLanguageId != null) {
                        languageNode.setNodeValue(currentFieldUpdate.fieldLanguageId);
                    }
                }
                // bump the history
                imdiTreeObject.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, cmdiNodeFile);
                for (FieldUpdateRequest currentFieldUpdate : fieldUpdates) {
                    // log to jornal file
                    LinorgJournal.getSingleInstance().saveJournalEntry(imdiTreeObject.getUrlString(), currentFieldUpdate.fieldPath, currentFieldUpdate.fieldOldValue, currentFieldUpdate.fieldNewValue, "save");
                    if (currentFieldUpdate.fieldLanguageId != null) {
                        LinorgJournal.getSingleInstance().saveJournalEntry(imdiTreeObject.getUrlString(), currentFieldUpdate.fieldPath + ":LanguageId", currentFieldUpdate.fieldLanguageId, "", "save");
                    }
                    if (currentFieldUpdate.keyNameValue != null) {
                        LinorgJournal.getSingleInstance().saveJournalEntry(imdiTreeObject.getUrlString(), currentFieldUpdate.fieldPath + ":Name", currentFieldUpdate.keyNameValue, "", "save");
                    }
                }
                return true;
            } catch (ParserConfigurationException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (SAXException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (IOException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (TransformerException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            }
            return false;
        }
    }

    public void testInsertFavouriteComponent() {
        try {
            ImdiTreeObject favouriteImdiTreeObject1 = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(new URI("file:/Users/petwit/.arbil/favourites/fav-784841449583527834.imdi#.METATRANSCRIPT.Session.MDGroup.Actors.Actor"));
            ImdiTreeObject favouriteImdiTreeObject2 = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(new URI("file:/Users/petwit/.arbil/favourites/fav-784841449583527834.imdi#.METATRANSCRIPT.Session.MDGroup.Actors.Actor(2)"));
            ImdiTreeObject destinationImdiTreeObject = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(new URI("file:/Users/petwit/.arbil/imdicache/20100527141926/20100527141926.imdi"));
            insertFavouriteComponent(destinationImdiTreeObject, favouriteImdiTreeObject1);
            insertFavouriteComponent(destinationImdiTreeObject, favouriteImdiTreeObject2);
        } catch (URISyntaxException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
    }

    public Node insertBefore(Node targetImdiDom, Node addableNode, String targetXpath, String insertBefore) throws TransformerException {
        // todo: read the template for max occurs values and use them here and for all inserts
        Node insertBeforeNode = null;
//                String insertBeforeCSL = insertableSectionDoc.getDocumentElement().getAttribute("InsertBefore");
        if (insertBefore != null && insertBefore.length() > 0) {
            String[] insertBeforeArray = insertBefore.split(",");
            // find the node to add the new section before
            int insertBeforeCounter = 0;
            while (insertBeforeNode == null & insertBeforeCounter < insertBeforeArray.length) {
                System.out.println("insertbefore: " + insertBeforeArray);
                insertBeforeNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath + "/:" + insertBeforeArray[insertBeforeCounter]);
                insertBeforeCounter++;
            }
        }
        // find the node to add the new section to
        Node targetNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath);
        Node addedNode;
        if (insertBeforeNode != null) {
            System.out.println("inserting before: " + insertBeforeNode.getNodeName());
            addedNode = targetNode.insertBefore(addableNode, insertBeforeNode);
        } else {
            System.out.println("inserting");
            addedNode = targetNode.appendChild(addableNode);
        }
        return addedNode;
    }

    public URI insertFavouriteComponent(ImdiTreeObject destinationImdiTreeObject, ImdiTreeObject favouriteImdiTreeObject) {
        URI returnUri = null;
        // this node has already been saved in the metadatabuilder which called this
        // but lets check this again in case this gets called elsewhere and to make things consistant
//        String elementName = favouriteImdiTreeObject.getURI().getFragment();
//        String insertBefore = destinationImdiTreeObject.nodeTemplate.getInsertBeforeOfTemplate(elementName);
//        System.out.println("insertBefore: " + insertBefore);
//        int maxOccurs = destinationImdiTreeObject.nodeTemplate.getMaxOccursForTemplate(elementName);
//        System.out.println("maxOccurs: " + maxOccurs);
        if (destinationImdiTreeObject.getNeedsSaveToDisk()) {
            destinationImdiTreeObject.saveChangesToCache(true);
        }
        try {
            Document favouriteDocument;
            synchronized (favouriteImdiTreeObject.getParentDomLockObject()) {
                favouriteDocument = getDocument(favouriteImdiTreeObject.getURI());
            }
            synchronized (destinationImdiTreeObject.getParentDomLockObject()) {
                Document destinationDocument = getDocument(destinationImdiTreeObject.getURI());
                String favouriteXpath = favouriteImdiTreeObject.getURI().getFragment();
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
                        destinationNode.appendChild(singleFavouriteNode);
                        System.out.println("inserting favouriteNode: " + singleFavouriteNode.getLocalName());
                    }
                }
                savePrettyFormatting(destinationDocument, destinationImdiTreeObject.getFile());
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
                    returnUri = new URI(destinationImdiTreeObject.getURI().toString().split("#")[0] + "#" + nodeFragment);
                } catch (URISyntaxException exception) {
                    GuiHelper.linorgBugCatcher.logError(exception);
                }
            }
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (ParserConfigurationException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (SAXException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (TransformerException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
        return returnUri;
    }

    public URI insertChildComponent(ImdiTreeObject imdiTreeObject, String targetXmlPath, String cmdiComponentId) {
        if (imdiTreeObject.getNeedsSaveToDisk()) {
            imdiTreeObject.saveChangesToCache(true);
        }
        synchronized (imdiTreeObject.getParentDomLockObject()) {
            System.out.println("insertChildComponent: " + cmdiComponentId);
            System.out.println("targetXmlPath: " + targetXmlPath);
            // check for issues with the path
            if (targetXmlPath == null) {
                targetXmlPath = cmdiComponentId.replaceAll("\\.[^.]+$", "");
            } else if (targetXmlPath.replaceAll("\\(\\d+\\)", "").length() == cmdiComponentId.length()) {
                // trim the last path component if the destination equals the new node path
                // i.e. xsdPath: .CMD.Components.Session.Resources.MediaFile.Keys.Key into .CMD.Components.Session.Resources.MediaFile(1).Keys.Key
                targetXmlPath = targetXmlPath.replaceAll("\\.[^.]+$", "");
            }
            // make sure the target xpath has all the required parts
            String[] cmdiComponentArray = cmdiComponentId.split("\\.");
            String[] targetXmlPathArray = targetXmlPath.replaceAll("\\(\\d+\\)", "").split("\\.");
            for (int pathPartCounter = targetXmlPathArray.length; pathPartCounter < cmdiComponentArray.length - 1; pathPartCounter++) {
                System.out.println("adding missing path component: " + cmdiComponentArray[pathPartCounter]);
                targetXmlPath = targetXmlPath + "." + cmdiComponentArray[pathPartCounter];
            }
            // end path corrections

            System.out.println("trimmed targetXmlPath: " + targetXmlPath);
            //String targetXpath = targetNode.getURI().getFragment();
            //System.out.println("targetXpath: " + targetXpath);
//            File cmdiNodeFile = imdiTreeObject.getFile();
            String nodeFragment = "";
            try {
                // load the schema
                SchemaType schemaType = getFirstSchemaType(imdiTreeObject.getNodeTemplate().templateFile);
                // load the dom
                Document targetDocument = getDocument(imdiTreeObject.getURI());
                // insert the new section
                try {
//                printoutDocument(targetDocument);
                    Node AddedNode = insertSectionToXpath(targetDocument, targetDocument.getFirstChild(), schemaType, targetXmlPath, cmdiComponentId);
                    nodeFragment = convertNodeToNodePath(targetDocument, AddedNode, targetXmlPath);
                } catch (Exception exception) {
                    GuiHelper.linorgBugCatcher.logError(exception);
                    return null;
                }
                // bump the history
                imdiTreeObject.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, imdiTreeObject.getFile()); // note that we want to make sure that this gets saved even without changes because we have bumped the history ant there will be no file otherwise
            } catch (IOException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            } catch (ParserConfigurationException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            } catch (SAXException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            }
//       diff_match_patch diffTool= new diff_match_patch();
//       diffTool.diff_main(targetXpath, targetXpath);
            try {
                System.out.println("nodeFragment: " + nodeFragment);
                // return the child node url and path in the xml
                // first strip off any fragment then add the full node fragment
                return new URI(imdiTreeObject.getURI().toString().split("#")[0] + "#" + nodeFragment);
            } catch (URISyntaxException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            }
        }
    }

    public void testRemoveArchiveHandles() {
        try {
            Document workingDocument = getDocument(new URI("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi"));
            removeArchiveHandles(workingDocument);
            printoutDocument(workingDocument);
        } catch (Exception exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
    }

    public void removeArchiveHandles(ImdiTreeObject imdiTreeObject) {
        synchronized (imdiTreeObject.getParentDomLockObject()) {
            try {
                Document workingDocument = getDocument(imdiTreeObject.getURI());
                removeArchiveHandles(workingDocument);
                savePrettyFormatting(workingDocument, imdiTreeObject.getFile());
            } catch (Exception exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            }
        }
    }

    private void removeDomIds(Document targetDocument) {
        String handleXpath = "/:METATRANSCRIPT[@id]|/:METATRANSCRIPT//*[@id]";
        try {
            NodeList domIdNodeList = org.apache.xpath.XPathAPI.selectNodeList(targetDocument, handleXpath);
            for (int nodeCounter = 0; nodeCounter < domIdNodeList.getLength(); nodeCounter++) {
                Node domIdNode = domIdNodeList.item(nodeCounter);
                if (domIdNode != null) {
                    domIdNode.getAttributes().removeNamedItem("id");
                }
            }
        } catch (TransformerException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
    }

    private void removeArchiveHandles(Document targetDocument) {
        String handleXpath = "/:METATRANSCRIPT[@ArchiveHandle]|/:METATRANSCRIPT//*[@ArchiveHandle]";
        try {
            NodeList archiveHandleNodeList = org.apache.xpath.XPathAPI.selectNodeList(targetDocument, handleXpath);
            for (int nodeCounter = 0; nodeCounter < archiveHandleNodeList.getLength(); nodeCounter++) {
                Node archiveHandleNode = archiveHandleNodeList.item(nodeCounter);
                if (archiveHandleNode != null) {
                    archiveHandleNode.getAttributes().removeNamedItem("ArchiveHandle");
                }
            }
        } catch (TransformerException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
    }

    private Node selectSingleNode(Document targetDocument, String targetXpath) throws TransformerException {
        // convert the syntax inherited from the imdi api into xpath
        // Because most imdi files use a name space syntax we need to try both queries
        String tempXpathArray[] = new String[]{targetXpath.replaceAll("\\.", "/"), targetXpath.replaceAll("\\.", "/:")};
        for (String tempXpath : tempXpathArray) {
            tempXpath = tempXpath.replaceAll("\\(", "[");
            tempXpath = tempXpath.replaceAll("\\)", "]");
//            tempXpath = "/CMD/Components/Session/MDGroup/Actors";
            System.out.println("tempXpath: " + tempXpath);
            // find the target node of the xml
            Node returnNode = org.apache.xpath.XPathAPI.selectSingleNode(targetDocument, tempXpath);
            if (returnNode != null) {
                return returnNode;
            }
        }
        GuiHelper.linorgBugCatcher.logError(new Exception("Xpath issue, no node found for: " + targetXpath));
        return null;
    }

    private Node insertSectionToXpath(Document targetDocument, Node documentNode, SchemaType schemaType, String targetXpath, String xsdPath) throws Exception {
        System.out.println("insertSectionToXpath");
        System.out.println("xsdPath: " + xsdPath);
        System.out.println("targetXpath: " + targetXpath);
        SchemaProperty foundProperty = null;
        String strippedXpath = null;
        if (targetXpath == null) {
            documentNode = documentNode.getParentNode();
        } else {
            try {
                documentNode = selectSingleNode(targetDocument, targetXpath);
                // todo: make sure the node is insterted before then next addable type, eg actors, description
            } catch (TransformerException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            }
            strippedXpath = targetXpath.replaceAll("\\(\\d+\\)", "");
        }
        // at this point we have the xml node that the user acted on but next must get any additional nodes with the next section        
        System.out.println("strippedXpath: " + strippedXpath);
        for (String currentPathComponent : xsdPath.split("\\.")) {
            if (currentPathComponent.length() > 0) {
                System.out.println("currentPathComponent: " + currentPathComponent);
                //System.out.println("documentNode: " + documentNode.getChildNodes());
                // get the starting point in the schema
                foundProperty = null;
                for (SchemaProperty schemaProperty : schemaType.getProperties()) {
                    String currentName = schemaProperty.getName().getLocalPart();
                    if (currentPathComponent.equals(currentName)) {
                        foundProperty = schemaProperty;
                        break;
                    }
                }
                if (foundProperty == null) {
                    throw new Exception("failed to find the path in the schema: " + currentPathComponent);
                } else {
                    schemaType = foundProperty.getType();
                    System.out.println("foundProperty: " + foundProperty.getName().getLocalPart());
                }
                // get the starting node in the xml document
                // TODO: this will not navigate to the nth child node when the xpath is provided
                //if (foundNode != null) {
                //    // keep the last node found in the chain

//                if (strippedXpath != null && strippedXpath.startsWith("." + currentPathComponent)) {
//                    strippedXpath = strippedXpath.substring(currentPathComponent.length() + 1);
//                    System.out.println("strippedXpath: " + strippedXpath);
//                } else {
//                    Node childNode = documentNode.getFirstChild();
//                    while (childNode != null) {
//                        System.out.println("childNode: " + childNode.getNodeName());
//                        if (currentPathComponent.equals(childNode.getNodeName())) {
//                            System.out.println("found existing: " + currentPathComponent);
//                            documentNode = childNode;
//                            break;
//                        } else {
//                            childNode = childNode.getNextSibling();
//                        }
//                    }
//                    if (documentNode != childNode) {
//                        System.out.println("Adding destination node: " + currentPathComponent);
//                        System.out.println("Into: " + documentNode.getNodeName());
//                        documentNode = (Node) appendNode(targetDocument, null, documentNode, foundProperty);
////                        throw new Exception("failed to find the node path in the document: " + currentPathComponent);
//                    }
//                }
            }
        }
//        System.out.println("Adding marker node");
//        Element currentElement = targetDocument.createElement("MarkerNode");
//        currentElement.setTextContent(xsdPath);
//        documentNode.appendChild(currentElement);
        System.out.println("Adding destination sub nodes node to: " + documentNode.getLocalName());
        return constructXml(foundProperty, xsdPath, targetDocument, null, documentNode, false);
    }

    public String convertNodeToNodePath(Document targetDocument, Node documentNode, String targetXmlPath) {
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

    public URI createComponentFile(URI cmdiNodeFile, URI xsdFile, boolean addDummyData) {
        System.out.println("createComponentFile: " + cmdiNodeFile + " : " + xsdFile);
        try {
            Document workingDocument = getDocument(null);
            readSchema(workingDocument, xsdFile, addDummyData);
            savePrettyFormatting(workingDocument, new File(cmdiNodeFile));
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (ParserConfigurationException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (SAXException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
        return cmdiNodeFile;
    }

    private SchemaType getFirstSchemaType(File schemaFile) {
        try {
            InputStream inputStream = new FileInputStream(schemaFile);
            //Since we're dealing with xml schema files here the character encoding is assumed to be UTF-8
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setCharacterEncoding("UTF-8");
//            CatalogDocument catalogDoc = CatalogDocument.Factory.newInstance(); 
            xmlOptions.setEntityResolver(new ArbilEntityResolver(LinorgSessionStorage.getSingleInstance().getOriginatingUri(schemaFile.toURI()))); // this schema file is in the cache and must be resolved back to the origin in order to get unresolved imports within the schema file
            //xmlOptions.setCompileDownloadUrls();
            SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory.parse(inputStream, xmlOptions)}, XmlBeans.getBuiltinTypeSystem(), xmlOptions);
            // there can only be a single root node so we just get the first one, note that the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef)
            return sts.documentTypes()[0];
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (XmlException e) {
            // TODO: this is not really a good place to message this so modify to throw
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not read the XML Schema", "Error inserting node");
            GuiHelper.linorgBugCatcher.logError(e);
        }
        return null;
    }

    private void readSchema(Document workingDocument, URI xsdFile, boolean addDummyData) {
        File schemaFile;
        if (xsdFile.getScheme().toLowerCase().equals("file")) {
            // do not cache local xsd files
            schemaFile = new File(xsdFile);
        } else {
            schemaFile = LinorgSessionStorage.getSingleInstance().updateCache(xsdFile.toString(), 5);
        }
        SchemaType schemaType = getFirstSchemaType(schemaFile);
        constructXml(schemaType.getElementProperties()[0], "documentTypes", workingDocument, xsdFile.toString(), null, addDummyData);
    }

    private Element appendNode(Document workingDocument, String nameSpaceUri, Node parentElement, SchemaProperty schemaProperty, boolean addDummyData) {
//        Element currentElement = workingDocument.createElementNS("http://www.clarin.eu/cmd", schemaProperty.getName().getLocalPart());
        Element currentElement = workingDocument.createElement(schemaProperty.getName().getLocalPart());
        SchemaType currentSchemaType = schemaProperty.getType();
        for (SchemaProperty attributesProperty : currentSchemaType.getAttributeProperties()) {
            if (attributesProperty.getMinOccurs() != null && !attributesProperty.getMinOccurs().equals(BigInteger.ZERO)) {
                currentElement.setAttribute(attributesProperty.getName().getLocalPart(), attributesProperty.getDefaultText());
            }
        }
        if (parentElement == null) {
            // this is probably not the way to set these, however this will do for now (many other methods have been tested and all failed to function correctly)
            currentElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            currentElement.setAttribute("xsi:schemaLocation", "http://www.clarin.eu/cmd " + nameSpaceUri);
            //          currentElement.setAttribute("xsi:schemaLocation", "cmd " + nameSpaceUri);
            workingDocument.appendChild(currentElement);
        } else {
            parentElement.appendChild(currentElement);
        }
        //currentElement.setTextContent(schemaProperty.getMinOccurs() + ":" + schemaProperty.getMinOccurs());
        return currentElement;
    }

    private Node constructXml(SchemaProperty currentSchemaProperty, String pathString, Document workingDocument, String nameSpaceUri, Node parentElement, boolean addDummyData) {
        Node returnNode = null;
        // this must be tested against getting the actor description not the actor of an imdi profile instance
        String currentPathString = pathString + "." + currentSchemaProperty.getName().getLocalPart();
        System.out.println("Found Element: " + currentPathString);
        SchemaType currentSchemaType = currentSchemaProperty.getType();
        Element currentElement = appendNode(workingDocument, nameSpaceUri, parentElement, currentSchemaProperty, addDummyData);
        returnNode = currentElement;
        //System.out.println("printSchemaType " + schemaType.toString());
//        for (SchemaType schemaSubType : schemaType.getAnonymousTypes()) {
//            System.out.println("getAnonymousTypes:");
//            constructXml(schemaSubType, pathString + ".*getAnonymousTypes*", workingDocument, parentElement);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionConstituentTypes()) {
//            System.out.println("getUnionConstituentTypes:");
//            printSchemaType(schemaSubType);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionMemberTypes()) {
//            System.out.println("getUnionMemberTypes:");
//            constructXml(schemaSubType, pathString + ".*getUnionMemberTypes*", workingDocument, parentElement);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionSubTypes()) {
//            System.out.println("getUnionSubTypes:");
//            printSchemaType(schemaSubType);
//        }
        //SchemaType childType =schemaType.

        /////////////////////

        for (SchemaProperty schemaProperty : currentSchemaType.getElementProperties()) {
            //for (int childCounter = 0; childCounter < schemaProperty.getMinOccurs().intValue(); childCounter++) {
            // if the searched element is a child node of the given node return
            // its SchemaType
            //if (properties[i].getName().toString().equals(element)) {
            // if the searched element was not a child of the given Node
            // then again for each of these child nodes search recursively in
            // their child nodes, in the case they are a complex type, because
            // only complex types have child nodes
            //currentSchemaType.getAttributeProperties();
            //     if ((schemaProperty.getType() != null) && (!(currentSchemaType.isSimpleType()))) {

//            System.out.println("node name: " + schemaProperty.getName().getLocalPart());
//            System.out.println("node.getMinOccurs(): " + schemaProperty.getMinOccurs());
//            System.out.println("node.getMaxOccurs(): " + schemaProperty.getMaxOccurs());

            BigInteger maxNumberToAdd;
            if (addDummyData) {
                maxNumberToAdd = schemaProperty.getMaxOccurs();
                BigInteger dummyNumberToAdd = BigInteger.ONE.add(BigInteger.ONE).add(BigInteger.ONE);
                if (maxNumberToAdd == null) {
                    maxNumberToAdd = dummyNumberToAdd;
                } else {
                    if (dummyNumberToAdd.compareTo(maxNumberToAdd) == -1) {
                        // limit the number added and make sure it is less than the max number to add
                        maxNumberToAdd = dummyNumberToAdd;
                    }
                }
            } else {
                maxNumberToAdd = schemaProperty.getMinOccurs();
                if (maxNumberToAdd == null) {
                    maxNumberToAdd = BigInteger.ZERO;
                }
            }
            for (BigInteger addNodeCounter = BigInteger.ZERO; addNodeCounter.compareTo(maxNumberToAdd) < 0; addNodeCounter = addNodeCounter.add(BigInteger.ONE)) {
                constructXml(schemaProperty, currentPathString, workingDocument, nameSpaceUri, currentElement, addDummyData);
            }
        }
        return returnNode;
    }

    private void printoutDocument(Document workingDocument) {
        try {
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer transformer = tranFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source src = new DOMSource(workingDocument);
            Result dest = new StreamResult(System.out);
            transformer.transform(src, dest);
        } catch (Exception e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
    }

    public void testWalk() {
        try {
            //new CmdiComponentBuilder().readSchema();
            //File xsdFile = LinorgSessionStorage.getSingleInstance().updateCache("http://www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd", 5);
            Document workingDocument = getDocument(null);

            //Create instance of DocumentBuilderFactory
            //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //Get the DocumentBuilder
            //DocumentBuilder docBuilder = factory.newDocumentBuilder();
            //Create blank DOM Document
            //Document doc = docBuilder.newDocument();

            //create the root element
//            Element root = workingDocument.createElement("root");
//            //all it to the xml tree
//            workingDocument.appendChild(root);
//
//            //create a comment
//            Comment comment = workingDocument.createComment("This is comment");
//            //add in the root element
//            root.appendChild(comment);
//
//            //create child element
//            Element childElement = workingDocument.createElement("Child");
//            //Add the atribute to the child
//            childElement.setAttribute("attribute1", "The value of Attribute 1");
//            root.appendChild(childElement);

            readSchema(workingDocument, new URI("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1264769926773/xsd"), true);
            printoutDocument(workingDocument);
        } catch (Exception e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
    }

    public static void main(String args[]) {
        //new CmdiComponentBuilder().testWalk();
        //new CmdiComponentBuilder().testRemoveArchiveHandles();
        new CmdiComponentBuilder().testInsertFavouriteComponent();
    }
}
