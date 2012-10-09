package nl.mpi.arbil.data;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.data.ArbilDataNode.LoadingState;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeService {

    private DataNodeLoader dataNodeLoader;
    private MessageDialogHandler messageDialogHandler;
    private SessionStorage sessionStorage;
    private MimeHashQueue mimeHashQueue;
    private TreeHelper treeHelper;

    public ArbilDataNodeService(DataNodeLoader dataNodeLoader, MessageDialogHandler messageDialogHandler, SessionStorage sessionStorage, MimeHashQueue mimeHashQueue, TreeHelper treeHelper) {
	this.messageDialogHandler = messageDialogHandler;
	this.sessionStorage = sessionStorage;
	this.mimeHashQueue = mimeHashQueue;
	this.treeHelper = treeHelper;
	this.dataNodeLoader = dataNodeLoader;
    }

    public boolean isEditable(ArbilDataNode dataNode) {
	if (dataNode.isLocal()) {
	    return (sessionStorage.pathIsInsideCache(dataNode.getFile()))
		    || sessionStorage.pathIsInFavourites(dataNode.getFile());
	} else {
	    return false;

	}
    }

    public boolean isFavorite(ArbilDataNode dataNode) {
	if (!dataNode.isLocal()) {
	    // only local files can be favourites
	    return false;
	}
	return sessionStorage.pathIsInFavourites(dataNode.getFile());
    }

    public void pasteIntoNode(ArbilDataNode dataNode) {
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable transfer = clipboard.getContents(null);
	try {
	    String clipBoardString = "";
	    Object clipBoardData = transfer.getTransferData(DataFlavor.stringFlavor);
	    if (clipBoardData != null) {//TODO: check that this is not null first but let it pass on null so that the no data to paste messages get sent to the user
		clipBoardString = clipBoardData.toString();
		System.out.println("clipBoardString: " + clipBoardString);

		String[] elements;
		if (clipBoardString.contains("\n")) {
		    elements = clipBoardString.split("\n");
		} else {
		    elements = new String[]{clipBoardString};
		}
		for (String element : elements) {
		}
		for (ArbilDataNode clipboardNode : pasteIntoNode(dataNode, elements)) {
		    new MetadataBuilder().requestAddNode(dataNode, "copy of " + clipboardNode, clipboardNode);
		}
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    private Collection<ArbilDataNode> pasteIntoNode(ArbilDataNode dataNode, String[] clipBoardStrings) {
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
		    if (MetadataReader.getSingleInstance().nodeCanExistInNode(dataNode, templateDataNode)) {
			// Add source to destination
			new MetadataBuilder().requestAddNode(dataNode, templateDataNode.toString(), templateDataNode);
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
		dataNode.saveChangesToCache(true);
	    }
	    try {
		bumpHistory(dataNode.getFile());
		copyLastHistoryToCurrent(dataNode); // bump history is normally used afteropen and before save, in this case we cannot use that order so we must make a copy
		synchronized (dataNode.getParentDomLockObject()) {
		    return dataNode.getMetadataUtils().addCorpusLink(dataNode.getURI(), new URI[]{targetNode.getURI()});
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
	    dataNode.saveChangesToCache(false);
	}
	try {
	    dataNode.bumpHistory();
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
		dataNode.getMetadataUtils().removeCorpusLink(dataNode.getURI(), copusUriList);
		dataNode.getParentDomNode().loadArbilDom();
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
     * Inserts/sets resource location. Behavior will depend on node type
     *
     * @param location Location to insert/set
     */
    public void insertResourceLocation(ArbilDataNode dataNode, URI location) throws ArbilMetadataException {
	if (dataNode.isCmdiMetaDataNode()) {
	    ArbilDataNode resourceNode = null;
	    try {
		resourceNode = dataNodeLoader.getArbilDataNodeWithoutLoading(location);
	    } catch (Exception ex) {
		throw new ArbilMetadataException("Error creating resource node for URI: " + location.toString(), ex);
	    }
	    if (resourceNode == null) {
		throw new ArbilMetadataException("Unknown error creating resource node for URI: " + location.toString());
	    }

	    new MetadataBuilder().requestAddNode(dataNode, null, resourceNode);
	} else {
	    if (dataNode.hasResource()) {
		dataNode.resourceUrlField.setFieldValue(location.toString(), true, false);
	    }
	}
    }

    public void addField(ArbilDataNode dataNode, ArbilField fieldToAdd) {
	//        System.addField:out.println("addField: " + this.getUrlString() + " : " + fieldToAdd.xmlPath + " : " + fieldToAdd.getFieldValue());
	ArbilField[] currentFieldsArray = dataNode.getFieldArray(fieldToAdd.getTranslateFieldName());
	if (currentFieldsArray == null) {
	    currentFieldsArray = new ArbilField[]{fieldToAdd};
	} else {
	    //            System.out.println("appendingField: " + fieldToAdd);
	    ArbilField[] appendedFieldsArray = new ArbilField[currentFieldsArray.length + 1];
	    System.arraycopy(currentFieldsArray, 0, appendedFieldsArray, 0, currentFieldsArray.length);
	    appendedFieldsArray[appendedFieldsArray.length - 1] = fieldToAdd;
	    currentFieldsArray = appendedFieldsArray;

	    //            for (ImdiField tempField : currentFieldsArray) {
	    //                System.out.println("appended fields: " + tempField);
	    //            }
	}
	dataNode.addFieldArray(fieldToAdd.getTranslateFieldName(), currentFieldsArray);

	if (fieldToAdd.xmlPath.endsWith(".ResourceLink") && fieldToAdd.getParentDataNode().isChildNode()/* && fieldToAdd.parentImdi.getUrlString().contains("MediaFile") */) {
	    dataNode.resourceUrlField = fieldToAdd;
	    mimeHashQueue.addToQueue(dataNode);
	}
    }

    /**
     * Saves the current changes from memory into a new imdi file on disk.
     * Previous imdi files are renamed and kept as a history.
     * the caller is responsible for reloading the node if that is required
     */
    public void saveChangesToCache(ArbilDataNode datanode) {
	if (datanode != datanode.getParentDomNode()) {
	    //        if (this.isImdiChild()) {
	    saveChangesToCache(datanode.getParentDomNode());
	    return;
	}
	System.out.println("saveChangesToCache");
	
	synchronized (datanode.getParentDomLockObject()) {
	    ArbilJournal.getSingleInstance().clearFieldChangeHistory();
	    if (!datanode.isLocal() /* nodeUri.getScheme().toLowerCase().startsWith("http") */) {
		System.out.println("should not try to save remote files");
		return;
	    }
	    ArrayList<FieldUpdateRequest> fieldUpdateRequests = new ArrayList<FieldUpdateRequest>();
	    // this lock is to prevent the metadata file being modified and reloaded in the middle of this process
	    createFieldUpdateRequests(datanode, fieldUpdateRequests);
	    ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
	    boolean result = componentBuilder.setFieldValues(datanode, fieldUpdateRequests.toArray(new FieldUpdateRequest[]{}));
	    if (!result) {
		messageDialogHandler.addMessageDialogToQueue("Error saving changes to disk, check the log file via the help menu for more information.", "Save");
	    } else {
		datanode.nodeNeedsSaveToDisk = false;
		//            // update the icon to indicate the change
		//            setImdiNeedsSaveToDisk(null, false);
	    }
	}
	//        clearIcon(); this is called by setImdiNeedsSaveToDisk
    }

    private void createFieldUpdateRequests(ArbilDataNode datanode, ArrayList<FieldUpdateRequest> fieldUpdateRequests) {
	Vector<ArbilField[]> allFields = new Vector<ArbilField[]>();
	datanode.getAllFields(allFields);
	for (Enumeration<ArbilField[]> fieldsEnum = allFields.elements(); fieldsEnum.hasMoreElements();) {
	    {
		ArbilField[] currentFieldArray = fieldsEnum.nextElement();
		for (int fieldCounter = 0; fieldCounter < currentFieldArray.length; fieldCounter++) {
		    ArbilField currentField = currentFieldArray[fieldCounter];
		    if (currentField.fieldNeedsSaveToDisk()) {
			FieldUpdateRequest currentFieldUpdateRequest = new FieldUpdateRequest();
			currentFieldUpdateRequest.keyNameValue = currentField.getKeyName();
			currentFieldUpdateRequest.fieldOldValue = currentField.originalFieldValue;
			currentFieldUpdateRequest.fieldNewValue = currentField.getFieldValueForXml();
			currentFieldUpdateRequest.fieldPath = currentField.getFullXmlPath();
			currentFieldUpdateRequest.fieldLanguageId = currentField.getLanguageId();
			currentFieldUpdateRequest.attributeValuesMap = currentField.getAttributeValuesMap();
			fieldUpdateRequests.add(currentFieldUpdateRequest);
		    }
		}
	    }
	}
    }

    public void setDataNodeNeedsSaveToDisk(ArbilDataNode dataNode, ArbilField originatingField, boolean updateUI) {
	if (dataNode.resourceUrlField != null && dataNode.resourceUrlField.equals(originatingField)) {
	    dataNode.hashString = null;
	    dataNode.mpiMimeType = null;
	    dataNode.thumbnailFile = null;
	    dataNode.typeCheckerMessage = null;
	    mimeHashQueue.addToQueue(dataNode);
	}
	boolean needsSaveToDisk = dataNode.hasChangedFields() || dataNode.hasDomIdAttribute;
	if (dataNode.isMetaDataNode() && !dataNode.isChildNode()) {
	    if (needsSaveToDisk == false) {
		for (ArbilDataNode childNode : dataNode.getAllChildren()) {
		    if (childNode.nodeNeedsSaveToDisk) {
			needsSaveToDisk = true;
		    }
		}
	    }
	    if (dataNode.nodeNeedsSaveToDisk != needsSaveToDisk) {
		if (needsSaveToDisk) {
		    dataNodeLoader.addNodeNeedingSave(dataNode);
		} else {
		    dataNodeLoader.removeNodesNeedingSave(dataNode);
		}
		dataNode.nodeNeedsSaveToDisk = needsSaveToDisk;
	    }
	} else {
	    dataNode.nodeNeedsSaveToDisk = needsSaveToDisk; // this must be set before setImdiNeedsSaveToDisk is called
	    setDataNodeNeedsSaveToDisk(dataNode.getParentDomNode(), null, updateUI);
	}
	if (updateUI) {
	    dataNode.clearIcon();
	}
    }

    public void bumpHistory(File dataNodeFile) throws IOException {
	// update the files version number
	//TODO: the template add does not create a new history file
	int versionCounter = 0;
	File headVersion = dataNodeFile;
	//        if the .x file (the last head) exist then replace the current with it
	if (new File(dataNodeFile.getAbsolutePath() + ".x").exists()) {
	    versionCounter++;
	    headVersion = new File(dataNodeFile.getAbsolutePath() + ".x");
	}
	while (new File(dataNodeFile.getAbsolutePath() + "." + versionCounter).exists()) {
	    versionCounter++;
	}
	while (versionCounter >= 0) {
	    File lastFile = new File(dataNodeFile.getAbsolutePath() + "." + versionCounter);
	    versionCounter--;
	    File nextFile = new File(dataNodeFile.getAbsolutePath() + "." + versionCounter);
	    if (versionCounter >= 0) {
		System.out.println("renaming: " + nextFile + " : " + lastFile);
		if (!nextFile.renameTo(lastFile)) {
		    throw new IOException("Error while copying history files for metadata. Could not rename " + nextFile.toString() + " to " + lastFile.toString());
		}
	    } else {
		System.out.println("renaming: " + headVersion + " : " + lastFile);
		if (!headVersion.renameTo(lastFile)) {
		    throw new IOException("Error while copying history files for metadata. Could not rename " + headVersion.toString() + " to " + lastFile.toString());
		}
	    }
	}
    }

    public boolean resurrectHistory(ArbilDataNode dataNode, String historyVersion) {
	InputStream historyFile = null;
	OutputStream activeVersionFile = null;
	try {
	    if (historyVersion.equals(".x")) {
		if (dataNode.getFile().delete()) {
		    if (!new File(dataNode.getFile().getAbsolutePath() + ".x").renameTo(dataNode.getFile())) {
			throw new IOException("Could not rename history file '" + dataNode.getFile().getAbsolutePath() + ".x'");
		    }
		} else {
		    throw new IOException("Could not delete old history file: " + dataNode.getFile().getAbsolutePath());
		}
	    } else {
		try {
		    messageDialogHandler.offerUserToSaveChanges();
		} catch (Exception e) {
		    // user canceled the save action
		    // todo: alert user that nothing was done
		    return false;
		}
		if (!new File(dataNode.getFile().getAbsolutePath() + ".x").exists()) {
		    if (!dataNode.getFile().renameTo(new File(dataNode.getFile().getAbsolutePath() + ".x"))) {
			throw new IOException("Could not rename to history file: " + dataNode.getFile().getAbsolutePath());
		    }
		} else {
		    if (!dataNode.getFile().delete()) {
			throw new IOException("Could not delete history file: " + dataNode.getFile().getAbsolutePath());
		    }
		}
		historyFile = new FileInputStream(new File(dataNode.getFile().getAbsolutePath() + historyVersion));
		activeVersionFile = new FileOutputStream(dataNode.getFile(), true);

		byte[] copyBuffer = new byte[1024];
		int len;
		while ((len = historyFile.read(copyBuffer)) > 0) {
		    activeVersionFile.write(copyBuffer, 0, len);
		}

	    }
	} catch (FileNotFoundException e) {
	    messageDialogHandler.addMessageDialogToQueue(e.getLocalizedMessage() + ". History may be broken for " + this.toString(), "File not found");
	    BugCatcherManager.getBugCatcher().logError(e);
	    return false;
	} catch (IOException e) {
	    messageDialogHandler.addMessageDialogToQueue(e.getLocalizedMessage() + ". History may be broken for " + this.toString(), "Error while reading or writing to disk");
	    BugCatcherManager.getBugCatcher().logError(e);
	    return false;
	} finally {
	    if (null != historyFile) {
		try {
		    historyFile.close();
		} catch (IOException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	    if (null != activeVersionFile) {
		try {
		    activeVersionFile.close();
		} catch (IOException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	}
	dataNodeLoader.requestReload(dataNode.getParentDomNode());

	return true;
    }

    public void copyLastHistoryToCurrent(ArbilDataNode dataNode) {
	FileOutputStream outFile = null;
	InputStream inputStream = null;
	try {
	    outFile = new FileOutputStream(dataNode.getFile());
	    inputStream = new FileInputStream(new File(dataNode.getFile().getAbsolutePath() + ".0"));
	    int bufferLength = 1024 * 4;
	    byte[] buffer = new byte[bufferLength];
	    int bytesread = 0;
	    while (bytesread >= 0) {
		bytesread = inputStream.read(buffer);
		if (bytesread == -1) {
		    break;
		}
		outFile.write(buffer, 0, bytesread);
	    }
	} catch (IOException iOException) {
	    messageDialogHandler.addMessageDialogToQueue("Could not copy file when recovering from the last history file.", "Recover History");
	    BugCatcherManager.getBugCatcher().logError(iOException);
	} finally {
	    if (inputStream != null) {
		try {
		    inputStream.close();
		} catch (IOException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	    if (outFile != null) {
		try {
		    outFile.close();
		} catch (IOException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	}
    }

    public ArbilDataNode loadArbilDataNode(Object registeringObject, URI localUri) {
	return dataNodeLoader.getArbilDataNode(registeringObject, localUri);
    }

    public void reloadNodeShallowly(ArbilDataNode dataNode) {
	dataNode.getParentDomNode().nodeNeedsSaveToDisk = false; // clear any changes
	dataNodeLoader.requestShallowReload(dataNode.getParentDomNode());
    }

    public void reloadNode(ArbilDataNode dataNode) {
	dataNode.getParentDomNode().nodeNeedsSaveToDisk = false; // clear any changes
	//        if (!this.isImdi()) {
	//            initNodeVariables();
	//            //loadChildNodes();
	//            clearIcon();
	//            // TODO: this could just remove the decendant nodes and let the user re open them
	//            ArbilTreeHelper.getSingleInstance().updateTreeNodeChildren(this);
	////            this.clearIcon();
	//        } else {
	////            if (getParentDomNode().isCorpus()) {
	////                getParentDomNode().autoLoadChildNodes = true;
	////            }
	dataNodeLoader.requestReload(dataNode.getParentDomNode());
	//        }
    }

    public void loadArbilDom(ArbilDataNode dataNode) {
	if (dataNode.getParentDomNode() != dataNode) {
	    dataNode.getParentDomNode().loadArbilDom();
	} else {
	    synchronized (dataNode.getParentDomLockObject()) {
		dataNode.initNodeVariables(); // this might be run too often here but it must be done in the loading thread and it also must be done when the object is created
		if (!dataNode.isMetaDataNode() && !dataNode.isDirectory() && dataNode.isLocal()) {
		    // if it is an not imdi or a loose file but not a direcotry then get the md5sum
		    mimeHashQueue.addToQueue(dataNode);
		    dataNode.setLoadingState(ArbilDataNode.LoadingState.LOADED);
		}
		if (dataNode.isDirectory()) {
		    getDirectoryLinks(dataNode);
		    dataNode.setLoadingState(ArbilDataNode.LoadingState.LOADED);
		    //            clearIcon();
		}
		if (dataNode.isMetaDataNode()) {
		    loadMetadataDom(dataNode);

		    LoadingState requestedLoadingState = dataNode.getRequestedLoadingState();
		    if (requestedLoadingState == null) {
			requestedLoadingState = LoadingState.LOADED;
		    }

		    dataNode.setLoadingState(requestedLoadingState);
		}
	    }
	}
    }

    /**
     * Sets requested loading state to {@link LoadingState#LOADED} and performs a {@link #loadArbilDom() }
     */
    public void loadFullArbilDom(ArbilDataNode dataNode) {
	dataNode.setLoadingState(ArbilDataNode.LoadingState.UNLOADED);
	dataNode.setRequestedLoadingState(ArbilDataNode.LoadingState.LOADED);
	dataNode.updateLoadingState(+1);
	dataNode.loadArbilDom();
	dataNode.updateLoadingState(-1);
    }

    //<editor-fold defaultstate="collapsed" desc="Utilities (should probably be moved into a separate utility class)">
    // TODO: this is not used yet but may be required for unicode paths
    public String urlEncodePath(String inputPath) {
	// url encode the path elements
	String encodedString = null;
	try {
	    for (String inputStringPart : inputPath.split("/")) {
		//                    System.out.println("inputStringPart: " + inputStringPart);
		if (encodedString == null) {
		    encodedString = URLEncoder.encode(inputStringPart, "UTF-8");
		} else {
		    encodedString = encodedString + "/" + URLEncoder.encode(inputStringPart, "UTF-8");
		}
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	return encodedString;
    }

    public static URI conformStringToUrl(String inputUrlString) throws URISyntaxException {
	//            localUrlString = localUrlString.replace("\\", "/");
	if (!inputUrlString.toLowerCase().startsWith("http") && !inputUrlString.toLowerCase().startsWith("file:") && !inputUrlString.toLowerCase().startsWith(".")) {
	    return new File(inputUrlString).toURI();
	} else {
	    // apache method
	    //                URI tempURI = new URI(inputUrlString);
	    //                URI returnURI = URIUtils.createURI(tempURI.getScheme(), tempURI.getHost(), tempURI.getPort(), tempURI.getPath(), tempURI.getQuery(), tempURI.getFragment());
	    //                return returnURI;
	    // end apache method : this requires the uri to be broken into its parts so we might as well do it with the standard classes
	    // mpi method
	    //                URI returnURI = URIUtil.newURI(inputUrlString);
	    // end mpi method : this will url encode the # etc. and therefore loose the fragment and other parts
	    //                boolean isUncPath = inputUrlString.toLowerCase().startsWith("file:////");
	    //                if (isUncPath) {
	    //                    try {
	    //                        returnURI = new URI("file:////" + returnURI.toString().substring("file:/".length()));
	    //                    } catch (URISyntaxException urise) {
	    //                       .logError(urise);
	    //                    }
	    //                }

	    // separate the path and protocol
	    int protocolEndIndex;
	    if (inputUrlString.startsWith(".")) {
		// TODO: this is un tested for ./ paths, but at this stage it appears unlikey to ever be needed
		protocolEndIndex = 0;
	    } else {
		protocolEndIndex = inputUrlString.indexOf(":/");
	    }
	    //                while (inputUrlString.charAt(protocolEndIndex) == '/') {
	    //                    protocolEndIndex++;
	    //                }
	    String protocolComponent = inputUrlString.substring(0, protocolEndIndex);
	    String remainingComponents = inputUrlString.substring(protocolEndIndex + 1);
	    String[] pathComponentArray = remainingComponents.split("#");
	    String pathComponent = pathComponentArray[0];
	    String fragmentComponent = null;
	    if (pathComponentArray.length > 1) {
		fragmentComponent = pathComponentArray[1];
	    }
	    // note that this must be done as separate parameters not a single string otherwise it will not get url encoded
	    // TODO: this could require the other url components to be added here
	    return new URI(protocolComponent, pathComponent, fragmentComponent);
	    //                System.out.println("returnUrl: " + returnUrl);
	    ////                int protocolEndIndex = inputUrlString.lastIndexOf("/", "xxxx:".length());

	    //                String pathComponentEncoded = URLEncoder.encode(pathComponent, "UTF-8");
	    //                returnUrl = new URI(protocolComponent + pathComponentEncoded);
	    //                System.out.println("returnUrl: " + returnUrl);
	}
	//            // if the imdi api finds only one / after the file: it will interpret the url as relative and make a bit of a mess of it, so we have to make sure that we have two for the url and one for the root
	//            if (returnUrl.toString().toLowerCase().startsWith("file:") && !returnUrl.toString().toLowerCase().startsWith("file:///")) {
	//                // here we assume that this application does not use relative file paths
	//                returnUrl = new URL("file", "", "//" + returnUrl.getPath());
	//            }
	//            System.out.println("conformStringToUrl URI: " + new URI(returnUrl.toString()));

	//        System.out.println("conformStringToUrl out: " + returnUrl.toString());
    }

    static public URI normaliseURI(URI inputURI) {
	//        System.out.println("normaliseURI: " + inputURI);
	boolean isUncPath = inputURI.toString().toLowerCase().startsWith("file:////");
	URI returnURI = inputURI.normalize();
	if (isUncPath) {
	    try {
		// note that this must use the single string parameter to prevent re url encoding
		returnURI = new URI("file:////" + returnURI.toString().substring("file:/".length()));
	    } catch (URISyntaxException urise) {
		System.err.println(urise.toString());
		//BugCatcherManager.getBugCatcher().logError(urise);
	    }
	}
	return returnURI;
    }
    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Private methods">
    //<editor-fold defaultstate="collapsed" desc="Metadata DOM loading">    
    private void loadMetadataDom(ArbilDataNode dataNode) {
	if (dataNode.isLocal() && !dataNode.getFile().exists() && new File(dataNode.getFile().getAbsolutePath() + ".0").exists()) {
	    // if the file is missing then try to find a valid history file
	    copyLastHistoryToCurrent(dataNode);
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
	final boolean shallowLoading = LoadingState.PARTIAL.equals(dataNode.getRequestedLoadingState());
	MetadataReader.getSingleInstance().iterateChildNodes(dataNode, childLinks, startNode, fullNodePath, fullNodePath, parentChildTree, siblingNodePathCounter, 0, shallowLoading);
	if (dataNode.isCmdiMetaDataNode()) {
	    // Add all links that have no references to the root node (might confuse users but at least it will show what's going on)
	    MetadataReader.getSingleInstance().addUnreferencedResources(dataNode, parentChildTree, childLinks);
	}
	return childLinks.toArray(new String[][]{});
    }

    private void checkRemovedChildNodes(Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	for (Entry<ArbilDataNode, HashSet<ArbilDataNode>> entry : parentChildTree.entrySet()) {
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

    private void getDirectoryLinks(ArbilDataNode dataNode) {
	File[] dirLinkArray = null;
	File nodeFile = dataNode.getFile();
	if (nodeFile != null && nodeFile.isDirectory()) {
	    dirLinkArray = nodeFile.listFiles();
	    Vector<ArbilDataNode> childLinksTemp = new Vector<ArbilDataNode>();
	    for (int linkCount = 0; linkCount < dirLinkArray.length; linkCount++) {
		try {
		    //                    System.out.println("nodeFile: " + nodeFile);
		    //                    System.out.println("dirLinkArray[linkCount]: " + dirLinkArray[linkCount]);
		    URI childURI = dirLinkArray[linkCount].toURI();
		    ArbilDataNode currentNode = dataNodeLoader.getArbilDataNodeWithoutLoading(childURI);
		    if (treeHelper.isShowHiddenFilesInTree() || !currentNode.getFile().isHidden()) {
			childLinksTemp.add(currentNode);
		    }
		} catch (Exception ex) {
		    messageDialogHandler.addMessageDialogToQueue(dirLinkArray[linkCount] + " could not be loaded in\n" + dataNode.getUrlString(), "Load Directory");
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	    //childLinks = childLinksTemp.toArray(new String[][]{});
	    dataNode.childArray = childLinksTemp.toArray(new ArbilDataNode[]{});
	}
    }
    //</editor-fold>
    //</editor-fold>
}
