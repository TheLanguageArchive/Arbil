/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.Collection;
import java.util.Vector;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilDataNodeService {

    private final DataNodeLoader dataNodeLoader;
    private final MessageDialogHandler messageDialogHandler;
    private final MimeHashQueue mimeHashQueue;
    private final TreeHelper treeHelper;

    public ArbilDataNodeService(DataNodeLoader dataNodeLoader, MessageDialogHandler messageDialogHandler, MimeHashQueue mimeHashQueue, TreeHelper treeHelper) {
	this.dataNodeLoader = dataNodeLoader;
	this.messageDialogHandler = messageDialogHandler;
	this.mimeHashQueue = mimeHashQueue;
	this.treeHelper = treeHelper;
    }

    public abstract void deleteCorpusLink(ArbilDataNode dataNode, ArbilDataNode[] targetImdiNodes);

    /**
     * Inserts/sets resource location. Behavior will depend on node type
     *
     * @param location Location to insert/set
     */
    public abstract void insertResourceLocation(ArbilDataNode dataNode, URI location) throws ArbilMetadataException;

    public abstract boolean isEditable(ArbilDataNode dataNode);

    public abstract boolean isFavorite(ArbilDataNode dataNode);

    public abstract boolean nodeCanExistInNode(ArbilDataNode targetDataNode, ArbilDataNode childDataNode);

    public abstract boolean addCorpusLink(ArbilDataNode dataNode, ArbilDataNode targetNode);

    public abstract void addField(ArbilDataNode dataNode, ArbilField fieldToAdd);

    public abstract void saveChangesToCache(ArbilDataNode datanode);

    public ArbilDataNode loadArbilDataNode(Object registeringObject, URI localUri) {
	return dataNodeLoader.getArbilDataNode(registeringObject, localUri);
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
		    dataNode.setDataLoaded(true);
		}
		if (dataNode.isDirectory()) {
		    getDirectoryLinks(dataNode);
		    dataNode.setDataLoaded(true);
		    //            clearIcon();
		}
		if (dataNode.isMetaDataNode()) {
		    loadMetadataDom(dataNode);
		}
	    }
	}
    }

    private void loadMetadataDom(ArbilDataNode dataNode) {
	if (dataNode.isLocal() && !dataNode.getFile().exists() && new File(dataNode.getFile().getAbsolutePath() + ".0").exists()) {
	    // if the file is missing then try to find a valid history file
	    copyLastHistoryToCurrent(dataNode);
	    messageDialogHandler.addMessageDialogToQueue("Missing file has been recovered from the last history item.", "Recover History");
	}
	getMetadataDomLoader().loadMetadataDom(dataNode);
	dataNode.setDataLoaded(true);
    }

    protected abstract MetadataDomLoader getMetadataDomLoader();

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

    public static URI normaliseURI(URI inputURI) {
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
		    throw new IOException("Error while copying history files for metadata. Could not rename " + nextFile.toString() + " to " + lastFile.toString());
		}
	    }
	}
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

    public void pasteIntoNode(ArbilDataNode dataNode) {
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable transfer = clipboard.getContents(null);
	try {
	    String clipBoardString = "";
	    Object clipBoardData = transfer.getTransferData(DataFlavor.stringFlavor);
	    if (clipBoardData != null) {
		//TODO: check that this is not null first but let it pass on null so that the no data to paste messages get sent to the user
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

    protected abstract Collection<ArbilDataNode> pasteIntoNode(ArbilDataNode dataNode, String[] clipBoardStrings);
}
