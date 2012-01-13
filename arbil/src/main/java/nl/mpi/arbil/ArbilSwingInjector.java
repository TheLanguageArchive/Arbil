package nl.mpi.arbil;

import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.search.ArbilRemoteSearch;
import nl.mpi.arbil.ui.ArbilDragDrop;
import nl.mpi.arbil.ui.ArbilFieldViewTableModel;
import nl.mpi.arbil.ui.ArbilFieldViews;
import nl.mpi.arbil.ui.ArbilHelp;
import nl.mpi.arbil.ui.ArbilHyperlinkListener;
import nl.mpi.arbil.ui.ArbilNodeSearchColumnComboBox;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableCellEditor;
import nl.mpi.arbil.ui.CmdiProfilesPanel;
import nl.mpi.arbil.ui.ImportExportDialog;
import nl.mpi.arbil.ui.LanguageListDialogue;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.ui.RemoteServerSearchTermPanel;
import nl.mpi.arbil.ui.TemplateDialogue;
import nl.mpi.arbil.ui.fieldeditors.ArbilLongFieldEditor;
import nl.mpi.arbil.ui.menu.ArbilContextMenu;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.menu.TableContextMenu;
import nl.mpi.arbil.ui.menu.TreeContextMenu;
import nl.mpi.arbil.ui.wizard.setup.ArbilSetupWizard;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * Extension of ArbilInjector that also injects into swing UI classes
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSwingInjector extends ArbilInjector {

    @Override
    public void injectSessionStorage(SessionStorage sessionStorage) {
	super.injectSessionStorage(sessionStorage);
	ArbilFieldViews.setSessionStorage(sessionStorage);
	ArbilSplitPanel.setSessionStorage(sessionStorage);
	ArbilNodeSearchColumnComboBox.setSessionStorage(sessionStorage);
	ArbilMenuBar.setSessionStorage(sessionStorage);
	ImportExportDialog.setSessionStorage(sessionStorage);
	TreeContextMenu.setSessionStorage(sessionStorage);
	ArbilSetupWizard.setSessionStorage(sessionStorage);
	ArbilHyperlinkListener.setSessionStorage(sessionStorage);
	ArbilDragDrop.setSessionStorage(sessionStorage);
    }

    @Override
    public void injectTreeHelper(TreeHelper treeHelper) {
	super.injectTreeHelper(treeHelper);
	TableContextMenu.setTreeHelper(treeHelper);
	TreeContextMenu.setTreeHelper(treeHelper);
	ArbilSetupWizard.setTreeHelper(treeHelper);
	ArbilSplitPanel.setTreeHelper(treeHelper);
	ArbilDragDrop.setTreeHelper(treeHelper);
	ArbilMenuBar.setTreeHelper(treeHelper);
    }

    public void injectMimeHashQueue(MimeHashQueue mimeHashQueue) {
	ArbilMenuBar.setMimeHashQueue(mimeHashQueue);
    }

    @Override
    public void injectWindowManager(WindowManager windowManager) {
	super.injectWindowManager(windowManager);
	ArbilContextMenu.setWindowManager(windowManager);
	LanguageListDialogue.setWindowManager(windowManager);
	TableContextMenu.setWindowManager(windowManager);
	TreeContextMenu.setWindowManager(windowManager);
	ArbilTableCellEditor.setWindowManager(windowManager);
	CmdiProfilesPanel.setWindowManager(windowManager);
	PreviewSplitPanel.setWindowManager(windowManager);
	ArbilHyperlinkListener.setWindowManager(windowManager);
	ArbilDragDrop.setWindowManager(windowManager);
	ArbilLongFieldEditor.setWindowManager(windowManager);
	ArbilTable.setWindowManager(windowManager);
	ArbilMenuBar.setWindowManager(windowManager);
	TemplateDialogue.setWindowManager(windowManager);
	ImportExportDialog.setWindowManager(windowManager);
    }

    @Override
    public void injectDialogHandler(MessageDialogHandler messageDialogHandler) {
	super.injectDialogHandler(messageDialogHandler);
	ArbilHyperlinkListener.setMessageDialogHandler(messageDialogHandler);
	ArbilDragDrop.setMessageDialogHandler(messageDialogHandler);
	ArbilLongFieldEditor.setMessageDialogHandler(messageDialogHandler);
	ArbilTable.setMessageDialogHandler(messageDialogHandler);
	TemplateDialogue.setMessageDialogHandler(messageDialogHandler);
	ArbilFieldViewTableModel.setMessageDialogHandler(messageDialogHandler);
	CmdiProfilesPanel.setMessageDialogHandler(messageDialogHandler);
	ArbilContextMenu.setMessageDialogHandler(messageDialogHandler);
	TableContextMenu.setMessageDialogHandler(messageDialogHandler);
	RemoteServerSearchTermPanel.setMessageDialogHandler(messageDialogHandler);
	ArbilHelp.setMessageDialogHandler(messageDialogHandler);
	ArbilMenuBar.setMessageDialogHandler(messageDialogHandler);
	TreeContextMenu.setMessageDialogHandler(messageDialogHandler);
	ImportExportDialog.setMessageDialogHandler(messageDialogHandler);
	ArbilRemoteSearch.setMessageDialogHandler(messageDialogHandler);
    }

    @Override
    public void injectDataNodeLoader(DataNodeLoader dataNodeLoader) {
	super.injectDataNodeLoader(dataNodeLoader);
	ArbilHyperlinkListener.setDataNodeLoader(dataNodeLoader);
	ArbilContextMenu.setDataNodeLoader(dataNodeLoader);
	ArbilMenuBar.setDataNodeLoader(dataNodeLoader);
	TreeContextMenu.setDataNodeLoader(dataNodeLoader);
    }
    
    
}
