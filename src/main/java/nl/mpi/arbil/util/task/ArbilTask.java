package nl.mpi.arbil.util.task;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilTask {

    boolean isIndeterminate();
    
    int getTargetValue();

    int getProgressValue();
    
    String getProgressString();

    String getName();

    String getDescription();

    String getStatus();
}
