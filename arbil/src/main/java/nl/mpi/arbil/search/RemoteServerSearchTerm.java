package nl.mpi.arbil.search;

import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface RemoteServerSearchTerm {

    static final String IMDI_RESULT_URL_XPATH = "/ImdiSearchResponse/Result/Match/URL";
    static final String IMDI_SEARCH_BASE = "http://corpus1.mpi.nl/ds/imdi_search/servlet?action=getMatches";
    static final String valueFieldMessage = "<remote server search term (required)>";
    
    URI[] getServerSearchResults(ArbilDataNode[] arbilDataNodeArray);
    
}
