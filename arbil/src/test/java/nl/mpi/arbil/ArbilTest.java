package nl.mpi.arbil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
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
    private Set<URI> localTreeItems;

    @After
    public void cleanUp() {
	if (sessionStorage != null) {
	    deleteDirectory(sessionStorage.getStorageDirectory());
	}
	sessionStorage = null;
	treeHelper = null;
	localTreeItems = null;
    }

    protected void addToLocalTreeFromResource(String resourceClassPath) throws URISyntaxException {
	URI uri = getClass().getResource(resourceClassPath).toURI();

	if (localTreeItems == null) {
	    localTreeItems = new HashSet<URI>();
	}
	localTreeItems.add(uri);

	getTreeHelper().addLocation(uri);
	for (ArbilDataNode node : getTreeHelper().getLocalCorpusNodes()) {
	    node.waitTillLoaded();
	}
    }

    protected void inject() throws Exception {
	ArbilTestInjector.injectBugCatcher(getBugCatcher());
	ArbilTestInjector.injectDialogHandler(getDialogHandler());
	ArbilTestInjector.injectSessionStorage(getSessionStorage());
	ArbilTestInjector.injectTreeHelper(getTreeHelper());
    }

    protected synchronized TreeHelper getTreeHelper() {
	if (treeHelper == null) {
	    treeHelper = newTreeHelper();
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
	return new ArbilTreeHelper() {

	    @Override
	    protected SessionStorage getSessionStorage() {
		return ArbilTest.this.getSessionStorage();
	    }
	};
    }

    protected SessionStorage newSessionStorage() {
	return new MockSessionStorage() {

	    @Override
	    public boolean pathIsInFavourites(File fullTestFile) {
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
