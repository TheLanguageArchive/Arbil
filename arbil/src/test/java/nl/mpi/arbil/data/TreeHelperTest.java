package nl.mpi.arbil.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import org.junit.BeforeClass;
import java.io.File;
import java.io.FileOutputStream;
import nl.mpi.arbil.MockSessionStorage;
import nl.mpi.arbil.MockBugCatcher;
import nl.mpi.arbil.MockDialogHandler;
import java.io.IOException;
import java.io.OutputStreamWriter;
import nl.mpi.arbil.ArbilTestInjector;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class TreeHelperTest {

    @BeforeClass
    public static void inject() throws IOException {
	MockBugCatcher bc = new MockBugCatcher();
	MockDialogHandler dh = new MockDialogHandler();

	ArbilTestInjector.injectHandlers(new TestSessionStorage(), bc, dh, null, null);
    }

    @Test
    public void testLocationsList() throws Exception {
	// This will create the treehelper and load locations in the process
	TreeHelper th = TreeHelper.getSingleInstance();
	assertEquals(0, th.localCorpusNodes.length);
	th.addLocation(TreeHelperTest.class.getResource("/nl/mpi/arbil/data/testfiles/\u0131md\u0131test.imdi").toURI());
	for (ArbilDataNode node : th.localCorpusNodes) {
	    node.waitTillLoaded();
	}
	assertEquals(1, th.localCorpusNodes.length);
	assertFalse(th.localCorpusNodes[0].fileNotFound);
    }

    private static class TestSessionStorage extends MockSessionStorage {
	@Override
	public final String[] loadStringArray(String filename) throws IOException {
	    File currentConfigFile = new File(getStorageDirectory(), filename + ".config");
	    if (currentConfigFile.exists()) {
		ArrayList<String> stringArrayList = new ArrayList<String>();
		FileInputStream fstream = new FileInputStream(currentConfigFile);
		DataInputStream in = new DataInputStream(fstream);
		try {
		    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));
		    try {
			String strLine;
			while ((strLine = br.readLine()) != null) {
			    stringArrayList.add(strLine);
			}
		    } finally {
			br.close();
		    }
		} finally {
		    in.close();
		    fstream.close();
		}
		return stringArrayList.toArray(new String[]{});
	    }
	    return null;
	}

	@Override
	public final void saveStringArray(String filename, String[] storableValue) throws IOException {
	    // save the location list to a text file that admin-users can read and hand edit if they really want to
	    File destinationConfigFile = new File(getStorageDirectory(), filename + ".config");
	    File tempConfigFile = new File(getStorageDirectory(), filename + ".config.tmp");

	    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempConfigFile),"UTF8"));
	    for (String currentString : storableValue) {
		out.write(currentString + "\r\n");
	    }
	    out.close();
	    destinationConfigFile.delete();
	    tempConfigFile.renameTo(destinationConfigFile);
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
