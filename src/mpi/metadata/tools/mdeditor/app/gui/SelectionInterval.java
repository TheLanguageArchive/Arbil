package mpi.metadata.tools.mdeditor.app.gui;

/**
 * Defines a selection interval.
 *
 * @author donwi
 * @version 1.0
 */
public class SelectionInterval {
    /** The start position of the selection. */
    public int start;

    /** The end position of the selection. */
    public int end;

    /**
     * Creates a new <CODE>SelectionInterval</CODE>.
     *
     * @param start The start position of the selection.
     * @param end The end position of the selection.
     */
    public SelectionInterval(int start, int end) {
        this.start = start;
        this.end = end;
    }
}
