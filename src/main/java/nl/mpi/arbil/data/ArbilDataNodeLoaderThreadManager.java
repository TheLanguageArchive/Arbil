package nl.mpi.arbil.data;

import java.util.Vector;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import nl.mpi.arbil.util.XsdChecker;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeLoaderThreadManager implements LoaderThreadManager {

    private final static int MAX_REMOTE_THREADS = 6;
    private final static int MAX_LOCAL_THREADS = 6;
    private boolean continueThread = true;
    private int arbilFilesLoaded = 0;
    private int remoteArbilFilesLoaded = 0;
    private int threadStartCounter = 0;
    private ThreadGroup remoteLoaderThreadGroup;
    private ThreadGroup localLoaderThreadGroup;
    private final Vector<ArbilDataNode> arbilRemoteNodesToInit = new Vector<ArbilDataNode>();
    private final Vector<ArbilDataNode> arbilLocalNodesToInit = new Vector<ArbilDataNode>();
    private boolean schemaCheckLocalFiles = false;

    public ArbilDataNodeLoaderThreadManager() {
	continueThread = true;
	remoteLoaderThreadGroup = new ThreadGroup("RemoteLoaderThreads");
	localLoaderThreadGroup = new ThreadGroup("LocalLoaderThreads");

    }

    @Override
    public void addNodeToQueue(ArbilDataNode nodeToAdd) {
	startLoaderThreads();
	if (ArbilDataNode.isStringLocal(nodeToAdd.getUrlString())) {
	    synchronized (arbilLocalNodesToInit) {
		if (!arbilLocalNodesToInit.contains(nodeToAdd)) {
		    arbilLocalNodesToInit.addElement(nodeToAdd);
		    arbilLocalNodesToInit.notifyAll();
		}
	    }
	} else {
	    synchronized (arbilRemoteNodesToInit) {
		if (!arbilRemoteNodesToInit.contains(nodeToAdd)) {
		    arbilRemoteNodesToInit.addElement(nodeToAdd);
		    arbilRemoteNodesToInit.notifyAll();
		}
	    }
	}
    }

    private ArbilDataNode getNodeFromQueue(Vector<ArbilDataNode> dataNodesQueue) {
	synchronized (dataNodesQueue) {
	    if (dataNodesQueue.size() > 0) {
		ArbilDataNode tempDataNode = dataNodesQueue.remove(0);
		if (tempDataNode.lockedByLoadingThread) {
		    dataNodesQueue.add(tempDataNode);
		    return null;
		} else {
		    tempDataNode.lockedByLoadingThread = true;
		    dataNodesQueue.notifyAll();
		    return tempDataNode;
		}
	    } else {
		return null;
	    }
	}
    }

    @Override
    synchronized public void startLoaderThreads() {
	// start the remote imdi loader threads
	while (isContinueThread() && remoteExecutor.getActiveCount() < MAX_REMOTE_THREADS()) {
	    remoteExecutor.submit(new RemoteLoader());
	}
	// due to an apparent deadlock in the imdi api only one thread is used for local files. the deadlock appears to be in the look up host area
	// start the local imdi threads
	while (isContinueThread() && localExecutor.getActiveCount() < MAX_LOCAL_THREADS()) {
	    localExecutor.submit(new LocalLoader());
	}
    }

    /**
     * @return the schemaCheckLocalFiles
     */
    @Override
    public boolean isSchemaCheckLocalFiles() {
	return schemaCheckLocalFiles;
    }

    /**
     * @param schemaCheckLocalFiles the schemaCheckLocalFiles to set
     */
    @Override
    public void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles) {
	this.schemaCheckLocalFiles = schemaCheckLocalFiles;
    }

    /**
     * @return the continueThread
     */
    public boolean isContinueThread() {
	return continueThread;
    }

    /**
     * @param continueThread the continueThread to set
     */
    @Override
    public void setContinueThread(boolean continueThread) {
	this.continueThread = continueThread;
    }

    /**
     * @return the MAX_REMOTE_THREADS
     */
    public static int MAX_REMOTE_THREADS() {
	return MAX_REMOTE_THREADS;
    }

    /**
     * @return the MAX_LOCAL_THREADS
     */
    public static int MAX_LOCAL_THREADS() {
	return MAX_LOCAL_THREADS;
    }

    /***
     * Runnable that gets a node from the remote queue and loads it
     */
    private class RemoteLoader implements Runnable {
	// this has been separated in to two separate threads to prevent long delays when there is no server connection
	// each node is loaded one at a time and must time out before the next is started
	// the local corpus nodes are the fastest so they are now loaded in a separate thread
	// alternatively a thread pool may be an option

	ArbilDataNode currentArbilDataNode = null;

	@Override
	@SuppressWarnings("SleepWhileHoldingLock")
	public void run() {
	    while (isContinueThread() && !Thread.currentThread().isInterrupted()) {

		try {
		    currentArbilDataNode = waitForNodes(arbilRemoteNodesToInit);
		} catch (InterruptedException ex) {
		    System.out.println(Thread.currentThread().getName() + " interrupted. " + ex.getMessage());
		    return;
		}

		if (currentArbilDataNode != null) {
		    System.out.println("run RemoteArbilLoader processing: " + currentArbilDataNode.getUrlString());
		    currentArbilDataNode.loadArbilDom();
		    currentArbilDataNode.updateLoadingState(-1);
		    currentArbilDataNode.clearIcon();
		    currentArbilDataNode.clearChildIcons();
		    remoteArbilFilesLoaded++;
		    currentArbilDataNode.notifyLoaded();
		    currentArbilDataNode.lockedByLoadingThread = false;
		}
	    }
	}
    }

    /**
     * Runnable that gets a node from the local queue and loads it
     */
    private class LocalLoader implements Runnable {

	private ArbilDataNode currentArbilDataNode;

	@Override
	@SuppressWarnings("SleepWhileHoldingLock")
	public void run() {
	    while (isContinueThread() && !Thread.currentThread().isInterrupted()) {

		try {
		    currentArbilDataNode = waitForNodes(arbilLocalNodesToInit);
		} catch (InterruptedException ex) {
		    System.out.println(Thread.currentThread().getName() + " interrupted. " + ex.getMessage());
		    return;
		}

		if (currentArbilDataNode != null) {
		    System.out.println("run LocalArbilLoader processing: " + currentArbilDataNode.getUrlString());
		    if (currentArbilDataNode.getNeedsSaveToDisk(false)) {
			currentArbilDataNode.saveChangesToCache(false);
		    }
		    currentArbilDataNode.loadArbilDom();
		    if (isSchemaCheckLocalFiles()) {
			if (currentArbilDataNode.isMetaDataNode()) {
			    XsdChecker xsdChecker = new XsdChecker();
			    String checkerResult;
			    checkerResult = xsdChecker.simpleCheck(currentArbilDataNode.getFile(), currentArbilDataNode.getURI());
			    currentArbilDataNode.hasSchemaError = (checkerResult != null);
			}
		    } else {
			currentArbilDataNode.hasSchemaError = false;
		    }
		    currentArbilDataNode.updateLoadingState(-1);
		    currentArbilDataNode.clearIcon();
		    currentArbilDataNode.clearChildIcons();
		    arbilFilesLoaded++;
		    System.out.println("remoteArbilFilesLoaded: " + remoteArbilFilesLoaded + " arbilFilesLoaded: " + arbilFilesLoaded);
		    currentArbilDataNode.lockedByLoadingThread = false;
		    currentArbilDataNode.notifyLoaded();
		}
	    }
	}
    }

    protected void beforeExecuteLoaderThread(Thread t, Runnable r, boolean local) {
    }

    protected void afterExecuteLoaderThread(Runnable r, Throwable t, boolean local) {
    }
    private ScheduledThreadPoolExecutor remoteExecutor = new ScheduledThreadPoolExecutor(MAX_REMOTE_THREADS()) {

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
	    beforeExecuteLoaderThread(t, r, false);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
	    afterExecuteLoaderThread(r, t, false);
	}

	@Override
	public ThreadFactory getThreadFactory() {
	    return new ThreadFactory() {

		public Thread newThread(Runnable r) {
		    String threadName = "ArbilDataNodeLoader-remote-" + threadStartCounter++;
		    Thread thread = new Thread(remoteLoaderThreadGroup, r, threadName);
		    thread.setPriority(Thread.NORM_PRIORITY - 1);
		    return thread;
		}
	    };
	}
    };
    private ScheduledThreadPoolExecutor localExecutor = new ScheduledThreadPoolExecutor(MAX_LOCAL_THREADS()) {

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
	    beforeExecuteLoaderThread(t, r, true);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
	    afterExecuteLoaderThread(r, t, true);
	}

	@Override
	public ThreadFactory getThreadFactory() {
	    return new ThreadFactory() {

		public Thread newThread(Runnable r) {
		    String threadName = "ArbilDataNodeLoader-local-" + threadStartCounter++;
		    Thread thread = new Thread(localLoaderThreadGroup, r, threadName);
		    thread.setPriority(Thread.NORM_PRIORITY - 1);
		    return thread;
		}
	    };
	}
    };

    private ArbilDataNode waitForNodes(Vector<ArbilDataNode> queue) throws InterruptedException {
	synchronized (queue) {
	    while (queue.isEmpty()) {
		queue.wait();
	    }
	    return getNodeFromQueue(queue);
	}
    }
}
