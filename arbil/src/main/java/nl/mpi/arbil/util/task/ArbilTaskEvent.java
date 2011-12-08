package nl.mpi.arbil.util.task;

import java.util.EventObject;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTaskEvent extends EventObject {

    public enum ArbilTaskEventType {

	/**
	 * Task has been started
	 */
	STARTED,
	/**
	 * Task name, description or status has changed
	 */
	CHANGED,
	/**
	 * Progress or target value has changed or (in)determinate status has changed
	 */
	PROGRESS,
	/**
	 * Task has been completed
	 */
	COMPLETED,
	/**
	 * Task has been canceled
	 */
	CANCELED
    }
    private ArbilTaskEventType eventType;

    public ArbilTaskEvent(ArbilTask task, ArbilTaskEventType eventType) {
	super(task);
	this.eventType = eventType;
    }

    @Override
    public ArbilTask getSource() {
	return (ArbilTask) super.getSource();
    }

    public ArbilTaskEventType getEventType() {
	return eventType;
    }
}
