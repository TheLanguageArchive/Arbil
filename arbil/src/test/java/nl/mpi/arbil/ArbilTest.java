package nl.mpi.arbil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.DefaultMimeHashQueue;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import org.junit.After;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilTest {

    private TreeHelper treeHelper;
    private SessionStorage sessionStorage;
    private MessageDialogHandler dialogHandler;
    private BugCatcher bugCatcher;
    private DefaultMimeHashQueue mimeHashQueue;
    private Set<URI> localTreeItems;

    @After
    public void cleanUp() {
	sessionStorage = null;
	treeHelper = null;
	localTreeItems = null;
    }

    protected void addToLocalTreeFromResource(String resourceClassPath) throws URISyntaxException, InterruptedException {
	addToLocalTreeFromURI(uriFromResource(resourceClassPath));
    }

    protected void addToLocalTreeFromURI(URI uri) throws InterruptedException, URISyntaxException {
	if (localTreeItems == null) {
	    localTreeItems = new HashSet<URI>();
	}
	localTreeItems.add(uri);

	getTreeHelper().addLocation(uri);

	for (ArbilNode node : getTreeHelper().getLocalCorpusNodes()) {
	    waitForNodeToLoad((ArbilDataNode) node);
	}
    }

    protected static void waitForNodeToLoad(ArbilDataNode node) {
	while (node.isLoading() || !node.isDataLoaded()) {
	    try {
		Thread.sleep(100);
		node.waitTillLoaded();
	    } catch (InterruptedException ex) {
	    }
	}
	try {
	    Thread.sleep(100);
	} catch (InterruptedException ex) {
	}
    }

    protected URI uriFromResource(String resourceClassPath) throws URISyntaxException {
	return getClass().getResource(resourceClassPath).toURI();
    }

    protected ArbilDataNode dataNodeFromUri(String uriString) throws URISyntaxException {
	return dataNodeFromUri(new URI(uriString));
    }

    protected ArbilDataNode dataNodeFromUri(URI uri) {
	ArbilDataNode dataNode = newDataNodeLoader().getArbilDataNode(this, uri);
	waitForNodeToLoad(dataNode);
	return dataNode;
    }

    protected ArbilDataNode dataNodeFromResource(String resourceClassPath) throws URISyntaxException {
	return dataNodeFromUri(uriFromResource(resourceClassPath));
    }

    protected URI copyOfResource(URI uri) throws FileNotFoundException, IOException {
	File in = new File(uri);
	File out = new File(in.getParentFile(), System.currentTimeMillis() + in.getName());
	if (out.exists()) {
	    if (!out.delete()) {
		throw new IOException("File already exists");
	    }
	}
	FileChannel inChannel = new FileInputStream(in).getChannel();
	FileChannel outChannel = new FileOutputStream(out).getChannel();
	try {
	    inChannel.transferTo(0, inChannel.size(), outChannel);
	} catch (IOException e) {
	    throw e;
	} finally {
	    if (inChannel != null) {
		inChannel.close();
	    }
	    if (outChannel != null) {
		outChannel.close();
	    }
	}
	out.deleteOnExit();
	return out.toURI();
    }

    protected void inject() throws Exception {
	ArbilTestInjector injector = new ArbilTestInjector();
	injector.injectVersionManager(new ApplicationVersionManager(new ArbilVersion()));
	injector.injectBugCatcher(getBugCatcher());
	injector.injectDialogHandler(getDialogHandler());
	injector.injectSessionStorage(getSessionStorage());
	injector.injectDataNodeLoader(newDataNodeLoader());
	injector.injectTreeHelper(getTreeHelper());
    }

    protected synchronized DataNodeLoader newDataNodeLoader() {
	ArbilDataNodeLoader loader = new ArbilDataNodeLoader(getBugCatcher(), getDialogHandler(), getSessionStorage(), getMimeHashQueue(), getTreeHelper());
	getMimeHashQueue().setDataNodeLoader(loader);
	return loader;
    }

    protected synchronized DefaultMimeHashQueue getMimeHashQueue() {
	if(mimeHashQueue == null){
	    mimeHashQueue = newMimeHashQueue();
	}
	return mimeHashQueue;
    }
    
    protected DefaultMimeHashQueue newMimeHashQueue(){
	DefaultMimeHashQueue hashQueue = new DefaultMimeHashQueue(getSessionStorage());
	hashQueue.setBugCatcher(getBugCatcher());
	hashQueue.setMessageDialogHandler(getDialogHandler());
	return hashQueue;
    }

    protected synchronized TreeHelper getTreeHelper() {
	if (treeHelper == null) {
	    treeHelper = newTreeHelper();
	    treeHelper.init();
	}
	return treeHelper;
    }

    protected synchronized SessionStorage getSessionStorage() {
	if (sessionStorage == null) {
	    sessionStorage = newSessionStorage();
	}
	return sessionStorage;
    }

    protected synchronized BugCatcher getBugCatcher() {
	if (bugCatcher == null) {
	    bugCatcher = newBugCatcher();
	}
	return bugCatcher;
    }

    protected synchronized MessageDialogHandler getDialogHandler() {
	if (dialogHandler == null) {
	    dialogHandler = newDialogHandler();
	}
	return dialogHandler;
    }

    protected TreeHelper newTreeHelper() {
	TreeHelper treeHelper = new ArbilTreeHelper() {

	    @Override
	    protected SessionStorage getSessionStorage() {
		return ArbilTest.this.getSessionStorage();
	    }
	};
	treeHelper.init();
	return treeHelper;
    }

    protected SessionStorage newSessionStorage() {
	return new MockSessionStorage() {

	    @Override
	    public boolean pathIsInsideCache(File fullTestFile) {
		return localTreeItems.contains(fullTestFile.toURI());
	    }
	};
    }

    protected BugCatcher newBugCatcher() {
	return new MockBugCatcher();
    }

    protected MessageDialogHandler newDialogHandler() {
	return new MockDialogHandler();
    }

    static public boolean deleteDirectory(File path) {
	if (path.exists() && path.isDirectory()) {
	    File[] files = path.listFiles();
	    if (files != null) {
		for (int i = 0; i < files.length; i++) {
		    if (files[i].isDirectory()) {
			deleteDirectory(files[i]);
		    } else {
			files[i].delete();
		    }
		}
	    }
	}
	return (path.delete());
    }
}
