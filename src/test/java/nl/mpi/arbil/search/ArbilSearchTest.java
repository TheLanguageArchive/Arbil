package nl.mpi.arbil.search;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import nl.mpi.arbil.ArbilTest;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import nl.mpi.arbil.util.TreeHelper;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSearchTest extends ArbilTest {

    @Before
    public void setUp() throws Exception {
	inject();
    }

    @Test
    public void testSearchMatching() throws Exception {
	// Test empty tree structure
	ArbilSearch search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", "Session");
	assertEquals(0, search.getFoundNodes().size());

	// Add test session
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/test_session_1.imdi");
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/test_session_2.imdi");

	// Search for garbage
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", "alw;nr5aij2423mm");
	assertEquals(0, search.getFoundNodes().size());

	// Search for match
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", "Test session");
	assertEquals(2, search.getFoundNodes().size());
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", "Test session 1");
	assertEquals(1, search.getFoundNodes().size());
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", "Test session 2");
	assertEquals(1, search.getFoundNodes().size());

	// Actor should not contain 'Twan' but not 'session'
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_ACTOR, "", "Test Actor");
	assertEquals(2, search.getFoundNodes().size());	// Actor should not contain 'session'
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_ACTOR, "", "Session");
	assertEquals(0, search.getFoundNodes().size());
    }

    @Test
    public void testSearchFields() throws Exception {
	// Add test session
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/test_session_1.imdi");

	ArbilSearch search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "Name", "Test session title");
	assertEquals(0, search.getFoundNodes().size());
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "Title", "Test session title");
	assertEquals(1, search.getFoundNodes().size());
    }

    @Test
    public void testSearchToTableModel() throws Exception {
	AbstractArbilTableModel model = createTableModel();
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/test_session_1.imdi");
	ArbilSearch search = searchLocalTree(model, ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", "ataretw45w45");
	assertEquals(0, search.getFoundNodes().size());
	assertEquals(0, model.getRowCount());
	search = searchLocalTree(model, ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", "Test session 1");
	assertEquals(1, search.getFoundNodes().size());
	assertEquals(1, model.getArbilDataNodeCount());
    }

    private ArbilSearch searchLocalTree(String nodeType, String field, String searchString) {
	return searchLocalTree(createTableModel(), nodeType, field, searchString);
    }

    private ArbilSearch searchLocalTree(AbstractArbilTableModel model, String nodeType, String field, String searchString) {

	ArbilNodeSearchTerm nodeSearchTerm = new ArbilSimpleNodeSearchTerm();
	nodeSearchTerm.setBooleanAnd(true);
	nodeSearchTerm.setNodeType(nodeType);
	nodeSearchTerm.setSearchFieldName(field);
	nodeSearchTerm.setSearchString(searchString);

	Collection<ArbilNodeSearchTerm> terms = Collections.singleton(nodeSearchTerm);

	ArbilSearch search = new ArbilSearch(Arrays.asList(getTreeHelper().getLocalCorpusNodes()), terms, null, model);
	executeLocalSearch(search);
	return search;
    }

    private void executeLocalSearch(ArbilSearch search) {
	search.splitLocalRemote();
	search.searchLocalNodes();
    }

    /**
     * 
     * @return Mock table model
     */
    private AbstractArbilTableModel createTableModel() {
	return new AbstractArbilTableModel() {

	    private Hashtable<String, ArbilDataNode> hash = new Hashtable<String, ArbilDataNode>();
	    private ArbilTableCell[][] tableData = new ArbilTableCell[0][0];

	    @Override
	    protected Hashtable<String, ArbilDataNode> getDataNodeHash() {
		return hash;
	    }

	    @Override
	    protected ArbilTableCell[][] getData() {
		return tableData;
	    }

	    @Override
	    protected void setData(ArbilTableCell[][] data) {
		tableData = data;
	    }

	    @Override
	    protected String getRenderedText(ArbilTableCell data) {
		return data.toString();
	    }

	    @Override
	    public void dataNodeIconCleared(ArbilDataNode dataNode) {
	    }

	    @Override
	    public void dataNodeRemoved(ArbilDataNode dataNode) {
	    }

	    @Override
	    public void requestReloadTableData() {
	    }

	    @Override
	    protected void updateHiddenColumnsLabel(int hiddenColumnCount) {
	    }
	};
    }
}
