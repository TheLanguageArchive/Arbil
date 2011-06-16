package nl.mpi.arbil.MetadataFile;

import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import org.junit.Ignore;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.arbil.ArbilTestInjector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Document   : MetadataReaderTest
 * Created on : Jan 5, 2010, 16:26:47 PM
 * @author Peter.Withers@mpi.nl
 */
public class MetadataReaderTest {

    public MetadataReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
	ArbilTestInjector.injectHandlers();
	
	final SessionStorage sessionStorage = ArbilSessionStorage.getSingleInstance();
	ArbilTestInjector.injectSessionStorage(sessionStorage);
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

    public static void assertFileContents(URI leftUri, URI rightUri) {
        System.out.println("leftUri: " + leftUri);
        System.out.println("rightUri: " + rightUri);
        try {
            BufferedReader leftReader = new BufferedReader(new InputStreamReader(leftUri.toURL().openStream()));
            BufferedReader rightReader = new BufferedReader(new InputStreamReader(rightUri.toURL().openStream()));
            String leftLine;
            while ((leftLine = leftReader.readLine()) != null) {
                leftLine = leftLine.trim();
                String rightLine = rightReader.readLine().trim();
                boolean skipLine = false;

                if (leftLine.startsWith("Date=") || leftLine.startsWith("Originator=\"Arbil")) {
                    skipLine = true; // ignore the date and creator differences
                }
                System.out.println("leftLine: " + leftLine);
                System.out.println("rightLine: " + rightLine);
                if (!skipLine) {
                    assertEquals(leftLine, rightLine);
                }

            }
            assertNull("Left file has more lines then the expected.", leftReader.readLine());
            assertNull("Right file has more lines then the actual.", rightReader.readLine());
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
    }

    /**
     * Test of addFromTemplate method, of class MetadataReader.
     */
    @Test
    @Ignore
    public void testAddFromTemplate() {
        System.out.println("addFromTemplate");
        MetadataReader instance = MetadataReader.getSingleInstance();
        String[] testTemplateTypes = {".METATRANSCRIPT.Corpus", ".METATRANSCRIPT.Session", ".METATRANSCRIPT.Catalogue"};
        URI[] expectedTypes = null;
        try {
            expectedTypes = new URI[]{
                        MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/corpus.imdi").toURI(),
                        MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/session.imdi").toURI(),
                        MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/catalogue.imdi").toURI()
                    };
        } catch (URISyntaxException urise) {
            fail(urise.getMessage());
        }
        for (int testCounter = 0; testCounter < testTemplateTypes.length; testCounter++) {
            try {
                File destinationFile = File.createTempFile("testFile", ".imdi");
                destinationFile.deleteOnExit();
                String templateType = testTemplateTypes[testCounter];
                URI expResult = expectedTypes[testCounter];
                URI result = instance.addFromTemplate(destinationFile, templateType);
                System.out.println("assertFileContents: " + expResult);
                assertFileContents(expResult, result);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    /**
     * Test of getNodeTypeFromMimeType method, of class MetadataReader.
     */
    @Test
    public void testGetNodeTypeFromMimeType() {
        System.out.println("getNodeTypeFromMimeType");
        String[][] testCases = {
            {"application/pdf", ".METATRANSCRIPT.Session.Resources.WrittenResource"},
            {"image/jpeg", ".METATRANSCRIPT.Session.Resources.MediaFile"},
            {"Manual/WrittenResource", ".METATRANSCRIPT.Session.Resources.WrittenResource"},
            {"Manual/MediaFile", ".METATRANSCRIPT.Session.Resources.MediaFile"}
        };
        for (String[] currentTest : testCases) {
            String mimeType = currentTest[0];
            MetadataReader instance = MetadataReader.getSingleInstance();
            String expResult = currentTest[1];
            String result = instance.getNodeTypeFromMimeType(mimeType);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of insertFromTemplate method, of class MetadataReader.
     */
//    @Test
//    public void testInsertFromTemplate() {
//        System.out.println("insertFromTemplate");
//        ArbilTemplate currentTemplate = ArbilTemplateManager.getSingleInstance().getTemplate("Default");
//        MetadataReader instance = MetadataReader.getSingleInstance();
//        String[][][] testTemplates = {
//            {
//                {".METATRANSCRIPT.Catalogue.Access.Description",null},
//                {".METATRANSCRIPT.Catalogue.Author",null},
//                {".METATRANSCRIPT.Catalogue.ContentType",null},
//                {".METATRANSCRIPT.Catalogue.Description",null},
//                {".METATRANSCRIPT.Catalogue.DocumentLanguages.Description",null},
//                {".METATRANSCRIPT.Catalogue.DocumentLanguages.Language",".METATRANSCRIPT.Catalogue.DocumentLanguages.Language(1)"},
//                {".METATRANSCRIPT.Catalogue.Keys.Key",null},
//                {".METATRANSCRIPT.Catalogue.Location",".METATRANSCRIPT.Catalogue.Location(2)"}, // the plain template already contains one location
//                {".METATRANSCRIPT.Catalogue.Project.Author",null},
//                {".METATRANSCRIPT.Catalogue.Project.Description",null},
//                {".METATRANSCRIPT.Catalogue.Publisher",null},
//                {".METATRANSCRIPT.Catalogue.SubjectLanguages.Description",null},
//                {".METATRANSCRIPT.Catalogue.SubjectLanguages.Language",".METATRANSCRIPT.Catalogue.SubjectLanguages.Language(1)"},
//                {".METATRANSCRIPT.Catalogue.SubjectLanguages.Language(1).Description",".METATRANSCRIPT.Catalogue.SubjectLanguages.Language(1)"}
//            },
//            {
//                {".METATRANSCRIPT.Corpus.Description",null}
//            },
//            {
//                {".METATRANSCRIPT.Session.Description",null},
//                {".METATRANSCRIPT.Session.MDGroup.Actors.Actor",".METATRANSCRIPT.Session.MDGroup.Actors.Actor(1)"},
//                {".METATRANSCRIPT.Session.MDGroup.Actors.Actor(1).Description",".METATRANSCRIPT.Session.MDGroup.Actors.Actor(1)"},
//                {".METATRANSCRIPT.Session.MDGroup.Actors.Actor(1).Keys.Key",".METATRANSCRIPT.Session.MDGroup.Actors.Actor(1)"},
//                {".METATRANSCRIPT.Session.MDGroup.Actors.Actor(1).Languages.Language",".METATRANSCRIPT.Session.MDGroup.Actors.Actor(1).Languages.Language(1)"},
//                {".METATRANSCRIPT.Session.MDGroup.Content.Keys.Key",null},
//                {".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language",".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language(1)"},
//                {".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language(1).Description",".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language(1)"},
//                {".METATRANSCRIPT.Session.MDGroup.Keys.Key",null},
//                {".METATRANSCRIPT.Session.Resources.MediaFile",".METATRANSCRIPT.Session.Resources.MediaFile(1)"},
//                {".METATRANSCRIPT.Session.Resources.Source",".METATRANSCRIPT.Session.Resources.Source(1)"},
//                {".METATRANSCRIPT.Session.Resources.Source(1).Keys.Key",".METATRANSCRIPT.Session.Resources.Source(1)"},
//                {".METATRANSCRIPT.Session.Resources.WrittenResource",".METATRANSCRIPT.Session.Resources.WrittenResource(1)"},
//                {".METATRANSCRIPT.Session.Resources.WrittenResource(1).Keys.Key",".METATRANSCRIPT.Session.Resources.WrittenResource(1)"}
//            }
//        };
//        String[] testTemplateTypes = {".METATRANSCRIPT.Catalogue", ".METATRANSCRIPT.Corpus", ".METATRANSCRIPT.Session"};
//        URI[] baseMetadataFiles = null;
//        try {
//            baseMetadataFiles = new URI[]{
//                        MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/catalogue-withparts.imdi").toURI(),
//                        MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/corpus-withparts.imdi").toURI(),
//                        MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/session-withparts.imdi").toURI()
//                    };
//        } catch (URISyntaxException urise) {
//            fail(urise.getMessage());
//        }
//        for (int testCounter = 0; testCounter < baseMetadataFiles.length; testCounter++) {
//            try {
//                File destinationFile = File.createTempFile("testFile", ".imdi");
//                System.out.println("destinationFile: " + destinationFile);
//                destinationFile.deleteOnExit();
//                String templateType = testTemplateTypes[testCounter];
//                URI expResult = baseMetadataFiles[testCounter];
//                URI targetMetadataUri = instance.addFromTemplate(destinationFile, templateType);
//                File resourceDestinationDirectory = destinationFile.getParentFile();
//                String targetXmlPath = null;
//                Document targetImdiDom = ImdiTreeObject.api.loadIMDIDocument(new OurURL(targetMetadataUri.toURL()), false);
//                URI resourceUrl = null;
//                String mimeType = null;
//
//                for (String[] currentTemplateTest : testTemplates[testCounter]) {
//                    URI subNodeResult = instance.insertFromTemplate(currentTemplate, targetMetadataUri, resourceDestinationDirectory, currentTemplateTest[0], targetXmlPath, targetImdiDom, resourceUrl, mimeType);
//                    // test the subNodeResult
//                    assertEquals(currentTemplateTest[1], subNodeResult.getFragment());
//                }
//                ImdiTreeObject.api.writeDOM(targetImdiDom, destinationFile, true); // add the id attributes
//                targetImdiDom = ImdiTreeObject.api.loadIMDIDocument(new OurURL(targetMetadataUri.toURL()), false);
//                ImdiTreeObject.api.writeDOM(targetImdiDom, destinationFile, false); // add the id attributes
//                System.out.println("assertFileContents: " + expResult);
//                assertFileContents(expResult, targetMetadataUri);
//            } catch (IOException ioe) {
//                fail(ioe.getMessage());
//            }
//        }
//    }
}
