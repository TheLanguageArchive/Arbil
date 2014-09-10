package nl.mpi.arbil.wicket.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import nl.mpi.arbil.ui.ArbilFieldView;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Table model that can be used as DataProvider for Wicket DataTables, inherits
 * (some) logic from AbstractArbilTableModel. DataNode references get detached, only ArbilFields
 * will be serialized
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTableModel extends AbstractArbilTableModel implements ISortableDataProvider<ArrayList<ArbilTableCell>> {

    private ArbilTableCell[][] data = new ArbilTableCell[0][0];
    private transient Hashtable<String, ArbilDataNode> dataNodeHash;
    private Map<String, URI> dataNodeUrisMap = Collections.synchronizedMap(new HashMap<String, URI>());
    private boolean reloadSuspended = false;
    private boolean reloadRequestedWhileSuspended = false;

    public ArbilWicketTableModel(ArbilFieldView fieldView) {
	super(fieldView);
    }

    public ArbilWicketTableModel() {
	super(new ArbilFieldView());
    }

    // IDataProvider<ArbilDataNode> method implementations
    public Iterator<? extends ArrayList<ArbilTableCell>> iterator(int first, int count) {
	// Create list of rows (over requested range)
	List<ArbilTableCell[]> arrayRows = Arrays.asList(data).subList(first, count);
	// Iterator from this will be used by resulting iterator
	final Iterator<ArbilTableCell[]> arrayRowsIterator = arrayRows.iterator();
	// Wrap into iterator that converts array based rows to row lists
	return new Iterator<ArrayList<ArbilTableCell>>() {

	    public ArrayList<ArbilTableCell> next() {
		// Create ArrayList out of array
		return new ArrayList<ArbilTableCell>(Arrays.asList(arrayRowsIterator.next()));
	    }

	    public boolean hasNext() {
		return arrayRowsIterator.hasNext();
	    }

	    public void remove() {
		arrayRowsIterator.remove();
	    }
	};
    }

    public IModel<ArrayList<ArbilTableCell>> model(ArrayList<ArbilTableCell> object) {
	return new Model<ArrayList<ArbilTableCell>>(object);
    }

    public int size() {
	return data.length;
    }

    public void detach() {
	dataNodeHash = null;
    }

    public synchronized void suspendReload() {
	if (!reloadSuspended) {
	    reloadSuspended = true;
	    reloadRequestedWhileSuspended = false;
	}
    }

    public synchronized void resumeReload() {
	if (reloadSuspended) {
	    reloadSuspended = false;
	    if (reloadRequestedWhileSuspended) {
		requestReloadTableData();
	    }
	}
    }

    // AbstractArbilTableModel method implementations
    @Override
    protected Hashtable<String, ArbilDataNode> getDataNodeHash() {
	loadDataNodeHash();
	return dataNodeHash;
    }

    private void loadDataNodeHash() {
	if (dataNodeHash == null) {
	    dataNodeHash = new Hashtable<String, ArbilDataNode>();
	    for (Entry<String, URI> entry : dataNodeUrisMap.entrySet()) {
		dataNodeHash.put(entry.getKey(), ArbilWicketSession.get().getDataNodeLoader().getArbilDataNode(null, entry.getValue()));
	    }
	}
    }

    @Override
    protected synchronized void putInDataNodeHash(ArbilDataNode arbilDataNode) {
	super.putInDataNodeHash(arbilDataNode);
	// Note: this may be redundant, uri can be constructed from urlstring
	dataNodeUrisMap.put(arbilDataNode.getUrlString(), arbilDataNode.getUri());
    }

    @Override
    protected synchronized void clearDataNodeHash() {
	dataNodeUrisMap.clear();
	// Invalidate dataNodeHash
	detach();
    }

    @Override
    protected synchronized void removeFromDataNodeHash(ArbilDataNode arbilDataNode) {
	super.removeFromDataNodeHash(arbilDataNode);
	dataNodeUrisMap.remove(arbilDataNode.getUrlString());
    }

    @Override
    protected ArbilTableCell[][] getData() {
	return this.data;
    }

    @Override
    protected void setData(ArbilTableCell[][] data) {
	this.data = data;
    }

    @Override
    protected String getRenderedText(ArbilTableCell data) {
	return data.toString();
    }

    @Override
    public void requestReloadTableData() {
	if (reloadSuspended) {
	    synchronized (this) {
		reloadRequestedWhileSuspended = true;
	    }
	} else {
	    // Synchronous table reload
	    reloadTableDataPrivate();
	}
    }

    @Override
    protected void updateHiddenColumnsLabel(int hiddenColumnCount, int hiddenCellsCount) {
	// No such label (at the moment)
    }

    // ArbilDataNodeContainer method implementations
    @Override
    public void dataNodeIconCleared(ArbilNode dataNode) {
	requestReloadTableData();
    }

    @Override
    public void dataNodeRemoved(ArbilNode dataNode) {
	requestReloadTableData();
    }

    @Override
    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newNode) {
    }

    public ISortState getSortState() {
	return sortState;
    }

    public void setSortState(ISortState state) {
	this.sortState = state;
    }
    private ISortState sortState;
}
