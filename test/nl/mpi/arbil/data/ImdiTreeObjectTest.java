package nl.mpi.arbil.data;

import nl.mpi.arbil.MetadataFile.MetadataReaderTest;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
        String[][] testStringArray = {
//            {
//                "C:\\Documents and Settings\\user\\Application Data\\.arbil\\imdicache\\http\\corpus1.mpi.nl\\qfs1\\media-archive\\lac_data\\Corpusstructure\\southeastasia.imdi",
//                "file:/C/Documents%20and%20Settings/user/Application%20Data/.arbil/imdicache/http/corpus1.mpi.nl/qfs1/media-archive/lac_data/Corpusstructure/southeastasia.imdi",
//            },
            {
                "file:/C:/Documents and Settings/user/Application Data/.arbil/imdicache/http/corpus1.mpi.nl/qfs1/media-archive/lac_data/Corpusstructure/southeastasia.imdi",
                "file:/C:/Documents%20and%20Settings/user/Application%20Data/.arbil/imdicache/http/corpus1.mpi.nl/qfs1/media-archive/lac_data/Corpusstructure/southeastasia.imdi"
            }, {
                "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Beaver/Corpusstructure/Beaver.imdi",
                "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Beaver/Corpusstructure/Beaver.imdi"
            }, {
                "https://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Beaver/Corpusstructure/Beaver.imdi",
                "https://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Beaver/Corpusstructure/Beaver.imdi"
            }, {
                "file:/Users/petwit/Library/Mail Downloads/arbil_export/20100201175544/20100201175545/20100201180205/20100201183806/20100201183822/20100202184429/20100202184443.imdi",
                "file:/Users/petwit/Library/Mail%20Downloads/arbil_export/20100201175544/20100201175545/20100201180205/20100201183806/20100201183822/20100202184429/20100202184443.imdi"
            }
        };
        for (String testString[] : testStringArray) {
            URI expResult = URI.create(testString[1]);
            URI result = ImdiTreeObject.conformStringToUrl(testString[0]);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getNeedsSaveToDisk method, of class ImdiTreeObject.
     */
    @Test
    public void testGetNeedsSaveToDisk() {
        System.out.println("getNeedsSaveToDisk");
        URI[][] testFileUris = null;
        try {
            testFileUris = new URI[][]{
                        {MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/catalogue.imdi").toURI(),
                            new URI(MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/catalogue.imdi").toString() + "#.METATRANSCRIPT.Catalogue.Location(1)")},
                        {MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/corpus.imdi").toURI(),
                            MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/corpus.imdi").toURI()},
                        {MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/session-with-actors.imdi").toURI(),
                            new URI(MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/session-with-actors.imdi").toString() + "#.METATRANSCRIPT.Session.MDGroup.Actors.Actor(2)")},
                        {MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/session-with-actors.imdi").toURI(),
                            new URI(MetadataReaderTest.class.getResource("/nl/mpi/arbil/data/testfiles/session-with-actors.imdi").toString() + "#.METATRANSCRIPT.Session.MDGroup.Actors.Actor(2).Languages.Language(3)")}
                    };
        } catch (URISyntaxException urise) {
            fail(urise.getMessage());
        }
        for (URI[] testCaseUri : testFileUris) {
            System.out.println("testCaseUri[1]: " + testCaseUri[1]);
            ImdiTreeObject instance = ImdiLoader.getSingleInstance().getImdiObject(null, testCaseUri[0]);
            ImdiTreeObject instanceChild = ImdiLoader.getSingleInstance().getImdiObject(null, testCaseUri[1]);
            instance.waitTillLoaded();
            assertFalse(instance.isLoading());
            // start modifying values
            ImdiField[] firstRootField = instance.getFields().elements().nextElement();
            ImdiField[] firstChildField = instanceChild.getFields().elements().nextElement();
            for (ImdiField[] currentTestField : new ImdiField[][]{firstRootField, firstChildField}) {
                // check that it does not need to be saved yet
                assertFalse(instance.getUrlString(), instance.getNeedsSaveToDisk());
                assertFalse(instanceChild.getUrlString(), instanceChild.getNeedsSaveToDisk());
                // value test
                currentTestField[0].setFieldValue("test", false, true);
                // check that it now needs to be saved
                assertTrue(instance.getUrlString(), instance.getNeedsSaveToDisk());
                if (currentTestField[0].equals(firstChildField[0])) {
                    assertTrue(instanceChild.getUrlString(), instanceChild.getNeedsSaveToDisk());
                } else {
                    assertFalse(instanceChild.getUrlString(), instanceChild.getNeedsSaveToDisk());
                }
                // note that the field change triggers can leave changes that require saving for nodes such as language id
                // the test case .Actors.Actor(2).Languages.Language(3) for instance will cause this issue if the second field is reverted
                currentTestField[0].revertChanges();
                // check that it does not need to be saved again
                assertFalse(instance.getUrlString(), instance.getNeedsSaveToDisk());
                assertFalse(instanceChild.getUrlString(), instanceChild.getNeedsSaveToDisk());
                // note that this does not check the state change for language id and key name changes
            }
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
            instance.resourceUrlField = new ImdiField(-1, instance, "test-sub-xml-path", testString[1], -1);
            // the UNC (////) path need to be retained
            URI expResult = URI.create(testString[2]);
            URI result = instance.getFullResourceURI();
            assertEquals(expResult, result);
        }
    }

    /**
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
}

