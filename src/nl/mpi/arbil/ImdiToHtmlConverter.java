package nl.mpi.arbil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : ImdiToHtmlConverter
 *  Created on : Apr 15, 2010, 1:33:02 PM
 *  Author     : Peter Withers
 */
public class ImdiToHtmlConverter {

    public void exportImdiToHtml(ImdiTreeObject[] inputImdiArray) {
        File destinationDirectory = LinorgWindowManager.getSingleInstance().showEmptyExportDirectoryDialogue("Export HTML");
        if (destinationDirectory != null) {
            copyDependancies(destinationDirectory, false);
            for (ImdiTreeObject currentImdi : inputImdiArray) {
                File destinationFile = new File(destinationDirectory, currentImdi.toString() + ".html");
                int fileCounter = 1;
                while (destinationFile.exists()) {
                    destinationFile = new File(destinationDirectory, currentImdi.toString() + "(" + fileCounter + ").html");
                }
                try {
                    transformImdiToHtml(currentImdi, destinationFile);
                } catch (Exception exception) {
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot convert IMDI", "HTML Export");
                    GuiHelper.linorgBugCatcher.logError(exception);
                }
            }
        }
        GuiHelper.getSingleInstance().openFileInExternalApplication(destinationDirectory.toURI());
    }

    public File convertToHtml(ImdiTreeObject inputImdi) throws IOException, TransformerException {
        File tempHtmlFile;
        tempHtmlFile = File.createTempFile("tmp", ".html");
        tempHtmlFile.deleteOnExit();
        copyDependancies(tempHtmlFile.getParentFile(), true);
        transformImdiToHtml(inputImdi, tempHtmlFile);
        return tempHtmlFile;
    }

    private void transformImdiToHtml(ImdiTreeObject inputImdi, File destinationFile) throws IOException, TransformerException {
        // 1. Instantiate a TransformerFactory.
        javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
        // 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
        URL xslUrl = this.getClass().getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl");
        // look in the current template for a custom xsl
        File xslFile = null;
        xslFile = new File(inputImdi.getNodeTemplate().getTemplateDirectory(), "format.xsl");
        if (xslFile != null && xslFile.exists()) {
            xslUrl = xslFile.toURL();
        }
        javax.xml.transform.Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xslUrl.toString()));
        // 3. Use the Transformer to transform an XML Source and send the output to a Result object.
        transformer.transform(new javax.xml.transform.stream.StreamSource(inputImdi.getURI().toString()), new javax.xml.transform.stream.StreamResult(new java.io.FileOutputStream(destinationFile.getCanonicalPath())));
    }

    private void copyDependancies(File destinationDirectory, boolean deleteOnExit) {
        // copy any dependent files from the jar
        String[] dependentFiles = {"imdi-viewer-open.gif", "imdi-viewer-closed.gif", "imdi-viewer.js", "additTooltip.js", "additPopup.js", "imdi-viewer.css", "additTooltip.css"};

        for (String dependantFileString : dependentFiles) {
            File tempDependantFile = new File(destinationDirectory, dependantFileString);
            if (deleteOnExit) {
                tempDependantFile.deleteOnExit();
            }
            try {
                FileOutputStream outFile = new FileOutputStream(tempDependantFile);
                //InputStream inputStream = this.getClass().getResourceAsStream("html/imdi-viewer/" + dependantFileString);
                InputStream inputStream = this.getClass().getResourceAsStream("/nl/mpi/arbil/resources/xsl/" + dependantFileString);
                if(inputStream == null){
                    GuiHelper.linorgBugCatcher.logError(new Exception("Inputstream null for " + dependantFileString));
                } else{
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
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot copy requisite file", "HTML Export");
                GuiHelper.linorgBugCatcher.logError(iOException);
            }
        }
    }
}
