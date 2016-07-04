/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import org.junit.Before;
import nl.mpi.arbil.ArbilTest;
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
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class TreeHelperTest extends ArbilTest {

    private MockSessionStorage sessionStorage;

    @Before
    public void setUp() throws Exception {
	inject();
    }

    @After
    @Override
    public void cleanUp() {
	super.cleanUp();
	if (sessionStorage != null) {
	    sessionStorage.cleanUp();
	}
    }

    @Test
    public void testLocationsList() throws Exception {
	assertEquals(0, getTreeHelper().getLocalCorpusNodes().length);
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/\u0131md\u0131test.imdi");
	assertEquals(1, getTreeHelper().getLocalCorpusNodes().length);
	assertFalse(((ArbilDataNode) getTreeHelper().getLocalCorpusNodes()[0]).fileNotFound);
    }

    @Override
    protected MockSessionStorage newSessionStorage() {
	if (sessionStorage != null) {
	    sessionStorage.cleanUp();
	}
	return sessionStorage = new MockSessionStorage() {
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
