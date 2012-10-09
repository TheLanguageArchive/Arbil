package nl.mpi.arbil.search;

import java.util.List;
import nl.mpi.arbil.data.ArbilNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import nl.mpi.arbil.ArbilTest;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSearchTest extends ArbilTest {

    public static final String NAME_TEST_SESSION_1 = "Test session 1";
    public static final String NAME_TEST_SESSION_2 = "Test session 2";
    public static final String NON_MATCHING_STRING = "alw;nr5aij2423mm";

    @Before
    public void setUp() throws Exception {
	inject();
    }

    /**
     * Tests for matches and non-matches
     * @throws Exception 
     */
    @Test
    public void testSearchMatching() throws Exception {
	// Test empty tree structure
	ArbilSearch search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", "Session");
	assertEquals(0, search.getFoundNodes().size());

	// Add test session
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/test_session_1.imdi");
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/test_session_2.imdi");

	// Search for garbage
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", NON_MATCHING_STRING);
	assertEquals(0, search.getFoundNodes().size());

	// Search for match
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", NAME_TEST_SESSION_1);
	assertEquals(1, search.getFoundNodes().size());
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", NAME_TEST_SESSION_2);
	assertEquals(1, search.getFoundNodes().size());
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", "Test session");
	assertEquals(2, search.getFoundNodes().size());

	// Actor should not contain 'Twan' but not 'session'
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_ACTOR, "", "Test Actor");
	assertEquals(2, search.getFoundNodes().size());	// Actor should not contain 'session'
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_ACTOR, "", "Session");
	assertEquals(0, search.getFoundNodes().size());
    }

    /**
     * Tests field name specification in search term
     * @throws Exception 
     */
    @Test
    public void testSearchFields() throws Exception {
	// Add test session
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/test_session_1.imdi");

	// Name is in node, but not in "Title" field
	ArbilSearch search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "Title", NAME_TEST_SESSION_1);
	assertEquals(0, search.getFoundNodes().size());
	// It is in "Name", so we should get one hit
	search = searchLocalTree(ArbilNodeSearchTerm.NODE_TYPE_SESSION, "Name", NAME_TEST_SESSION_1);
	assertEquals(1, search.getFoundNodes().size());
    }

    /**
     * Tests adding results to table model
     * @throws Exception 
     */
    @Test
    public void testSearchToTableModel() throws Exception {
	AbstractArbilTableModel model = createTableModel();
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/test_session_1.imdi");

	// Search with no results, nothing should be added to table model
	ArbilSearch search = searchLocalTree(model, ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", NON_MATCHING_STRING);
	assertEquals(0, search.getFoundNodes().size());
	assertEquals(0, model.getArbilDataNodeCount());

	// Search with one result, should show in table model!
	search = searchLocalTree(model, ArbilNodeSearchTerm.NODE_TYPE_SESSION, "", NAME_TEST_SESSION_1);
	assertEquals(1, search.getFoundNodes().size());
	assertEquals(1, model.getArbilDataNodeCount());
	ArbilNode node = ((ArbilNode) model.getArbilDataNodes().nextElement());
	assertTrue(node.isSession());
	assertEquals(NAME_TEST_SESSION_1, node.toString());
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

	List<ArbilNodeSearchTerm> terms = Collections.singletonList(nodeSearchTerm);

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
	    public void dataNodeIconCleared(ArbilNode dataNode) {
	    }

	    @Override
	    public void dataNodeRemoved(ArbilNode dataNode) {
	    }

	    @Override
	    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newNode) {
	    }

	    @Override
	    public void requestReloadTableData() {
	    }

	    @Override
	    protected void updateHiddenColumnsLabel(int hiddenColumnCount, int hiddenCellsCount) {
	    }
	};
    }
}
