package nl.mpi.arbil.util.task;

import java.util.Collection;

/**
 * Basic implementation of ArbilTask with synchronized getters and setters for all properties
 * @see ArbilTask
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DefaultArbilTask implements ArbilTask {

    private String name;
    private String description;
    private String status = null;
    private boolean indeterminate = true;
    private int targetValue = 100;
    private int progressValue = 0;
    private Collection<ArbilTaskListener> listeners;
    private String progressFormat;

    /**
     * Creates new indeterminate task with provided name and description
     * @param name
     * @param description 
     * @param progressFormat String format for the progress string (arguments: progress, target). Null for no progress string
     */
    public DefaultArbilTask(String name, String description, String progressFormat, Collection<ArbilTaskListener> listeners) {
	this.name = name;
	this.description = description;
	this.progressFormat = progressFormat;
	this.listeners = listeners;
    }

    public synchronized boolean isIndeterminate() {
	return indeterminate;
    }

    /**
     * Sets whether indeterminate and sends out a PROGRESS event
     */
    public synchronized void setIndeterminate(boolean indeterminate) {
	this.indeterminate = indeterminate;
	notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType.PROGRESS);
    }

    public synchronized int getTargetValue() {
	return targetValue;
    }

    /**
     * Sets target value and sends out a PROGRESS event
     */
    public synchronized void setTargetValue(int targetValue) {
	this.targetValue = targetValue;
	notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType.PROGRESS);
    }

    public synchronized int getProgressValue() {
	return progressValue;
    }

    /**
     * Sets progress value and sends out a PROGRESS event
     */
    public synchronized void setProgressValue(int progressValue) {
	this.progressValue = progressValue;
	notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType.PROGRESS);
    }

    public synchronized String getName() {
	return name;
    }

    /**
     * Sets name and sends out a CHANGED event
     */
    public synchronized void setName(String name) {
	this.name = name;
	notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType.CHANGED);
    }

    public synchronized String getDescription() {
	return description;
    }

    /**
     * Sets description and sends out a CHANGED event 
     */
    public synchronized void setDescription(String description) {
	this.description = description;
	notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType.CHANGED);
    }

    public String getProgressString() {
	if (progressFormat == null) {
	    return null;
	} else {
	    return String.format(progressFormat, progressValue, targetValue);
	}
    }

    /**
     * Sets unit and sends out a CHANGED event 
     */
    public synchronized void setProgressFormat(String format) {
	this.progressFormat = format;
	notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType.CHANGED);
    }

    public synchronized String getStatus() {
	return status;
    }

    /**
     * Sets status and sends out a CHANGED event
     */
    public synchronized void setStatus(String status) {
	this.status = status;
	notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType.CHANGED);
    }

    /**
     * Sends out a STARTED event
     */
    public void start() {
	notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType.STARTED);
    }

    /**
     * Sends out a COMPLETED event
     */
    public void finish() {
	notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType.COMPLETED);
    }

    protected void notifyAllListeners(ArbilTaskEvent.ArbilTaskEventType eventType) {
	for (ArbilTaskListener listener : getListeners()) {
	    notifyListener(listener, eventType);
	}
    }

    protected void notifyListener(ArbilTaskListener listener, ArbilTaskEvent.ArbilTaskEventType eventType) {
	listener.notifyTask(new ArbilTaskEvent(this, eventType));
    }

    protected Collection<ArbilTaskListener> getListeners() {
	return listeners;
    }
}
