package nl.mpi.arbil.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.data.*;

/**
 * Document   : ArbilCsvImporter
 * Created on : Nov 16, 2009, 10:34:47 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilCsvImporter {

    ImdiTreeObject destinationCorpusNode;

    public ArbilCsvImporter(ImdiTreeObject destinationCorpusNodeLocal) {
        destinationCorpusNode = destinationCorpusNodeLocal;
    }

    public void doImport() {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter csvFileFilter = new FileFilter() {

            @Override
            public String getDescription() {
                return "CSV File (comma or tab separated values)";
            }

            @Override
            public boolean accept(File selectedFile) {
                return selectedFile.getName().toLowerCase().endsWith(".csv");
            }
        };
        fileChooser.addChoosableFileFilter(csvFileFilter);
        fileChooser.setMultiSelectionEnabled(false);
        if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Import CSV")) {
            processCsvFile(fileChooser.getSelectedFile());
        }
    }

    private void cleanQuotes(String[] arrayToClean, String fileType) {
        if (arrayToClean.length > 0) {
            if (fileType.indexOf("\"") != -1) {
                arrayToClean[0] = arrayToClean[0].replaceAll("^\"", "");
                arrayToClean[arrayToClean.length - 1] = arrayToClean[arrayToClean.length - 1].replaceAll("\"$", "");
            }
        }
    }

    private void processCsvFile(File inputFile) {
        String csvHeaders[] = null;
        String fileType = ",";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            String currentLine = "";
            String remainderOfLastLine = "";
            StringTokenizer stringTokeniser = null;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (csvHeaders == null) {
                    fileType = "\"\\t\"";
                    csvHeaders = currentLine.split(fileType);
                    if (csvHeaders.length == 1) {
                        fileType = "\t";
                        csvHeaders = currentLine.split(fileType);
                    }
                    if (csvHeaders.length == 1) {
                        fileType = "\",\"";
                        csvHeaders = currentLine.split(fileType);
                    }
                    if (csvHeaders.length == 1) {
                        fileType = ",";
                        csvHeaders = currentLine.split(fileType);
                    }
                    cleanQuotes(csvHeaders, fileType);
                } else {
                    boolean skipLine = false;
                    if (fileType.contains("\"")) {
                        // some fields will contain line breaks and they, must be reassembled here
                        // but this can only be done if the file contains quotes around each feild
                        if (!currentLine.endsWith("\"")) {
                            remainderOfLastLine = remainderOfLastLine + "\n" + currentLine;
                            skipLine = true;
                        } else if (remainderOfLastLine.length() > 0) {
                            currentLine = remainderOfLastLine + "\n" + currentLine;
                            remainderOfLastLine = "";
                        }
                    }
                    if (!skipLine) {
                        String nodeType = ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session";
                        ImdiTreeObject addedImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, destinationCorpusNode.addChildNode(nodeType, null, null, null));
                        addedImdiObject.waitTillLoaded();
                        Hashtable<String, ImdiField[]> addedNodesFields = addedImdiObject.getFields();
                        String[] currentLineArray = currentLine.split(fileType);
                        cleanQuotes(currentLineArray, fileType);
                        for (int columnCounter = 0; columnCounter < csvHeaders.length && columnCounter < currentLineArray.length; columnCounter++) {
                            System.out.println(csvHeaders[columnCounter] + " : " + currentLineArray[columnCounter]);
                            ImdiField[] currentFieldArray = addedNodesFields.get(csvHeaders[columnCounter]);
                            if (currentFieldArray != null) {
                                // TODO: check that the field does not already have a value and act accordingly (add new description?) if it does
                                currentFieldArray[0].setFieldValue(currentLineArray[columnCounter], false, true);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }
}
