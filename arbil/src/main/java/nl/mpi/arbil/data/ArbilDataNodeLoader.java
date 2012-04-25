package nl.mpi.arbil.data;

import java.net.URI;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;

/**
 * Document : ArbilDataNodeLoader formerly known as ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 *
 * @author Peter.Withers@mpi.nl
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeLoader extends DefaultDataNodeLoader {

    private final ArbilDataNodeService imdiDataNodeService;
    private final ArbilDataNodeService cmdiDataNodeService;

    public ArbilDataNodeLoader(MessageDialogHandler messageDialogHandler, SessionStorage sessionStorage, MimeHashQueue mimeHashQueue, TreeHelper treeHelper) {
	super(new DataNodeLoaderThreadManager());
	imdiDataNodeService = new ImdiDataNodeService(this, messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper);
	cmdiDataNodeService = new CmdiDataNodeService(this, messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper);
	setSchemaCheckLocalFiles(sessionStorage.loadBoolean("schemaCheckLocalFiles", getThreadManager().isSchemaCheckLocalFiles()));
    }

    @Override
    protected ArbilDataNodeService getDataNodeServiceForUri(URI uri) {
	if (ArbilDataNode.isPathCmdi(uri.getPath())) {
	    return cmdiDataNodeService;
	} else {
	    return imdiDataNodeService;
	}
    }
}
