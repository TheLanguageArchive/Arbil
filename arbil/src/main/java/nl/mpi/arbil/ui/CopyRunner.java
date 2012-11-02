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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.MetadataFormat;
import nl.mpi.arbil.data.metadatafile.MetadataUtils;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.arbil.util.XsdChecker;

/////////////////////////////////////////

// end functions called by the threads //
class CopyRunner implements Runnable {
    private final ImportExportDialog impExpUI;
    private final WindowManager windowManager;
    private final MessageDialogHandler dialogHandler;
    private final SessionStorage sessionStorage;
    private final DataNodeLoader dataNodeLoader;
    private final TreeHelper treeHelper;
    
    public CopyRunner(ImportExportDialog impExpUI, WindowManager windowManager, MessageDialogHandler dialogHandler, SessionStorage sessionStorage, DataNodeLoader dataNodeLoader, TreeHelper treeHelper) {
	this.impExpUI = impExpUI;
	this.windowManager = windowManager;
	this.dialogHandler = dialogHandler;
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
	directoryForSizeTest = impExpUI.exportDestinationDirectory != null ? impExpUI.exportDestinationDirectory : sessionStorage.getProjectWorkingDirectory();
	// Append message about copying resource files to the copy output
	if (impExpUI.copyFilesImportCheckBox.isSelected() || impExpUI.copyFilesExportCheckBox.isSelected()) {
	    impExpUI.resourceCopyOutput.append("'Copy Resource Files' is selected: Resource files will be downloaded where appropriate permission are granted." + "\n");
	} else {
	    impExpUI.resourceCopyOutput.append("'Copy Resource Files' is not selected: No resource files will be downloaded, however they will be still accessible via the web server." + "\n");
	}
	try {
	    // Copy the selected nodes
	    copyElements(impExpUI.selectedNodes.elements());
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	    finalMessageString = finalMessageString + "There was a critical error.";
	}
	// Done copying
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		impExpUI.setUItoStoppedState();
	    }
	});
	System.out.println("finalMessageString: " + finalMessageString);
	Object[] options = {"Close", "Details"};
	int detailsOption = JOptionPane.showOptionDialog(windowManager.getMainFrame(), finalMessageString, impExpUI.importExportDialog.getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	if (detailsOption == 0) {
	    impExpUI.importExportDialog.dispose();
	} else {
	    if (!impExpUI.showingDetails) {
		impExpUI.updateDialog(impExpUI.showingMoreOptions, true);
		impExpUI.importExportDialog.pack();
	    }
	}
	if (impExpUI.exportDestinationDirectory != null) {
	    windowManager.openFileInExternalApplication(impExpUI.exportDestinationDirectory.toURI());
	}
    }

    private void copyElements(Enumeration selectedNodesEnum) {
	XsdChecker xsdChecker = new XsdChecker();
	impExpUI.waitTillVisible();
	impExpUI.progressBar.setIndeterminate(true);
	ArrayList<ArbilDataNode> finishedTopNodes = new ArrayList<ArbilDataNode>();
	Hashtable<URI, RetrievableFile> seenFiles = new Hashtable<URI, RetrievableFile>();
	ArrayList<URI> getList = new ArrayList<URI>();
	ArrayList<URI> doneList = new ArrayList<URI>();
	while (selectedNodesEnum.hasMoreElements() && !impExpUI.stopCopy) {
	    Object currentElement = selectedNodesEnum.nextElement();
	    if (currentElement instanceof ArbilDataNode) {
		copyElement(currentElement, getList, seenFiles, doneList, xsdChecker, finishedTopNodes);
	    }
	}
	finalMessageString = finalMessageString + "Processed " + totalLoaded + " Metadata Files.\n";
	if (impExpUI.exportDestinationDirectory == null) {
	    if (!impExpUI.stopCopy) {
		for (ArbilDataNode currentFinishedNode : finishedTopNodes) {
		    if (impExpUI.destinationNode != null) {
			if (!impExpUI.destinationNode.getURI().equals(currentFinishedNode.getURI())) {
			    impExpUI.destinationNode.addCorpusLink(currentFinishedNode);
			}
		    } else {
			if (!treeHelper.addLocation(currentFinishedNode.getURI())) {
			    finalMessageString = finalMessageString + "The location:\n" + currentFinishedNode + "\nalready exists and need not be added again\n";
			}
		    }
		    currentFinishedNode.reloadNode();
		}
	    }
	    if (impExpUI.destinationNode == null) {
		treeHelper.applyRootLocations();
	    } else {
		impExpUI.destinationNode.reloadNode();
	    }
	}
	impExpUI.progressBar.setIndeterminate(false);
	if (totalErrors != 0) {
	    finalMessageString = finalMessageString + "There were " + totalErrors + " errors, some files may not have been copied.\n";
	}
	if (xsdErrors != 0) {
	    finalMessageString = finalMessageString + "There were " + xsdErrors + " files that failed to validate and have xml errors.\n";
	}
	if (impExpUI.stopCopy) {
	    impExpUI.appendToTaskOutput("copy canceled");
	    System.out.println("copy canceled");
	    finalMessageString = finalMessageString + "The process was canceled, some files may not have been copied.\n";
	} else {
	    impExpUI.selectedNodes.removeAllElements();
	}
    }

    private void copyElement(Object currentElement, ArrayList<URI> getList, Hashtable<URI, RetrievableFile> seenFiles, ArrayList<URI> doneList, XsdChecker xsdChecker, ArrayList<ArbilDataNode> finishedTopNodes) {
	URI currentGettableUri = ((ArbilDataNode) currentElement).getParentDomNode().getURI();
	getList.add(currentGettableUri);
	if (!seenFiles.containsKey(currentGettableUri)) {
	    seenFiles.put(currentGettableUri, new RetrievableFile(((ArbilDataNode) currentElement).getParentDomNode().getURI(), impExpUI.exportDestinationDirectory));
	}
	while (!impExpUI.stopCopy && getList.size() > 0) {
	    RetrievableFile currentRetrievableFile = seenFiles.get(getList.remove(0));
	    copyFile(currentRetrievableFile, seenFiles, doneList, getList, xsdChecker);
	}
	if (impExpUI.exportDestinationDirectory == null) {
	    File newNodeLocation = sessionStorage.getSaveLocation(((ArbilDataNode) currentElement).getParentDomNode().getUrlString());
	    finishedTopNodes.add(dataNodeLoader.getArbilDataNodeWithoutLoading(newNodeLocation.toURI()));
	}
    }

    private void copyFile(RetrievableFile currentRetrievableFile, Hashtable<URI, RetrievableFile> seenFiles, ArrayList<URI> doneList, ArrayList<URI> getList, XsdChecker xsdChecker) {
	try {
	    if (!doneList.contains(currentRetrievableFile.sourceURI)) {
		String journalActionString;
		if (impExpUI.exportDestinationDirectory == null) {
		    currentRetrievableFile.calculateUriFileName();
		    journalActionString = "import";
		} else {
		    if (impExpUI.renameFileToNodeName.isSelected() && impExpUI.exportDestinationDirectory != null) {
			currentRetrievableFile.calculateTreeFileName(impExpUI.renameFileToLamusFriendlyName.isSelected());
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
		boolean replacingExitingFile = currentRetrievableFile.destinationFile.exists() && impExpUI.overwriteCheckBox.isSelected();
		if (currentRetrievableFile.destinationFile.exists()) {
		    totalExisting++;
		}
		if (currentRetrievableFile.destinationFile.exists() && !impExpUI.overwriteCheckBox.isSelected()) {
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
			impExpUI.xmlOutput.append(currentRetrievableFile.sourceURI.toString() + "\n");
			impExpUI.xmlOutput.append("destination path: " + currentRetrievableFile.destinationFile.getAbsolutePath());
			System.out.println("checkerResult: " + checkerResult);
			impExpUI.xmlOutput.append(checkerResult + "\n");
			impExpUI.xmlOutput.setCaretPosition(impExpUI.xmlOutput.getText().length() - 1);
			impExpUI.validationErrors.add(currentRetrievableFile.sourceURI);
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
	    impExpUI.metaDataCopyErrors.add(currentRetrievableFile.sourceURI);
	    impExpUI.appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI + " (" + ex.getMessage() + ")");
	} catch (MalformedURLException ex) {
	    BugCatcherManager.getBugCatcher().logError(currentRetrievableFile.sourceURI.toString(), ex);
	    totalErrors++;
	    impExpUI.metaDataCopyErrors.add(currentRetrievableFile.sourceURI);
	    impExpUI.appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI);
	    System.out.println("Error getting links from: " + currentRetrievableFile.sourceURI);
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError(currentRetrievableFile.sourceURI.toString(), ex);
	    totalErrors++;
	    impExpUI.metaDataCopyErrors.add(currentRetrievableFile.sourceURI);
	    impExpUI.appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI);
	}
	totalLoaded++;
	impExpUI.progressFoundLabel.setText(impExpUI.progressFoundLabelText + (getList.size() + totalLoaded));
	impExpUI.progressProcessedLabel.setText(impExpUI.progressProcessedLabelText + totalLoaded);
	impExpUI.progressAlreadyInCacheLabel.setText(impExpUI.progressAlreadyInCacheLabelText + totalExisting);
	impExpUI.progressFailedLabel.setText(impExpUI.progressFailedLabelText + totalErrors);
	impExpUI.progressXmlErrorsLabel.setText(impExpUI.progressXmlErrorsLabelText + xsdErrors);
	impExpUI.resourceCopyErrorsLabel.setText(impExpUI.resourceCopyErrorsLabelText + resourceCopyErrors);
	impExpUI.progressBar.setString(totalLoaded + "/" + (getList.size() + totalLoaded) + " (" + (totalErrors + xsdErrors + resourceCopyErrors) + " errors)");
	if (testFreeSpace) {
	    testFreeSpace();
	}
    }

    private void copyLinks(URI[] linksUriArray, Hashtable<URI, RetrievableFile> seenFiles, RetrievableFile currentRetrievableFile, ArrayList<URI> getList, ArrayList<URI[]> uncopiedLinks) throws MalformedURLException {
	for (int linkCount = 0; linkCount < linksUriArray.length && !impExpUI.stopCopy; linkCount++) {
	    System.out.println("Link: " + linksUriArray[linkCount].toString());
	    String currentLink = linksUriArray[linkCount].toString();
	    URI gettableLinkUri = linksUriArray[linkCount].normalize();
	    if (!seenFiles.containsKey(gettableLinkUri)) {
		seenFiles.put(gettableLinkUri, new RetrievableFile(gettableLinkUri, currentRetrievableFile.childDestinationDirectory));
	    }
	    RetrievableFile retrievableLink = seenFiles.get(gettableLinkUri);
	    if (MetadataFormat.isPathMetadata(currentLink)) {
		getList.add(gettableLinkUri);
		if (impExpUI.renameFileToNodeName.isSelected() && impExpUI.exportDestinationDirectory != null) {
		    retrievableLink.calculateTreeFileName(impExpUI.renameFileToLamusFriendlyName.isSelected());
		} else {
		    retrievableLink.calculateUriFileName();
		}
		uncopiedLinks.add(new URI[]{linksUriArray[linkCount], retrievableLink.destinationFile.toURI()});
	    } else {
		if (!impExpUI.copyFilesImportCheckBox.isSelected() && !impExpUI.copyFilesExportCheckBox.isSelected()) {
		    uncopiedLinks.add(new URI[]{linksUriArray[linkCount], linksUriArray[linkCount]});
		} else {
		    File downloadFileLocation;
		    if (impExpUI.exportDestinationDirectory == null) {
			downloadFileLocation = sessionStorage.updateCache(currentLink, impExpUI.shibbolethNegotiator, false, false, impExpUI.downloadAbortFlag, impExpUI.resourceProgressLabel);
		    } else {
			if (impExpUI.renameFileToNodeName.isSelected() && impExpUI.exportDestinationDirectory != null) {
			    retrievableLink.calculateTreeFileName(impExpUI.renameFileToLamusFriendlyName.isSelected());
			} else {
			    retrievableLink.calculateUriFileName();
			}
			if (!retrievableLink.destinationFile.getParentFile().exists()) {
			    if (!retrievableLink.destinationFile.getParentFile().mkdirs()) {
				BugCatcherManager.getBugCatcher().logError(new IOException("Could not create missing parent directory for " + retrievableLink.destinationFile));
			    }
			}
			downloadFileLocation = retrievableLink.destinationFile;
			impExpUI.resourceProgressLabel.setText(" ");
			sessionStorage.saveRemoteResource(new URL(currentLink), downloadFileLocation, impExpUI.shibbolethNegotiator, true, false, impExpUI.downloadAbortFlag, impExpUI.resourceProgressLabel);
			impExpUI.resourceProgressLabel.setText(" ");
		    }
		    if (downloadFileLocation != null && downloadFileLocation.exists()) {
			impExpUI.appendToTaskOutput("Downloaded resource: " + downloadFileLocation.getAbsolutePath());
			uncopiedLinks.add(new URI[]{linksUriArray[linkCount], downloadFileLocation.toURI()});
		    } else {
			impExpUI.resourceCopyOutput.append("Download failed: " + currentLink + " \n");
			impExpUI.fileCopyErrors.add(currentRetrievableFile.sourceURI);
			uncopiedLinks.add(new URI[]{linksUriArray[linkCount], linksUriArray[linkCount]});
			resourceCopyErrors++;
		    }
		    impExpUI.resourceCopyOutput.setCaretPosition(impExpUI.resourceCopyOutput.getText().length() - 1);
		}
	    }
	}
    }

    private void testFreeSpace() {
	try {
	    int freeGBytes = (int) (directoryForSizeTest.getFreeSpace() / 1073741824);
	    impExpUI.diskSpaceLabel.setText(impExpUI.diskFreeLabelText + freeGBytes + "GB");
	    if (freeGbWarningPoint > freeGBytes) {
		impExpUI.progressBar.setIndeterminate(false);
		if (JOptionPane.YES_OPTION == dialogHandler.showDialogBox("There is only " + freeGBytes + "GB free space left on the disk.\nTo you still want to continue?", impExpUI.importExportDialog.getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
		    freeGbWarningPoint = freeGBytes - 1;
		} else {
		    impExpUI.stopCopy = true;
		}
		impExpUI.progressBar.setIndeterminate(true);
	    }
	} catch (Exception ex) {
	    impExpUI.diskSpaceLabel.setText(impExpUI.diskFreeLabelText + "N/A");
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
