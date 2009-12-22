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
        String inputUrlString = "file:/C:/Documents and Settings/user/Application Data/.arbil/imdicache/http/corpus1.mpi.nl/qfs1/media-archive/lac_data/Corpusstructure/southeastasia.imdi";
        URI expResult = URI.create("file:/C%3A/Documents+and+Settings/user/Application+Data/.arbil/imdicache/http/corpus1.mpi.nl/qfs1/media-archive/lac_data/Corpusstructure/southeastasia.imdi");
        URI result = ImdiTreeObject.conformStringToUrl(inputUrlString);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFullResourceURI method, of class ImdiTreeObject.
     */
    @Test
    public void testGetFullResourceURI() {
        System.out.println("getFullResourceURI");
        ImdiTreeObject instance = new ImdiTreeObject(URI.create("file:////test-江西-directory/test+subdirectory////test-file.imdi#test-xml-path"));
        instance.resourceUrlField = new ImdiField(instance, "test-sub-xml-path", "../test resource file.jpg");
        // the UNC (////) path need to be retained
        URI expResult = URI.create("file:////test-%E6%B1%9F%E8%A5%BF-directory/test+resource+file.jpg");
        URI result = instance.getFullResourceURI();
        assertEquals(expResult, result);
    }
}