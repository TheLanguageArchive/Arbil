package nl.mpi.arbil.data;

import nl.mpi.arbil.userstorage.SessionStorage;
import org.junit.Before;
import nl.mpi.arbil.ArbilTest;
import nl.mpi.arbil.util.TreeHelper;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import nl.mpi.arbil.MockSessionStorage;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class TreeHelperTest extends ArbilTest {

    @Before
    public void setUp() throws Exception {
        inject();
    }

    @Test
    public void testLocationsList() throws Exception {
        assertEquals(0, getTreeHelper().getLocalCorpusNodes().length);
        addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/\u0131md\u0131test.imdi");
        assertEquals(1, getTreeHelper().getLocalCorpusNodes().length);
        assertFalse(((ArbilDataNode) getTreeHelper().getLocalCorpusNodes()[0]).fileNotFound);
    }

    @Override
    protected SessionStorage newSessionStorage() {
        return new MockSessionStorage() {
            @Override
            public final String[] loadStringArray(String filename) throws IOException {
                File currentConfigFile = new File(getApplicationSettingsDirectory(), filename + ".config");
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
                File destinationConfigFile = new File(getApplicationSettingsDirectory(), filename + ".config");
                File tempConfigFile = new File(getApplicationSettingsDirectory(), filename + ".config.tmp");

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempConfigFile), "UTF8"));
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
}
