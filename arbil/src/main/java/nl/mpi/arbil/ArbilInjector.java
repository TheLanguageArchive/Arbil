package nl.mpi.arbil;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.clarin.profiles.ProfilePreview;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNodeArrayTableCell;
import nl.mpi.arbil.data.ArbilDataNodeTableCell;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.DocumentationLanguages;
import nl.mpi.arbil.data.FieldChangeTriggers;
import nl.mpi.arbil.data.ImdiMetadataBuilder;
import nl.mpi.arbil.data.importexport.ArbilCsvImporter;
import nl.mpi.arbil.data.importexport.ArbilToHtmlConverter;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.search.ArbilSearch;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.templates.ImdiTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.ArbilFieldPlaceHolder;
import nl.mpi.arbil.ui.ArbilHyperlinkListener;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ImportExportDialog;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.wizard.setup.RemoteLocationsContent;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.MessageDialogHandler;
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
	    ApplicationVersionManager versionManager,
	    ClipboardOwner clipboardOwner) {
	injectVersionManager(versionManager);
	injectDataNodeLoader(dataNodeLoader);
	injectSessionStorage(sessionStorage);
	injectWindowManager(windowManager);
	injectDialogHandler(messageDialogHandler);
	injectTreeHelper(treeHelper);
    }

    public void injectVersionManager(ApplicationVersionManager versionManager) {
	ArbilIcons.setVersionManager(versionManager);
	ArbilMenuBar.setVersionManager(versionManager);
	ArbilTableModel.setVersionManager(versionManager);
	ImdiMetadataBuilder.setVersionManager(versionManager);
    }

    public void injectDataNodeLoader(DataNodeLoader dataNodeLoader) {
	ArbilComponentBuilder.setDataNodeLoader(dataNodeLoader);
	ArbilCsvImporter.setDataNodeLoader(dataNodeLoader);
	ArbilDataNodeTableCell.setDataNodeLoader(dataNodeLoader);
	ArbilDataNodeArrayTableCell.setDataNodeLoader(dataNodeLoader);
	ArbilField.setDataNodeLoader(dataNodeLoader);
	ArbilFieldPlaceHolder.setDataNodeLoader(dataNodeLoader);
	ArbilSearch.setDataNodeLoader(dataNodeLoader);
	ImportExportDialog.setDataNodeLoader(dataNodeLoader);
	ImdiMetadataBuilder.setDataNodeLoader(dataNodeLoader);
	ProfilePreview.setDataNodeLoader(dataNodeLoader);
    }
    
    public void injectTreeHelper(TreeHelper treeHelper) {
	//Inject tree helper
	ArbilFavourites.setTreeHelper(treeHelper);
	ArbilTree.setTreeHelper(treeHelper);
	ImportExportDialog.setTreeHelper(treeHelper);
	ImdiMetadataBuilder.setTreeHelper(treeHelper);
	RemoteLocationsContent.setTreeHelper(treeHelper);
	ArbilHyperlinkListener.setTreeHelper(treeHelper);
    }

    public void injectDialogHandler(MessageDialogHandler messageDialogHandler) {
	// Inject message dialog handler
	ArbilComponentBuilder.setMessageDialogHandler(messageDialogHandler);
	ArbilCsvImporter.setMessageDialogHandler(messageDialogHandler);
	ArbilFavourites.setMessageDialogHandler(messageDialogHandler);
	ArbilJournal.setMessageDialogHandler(messageDialogHandler);
	ArbilTableModel.setMessageDialogHandler(messageDialogHandler);
	ImdiTemplate.setMessageDialogHandler(messageDialogHandler);
	ArbilToHtmlConverter.setMessageDialogHandler(messageDialogHandler);
	ApplicationVersionManager.setMessageDialogHandler(messageDialogHandler);
	ArbilVocabularies.setMessageDialogHandler(messageDialogHandler);
	CmdiTemplate.setMessageDialogHandler(messageDialogHandler);
	FieldChangeTriggers.setMessageDialogHandler(messageDialogHandler);
	ImdiUtils.setMessageDialogHandler(messageDialogHandler);
	ImdiMetadataBuilder.setMessageDialogHandler(messageDialogHandler);
	ShibbolethNegotiator.setMessageDialogHandler(messageDialogHandler);
    }

    public void injectWindowManager(WindowManager windowManager) {
	ArbilTree.setWindowManager(windowManager);
	ArbilVocabularies.setWindowManager(windowManager);
	ImdiMetadataBuilder.setWindowManager(windowManager);
    }

    public void injectSessionStorage(SessionStorage sessionStorage) {
	ArbilComponentBuilder.setSessionStorage(sessionStorage);
	ArbilEntityResolver.setSessionStorage(sessionStorage);
	ArbilField.setSessionStorage(sessionStorage);
	ArbilJournal.setSessionStorage(sessionStorage);
	ArbilVocabularies.setSessionStorage(sessionStorage);
	DocumentationLanguages.setSessionStorage(sessionStorage);
	ImdiMetadataBuilder.setSessionStorage(sessionStorage);
	CmdiProfileReader.setSessionStorage(sessionStorage);
	ProfilePreview.setSessionStorage(sessionStorage);
	CmdiTemplate.setSessionStorage(sessionStorage);
	ArbilFavourites.setSessionStorage(sessionStorage);
	XsdChecker.setSessionStorage(sessionStorage);
	ApplicationVersionManager.setSessionStorage(sessionStorage);
	ArbilTemplateManager.setSessionStorage(sessionStorage);
    }
}
