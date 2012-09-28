package nl.mpi.arbil.help;

import java.io.InputStream;
import junit.framework.TestCase;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.kinnate.plugin.AbstractBasePlugin;

/**
 * Created on : Sep 27, 2012, 11:30
 *
 * @author Peter Withers
 */
public class ArbilHelpResourcesTest extends TestCase {

    /**
     * Test of getArtifactVersion method, of class AbstractBasePlugin.
     */
    public void testGetArtifactVersion() {
	System.out.println("getArtifactVersion");
	try {
	    AbstractBasePlugin abstractBasePlugin = new ArbilHelpResources();
	    assertTrue(abstractBasePlugin.isMavenVersionCorrect());
	} catch (PluginException exception) {
	    fail(exception.getMessage());
	}
    }

    public void testIndexResourcesImdi() throws Exception {
	InputStream resourceAsStream = getClass().getResourceAsStream(ArbilHelpResources.IMDI_HELP_INDEX_XML);
	assertNotNull("Expected IMDI index resource at " + ArbilHelpResources.IMDI_HELP_INDEX_XML, resourceAsStream);
    }

    public void testIndexResourcesCmdi() throws Exception {
	InputStream resourceAsStream = getClass().getResourceAsStream(ArbilHelpResources.CMDI_HELP_INDEX_XML);
	assertNotNull("Expected CMDI index resource at " + ArbilHelpResources.CMDI_HELP_INDEX_XML, resourceAsStream);
    }
}
