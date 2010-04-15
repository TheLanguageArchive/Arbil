package nl.mpi.arbil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ImdiSchema;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : ImdiToHtmlConverter
 *  Created on : Apr 15, 2010, 1:33:02 PM
 *  Author     : Peter Withers
 */
public class ImdiToHtmlConverter {

    public void exportImdiToHtml(ImdiTreeObject[] inputImdiArray){
        
    }

    public File convertToHtml(ImdiTreeObject inputImdi) throws IOException, TransformerException {
        URI nodeUri = inputImdi.getURI();
        // 1. Instantiate a TransformerFactory.
        javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
        // 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
        URL xslUrl = this.getClass().getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl");
        File tempHtmlFile;
        File xslFile = null;
        if (ImdiSchema.getSingleInstance().selectedTemplateDirectory != null) {
            xslFile = new File(ImdiSchema.getSingleInstance().selectedTemplateDirectory.toString() + File.separatorChar + "format.xsl");
        }
        if (xslFile != null && xslFile.exists()) {
            xslUrl = xslFile.toURL();
            tempHtmlFile = File.createTempFile("tmp", ".html", xslFile.getParentFile());
            tempHtmlFile.deleteOnExit();
        } else {
            // copy any dependent files from the jar
            String[] dependentFiles = {"imdi-viewer-open.gif", "imdi-viewer-closed.gif", "imdi-viewer.js", "additTooltip.js", "additPopup.js", "imdi-viewer.css", "additTooltip.css"};
            tempHtmlFile = File.createTempFile("tmp", ".html");
            tempHtmlFile.deleteOnExit();
            for (String dependantFileString : dependentFiles) {
                File tempDependantFile = new File(tempHtmlFile.getParent() + File.separatorChar + dependantFileString);
                tempDependantFile.deleteOnExit();
//                        File tempDependantFile = File.createTempFile(dependantFileString, "");
                FileOutputStream outFile = new FileOutputStream(tempDependantFile);
                //InputStream inputStream = this.getClass().getResourceAsStream("html/imdi-viewer/" + dependantFileString);
                InputStream inputStream = this.getClass().getResourceAsStream("/nl/mpi/arbil/resources/xsl/" + dependantFileString);
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
                outFile.close();
            }
        }
        javax.xml.transform.Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xslUrl.toString()));
        // 3. Use the Transformer to transform an XML Source and send the output to a Result object.
        transformer.transform(new javax.xml.transform.stream.StreamSource(nodeUri.toString()), new javax.xml.transform.stream.StreamResult(new java.io.FileOutputStream(tempHtmlFile.getCanonicalPath())));
        return tempHtmlFile;
    }
}
