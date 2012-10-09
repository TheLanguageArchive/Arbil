package nl.mpi.arbil.data;

import java.io.Serializable;

/**
 * Serializable container for arbil table cells
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilTableCell<T> extends Serializable {

    /**
     * @return the content
     */
    T getContent();

    /**
     * @param content the content to set
     */
    void setContent(T content);
    
}
