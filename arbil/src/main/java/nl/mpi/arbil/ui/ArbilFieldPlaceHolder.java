package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilDataNode;

/**
 * Used as blank in horizontal tables (grid view) in cells that represent
 * fields that are not present in the node represented by their row, i.e. the
 * 'grey fields'.
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFieldPlaceHolder {

    private String fieldName;
    private ArbilDataNode arbilDataNode;

    public ArbilFieldPlaceHolder(String fieldName, ArbilDataNode dataNode) {
        this.fieldName = fieldName;
        this.arbilDataNode = dataNode;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ArbilDataNode getArbilDataNode() {
        return arbilDataNode;
    }

    @Override
    public String toString() {
        return "";
    }
}
