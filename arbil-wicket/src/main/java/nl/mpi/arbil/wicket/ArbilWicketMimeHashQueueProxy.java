package nl.mpi.arbil.wicket;

import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.util.MimeHashQueue;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketMimeHashQueueProxy implements MimeHashQueue {
    
    private MimeHashQueue getMimeHashQueue() {
	return ArbilWicketSession.get().getMimeHashQueue();
    }
    
    public void addToQueue(ArbilDataNode dataNode) {
	getMimeHashQueue().addToQueue(dataNode);
    }
    
    public boolean isCheckResourcePermissions() {
	return getMimeHashQueue().isCheckResourcePermissions();
    }
    
    public void setCheckResourcePermissions(boolean checkResourcePermissions) {
	getMimeHashQueue().setCheckResourcePermissions(checkResourcePermissions);
    }
    
    public void startMimeHashQueueThread() {
	getMimeHashQueue().startMimeHashQueueThread();
    }
    
    public void stopMimeHashQueueThread() {
	getMimeHashQueue().stopMimeHashQueueThread();
    }
    
    public String[] getMimeType(URI fileUri) {
	return getMimeHashQueue().getMimeType(fileUri);
    }
    
    public void terminateQueue() {
	getMimeHashQueue().terminateQueue();
    }
    
    @Override
    public void forceInQueue(ArbilDataNode dataNode) {
	getMimeHashQueue().forceInQueue(dataNode);
    }

    @Override
    public ArbilNode getActiveNode() {
	return getMimeHashQueue().getActiveNode();
    }
}
