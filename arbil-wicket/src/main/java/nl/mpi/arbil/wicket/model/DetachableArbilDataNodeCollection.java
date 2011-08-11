package nl.mpi.arbil.wicket.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import org.apache.wicket.model.IDetachable;

/**
 * Base class that keeps a list of URI's and attaches/detaches ArbilDataNodes
 * on basis of these. Use to prevent wicket from trying to serialize (collections
 * of ArbilDataNodes)
 * @see ArbilDataNode
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DetachableArbilDataNodeCollection implements Serializable, IDetachable, Collection<ArbilDataNode> {

    /**
     * URIs that represent data nodes
     */
    private List<URI> URIs;
    /**
     * Transient storage of actual data nodes
     */
    private transient List<ArbilDataNode> dataNodes;

    public DetachableArbilDataNodeCollection(List<URI> uris) {
	this.URIs = uris;
    }

    public DetachableArbilDataNodeCollection(ArbilDataNode[] dataNodes) {
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
		return ArbilWicketSession.get().getDataNodeLoader().getArbilDataNode(null, f);
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

    public int size() {
	return getURIs().size();
    }

    public boolean isEmpty() {
	return getURIs().isEmpty();
    }

    public boolean contains(Object o) {
	if (o instanceof ArbilDataNode) {
	    return getURIs().contains(((ArbilDataNode) o).getURI());
	} else if (o instanceof URI) {
	    return getURIs().contains((URI) o);
	} else {
	    return false;
	}
    }

    public Iterator<ArbilDataNode> iterator() {
	return getDataNodes().iterator();
    }

    public Object[] toArray() {
	return getDataNodes().toArray();
    }

    public <T> T[] toArray(T[] a) {
	return getDataNodes().toArray(a);
    }

    public boolean add(ArbilDataNode e) {
	if (getURIs().add(e.getURI())) {
	    // Invalidate dataNodes
	    detach();
	    return true;
	} else {
	    return false;
	}
    }

    public boolean remove(Object o) {
	boolean result = false;
	if (o instanceof ArbilDataNode) {
	    result = getURIs().remove(((ArbilDataNode) o).getURI());
	} else if (o instanceof URI) {
	    result = getURIs().remove((URI) o);
	}
	if (result) {
	    // Invalidate dataNodes
	    detach();
	}
	return result;
    }

    public boolean containsAll(Collection<?> c) {
	return getDataNodes().containsAll(c);
    }

    public boolean addAll(Collection<? extends ArbilDataNode> c) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeAll(Collection<?> c) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean retainAll(Collection<?> c) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
	URIs.clear();
	// Invalidate dataNodes
	detach();
    }
}
