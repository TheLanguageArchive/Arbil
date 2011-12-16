package nl.mpi.arbil;

import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilDragDrop;
import nl.mpi.arbil.ui.ArbilFieldViews;
import nl.mpi.arbil.ui.ArbilHelp;
import nl.mpi.arbil.ui.ArbilHyperlinkListener;
import nl.mpi.arbil.ui.ArbilNodeSearchColumnComboBox;
import nl.mpi.arbil.ui.ArbilNodeSearchTermPanel;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableCellEditor;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.ui.ImageBoxRenderer;
import nl.mpi.arbil.ui.ImportExportDialog;
import nl.mpi.arbil.ui.TemplateDialogue;
import nl.mpi.arbil.ui.fieldeditors.LanguageIdBox;
import nl.mpi.arbil.ui.menu.ArbilContextMenu;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.menu.TableContextMenu;
import nl.mpi.arbil.ui.menu.TreeContextMenu;
import nl.mpi.arbil.ui.wizard.setup.ArbilSetupWizard;
import nl.mpi.arbil.ui.wizard.setup.TextInstructionWizardContent;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;

/**
 *
 * Extension of ArbilInjector that also injects into swing UI classes
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSwingInjector extends ArbilInjector{

    @Override
    public void injectBugCatcher(BugCatcher bugCatcher) {
	GuiHelper.setBugCatcher(bugCatcher);
	super.injectBugCatcher(bugCatcher);
	ArbilTableCellEditor.setBugCatcher(bugCatcher);
	TableContextMenu.setBugCatcher(bugCatcher);
	TreeContextMenu.setBugCatcher(bugCatcher);
	ImageBoxRenderer.setBugCatcher(bugCatcher);
	ArbilHelp.setBugCatcher(bugCatcher);
	ArbilNodeSearchTermPanel.setBugCatcher(bugCatcher);
	TemplateDialogue.setBugCatcher(bugCatcher);
	TextInstructionWizardContent.setBugCatcher(bugCatcher);
	ArbilNodeSearchColumnComboBox.setBugCatcher(bugCatcher);
	LanguageIdBox.setBugCatcher(bugCatcher);
	ImportExportDialog.setBugCatcher(bugCatcher);
	ArbilNodeSearchTermPanel.setBugCatcher(bugCatcher);
	ArbilMenuBar.setBugCatcher(bugCatcher);
	ArbilSetupWizard.setBugCatcher(bugCatcher);
	ArbilTable.setBugCatcher(bugCatcher);
	ArbilContextMenu.setBugCatcher(bugCatcher);
	ArbilDragDrop.setBugCatcher(bugCatcher);
	ArbilFieldViews.setBugCatcher(bugCatcher);
    }
    
    @Override
    public void injectSessionStorage(SessionStorage sessionStorage) {
	super.injectSessionStorage(sessionStorage);
	ArbilFieldViews.setSessionStorage(sessionStorage);
	ArbilSplitPanel.setSessionStorage(sessionStorage);
	ArbilTreeHelper.setSessionStorage(sessionStorage);
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
    }

    @Override
    public void injectMimeHashQueue(MimeHashQueue mimeHashQueue) {
	super.injectMimeHashQueue(mimeHashQueue);
	ArbilMenuBar.setMimeHashQueue(mimeHashQueue);
    }
    
    
}
