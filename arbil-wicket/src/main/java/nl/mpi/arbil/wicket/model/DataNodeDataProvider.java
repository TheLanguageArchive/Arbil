package nl.mpi.arbil.wicket.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 * Provider for ArbilDataNodes. Gets nodes on basis of URIs
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DataNodeDataProvider implements IDataProvider<ArbilDataNode> {

    /**
     * URIs that represent data nodes
     */
    private List<URI> URIs;
    /**
     * Transient storage of actual data nodes
     */
    private transient List<ArbilDataNode> dataNodes;

    public DataNodeDataProvider(List<URI> uris) {
	this.URIs = uris;
    }

    public DataNodeDataProvider(ArbilDataNode[] dataNodes) {
	this.dataNodes = Arrays.asList(dataNodes);
	URIs = URIsFromNodes(this.dataNodes);
    }

    public Iterator<? extends ArbilDataNode> iterator(int first, int count) {
	ensureNodesLoaded();
	return dataNodes.subList(first, count).listIterator();
    }

    public int size() {
	return URIs.size();
    }

    public IModel<ArbilDataNode> model(final ArbilDataNode object) {
	return new DataNodeModel(object);
    }

    public void detach() {
	// Data nodes are not serializable, leave no reference
	dataNodes = null;
    }

    public List<ArbilDataNode> getDataNodes() {
	ensureNodesLoaded();
	return dataNodes;
    }

    private void ensureNodesLoaded() {
	synchronized (this) {
	    if (dataNodes == null) {
		dataNodes = nodesFromURIs(URIs);
	    }
	}
    }

    public static List<URI> URIsFromNodes(List<ArbilDataNode> nodes) {
	// Copy to immutable because transform returns lazy loaded list, we don't want this
	return ImmutableList.copyOf(Lists.transform(nodes, new Function<ArbilDataNode, URI>() {

	    public URI apply(ArbilDataNode f) {
		return f.getURI();
	    }
	}));
    }

    public static List<ArbilDataNode> nodesFromURIs(List<URI> nodeURIs) {
	// Copy to immutable because transform returns lazy loaded list, we don't want this
	return ImmutableList.copyOf(Lists.transform(nodeURIs, new Function<URI, ArbilDataNode>() {

	    public ArbilDataNode apply(URI f) {
		return ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, f);
	    }
	}));
    }
}
