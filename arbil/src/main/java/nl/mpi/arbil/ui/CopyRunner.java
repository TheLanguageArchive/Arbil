/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.MetadataFormat;
import nl.mpi.arbil.data.metadatafile.MetadataUtils;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.XsdChecker;


/**
 * Runner for copy actions in import or export context. Split off from {@link nl.mpi.arbil.ui.ImportExportDialog}
 *
 * @author Twan.Goosen@mpi.nl
 * @author Peter.Withers@mpi.nl
 */
public class CopyRunner implements Runnable {

    public final static String DISK_FREE_LABEL_TEXT = "Total Disk Free: ";
    private final ImportExportUI impExpUI;
    private final SessionStorage sessionStorage;
    private final DataNodeLoader dataNodeLoader;
    private final TreeHelper treeHelper;

    public CopyRunner(ImportExportUI ui, SessionStorage sessionStorage, DataNodeLoader dataNodeLoader, TreeHelper treeHelper) {
	this.impExpUI = ui;
	this.sessionStorage = sessionStorage;
	this.dataNodeLoader = dataNodeLoader;
	this.treeHelper = treeHelper;
    }
    private int freeGbWarningPoint = 3;
    private int xsdErrors = 0;
    private int totalLoaded = 0;
    private int totalErrors = 0;
    private int totalExisting = 0;
    private int resourceCopyErrors = 0;
    private String finalMessageString = "";
    private File directoryForSizeTest;
    private boolean testFreeSpace;

    @Override
    public void run() {
	String javaVersionString = System.getProperty("java.version");
	// TG: Apparently test not required for version >= 1.5 (2011/2/3)
	testFreeSpace = !(javaVersionString.startsWith("1.4.") || javaVersionString.startsWith("1.5."));
	directoryForSizeTest = impExpUI.getExportDestinationDirectory() != null ? impExpUI.getExportDestinationDirectory() : sessionStorage.getProjectWorkingDirectory();
	// Append message about copying resource files to the copy output
	if (impExpUI.isCopyFilesOnImport() || impExpUI.isCopyFilesOnExport()) {
	    impExpUI.appendToResourceCopyOutput("'Copy Resource Files' is selected: Resource files will be downloaded where appropriate permission are granted." + "\n");
	} else {
	    impExpUI.appendToResourceCopyOutput("'Copy Resource Files' is not selected: No resource files will be downloaded, however they will be still accessible via the web server." + "\n");
	}
	try {
	    impExpUI.onCopyStart();
	    impExpUI.setProgressIndeterminate(true);
	    // Copy the selected nodes
	    copyElements(impExpUI.getSelectedNodesEnumeration());
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	    finalMessageString = finalMessageString + "There was a critical error.";
	}
	// Done copying
	impExpUI.onCopyEnd(finalMessageString);
    }

    private void copyElements(Enumeration selectedNodesEnum) {
	final ArrayList<ArbilDataNode> finishedTopNodes = new ArrayList<ArbilDataNode>();
	final Hashtable<URI, RetrievableFile> seenFiles = new Hashtable<URI, RetrievableFile>();
	final ArrayList<URI> getList = new ArrayList<URI>();
	final ArrayList<URI> doneList = new ArrayList<URI>();
	final XsdChecker xsdChecker = new XsdChecker();
	while (selectedNodesEnum.hasMoreElements() && !impExpUI.isStopCopy()) {
	    Object currentElement = selectedNodesEnum.nextElement();
	    if (currentElement instanceof ArbilDataNode) {
		copyElement(currentElement, getList, seenFiles, doneList, xsdChecker, finishedTopNodes);
	    }
	}
	finalMessageString = finalMessageString + "Processed " + totalLoaded + " Metadata Files.\n";
	if (impExpUI.getExportDestinationDirectory() == null) {
	    if (!impExpUI.isStopCopy()) {
		for (ArbilDataNode currentFinishedNode : finishedTopNodes) {
		    if (impExpUI.getDestinationNode() != null) {
			if (!impExpUI.getDestinationNode().getURI().equals(currentFinishedNode.getURI())) {
			    impExpUI.getDestinationNode().addCorpusLink(currentFinishedNode);
			}
		    } else {
			if (!treeHelper.addLocation(currentFinishedNode.getURI())) {
			    finalMessageString = finalMessageString + "The location:\n" + currentFinishedNode + "\nalready exists and need not be added again\n";
			}
		    }
		    currentFinishedNode.reloadNode();
		}
	    }
	    if (impExpUI.getDestinationNode() == null) {
		treeHelper.applyRootLocations();
	    } else {
		impExpUI.getDestinationNode().reloadNode();
	    }
	}

	impExpUI.setProgressIndeterminate(false);
	if (totalErrors != 0) {
	    finalMessageString = finalMessageString + "There were " + totalErrors + " errors, some files may not have been copied.\n";
	}
	if (xsdErrors != 0) {
	    finalMessageString = finalMessageString + "There were " + xsdErrors + " files that failed to validate and have xml errors.\n";
	}
	if (impExpUI.isStopCopy()) {
	    impExpUI.appendToTaskOutput("copy canceled");
	    System.out.println("copy canceled");
	    finalMessageString = finalMessageString + "The process was canceled, some files may not have been copied.\n";
	} else {
	    impExpUI.removeNodeSelection();
	}
    }

    private void copyElement(Object currentElement, ArrayList<URI> getList, Hashtable<URI, RetrievableFile> seenFiles, ArrayList<URI> doneList, XsdChecker xsdChecker, ArrayList<ArbilDataNode> finishedTopNodes) {
	URI currentGettableUri = ((ArbilDataNode) currentElement).getParentDomNode().getURI();
	getList.add(currentGettableUri);
	if (!seenFiles.containsKey(currentGettableUri)) {
	    seenFiles.put(currentGettableUri, new RetrievableFile(((ArbilDataNode) currentElement).getParentDomNode().getURI(), impExpUI.getExportDestinationDirectory()));
	}
	while (!impExpUI.isStopCopy() && getList.size() > 0) {
	    RetrievableFile currentRetrievableFile = seenFiles.get(getList.remove(0));
	    copyFile(currentRetrievableFile, seenFiles, doneList, getList, xsdChecker);
	}
	if (impExpUI.getExportDestinationDirectory() == null) {
	    File newNodeLocation = sessionStorage.getSaveLocation(((ArbilDataNode) currentElement).getParentDomNode().getUrlString());
	    finishedTopNodes.add(dataNodeLoader.getArbilDataNodeWithoutLoading(newNodeLocation.toURI()));
	}
    }

    private void copyFile(RetrievableFile currentRetrievableFile, Hashtable<URI, RetrievableFile> seenFiles, ArrayList<URI> doneList, ArrayList<URI> getList, XsdChecker xsdChecker) {
	try {
	    if (!doneList.contains(currentRetrievableFile.sourceURI)) {
		String journalActionString;
		if (impExpUI.getExportDestinationDirectory() == null) {
		    currentRetrievableFile.calculateUriFileName();
		    journalActionString = "import";
		} else {
		    if (impExpUI.isRenameFileToNodeName() && impExpUI.getExportDestinationDirectory() != null) {
			currentRetrievableFile.calculateTreeFileName(impExpUI.isRenameFileToLamusFriendlyName());
		    } else {
			currentRetrievableFile.calculateUriFileName();
		    }
		    journalActionString = "export";
		}
		MetadataUtils currentMetdataUtil = ArbilDataNode.getMetadataUtils(currentRetrievableFile.sourceURI.toString());
		if (currentMetdataUtil == null) {
		    throw new ArbilMetadataException("Metadata format could not be determined");
		}
		ArrayList<URI[]> uncopiedLinks = new ArrayList<URI[]>();
		URI[] linksUriArray = currentMetdataUtil.getCorpusLinks(currentRetrievableFile.sourceURI);
		if (linksUriArray != null) {
		    copyLinks(linksUriArray, seenFiles, currentRetrievableFile, getList, uncopiedLinks);
		}
		boolean replacingExitingFile = currentRetrievableFile.destinationFile.exists() && impExpUI.isOverwrite();
		if (currentRetrievableFile.destinationFile.exists()) {
		    totalExisting++;
		}
		if (currentRetrievableFile.destinationFile.exists() && !impExpUI.isOverwrite()) {
		    impExpUI.appendToTaskOutput(currentRetrievableFile.sourceURI.toString());
		    impExpUI.appendToTaskOutput("Destination already exists, skipping file: " + currentRetrievableFile.destinationFile.getAbsolutePath());
		} else {
		    if (replacingExitingFile) {
			impExpUI.appendToTaskOutput("Replaced: " + currentRetrievableFile.destinationFile.getAbsolutePath());
		    } else {
		    }
		    ArbilDataNode destinationNode = dataNodeLoader.getArbilDataNodeWithoutLoading(currentRetrievableFile.destinationFile.toURI());
		    if (destinationNode.getNeedsSaveToDisk(false)) {
			destinationNode.saveChangesToCache(true);
		    }
		    if (destinationNode.hasHistory()) {
			destinationNode.bumpHistory();
		    }
		    if (!currentRetrievableFile.destinationFile.getParentFile().exists()) {
			if (!currentRetrievableFile.destinationFile.getParentFile().mkdir()) {
			    BugCatcherManager.getBugCatcher().logError(new IOException("Could not create missing parent directory for " + currentRetrievableFile.destinationFile));
			}
		    }
		    currentMetdataUtil.copyMetadataFile(currentRetrievableFile.sourceURI, currentRetrievableFile.destinationFile, uncopiedLinks.toArray(new URI[][]{}), true);
		    ArbilJournal.getSingleInstance().saveJournalEntry(currentRetrievableFile.destinationFile.getAbsolutePath(), "", currentRetrievableFile.sourceURI.toString(), "", journalActionString);
		    String checkerResult;
		    checkerResult = xsdChecker.simpleCheck(currentRetrievableFile.destinationFile);
		    if (checkerResult != null) {
			impExpUI.appendToXmlOutput(currentRetrievableFile.sourceURI.toString() + "\n");
			impExpUI.appendToXmlOutput("destination path: " + currentRetrievableFile.destinationFile.getAbsolutePath());
			System.out.println("checkerResult: " + checkerResult);
			impExpUI.appendToXmlOutput(checkerResult + "\n");
			impExpUI.addToValidationErrors(currentRetrievableFile.sourceURI);
			xsdErrors++;
		    }
		    if (replacingExitingFile) {
			dataNodeLoader.requestReloadOnlyIfLoaded(currentRetrievableFile.destinationFile.toURI());
		    }
		}
	    }
	} catch (ArbilMetadataException ex) {
	    BugCatcherManager.getBugCatcher().logError(currentRetrievableFile.sourceURI.toString(), ex);
	    totalErrors++;
	    impExpUI.addToMetadataCopyErrors(currentRetrievableFile.sourceURI);
	    impExpUI.appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI + " (" + ex.getMessage() + ")");
	} catch (MalformedURLException ex) {
	    BugCatcherManager.getBugCatcher().logError(currentRetrievableFile.sourceURI.toString(), ex);
	    totalErrors++;
	    impExpUI.addToMetadataCopyErrors(currentRetrievableFile.sourceURI);
	    impExpUI.appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI);
	    System.out.println("Error getting links from: " + currentRetrievableFile.sourceURI);
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError(currentRetrievableFile.sourceURI.toString(), ex);
	    totalErrors++;
	    impExpUI.addToMetadataCopyErrors(currentRetrievableFile.sourceURI);
	    impExpUI.appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI);
	}
	totalLoaded++;
	final int getCount = getList.size();
	impExpUI.updateStatus(getCount, totalLoaded, totalExisting, totalErrors, xsdErrors, resourceCopyErrors);
	if (testFreeSpace) {
	    testFreeSpace();
	}
    }

    private void copyLinks(URI[] linksUriArray, Hashtable<URI, RetrievableFile> seenFiles, RetrievableFile currentRetrievableFile, ArrayList<URI> getList, ArrayList<URI[]> uncopiedLinks) throws MalformedURLException {
	for (int linkCount = 0; linkCount < linksUriArray.length && !impExpUI.isStopCopy(); linkCount++) {
	    System.out.println("Link: " + linksUriArray[linkCount].toString());
	    String currentLink = linksUriArray[linkCount].toString();
	    URI gettableLinkUri = linksUriArray[linkCount].normalize();
	    if (!seenFiles.containsKey(gettableLinkUri)) {
		seenFiles.put(gettableLinkUri, new RetrievableFile(gettableLinkUri, currentRetrievableFile.childDestinationDirectory));
	    }
	    RetrievableFile retrievableLink = seenFiles.get(gettableLinkUri);
	    if (MetadataFormat.isPathMetadata(currentLink)) {
		getList.add(gettableLinkUri);
		if (impExpUI.isRenameFileToNodeName() && impExpUI.getExportDestinationDirectory() != null) {
		    retrievableLink.calculateTreeFileName(impExpUI.isRenameFileToLamusFriendlyName());
		} else {
		    retrievableLink.calculateUriFileName();
		}
		uncopiedLinks.add(new URI[]{linksUriArray[linkCount], retrievableLink.destinationFile.toURI()});
	    } else {
		if (!impExpUI.isCopyFilesOnImport() && !impExpUI.isCopyFilesOnExport()) {
		    uncopiedLinks.add(new URI[]{linksUriArray[linkCount], linksUriArray[linkCount]});
		} else {
		    File downloadFileLocation;
		    if (impExpUI.getExportDestinationDirectory() == null) {
			downloadFileLocation = sessionStorage.updateCache(currentLink, impExpUI.getShibbolethNegotiator(), false, false, impExpUI.getDownloadAbortFlag(), impExpUI);
		    } else {
			if (impExpUI.isRenameFileToNodeName() && impExpUI.getExportDestinationDirectory() != null) {
			    retrievableLink.calculateTreeFileName(impExpUI.isRenameFileToLamusFriendlyName());
			} else {
			    retrievableLink.calculateUriFileName();
			}
			if (!retrievableLink.destinationFile.getParentFile().exists()) {
			    if (!retrievableLink.destinationFile.getParentFile().mkdirs()) {
				BugCatcherManager.getBugCatcher().logError(new IOException("Could not create missing parent directory for " + retrievableLink.destinationFile));
			    }
			}
			downloadFileLocation = retrievableLink.destinationFile;
			sessionStorage.saveRemoteResource(new URL(currentLink), downloadFileLocation, impExpUI.getShibbolethNegotiator(), true, false, impExpUI.getDownloadAbortFlag(), impExpUI);
			impExpUI.setProgressText(" ");
		    }
		    if (downloadFileLocation != null && downloadFileLocation.exists()) {
			impExpUI.appendToTaskOutput("Downloaded resource: " + downloadFileLocation.getAbsolutePath());
			uncopiedLinks.add(new URI[]{linksUriArray[linkCount], downloadFileLocation.toURI()});
		    } else {
			impExpUI.appendToResourceCopyOutput("Download failed: " + currentLink + " \n");
			impExpUI.addToFileCopyErrors(currentRetrievableFile.sourceURI);
			uncopiedLinks.add(new URI[]{linksUriArray[linkCount], linksUriArray[linkCount]});
			resourceCopyErrors++;
		    }
		}
	    }
	}
    }

    private void testFreeSpace() {
	try {
	    int freeGBytes = (int) (directoryForSizeTest.getFreeSpace() / 1073741824);
	    impExpUI.setDiskspaceState(DISK_FREE_LABEL_TEXT + freeGBytes + "GB");
	    if (freeGbWarningPoint > freeGBytes) {
		impExpUI.setProgressIndeterminate(false);
		if (impExpUI.askContinue("There is only " + freeGBytes + "GB free space left on the disk.\nTo you still want to continue?")) {
		    freeGbWarningPoint = freeGBytes - 1;
		} else {
		    impExpUI.setStopCopy(true);
		}
		impExpUI.setProgressIndeterminate(true);
	    }
	} catch (Exception ex) {
	    impExpUI.setDiskspaceState(DISK_FREE_LABEL_TEXT + "N/A");
	    testFreeSpace = false;
	}
    }

    private class RetrievableFile {

	private URI sourceURI;
	private File destinationDirectory;
	private File childDestinationDirectory;
	private File destinationFile;
	private String fileSuffix;

	public RetrievableFile(URI sourceURILocal, File destinationDirectoryLocal) {
	    sourceURI = sourceURILocal;
	    destinationDirectory = destinationDirectoryLocal;
	}

	private String makeFileNameLamusFriendly(String fileNameString) {
	    String friendlyFileName = fileNameString.replaceAll("[^A-Za-z0-9-]", "_");
	    friendlyFileName = friendlyFileName.replaceAll("__+", "_");
	    return friendlyFileName;
	}

	public void calculateUriFileName() {
	    if (destinationDirectory != null) {
		destinationFile = sessionStorage.getExportPath(sourceURI.toString(), destinationDirectory.getPath());
	    } else {
		destinationFile = sessionStorage.getSaveLocation(sourceURI.toString());
	    }
	    childDestinationDirectory = destinationDirectory;
	}

	public void calculateTreeFileName(boolean lamusFriendly) {
	    final int suffixSeparator = sourceURI.toString().lastIndexOf(".");
	    if (suffixSeparator > 0) {
		fileSuffix = sourceURI.toString().substring(suffixSeparator);
	    } else {
		fileSuffix = "";
	    }
	    ArbilDataNode currentNode = dataNodeLoader.getArbilDataNode(null, sourceURI);
	    currentNode.waitTillLoaded();
	    String fileNameString;
	    if (currentNode.isMetaDataNode()) {
		fileNameString = currentNode.toString();
	    } else {
		String urlString = sourceURI.toString();
		try {
		    urlString = URLDecoder.decode(urlString, "UTF-8");
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(urlString, ex);
		    impExpUI.appendToTaskOutput("unable to decode the file name for: " + urlString);
		    System.out.println("unable to decode the file name for: " + urlString);
		}
		final int separator = urlString.lastIndexOf(".");
		if (separator > 0) {
		    fileNameString = urlString.substring(urlString.lastIndexOf("/") + 1, separator);
		} else {
		    fileNameString = urlString.substring(urlString.lastIndexOf("/") + 1);
		}
	    }
	    fileNameString = fileNameString.replace("\\", "_");
	    fileNameString = fileNameString.replace("/", "_");
	    if (lamusFriendly) {
		fileNameString = makeFileNameLamusFriendly(fileNameString);
	    }
	    if (fileNameString.length() < 1) {
		fileNameString = "unnamed";
	    }
	    destinationFile = new File(destinationDirectory, fileNameString + fileSuffix);
	    childDestinationDirectory = new File(destinationDirectory, fileNameString);
	    int fileCounter = 1;
	    while (destinationFile.exists()) {
		if (lamusFriendly) {
		    destinationFile = new File(destinationDirectory, fileNameString + "_" + fileCounter + fileSuffix);
		    childDestinationDirectory = new File(destinationDirectory, fileNameString + "_" + fileCounter);
		} else {
		    destinationFile = new File(destinationDirectory, fileNameString + "(" + fileCounter + ")" + fileSuffix);
		    childDestinationDirectory = new File(destinationDirectory, fileNameString + "(" + fileCounter + ")");
		}
		fileCounter++;
	    }
	}
    }
}
