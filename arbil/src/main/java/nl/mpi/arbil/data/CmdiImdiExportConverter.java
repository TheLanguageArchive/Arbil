/**
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ui.ImportExportUI;
import nl.mpi.arbil.util.XsdChecker;
import nl.mpi.arbilcommons.journal.ArbilJournal;
import nl.mpi.translation.tools.Translator;
import nl.mpi.translation.tools.TranslatorImpl;
import nl.mpi.translation.tools.UrlStreamResolverImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiImdiExportConverter {

    private final static Logger logger = LoggerFactory.getLogger(CmdiImdiExportConverter.class);
    private final XsdChecker xsdChecker = new XsdChecker();

    private final File root;
    private final ImportExportUI ui;
    private final Translator translator;

    private String fatalError = null;
    private int fileCount = 0;
    private int successCount = 0;
    private int failureCount = 0;
    private int validationErrorCount = 0;

    public CmdiImdiExportConverter(ImportExportUI ui, File directory) {
        this.root = directory;
        this.ui = ui;
        translator = newTranslator(ui);
    }

    public void run() {
        fatalError = null;
        if (translator != null) {
            convertDir(root);
        } else {
            logger.error("No translator instance available, could not transform to CMDI");
            fatalError = "Conversion could not be carried out! See error log for details.";
        }
    }

    public CharSequence getResultMessage() {
        final StringBuilder sb = new StringBuilder();
        if (fatalError != null) {
            sb.append(fatalError).append(" ");
        }
        if (successCount > 0) {
            sb.append(String.format("Converted %d files to CMDI. ", successCount));
        }
        if (failureCount > 0) {
            sb.append(String.format("\nFailed to convert %d files. Choose 'Details' to get more information. ", failureCount));
        }
        return sb;
    }

    /**
     * Converts IMDIs in a directory and its child directories (recursively)
     *
     * @param dir directory to convert in
     */
    private void convertDir(File dir) {
        final File[] imdiFiles = dir.listFiles(new ImdiFilter());
        fileCount += imdiFiles.length;
        for (File imdiFile : imdiFiles) {
            if (ui.isStopCopy()) {
                return;
            } else {
                convertToCmdi(imdiFile);
                ui.updateConversionStatus(fileCount, successCount + failureCount, failureCount, validationErrorCount);
            }
        }

        final File[] directories = dir.listFiles(new DirectoryFilter());
        for (File directory : directories) {
            convertDir(directory);
        }
    }

    /**
     * Replaces a single file with a CMDI equivalent
     *
     * @param imdiFile
     */
    private void convertToCmdi(File imdiFile) {
        // Determine output file name
        final File cmdiFile = new File(imdiFile.getAbsolutePath().replaceAll(".imdi$", ".cmdi"));
        if (imdiFile.equals(cmdiFile)) {
            // file extension not changed, original does not end in .imdi
            logger.error("Could not convert {} to CMDI: unexpected file extension", imdiFile);
            ui.addToMetadataCopyErrors(imdiFile.toURI());
            ui.appendToTaskOutput("Failed to convert to CMDI: " + imdiFile.getAbsolutePath());
            return;
        }

        if (!cmdiFile.exists() || ui.askOverwrite(cmdiFile.toURI())) {
            // Perform conversion
            logger.debug("Converting {} to {}", imdiFile, cmdiFile);
            if (applyXslt(imdiFile, cmdiFile)) {
                validateOutput(cmdiFile, imdiFile);
            }
        }

        // Delete original
        logger.debug("Conversion complete, deleting original IMDI {}", imdiFile);
        imdiFile.delete();
    }

    private void validateOutput(final File cmdiFile, File imdiFile) {
        final String checkerResult = xsdChecker.simpleCheck(cmdiFile);
        if (checkerResult != null) {
            ui.appendToXmlOutput(imdiFile.toString() + "\n");
            ui.appendToXmlOutput("destination path: " + cmdiFile.getAbsolutePath());
            logger.debug("checkerResult: {}", checkerResult);
            ui.appendToXmlOutput(checkerResult + "\n");
            ui.addToValidationErrors(imdiFile.toURI());
            validationErrorCount++;
        }
    }

    private boolean applyXslt(File source, File result) {
        try {
            final String cmdiOut = translator.getCMDI(source.toURI().toURL(), null);
            final Writer fw = new OutputStreamWriter(new FileOutputStream(result), "UTF-8");
            try {
                fw.write(cmdiOut);
            } finally {
                fw.close();
            }
            successCount++;
            ArbilJournal.getSingleInstance().saveJournalEntry(result.getAbsolutePath(), "", source.getAbsolutePath(), "", "conversion");
            return true;
        } catch (IOException ex) {
            handleTransformError(source, ex);
        } catch (TransformerException ex) {
            handleTransformError(source, ex);
        } catch (XMLStreamException ex) {
            handleTransformError(source, ex);
        }
        return false;
    }

    private void handleTransformError(File source, Exception ex) {
        ui.addToMetadataCopyErrors(source.toURI());
        ui.appendToTaskOutput("Failed to convert file to CMDI: " + source.getAbsolutePath());
        failureCount++;
    }

    private static class DirectoryFilter implements FileFilter {

        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }

    private static class ImdiFilter implements FileFilter {

        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(".imdi");
        }

    }

    private static TranslatorImpl newTranslator(ImportExportUI ui) {
        try {
            return new TranslatorImpl(new UrlStreamResolverImpl());
        } catch (IOException ex) {
            logger.error("Could not instantiate metadata translator", ex);
        } catch (TransformerConfigurationException ex) {
            logger.error("Could not instantiate metadata translator", ex);
        }
        ui.appendToTaskOutput("Could not transform to IMDI, failed to instantiate transformer");
        return null;
    }
}
