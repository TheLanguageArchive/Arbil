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
     * Initializes handlers for 'Quit' and 'About' options on MacOS (application menu + CMD-Q)
     * This is done using reflection so that no compile or run time errors occur on systems that are not Mac
     * and do not have the required classes available...
     * 
     * The about item only gets shown if {@link #shouldShowAbout() } return true (the default implementation does this).
     *
     * @return whether initializing the handlers was successful; if false, this can usually be interpreted as the system
     * not being
     */
    public boolean initMacApplicationHandlers() throws IllegalStateException {
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
		throw new IllegalStateException(ex);
	    } catch (IllegalArgumentException ex) {
		throw new IllegalStateException(ex);
	    } catch (InvocationTargetException ex) {
		throw new IllegalStateException(ex);
	    } catch (NoSuchMethodException ex) {
		throw new IllegalStateException(ex);
	    } catch (SecurityException ex) {
		throw new IllegalStateException(ex);
	    } catch (ClassNotFoundException ex) {
		throw new IllegalStateException(ex);
	    }
	} catch (ClassNotFoundException ex) {
	    // Application class not found - not on a Mac or not supported for some other reason. Fail silently.
	}
	return false;
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
}
