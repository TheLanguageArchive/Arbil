package nl.mpi.arbil;

import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilDragDrop;
import nl.mpi.arbil.ui.ArbilFieldViews;
import nl.mpi.arbil.ui.ArbilHyperlinkListener;
import nl.mpi.arbil.ui.ArbilNodeSearchColumnComboBox;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ImportExportDialog;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.menu.TreeContextMenu;
import nl.mpi.arbil.ui.wizard.setup.ArbilSetupWizard;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 *
 * Extension of ArbilInjector that also injects into swing UI classes
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSwingInjector extends ArbilInjector{

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
}
