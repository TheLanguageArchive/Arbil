package nl.mpi.arbil.util;

/**
 * Buffer mainly for reloading/sorting actions that get performed on request,
 * where multiple request may occur in a short time frame and repeated execution
 * is not desired. Such requests can be 'time-buffered' so that a set amount of
 * request idleness is required before the action is executed.
 *
 * The actual thread is not created until a request is put
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilActionBuffer {

    public ArbilActionBuffer(String title, int delay) {
        this(title, delay, new Object());
    }

    /**
     *
     * @param title Title of the thread that will be created
     * @param delay Amount of time that is waited after a request before action is executed
     */
    public ArbilActionBuffer(String title, int delay, final Object lock) {
        this.actionLock = lock;
        this.delay = delay;
        this.title = title;
    }

    public void requestAction() {
        synchronized (actionLock) {
            actionRequested = true;

            if (workerThread == null || !workerThread.isAlive()) {
                workerThread = new Thread(new ArbilBufferedWorkerRunnable(), title);
                workerThread.start();
            }
        }
    }

    public void requestActionAndNotify() {
        synchronized (actionLock) {
            requestAction();
            actionLock.notifyAll();
        }
    }

    protected abstract void executeAction();
    private final Object actionLock;
    private String title;
    private int delay;
    private boolean actionRequested;
    private Thread workerThread;

    private class ArbilBufferedWorkerRunnable implements Runnable {

        @Override
        public void run() {
            // There may be new requests. If so, keep in the loop
            while (actionRequested) {
                try {
                    // Go into wait for some short time while more actions are requested
                    waitForIncomingRequests();
                    // No requests have been added for some time, so do the action
                    executeAction();
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }

        private void waitForIncomingRequests() throws InterruptedException {
            synchronized (actionLock) {
                if (!actionRequested) {
                    try {
                        actionLock.wait();
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
                while (actionRequested) {
                    actionRequested = false;
                    // Give some time for another reload to be requested
                    actionLock.wait(delay);
                }
            }
        }
    }
}
