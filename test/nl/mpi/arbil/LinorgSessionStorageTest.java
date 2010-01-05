package nl.mpi.arbil;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import nl.mpi.arbil.importexport.ShibbolethNegotiator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Document   : LinorgSessionStorageTest
 * Created on : December 28, 2009, 10:32:23 PM
 * @author Peter.Withers@mpi.nl
 */
public class LinorgSessionStorageTest {

    public LinorgSessionStorageTest() {
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
    public void testGetOriginatingUri() {
        System.out.println("getOriginatingUri");
        String[][] testInputArray = {
            {"file:/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml",
                "http://www.mpi.nl/IMDI/Schema/Continents.xml"},
            {"file:/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Some%20Continents.xml",
                "http://www.mpi.nl/IMDI/Schema/Some%20Continents.xml"
            },
            {"file:/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Some%20Continents.imdi#some.fragment.part.here",
                "http://www.mpi.nl/IMDI/Schema/Some%20Continents.imdi"
            }
        };
        LinorgSessionStorage instance = LinorgSessionStorage.getSingleInstance();
        for (String[] currentTest : testInputArray) {
            try {
                URI locationInCacheURI = new URI(currentTest[0]);
                URI expResult = new URI(currentTest[1]);
                URI result = instance.getOriginatingUri(locationInCacheURI);
                assertEquals(expResult, result);
            } catch (URISyntaxException urise) {
                fail(urise.getMessage());
            }
        }
    }

    /**
     * Test of getSaveLocation method, of class LinorgSessionStorage.
     */
    @Test
    public void testGetSaveLocation() {
        System.out.println("getSaveLocation");
        // this test must confirm that "/.linorg/imdicache/file/Users/testUser/.linorg/imdicache/" can never happen
        String[][] testInputArray = {
            {"file:/Users/testUser/.arbil/imdicache/20091222113221/20091222113221.imdi",
                "/Users/testUser/.arbil/imdicache/20091222113221/20091222113221.imdi",
                "/Users/testUser/.arbil/imdicache/"},
            {"file:/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/.arbil/imdicache/"},
            {"http://www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/.arbil/imdicache/"
            },
            {"file:/Users/testUser/.linorg/imdicache/20091222113221/20091222113221.imdi",
                "/Users/testUser/.linorg/imdicache/20091222113221/20091222113221.imdi",
                "/Users/testUser/.linorg/imdicache/"},
            {"file:/Users/testUser/.linorg/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/.linorg/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/.linorg/imdicache/"},
            {"http://www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/.linorg/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/.linorg/imdicache/"
            },
            {"file:/Users/testUser/Library/Mail%20Downloads/MPI-Korpus/Corpusstructure/1.imdi",
                "/Users/testUser/.linorg/imdicache/file/Users/testUser/Library/Mail Downloads/MPI-Korpus/Corpusstructure/1.imdi",
                "/Users/testUser/.linorg/imdicache/"
            }
        };
        LinorgSessionStorage instance = LinorgSessionStorage.getSingleInstance();
//        instance.storageDirectory = "/Users/testUser/";
        for (String[] currentTest : testInputArray) {
            instance.cacheDirectory = currentTest[2];
            File expResult = new File(currentTest[1]);
            File result = instance.getSaveLocation(currentTest[0]);
            assertEquals(expResult, result);
        }
    }
}
