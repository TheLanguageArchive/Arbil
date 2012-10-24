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
package nl.mpi.arbil.data;

import java.util.Map;
import nl.mpi.arbil.util.XsdChecker;
import java.util.ArrayList;
import java.util.Enumeration;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import java.net.URI;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.ArbilMetadataException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Document   : MetadataBuilder
 * Created on : Nov 4, 2010, 10:46:21 PM
 * @author Peter.Withers@mpi.nl
 */
@Ignore
public class MetadataBuilderTest {

    public MetadataBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
	ArbilDesktopInjector.injectHandlers();
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

    private void checkAgainstSchema(ArbilDataNode testImdiObject) {
        XsdChecker xsdChecker = new XsdChecker();
        String checkerResult;
        checkerResult = xsdChecker.simpleCheck(testImdiObject.getFile());
        assertNull("schema error: " + checkerResult, checkerResult);
    }

    @Test
    public void testAddChildNodeJar() {
        testAddChildNode(MetadataBuilderTest.class.getResource("/nl/mpi/arbil/data/clarin.eu_cr1_p_1271859438162.xsd").toExternalForm());
    }

    @Test
    public void testAddChildNodeServer() {
        testAddChildNode("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438162/xsd");
    }

    @Test
    public void testAddChildNodeNoSubNodesInProfile() {
        testAddChildNode("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614026/xsd");
    }

    @Test
    public void testAddChildNodeImdiSession() {
        testAddChildNode(".METATRANSCRIPT.Session : this will fail as imdi add is currently handled by metadatareader and should be consolidated");
    }
    private void testAddChildNode(String currentTestTemplate) {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();

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
        URI targetFileURI = ArbilSessionStorage.getSingleInstance().getNewArbilFileName(ArbilSessionStorage.getSingleInstance().getCacheDirectory(), currentTestTemplate);
        //            try {
        //                        targetFileURI = MetadataReader.getSingleInstance().addFromTemplate(new File(eniryFileURI), "Entity");
        //                        gedcomImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
        //                        gedcomImdiObject.waitTillLoaded();
        targetFileURI = componentBuilder.createComponentFile(targetFileURI, ArbilSessionStorage.getSingleInstance().updateCache(currentTestTemplate, 7, false).toURI(), false);
        //            } catch (URISyntaxException ex) {
        //                GuiHelper.linorgBugCatcher.logError(ex);
        //                return;
        //            }
        ArbilDataNode gedcomImdiObject = ArbilDataNodeLoader.getSingleInstance().getArbilDataNodeWithoutLoading(targetFileURI);
        //TreeHelper.getSingleInstance().saveLocations(new ImdiTreeObject[]{gedcomImdiObject}, null);
        gedcomImdiObject.loadArbilDom();
        checkAgainstSchema(gedcomImdiObject);
        //            gedcomImdiObject.waitTillLoaded();
        ArrayList<String> allTemplates = gedcomImdiObject.nodeTemplate.listAllTemplates();
        ArrayList<ArbilDataNode> currentLevel = new ArrayList<ArbilDataNode>();
        //            ImdiLoader.getSingleInstance().schemaCheckLocalFiles = true;
//        for (int addNodeCount = 0; addNodeCount < 3; addNodeCount++) {
        currentLevel.add(gedcomImdiObject);
//            ArrayList<ImdiTreeObject> completedNodes = new ArrayList<ImdiTreeObject>();
        while (currentLevel.size() > 0) {
            for (ArbilDataNode currentLevelNode : currentLevel) {
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
//                    completedNodes.add(currentLevelNode);
            }
//                gedcomImdiObject.waitTillLoaded();
            gedcomImdiObject.loadArbilDom();
            gedcomImdiObject.waitTillLoaded();
            //gedcomImdiObject.reloadNode();
            //gedcomImdiObject.waitTillLoaded();
            //                gedcomImdiObject.saveChangesToCache(false);
            //                gedcomImdiObject.waitTillLoaded();
            //                gedcomImdiObject.loadImdiDom();
            // loop over all the children and make sure any required values are set
            //gedcomImdiObject.loadImdiDom();
            //                          gedcomImdiObject.waitTillLoaded();
            for (ArbilDataNode childNode : gedcomImdiObject.getAllChildren()) {
                if (childNode != null) {
                    Map<String, ArbilField[]> currentFields = childNode.getFields();
                    if (currentFields != null) {
                        // populate any fields in the list provided
                        for (String[] currentFieldData : autoFields) {
                            //                                for (ImdiField[] currentField : addedImdiObject.getFields().values()) {
                            //                                    if (currentField != null && currentField.length > 0) {
                            //                                        System.out.println(currentField[0].getTranslateFieldName());
                            //                                        currentField[0].setFieldValue(currentFieldData[1], false, true);
                            //                                    }
                            //                                }
                            ArbilField[] currentFieldArray = currentFields.get(currentFieldData[0]);
                            if (currentFieldArray != null && currentFieldArray.length > 0) {
                                for (ArbilField currentField : currentFieldArray) {
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
            gedcomImdiObject.loadArbilDom();
            checkAgainstSchema(gedcomImdiObject);
            //                gedcomImdiObject.waitTillLoaded();
            //
            ArrayList<ArbilDataNode> nextLevel = new ArrayList<ArbilDataNode>();
            for (ArbilDataNode currentLevelNode : currentLevel) {
                for (ArbilDataNode nextLevelNode : currentLevelNode.getChildArray()) {
                    nextLevel.add(nextLevelNode);
                }
            }
            currentLevel.clear();
            currentLevel = nextLevel;
//                for (ImdiTreeObject nextLevelNode : gedcomImdiObject.getAllChildren()) {
//                    if (!completedNodes.contains(nextLevelNode)) {
//                        currentLevel.add(nextLevelNode);
//                    }
//
//                }
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
//        }
    }
}
