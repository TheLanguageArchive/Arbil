/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 *
 * Stub intended for the purpose of getting rid of annoying 'Authentication required' popups when running from Java WebStart.
 * To use, do the following (only needed once per session):
 * {@code java.net.Authenticator.setDefault(new AuthenticatorStub(dialogHandler)); }
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class AuthenticatorStub extends Authenticator {

    protected final static String NOTIFICATION_DEFAULT_MESSAGE = "Access to a location has been requested that requires authentication. This is currently not supported.\n\nThe accessed metadata instance(s) may be referencing unpublished content.\nAs a result, some information that appears may be incomplete.";
    protected final static String NOTIFICATION_DEFAULT_TITLE = "Protected resource requested";
    private MessageDialogHandler dialogHandler;
    private boolean notificationShown = false;
    private final Object notificationLock = new Object();
    private String notificationMessage;
    private String notificationTitle;

    /**
     * 
     * @param dialogHandler 
     */
    public AuthenticatorStub(MessageDialogHandler dialogHandler) {
	this(dialogHandler, NOTIFICATION_DEFAULT_MESSAGE, NOTIFICATION_DEFAULT_TITLE);
    }

    public AuthenticatorStub(MessageDialogHandler dialogHandler, String notificationMessage, String notificationTitle) {
	this.dialogHandler = dialogHandler;
	this.notificationMessage = notificationMessage;
	this.notificationTitle = notificationTitle;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
	synchronized (notificationLock) {
	    if (!notificationShown) {
		dialogHandler.addMessageDialogToQueue(notificationMessage, notificationTitle);
		notificationShown = true;
	    }
	}

	return super.getPasswordAuthentication();
    }
}
