package nl.mpi.arbil.data;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JOptionPane;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class ImdiDataNodeService extends ArbilDataNodeService {

    private final DataNodeLoader dataNodeLoader;
    private final MessageDialogHandler messageDialogHandler;
    private final SessionStorage sessionStorage;
    private final MimeHashQueue mimeHashQueue;
    private final MetadataDomLoader metadataDomLoader;
    private final MetadataBuilder metadataBuilder;

    public ImdiDataNodeService(DataNodeLoader dataNodeLoader, MessageDialogHandler messageDialogHandler, WindowManager windowManager, SessionStorage sessionStorage, MimeHashQueue mimeHashQueue, TreeHelper treeHelper, ApplicationVersionManager versionManager) {
	super(dataNodeLoader, messageDialogHandler, mimeHashQueue, treeHelper, sessionStorage);

	this.messageDialogHandler = messageDialogHandler;
	this.sessionStorage = sessionStorage;
	this.mimeHashQueue = mimeHashQueue;
	this.dataNodeLoader = dataNodeLoader;

	this.metadataDomLoader = new ImdiDomLoader(dataNodeLoader, messageDialogHandler);
	this.metadataBuilder = new ImdiMetadataBuilder(this, messageDialogHandler, windowManager, sessionStorage, treeHelper, dataNodeLoader, versionManager);
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
		dataNode.getMetadataUtils().removeCorpusLink(dataNode.getURI(), copusUriList);
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

	    getMetadataBuilder().requestAddNode(dataNode, null, resourceNode);
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

    public boolean nodeCanExistInNode(ArbilDataNode targetDataNode, ArbilDataNode childDataNode) {
	String targetImdiPath = ImdiUtils.getNodePath((ArbilDataNode) targetDataNode);
	String childPath = ImdiUtils.getNodePath((ArbilDataNode) childDataNode);
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
	System.out.println("saveChangesToCache");
	ArbilJournal.getSingleInstance().clearFieldChangeHistory();
	if (!datanode.isLocal()) {
	    System.out.println("should not try to save remote files");
	    return;
	}
	ArrayList<FieldUpdateRequest> fieldUpdateRequests = getFieldUpdateRequests(datanode);
	ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
	boolean result = componentBuilder.setFieldValues(datanode, fieldUpdateRequests.toArray(new FieldUpdateRequest[]{}));
	if (!result) {
	    messageDialogHandler.addMessageDialogToQueue("Error saving changes to disk, check the log file via the help menu for more information.", "Save");
	} else {
	    datanode.nodeNeedsSaveToDisk = false;
	    //            // update the icon to indicate the change
	    //            setImdiNeedsSaveToDisk(null, false);
	}
	//        clearIcon(); this is called by setImdiNeedsSaveToDisk
    }

    @Override
    public MetadataDomLoader getMetadataDomLoader() {
	return metadataDomLoader;
    }

    @Override
    public MetadataBuilder getMetadataBuilder() {
	return metadataBuilder;
    }
}
