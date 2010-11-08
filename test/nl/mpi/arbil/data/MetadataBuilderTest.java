package nl.mpi.arbil.data;

import java.util.Hashtable;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.XsdChecker;
import java.util.ArrayList;
import java.util.Enumeration;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import java.net.URI;
import nl.mpi.arbil.clarin.ArbilMetadataException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Document   : MetadataBuilder
 * Created on : Nov 4, 2010, 10:46:21 PM
 * @author Peter.Withers@mpi.nl
 */
public class MetadataBuilderTest {

    public MetadataBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void checkAgainstSchema(ImdiTreeObject testImdiObject) {
        XsdChecker xsdChecker = new XsdChecker();
        String checkerResult;
        checkerResult = xsdChecker.simpleCheck(testImdiObject.getFile(), testImdiObject.getURI());
        assertNull("schema error: " + checkerResult, checkerResult);
    }

    @Test
    public void testAddChildNode() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();

        String[][] autoFields = new String[][]{
            {"Language.ISO639.iso-639-3-code", "zts", ".CMD.Components.TextProfile(x).TEXT.SubjectLanguages(x).SubjectLanguage(x).Language.ISO639.iso-639-3-code"},
            {"Language.ISO639.iso-639-3-code", "ymt", ".CMD.Components.TextProfile(x).TEXT.SubjectLanguages(x).SubjectLanguage(x).Language.ISO639.iso-639-3-code"},
            {"AnnotationType", "Phonetics", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).AnnotationType(x).AnnotationType"},
            {"Modality", "Facial-expressions", ".CMD.Components.TextProfile(x).TEXT.Content(x).Modality(x).Modality"},
            {"Code", "PK", ".CMD.Components.TextProfile(x).TEXT.OriginLocation(x).Location.Country(x).Code"},
            {"Code", "EU", ".CMD.Components.TextProfile(x).TEXT.OriginLocation(x).Location.Continent(x).Code"},
            {"Genre", "Discourse", ".CMD.Components.TextProfile(x).TEXT.Content(x).Genre"},
            {"SubGenre", "Interview", ".CMD.Components.TextProfile(x).TEXT.Content(x).SubGenre"},
            {"Involvement", "non-elicited", ".CMD.Components.TextProfile(x).TEXT.Content(x).CommunicationContext(x).Involvement"},
            {"PlanningType", "spontaneous", ".CMD.Components.TextProfile(x).TEXT.Content(x).CommunicationContext(x).PlanningType"},
            {"Interactivity", "semi-interactive", ".CMD.Components.TextProfile(x).TEXT.Content(x).CommunicationContext(x).Interactivity"},
            {"SocialContext", "Family", ".CMD.Components.TextProfile(x).TEXT.Content(x).CommunicationContext(x).SocialContext"},
            {"EventStructure", "Not a natural format", ".CMD.Components.TextProfile(x).TEXT.Content(x).CommunicationContext(x).EventStructure"},
            {"Channel", "wizard-of-oz", ".CMD.Components.TextProfile(x).TEXT.Content(x).CommunicationContext(x).Channel"},
            {"Age", "197", ".CMD.Components.TextProfile(x).TEXT.Authors(x).Author(x).Age"},
            {"BirthYear", "1821", ".CMD.Components.TextProfile(x).TEXT.Authors(x).Author(x).BirthYear"},
            {"Sex", "Male", ".CMD.Components.TextProfile(x).TEXT.Authors(x).Author(x).Sex"},
            {"Anonymized", "true", ".CMD.Components.TextProfile(x).TEXT.Authors(x).Author(x).Anonymized"},
            {"CreationYear", "1364", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).CreationYear"},
            {"Derivation", "Translation", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).Derivation"},
            {"DerivationMode", "Automatic/Manual", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).DerivationMode"},
            {"AnnotationStyle", "stand-off", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).AnnotationStyle"},
            {"AnnotationStyle", "mixed", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).AnnotationStyle"},
            {"Anonymized", "true", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).Anonymized"},
            {"AnnotationFormat", "text/x-shoebox-lexicon", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).AnnotationFormat(x).AnnotationFormat"},
            {"Number", "546", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).TotalSize(x).Number"},
            {"ISO639.iso-639-3-code", "amc", ".CMD.Components.TextProfile(x).TEXT.WrittenResources(x).WrittenResource(x).Language(x).ISO639.iso-639-3-code"},
            {"Dominant", "true", ".CMD.Components.TextProfile(x).TEXT.SubjectLanguages(x).SubjectLanguage(x).Dominant"},
            {"SourceLanguage", "true", ".CMD.Components.TextProfile(x).TEXT.SubjectLanguages(x).SubjectLanguage(x).SourceLanguage"},
            {"TargetLanguage", "true", ".CMD.Components.TextProfile(x).TEXT.SubjectLanguages(x).SubjectLanguage(x).TargetLanguage"}
        };
        // loop over the templates and profiles that are to be tested
        for (String currentTestTemplate : new String[]{MetadataBuilderTest.class.getResource("/nl/mpi/arbil/data/clarin.eu_cr1_p_1271859438162.xsd").toExternalForm(), "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438162/xsd", ".METATRANSCRIPT.Session : this is currently handled by metadatareader and should be consolidated"}) {
            URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), currentTestTemplate);
            //            try {
            //                        targetFileURI = MetadataReader.getSingleInstance().addFromTemplate(new File(eniryFileURI), "Entity");
            //                        gedcomImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
            //                        gedcomImdiObject.waitTillLoaded();
            targetFileURI = componentBuilder.createComponentFile(targetFileURI, LinorgSessionStorage.getSingleInstance().updateCache(currentTestTemplate, 7).toURI(), false);
            //            } catch (URISyntaxException ex) {
            //                GuiHelper.linorgBugCatcher.logError(ex);
            //                return;
            //            }
            ImdiTreeObject gedcomImdiObject = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(targetFileURI);
            //TreeHelper.getSingleInstance().saveLocations(new ImdiTreeObject[]{gedcomImdiObject}, null);
            gedcomImdiObject.loadImdiDom();
            checkAgainstSchema(gedcomImdiObject);
            //            gedcomImdiObject.waitTillLoaded();
            ArrayList<String> allTemplates = gedcomImdiObject.nodeTemplate.listAllTemplates();
            ArrayList<ImdiTreeObject> currentLevel = new ArrayList<ImdiTreeObject>();
            currentLevel.add(gedcomImdiObject);
            //            ImdiLoader.getSingleInstance().schemaCheckLocalFiles = true;
            while (currentLevel.size() > 0) {
                for (ImdiTreeObject currentLevelNode : currentLevel) {
                    //ImdiTreeObject currentLevelNode = currentLevel.remove(0);
                    //currentLevel.remove(currentLevelNode);
                    System.out.println(currentLevelNode.getUrlString());
                    // loop over all nodes and add one of every type that would be shown in the add menu for each node and sub node
                    for (Enumeration menuItemName = currentLevelNode.getParentDomNode().nodeTemplate.listTypesFor(currentLevelNode); menuItemName.hasMoreElements();) {
                        String[] currentComponent = (String[]) menuItemName.nextElement();
                        System.out.println(currentComponent[0] + " : " + currentComponent[1]);// + " : "+ currentField[2]);
                        try {
                            URI linkUri = metadataBuilder.addChildNode(currentLevelNode, currentComponent[1], null, null, null);
                            //ImdiTreeObject addedImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, linkUri);
                            // keep track of which addables have been used
                            allTemplates.remove(currentComponent[1]);
                        } catch (ArbilMetadataException exception) {
                            fail(exception.getMessage());
                        }
                        //        System.out.println("getNodeTypeFromMimeType");
                        //        String[][] testCases = {
                        //            {"application/pdf", ".METATRANSCRIPT.Session.Resources.WrittenResource"},
                        //            {"image/jpeg", ".METATRANSCRIPT.Session.Resources.MediaFile"},
                        //            {"Manual/WrittenResource", ".METATRANSCRIPT.Session.Resources.WrittenResource"},
                        //            {"Manual/MediaFile", ".METATRANSCRIPT.Session.Resources.MediaFile"}
                        //        };
                        //        for (String[] currentTest : testCases) {
                        //            String mimeType = currentTest[0];
                        //            MetadataReader instance = MetadataReader.getSingleInstance();
                    }
                }
//                gedcomImdiObject.waitTillLoaded();
                gedcomImdiObject.loadImdiDom();
                gedcomImdiObject.waitTillLoaded();
                //gedcomImdiObject.reloadNode();
                //gedcomImdiObject.waitTillLoaded();
                //                gedcomImdiObject.saveChangesToCache(false);
                //                gedcomImdiObject.waitTillLoaded();
                //                gedcomImdiObject.loadImdiDom();
                // loop over all the children and make sure any required values are set
                //gedcomImdiObject.loadImdiDom();
                //                          gedcomImdiObject.waitTillLoaded();
                for (ImdiTreeObject childNode : gedcomImdiObject.getAllChildren()) {
                    if (childNode != null) {
                        Hashtable<String, ImdiField[]> currentFields = childNode.getFields();
                        if (currentFields != null) {
                            // populate any fields in the list provided
                            for (String[] currentFieldData : autoFields) {
                                //                                for (ImdiField[] currentField : addedImdiObject.getFields().values()) {
                                //                                    if (currentField != null && currentField.length > 0) {
                                //                                        System.out.println(currentField[0].getTranslateFieldName());
                                //                                        currentField[0].setFieldValue(currentFieldData[1], false, true);
                                //                                    }
                                //                                }
                                ImdiField[] currentFieldArray = currentFields.get(currentFieldData[0]);
                                if (currentFieldArray != null && currentFieldArray.length > 0) {
                                    for (ImdiField currentField : currentFieldArray) {
                                        System.out.println("getGenericFullXmlPath: " + currentField.getGenericFullXmlPath());
                                        System.out.println("currentFieldData[2]: " + currentFieldData[2]);
                                        if (currentField.getGenericFullXmlPath().equals(currentFieldData[2])) {
                                            currentField.setFieldValue(currentFieldData[1], false, true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                gedcomImdiObject.saveChangesToCache(false);
                gedcomImdiObject.loadImdiDom();
                checkAgainstSchema(gedcomImdiObject);
                //                gedcomImdiObject.waitTillLoaded();
                //
                ArrayList<ImdiTreeObject> nextLevel = new ArrayList<ImdiTreeObject>();
                for (ImdiTreeObject currentLevelNode : currentLevel) {
                    for (ImdiTreeObject nextLevelNode : currentLevelNode.getChildArray()) {
                        nextLevel.add(nextLevelNode);
                    }
                }
                currentLevel.clear();
                currentLevel = nextLevel;
            }
            boolean allTemplatesUsed = true;
            for (String remainingTemplate : allTemplates) {
                if (remainingTemplate.startsWith(".CMD.Header") || remainingTemplate.startsWith(".CMD.Resources")) {
                    System.out.println("ignoring template: " + remainingTemplate);
                } else {
                    allTemplatesUsed = false;
                    System.out.println("unused template: " + remainingTemplate);
                }
            }
            assertTrue("Not all templates have been used", allTemplatesUsed);
            String expResult = "";//currentTest[1];
            String result = "";//instance.getNodeTypeFromMimeType(mimeType);
            assertEquals(expResult, result);
        }
    }
}
