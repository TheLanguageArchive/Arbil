/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogManager;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.DefaultMimeHashQueue;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilTest {
    
    static {
	try {
	    final InputStream resourceAsStream = ArbilTest.class.getResourceAsStream("/logging-test.properties");
	    LogManager.getLogManager().readConfiguration(resourceAsStream);
	} catch (IOException ex) {
	    System.err.println("Could not read logging configuration for testing");
	    ex.printStackTrace(System.err);
	}
    }
    private final static Logger logger = LoggerFactory.getLogger(ArbilTest.class);
    private SessionStorage sessionStorage;
    private MessageDialogHandler dialogHandler;
    private DefaultMimeHashQueue mimeHashQueue;
    private Set<URI> localTreeItems;
    
    @After
    public void cleanUp() {
	sessionStorage = null;
	localTreeItems = null;
    }
    
    protected void addToLocalTreeFromResource(String resourceClassPath) throws Exception {
	addToLocalTreeFromURI(uriFromResource(resourceClassPath));
    }
    
    protected void addToLocalTreeFromURI(URI uri) throws Exception {
	if (localTreeItems == null) {
	    localTreeItems = new HashSet<URI>();
	}
	logger.debug("Adding node to local tree: {}", uri);
	localTreeItems.add(uri);
	
	if (!getTreeHelper().addLocation(uri)) {
	    throw new Exception("Could not add location to tree" + uri);
	}
	
	for (ArbilNode node : getTreeHelper().getLocalCorpusNodes()) {
	    logger.debug("Waiting for node from tree to load: {}", node);
	    waitForNodeToLoad((ArbilDataNode) node);
	}
    }
    
    protected static void waitForNodeToLoad(ArbilDataNode node) {
	while (node.isLoading() || !node.isDataLoaded()) {
	    try {
		Thread.sleep(100);
		logger.debug("Waiting for node to load: {}", node);
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
	ArbilDataNode dataNode = getDataNodeLoader().getArbilDataNode(this, uri);
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
    ArbilTestInjector injector;
    
    protected void inject() throws Exception {
	injector = new ArbilTestInjector();
	injector.injectHandlers(getSessionStorage());
//	injector.injectVersionManager(new ApplicationVersionManager(new ArbilVersion()));
//	injector.injectDialogHandler(getDialogHandler());
//	injector.injectSessionStorage(getSessionStorage());
//	injector.injectDataNodeLoader(getDataNodeLoader());
//	injector.injectTreeHelper(getTreeHelper());
    }
    
    protected synchronized DataNodeLoader getDataNodeLoader() {
	return injector.dataNodeLoader;
    }
    
    protected synchronized DefaultMimeHashQueue getMimeHashQueue() {
	if (mimeHashQueue == null) {
	    mimeHashQueue = newMimeHashQueue();
	}
	return mimeHashQueue;
    }
    
    protected DefaultMimeHashQueue newMimeHashQueue() {
	DefaultMimeHashQueue hashQueue = new DefaultMimeHashQueue(getSessionStorage());
	hashQueue.setMessageDialogHandler(getDialogHandler());
	return hashQueue;
    }
    
    protected synchronized ArbilTreeHelper getTreeHelper() {
	return injector.treeHelper;
    }
    
    protected synchronized SessionStorage getSessionStorage() {
	if (sessionStorage == null) {
	    sessionStorage = newSessionStorage();
	}
	return sessionStorage;
    }
    
    protected synchronized MessageDialogHandler getDialogHandler() {
	if (dialogHandler == null) {
	    dialogHandler = newDialogHandler();
	}
	return dialogHandler;
    }
    
    protected SessionStorage newSessionStorage() {
	return new MockSessionStorage() {
	    @Override
	    public boolean pathIsInsideCache(File fullTestFile) {
		return localTreeItems.contains(fullTestFile.toURI());
	    }
	};
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
