package nl.mpi.arbil.help;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HelpItem extends HelpIndex {

    private String file;
    private String name;

    public String getFile() {
	return file;
    }

    public void setFile(String file) {
	this.file = file;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }
}
