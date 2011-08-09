package nl.mpi.arbil;

import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.util.ArbilVersionChecker;
import nl.mpi.arbil.util.ArbilBugCatcher;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.PreviewSplitPanel;

/*
 * LinorgView.java
 * This version uses only a JFrame and does not require additional dependencies
 * Created on 23 September 2008, 17:23
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMain extends javax.swing.JFrame {

    protected javax.swing.JSplitPane mainSplitPane;
    protected ArbilMenuBar arbilMenuBar;
//    static boolean updateViaJavaws = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	System.setProperty("sun.swing.enableImprovedDragGesture", "true");
	System.setProperty("apple.awt.graphics.UseQuartz", "true");
	System.setProperty("apple.laf.useScreenMenuBar", "true");
	java.awt.EventQueue.invokeLater(new Runnable() {

	    public void run() {
		try {
		    new ArbilMain();
		} catch (Exception ex) {
		    new ArbilBugCatcher().logError(ex);
		}
	    }
	});
    }

    public ArbilMain() {
	ArbilDesktopInjector.injectHandlers();

	this.addWindowListener(new WindowAdapter() {

	    @Override
	    public void windowClosing(WindowEvent e) {
		arbilMenuBar.performCleanExit();
		//super.windowClosing(e);
	    }
	});

	initComponents();
	PreviewSplitPanel previewSplitPanel = PreviewSplitPanel.getInstance();
	mainSplitPane.setRightComponent(previewSplitPanel);
	ArbilTreePanels arbilTreePanels = new ArbilTreePanels();
	mainSplitPane.setLeftComponent(arbilTreePanels);
	arbilMenuBar = new ArbilMenuBar(previewSplitPanel, null);
	setJMenuBar(arbilMenuBar);

	mainSplitPane.setDividerLocation(0.25);

	ArbilWindowManager.getSingleInstance().loadGuiState(this);
	setTitle(new ArbilVersion().applicationTitle + " " + new ArbilVersion().compileDate);
	setIconImage(ArbilIcons.getSingleInstance().linorgIcon.getImage());
	// load the templates and populate the templates menu
	setVisible(true);
	ArbilWindowManager.getSingleInstance().openIntroductionPage();

	if (arbilMenuBar.checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
	    new ArbilVersionChecker().checkForUpdate();
	}

	initMacApplicationHandlers();
    }

    private void initComponents() {

	mainSplitPane = new javax.swing.JSplitPane();

	setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	setTitle("Arbil");

	mainSplitPane.setDividerLocation(100);
	mainSplitPane.setDividerSize(5);
	mainSplitPane.setName("mainSplitPane");
	getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

	pack();
    }

    /**
     * Initializes handlers for 'Quit' and 'About' options on MacOS (application menu + CMD-Q)
     * This is done using reflection so that no compile or run time errors occur on systems that are not Mac
     * and do not have the required classes available...
     */
    private void initMacApplicationHandlers() {
	try {
	    // Get application class
	    Class applicationClass = Class.forName("com.apple.eawt.Application");

	    Exception exception = null;
	    try {
		// Get application object
		Object application = applicationClass.getMethod("getApplication").invoke(null);
		// Init quit handler
		initMacQuitHandler(applicationClass, application);
		// Init about handler
		initMacAboutHandler(applicationClass, application);
		// Successfully set handlers, now remove redundant options from menu bar
		arbilMenuBar.setMacOsMenu(true);
		return;
	    } catch (IllegalAccessException ex) {
		exception = ex;
	    } catch (IllegalArgumentException ex) {
		exception = ex;
	    } catch (InvocationTargetException ex) {
		exception = ex;
	    } catch (NoSuchMethodException ex) {
		exception = ex;
	    } catch (SecurityException ex) {
		exception = ex;
	    } catch (ClassNotFoundException ex) {
		exception = ex;
	    }
	    System.err.println("Could not configure MacOS application handlers");
	    if (exception != null) {
		System.err.println(exception);
	    }
	} catch (ClassNotFoundException ex) {
	    // Application class not found - not on a Mac or not supported for some other reason. Fail silently.
	    return;
	}
    }

    private void initMacQuitHandler(Class applicationClass, Object application) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, InvocationTargetException {
	// Create quitHandler
	InvocationHandler quitHandlerInvocationHandler = new InvocationHandler() {

	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("handleQuitRequestWith")) {
		    if (!arbilMenuBar.performCleanExit()) {
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
	};
	Class quitHandlerInterface = Class.forName("com.apple.eawt.QuitHandler");
	Object quitHandler = Proxy.newProxyInstance(quitHandlerInterface.getClassLoader(), new Class[]{quitHandlerInterface}, quitHandlerInvocationHandler);
	// Add to application
	Method addQuitHandlerMethod = applicationClass.getMethod("setQuitHandler", quitHandlerInterface);
	addQuitHandlerMethod.invoke(application, quitHandler);
    }

    private void initMacAboutHandler(Class applicationClass, Object application) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, InvocationTargetException {
	// Create quitHandler
	InvocationHandler aboutHandlerInvocationHandler = new InvocationHandler() {

	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		ArbilWindowManager.getSingleInstance().openAboutPage();
		return null;
	    }
	};
	Class aboutHandlerInterface = Class.forName("com.apple.eawt.AboutHandler");
	Object aboutHandler = Proxy.newProxyInstance(aboutHandlerInterface.getClassLoader(), new Class[]{aboutHandlerInterface}, aboutHandlerInvocationHandler);
	// Add to application
	Method addAboutHandlerMethod = applicationClass.getMethod("setAboutHandler", aboutHandlerInterface);
	addAboutHandlerMethod.invoke(application, aboutHandler);
    }
}
