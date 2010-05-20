package nl.mpi.arbil.clarin;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JProgressBar;
import nl.mpi.arbil.LinorgSessionStorage;
import org.apache.commons.digester.Digester;

/**
 * CmdiProfileReader.java
 * Created on February 1, 2010, 14:22:03
 * @author Peter.Withers@mpi.nl
 */
public class CmdiProfileReader {

    public ArrayList<CmdiProfile> cmdiProfileArray = null;
    // todo: move this url into the config file
    String profilesUrlString = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles";
    //String profilesUrlString = "http://lux16.mpi.nl/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1264758016524/xsd";
    //"http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles"

    public static void main(String args[]) {
        new CmdiProfileReader();
    }

    public static boolean pathIsProfile(String pathString) {
        // TODO: make this smarter
        return (pathString.startsWith("http") && pathString.contains("clarin"));
    }

    public String getProfileName(String XsdHref) {
        for (CmdiProfile currentProfile : cmdiProfileArray) {
            if (currentProfile.getXsdHref().equals(XsdHref)) {
                return currentProfile.name;
            }
        }
        return "unknown Clarin profile";
    }

    public class CmdiProfile {

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

    public CmdiProfileReader() {
        loadProfiles();
        // get all the xsd files from the profile listing and store them on disk for offline use
        for (CmdiProfileReader.CmdiProfile currentCmdiProfile : cmdiProfileArray) {
            System.out.println("checking profile exists on disk: " + currentCmdiProfile.getXsdHref());
            LinorgSessionStorage.getSingleInstance().updateCache(currentCmdiProfile.getXsdHref(), 90);
        }
    }

    public void refreshProfiles(JProgressBar progressBar) {
        progressBar.setIndeterminate(true);
        progressBar.setString("");
        LinorgSessionStorage.getSingleInstance().updateCache(profilesUrlString, 0);
        loadProfiles();
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(cmdiProfileArray.size() + 1);
        progressBar.setValue(1);
        // get all the xsd files from the profile listing and store them on disk for offline use
        for (CmdiProfileReader.CmdiProfile currentCmdiProfile : cmdiProfileArray) {
            progressBar.setString(currentCmdiProfile.name);
            System.out.println("resaving profile to disk: " + currentCmdiProfile.getXsdHref());
            LinorgSessionStorage.getSingleInstance().updateCache(currentCmdiProfile.getXsdHref(), 0);
            progressBar.setValue(progressBar.getValue() + 1);
        }
        progressBar.setString("");
        progressBar.setValue(0);
    }

    public void loadProfiles() {
        File profileXmlFile = LinorgSessionStorage.getSingleInstance().updateCache(profilesUrlString, 10);
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
            e.printStackTrace();
        }
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
}
