/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.help;

import java.io.InputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HelpItemsParserTest {

    /**
     * Test of parse method, of class HelpItemsParser.
     */
    @Test
    public void testParse() throws Exception {
	HelpItemsParser parser = new HelpItemsParser();
	InputStream is = getClass().getResourceAsStream("/nl/mpi/arbil/resources/html/help/arbil.xml");
	HelpIndex result = parser.parse(is);
	assertEquals(2, result.getSubItems().size());

	HelpItem child1 = result.getSubItems().get(0);

	assertEquals("First name", child1.getName());
	assertEquals("FirstFile.html", child1.getFile());
	assertEquals(2, child1.getSubItems().size());

	HelpItem child2 = result.getSubItems().get(1);
	assertEquals("Second name", child2.getName());
	assertEquals("SecondFile.html", child2.getFile());
	assertEquals(0, child2.getSubItems().size());
    }
}
