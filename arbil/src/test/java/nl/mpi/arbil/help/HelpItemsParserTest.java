package nl.mpi.arbil.help;

import nl.mpi.arbil.help.HelpIndex;
import nl.mpi.arbil.help.HelpItemsParser;
import nl.mpi.arbil.help.HelpItem;
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
