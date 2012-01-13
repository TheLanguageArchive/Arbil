/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.wicket;

import nl.mpi.arbil.data.ArbilDataNodeService;
import nl.mpi.arbil.data.DataNodeLoaderThreadManager;
import nl.mpi.arbil.data.DefaultDataNodeLoader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketDataNodeLoader extends DefaultDataNodeLoader {

    public ArbilWicketDataNodeLoader(MessageDialogHandler messageDialogHandler, SessionStorage sessionStorage, MimeHashQueue mimeHashQueue, TreeHelper treeHelper) {
	super(new DataNodeLoaderThreadManager());
	setDataNodeService(new ArbilDataNodeService(this, messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper));
	setSchemaCheckLocalFiles(sessionStorage.loadBoolean("schemaCheckLocalFiles", getThreadManager().isSchemaCheckLocalFiles()));
    }
}
