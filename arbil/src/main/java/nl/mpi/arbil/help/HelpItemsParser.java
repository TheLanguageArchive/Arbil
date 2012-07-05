package nl.mpi.arbil.help;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.digester.Digester;
import org.apache.commons.logging.impl.SimpleLog;
import org.xml.sax.SAXException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HelpItemsParser {

    public HelpIndex parse(InputStream helpInputStream) throws IOException, SAXException {
	Digester digester = new Digester();
	digester.addObjectCreate("helpToc", HelpIndex.class);
	digester.addObjectCreate("*/item", HelpItem.class);
	digester.addBeanPropertySetter("*/item/file", "file");
	digester.addBeanPropertySetter("*/item/name", "name");
	digester.addSetNext("*/item", "addSubItem");
	digester.setLogger(new SimpleLog("digester"));
	return (HelpIndex) digester.parse(helpInputStream);
    }
}
