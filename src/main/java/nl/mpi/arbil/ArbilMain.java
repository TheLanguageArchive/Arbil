package nl.mpi.arbil;

import java.awt.BorderLayout;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilTaskStatusBar;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.util.ArbilMimeHashQueue;

/*
 * ArbilMain.java
 * This version uses only a JFrame and does not require additional dependencies
 * Created on 23 September 2008, 17:23
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 */
public class ArbilMain extends javax.swing.JFrame {

    private javax.swing.JSplitPane mainSplitPane;
    private ArbilMenuBar arbilMenuBar;
    private ArbilTaskStatusBar statusBar;

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
	final ApplicationVersionManager versionManager = new ApplicationVersionManager(new ArbilVersion());
	final ArbilDesktopInjector injector = new ArbilDesktopInjector();
	injector.injectHandlers(versionManager);

	initApplication(injector.getMimeHashQueue());
	initUI(injector.getTreeHelper(), versionManager);
	
	checkFirstRun();
    }

    private void initApplication(ArbilMimeHashQueue hashQueue) {
	hashQueue.init();
    }

    private void initUI(ArbilTreeHelper treeHelper, final ApplicationVersionManager versionManager) {
	this.addWindowListener(new WindowAdapter() {

	    @Override
	    public void windowClosing(WindowEvent e) {
		arbilMenuBar.performCleanExit();
		//super.windowClosing(e);
	    }
	});

	initComponents();
	ArbilWindowManager.getSingleInstance().addTaskListener(statusBar);
	PreviewSplitPanel previewSplitPanel = PreviewSplitPanel.getInstance();
	mainSplitPane.setRightComponent(previewSplitPanel);
	ArbilTreePanels arbilTreePanels = new ArbilTreePanels(treeHelper);
	mainSplitPane.setLeftComponent(arbilTreePanels);
	arbilMenuBar = new ArbilMenuBar(previewSplitPanel, null);
	setJMenuBar(arbilMenuBar);

	mainSplitPane.setDividerLocation(0.25);

	ArbilWindowManager.getSingleInstance().loadGuiState(this, statusBar);
	setTitle(versionManager.getApplicationVersion().applicationTitle + " " + versionManager.getApplicationVersion().compileDate);
	setIconImage(ArbilIcons.getSingleInstance().linorgIcon.getImage());
	// load the templates and populate the templates menu
	setVisible(true);

	if (arbilMenuBar.checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
	    versionManager.checkForUpdate();
	}

	initMacApplicationHandlers();
    }

    private void checkFirstRun() {
	ArbilWindowManager.getSingleInstance().showSetupWizardIfFirstRun();
	ArbilWindowManager.getSingleInstance().openIntroductionPage();
    }

    private void initComponents() {

	mainSplitPane = new javax.swing.JSplitPane();
	statusBar = new ArbilTaskStatusBar();

	setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	setTitle("Arbil");

	mainSplitPane.setDividerLocation(100);
	mainSplitPane.setDividerSize(5);
	mainSplitPane.setName("mainSplitPane");
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);
	getContentPane().add(statusBar, BorderLayout.SOUTH);

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
	}
    }

    private void initMacQuitHandler(Class applicationClass, Object application) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, InvocationTargetException {
	initMacHandler(applicationClass, application, "com.apple.eawt.QuitHandler", "setQuitHandler", new InvocationHandler() {

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
	});
    }

    private void initMacAboutHandler(Class applicationClass, Object application) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, InvocationTargetException {
	initMacHandler(applicationClass, application, "com.apple.eawt.AboutHandler", "setAboutHandler", new InvocationHandler() {

	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		ArbilWindowManager.getSingleInstance().openAboutPage();
		return null;
	    }
	});
    }

    /**
     * Sets a MacOS application handler
     * @param applicationClass Application class (com.apple.eawt.Application)
     * @param application Application instance
     * @param interfaceName Name of handler interface
     * @param setMethodName Name of method on application that sets the handler
     * @param invocationHandler Invocation handler that does the work for application handler
     */
    private static void initMacHandler(Class applicationClass, Object application, String interfaceName, String setMethodName, InvocationHandler invocationHandler) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, InvocationTargetException {
	// Get class for named interface
	Class handlerInterface = Class.forName(interfaceName);
	// Create dynamic proxy using specified invocation handler
	Object handler = Proxy.newProxyInstance(handlerInterface.getClassLoader(), new Class[]{handlerInterface}, invocationHandler);
	// Set on specified application using specified method
	applicationClass.getMethod(setMethodName, handlerInterface).invoke(application, handler);
    }
}
