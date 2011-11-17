package nl.mpi.arbil.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.templates.ArbilTemplateManager;

/*
 * TemplateDialogue.java
 * Created on May 20, 2010, 9:03:29 AM
 * @author Peter.Withers@mpi.nl
 */
public class TemplateDialogue extends javax.swing.JPanel {

    JDialog parentFrame;

    /** Creates new form TemplateDialogue */
    public TemplateDialogue(JDialog parentFrameLocal) {
	parentFrame = parentFrameLocal;
	initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {

	internalTemplatesPanel = new javax.swing.JPanel();
	internalTemplatesButtonPanel = new javax.swing.JPanel();
	newTemplateButton = new javax.swing.JButton();
	templatesScrollPane = new javax.swing.JScrollPane();
	templatesPanel = new javax.swing.JPanel();
	cmdiProfilesPanel = new CmdiProfilesPanel(parentFrame);

	internalTemplatesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("IMDI Templates"));
	internalTemplatesPanel.setLayout(new java.awt.BorderLayout());

	internalTemplatesButtonPanel.setLayout(new javax.swing.BoxLayout(internalTemplatesButtonPanel, javax.swing.BoxLayout.LINE_AXIS));

	newTemplateButton.setText("New Template");
	newTemplateButton.setToolTipText("Create a new editable template");
	newTemplateButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		newTemplateButtonActionPerformed(evt);
	    }
	});
	internalTemplatesButtonPanel.add(newTemplateButton);

	internalTemplatesPanel.add(internalTemplatesButtonPanel, java.awt.BorderLayout.PAGE_END);

	templatesScrollPane.setViewportView(templatesPanel);

	internalTemplatesPanel.add(templatesScrollPane, java.awt.BorderLayout.CENTER);


	cmdiProfilesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Clarin Profiles"));

	// todo: this should probably have a cancel button also
	JPanel outerPanel = new JPanel();
	outerPanel.setLayout(new java.awt.GridLayout(1, 0));
	outerPanel.add(internalTemplatesPanel);
	outerPanel.add(cmdiProfilesPanel);
	this.setLayout(new java.awt.BorderLayout());
	this.add(outerPanel, java.awt.BorderLayout.CENTER);
	JButton closeButton = new JButton("Close");
	closeButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		parentFrame.setVisible(false);
	    }
	});
	JPanel closeButtonPanel = new JPanel();
	closeButtonPanel.setLayout(new java.awt.BorderLayout());
	closeButtonPanel.add(closeButton, java.awt.BorderLayout.LINE_END);
	this.add(outerPanel, java.awt.BorderLayout.CENTER);
	this.add(closeButtonPanel, java.awt.BorderLayout.PAGE_END);
    }

    private void newTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {
	try {
	    String newDirectoryName = JOptionPane.showInputDialog(ArbilWindowManager.getSingleInstance().linorgFrame, "Enter the name for the new template", ArbilWindowManager.getSingleInstance().linorgFrame.getTitle(), JOptionPane.PLAIN_MESSAGE, null, null, null).toString();
	    // if the user cancels the directory string will be a empty string.
	    if (ArbilTemplateManager.getSingleInstance().getTemplateFile(newDirectoryName).exists()) {
		ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("The template \"" + newDirectoryName + "\" already exists.", "Templates");
	    }
	    File freshTemplateFile = ArbilTemplateManager.getSingleInstance().createTemplate(newDirectoryName);
	    if (freshTemplateFile != null) {
		GuiHelper.getSingleInstance().openFileInExternalApplication(freshTemplateFile.toURI());
		GuiHelper.getSingleInstance().openFileInExternalApplication(freshTemplateFile.getParentFile().toURI());
	    } else {
		ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("The template \"" + newDirectoryName + "\" could not be created.", "Templates");
	    }
//                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("This action is not yet available.", "Templates");
	    //GuiHelper.linorgWindowManager.openUrlWindow(evt.getActionCommand() + templateList.get(evt.getActionCommand()).toString(), new File(templateList.get(evt.getActionCommand()).toString()).toURL());
//                    System.out.println("setting template: " + evt.getActionCommand());
//                    ArbilTemplateManager.getSingleInstance().setCurrentTemplate(evt.getActionCommand());
	} catch (Exception e) {
	    GuiHelper.linorgBugCatcher.logError(e);
	}
	populateLists();
    }

    protected static void addSorted(JPanel targetPanel, ArrayList<JCheckBox> checkBoxArray) {
	targetPanel.removeAll();
	targetPanel.setLayout(new javax.swing.BoxLayout(targetPanel, javax.swing.BoxLayout.PAGE_AXIS));
	Collections.sort(checkBoxArray, new Comparator() {

	    public int compare(Object firstItem, Object secondItem) {
		return (((JCheckBox) firstItem).getText().compareToIgnoreCase(((JCheckBox) secondItem).getText()));
	    }
	});
	for (JCheckBox checkBox : checkBoxArray) {
	    targetPanel.add(checkBox);
	}
    }

    protected void populateLists() {
	ArrayList<String> selectedTamplates = ArbilTemplateManager.getSingleInstance().getSelectedTemplateArrayList();
	ArrayList<JCheckBox> checkBoxArray = new ArrayList<JCheckBox>();
	// add built in types
	for (String currentTemplateName[] : ArbilTemplateManager.getSingleInstance().getTemplate(null).getRootTemplatesArray()) {
	    JCheckBox templateCheckBox;
	    templateCheckBox = new JCheckBox();
	    templateCheckBox.setText(currentTemplateName[1] + " (internal)");
	    templateCheckBox.setName(currentTemplateName[1]);
	    templateCheckBox.setActionCommand("builtin:" + currentTemplateName[0]);
	    templateCheckBox.setSelected(selectedTamplates.contains(templateCheckBox.getActionCommand()));
	    templateCheckBox.setToolTipText(currentTemplateName[1]);
	    templateCheckBox.addActionListener(templateSelectionListener);
	    checkBoxArray.add(templateCheckBox);
	}
	// add custom templates
	for (String currentTemplateName : ArbilTemplateManager.getSingleInstance().getAvailableTemplates()) {
	    JCheckBox templateCheckBox;
	    templateCheckBox = new JCheckBox();
	    templateCheckBox.setText(currentTemplateName);
	    templateCheckBox.setName(currentTemplateName);
	    templateCheckBox.setActionCommand("template:" + currentTemplateName);
	    templateCheckBox.setSelected(selectedTamplates.contains(templateCheckBox.getActionCommand()));
	    templateCheckBox.setToolTipText(currentTemplateName);
	    templateCheckBox.addActionListener(templateSelectionListener);
	    checkBoxArray.add(templateCheckBox);
	}
	addSorted(templatesPanel, checkBoxArray);

	if (cmdiProfilesPanel != null) {
	    cmdiProfilesPanel.populateProfilesList();
	}

	parentFrame.pack();
    }

    public void loadProfiles(final boolean forceUpdate) {
	if (cmdiProfilesPanel != null) {
	    cmdiProfilesPanel.loadProfiles(forceUpdate);
	}
    }

    public static void showTemplatesDialogue() {
	showDialogue("Available Templates & Profiles");
    }

    protected static void showDialogue(String titleStirng) {
	JDialog dialog = new JDialog(ArbilWindowManager.getSingleInstance().linorgFrame, titleStirng, true);
	TemplateDialogue templateDialogue = new TemplateDialogue(dialog);
	dialog.setContentPane(templateDialogue);
	templateDialogue.populateLists();
	templateDialogue.loadProfiles(false);
	dialog.pack();
	dialog.setVisible(true);
    }
    public final static ActionListener templateSelectionListener = new ActionListener() {

	public void actionPerformed(ActionEvent e) {
	    if (((JCheckBox) e.getSource()).isSelected()) {
		ArbilTemplateManager.getSingleInstance().addSelectedTemplates(e.getActionCommand());
	    } else {
		ArbilTemplateManager.getSingleInstance().removeSelectedTemplates(e.getActionCommand());
	    }
	}
    };

    public static void main(String[] args) {
	ArbilDesktopInjector.injectHandlers();
	TemplateDialogue.showTemplatesDialogue();
	System.exit(0);
    }
    // Variables declaration
    private javax.swing.JButton newTemplateButton;
    protected javax.swing.JPanel internalTemplatesPanel;
    protected javax.swing.JPanel internalTemplatesButtonPanel;
    private javax.swing.JScrollPane templatesScrollPane;
    protected javax.swing.JPanel templatesPanel;
    protected CmdiProfilesPanel cmdiProfilesPanel;
    // End of variables declaration
}
