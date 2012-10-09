package nl.mpi.arbil.search;

import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 * Interface for remote server search term, to be used with MDSearch service
 * Results will need further processing. Filtering can be done by supplying ArbilNodeSearchTerms
 * @see nl.mpi.arbil.search.ArbilSearch
 * @see nl.mpi.arbil.search.ArbilNodeSearchTerm
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface RemoteServerSearchTerm {

    static final String IMDI_RESULT_URL_XPATH = "/ImdiSearchResponse/Result/Match/URL";
    static final String IMDI_SEARCH_BASE = "http://corpus1.mpi.nl/ds/imdi_search/servlet?action=getMatches";
    static final String valueFieldMessage = "<remote server search term (required)>";
    
    /**
     * 
     * @param searchNodes Nodes to search in
     * @return Results of the MDSearch call for the given nodes
     */
    URI[] getServerSearchResults(ArbilDataNode[] searchNodes);
    
}
