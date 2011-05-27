package nl.mpi.arbil.data;

import org.junit.BeforeClass;
import nl.mpi.arbil.userstorage.SessionStorage;
import java.io.File;
import nl.mpi.arbil.MockSessionStorage;
import nl.mpi.arbil.MockBugCatcher;
import nl.mpi.arbil.MockDialogHandler;
import java.io.IOException;
import nl.mpi.arbil.TestInjector;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class TreeHelperTest {

    @BeforeClass
    public static void inject() {
	MockBugCatcher bc = new MockBugCatcher();
	MockDialogHandler dh = new MockDialogHandler();

	TestInjector.injectHandlers(testSessionStorage, bc, dh, null, null);
    }

    @Test
    public void testConformStringToUrl() throws Exception {
	// This will create the treehelper and load locations in the process
	TreeHelper th = TreeHelper.getSingleInstance();
	assertEquals(1, th.localCorpusNodes.length);
	for (ArbilDataNode node : th.localCorpusNodes) {
	    node.waitTillLoaded();
	}
    }
    private static SessionStorage testSessionStorage = new MockSessionStorage() {

	@Override
	public String[] loadStringArray(String filename) throws IOException {
	    if ("locationsList".equals(filename)) {
		return new String[]{
			    // Filename with non-ASCII characters
			    TreeHelperTest.class.getResource("/nl/mpi/arbil/data/testfiles/ımdıtest.imdi").toExternalForm()
			};
	    } else {
		return null;
	    }
	}

	@Override
	public boolean pathIsInsideCache(File fullTestFile) {
	    return true;
	}

	@Override
	public boolean pathIsInFavourites(File fullTestFile) {
	    return false;
	}
    };
}
