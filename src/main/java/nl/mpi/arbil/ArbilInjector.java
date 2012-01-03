package nl.mpi.arbil;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.clarin.profiles.ProfilePreview;
import nl.mpi.arbil.data.AbstractTreeHelper;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeArrayTableCell;
import nl.mpi.arbil.data.ArbilDataNodeTableCell;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.DocumentationLanguages;
import nl.mpi.arbil.data.FieldChangeTriggers;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.importexport.ArbilCsvImporter;
import nl.mpi.arbil.data.importexport.ArbilToHtmlConverter;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.data.metadatafile.CmdiUtils;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.search.ArbilRemoteSearch;
import nl.mpi.arbil.search.ArbilSearch;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import nl.mpi.arbil.ui.ArbilFieldPlaceHolder;
import nl.mpi.arbil.ui.ArbilFieldViews;
import nl.mpi.arbil.ui.ArbilHyperlinkListener;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.ImportExportDialog;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.wizard.setup.RemoteLocationsContent;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BinaryMetadataReader;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.arbil.util.XsdChecker;

/**
 * Takes care of injecting certain class instances into objects or classes.
 * This provides us with a sort of dependency injection, which enables loosening
 * the coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilInjector {

    protected synchronized void injectHandlers(
	    MessageDialogHandler messageDialogHandler,
	    WindowManager windowManager,
	    SessionStorage sessionStorage,
	    TreeHelper treeHelper,
	    DataNodeLoader dataNodeLoader,
	    BugCatcher bugCatcher,
	    ApplicationVersionManager versionManager,
	    ClipboardOwner clipboardOwner) {
	injectVersionManager(versionManager);
	injectDataNodeLoader(dataNodeLoader);
	injectSessionStorage(sessionStorage);
	injectWindowManager(windowManager);
	injectDialogHandler(messageDialogHandler);
	injectTreeHelper(treeHelper);
	injectBugCatcher(bugCatcher);
    }
    
    public void injectVersionManager(ApplicationVersionManager versionManager){
	ArbilBugCatcher.setVersionManager(versionManager);
	ArbilWindowManager.setVersionManager(versionManager);
	ArbilIcons.setVersionManager(versionManager);
	ArbilMenuBar.setVersionManager(versionManager);
	ArbilTableModel.setVersionManager(versionManager);
	MetadataReader.setVersionManager(versionManager);
    }
    
    public void injectDataNodeLoader(DataNodeLoader dataNodeLoader){
	AbstractTreeHelper.setDataNodeLoader(dataNodeLoader);
	ArbilComponentBuilder.setDataNodeLoader(dataNodeLoader);
	ArbilCsvImporter.setDataNodeLoader(dataNodeLoader);
	ArbilDataNode.setDataNodeLoader(dataNodeLoader);
	ArbilDataNodeTableCell.setDataNodeLoader(dataNodeLoader);
	ArbilDataNodeArrayTableCell.setDataNodeLoader(dataNodeLoader);
	ArbilField.setDataNodeLoader(dataNodeLoader);
	ArbilFieldPlaceHolder.setDataNodeLoader(dataNodeLoader);
	ArbilSearch.setDataNodeLoader(dataNodeLoader);
	ImportExportDialog.setDataNodeLoader(dataNodeLoader);
	MetadataBuilder.setDataNodeLoader(dataNodeLoader);
	MetadataReader.setDataNodeLoader(dataNodeLoader);
	ProfilePreview.setDataNodeLoader(dataNodeLoader);
    }
    
    public void injectMimeHashQueue(MimeHashQueue mimeHashQueue){
	ArbilDataNode.setMimeHashQueue(mimeHashQueue);
    }

    public void injectBugCatcher(BugCatcher bugCatcher) {
	// Inject bug catcher
	ArbilComponentBuilder.setBugCatcher(bugCatcher);
	ArbilCsvImporter.setBugCatcher(bugCatcher);
	ArbilDataNode.setBugCatcher(bugCatcher);
	ArbilEntityResolver.setBugCatcher(bugCatcher);
	ArbilFavourites.setBugCatcher(bugCatcher);
	ArbilFieldViews.setBugCatcher(bugCatcher);
	ArbilIcons.setBugCatcher(bugCatcher);
	ArbilJournal.setBugCatcher(bugCatcher);
	AbstractArbilTableModel.setBugCatcher(bugCatcher);
	ArbilTemplate.setBugCatcher(bugCatcher);
	ArbilTemplateManager.setBugCatcher(bugCatcher);
	ArbilToHtmlConverter.setBugCatcher(bugCatcher);
	ArbilTree.setBugCatcher(bugCatcher);
	AbstractTreeHelper.setBugCatcher(bugCatcher);
	ApplicationVersionManager.setBugCatcher(bugCatcher);
	ArbilVocabularies.setBugCatcher(bugCatcher);
	BinaryMetadataReader.setBugCatcher(bugCatcher);
	CmdiComponentLinkReader.setBugCatcher(bugCatcher);
	CmdiProfileReader.setBugCatcher(bugCatcher);
	CmdiTemplate.setBugCatcher(bugCatcher);
	CmdiUtils.setBugCatcher(bugCatcher);
	DocumentationLanguages.setBugCatcher(bugCatcher);
	ImdiUtils.setBugCatcher(bugCatcher);
	MetadataBuilder.setBugCatcher(bugCatcher);
	MetadataReader.setBugCatcher(bugCatcher);
	ShibbolethNegotiator.setBugCatcher(bugCatcher);
	XsdChecker.setBugCatcher(bugCatcher);
	
	RemoteLocationsContent.setBugCatcher(bugCatcher);
	ArbilRemoteSearch.setBugCatcher(bugCatcher);
    }

    public void injectTreeHelper(TreeHelper treeHelper) {
	//Inject tree helper
	ArbilDataNode.setTreeHelper(treeHelper);
	ArbilFavourites.setTreeHelper(treeHelper);
	ArbilTree.setTreeHelper(treeHelper);
	ImportExportDialog.setTreeHelper(treeHelper);
	MetadataBuilder.setTreeHelper(treeHelper);
	RemoteLocationsContent.setTreeHelper(treeHelper);
	ArbilHyperlinkListener.setTreeHelper(treeHelper);
    }

    public void injectDialogHandler(MessageDialogHandler messageDialogHandler) {
	// Inject message dialog handler
	ArbilComponentBuilder.setMessageDialogHandler(messageDialogHandler);
	ArbilCsvImporter.setMessageDialogHandler(messageDialogHandler);
	ArbilDataNode.setMessageDialogHandler(messageDialogHandler);
	ArbilFavourites.setMessageDialogHandler(messageDialogHandler);
	ArbilJournal.setMessageDialogHandler(messageDialogHandler);
	ArbilTableModel.setMessageDialogHandler(messageDialogHandler);
	ArbilTemplate.setMessageDialogHandler(messageDialogHandler);
	ArbilToHtmlConverter.setMessageDialogHandler(messageDialogHandler);
	AbstractTreeHelper.setMessageDialogHandler(messageDialogHandler);
	ArbilTreeHelper.setMessageDialogHandler(messageDialogHandler);
	ApplicationVersionManager.setMessageDialogHandler(messageDialogHandler);
	ArbilVocabularies.setMessageDialogHandler(messageDialogHandler);
	CmdiTemplate.setMessageDialogHandler(messageDialogHandler);
	FieldChangeTriggers.setMessageDialogHandler(messageDialogHandler);
	ImdiUtils.setMessageDialogHandler(messageDialogHandler);
	MetadataBuilder.setMessageDialogHandler(messageDialogHandler);
	MetadataReader.setMessageDialogHandler(messageDialogHandler);
	ShibbolethNegotiator.setMessageDialogHandler(messageDialogHandler);
    }

    public void injectWindowManager(WindowManager windowManager) {
	// Inject window manager
	ArbilBugCatcher.setWindowManager(windowManager);
	ArbilTree.setWindowManager(windowManager);
	ArbilVocabularies.setWindowManager(windowManager);
	MetadataBuilder.setWindowManager(windowManager);
    }

    public void injectSessionStorage(SessionStorage sessionStorage) {
	ArbilComponentBuilder.setSessionStorage(sessionStorage);
	ArbilDataNode.setSessionStorage(sessionStorage);
	ArbilEntityResolver.setSessionStorage(sessionStorage);
	ArbilField.setSessionStorage(sessionStorage);
	ArbilJournal.setSessionStorage(sessionStorage);
	ArbilVocabularies.setSessionStorage(sessionStorage);
	DocumentationLanguages.setSessionStorage(sessionStorage);
	MetadataBuilder.setSessionStorage(sessionStorage);
	MetadataReader.setSessionStorage(sessionStorage);
	CmdiProfileReader.setSessionStorage(sessionStorage);
	ProfilePreview.setSessionStorage(sessionStorage);
	CmdiTemplate.setSessionStorage(sessionStorage);
	ArbilFavourites.setSessionStorage(sessionStorage);
	XsdChecker.setSessionStorage(sessionStorage);
	ApplicationVersionManager.setSessionStorage(sessionStorage);
	ArbilTemplateManager.setSessionStorage(sessionStorage);
    }
}
