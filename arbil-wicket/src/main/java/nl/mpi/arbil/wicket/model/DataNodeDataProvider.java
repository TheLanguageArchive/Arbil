package nl.mpi.arbil.wicket.model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import nl.mpi.arbil.data.ArbilDataNode;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DataNodeDataProvider implements IDataProvider<ArbilDataNode> {

    private List<ArbilDataNode> dataNodes;

    public DataNodeDataProvider(ArbilDataNode[] dataNodes) {
	this.dataNodes = Arrays.asList(dataNodes);
    }

    public Iterator<? extends ArbilDataNode> iterator(int first, int count) {
	return dataNodes.subList(first, count).listIterator();
    }

    public int size() {
	return dataNodes.size();
    }

    public IModel<ArbilDataNode> model(final ArbilDataNode object) {
	return new DataNodeModel(object);
    }    
    
    public List<ArbilDataNode> getDataNodes() {
	return dataNodes;
    }

    public void detach() {
    }
}
