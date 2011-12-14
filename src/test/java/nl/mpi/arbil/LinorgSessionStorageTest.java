package nl.mpi.arbil;

import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
    @Ignore("Machine specific, should be fixed")
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
        ArbilSessionStorage instance = new ArbilSessionStorage();
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
     * Test of pathIsInsideCache method, of class LinorgSessionStorage.
     */
    @Test
    @Ignore("Machine specific, should be fixed")
    public void testPathIsInsideCache() {
        System.out.println("pathIsInsideCache");
        ArbilSessionStorage instance = new ArbilSessionStorage();
        //instance.storageDirectory = new File("/Users/testUser/.arbil/");
        File[] testInputArrayTrue = {
            new File("/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml"),
            new File("//Users//testUser//.arbil//imdicache//http//www.mpi.nl//IMDI//Schema//Continents.xml"),
            new File("/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml")
        };
        File[] testInputArrayFalse = {
            new File("/Users/testUser/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml"),
            new File("c:\\Users\\testUser\\.arbil\\imdicache\\http\\www.mpi.nl\\IMDI\\Schema\\Continents.xml"),
            new File("/Users/testUser/.arbil//http/www.mpi.nl/IMDI/Schema/Continents.xml")
        };
        for (File fullTestFile : testInputArrayTrue) {
            assertTrue(instance.pathIsInsideCache(fullTestFile));
        }
        for (File fullTestFile : testInputArrayFalse) {
            assertFalse(instance.pathIsInsideCache(fullTestFile));
        }
        //instance.storageDirectory = oldStorageDirectory;
    }

    /**
     * Test of getExportPath method, of class LinorgSessionStorage.
     */
    @Test
    @Ignore("Machine specific, should be fixed")
    public void testGetExportPath() {
        System.out.println("getExportPath");
        ArbilSessionStorage instance = new ArbilSessionStorage();
        String[][] testInputArray = {
            {"file:/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Continents.xml",
                "/home/user/exportdirectory/http/www.mpi.nl/IMDI/Schema/Continents.xml"},
            {"file:/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Some%20Continents.xml",
                "/home/user/exportdirectory/http/www.mpi.nl/IMDI/Schema/Some%20Continents.xml"
            },
            {"file:/Users/testUser/.arbil/imdicache/http/www.mpi.nl/IMDI/Schema/Some%20Continents.imdi",
                "/home/user/exportdirectory/http/www.mpi.nl/IMDI/Schema/Some%20Continents.imdi"
            },
            {"file:/Users/petwit/TestStorageDirectory/ArbilWorkingFiles/20100315171558/20100315171558.imdi",
                "/home/user/exportdirectory/20100315171558/20100315171558.imdi"
            }
        };
        for (String[] currentTest : testInputArray) {
            String pathString = currentTest[0];
            String destinationDirectory = "/home/user/exportdirectory/";
            File expResult = new File(currentTest[1]);
            File result = instance.getExportPath(pathString, destinationDirectory);
            assertEquals(expResult, result);
        }
    }
    /**
     * Test of getSaveLocation method, of class LinorgSessionStorage.
     */
    @Test
    @Ignore("Machine specific, should be fixed")
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
            },
            {"file:/Z/Documents and Settings/micsta/Application Data/.arbil/imdicache/file/C/Documents and Settings/micsta/Application Data/.arbil/imdicache/file/P/L&C_Assist_Task/Nick Corpus/Arbil_Corpus_structure/arbil_export_03/arbil_export/lac_data/Corpusstructure/l-lao/1.imdi",
                "/Z/Documents and Settings/micsta/Application Data/.arbil/imdicache/file/P/L&C_Assist_Task/Nick Corpus/Arbil_Corpus_structure/arbil_export_03/arbil_export/lac_data/Corpusstructure/l-lao/1.imdi",
                "/Z/Documents and Settings/micsta/Application Data/.arbil/imdicache/"
            },
            {"http://www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/TestStorageDirectory/ArbilWorkingFiles/http/www.mpi.nl/IMDI/Schema/Continents.xml",
                "/Users/testUser/TestStorageDirectory/ArbilWorkingFiles/"
            },
            {"file:/Users/testUser/Library/Mail%20Downloads/MPI-Korpus/Corpusstructure/1.imdi",
                "/Users/testUser/TestStorageDirectory/ArbilWorkingFiles/file/Users/testUser/Library/Mail Downloads/MPI-Korpus/Corpusstructure/1.imdi",
                "/Users/testUser/TestStorageDirectory/ArbilWorkingFiles/"
            },
            {"file:/Z/Documents and Settings/micsta/Application Data/.arbil/imdicache/file/C/Documents and Settings/micsta/Application Data/.arbil/imdicache/file/P/L&C_Assist_Task/Nick Corpus/Arbil_Corpus_structure/arbil_export_03/arbil_export/lac_data/Corpusstructure/l-lao/1.imdi",
                "/Z/Documents and Settings/micsta/Application Data/TestStorageDirectory/ArbilWorkingFiles/file/P/L&C_Assist_Task/Nick Corpus/Arbil_Corpus_structure/arbil_export_03/arbil_export/lac_data/Corpusstructure/l-lao/1.imdi",
                "/Z/Documents and Settings/micsta/Application Data/TestStorageDirectory/ArbilWorkingFiles/"
            },
            {"file:/Z/Documents and Settings/micsta/Application Data/TestStorageDirectory/ArbilWorkingFiles/file/C/Documents and Settings/micsta/Application Data/.arbil/imdicache/file/P/L&C_Assist_Task/Nick Corpus/Arbil_Corpus_structure/arbil_export_03/arbil_export/lac_data/Corpusstructure/l-lao/1.imdi",
                "/Z/Documents and Settings/micsta/Application Data/TestStorageDirectory/ArbilWorkingFiles/file/P/L&C_Assist_Task/Nick Corpus/Arbil_Corpus_structure/arbil_export_03/arbil_export/lac_data/Corpusstructure/l-lao/1.imdi",
                "/Z/Documents and Settings/micsta/Application Data/TestStorageDirectory/ArbilWorkingFiles/"
            }

        };
        ArbilSessionStorage instance = new ArbilSessionStorage();
//        instance.storageDirectory = "/Users/testUser/";
        for (String[] currentTest : testInputArray) {
            instance.changeCacheDirectory(new File(currentTest[2]), false);
            File expResult = new File(currentTest[1]);
            File result = instance.getSaveLocation(currentTest[0]);
            assertEquals(expResult, result);
            if (result.toString().length() > 259) {
                fail("path too long: " + result);
            }
        }
    }
}
