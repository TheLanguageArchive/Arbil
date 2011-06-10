package nl.mpi.arbil.data;

/**
 * Generic implementation for ArbilTableCell
 * @author Twan Goosen <twan.goosen@mpi.nl>
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
}
