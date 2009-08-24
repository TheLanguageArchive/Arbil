package mpi.linorg;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

/**
 * ArbilTemplate.java
 * Created on Aug 14, 2009, 11:30:20 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTemplate{

    public String[][] triggersArray = {
        //        TODO: read this array from a file in the teplates directory
        {".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language(x).Name", ".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language(x).Id", "description"},
        {".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language(x).Name", ".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language(x).Id", "description"},
        {".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Language.Name", ".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Language.Id", "description"},
        {".METATRANSCRIPT.Catalogue.SubjectLanguages.Language(x).Name", ".METATRANSCRIPT.Catalogue.SubjectLanguages.Language(x).Id", "description"},
        {".METATRANSCRIPT.Catalogue.DocumentLanguages.Language.Name", ".METATRANSCRIPT.Catalogue.DocumentLanguages.Language.Id", "description"}//,
//            this LexiconResource field has no related id field {".METATRANSCRIPT.Session.Resources.LexiconResource(x).MetaLanguages.Language", ".METATRANSCRIPT.Session.Resources.LexiconResource(x).MetaLanguages.Id", "description"},
    };
    public String[][] genreSubgenreArray = {
        //        TODO: read this array from a file in the teplates directory
        {".METATRANSCRIPT.Session.MDGroup.Content.SubGenre", ".METATRANSCRIPT.Session.MDGroup.Content.Genre", "description"}
    };
    public String[] requiredFields = {".METATRANSCRIPT.Session.MDGroup.Content.Genre", ".METATRANSCRIPT.Session.MDGroup.Content.SubGenre", ".METATRANSCRIPT.Session.Resources.Anonyms.Access.Contact.Email", ".METATRANSCRIPT.Session.MDGroup.Project.Title", ".METATRANSCRIPT.Corpus.Title", ".METATRANSCRIPT.Session.Title", ".METATRANSCRIPT.Session.MDGroup.Project.Id"};

    public String pathIsChildNode(String nodePath) {
        // TODO: change this to use a master list of types and populate it from the schema
        if (nodePath.contains(".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language")) {
            return "Languages";
        }
        if (nodePath.contains(".Languages.Language")) {
            return "Languages";
        }
        if (nodePath.contains(".METATRANSCRIPT.Session.MDGroup.Actors.Actor")) {
            return "Actors";
        }
        if (nodePath.contains(".METATRANSCRIPT.Session.Resources.MediaFile")) {
            return "MediaFiles";
        }
        if (nodePath.contains(".METATRANSCRIPT.Session.Resources.WrittenResource")) {
            return "WrittenResources";
        }
        if (nodePath.contains(".METATRANSCRIPT.Session.Resources.Source")) {
            return "Sources";
        }
        if (nodePath.contains(".METATRANSCRIPT.Session.Resources.LexiconResource")) {
            return "LexiconResource";
        }
        if (nodePath.contains(".METATRANSCRIPT.Catalogue.Location")) {
            return "Location";
        }
        if (nodePath.contains(".METATRANSCRIPT.Catalogue.SubjectLanguages.Language")) {
            return "SubjectLanguages";
        }
        return null;
    }

    private Vector getSubnodesFromTemplatesDir(String nodepath) {
        Vector<String[]> returnVector = new Vector<String[]>();
        System.out.println("getSubnodesOf: " + nodepath);
        String targetNodePath = nodepath.substring(0, nodepath.lastIndexOf(")") + 1);
        nodepath = nodepath.replaceAll("\\(\\d\\)", "\\(x\\)");
        System.out.println("nodepath: " + nodepath);
        System.out.println("targetNodePath: " + targetNodePath);
        String[][] templatesArray = {
            {"METATRANSCRIPT.Catalogue.Access.Description.xml", "Access Description"},
            {"METATRANSCRIPT.Catalogue.Author.xml", "Author"},
            {"METATRANSCRIPT.Catalogue.ContentType.xml", "ContentType"},
            {"METATRANSCRIPT.Catalogue.Description.xml", "Description"},
            {"METATRANSCRIPT.Catalogue.DocumentLanguages.Description.xml", "Document Languages Description"},
            {"METATRANSCRIPT.Catalogue.DocumentLanguages.Language.xml", "Document Languages Language"},
            {"METATRANSCRIPT.Catalogue.Keys.Key.xml", "Key"},
            {"METATRANSCRIPT.Catalogue.Location.xml", "Location"},
            {"METATRANSCRIPT.Catalogue.Project.Author.xml", "Project Author"},
            {"METATRANSCRIPT.Catalogue.Project.Description.xml", "Project Description"},
            {"METATRANSCRIPT.Catalogue.Publisher.xml", "Publisher"},
            {"METATRANSCRIPT.Catalogue.SubjectLanguages.Description.xml", "Subject Languages Description"},
            {"METATRANSCRIPT.Catalogue.SubjectLanguages.Language(x).Description.xml", "Subject Languages Language Description"},
            {"METATRANSCRIPT.Catalogue.SubjectLanguages.Language.xml", "Subject Languages Language"},
            {"METATRANSCRIPT.Catalogue.xml", "Catalogue"},
            {"METATRANSCRIPT.Corpus.Description.xml", "Description"},
            {"METATRANSCRIPT.Corpus.xml", "Corpus"},
            {"METATRANSCRIPT.Session.Description.xml", "Description"},
            {"METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Description.xml", "Actor Description"},
            {"METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Keys.Key.xml", "Actor Key"},
            {"METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language.xml", "Actor Language"},
            {"METATRANSCRIPT.Session.MDGroup.Actors.Actor.xml", "Actor"},
            {"METATRANSCRIPT.Session.MDGroup.Content.Keys.Key.xml", "Content Key"},
            {"METATRANSCRIPT.Session.MDGroup.Content.Languages.Language.xml", "Content Language"},
            {"METATRANSCRIPT.Session.MDGroup.Keys.Key.xml", "Key"},
            {"METATRANSCRIPT.Session.Resources.MediaFile.xml", "MediaFile"},
            {"METATRANSCRIPT.Session.Resources.Source(x).Keys.Key.xml", "Source Key"},
            {"METATRANSCRIPT.Session.Resources.Source.xml", "Source"},
            {"METATRANSCRIPT.Session.Resources.WrittenResource(x).Keys.Key.xml", "WrittenResource Key"},
            {"METATRANSCRIPT.Session.Resources.WrittenResource.xml", "WrittenResource"},
            {"METATRANSCRIPT.Session.xml", "Session"}
        };
        try {
//            System.out.println("get templatesDirectory");
            File templatesDirectory = new File(this.getClass().getResource("/mpi/linorg/resources/templates/").getFile());
//            System.out.println("check templatesDirectory");
            if (templatesDirectory.exists()) { // compare the templates directory to the array and throw if there is a discrepancy
//                System.out.println("using templatesDirectory");
                String[] testingListing = templatesDirectory.list();
                Arrays.sort(testingListing);
                for (String itemString : testingListing) {
                    System.out.println("\"" + itemString + "\",");
                }
                int linesRead = 0;
                for (String[] currentTemplate : templatesArray) {
//                    System.out.println("currentTemplate: " + currentTemplate + " : " + testingListing[linesRead]);
                    if (testingListing != null) {
                        if (!testingListing[linesRead].equals(currentTemplate[0])) {
                            System.out.println("error: " + currentTemplate[0] + " : " + testingListing[linesRead]);
                            throw new Exception("error in the templates array");
                        }
                    }
                    linesRead++;
                }
                Arrays.sort(templatesArray, new Comparator() {

                    public int compare(Object obj1, Object obj2) {
                        return ((String[]) obj1)[1].compareToIgnoreCase(((String[]) obj2)[1]);
                    }
                });
                if (testingListing != null) {
                    if (testingListing.length != linesRead) {
                        System.out.println(testingListing[linesRead]);
                        throw new Exception("error missing line in the templates array");
                    }
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        for (String[] currentTemplate : templatesArray) {
            currentTemplate[0] = "." + currentTemplate[0];
            if (!currentTemplate[0].endsWith("Session.xml") && !currentTemplate[0].endsWith("Catalogue.xml")) { // sessions cannot be added to a session
                if (currentTemplate[0].startsWith(nodepath)) {
                    if (targetNodePath.replaceAll("[^(]*", "").length() >= currentTemplate[0].replaceAll("[^(]*", "").length()) {
                        currentTemplate[0] = currentTemplate[0].replaceFirst("\\.xml$", "");
//                            String currentTemplateXPath = currentTemplate[0].replaceFirst("\\.xml$", "");
//                            String currentTemplateName = currentTemplateXPath.substring(nodepath.length());
//                        System.out.println("currentTemplateXPath: " + currentTemplateXPath);
//                        System.out.println("targetNodePath: " + targetNodePath);
//                            String destinationXPath;
//                            if (currentTemplateXPath.contains(")")) {
//                                destinationXPath = targetNodePath + currentTemplateXPath.substring(currentTemplateXPath.lastIndexOf(")") + 1);
//                            } else {
//                                destinationXPath = currentTemplateXPath;
//                            }
//                        System.out.println("destinationXPath: " + destinationXPath);

                        returnVector.add(new String[]{currentTemplate[1], currentTemplate[0]});// TODO: update the menu title to include location
                    }
                }
            }
        }
        Collections.sort(returnVector, new Comparator() {

            public int compare(Object o1, Object o2) {
                String value1 = ((String[]) o1)[0];
                String value2 = ((String[]) o2)[0];
                return value1.compareTo(value2);
            }
        });
        return returnVector;
    }

    /**
     * This function is only a place holder and will be replaced.
     * @param targetNodeUserObject The imdi node that will receive the new child.
     * @return An enumeration of Strings for the available child types, one of which will be passed to "listFieldsFor()".
     */
    public Enumeration listTypesFor(Object targetNodeUserObject) {
        // temp method for testing until replaced
        // TODO: implement this using data from the xsd on the server (server version needs to be updated)
        Vector childTypes = new Vector();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            if (((ImdiTreeObject) targetNodeUserObject).isCatalogue() || ((ImdiTreeObject) targetNodeUserObject).isSession() || ((ImdiTreeObject) targetNodeUserObject).isImdiChild()) {
                String xpath = ImdiSchema.getNodePath((ImdiTreeObject) targetNodeUserObject);
                childTypes = getSubnodesFromTemplatesDir(xpath);
            } else if (!((ImdiTreeObject) targetNodeUserObject).isImdiChild()) {
                childTypes.add(new String[]{"Corpus Branch", ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Corpus"});
                childTypes.add(new String[]{"Corpus Description", ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Corpus" + ImdiSchema.imdiPathSeparator + "Description"});
//                TODO: make sure the catalogue can only be added once
                if (!((ImdiTreeObject) targetNodeUserObject).hasCatalogue()) {
                    childTypes.add(new String[]{"Catalogue", ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Catalogue"});
                }
                childTypes.add(new String[]{"Session", ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session"});
            }
//            System.out.println("childTypes: " + childTypes);
        } else {
            // corpus can be added to the root node
            childTypes.add(new String[]{"Unattached Corpus Branch", ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Corpus"});
            childTypes.add(new String[]{"Unattached Session", ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session"});
        }
        return childTypes.elements();
    }
}
