package nl.mpi.arbil.data;

/**
 * Generic implementation for ArbilTableCell
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @see ArbilDataNodeTableCell
 * @see ArbilDataNodeArrayTableCell
 */
public class DefaultArbilTableCell<T> implements ArbilTableCell<T> {

    private T content;

    public DefaultArbilTableCell(T content) {
	this.content = content;
    }

    /**
     * @return the content
     */
    @Override
    public T getContent() {
	return content;
    }

    /**
     * @param content the content to set
     */
    @Override
    public void setContent(T content) {
	this.content = content;
    }

    @Override
    public String toString() {
	// Contents of types ArbilDataNode and ArbilDataNode[] should be in their respective implementations of ArbilTableCell
	 if (content instanceof ArbilField[]) {
	    return "<multiple values>";
	} else if (content instanceof ArbilField && ((ArbilField) content).isRequiredField() && ((ArbilField) content).toString().length() == 0) {
	    //super.setForeground(Color.RED);
	    return "<required field>";
	} else if (content instanceof ArbilField && !((ArbilField) content).fieldValueValidates()) {
	    //super.setForeground(Color.RED);
	    return content.toString();
	} else {
	    return content.toString();
	}
    }
}
