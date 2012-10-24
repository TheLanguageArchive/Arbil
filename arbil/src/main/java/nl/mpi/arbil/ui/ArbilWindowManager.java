/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.importexport.ArbilToHtmlConverter;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginDialogHandler.DialogueType;
import nl.mpi.arbil.ui.fieldeditors.ArbilLongFieldEditor;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.wizard.setup.ArbilSetupWizard;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.arbil.util.task.ArbilTaskListener;
import org.xml.sax.SAXException;

/**
 * Document : ArbilWindowManager
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilWindowManager implements MessageDialogHandler, WindowManager {

    private Hashtable<String, Component[]> windowList = new Hashtable<String, Component[]>();
    private Hashtable windowStatesHashtable;
    private JDesktopPane desktopPane;
    private JFrame linorgFrame;
    private ArbilTaskStatusBar statusBar;
    private final static int defaultWindowX = 50;
    private final static int defaultWindowY = 50;
    private final static int nextWindowWidth = 800;
    private final static int nextWindowHeight = 600;
    private int nextWindowX = defaultWindowX;
    private int nextWindowY = defaultWindowY;
    float fontScale = 1;
    private Hashtable<String, String> messageDialogQueue = new Hashtable<String, String>();
    private boolean messagesCanBeShown = false;
    boolean showMessageThreadrunning = false;
    private Collection<ArbilTaskListener> taskListeners = new HashSet<ArbilTaskListener>();
    private ApplicationVersionManager versionManager;
    private ImageBoxRenderer imageBoxRenderer;

    public void setVersionManager(ApplicationVersionManager versionManagerInstance) {
	versionManager = versionManagerInstance;
    }
    private SessionStorage sessionStorage;

    public void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private TreeHelper treeHelper;

    public void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private DataNodeLoader dataNodeLoader;

    public void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public void setImageBoxRenderer(ImageBoxRenderer imageBoxRenderer) {
	this.imageBoxRenderer = imageBoxRenderer;
    }

    public ArbilWindowManager() {
	desktopPane = new JDesktopPane();
	desktopPane.setBackground(new java.awt.Color(204, 204, 204));
	ArbilDragDrop.getSingleInstance().setTransferHandlerOnComponent(desktopPane);
    }

    public void setMessagesCanBeShown(boolean messagesCanBeShown) {
	// this should be set to true whent the main window has been shown, before this stage of loading messages should not be shown
	this.messagesCanBeShown = messagesCanBeShown;
    }

    public void loadGuiState(JFrame linorgFrameLocal, ArbilTaskStatusBar statusBar) {
	linorgFrame = linorgFrameLocal;
	this.statusBar = statusBar;
	try {
	    setStatusBarVisible(sessionStorage.loadBoolean("showStatusBar", true));
	    // load the saved states
	    windowStatesHashtable = (Hashtable) sessionStorage.loadObject("windowStates");

	    // set the main window position and size. Also puts window on the correct screen (relevant for maximization)
	    Object linorgFrameBounds = windowStatesHashtable.get("linorgFrameBounds");
	    if (linorgFrameBounds != null) {
		getMainFrame().setBounds((Rectangle) linorgFrameBounds);
	    }

	    // set window state (i.e. maximized or not)
	    getMainFrame().setExtendedState((Integer) windowStatesHashtable.get("linorgFrameExtendedState"));
	    if (getMainFrame().getExtendedState() == JFrame.ICONIFIED) {
		// start up iconified is just too confusing to the user
		getMainFrame().setExtendedState(JFrame.NORMAL);
	    }

	    if (windowStatesHashtable.containsKey("ScreenDeviceCount")) {
		int screenDeviceCount = ((Integer) windowStatesHashtable.get("ScreenDeviceCount"));
		if (screenDeviceCount > GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length) {
		    getMainFrame().setLocationRelativeTo(null);
		    // make sure the main frame is visible. for instance when a second monitor has been removed.
		    Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		    if (linorgFrameBounds instanceof Rectangle && ((Rectangle) linorgFrameBounds).intersects(new Rectangle(screenDimension))) {
			getMainFrame().setBounds(((Rectangle) linorgFrameBounds).intersection(new Rectangle(screenDimension)));
		    } else {
			getMainFrame().setBounds(0, 0, 800, 600);
			getMainFrame().setLocationRelativeTo(null);
		    }
		}
	    }
	} catch (Exception ex) {
	    System.out.println("load windowStates failed: " + ex.getMessage());
	    System.out.println("setting default windowStates");
	    windowStatesHashtable = new Hashtable();
	    getMainFrame().setBounds(0, 0, 800, 600);
	    getMainFrame().setLocationRelativeTo(null);
	    getMainFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	// set the split pane positions
	loadSplitPlanes(getMainFrame().getContentPane().getComponent(0));
    }

    public void openAboutPage() {
	ApplicationVersion appVersion = versionManager.getApplicationVersion();
	String messageString = "Archive Builder\n"
		+ "A local tool for organising linguistic data.\n"
		+ "Max Planck Institute for Psycholinguistics\n\n"
		+ "Application originally designed and developed by Peter Withers\n"
		+ "Current maintenance and extentions by Peter Withers and Twan Goosen\n"
		+ "Arbil also uses components of the IMDI API and Lamus Type Checker\n\n"
		+ "Version: " + appVersion.currentMajor + "." + appVersion.currentMinor + "." + appVersion.currentRevision + "\n"
		+ appVersion.lastCommitDate + "\n"
		+ "Compile Date: " + appVersion.compileDate + "\n\n"
		+ "Java version: " + System.getProperty("java.version") + " by " + System.getProperty("java.vendor") + "\n\n"
		+ "Copyright (C) 2012 Max Planck Institute for Psycholinguistics\n\n"
		+ "Arbil has been licensed under the GNU General Public License version 2.\n"
		+ "Click the button below to see the full version of this license.";

	final JTextArea textComponent = new JTextArea();
	textComponent.setText(messageString);
	textComponent.setEditable(false);
	textComponent.setOpaque(false);
	textComponent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	final JButton licenseButton = new JButton("Display license");
	licenseButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		showLicenseWindow();
	    }
	});

	final JButton closeButton = new JButton("Close");

	final JDialog aboutDialog = createModalDialog("About " + appVersion.applicationTitle, textComponent, null, licenseButton, closeButton);
	closeButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		aboutDialog.dispose();
	    }
	});

	aboutDialog.setVisible(true);
    }

    private void showLicenseWindow() {
	try {
	    final HtmlViewPane licensePane = new HtmlViewPane(getClass().getResource("/nl/mpi/arbil/resources/html/license/gpl2.html"));
	    final JButton closeButton = new JButton("Close");
	    final JDialog licenseDialog = createModalDialog("License", licensePane.createScrollPane(), new Dimension(800, 600), closeButton);
	    closeButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    licenseDialog.dispose();
		}
	    });
	    licenseDialog.setVisible(true);
	} catch (IOException ioEx) {
	    addMessageDialogToQueue("Error while trying to show license. See error log for details.", "Error");
	    BugCatcherManager.getBugCatcher().logError(ioEx);
	}
    }

    public void offerUserToSaveChanges() throws Exception {
	if (dataNodeLoader.nodesNeedSave()) {
	    if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(getMainFrame(),
		    "There are unsaved changes.\nSave now?", "Save Changes",
		    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
		dataNodeLoader.saveNodesNeedingSave(true);
	    } else {
		throw new Exception("user canceled save action");
	    }
	}
    }

    public File showEmptyExportDirectoryDialogue(String titleText) {
	boolean fileSelectDone = false;
	while (!fileSelectDone) {
	    File[] selectedFiles = showFileSelectBox(titleText + " Destination Directory", true, false, null, DialogueType.custom, null);
	    if (selectedFiles != null && selectedFiles.length > 0) {
		File destinationDirectory = selectedFiles[0];
		boolean mkdirsOkay = true;
		if (destinationDirectory != null && !destinationDirectory.exists()/* && parentDirectory.getParentFile().exists() */) {
		    // create the directory provided that the parent directory exists
		    // ths is here due the the way the mac file select gui leads the user to type in a new directory name
		    mkdirsOkay = destinationDirectory.mkdirs();
		}
		if (destinationDirectory == null || !mkdirsOkay || !destinationDirectory.exists()) {
		    JOptionPane.showMessageDialog(getMainFrame(), "The export directory\n\"" + destinationDirectory + "\"\ndoes not exist.\nPlease select or create a directory.", titleText, JOptionPane.PLAIN_MESSAGE);
		} else {
//                        if (!createdDirectory) {
//                            String newDirectoryName = JOptionPane.showInputDialog(linorgFrame, "Enter Export Name", titleText, JOptionPane.PLAIN_MESSAGE, null, null, "arbil_export").toString();
//                            try {
//                                destinationDirectory = new File(parentDirectory.getCanonicalPath() + File.separatorChar + newDirectoryName);
//                                destinationDirectory.mkdir();
//                            } catch (Exception e) {
//                                JOptionPane.showMessageDialog(LinorgWindowManager.getArbilHelpInstance().linorgFrame, "Could not create the export directory + \'" + newDirectoryName + "\'", titleText, JOptionPane.PLAIN_MESSAGE);
//                            }
//                        }
		    if (destinationDirectory.exists()) {
			if (destinationDirectory.list().length == 0) {
			    fileSelectDone = true;
			    return destinationDirectory;
			} else {
			    if (showConfirmDialogBox("The selected export directory is not empty.\nTo continue will create dupicates of existing files.\nDo you want to continue?", titleText)) {
				return destinationDirectory;
			    }
			    //JOptionPane.showMessageDialog(LinorgWindowManager.getArbilHelpInstance().linorgFrame, "The export directory must be empty", titleText, JOptionPane.PLAIN_MESSAGE);
			}
		    }
		}
	    } else {
		fileSelectDone = true;
	    }
	}
	return null;
    }

    public File[] showMetadataFileSelectBox(String titleText, boolean multipleSelect) {
	HashMap<String, FileFilter> fileFilterMap = new HashMap<String, FileFilter>(2);
	fileFilterMap.put("Metadata Files", new FileFilter() {
	    @Override
	    public boolean accept(File selectedFile) {
		if (selectedFile.isDirectory()) {
		    return true;
		}
		if (selectedFile.exists() && !selectedFile.isDirectory()) {
		    final String fileNameLowerCase = selectedFile.getName().toLowerCase();
		    if (fileNameLowerCase.endsWith(".cmdi")) {
			return true;
		    }
		    if (fileNameLowerCase.endsWith(".imdi")) {
			return true;
		    }
		}
		return false;
	    }

	    @Override
	    public String getDescription() {
		return "Metadata Files";
	    }
	});
	return showFileSelectBox(titleText, false, multipleSelect, fileFilterMap, DialogueType.open, null);
    }

    public File[] showDirectorySelectBox(String titleText, boolean multipleSelect) {
	HashMap fileFilterMap = new HashMap<String, FileFilter>(2);
	// this filter is only cosmetic but gives the user an indication of what to select
	FileFilter imdiFileFilter = new FileFilter() {
	    public String getDescription() {
		return "Directories";
	    }

	    @Override
	    public boolean accept(File selectedFile) {
		return (selectedFile.exists() && selectedFile.isDirectory());
	    }
	};
	fileFilterMap.put("Directories", imdiFileFilter);
	return showFileSelectBox(titleText, true, multipleSelect, fileFilterMap, DialogueType.custom, null);
    }

    public File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, Map<String, javax.swing.filechooser.FileFilter> fileFilterMap, PluginDialogHandler.DialogueType dialogueType, JComponent customAccessory) {
	// test for os: if mac or file then awt else for other and directory use swing
	// save/load last directory accoring to the title of the dialogue
	//Hashtable<String, File> fileSelectLocationsHashtable;
	File workingDirectory = null;
	String workingDirectoryPathString = sessionStorage.loadString("fileSelect." + titleText);
	if (workingDirectoryPathString == null) {
	    workingDirectory = new File(System.getProperty("user.home"));
	} else {
	    workingDirectory = new File(workingDirectoryPathString);
	}
	File lastUsedWorkingDirectory;

	File[] returnFile;

	JFileChooser fileChooser = createFileChooser(fileFilterMap);

	if (directorySelectOnly) {
	    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	} else {
	    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}
	fileChooser.setCurrentDirectory(workingDirectory);
	fileChooser.setMultiSelectionEnabled(multipleSelect);
	if (customAccessory != null) {
	    // a custom accessory can be provided to add additional controls to the dialogue
	    fileChooser.setAccessory(customAccessory);
	}
	final int showDialogResult;
	switch (dialogueType) {
	    case open:
		fileChooser.setDialogTitle(titleText);
		showDialogResult = fileChooser.showOpenDialog(getMainFrame());
		break;
	    case save:
		fileChooser.setDialogTitle(titleText);
		showDialogResult = fileChooser.showSaveDialog(getMainFrame());
		break;
	    default:
		showDialogResult = fileChooser.showDialog(getMainFrame(), titleText);
	}
	if (JFileChooser.APPROVE_OPTION == showDialogResult) {
	    returnFile = fileChooser.getSelectedFiles();
	    if (returnFile.length == 0) {
		returnFile = new File[]{fileChooser.getSelectedFile()};
	    }
	    storeSelectedMetadataFileFilter(fileFilterMap, fileChooser);
	} else {
	    returnFile = null;
	}
	if (returnFile != null && returnFile.length == 1 && !returnFile[0].exists()) {
	    if (returnFile[0].getName().equals(returnFile[0].getParentFile().getName())) {
		returnFile[0] = returnFile[0].getParentFile();
	    }
	}
	lastUsedWorkingDirectory = fileChooser.getCurrentDirectory();
	// save last use working directory
	sessionStorage.saveString("fileSelect." + titleText, lastUsedWorkingDirectory.getAbsolutePath());
	return returnFile;
    }

    private JFileChooser createFileChooser(Map<String, FileFilter> fileFilterMap) {
	JFileChooser fileChooser = new JFileChooser();
	if (fileFilterMap != null) {
	    for (FileFilter filter : fileFilterMap.values()) {
		fileChooser.addChoosableFileFilter(filter);
	    }
	    String lastFileFilter = sessionStorage.loadString(SessionStorage.PARAM_LAST_FILE_FILTER);
	    if (lastFileFilter != null && fileFilterMap.containsKey(lastFileFilter)) {
		fileChooser.setFileFilter(fileFilterMap.get(lastFileFilter));
	    }
	}
	return fileChooser;
    }

    private void storeSelectedMetadataFileFilter(Map<String, FileFilter> fileFilterMap, JFileChooser fileChooser) {
	// Store selected file filter
	FileFilter selectedFilter = fileChooser.getFileFilter();
	if (selectedFilter != null && fileFilterMap != null) {
	    if (fileFilterMap.containsValue(selectedFilter)) {
		for (Map.Entry<String, FileFilter> filterEntry : fileFilterMap.entrySet()) {
		    if (filterEntry.getValue() == selectedFilter) {
			sessionStorage.saveString(SessionStorage.PARAM_LAST_FILE_FILTER, filterEntry.getKey());
			return;
		    }
		}
	    }
	}
    }

    public boolean showConfirmDialogBox(String messageString, String messageTitle) {
	if (messageTitle == null) {
	    messageTitle = "Arbil";
	}
	if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(getMainFrame(),
		messageString, messageTitle,
		JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
	    return true;
	} else {
	    return false;
	}
    }

    public void addMessageDialogToQueue(String messageString, String messageTitle) {
	if (messageTitle == null) {
	    messageTitle = versionManager.getApplicationVersion().applicationTitle;
	}
	String currentMessage = messageDialogQueue.get(messageTitle);
	if (currentMessage != null) {
	    messageString = messageString + "\n-------------------------------\n" + currentMessage;
	}
	messageDialogQueue.put(messageTitle, messageString);
	showMessageDialogQueue();
    }

    private void applyWindowDefaults(JInternalFrame currentInternalFrame) {
	int tempWindowWidth, tempWindowHeight;
	if (desktopPane.getWidth() > nextWindowWidth) {
	    tempWindowWidth = nextWindowWidth;
	} else {
	    tempWindowWidth = desktopPane.getWidth() - 50;
	}
	if (desktopPane.getHeight() > nextWindowHeight) {
	    tempWindowHeight = nextWindowHeight;
	} else {
	    tempWindowHeight = desktopPane.getHeight() - 50;
	}
	if (tempWindowHeight < 100) {
	    tempWindowHeight = 100;
	}
	currentInternalFrame.setSize(tempWindowWidth, tempWindowHeight);

	currentInternalFrame.setClosable(true);
	currentInternalFrame.setIconifiable(true);
	currentInternalFrame.setMaximizable(true);
	currentInternalFrame.setResizable(true);
	currentInternalFrame.setVisible(true);

//        selectedFilesFrame.setSize(destinationComp.getWidth(), 300);
//        selectedFilesFrame.setRequestFocusEnabled(false);
//        selectedFilesFrame.getContentPane().add(selectedFilesPanel, java.awt.BorderLayout.CENTER);
//        selectedFilesFrame.setBounds(0, 0, 641, 256);
//        destinationComp.add(selectedFilesFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
	// set the window position so that they are cascaded
	currentInternalFrame.setLocation(nextWindowX, nextWindowY);
	nextWindowX += Math.max(10, currentInternalFrame.getInsets().left);
	nextWindowY += Math.max(10, currentInternalFrame.getInsets().top - 10);
	// TODO: it would be nice to use the JInternalFrame's title bar height to increment the position
	if (nextWindowX + tempWindowWidth > desktopPane.getWidth()) {
	    nextWindowX = 0;
	}
	if (nextWindowY + tempWindowHeight > desktopPane.getHeight()) {
	    nextWindowY = 0;
	}
    }

    private synchronized void showMessageDialogQueue() {
	if (!showMessageThreadrunning) {
	    new Thread("showMessageThread") {
		public void run() {
		    try {
			sleep(100);
		    } catch (Exception ex) {
			BugCatcherManager.getBugCatcher().logError(ex);
		    }
		    showMessageThreadrunning = true;
		    if (messagesCanBeShown) {
			while (messageDialogQueue.size() > 0) {
			    final String messageTitle = messageDialogQueue.keys().nextElement();
			    final String messageText = messageDialogQueue.remove(messageTitle);
			    if (messageText != null) {
				SwingUtilities.invokeLater(new Runnable() {
				    public void run() {
					JOptionPane.showMessageDialog(getMainFrame(), messageText, messageTitle, JOptionPane.PLAIN_MESSAGE);
				    }
				});
			    }
			}
		    }
		    showMessageThreadrunning = false;
		}
	    }.start();
	}
    }

    public void showSetupWizardIfFirstRun() {
	if (!treeHelper.locationsHaveBeenAdded()
		&& !"yes".equals(sessionStorage.loadString(SessionStorage.PARAM_WIZARD_RUN))) {
	    sessionStorage.saveString(SessionStorage.PARAM_WIZARD_RUN, "yes");
	    new ArbilSetupWizard(getMainFrame()).showModalDialog();
	}
    }

    public void openIntroductionPage() {
	// open the introduction page
	// TODO: always get this page from the server if available, but also save it for off line use
//        URL introductionUrl = this.getClass().getResource("/nl/mpi/arbil/resources/html/Introduction.html");
//        openUrlWindowOnce("Introduction", introductionUrl);
//        get remote file to local disk
//        if local file exists then open that
//        else open the one in the jar file
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  The features html file has been limited to the version in the jar (not the server), so that it is specific to the version of linorg in the jar. //
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        String remoteUrl = "http://www.mpi.nl/tg/j2se/jnlp/linorg/Features.html";
//        String cachePath = GuiHelper.linorgSessionStorage.updateCache(remoteUrl, true);
//        System.out.println("cachePath: " + cachePath);
//        URL destinationUrl = null;
//        try {
//            if (new File(cachePath).exists()) {
//                destinationUrl = new File(cachePath).toURL();
//            }
//        } catch (Exception ex) {
//        }
//        if (destinationUrl == null) {
//        destinationUrl = this.getClass().getResource("/nl/mpi/arbil/resources/html/Features.html");
////        }
//        System.out.println("destinationUrl: " + destinationUrl);
//        openUrlWindowOnce("Features/Known Bugs", destinationUrl);

	initWindows();

	if (!treeHelper.locationsHaveBeenAdded()) {
	    System.out.println("no local locations found, showing help window");
	    try {
		ArbilHelp helpComponent = ArbilHelp.getArbilHelpInstance();
		if (null == focusWindow(ArbilHelp.helpWindowTitle)) {
		    createWindow(ArbilHelp.helpWindowTitle, helpComponent);
		}
		helpComponent.setCurrentPage(ArbilHelp.IMDI_HELPSET, ArbilHelp.INTRODUCTION_PAGE);
	    } catch (IOException ex) {
		// Ignore, don't show help window
		BugCatcherManager.getBugCatcher().logError(ex);
	    } catch (SAXException ex) {
		// Ignore, don't show help window
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
	startKeyListener();
	setMessagesCanBeShown(true);
	showMessageDialogQueue();
    }

    /**
     * Loads previously saved windows and restores their state
     */
    private void initWindows() {
	try {
	    // load the saved windows
	    Hashtable windowListHashtable = (Hashtable) sessionStorage.loadObject("openWindows");
	    for (Enumeration windowNamesEnum = windowListHashtable.keys(); windowNamesEnum.hasMoreElements();) {
		String currentWindowName = windowNamesEnum.nextElement().toString();
		System.out.println("currentWindowName: " + currentWindowName);
		ArbilWindowState windowState;
		Object windowStateObject = windowListHashtable.get(currentWindowName);

//                Vector imdiURLs;
//                Point windowLocation = null;
//                Dimension windowSize = null;

		if (windowStateObject instanceof Vector) {
		    // In previous versions or Arbil, window state was stored as a vector of IMDI urls
		    windowState = new ArbilWindowState();
		    windowState.currentNodes = (Vector) windowStateObject;
		} else if (windowStateObject instanceof ArbilWindowState) {
		    windowState = (ArbilWindowState) windowStateObject;
		} else {
		    throw new Exception("Unknown window state format");
		}

		//= (Vector) windowListHashtable.get(currentWindowName);
//                System.out.println("imdiEnumeration: " + imdiEnumeration);
		if (windowState.currentNodes != null) {
		    ArbilDataNode[] imdiObjectsArray = new ArbilDataNode[windowState.currentNodes.size()];
		    for (int arrayCounter = 0; arrayCounter < imdiObjectsArray.length; arrayCounter++) {
			try {
			    imdiObjectsArray[arrayCounter] = (dataNodeLoader.getArbilDataNode(null, new URI(windowState.currentNodes.elementAt(arrayCounter).toString())));
			} catch (URISyntaxException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			}
		    }

		    // Create window array for open method to put new window reference in
		    Component[] window = new Component[1];
		    if (windowState.windowType == ArbilWindowState.ArbilWindowType.nodeTable) {
			openFloatingTableGetModel(imdiObjectsArray, currentWindowName, window, windowState.fieldView);
		    } else if (windowState.windowType == ArbilWindowState.ArbilWindowType.subnodesPanel) {
			openFloatingSubnodesWindowOnce(imdiObjectsArray[0], currentWindowName, window);
		    }

		    if (window[0] != null) {
			// Set size of new window from saved state
			if (windowState.size != null) {
			    window[0].setSize(windowState.size);
			}
			// Set location new window from saved state
			if (windowState.location != null) {
			    window[0].setLocation(fixLocation(windowState.location));
			}
		    }
		}

		//openFloatingTable(null, currentWindowName);
	    }
	    System.out.println("done loading windowStates");
	} catch (Exception ex) {
	    windowStatesHashtable = new Hashtable();
	    System.out.println("load windowStates failed: " + ex.getMessage());
	}
    }

    public void loadSplitPlanes(Component targetComponent) {
	//System.out.println("loadSplitPlanes: " + targetComponent);
	if (targetComponent instanceof JSplitPane) {
	    System.out.println("loadSplitPlanes: " + targetComponent.getName());
	    Object linorgSplitPosition = windowStatesHashtable.get(targetComponent.getName());
	    if (linorgSplitPosition instanceof Integer) {
		System.out.println(targetComponent.getName() + ": " + linorgSplitPosition);
		((JSplitPane) targetComponent).setDividerLocation((Integer) linorgSplitPosition);
	    } else {
		if (targetComponent.getName().equals("rightSplitPane")) {
		    ((JSplitPane) targetComponent).setDividerLocation(150);
		} else {
		    //leftSplitPane  leftLocalSplitPane rightSplitPane)
		    ((JSplitPane) targetComponent).setDividerLocation(200);
		}
	    }
	    for (Component childComponent : ((JSplitPane) targetComponent).getComponents()) {
		loadSplitPlanes(childComponent);
	    }
	}
	if (targetComponent instanceof JPanel) {
	    for (Component childComponent : ((JPanel) targetComponent).getComponents()) {
		loadSplitPlanes(childComponent);
	    }
	}
    }

    public void saveSplitPlanes(Component targetComponent) {
	//System.out.println("saveSplitPlanes: " + targetComponent);
	if (targetComponent instanceof JSplitPane) {
	    System.out.println("saveSplitPlanes: " + targetComponent.getName());
	    windowStatesHashtable.put(targetComponent.getName(), ((JSplitPane) targetComponent).getDividerLocation());
	    for (Component childComponent : ((JSplitPane) targetComponent).getComponents()) {
		saveSplitPlanes(childComponent);
	    }
	}
	if (targetComponent instanceof JPanel) {
	    for (Component childComponent : ((JPanel) targetComponent).getComponents()) {
		saveSplitPlanes(childComponent);
	    }
	}
    }

    /**
     * Resets all windows to default size and location
     */
    public void resetWindows() {
	nextWindowX = defaultWindowX;
	nextWindowY = defaultWindowY;
	for (Enumeration windowNamesEnum = windowList.keys(); windowNamesEnum.hasMoreElements();) {
	    String currentWindowName = windowNamesEnum.nextElement().toString();
	    System.out.println("currentWindowName: " + currentWindowName);
	    // set the value of the windowListHashtable to be the imdi urls rather than the windows
	    Object windowObject = ((Component[]) windowList.get(currentWindowName))[0];
	    if (windowObject != null) {
		applyWindowDefaults((JInternalFrame) windowObject);
	    }
	}
    }

    public void saveWindowStates() {
	// loop windowList and make a hashtable of window names with a vector of the imdinodes displayed, then save the hashtable
	try {
	    // collect the main window size and position for saving
	    windowStatesHashtable.put("linorgFrameBounds", getMainFrame().getBounds());

	    windowStatesHashtable.put("ScreenDeviceCount", GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length);
	    windowStatesHashtable.put("linorgFrameExtendedState", getMainFrame().getExtendedState());
	    // collect the split pane positions for saving
	    saveSplitPlanes(getMainFrame().getContentPane().getComponent(0));
	    // save the collected states
	    sessionStorage.saveObject(windowStatesHashtable, "windowStates");
	    // save the windows
	    Hashtable windowListHashtable = new Hashtable();
	    //Hashtable windowDimensions = new Hashtable();
	    //Hashtable windowLocations = new Hashtable();
	    //(Hashtable) windowList.clone();
	    for (Enumeration windowNamesEnum = windowList.keys(); windowNamesEnum.hasMoreElements();) {
		ArbilWindowState windowState = new ArbilWindowState();

		String currentWindowName = windowNamesEnum.nextElement().toString();
		System.out.println("currentWindowName: " + currentWindowName);
		// set the value of the windowListHashtable to be the imdi urls rather than the windows
		Object windowObject = ((Component[]) windowList.get(currentWindowName))[0];
		try {
		    if (windowObject != null) {
			windowState.location = ((JInternalFrame) windowObject).getLocation();
			windowState.size = ((JInternalFrame) windowObject).getSize();
			Object currentComponent = ((JInternalFrame) windowObject).getContentPane().getComponent(0);
			if (currentComponent != null) {
			    if (currentComponent instanceof ArbilSplitPanel) {
				// Store as a node table window
				windowState.windowType = ArbilWindowState.ArbilWindowType.nodeTable;

				// if this table has no nodes then don't save it
				if (0 < ((ArbilSplitPanel) currentComponent).arbilTable.getRowCount()) {

				    ArbilTable table = ((ArbilSplitPanel) currentComponent).arbilTable;

				    // Store field view (columns shown + widths)
				    table.updateStoredColumnWidhts();
				    windowState.fieldView = table.getArbilTableModel().getFieldView();

				    Vector currentNodesVector = new Vector(Arrays.asList(table.getArbilTableModel().getArbilDataNodesURLs()));
				    windowState.currentNodes = currentNodesVector;
				    System.out.println("saved");
				}
			    } else if (currentComponent instanceof ArbilSubnodesScrollPane) {
				// Store as a subnodes panel window
				windowState.windowType = ArbilWindowState.ArbilWindowType.subnodesPanel;

				// Set top level node as only entry in the current nodes vector
				Vector nodeVector = new Vector(1);
				nodeVector.add(((ArbilSubnodesScrollPane) currentComponent).getDataNode().getUrlString());
				windowState.currentNodes = nodeVector;
			    }
			}
			windowListHashtable.put(currentWindowName, windowState);
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
//                    System.out.println("Exception: " + ex.getMessage());
		}
	    }
	    // save the windows
	    sessionStorage.saveObject(windowListHashtable, "openWindows");

	    System.out.println("saved windowStates");
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
//            System.out.println("save windowStates exception: " + ex.getMessage());
	}
    }

    private String addWindowToList(String windowName, final JInternalFrame windowFrame) {
	int instanceCount = 0;
	String currentWindowName = windowName;
	while (windowList.containsKey(currentWindowName)) {
	    currentWindowName = windowName + "(" + ++instanceCount + ")";
	}
	JMenuItem windowMenuItem = new JMenuItem();
	windowMenuItem.setText(currentWindowName);
	windowMenuItem.setName(currentWindowName);
	windowFrame.setName(currentWindowName);
	windowMenuItem.setActionCommand(currentWindowName);
	windowMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    focusWindow(evt.getActionCommand());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	windowFrame.addInternalFrameListener(new InternalFrameAdapter() {
	    @Override
	    public void internalFrameClosed(InternalFrameEvent e) {
		String windowName = e.getInternalFrame().getName();
		System.out.println("Closing window: " + windowName);
		Component[] windowAndMenu = windowList.get(windowName);

		// Remove from windows menu
		if (ArbilMenuBar.windowMenu != null && windowAndMenu != null) {
		    ArbilMenuBar.windowMenu.remove(windowAndMenu[1]);
		}

		// Check if the child component(s) implement ArbilWindowComponent. If so, call their window closed method
		for (Component childComponent : ((JInternalFrame) windowFrame).getContentPane().getComponents()) {
		    if (childComponent instanceof ArbilWindowComponent) {
			((ArbilWindowComponent) childComponent).arbilWindowClosed();
		    }
		}
		windowList.remove(windowName);
		super.internalFrameClosed(e);
	    }
	});
	windowList.put(currentWindowName, new Component[]{windowFrame, windowMenuItem});
	if (ArbilMenuBar.windowMenu != null) {
	    ArbilMenuBar.windowMenu.add(windowMenuItem);
	}
	return currentWindowName;
    }

    public void stopEditingInCurrentWindow() {
	// when saving make sure the current editing table or long field editor saves its data first
	Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
	while (focusedComponent != null) {
	    if (focusedComponent instanceof ArbilLongFieldEditor) {
		((ArbilLongFieldEditor) focusedComponent).storeChanges();
	    } else if (focusedComponent instanceof JTable) {
		TableCellEditor editor = ((JTable) focusedComponent).getCellEditor();
		if (editor != null) {
		    editor.stopCellEditing();
		}
	    }
	    focusedComponent = focusedComponent.getParent();
	}
    }

    public void closeAllWindows() {
	for (JInternalFrame focusedWindow : desktopPane.getAllFrames()) {
	    if (focusedWindow != null) {
		String windowName = focusedWindow.getName();
		Component[] windowAndMenu = (Component[]) windowList.get(windowName);
		if (windowAndMenu != null && ArbilMenuBar.windowMenu != null) {
		    ArbilMenuBar.windowMenu.remove(windowAndMenu[1]);
		}
		windowList.remove(windowName);
		desktopPane.remove(focusedWindow);
	    }
	}
	desktopPane.repaint();
    }

    public JInternalFrame focusWindow(String windowName) {
	if (windowList.containsKey(windowName)) {
	    Object windowObject = ((Component[]) windowList.get(windowName))[0];
	    try {
		if (windowObject != null) {
		    ((JInternalFrame) windowObject).setIcon(false);
		    ((JInternalFrame) windowObject).setSelected(true);
		    return (JInternalFrame) windowObject;
		}
	    } catch (Exception ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
//            System.out.println(ex.getMessage());
	    }
	}
	return null;
    }

    private void startKeyListener() {

//        desktopPane.addKeyListener(new KeyAdapter() {
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                System.out.println("keyPressed");
//                if (e.VK_W == e.getKeyCode()){
//                    System.out.println("VK_W");
//                }
//                super.keyPressed(e);
//            }
//        
//        });

	Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
	    public void eventDispatched(AWTEvent e) {
		boolean isKeybordRepeat = false;
		if (e instanceof KeyEvent) {
		    // only consider key release events
		    if (e.getID() == KeyEvent.KEY_RELEASED) {
			// work around for jvm in linux
			// due to the bug in the jvm for linux the keyboard repeats are shown as real key events, so we attempt to prevent ludicrous key events being used here
			KeyEvent nextPress = (KeyEvent) Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(KeyEvent.KEY_PRESSED);
			if (nextPress != null) {
			    // the next key event is at the same time as this event
			    if ((nextPress.getWhen() == ((KeyEvent) e).getWhen())) {
				// the next key code is the same as this event
				if (((nextPress.getKeyCode() == ((KeyEvent) e).getKeyCode()))) {
				    isKeybordRepeat = true;
				}
			    }
			}
			// end work around for jvm in linux
			if (!isKeybordRepeat) {
//                            System.out.println("KeyEvent.paramString: " + ((KeyEvent) e).paramString());
//                            System.out.println("KeyEvent.getWhen: " + ((KeyEvent) e).getWhen());
			    if ((((KeyEvent) e).isMetaDown() || ((KeyEvent) e).isControlDown()) && ((KeyEvent) e).getKeyCode() == KeyEvent.VK_W) {
				JInternalFrame[] windowsToClose;
				if (((KeyEvent) e).isShiftDown()) {
				    windowsToClose = getDesktopPane().getAllFrames();
				} else {
				    windowsToClose = new JInternalFrame[]{getDesktopPane().getSelectedFrame()};
				}
				for (JInternalFrame focusedWindow : windowsToClose) {
				    if (focusedWindow != null) {
					String windowName = focusedWindow.getName();
					Component[] windowAndMenu = (Component[]) windowList.get(windowName);
					if (windowAndMenu != null && ArbilMenuBar.windowMenu != null) {
					    ArbilMenuBar.windowMenu.remove(windowAndMenu[1]);
					}
					windowList.remove(windowName);
					getDesktopPane().remove(focusedWindow);
					try {
					    JInternalFrame[] allWindows = getDesktopPane().getAllFrames();
					    if (allWindows.length > 0) {
						JInternalFrame topMostWindow = allWindows[0];
						if (topMostWindow != null) {
						    System.out.println("topMostWindow: " + topMostWindow);
						    topMostWindow.setIcon(false);
						    topMostWindow.setSelected(true);
						}
					    }
					} catch (Exception ex) {
					    BugCatcherManager.getBugCatcher().logError(ex);
//                                        System.out.println(ex.getMessage());
					}
				    }
				}
				getDesktopPane().repaint();
			    }
			    if ((((KeyEvent) e).getKeyCode() == KeyEvent.VK_TAB && ((KeyEvent) e).isControlDown())) {
				// the [meta `] is consumed by the operating system, the only way to enable the back quote key for window switching is to use separate windows and rely on the OS to do the switching
				// || (((KeyEvent) e).getKeyCode() == KeyEvent.VK_BACK_QUOTE && ((KeyEvent) e).isMetaDown())
				try {
				    JInternalFrame[] allWindows = getDesktopPane().getAllFrames();
				    int targetLayerInt;
				    if (((KeyEvent) e).isShiftDown()) {
					allWindows[0].moveToBack();
					targetLayerInt = 1;
				    } else {
					targetLayerInt = allWindows.length - 1;
				    }
				    allWindows[targetLayerInt].setIcon(false);
				    allWindows[targetLayerInt].setSelected(true);
				} catch (Exception ex) {
				    BugCatcherManager.getBugCatcher().logError(ex);
//                                    System.out.println(ex.getMessage());
				}
			    }
			    if ((((KeyEvent) e).isMetaDown() || ((KeyEvent) e).isControlDown()) && (((KeyEvent) e).getKeyCode() == KeyEvent.VK_MINUS || ((KeyEvent) e).getKeyCode() == KeyEvent.VK_EQUALS || ((KeyEvent) e).getKeyCode() == KeyEvent.VK_PLUS)) {
				if (((KeyEvent) e).getKeyCode() != KeyEvent.VK_MINUS) {
				    fontScale = fontScale + (float) 0.1;
				} else {
				    fontScale = fontScale - (float) 0.1;
				}
				if (fontScale < 1) {
				    fontScale = 1;
				}
				System.out.println("fontScale: " + fontScale);
				UIDefaults defaults = UIManager.getDefaults();
				Enumeration keys = defaults.keys();
				while (keys.hasMoreElements()) {
				    Object key = keys.nextElement();
				    Object value = defaults.get(key);
				    if (value != null && value instanceof Font) {
					UIManager.put(key, null);
					Font font = UIManager.getFont(key);
					if (font != null) {
					    float size = font.getSize2D();
					    UIManager.put(key, new FontUIResource(font.deriveFont(size * fontScale)));
					}
				    }
				}
				SwingUtilities.updateComponentTreeUI(getDesktopPane().getParent().getParent());
			    }
			    if ((((KeyEvent) e).isMetaDown() || ((KeyEvent) e).isControlDown()) && ((KeyEvent) e).getKeyCode() == KeyEvent.VK_F) {
				JInternalFrame windowToSearch = getDesktopPane().getSelectedFrame();
				//System.out.println(windowToSearch.getContentPane());
				for (Component childComponent : windowToSearch.getContentPane().getComponents()) {
				    // loop through all the child components in the window (there will probably only be one)
				    if (childComponent instanceof ArbilSplitPanel) {
					((ArbilSplitPanel) childComponent).showSearchPane();
				    }
				}
			    }
			}
		    }
		}
	    }
	}, AWTEvent.KEY_EVENT_MASK);
    }

    public JDialog createModalDialog(String title, Component contentsComponent, Dimension size, JButton... buttons) {
	//JFrame frame = new JFrame();
	JDialog dialog = new JDialog(linorgFrame, title, true);
	dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	dialog.setLayout(new BorderLayout());
	dialog.add(contentsComponent, BorderLayout.CENTER);

	dialog.setTitle(title);
	dialog.setName(title);

	dialog.setLocationRelativeTo(linorgFrame);

	if (buttons.length > 0) {
	    final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
	    for (JButton button : buttons) {
		buttonPanel.add(button);
	    }
	    dialog.add(buttonPanel, BorderLayout.SOUTH);
	}

	if (size != null) {
	    dialog.setSize(size);
	} else {
	    dialog.pack();
	}
	return dialog;
    }

    public JInternalFrame createWindow(String windowTitle, Component contentsComponent) {
	JInternalFrame currentInternalFrame = new javax.swing.JInternalFrame();
	currentInternalFrame.setLayout(new BorderLayout());
	//        GuiHelper.arbilDragDrop.addTransferHandler(currentInternalFrame);
	currentInternalFrame.add(contentsComponent, BorderLayout.CENTER);
	windowTitle = addWindowToList(windowTitle, currentInternalFrame);

	currentInternalFrame.setTitle(windowTitle);
	currentInternalFrame.setToolTipText(windowTitle);
	currentInternalFrame.setName(windowTitle);

	applyWindowDefaults(currentInternalFrame);


	desktopPane.add(currentInternalFrame, 0);
	try {
	    // prevent the frame focus process consuming mouse events that should be recieved by the jtable etc.
	    currentInternalFrame.setSelected(true);
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
//            System.out.println(ex.getMessage());
	}

	// Add frame listener that puts windows with negative y-positions back on the desktop pane
	currentInternalFrame.addInternalFrameListener(new InternalFrameAdapter() {
	    @Override
	    public void internalFrameDeactivated(InternalFrameEvent e) {
		fixLocation(e.getInternalFrame());
	    }

	    @Override
	    public void internalFrameActivated(InternalFrameEvent e) {
		fixLocation(e.getInternalFrame());
	    }

	    private void fixLocation(final JInternalFrame frame) {
		if (frame.getLocation().getY() < 0) {
		    frame.setLocation(new Point((int) frame.getLocation().getX(), 0));
		}
	    }
	});

	return currentInternalFrame;
    }

    public JEditorPane openUrlWindowOnce(String frameTitle, URL locationUrl) {
	JEditorPane htmlDisplay = new JEditorPane();
	htmlDisplay.setEditable(false);
	htmlDisplay.setContentType("text/html");
	try {
	    htmlDisplay.setPage(locationUrl);
	    htmlDisplay.addHyperlinkListener(new ArbilHyperlinkListener());

	    //gridViewInternalFrame.setMaximum(true);
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
//            System.out.println(ex.getMessage());
	}

	JInternalFrame existingWindow = focusWindow(frameTitle);
	if (existingWindow == null) {
//            return openUrlWindow(frameTitle, htmlDisplay);
	    JScrollPane jScrollPane6;
	    jScrollPane6 = new javax.swing.JScrollPane();
	    jScrollPane6.setViewportView(htmlDisplay);
	    createWindow(frameTitle, jScrollPane6);
	} else {
	    ((JScrollPane) existingWindow.getContentPane().getComponent(0)).setViewportView(htmlDisplay);
	}
	return htmlDisplay;
    }

    public void openSearchTable(ArbilNode[] selectedNodes, String frameTitle) {
	// Create tabel with model and split panel to show it in
	ArbilTableModel resultsTableModel = new ArbilTableModel(imageBoxRenderer);
	ArbilTable arbilTable = new ArbilTable(resultsTableModel, frameTitle);
	arbilTable.setAllowNodeDrop(false);
	ArbilSplitPanel tablePanel = new ArbilSplitPanel(arbilTable);

	// Create window with search table in center
	JInternalFrame searchFrame = this.createWindow(frameTitle, tablePanel);
	// Add search panel above
	ArbilNodeSearchPanel searchPanel = new ArbilNodeSearchPanel(searchFrame, resultsTableModel, selectedNodes);
	searchFrame.add(searchPanel, BorderLayout.NORTH);

	// Prepare table panel and window for display
	tablePanel.setSplitDisplay();
	tablePanel.addFocusListener(searchFrame);
	searchFrame.pack();
    }

    public void openFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
	openFloatingTableOnceGetModel(rowNodesArray, frameTitle);
    }

    public void openFloatingTableOnce(ArbilDataNode[] rowNodesArray, String frameTitle) {
	openFloatingTableOnceGetModel(rowNodesArray, frameTitle);
    }

    public void openFloatingTable(ArbilDataNode[] rowNodesArray, String frameTitle) {
	openFloatingTableGetModel(rowNodesArray, frameTitle, null, null);
    }

    public ArbilTableModel openFloatingTableOnceGetModel(URI[] rowNodesArray, String frameTitle) {
	ArbilDataNode[] tableNodes = new ArbilDataNode[rowNodesArray.length];
	ArrayList<String> fieldPathsToHighlight = new ArrayList<String>();
	for (int arrayCounter = 0; arrayCounter < rowNodesArray.length; arrayCounter++) {
	    try {
		if (rowNodesArray[arrayCounter] != null) {
		    ArbilDataNode parentNode = dataNodeLoader.getArbilDataNode(null, new URI(rowNodesArray[arrayCounter].toString().split("#")[0]));
//                parentNode.waitTillLoaded();
		    String fieldPath = rowNodesArray[arrayCounter].getFragment();
		    String parentNodeFragment;
		    if (parentNode.nodeTemplate == null) {
			BugCatcherManager.getBugCatcher().logError(new Exception("nodeTemplate null in: " + parentNode.getUrlString()));
			parentNodeFragment = "";
		    } else {
			parentNodeFragment = parentNode.nodeTemplate.getParentOfField(fieldPath);
		    }
		    URI targetNode;
		    // note that the url has already be encoded and so we must not use the separate parameter version of new URI otherwise it would be encoded again which we do not want
		    if (parentNodeFragment.length() > 0) {
			targetNode = new URI(rowNodesArray[arrayCounter].toString().split("#")[0] + "#" + parentNodeFragment);
		    } else {
			targetNode = new URI(rowNodesArray[arrayCounter].toString().split("#")[0]);
		    }
		    tableNodes[arrayCounter] = dataNodeLoader.getArbilDataNode(null, targetNode);
		    fieldPathsToHighlight.add(fieldPath);
		}
	    } catch (URISyntaxException ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
	ArbilTableModel targetTableModel = openFloatingTableOnceGetModel(tableNodes, frameTitle);
	targetTableModel.highlightMatchingFieldPaths(fieldPathsToHighlight.toArray(new String[]{}));
	return targetTableModel;
    }

    public ArbilTableModel openAllChildNodesInFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
	HashSet<ArbilDataNode> tableNodes = new HashSet();
	for (int arrayCounter = 0; arrayCounter < rowNodesArray.length; arrayCounter++) {
//            try {
	    ArbilDataNode currentNode = dataNodeLoader.getArbilDataNode(null, rowNodesArray[arrayCounter]);
	    tableNodes.add(currentNode);
	    for (ArbilDataNode currentChildNode : currentNode.getAllChildren()) {
		tableNodes.add(currentChildNode);
	    }
//            } catch (URISyntaxException ex) {
//                BugCatcherManager.getBugCatcher().logError(ex);
//            }
	}
	return openFloatingTableOnceGetModel(tableNodes.toArray(new ArbilDataNode[]{}), frameTitle);
    }

    public ArbilTableModel openFloatingTableOnceGetModel(ArbilDataNode[] rowNodesArray, String frameTitle) {
	if (rowNodesArray.length == 1 && rowNodesArray[0] != null && rowNodesArray[0].isInfoLink) {
	    try {
		if (rowNodesArray[0].getUrlString().toLowerCase().endsWith(".html") || rowNodesArray[0].getUrlString().toLowerCase().endsWith(".txt")) {
		    openUrlWindowOnce(rowNodesArray[0].toString(), rowNodesArray[0].getURI().toURL());
		    return null;
		}
	    } catch (MalformedURLException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
	    }
	}
	// open find a table containing exactly the same nodes as requested or create a new table
	for (Component[] currentWindow : windowList.values().toArray(new Component[][]{})) {
	    // loop through all the windows
	    for (Component childComponent : ((JInternalFrame) currentWindow[0]).getContentPane().getComponents()) {
		// loop through all the child components in the window (there will probably only be one)
		if (childComponent instanceof ArbilSplitPanel) {
		    // only consider components with a LinorgSplitPanel
		    ArbilTableModel currentTableModel = ((ArbilSplitPanel) childComponent).arbilTable.getArbilTableModel();
		    if (currentTableModel.getArbilDataNodeCount() == rowNodesArray.length) {
			// first check that the number of nodes in the table matches
			boolean tableMatches = true;
			for (ArbilDataNode currentItem : rowNodesArray) {
			    // compare each node for a verbatim match
			    if (!currentTableModel.containsArbilDataNode(currentItem)) {
//                              // ignore this window because the nodes do not match
				tableMatches = false;
				break;
			    }
			}
			if (tableMatches) {
//                            System.out.println("tableMatches");
			    try {
				((JInternalFrame) currentWindow[0]).setIcon(false);
				((JInternalFrame) currentWindow[0]).setSelected(true);
				return currentTableModel;
			    } catch (Exception ex) {
				BugCatcherManager.getBugCatcher().logError(ex);
			    }
			}
		    }
		}
	    }
	}
	// if through the above process a table containing all and only the nodes requested has not been found then create a new table
	return openFloatingTableGetModel(rowNodesArray, frameTitle, null, null);
    }

    /**
     *
     * @param rowNodesArray
     * @param frameTitle
     * @param window Array in which created window is inserted (at index 0). If left null, this is skipped
     * @param fieldView Field view to initialize table model with. If left null, the default field view will be used
     * @return Table model for newly created table window
     */
    private ArbilTableModel openFloatingTableGetModel(ArbilDataNode[] rowNodesArray, String frameTitle, Component[] window, ArbilFieldView fieldView) {
	if (frameTitle == null) {
	    if (rowNodesArray.length == 1) {
		frameTitle = rowNodesArray[0].toString();
	    } else {
		frameTitle = "Selection";
	    }
	}
	ArbilTableModel arbilTableModel = fieldView == null ? new ArbilTableModel(imageBoxRenderer) : new ArbilTableModel(fieldView, imageBoxRenderer);
	ArbilTable arbilTable = new ArbilTable(arbilTableModel, frameTitle);
	ArbilSplitPanel arbilSplitPanel = new ArbilSplitPanel(arbilTable);
	arbilTableModel.addArbilDataNodes(rowNodesArray);
	arbilSplitPanel.setSplitDisplay();
	JInternalFrame tableFrame = this.createWindow(frameTitle, arbilSplitPanel);
	arbilSplitPanel.addFocusListener(tableFrame);

	if (window != null && window.length > 0) {
	    window[0] = tableFrame;
	}

	return arbilTableModel;
    }

    /**
     * Opens a new window containing a scrollpane with a nested collection of tables
     * for the specified node and all of its subnodes.
     *
     * @param arbilDataNode Node to open window for
     * @param frameTitle Title of window to be created
     */
    public void openFloatingSubnodesWindows(ArbilDataNode[] arbilDataNodes) {
	for (ArbilDataNode arbilDataNode : arbilDataNodes) {
	    openFloatingSubnodesWindowOnce(arbilDataNode, arbilDataNode.toString(), null);
	}
    }

    private void openFloatingSubnodesWindowOnce(ArbilDataNode arbilDataNode, String frameTitle, Component[] window) {
	// Check if no subnodes window is opened with the same data node as top level node yet

	for (String currentWindowName : windowList.keySet()) {
	    Component[] currentWindow = (Component[]) windowList.get(currentWindowName);
	    for (Component childComponent : ((JInternalFrame) currentWindow[0]).getContentPane().getComponents()) {
		// loop through all the child components in the window (there will probably only be one)
		if (childComponent instanceof ArbilSubnodesScrollPane) {
		    if (((ArbilSubnodesScrollPane) childComponent).getDataNode().equals(arbilDataNode)) {
			// Make window get focus - a window was requested after all
			focusWindow(currentWindowName);
			// Return so a new window does not get created
			return;
		    }
		}
	    }
	}

	ArbilSubnodesScrollPane scrollPane = new ArbilSubnodesScrollPane(arbilDataNode, imageBoxRenderer);
	JInternalFrame tableFrame = createWindow(frameTitle, scrollPane);
	tableFrame.addInternalFrameListener(scrollPane.getInternalFrameListener());
	if (window != null && window.length > 0) {
	    window[0] = tableFrame;
	}
    }

    public boolean openFileInExternalApplication(URI targetUri) {
	boolean result = false;
	boolean awtDesktopFound = false;
	try {
	    Class.forName("java.awt.Desktop");
	    awtDesktopFound = true;
	} catch (ClassNotFoundException cnfE) {
	    awtDesktopFound = false;
	    System.out.println("java.awt.Desktop class not found");
	}
	if (awtDesktopFound) {
	    try {
		// this method is failing on some windows installations so we will just use browse instead
		// TODO: verify that removing this helps and that it does not cause issues on other OSs
		// removing this breaks launching directories on mac
		if (targetUri.getScheme().toLowerCase().equals("file")) {
		    final File targetFile = new File(targetUri);
		    Desktop.getDesktop().open(targetFile);
		} else {
		    Desktop.getDesktop().browse(targetUri);
		}
		result = true;
	    } catch (MalformedURLException muE) {
		BugCatcherManager.getBugCatcher().logError("awtDesktopFound", muE);
		addMessageDialogToQueue("Failed to find the file: " + muE.getMessage(), "Open In External Application");
	    } catch (IOException ioE) {
		BugCatcherManager.getBugCatcher().logError("awtDesktopFound", ioE);
		if (targetUri.getScheme().equalsIgnoreCase("file")) {
		    if (showConfirmDialogBox("Failed to open the file. Please check that it is accessible and has an application associated with it.\n\nDo you want to open the parent directory?", "Open In External Application")) {
			openFileInExternalApplication(new File(targetUri).getParentFile().toURI());
		    }
		} else {
		    addMessageDialogToQueue("Failed to open the remote location: " + ioE.getMessage(), "Open In External Application");
		}
	    }
	} else {
	    String osNameString = null;
	    try {
		osNameString = System.getProperty("os.name").toLowerCase();
//                String openCommand = "";
		String fileString;
		if (ArbilDataNode.isUriLocal(targetUri)) {
		    fileString = new File(targetUri).getAbsolutePath();
		} else {
		    fileString = targetUri.toString();
		}
		Process launchedProcess = null;

		if (osNameString.indexOf("windows") != -1 || osNameString.indexOf("nt") != -1) {
//                    openCommand = "cmd /c start ";
		    launchedProcess = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", fileString});
		}
		if (osNameString.equals("windows 95") || osNameString.equals("windows 98")) {
//                    openCommand = "command.com /C start ";
		    launchedProcess = Runtime.getRuntime().exec(new String[]{"command.com", "/C", "start", fileString});
		}
		if (osNameString.indexOf("mac") != -1) {
//                    openCommand = "open ";
		    launchedProcess = Runtime.getRuntime().exec(new String[]{"open", fileString});
		}
		if (osNameString.indexOf("linux") != -1) {
//                    openCommand = "gnome-open ";
		    launchedProcess = Runtime.getRuntime().exec(new String[]{"gnome-open", fileString});
		}
//                String execString = openCommand + targetUri.getPath();
//                System.out.println(execString);
//                Process launchedProcess = Runtime.getRuntime().exec(new String[]{openCommand, targetUri.getPath()});
		if (launchedProcess != null) {
		    BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream()));
		    String line;
		    while ((line = errorStreamReader.readLine()) != null) {
			addMessageDialogToQueue(line, "Open In External Application");
			System.out.println("Launched process error stream: \"" + line + "\"");
		    }
		    result = true;
		}
	    } catch (Exception e) {
		BugCatcherManager.getBugCatcher().logError(osNameString, e);
	    }
	}
	return result;
    }

    public void openImdiXmlWindow(Object userObject, boolean formatXml, boolean launchInBrowser) {
	if (userObject instanceof ArbilDataNode) {
	    if (((ArbilDataNode) (userObject)).getNeedsSaveToDisk(false)) {
		if (JOptionPane.OK_OPTION == showDialogBox("The node must be saved first.\nSave now?", "View IMDI XML", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
		    ((ArbilDataNode) (userObject)).saveChangesToCache(true);
		} else {
		    return;
		}
	    }
	    URI nodeUri = ((ArbilDataNode) (userObject)).getURI();
	    System.out.println("openImdiXmlWindow: " + nodeUri);
	    String nodeName = ((ArbilDataNode) (userObject)).toString();
	    if (formatXml) {
		try {
		    File tempHtmlFile = new ArbilToHtmlConverter().convertToHtml((ArbilDataNode) userObject);
		    if (!launchInBrowser) {
			openUrlWindowOnce(nodeName + " formatted", tempHtmlFile.toURL());
		    } else {
			openFileInExternalApplication(tempHtmlFile.toURI());
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		    //System.out.println(ex.getMessage());
		    //LinorgWindowManager.getArbilHelpInstance().openUrlWindow(nodeName, nodeUrl);
		}
	    } else {
		try {
		    openUrlWindowOnce(nodeName + "-xml", nodeUri.toURL());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		    //System.out.println(ex.getMessage());
		    //LinorgWindowManager.getArbilHelpInstance().openUrlWindow(nodeName, nodeUrl);
		}
	    }
	}
    }

    //JOptionPane.showConfirmDialog(linorgFrame,
    //"Moving files from:\n" + fromDirectory + "\nto:\n" + preferedCacheDirectory + "\n"
    //+ "Arbil will need to close all tables once the files are moved.\nDo you wish to continue?", "Arbil", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
    /**
     *
     * @param message Message of the dialog
     * @param title Title of the dialog
     * @param optionType Option type, one of the constants of JOptionPane
     * @param messageType Message type, one of the constants of JOptionPane
     * @return One of the JOptionPane constants indicating the chosen option
     *
     * @see javax.swing.JOptionPane
     */
    public int showDialogBox(String message, String title, int optionType, int messageType) {
	return JOptionPane.showConfirmDialog(getMainFrame(), message, title, optionType, messageType);
    }

    /**
     *
     * @param message Message of the dialog
     * @param title Title of the dialog
     * @param optionType Option type, one of the constants of JOptionPane
     * @param messageType Message type, one of the constants of JOptionPane
     * @param options Dialog options to show
     * @param initialValue Value of options to select initially (default option)
     * @return One of the JOptionPane constants indicating the chosen option
     *
     * @see javax.swing.JOptionPane
     */
    public int showDialogBox(String message, String title, int optionType, int messageType, Object[] options, Object initialValue) {
	return JOptionPane.showOptionDialog(getMainFrame(), message, title, optionType, messageType, null, options, initialValue);
    }

    public ProgressMonitor newProgressMonitor(Object message, String note, int min, int max) {
	return new ProgressMonitor(desktopPane, message, note, min, max);
    }

    public JFrame getMainFrame() {
	return linorgFrame;
    }

    public boolean askUserToSaveChanges(String entityName) {
	return showConfirmDialogBox("This action will save all pending changes on " + entityName + " to disk. Continue?", "Save to disk?");
    }

    private Point fixLocation(Point location) {
	if (location.getY() < 0) {
	    location.move((int) location.getX(), Math.max(0, (int) location.getY()));
	}
	return location;
    }

    public void setStatusBarVisible(boolean visible) {
	statusBar.setVisible(visible);
    }

    public boolean getStatusBarVisible() {
	return statusBar.isVisible();
    }

    public synchronized Collection<ArbilTaskListener> getTaskListeners() {
	return taskListeners;
    }

    public synchronized void addTaskListener(ArbilTaskListener taskListener) {
	taskListeners.add(taskListener);
    }

    /**
     * TODO: this is public for the dialog boxes to use, but will change when the strings are loaded from the resources
     *
     * @return the desktopPane
     */
    public JDesktopPane getDesktopPane() {
	return desktopPane;
    }
}
