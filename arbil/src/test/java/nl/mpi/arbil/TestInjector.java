package nl.mpi.arbil;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.clarin.profiles.ProfilePreview;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.data.DocumentationLanguages;
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
import nl.mpi.arbil.ui.ArbilFieldViews;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ArbilVersionChecker;
import nl.mpi.arbil.util.BinaryMetadataReader;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.arbil.util.XsdChecker;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class TestInjector {

    public static synchronized void injectHandlers(SessionStorage sessionStorage, BugCatcher bugCatcher, MessageDialogHandler messageDialogHandler, WindowManager windowManager, ClipboardOwner clipboardOwner) {
	if (sessionStorage != null) {
	    ArbilComponentBuilder.setSessionStorage(sessionStorage);
	    ArbilDataNode.setSessionStorage(sessionStorage);
	    ArbilDataNodeLoader.setSessionStorage(sessionStorage);
	    ArbilEntityResolver.setSessionStorage(sessionStorage);
	    ArbilField.setSessionStorage(sessionStorage);
	    ArbilJournal.setSessionStorage(sessionStorage);
	    ArbilVocabularies.setSessionStorage(sessionStorage);
	    DocumentationLanguages.setSessionStorage(sessionStorage);
	    MetadataBuilder.setSessionStorage(sessionStorage);
	    TreeHelper.setSessionStorage(sessionStorage);
	    MetadataReader.setSessionStorage(sessionStorage);
	    CmdiProfileReader.setSessionStorage(sessionStorage);
	    ProfilePreview.setSessionStorage(sessionStorage);
	    CmdiTemplate.setSessionStorage(sessionStorage);
	    ArbilFavourites.setSessionStorage(sessionStorage);
	    MimeHashQueue.setSessionStorage(sessionStorage);
	    XsdChecker.setSessionStorage(sessionStorage);
	    ArbilVersionChecker.setSessionStorage(sessionStorage);
	    ArbilTemplateManager.setSessionStorage(sessionStorage);
	}

	if (windowManager != null) {
	    // Inject window manager
	    //ArbilBugCatcher.setWindowManager(windowManager);
	    ArbilTree.setWindowManager(windowManager);
	    ArbilVocabularies.setWindowManager(windowManager);
	    MetadataBuilder.setWindowManager(windowManager);
	}

	if (messageDialogHandler != null) {
	    // Inject message dialog handler
	    ArbilComponentBuilder.setMessageDialogHandler(messageDialogHandler);
	    ArbilCsvImporter.setMessageDialogHandler(messageDialogHandler);
	    ArbilDataNode.setMessageDialogHandler(messageDialogHandler);
	    ArbilFavourites.setMessageDialogHandler(messageDialogHandler);
	    ArbilJournal.setMessageDialogHandler(messageDialogHandler);
	    ArbilTableModel.setMessageDialogHandler(messageDialogHandler);
	    ArbilTemplate.setMessageDialogHandler(messageDialogHandler);
	    ArbilToHtmlConverter.setMessageDialogHandler(messageDialogHandler);
	    ArbilVersionChecker.setMessageDialogHandler(messageDialogHandler);
	    ArbilVocabularies.setMessageDialogHandler(messageDialogHandler);
	    CmdiTemplate.setMessageDialogHandler(messageDialogHandler);
	    FieldChangeTriggers.setMessageDialogHandler(messageDialogHandler);
	    ImdiUtils.setMessageDialogHandler(messageDialogHandler);
	    MetadataBuilder.setMessageDialogHandler(messageDialogHandler);
	    MetadataReader.setMessageDialogHandler(messageDialogHandler);
	    ShibbolethNegotiator.setMessageDialogHandler(messageDialogHandler);
	    TreeHelper.setMessageDialogHandler(messageDialogHandler);
	}

	if (bugCatcher != null) {
	    // Inject bug catcher
	    ArbilComponentBuilder.setBugCatcher(bugCatcher);
	    ArbilCsvImporter.setBugCatcher(bugCatcher);
	    ArbilDataNode.setBugCatcher(bugCatcher);
	    ArbilEntityResolver.setBugCatcher(bugCatcher);
	    ArbilFavourites.setBugCatcher(bugCatcher);
	    ArbilFieldViews.setBugCatcher(bugCatcher);
	    ArbilIcons.setBugCatcher(bugCatcher);
	    ArbilJournal.setBugCatcher(bugCatcher);
	    ArbilTableModel.setBugCatcher(bugCatcher);
	    ArbilTemplate.setBugCatcher(bugCatcher);
	    ArbilTemplateManager.setBugCatcher(bugCatcher);
	    ArbilToHtmlConverter.setBugCatcher(bugCatcher);
	    ArbilTree.setBugCatcher(bugCatcher);
	    ArbilVersionChecker.setBugCatcher(bugCatcher);
	    ArbilVocabularies.setBugCatcher(bugCatcher);
	    BinaryMetadataReader.setBugCatcher(bugCatcher);
	    CmdiComponentLinkReader.setBugCatcher(bugCatcher);
	    CmdiTemplate.setBugCatcher(bugCatcher);
	    CmdiUtils.setBugCatcher(bugCatcher);
	    DocumentationLanguages.setBugCatcher(bugCatcher);
	    ImdiUtils.setBugCatcher(bugCatcher);
	    MetadataBuilder.setBugCatcher(bugCatcher);
	    MetadataReader.setBugCatcher(bugCatcher);
	    MimeHashQueue.setBugCatcher(bugCatcher);
	    ShibbolethNegotiator.setBugCatcher(bugCatcher);
	    TreeHelper.setBugCatcher(bugCatcher);
	    XsdChecker.setBugCatcher(bugCatcher);
	}

	if (clipboardOwner != null) {
	    // Clipboard owner
	    ArbilTree.setClipboardOwner(clipboardOwner);
	    ArbilTableModel.setClipboardOwner(clipboardOwner);
	}
    }
}
