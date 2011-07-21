package nl.mpi.arbil.data;

import java.util.Hashtable;
import java.util.List;

/**
 * ArbilNode that provides fields that can be displayed in table views
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * 
 * @see nl.mpi.arbil.ui.ArbilTable
 * @see nl.mpi.arbil.ui.AbstractArbilTableModel
 */
public interface ArbilFieldsNode extends ArbilNode {
    Hashtable<String, ArbilField[]> getFields();
    List<ArbilField[]> getFieldsSorted();

    boolean hasChangedFields();
    boolean hasChangedFieldsInSubtree();
    
    String getHashKey();
}
