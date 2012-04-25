package nl.mpi.arbil.data;

import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;

/**
 * Document   : ArbilDataNodeLoader formerly known as ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author Peter.Withers@mpi.nl 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeLoader extends DefaultDataNodeLoader {

    public ArbilDataNodeLoader(MessageDialogHandler messageDialogHandler, SessionStorage sessionStorage, MimeHashQueue mimeHashQueue, TreeHelper treeHelper) {
	super(new DataNodeLoaderThreadManager());
	setImdiDataNodeService(new ImdiDataNodeService(this, messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper));
	setCmdiDataNodeService(new CmdiDataNodeService(this, messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper));
	setSchemaCheckLocalFiles(sessionStorage.loadBoolean("schemaCheckLocalFiles", getThreadManager().isSchemaCheckLocalFiles()));
    }
}
