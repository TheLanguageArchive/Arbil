package nl.mpi.arbil.wicket;

import nl.mpi.arbil.data.AbstractTreeHelper;
import nl.mpi.arbil.data.DataNodeLoaderThreadManager;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.DefaultMimeHashQueue;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.wicket.pages.HomePage;
import org.apache.wicket.Application;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;

public class ArbilWicketApplication extends WebApplication {

    private ArbilWicketInjector injector;

    /**
     * Constructor
     */
    public ArbilWicketApplication() {
	injector = new ArbilWicketInjector();
	injector.injectHandlers();
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    public Class<HomePage> getHomePage() {
	return HomePage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {
	// We don't want no ordinary session we don't!
	ArbilWicketSession session = new ArbilWicketSession(this, request);
	session.init();
	return session;
    }

    /**
     * Creates a new arbil SessionStorage
     * @return New session storage object
     */
    public SessionStorage newSessionStorage() {
	ArbilSessionStorage sessionStorage = new ArbilSessionStorage();
	sessionStorage.setMessageDialogHandler(injector.getMessageDialogHandler());
	sessionStorage.setWindowManager(injector.getWindowManager());
	sessionStorage.setTreeHelper(injector.getTreeHelper());
	return sessionStorage;
    }

    /**
     * 
     * @param sessionStorage Session storage the treehelper should be tied to
     * @return New treehelper object
     */
    public TreeHelper newTreeHelper(final SessionStorage sessionStorage) {
	TreeHelper th = new ArbilWicketTreeHelper(injector.getMessageDialogHandler(), sessionStorage);
	return th;
    }

    /**
     * 
     * @param session Session to set as session in any new loader thread (generally Session.get() will do)
     * @return New DataNodeLoader instance
     */
    DataNodeLoader newDataNodeLoader(ArbilWicketSession session, SessionStorage sessionStorage, TreeHelper treeHelper, MimeHashQueue mimeHashQueue) {
	DataNodeLoader loader = new ArbilWicketDataNodeLoader(injector.getMessageDialogHandler(), sessionStorage, mimeHashQueue, treeHelper);
	if (treeHelper instanceof AbstractTreeHelper) {
	    ((AbstractTreeHelper) treeHelper).setDataNodeLoader(loader);
	}
	return loader;
    }

    /**
     * 
     * @param session Session to set as session in the mime hash queue thread (generally Session.get() will do)
     * @return New MimeHashQueue instance
     */
    public MimeHashQueue newMimeHashQueue(final ArbilWicketSession session, final SessionStorage sessionStorage) {
	DefaultMimeHashQueue newHashQueue = new DefaultMimeHashQueue(sessionStorage) {

	    @Override
	    protected void beforeExecuteThread(Thread t, Runnable r) {
		super.beforeExecuteThread(t, r);
		// Set application and session copies for the new thread
		setupSessionInThread(session);
	    }

	    @Override
	    protected void afterExecuteThread(Runnable r, Throwable t) {
		super.afterExecuteThread(r, t);
		// Clean up
		cleanupSessionInThread();
	    }
	};
	newHashQueue.setDataNodeLoader(injector.getDataNodeLoader());
	newHashQueue.setMessageDialogHandler(injector.getMessageDialogHandler());
	newHashQueue.startMimeHashQueueThread();
	return newHashQueue;
    }

    private DataNodeLoaderThreadManager newLoaderThreadManager(final Session session) {
	return new DataNodeLoaderThreadManager() {

	    @Override
	    protected void beforeExecuteLoaderThread(Thread t, Runnable r, boolean local) {
		super.beforeExecuteLoaderThread(t, r, local);
		// Set application and session copies for the new thread
		setupSessionInThread(session);
	    }

	    @Override
	    protected void afterExecuteLoaderThread(Runnable r, Throwable t, boolean local) {
		super.afterExecuteLoaderThread(r, t, local);
		// Clean up
		cleanupSessionInThread();
	    }
	};
    }

    private void setupSessionInThread(Session session) {
	Application.set(ArbilWicketApplication.this);
	Session.set(session);
    }

    private void cleanupSessionInThread() {
	Application.unset();
	Session.unset();
    }
}
