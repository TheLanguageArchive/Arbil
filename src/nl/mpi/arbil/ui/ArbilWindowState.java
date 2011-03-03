package nl.mpi.arbil.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;
import java.util.Vector;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWindowState implements Serializable {
    public Dimension size;
    public Point location;
    public Vector currentNodes;
    public ArbilWindowType windowType = ArbilWindowType.nodeTable;

    public enum ArbilWindowType{
        nodeTable,
        subnodesPanel
    }
}
