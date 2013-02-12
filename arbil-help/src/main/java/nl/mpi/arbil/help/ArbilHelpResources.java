package nl.mpi.arbil.help;

import nl.mpi.flap.module.AbstractBaseModule;
import nl.mpi.flap.plugin.PluginException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilHelpResources extends AbstractBaseModule {

    public final static String IMDI_HELP_RESOURCE_BASE = "/nl/mpi/arbil/resources/html/help/arbil-imdi/";
    public final static String IMDI_HELP_INDEX_XML = IMDI_HELP_RESOURCE_BASE + "arbil-imdi.xml";
    public final static String CMDI_HELP_RESOURCE_BASE = "/nl/mpi/arbil/resources/html/help/arbil-cmdi/";
    public final static String CMDI_HELP_INDEX_XML = CMDI_HELP_RESOURCE_BASE + "arbil-cmdi.xml";
    public static final String IMDI_URL_PATTERN = "arbil-imdi";
    public static final String CMDI_URL_PATTERN = "arbil-cmdi";

    public ArbilHelpResources() throws PluginException {
	super("ArbilHelp", "Package containing the Arbil help resource sets for both IMDI and CMDI. Not a plugin.", "nl.mpi.arbil.help");
    }
}
