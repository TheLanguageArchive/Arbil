package nl.mpi.arbil.wicket;

import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.MimeHashQueue;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketMimeHashQueueProxy implements MimeHashQueue{

    private MimeHashQueue getMimeHashQueue(){
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
    
}
