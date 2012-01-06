package nl.mpi.arbil.wicket;

import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;

/**
 * Session for ArbilWicket, keeps user/session specific stuff, such as SessionStorage
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketSession extends WebSession {

    private SessionStorage sessionStorage;
    private TreeHelper treeHelper;
    private ArbilWicketApplication application;
    private DataNodeLoader dataNodeLoader;
    private MimeHashQueue mimeHashQueue;
    
    public ArbilWicketSession(ArbilWicketApplication application, Request request) {
	super(request);
	this.application = application;
    }
    
    public void init(){
	getDataNodeLoader();
	getTreeHelper().init();
    }

    public static ArbilWicketSession get() {
	return (ArbilWicketSession) Session.get();
    }

    /**
     * @return This session's SessionStorage
     */
    public synchronized SessionStorage getSessionStorage() {
	if (sessionStorage == null) {
	    sessionStorage = application.newSessionStorage();
	}
	return sessionStorage;
    }

    /**
     * 
     * @return This session's TreeHelper
     */
    public synchronized TreeHelper getTreeHelper() {
	if (treeHelper == null) {
	    treeHelper = application.newTreeHelper(getSessionStorage());
	}
	return treeHelper;
    }

    public synchronized DataNodeLoader getDataNodeLoader(){
	if(dataNodeLoader == null){
	    dataNodeLoader = application.newDataNodeLoader(this, getSessionStorage(), getTreeHelper(), getMimeHashQueue());
	}
	return dataNodeLoader;
    }
    
    public synchronized MimeHashQueue getMimeHashQueue(){
	if(mimeHashQueue == null){
	    mimeHashQueue = application.newMimeHashQueue(this, sessionStorage);
	}
	return mimeHashQueue;
    }
}
