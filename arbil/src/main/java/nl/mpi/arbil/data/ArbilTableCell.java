package nl.mpi.arbil.data;

import java.io.Serializable;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTableCell<T> implements Serializable {

    private T content;

    public ArbilTableCell(T content) {
	this.content = content;
    }

    /**
     * @return the content
     */
    public T getContent() {
	return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(T content) {
	this.content = content;
    }
}
