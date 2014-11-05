/**
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Stub intended for the purpose of getting rid of annoying 'Authentication required' popups when running from Java WebStart.
 * To use, do the following (only needed once per session):
 * {@code java.net.Authenticator.setDefault(new AuthenticatorStub(dialogHandler)); }
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class AuthenticatorStub extends Authenticator {
    private final static Logger logger = LoggerFactory.getLogger(AuthenticatorStub.class);

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
	logger.debug("Authentication requested for {}. Handled by stub.", getRequestingURL());
	synchronized (notificationLock) {
	    if (!notificationShown) {
		dialogHandler.addMessageDialogToQueue(notificationMessage, notificationTitle);
		notificationShown = true;
	    }
	}

	return super.getPasswordAuthentication();
    }
}
