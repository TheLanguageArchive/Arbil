package nl.mpi.arbil.wicket.model;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import nl.mpi.arbil.data.ArbilDataNode;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 * Provider for ArbilDataNodes
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DataNodeDataProvider extends DetachableArbilDataNodeCollection implements IDataProvider<ArbilDataNode> {

    public DataNodeDataProvider(List<URI> uris) {
	super(uris);
    }

    public Iterator<? extends ArbilDataNode> iterator(int first, int count) {
	return getDataNodes().subList(first, count).listIterator();
    }

    public IModel<ArbilDataNode> model(final ArbilDataNode object) {
	return new DataNodeModel(object);
    }
}
