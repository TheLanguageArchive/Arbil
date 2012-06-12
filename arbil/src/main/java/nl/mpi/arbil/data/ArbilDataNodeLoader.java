package nl.mpi.arbil.data;

import java.net.URI;
import nl.mpi.arbil.data.service.DataNodeServiceLocator;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 * Document : ArbilDataNodeLoader formerly known as ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 *
 * @author Peter.Withers@mpi.nl
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeLoader extends DefaultDataNodeLoader implements DataNodeServiceLocator {

    private final ArbilDataNodeService imdiDataNodeService;
    private final ArbilDataNodeService cmdiDataNodeService;

    public ArbilDataNodeLoader(MessageDialogHandler messageDialogHandler, WindowManager windowManager, SessionStorage sessionStorage, MimeHashQueue mimeHashQueue, TreeHelper treeHelper, ApplicationVersionManager versionManager) {
	super(new DataNodeLoaderThreadManager());
	imdiDataNodeService = new ImdiDataNodeService(this, messageDialogHandler, windowManager, sessionStorage, mimeHashQueue, treeHelper, versionManager);
	cmdiDataNodeService = new CmdiDataNodeService(this, messageDialogHandler, windowManager, sessionStorage, mimeHashQueue, treeHelper, versionManager);
	setSchemaCheckLocalFiles(sessionStorage.loadBoolean("schemaCheckLocalFiles", getThreadManager().isSchemaCheckLocalFiles()));
    }

    public ArbilDataNodeService getDataNodeServiceForUri(URI uri) {
	if (ArbilDataNode.isPathCmdi(uri.getPath())) {
	    return cmdiDataNodeService;
	} else {
	    return imdiDataNodeService;
	}
    }

    @Override
    protected DataNodeServiceLocator getServiceLocator() {
	return this;
    }
}
