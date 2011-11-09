package nl.mpi.arbil.clarin.profiles;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JProgressBar;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import org.apache.commons.digester.Digester;

/**
 * CmdiProfileReader.java
 * Created on February 1, 2010, 14:22:03
 * @author Peter.Withers@mpi.nl
 */
public class CmdiProfileReader {

    private static SessionStorage sessionStorage;
    private static BugCatcher bugCatcher;
    public static final String PARAM_PROFILESELECTION = "profileSelection";
    public static final String PARAM_PROFILES_URL = "profilesUrlAll";
    public static final String PARAM_SELECTED_PROFILES_URL = "profilesUrlSelected";
    public final static String DEFAULT_ALL_PROFILES_URL_STRING = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles";
    public final static String DEFAULT_SELECTED_PROFILES_URL_STRING = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles?mdEditor=true";
    private ProfileSelection selection;

    public static enum ProfileSelection {

	SELECTED,
	ALL
    };

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
    }
    public ArrayList<CmdiProfile> cmdiProfileArray = null;
    static CmdiProfileReader singleInstance = null;

    static synchronized public CmdiProfileReader getSingleInstance() {
	// make sure the profiles xml need only be read once per session
	if (singleInstance == null) {
	    singleInstance = new CmdiProfileReader(getStoredProfileSelection());
	}
	return singleInstance;
    }

    public static boolean pathIsProfile(String pathString) {
	// TODO: make this smarter (this only needs to determin if the path is to a template file or an xsd)
	return (pathString.startsWith("http") || pathString.contains("clarin") || pathString.endsWith(".xsd") || pathString.endsWith("/xsd"));
    }

    public CmdiProfile getProfile(String XsdHref) {
	for (CmdiProfile currentProfile : cmdiProfileArray) {
	    if (currentProfile.getXsdHref().equals(XsdHref)) {
		return currentProfile;
	    }
	}
	return null;
    }

    public static class CmdiProfile {

	public String id;
	public String description;
	public String name;
	public String registrationDate;
	public String creatorName;
	public String href;

	public String getXsdHref() {
	    return href + "/xsd";
	}
    }

    private CmdiProfileReader(ProfileSelection selection) {
	setSelection(selection);
	loadProfiles(getProfilesUrlStringForSelection(getSelection()));
    }

    public void refreshProfiles(JProgressBar progressBar, boolean forceUpdate) {
	progressBar.setIndeterminate(true);
	progressBar.setString("");
	int updateDays;
	if (forceUpdate) {
	    updateDays = 0;
	} else {
	    updateDays = 100;
	}
	sessionStorage.updateCache(getProfilesUrlStringForSelection(getSelection()), updateDays, false);
	loadProfiles(getProfilesUrlStringForSelection(getSelection()));
	progressBar.setIndeterminate(false);
	progressBar.setMinimum(0);
	progressBar.setMaximum(cmdiProfileArray.size() + 1);
	progressBar.setValue(1);
	// get all the xsd files from the profile listing and store them on disk for offline use
	for (CmdiProfileReader.CmdiProfile currentCmdiProfile : cmdiProfileArray) {
	    progressBar.setString(currentCmdiProfile.name);
	    System.out.println("resaving profile to disk: " + currentCmdiProfile.getXsdHref());
	    sessionStorage.updateCache(currentCmdiProfile.getXsdHref(), updateDays, false);
	    progressBar.setValue(progressBar.getValue() + 1);
	}
	progressBar.setString("");
	progressBar.setValue(0);
    }

    private void loadProfiles(final String profilesUrl) {
	File profileXmlFile = sessionStorage.updateCache(profilesUrl, 10, false);
	try {
	    Digester digester = new Digester();
	    // This method pushes this (SampleDigester) class to the Digesters
	    // object stack making its methods available to processing rules.
	    digester.push(this);
	    // This set of rules calls the addProfile method and passes
	    // in five parameters to the method.
	    digester.addCallMethod("profileDescriptions/profileDescription", "addProfile", 6);
	    digester.addCallParam("profileDescriptions/profileDescription/id", 0);
	    digester.addCallParam("profileDescriptions/profileDescription/description", 1);
	    digester.addCallParam("profileDescriptions/profileDescription/name", 2);
	    digester.addCallParam("profileDescriptions/profileDescription/registrationDate", 3);
	    digester.addCallParam("profileDescriptions/profileDescription/creatorName", 4);
	    digester.addCallParam("profileDescriptions/profileDescription/ns2:href", 5);

	    cmdiProfileArray = new ArrayList<CmdiProfile>();
	    digester.parse(profileXmlFile);
	} catch (Exception e) {
	    bugCatcher.logError(e);
	}
	// get all the xsd files from the profile listing and store them on disk for offline use
//        for (CmdiProfileReader.CmdiProfile currentCmdiProfile : cmdiProfileArray) {
//            System.out.println("checking profile exists on disk: " + currentCmdiProfile.getXsdHref());
//            LinorgSessionStorage.getSingleInstance().getFromCache(currentCmdiProfile.getXsdHref(), 90);
//        }
    }

    public void addProfile(String id,
	    String description,
	    String name,
	    String registrationDate,
	    String creatorName,
	    String href) {
//        System.out.println(id + " : " + description + " : " + name + " : " + registrationDate + " : " + creatorName + " : " + href);

	CmdiProfile cmdiProfile = new CmdiProfile();
	cmdiProfile.id = id;
	cmdiProfile.description = description;
	cmdiProfile.name = name;
	cmdiProfile.registrationDate = registrationDate;
	cmdiProfile.creatorName = creatorName;
	cmdiProfile.href = href;

	cmdiProfileArray.add(cmdiProfile);
    }

    /**
     * Get the value of selection
     *
     * @return the value of selection
     */
    public final ProfileSelection getSelection() {
	return selection;
    }

    /**
     * Set the value of selection
     *
     * @param selection new value of selection
     */
    public final void setSelection(ProfileSelection selection) {
	this.selection = selection;
	storeProfileSelection(selection);
    }

    protected final String getProfilesUrlStringForSelection(ProfileSelection selection) {
	return selection == ProfileSelection.SELECTED ? getSelectedProfileUrlString() : getAllProfilesUrlString();
    }

    public static void main(String args[]) {
	new CmdiProfileReader(ProfileSelection.SELECTED);
    }

    private synchronized static ProfileSelection getStoredProfileSelection() {
	String selectionString = sessionStorage.loadString(PARAM_PROFILESELECTION);
	if (selectionString == null) {
	    selectionString = ProfileSelection.SELECTED.toString();
	}
	return ProfileSelection.valueOf(selectionString);
    }

    private void storeProfileSelection(ProfileSelection selection) {
	sessionStorage.saveString(PARAM_PROFILESELECTION, selection.toString());
    }

    private String getAllProfilesUrlString() {
	String savedProfilesUrlString = sessionStorage.loadString(PARAM_PROFILES_URL);
	if (savedProfilesUrlString == null) {
	    sessionStorage.saveString(PARAM_PROFILES_URL, DEFAULT_ALL_PROFILES_URL_STRING);
	    return DEFAULT_ALL_PROFILES_URL_STRING;
	} else {
	    return savedProfilesUrlString;
	}
    }

    private String getSelectedProfileUrlString() {
	String savedProfilesUrlString = sessionStorage.loadString(PARAM_SELECTED_PROFILES_URL);
	if (savedProfilesUrlString == null) {
	    sessionStorage.saveString(PARAM_SELECTED_PROFILES_URL, DEFAULT_SELECTED_PROFILES_URL_STRING);
	    return DEFAULT_SELECTED_PROFILES_URL_STRING;
	} else {
	    return savedProfilesUrlString;
	}
    }
}
