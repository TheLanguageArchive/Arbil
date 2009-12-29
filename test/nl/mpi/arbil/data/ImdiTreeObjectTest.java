package nl.mpi.arbil.data;

import java.io.File;
import java.net.URI;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ImageIcon;
import nl.mpi.arbil.ImdiField;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Document   : ImdiTreeObjectTest
 * Created on : December 22, 2009, 13:52:47 PM
 * @author Peter.Withers@mpi.nl
 */
public class ImdiTreeObjectTest {

    public ImdiTreeObjectTest() {
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

    /**
     * Test of conformStringToUrl method, of class ImdiTreeObject.
     */
    @Test
    public void testConformStringToUrl() {
        System.out.println("conformStringToUrl");
        String[] testStringArray = {
            //            "C:\\Documents and Settings\\user\\Application Data\\.arbil\\imdicache\\http\\corpus1.mpi.nl\\qfs1\\media-archive\\lac_data\\Corpusstructure\\southeastasia.imdi",
            //            "/C/Documents and Settings/user/Application Data/.arbil/imdicache/http/corpus1.mpi.nl/qfs1/media-archive/lac_data/Corpusstructure/southeastasia.imdi",
            "file:/C:/Documents and Settings/user/Application Data/.arbil/imdicache/http/corpus1.mpi.nl/qfs1/media-archive/lac_data/Corpusstructure/southeastasia.imdi"
        };
        for (String testString : testStringArray) {
            URI expResult = URI.create("file:/C:/Documents%20and%20Settings/user/Application%20Data/.arbil/imdicache/http/corpus1.mpi.nl/qfs1/media-archive/lac_data/Corpusstructure/southeastasia.imdi");
            URI result = ImdiTreeObject.conformStringToUrl(testString);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getFullResourceURI method, of class ImdiTreeObject.
     */
    @Test
    public void testGetFullResourceURI() {
        System.out.println("getFullResourceURI"); // TODO: test this with unicode: 江西 : %e6%b1%9f%e8%a5%bf
        String[][] testStringArray = {
            {"file:////test- directory/test subdirectory////test-file.imdi#test-xml-path",
                "../test resource file.jpg",
                "file:////test-%20directory/test%20resource%20file.jpg"
            },
            {"file:////test- directory/test subdirectory////test-file.imdi#test-xml-path",
                "file:/otherlocation/test resource file.jpg",
                "file:/otherlocation/test%20resource%20file.jpg"
            }
        };
        for (String testString[] : testStringArray) {
            ImdiTreeObject instance = new ImdiTreeObject(ImdiTreeObject.conformStringToUrl(testString[0]));
            instance.resourceUrlField = new ImdiField(instance, "test-sub-xml-path", testString[1]);
            // the UNC (////) path need to be retained
            URI expResult = URI.create(testString[2]);
            URI result = instance.getFullResourceURI();
            assertEquals(expResult, result);
        }
    }
     * Test of getFile method, of class ImdiTreeObject.
     */
    @Test
    public void testGetFile() {
        System.out.println("getFile");
        // TODO: test this with unicode: 江西 : %e6%b1%9f%e8%a5%bf
        System.out.println("getFullResourceURI"); // TODO: test this with unicode: 江西 : %e6%b1%9f%e8%a5%bf
        String[][] testStringArray = {
            //            {"file:////test-directory/test subdirectory////test-file.imdi#test-xml-path",
            //                "//test-directory/test subdirectory/test-file.imdi"
            // TODO: this UNC path fails the test on Mac but might pass on Windows and assumption this must be tested
            //            },
            {"file:////.host/Shared%20Folders/imdiapi-co/mpi/imdi/",
                "////.host/Shared%20Folders/imdiapi-co/mpi/imdi/"}, // todo: should this not be decoded?
            {"file:/test-directory/test subdirectory////test-file.imdi#test-xml-path",
                "/test-directory/test subdirectory/test-file.imdi"
            }
        };
        for (String testString[] : testStringArray) {
            ImdiTreeObject instance = new ImdiTreeObject(ImdiTreeObject.conformStringToUrl(testString[0]));
            File expResult = new File(testString[1]);
            File result = instance.getFile();
            assertEquals(expResult, result);
        }
    }
    /**
}
