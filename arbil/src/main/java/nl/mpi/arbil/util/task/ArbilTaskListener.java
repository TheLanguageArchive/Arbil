package nl.mpi.arbil.util.task;

import java.util.EventListener;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilTaskListener extends EventListener {

    void notifyTask(ArbilTaskEvent taskEvent);
}
