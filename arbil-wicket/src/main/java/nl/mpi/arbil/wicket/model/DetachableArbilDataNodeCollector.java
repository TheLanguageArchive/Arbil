package nl.mpi.arbil.wicket.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;

/**
 * Base class that keeps a list of URI's and attaches/detaches ArbilDataNodes
 * on basis of these. Use to prevent wicket from trying to serialize (collections
 * of ArbilDataNodes)
 * @see ArbilDataNode
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class DetachableArbilDataNodeCollector implements Serializable {

    /**
     * URIs that represent data nodes
     */
    private List<URI> URIs;
    /**
     * Transient storage of actual data nodes
     */
    private transient List<ArbilDataNode> dataNodes;

    public DetachableArbilDataNodeCollector(List<URI> uris) {
	this.URIs = uris;
    }

    public DetachableArbilDataNodeCollector(ArbilDataNode[] dataNodes) {
	this.dataNodes = Arrays.asList(dataNodes);
	URIs = URIsFromNodes(this.dataNodes);
    }

    public void detach() {
	// Data nodes are not serializable, leave no reference
	dataNodes = null;
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
    
    /**
     * @return the URIs
     */
    protected List<URI> getURIs() {
	return URIs;
    }

    /**
     * @return the dataNodes (will be attached if not already)
     */
    public synchronized List<ArbilDataNode> getDataNodes() {
	if (dataNodes == null) {
	    dataNodes = nodesFromURIs(URIs);
	}
	return dataNodes;
    }
}
