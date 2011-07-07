package nl.mpi.arbil.data.importexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 *  Document   : ArbilToHtmlConverter
 *  Created on : Apr 15, 2010, 1:33:02 PM
 *  Author     : Peter Withers
 */
public class ArbilToHtmlConverter {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
    }

    public URI exportImdiToHtml(ArbilDataNode[] inputNodeArray) {
	File destinationDirectory = messageDialogHandler.showEmptyExportDirectoryDialogue("Export HTML");
	if (destinationDirectory == null) {
	    return null;
	} else {
	    copyDependancies(destinationDirectory, false);
	    for (ArbilDataNode currentNode : inputNodeArray) {
		File destinationFile = new File(destinationDirectory, currentNode.toString() + ".html");
		int fileCounter = 1;
		while (destinationFile.exists()) {
		    destinationFile = new File(destinationDirectory, currentNode.toString() + "(" + fileCounter + ").html");
		}
		try {
		    transformNodeToHtml(currentNode, destinationFile);
		} catch (Exception exception) {
		    messageDialogHandler.addMessageDialogToQueue("Cannot convert data", "HTML Export");
		    bugCatcher.logError(exception);
		}
	    }
	    return destinationDirectory.toURI();
	}
    }

    public File convertToHtml(ArbilDataNode inputNode) throws IOException, TransformerException {
	File tempHtmlFile;
	tempHtmlFile = File.createTempFile("tmp", ".html");
	tempHtmlFile.deleteOnExit();
	copyDependancies(tempHtmlFile.getParentFile(), true);
	transformNodeToHtml(inputNode, tempHtmlFile);
	return tempHtmlFile;
    }

    private void transformNodeToHtml(ArbilDataNode inputNode, File destinationFile) throws IOException, TransformerException {
	// 1. Instantiate a TransformerFactory.
	javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
	// 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
	URL xslUrl = this.getClass().getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl");
	// look in the current template for a custom xsl
	File xslFile = null;
	xslFile = new File(inputNode.getNodeTemplate().getTemplateDirectory(), "format.xsl");
	if (xslFile != null && xslFile.exists()) {
	    xslUrl = xslFile.toURL();
	}
	javax.xml.transform.Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xslUrl.toString()));
	// 3. Use the Transformer to transform an XML Source and send the output to a Result object.
	transformer.transform(new javax.xml.transform.stream.StreamSource(inputNode.getURI().toString()), new javax.xml.transform.stream.StreamResult(new java.io.FileOutputStream(destinationFile.getCanonicalPath())));
    }

    private void copyDependancies(File destinationDirectory, boolean deleteOnExit) {
	// copy any dependent files from the jar
	String[] dependentFiles = {"imdi-viewer-open.gif", "imdi-viewer-closed.gif", "imdi-viewer.js", "imdi-viewer.css"};
//"additTooltip.js", "additPopup.js", , "additTooltip.css"
	for (String dependantFileString : dependentFiles) {
	    File tempDependantFile = new File(destinationDirectory, dependantFileString);
	    if (deleteOnExit) {
		tempDependantFile.deleteOnExit();
	    }
	    try {
		FileOutputStream outFile = new FileOutputStream(tempDependantFile);
		//InputStream inputStream = this.getClass().getResourceAsStream("html/imdi-viewer/" + dependantFileString);
		InputStream inputStream = this.getClass().getResourceAsStream("/nl/mpi/arbil/resources/xsl/" + dependantFileString);
		if (inputStream == null) {
		    bugCatcher.logError(new Exception("Missing file in jar: " + dependantFileString));
		} else {
		    int bufferLength = 1024 * 4;
		    byte[] buffer = new byte[bufferLength]; // make htis 1024*4 or something and read chunks not the whole file
		    int bytesread = 0;
		    while (bytesread >= 0) {
			bytesread = inputStream.read(buffer);
			if (bytesread == -1) {
			    break;
			}
			outFile.write(buffer, 0, bytesread);
		    }
		    inputStream.close();
		}
		outFile.close();
	    } catch (IOException iOException) {
		messageDialogHandler.addMessageDialogToQueue("Cannot copy requisite file", "HTML Export");
		bugCatcher.logError(iOException);
	    }
	}
    }
}
