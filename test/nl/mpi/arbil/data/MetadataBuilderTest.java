package nl.mpi.arbil.data;

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

    @Test
    public void testAddChildNode() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
        for (String currentTestTemplate : new String[]{"http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438162/xsd"}) {


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
            ImdiTreeObject gedcomImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
            gedcomImdiObject.waitTillLoaded();
            ArrayList<String> allTemplates = gedcomImdiObject.nodeTemplate.listAllTemplates();
            ArrayList<ImdiTreeObject> currentLevel = new ArrayList<ImdiTreeObject>();
            currentLevel.add(gedcomImdiObject);
//            ImdiLoader.getSingleInstance().schemaCheckLocalFiles = true;
            while (currentLevel.size() > 0) {
                for (ImdiTreeObject currentLevelNode : currentLevel) {
                    //ImdiTreeObject currentLevelNode = currentLevel.remove(0);
                    //currentLevel.remove(currentLevelNode);
                    System.out.println(currentLevelNode.getUrlString());
                    for (Enumeration menuItemName = currentLevelNode.getParentDomNode().nodeTemplate.listTypesFor(currentLevelNode); menuItemName.hasMoreElements();) {
                        String[] currentField = (String[]) menuItemName.nextElement();
                        System.out.println(currentField[0] + " : " + currentField[1]);// + " : "+ currentField[2]);
                        allTemplates.remove(currentField[1]);
                        try {
                            URI linkUri = metadataBuilder.addChildNode(currentLevelNode, currentField[1], null, null, null);
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
                gedcomImdiObject.waitTillLoaded();
                gedcomImdiObject.reloadNode();
                gedcomImdiObject.waitTillLoaded();
                XsdChecker xsdChecker = new XsdChecker();
                String checkerResult;
                checkerResult = xsdChecker.simpleCheck(gedcomImdiObject.getFile(), gedcomImdiObject.getURI());
                assertNull("schema error: " + checkerResult, checkerResult);
                ArrayList<ImdiTreeObject> nextLevel = new ArrayList<ImdiTreeObject>();
                for (ImdiTreeObject currentLevelNode : currentLevel) {
                    for (ImdiTreeObject nextLevelNode : currentLevelNode.getChildArray()) {
                        nextLevel.add(nextLevelNode);
                    }
                }
                currentLevel.clear();
                currentLevel = nextLevel;
            }
            for (String remainingTemplate : allTemplates) {
                System.out.println("unused template: " + remainingTemplate);
            }
            assertTrue("Not all templates have been used", allTemplates.isEmpty());
            String expResult = "";//currentTest[1];
            String result = "";//instance.getNodeTypeFromMimeType(mimeType);
            assertEquals(expResult, result);
        }
    }
}
