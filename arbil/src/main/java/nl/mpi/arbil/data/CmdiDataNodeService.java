package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JOptionPane;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.arbil.ArbilConstants;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.MetadataField;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class CmdiDataNodeService extends ArbilDataNodeService {

    private final DataNodeLoader dataNodeLoader;
    private final MessageDialogHandler messageDialogHandler;
    private final SessionStorage sessionStorage;
    private final MetadataDomLoader metadataDomLoader;
    private final MetadataBuilder metadataBuilder;
    private final MetadataAPI metadataAPI;

    public CmdiDataNodeService(DataNodeLoader dataNodeLoader, MessageDialogHandler messageDialogHandler, WindowManager windowManager, SessionStorage sessionStorage, MimeHashQueue mimeHashQueue, TreeHelper treeHelper, ApplicationVersionManager versionManager) {
	super(dataNodeLoader, messageDialogHandler, mimeHashQueue, treeHelper, sessionStorage);

	this.messageDialogHandler = messageDialogHandler;
	this.sessionStorage = sessionStorage;
	this.dataNodeLoader = dataNodeLoader;

	this.metadataAPI = ArbilTemplateManager.getSingleInstance().getCmdiApi();

	this.metadataDomLoader = new CmdiDomLoader(dataNodeLoader, metadataAPI);
	this.metadataBuilder = new CmdiMetadataBuilder(metadataAPI, this, messageDialogHandler, windowManager, sessionStorage, treeHelper, dataNodeLoader, versionManager);
    }

    protected Collection<ArbilDataNode> pasteIntoNode(ArbilDataNode dataNode, String[] clipBoardStrings) {
	try {
	    ArrayList<ArbilDataNode> nodesToAdd = new ArrayList<ArbilDataNode>();
	    boolean ignoreSaveChanges = false;
	    for (String clipBoardString : clipBoardStrings) {
		if (dataNode.isCorpus()) {
		    if (MetadataFormat.isPathMetadata(clipBoardString) || ArbilDataNode.isStringChildNode(clipBoardString)) {
			ArbilDataNode clipboardNode = dataNodeLoader.getArbilDataNode(null, conformStringToUrl(clipBoardString));
			if (sessionStorage.pathIsInsideCache(clipboardNode.getFile())) {
			    if (!(ArbilDataNode.isStringChildNode(clipBoardString) && (!dataNode.isSession() && !dataNode.isChildNode()))) {
				if (dataNode.getFile().exists()) {
				    if (!ignoreSaveChanges && clipboardNode.getNeedsSaveToDisk(false)) {
					if (JOptionPane.CANCEL_OPTION == messageDialogHandler.showDialogBox(
						"Some of the nodes to be copied contain unsaved changes.\nUnless they are saved, these changes will not be present in the resulting nodes. Continue anyway?", "Copying with unsaved changes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
					    return new ArrayList<ArbilDataNode>(0);
					} else {
					    ignoreSaveChanges = true;
					}
				    }

				    // this must use merge like favoirite to prevent instances end endless loops in corpus branches
				    nodesToAdd.add(clipboardNode);
				} else {
				    messageDialogHandler.addMessageDialogToQueue("The target node's file does not exist", null);
				}
			    } else {
				messageDialogHandler.addMessageDialogToQueue("Cannot paste session subnodes into a corpus", null);
			    }
			} else {
			    messageDialogHandler.addMessageDialogToQueue("The target file is not in the cache", null);
			}
		    } else {
			messageDialogHandler.addMessageDialogToQueue("Pasted string is not and IMDI file", null);
		    }
		} else if (dataNode.isMetaDataNode() || dataNode.isSession()) {
		    // Get source node
		    ArbilDataNode templateDataNode = dataNodeLoader.getArbilDataNode(null, conformStringToUrl(clipBoardString));
		    // Check if it can be contained by destination node
		    if (nodeCanExistInNode(dataNode, templateDataNode)) {
			// Add source to destination
			getMetadataBuilder().requestAddNode(dataNode, templateDataNode.toString(), templateDataNode);
		    } else {
			// Invalid copy/paste...
			messageDialogHandler.addMessageDialogToQueue("Cannot copy '" + templateDataNode.toString() + "' to '" + this.toString() + "'", "Cannot copy");
		    }
		} else { // Not corpus, session or metadata
		    messageDialogHandler.addMessageDialogToQueue("Nodes of this type cannot be pasted into at this stage", null);
		}
	    }
	    return nodesToAdd;
	} catch (URISyntaxException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	    return null;
	}
    }

    public boolean addCorpusLink(ArbilDataNode dataNode, ArbilDataNode targetNode) {
	boolean linkAlreadyExists = false;
	if (targetNode.isCatalogue()) {
	    if (dataNode.hasCatalogue()) {
		//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Only one catalogue can be added", null);
		// prevent adding a second catalogue file
		return false;
	    }
	}
	for (String[] currentLinkPair : dataNode.getChildLinks()) {
	    String currentChildPath = currentLinkPair[0];
	    if (!targetNode.waitTillLoaded()) { // we must wait here before we can tell if it is a catalogue or not
		messageDialogHandler.addMessageDialogToQueue("Error adding node, could not wait for file to load", "Loading Error");
		return false;
	    }
	    if (currentChildPath.equals(targetNode.getUrlString())) {
		linkAlreadyExists = true;
	    }
	}
	if (targetNode.getUrlString().equals(dataNode.getUrlString())) {
	    messageDialogHandler.addMessageDialogToQueue("Cannot link or move a node into itself", null);
	    return false;
	}
	if (linkAlreadyExists) {
	    messageDialogHandler.addMessageDialogToQueue(targetNode + " already exists in " + this + " and will not be added again", null);
	    return false;
	} else {
	    // if link is not already there
	    // if needs saving then save now while you can
	    // TODO: it would be nice to warn the user about this, but its a corpus node so maybe it is not important
	    if (dataNode.isNeedsSaveToDisk()) {
		saveChangesToCache(dataNode);
	    }
	    try {
		bumpHistory(dataNode);
		copyLastHistoryToCurrent(dataNode); // bump history is normally used afteropen and before save, in this case we cannot use that order so we must make a copy
		synchronized (dataNode.getParentDomLockObject()) {
		    return addCorpusLink(dataNode.getURI(), new URI[]{targetNode.getURI()});
		}
	    } catch (IOException ex) {
		// Usually renaming issue. Try block includes add corpus link because this should not be attempted if history saving failed.
		BugCatcherManager.getBugCatcher().logError("I/O exception while moving node " + targetNode.toString() + " to " + this.toString(), ex);
		messageDialogHandler.addMessageDialogToQueue("Could not move nodes because an error occurred while saving history for node. See error log for details.", "Error while moving nodes");
		return false;
	    }
	}
    }

    public void deleteCorpusLink(ArbilDataNode dataNode, ArbilDataNode[] targetImdiNodes) {
	// TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
	if (dataNode.nodeNeedsSaveToDisk) {
	    saveChangesToCache(dataNode);
	}
	try {
	    bumpHistory(dataNode);
	    copyLastHistoryToCurrent(dataNode); // bump history is normally used afteropen and before save, in this case we cannot use that order so we must make a copy
	    synchronized (dataNode.getParentDomLockObject()) {
		System.out.println("deleting by corpus link");
		URI[] copusUriList = new URI[targetImdiNodes.length];
		for (int nodeCounter = 0; nodeCounter < targetImdiNodes.length; nodeCounter++) {
		    //                if (targetImdiNodes[nodeCounter].hasResource()) {
		    //                    copusUriList[nodeCounter] = targetImdiNodes[nodeCounter].getFullResourceURI(); // todo: should this resouce case be used here? maybe just the uri
		    //                } else {
		    copusUriList[nodeCounter] = targetImdiNodes[nodeCounter].getURI();
		    //                }
		}
		removeCorpusLink(dataNode.getURI(), copusUriList);
		loadArbilDom(dataNode.getParentDomNode());
	    }
	    //        for (ImdiTreeObject currentChildNode : targetImdiNodes) {
	    ////            currentChildNode.clearIcon();
	    //            ArbilTreeHelper.getSingleInstance().updateTreeNodeChildren(currentChildNode);
	    //        }
	    for (ArbilDataNode removedChild : targetImdiNodes) {
		removedChild.removeFromAllContainers();
	    }
	} catch (IOException ex) {
	    // Usually renaming issue. Try block includes add corpus link because this should not be attempted if history saving failed.
	    BugCatcherManager.getBugCatcher().logError("I/O exception while deleting nodes from " + this.toString(), ex);
	    messageDialogHandler.addMessageDialogToQueue("Could not delete nodes because an error occurred while saving history for node. See error log for details.", "Error while moving nodes");
	}

	dataNode.getParentDomNode().clearIcon();
	dataNode.getParentDomNode().clearChildIcons();
	dataNode.clearIcon(); // this must be cleared so that the leaf / branch flag gets set
    }

    /**
     * Inserts/sets resource location.
     *
     * @param location Location to insert/set
     */
    public void insertResourceLocation(ArbilDataNode dataNode, URI location) throws ArbilMetadataException {
	ArbilDataNode resourceNode = null;
	try {
	    resourceNode = dataNodeLoader.getArbilDataNodeWithoutLoading(location);
	} catch (Exception ex) {
	    throw new ArbilMetadataException("Error creating resource node for URI: " + location.toString(), ex);
	}
	if (resourceNode == null) {
	    throw new ArbilMetadataException("Unknown error creating resource node for URI: " + location.toString());
	}

	getMetadataBuilder().requestAddNode(dataNode, null, resourceNode);
    }

    public void addField(ArbilDataNode dataNode, ArbilField fieldToAdd) {
	ArbilField[] currentFieldsArray = dataNode.getFieldArray(fieldToAdd.getTranslateFieldName());
	if (currentFieldsArray == null) {
	    currentFieldsArray = new ArbilField[]{fieldToAdd};
	} else {
	    ArbilField[] appendedFieldsArray = new ArbilField[currentFieldsArray.length + 1];
	    System.arraycopy(currentFieldsArray, 0, appendedFieldsArray, 0, currentFieldsArray.length);
	    appendedFieldsArray[appendedFieldsArray.length - 1] = fieldToAdd;
	    currentFieldsArray = appendedFieldsArray;
	}
	dataNode.addFieldArray(fieldToAdd.getTranslateFieldName(), currentFieldsArray);
    }

    @Override
    public MetadataDomLoader getMetadataDomLoader() {
	return metadataDomLoader;
    }

    public boolean nodeCanExistInNode(ArbilDataNode targetDataNode, ArbilDataNode childDataNode) {
	String targetImdiPath = getNodePath((ArbilDataNode) targetDataNode);
	String childPath = getNodePath((ArbilDataNode) childDataNode);
	targetImdiPath = targetImdiPath.replaceAll("\\(\\d*?\\)", "\\(x\\)");
	childPath = childPath.replaceAll("\\(\\d*?\\)", "\\(x\\)");
	//        System.out.println("nodeCanExistInNode: " + targetImdiPath + " : " + childPath);
	int targetBranchCount = targetImdiPath.replaceAll("[^(]*", "").length();
	int childBranchCount = childPath.replaceAll("[^(]*", "").length();
	//        System.out.println("targetBranchCount: " + targetBranchCount + " childBranchCount: " + childBranchCount);
	boolean hasCorrectSubNodeCount = childBranchCount - targetBranchCount < 2;
	return hasCorrectSubNodeCount && !childPath.equals(targetImdiPath) && childPath.startsWith(targetImdiPath);
    }

    /**
     * Saves the current changes from memory into a new imdi file on disk.
     * Previous imdi files are renamed and kept as a history.
     * the caller is responsible for reloading the node if that is required
     */
    public synchronized void saveChangesToCache(ArbilDataNode datanode) {
	if (datanode != datanode.getParentDomNode()) {
	    //        if (this.isImdiChild()) {
	    saveChangesToCache(datanode.getParentDomNode());
	    return;
	}

	synchronized (datanode.getParentDomLockObject()) {
	    System.out.println("saveChangesToCache");
	    ArbilJournal.getSingleInstance().clearFieldChangeHistory();
	    if (!datanode.isLocal()) {
		System.out.println("should not try to save remote files");
		return;
	    }

	    if (updateFields(datanode)) {
		if (saveToDisk(datanode)) {
		    datanode.nodeNeedsSaveToDisk = false;
		} else {
		    messageDialogHandler.addMessageDialogToQueue("Error saving changes to disk, check the log file via the help menu for more information.", "Save");
		}
	    }
	}
    }

    private boolean updateFields(ArbilDataNode datanode) throws IllegalArgumentException {
	//TODO: Attributes and xml:lang

	final MetadataDocument document = datanode.getMetadataElement().getMetadataDocument();
	ArrayList<FieldUpdateRequest> fieldUpdateRequests = getFieldUpdateRequests(datanode);
	for (FieldUpdateRequest updateRequest : fieldUpdateRequests) {
	    final String fieldXPath = updateRequest.fieldPath.replaceAll("\\.", "/:").replaceAll("\\((\\d+)\\)", "[$1]");
	    try {
		final MetadataElement childElement = document.getChildElement(fieldXPath);
		if (childElement instanceof MetadataField) {
		    MetadataField metadataField = (MetadataField) childElement;
		    if (updateRequest.fieldOldValue.equals(metadataField.getValue())) {
			metadataField.setValue(updateRequest.fieldNewValue);
		    } else {
			BugCatcherManager.getBugCatcher().logError("expecting \'" + updateRequest.fieldOldValue + "\' not \'" + metadataField.getValue() + "\' in " + fieldXPath, null);
			return false;
		    }
		}
	    } catch (IllegalArgumentException iaEx) {
		BugCatcherManager.getBugCatcher().logError("Element cannot be retrieved by path", iaEx);
		return false;
	    }
	}
	return true;
    }

    protected boolean saveToDisk(ArbilDataNode datanode) {
	//TODO: set field values
	//	boolean result = componentBuilder.setFieldValues(datanode, fieldUpdateRequests.toArray(new FieldUpdateRequest[]{}));
	boolean result = false;
	try {
	    final MetadataDocument metadataDocument = datanode.getMetadataElement().getMetadataDocument();
	    metadataAPI.writeMetadataDocument(metadataDocument, new StreamResult(datanode.getFile()));
	    metadataDocument.setFileLocation(datanode.getFile().toURI());
	    result = true;
	} catch (IOException ioEx) {
	    BugCatcherManager.getBugCatcher().logError(ioEx);
	} catch (TransformerException tEx) {
	    BugCatcherManager.getBugCatcher().logError(tEx);
	} catch (MetadataException mdEx) {
	    BugCatcherManager.getBugCatcher().logError(mdEx);
	}
	return result;
    }

    public String getNodeNameFromFields(ArbilDataNode dataNode) {
	if (dataNode.getMetadataElement() != null) {
	    return dataNode.getMetadataElement().getDisplayValue();
	} else {
	    return null;
	}
    }

    @Override
    public MetadataBuilder getMetadataBuilder() {
	return metadataBuilder;
    }

    @Override
    public File bumpHistory(ArbilDataNode dataNode) throws IOException {
	File historyFile = super.bumpHistory(dataNode);
	if (historyFile != null) {
	    dataNode.getMetadataElement().getMetadataDocument().setFileLocation(historyFile.toURI());
	}
	return historyFile;
    }

    @Override
    public List<ArbilVocabularyItem> getLanguageItems() {
	return DocumentationLanguages.getSingleInstance().getLanguageListSubsetForCmdi();
    }

    public boolean addCorpusLink(URI nodeURI, URI[] linkURI) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean copyMetadataFile(URI sourceURI, File destinationFile, URI[][] linksToUpdate, boolean updateLinks) {
	// TODO: Use metadata API
	return false;
    }

    /**
     * Returns all ResourceLinks in the specified file that are CMDI metadata instances
     *
     * @param nodeURI
     * @return
     */
    public URI[] getCorpusLinks(URI nodeURI) {
	ArrayList<URI> returnUriList = new ArrayList<URI>();
	// Get resource links in file
	CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
	ArrayList<CmdiComponentLinkReader.CmdiResourceLink> links = cmdiComponentLinkReader.readLinks(nodeURI);
	if (links != null) {
	    // Traverse links
	    for (CmdiComponentLinkReader.CmdiResourceLink link : links) {
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
		    BugCatcherManager.getBugCatcher().logError("Invalid link URI found in " + nodeURI.toString(), ex);
		}
	    }
	}
	return returnUriList.toArray(new URI[]{});
    }

    public boolean removeCorpusLink(URI nodeURI, URI[] linkURI) {
	throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getTranslateFieldName(ArbilField field) {
	String fieldName = field.xmlPath;

	// todo: these filter strings should really be read from the metadata format
	// handle the clarin path names
	fieldName = fieldName.replaceFirst("^\\.CMD\\.Components\\.[^\\.]+\\.", "");
	// handle the kinoath path names
	fieldName = fieldName.replaceFirst("^\\.Kinnate\\.CustomData\\.", "");

	if (fieldName.startsWith(".")) {
	    fieldName = fieldName.substring(1);
	}
	
	fieldName = fieldName.replaceAll("\\(\\d\\)", "");

	return addLanguageIdToFieldName(field, fieldName);
    }
}
