package nl.mpi.arbil.ui.wizard.setup;

import java.awt.Frame;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.wizard.ArbilWizard;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.TreeHelper;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSetupWizard extends ArbilWizard {

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    protected static final String WIZARDSTATE_PROPERTY = "wizardState";
    public final static Object INTRODUCTION = IntroductionContent.class;
    public final static Object METADATA_FORMAT_SELECT = MetadataFormatSelectContent.class;
    public final static Object CMDI_PROFILE_SELECT = CmdiProfileSelectContent.class;
    public final static Object REMOTE_LOCATIONS = RemoteLocationsContent.class;
    public final static Object CONFIRMATION = ConfirmationContent.class;
    private ArbilSetupWizardModel model;

    public ArbilSetupWizard() {
	this(null);
    }

    public ArbilSetupWizard(Frame owner) {
	super(owner);

	getModel();
	addContent(INTRODUCTION, new ArbilIconContentDecorator(new IntroductionContent()));
	addContent(METADATA_FORMAT_SELECT, new ArbilIconContentDecorator(new MetadataFormatSelectContent(model)));
	addContent(CMDI_PROFILE_SELECT, new ArbilIconContentDecorator(new CmdiProfileSelectContent(model, getWizardDialog())));
	addContent(REMOTE_LOCATIONS, new ArbilIconContentDecorator(new RemoteLocationsContent(model)));
	addContent(CONFIRMATION, new ArbilIconContentDecorator(new ConfirmationContent()));
	setCurrent(INTRODUCTION);
    }

    @Override
    protected boolean onFinish() {
	saveWizardState();
	applyConfiguration();
	return true;
    }

    @Override
    protected boolean onCancel() {
	return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
		getWizardDialog(),
		"Do you really want to cancel the wizard?",
		"Cancel wizard",
		JOptionPane.YES_NO_OPTION,
		JOptionPane.QUESTION_MESSAGE);
    }

    private void getModel() {
	try {
	    Object wizardState = sessionStorage.loadObject(WIZARDSTATE_PROPERTY);
	    if (wizardState instanceof ArbilSetupWizardModel) {
		model = (ArbilSetupWizardModel) wizardState;
		return;
	    }
	} catch (Exception ex) {
	    // Failed to load wizard state. Create new
	}
	model = new ArbilSetupWizardModel();
    }

    private void saveWizardState() {
	try {
	    sessionStorage.saveObject(model, WIZARDSTATE_PROPERTY);
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError("Could not save wizard state", ex);
	}
    }

    private void applyConfiguration() {
	applyTemplatesConfiguration();
	applyRemoteLocationsConfiguration();
	applyFileFilterConfiguration();
    }

    private void applyTemplatesConfiguration() {
	final ArbilTemplateManager templateManager = ArbilTemplateManager.getSingleInstance();
	// Add (if not already added) or remove (if present) default IMDI templates
	if (model.isImdiSelected()) {
	    templateManager.addDefaultImdiTemplates();
	} else {
	    templateManager.removeDefaultImdiTemplates();
	}

	if (!model.isCmdiSelected()) {
	    // Remove all selected CMDI profiles
	    for (String template : templateManager.getSelectedTemplateArrayList().toArray(new String[]{})) {
		if (template.startsWith(ArbilTemplateManager.CLARIN_PREFIX)) {
		    templateManager.removeSelectedTemplates(template);
		}
	    }
	}
    }

    private void applyFileFilterConfiguration() {
	if (model.isImdiSelected()) {
	    sessionStorage.saveString(SessionStorage.PARAM_LAST_FILE_FILTER, "IMDI");
	} else {
	    if (model.isCmdiSelected()) {
		sessionStorage.saveString(SessionStorage.PARAM_LAST_FILE_FILTER, "CMDI");
	    }
	}
    }

    public static void main(String args[]) {
	new ArbilDesktopInjector().injectHandlers();
	ArbilWizard wizard = new ArbilSetupWizard();
	wizard.showModalDialog();
    }

    private void applyRemoteLocationsConfiguration() {
	final String newLine = System.getProperty("line.separator");

	treeHelper.clearRemoteLocations();
	String[] locations = model.getRemoteLocations().split(newLine);
	List<URI> locationURIs = new ArrayList<URI>(locations.length);
	for (String location : locations) {
	    if (location != null && location.trim().length() > 0) {
		try {
		    URI uri = new URI(location);
		    locationURIs.add(uri);
		} catch (URISyntaxException ex) {
		    BugCatcherManager.getBugCatcher().logError("Invalid URI specified in wizard", ex);
		}
	    }
	}
	treeHelper.addLocations(locationURIs);
	treeHelper.applyRootLocations();
    }
}
