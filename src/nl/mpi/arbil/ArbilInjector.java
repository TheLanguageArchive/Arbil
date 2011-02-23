/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil;

import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.data.FieldChangeTriggers;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.TreeHelper;
import nl.mpi.arbil.data.importexport.ArbilCsvImporter;
import nl.mpi.arbil.data.importexport.ArbilToHtmlConverter;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.data.metadatafile.CmdiUtils;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilInjector {

    private final static MessageDialogHandler messageDialogHandler = ArbilWindowManager.getSingleInstance();
    private final static BugCatcher bugCatcher = GuiHelper.linorgBugCatcher;

    public static void injectHandlers() {
        
        // Inject message dialog handler
        ArbilDataNode.setMessageDialogHandler(messageDialogHandler);
        ArbilComponentBuilder.setMessageDialogHandler(messageDialogHandler);
        ArbilJournal.setMessageDialogHandler(messageDialogHandler);
        ArbilVocabularies.setMessageDialogHandler(messageDialogHandler);
        MetadataBuilder.setMessageDialogHandler(messageDialogHandler);
        FieldChangeTriggers.setMessageDialogHandler(messageDialogHandler);
        TreeHelper.setMessageDialogHandler(messageDialogHandler);
        MetadataReader.setMessageDialogHandler(messageDialogHandler);
        ImdiUtils.setMessageDialogHandler(messageDialogHandler);
        ArbilCsvImporter.setMessageDialogHandler(messageDialogHandler);
        ArbilToHtmlConverter.setMessageDialogHandler(messageDialogHandler);
        ShibbolethNegotiator.setMessageDialogHandler(messageDialogHandler);
        CmdiTemplate.setMessageDialogHandler(messageDialogHandler);
        ArbilFavourites.setMessageDialogHandler(messageDialogHandler);
        ArbilTemplate.setMessageDialogHandler(messageDialogHandler);
        ArbilSessionStorage.setMessageDialogHandler(messageDialogHandler);

        // Inject bug catcher
        ArbilDataNode.setBugCatcher(bugCatcher);
        ArbilComponentBuilder.setBugCatcher(bugCatcher);
        ArbilJournal.setBugCatcher(bugCatcher);
        ArbilVocabularies.setBugCatcher(bugCatcher);
        MetadataBuilder.setBugCatcher(bugCatcher);
        TreeHelper.setBugCatcher(bugCatcher);
        ArbilCsvImporter.setBugCatcher(bugCatcher);
        ArbilToHtmlConverter.setBugCatcher(bugCatcher);
        ShibbolethNegotiator.setBugCatcher(bugCatcher);
        CmdiUtils.setBugCatcher(bugCatcher);
        ImdiUtils.setBugCatcher(bugCatcher);
        MetadataReader.setBugCatcher(bugCatcher);
        ArbilEntityResolver.setBugCatcher(bugCatcher);
        CmdiTemplate.setBugCatcher(bugCatcher);
        CmdiComponentLinkReader.setBugCatcher(bugCatcher);
        ArbilFavourites.setBugCatcher(bugCatcher);
        ArbilTemplate.setBugCatcher(bugCatcher);
        ArbilTemplateManager.setBugCatcher(bugCatcher);
        ArbilSessionStorage.setBugCatcher(bugCatcher);
    }
}
