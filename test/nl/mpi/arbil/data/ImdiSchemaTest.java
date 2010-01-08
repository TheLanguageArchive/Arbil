package nl.mpi.arbil.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import nl.mpi.arbil.ArbilTemplate;
import nl.mpi.arbil.ImdiField;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Document   : ImdiSchemaTest
 * Created on : Jan 5, 2010, 16:26:47 PM
 * @author Peter.Withers@mpi.nl
 */
public class ImdiSchemaTest {

    public ImdiSchemaTest() {
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

    public static void assertFileContents(URI leftUri, URI rightUri) {
//        System.out.println("leftUri: " + leftUri);
//        System.out.println("rightUri: " + rightUri);
        try {
            BufferedReader leftReader = new BufferedReader(new InputStreamReader(leftUri.toURL().openStream()));
            BufferedReader rightReader = new BufferedReader(new InputStreamReader(rightUri.toURL().openStream()));
            String leftLine;
            while ((leftLine = leftReader.readLine()) != null) {
                leftLine = leftLine.trim();
                String rightLine = rightReader.readLine().trim();
                boolean skipLine = false;

                if (leftLine.startsWith("Date=")) {
                    skipLine = true; // ignore the date and creator differences
                }
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
     * Test of addFromTemplate method, of class ImdiSchema.
     */
    @Test
    public void testAddFromTemplate() {
        System.out.println("addFromTemplate");
        ImdiSchema instance = ImdiSchema.getSingleInstance();
        String[] testTemplateTypes = {".METATRANSCRIPT.Corpus", ".METATRANSCRIPT.Session", ".METATRANSCRIPT.Catalogue"};
        URI[] expectedTypes = null;
        try {
            expectedTypes = new URI[]{
                        ImdiSchemaTest.class.getResource("/nl/mpi/arbil/data/testfiles/corpus.imdi").toURI(),
                        ImdiSchemaTest.class.getResource("/nl/mpi/arbil/data/testfiles/session.imdi").toURI(),
                        ImdiSchemaTest.class.getResource("/nl/mpi/arbil/data/testfiles/catalogue.imdi").toURI()
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
     * Test of getNodeTypeFromMimeType method, of class ImdiSchema.
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
            ImdiSchema instance = ImdiSchema.getSingleInstance();
            String expResult = currentTest[1];
            String result = instance.getNodeTypeFromMimeType(mimeType);
            assertEquals(expResult, result);
        }
    }
}
