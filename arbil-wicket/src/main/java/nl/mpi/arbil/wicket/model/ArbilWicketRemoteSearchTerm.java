package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.search.ArbilRemoteSearch;
import nl.mpi.arbil.search.RemoteServerSearchTerm;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketRemoteSearchTerm implements RemoteServerSearchTerm, Serializable {

    private String searchString;

    public ArbilWicketRemoteSearchTerm() {
    }

    public ArbilWicketRemoteSearchTerm(String searchString) {
	this.searchString = searchString;
    }

    public RemoteServerSearchTerm getRemoteServerSearchTerm() {
	if (!(null == searchString || "".equals(searchString))) {
	    return this;
	} else {
	    return null;
	}
    }

    public URI[] getServerSearchResults(ArbilDataNode[] searchNodes) {
	return new ArbilRemoteSearch().getServerSearchResults(searchString, searchNodes);
    }
}
