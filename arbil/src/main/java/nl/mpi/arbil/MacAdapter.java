/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class MacAdapter {

    /**
     * Initializes handlers for 'Quit' and 'About' options on MacOS (application menu and CMD-Q)
     * This is done using reflection so that no compile or run time errors occur on systems that are not Mac
     * and do not have the required classes available...
     *
     * <p>The about item only gets shown if {@link #shouldShowAbout() } returns true (the default implementation does this).</p>
     *
     * @throws MacAdapterException if an exception occurred while accessing the MacOS application API, will be wrapped in this exception
     * @return whether initializing the handlers was successful; if false, this should be interpreted as the system
     * not being MacOS
     */
    public boolean initMacApplicationHandlers() throws MacAdapterException {
	try {
	    // Get application class
	    final Class applicationClass = Class.forName("com.apple.eawt.Application");

	    try {
		// Get application object
		final Object application = applicationClass.getMethod("getApplication").invoke(null);
		// Init quit handler
		initMacQuitHandler(applicationClass, application);
		if (shouldShowAbout()) {
		    // Init about handler
		    initMacAboutHandler(applicationClass, application);
		}
		return true;
	    } catch (IllegalAccessException ex) {
		throw new MacAdapterException(ex);
	    } catch (IllegalArgumentException ex) {
		throw new MacAdapterException(ex);
	    } catch (InvocationTargetException ex) {
		throw new MacAdapterException(ex);
	    } catch (NoSuchMethodException ex) {
		throw new MacAdapterException(ex);
	    } catch (SecurityException ex) {
		throw new MacAdapterException(ex);
	    } catch (ClassNotFoundException ex) {
		throw new MacAdapterException(ex);
	    }
	} catch (ClassNotFoundException ex) {
	    // Application class not found - not on a Mac or not supported for some other reason
	    return false;
	}
    }

    private void initMacQuitHandler(Class applicationClass, Object application) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, InvocationTargetException {
	initMacHandler(applicationClass, application, "com.apple.eawt.QuitHandler", "setQuitHandler", new InvocationHandler() {

	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("handleQuitRequestWith")) {
		    if (!performApplicationExit()) {
			// Quit canceled. Tell MacOS...
			if (args.length >= 2) {
			    // Second argument should be QuitResponse object
			    Object quitResponse = args[1];
			    Class quitResponseClass = Class.forName("com.apple.eawt.QuitResponse");
			    if (quitResponse.getClass().equals(quitResponseClass)) {
				// Tell MacOS that quit should be canceled
				quitResponseClass.getMethod("cancelQuit").invoke(quitResponse);
			    }
			}
		    }
		}
		return null;
	    }
	});
    }

    private void initMacAboutHandler(Class applicationClass, Object application) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, InvocationTargetException {
	initMacHandler(applicationClass, application, "com.apple.eawt.AboutHandler", "setAboutHandler", new InvocationHandler() {

	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		performApplicationAbout();
		return null;
	    }
	});
    }

    /**
     * Sets a MacOS application handler
     *
     * @param applicationClass Application class (com.apple.eawt.Application)
     * @param application Application instance
     * @param interfaceName Name of handler interface
     * @param setMethodName Name of method on application that sets the handler
     * @param invocationHandler Invocation handler that does the work for application handler
     */
    private void initMacHandler(Class applicationClass, Object application, String interfaceName, String setMethodName, InvocationHandler invocationHandler) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, InvocationTargetException {
	// Get class for named interface
	Class handlerInterface = Class.forName(interfaceName);
	// Create dynamic proxy using specified invocation handler
	Object handler = Proxy.newProxyInstance(handlerInterface.getClassLoader(), new Class[]{handlerInterface}, invocationHandler);
	// Set on specified application using specified method
	applicationClass.getMethod(setMethodName, handlerInterface).invoke(application, handler);
    }

    /**
     * Called by the handler when the user has selected 'Quit' from the application menu. Allows the application to cancel the
     * exit procedure (e.g. through user intervention) by returning false.
     *
     * @return whether the exit was canceled
     */
    protected abstract boolean performApplicationExit();

    /**
     * Called by the handler when the user has selected 'About {application}' from the application menu.
     */
    protected abstract void performApplicationAbout();

    /**
     * Override to disable the about item in the application menu
     *
     * @return whether the application menu should have an about item; true in the default application
     */
    protected boolean shouldShowAbout() {
	return true;
    }

    public class MacAdapterException extends Exception {

	public MacAdapterException(String message) {
	    super(message);
	}

	public MacAdapterException(Throwable cause) {
	    super(cause);
	}

	public MacAdapterException(String message, Throwable cause) {
	    super(message, cause);
	}
    }
}
