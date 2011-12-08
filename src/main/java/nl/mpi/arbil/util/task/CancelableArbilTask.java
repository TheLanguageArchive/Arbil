package nl.mpi.arbil.util.task;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface CancelableArbilTask extends ArbilTask{
    void cancel();
}
