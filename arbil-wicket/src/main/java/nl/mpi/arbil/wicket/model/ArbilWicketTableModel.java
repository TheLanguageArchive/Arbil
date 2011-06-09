package nl.mpi.arbil.wicket.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import nl.mpi.arbil.ui.ArbilFieldView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 * Table model that can be used as DataProvider for Wicket DataTables, inherits
 * (some) logic from AbstractArbilTableModel. DataNode references get detached, only ArbilFields
 * will be serialized
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTableModel extends AbstractArbilTableModel implements IDataProvider<ArbilDataNode> {

    private Object[][] data = new Object[0][0];
    private transient Hashtable<String,ArbilDataNode> dataNodeHash;
    private HashMap<String, URI> dataNodeUrisMap = new HashMap<String, URI>();
    
    public ArbilWicketTableModel(ArbilFieldView fieldView) {
	super(fieldView);
    }

    public ArbilWicketTableModel() {
	super(new ArbilFieldView());
    }

    // IDataProvider<ArbilDataNode> method implementations
    public Iterator<? extends ArbilDataNode> iterator(int first, int count) {
	return getDataNodeHash().values().iterator();
    }

    public int size() {
	return dataNodeUrisMap.size();
    }

    public IModel<ArbilDataNode> model(ArbilDataNode object) {
	return new DataNodeModel(object);
    }

    public void detach() {
	dataNodeHash = null;
    }

    // AbstractArbilTableModel method implementations
    @Override
    protected Hashtable<String, ArbilDataNode> getDataNodeHash() {
	loadDataNodeHash();
	return dataNodeHash;
    }

    private void loadDataNodeHash() {
	if(dataNodeHash == null){
	    dataNodeHash = new Hashtable<String, ArbilDataNode>();
	    for(Entry<String,URI> entry : dataNodeUrisMap.entrySet()){
		dataNodeHash.put(entry.getKey(), ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, entry.getValue()));
	    }
	}
    }
    
    @Override
    protected synchronized void putInDataNodeHash(ArbilDataNode arbilDataNode) {
	super.putInDataNodeHash(arbilDataNode);
	// Note: this may be redundant, uri can be constructed from urlstring
	dataNodeUrisMap.put(arbilDataNode.getUrlString(), arbilDataNode.getURI());
    }

    @Override
    protected synchronized void clearDataNodeHash() {
	dataNodeUrisMap.clear();
	// Invalidate dataNodeHash
	detach();
    }

    @Override
    protected void removeFromDataNodeHash(ArbilDataNode arbilDataNode) {
	super.removeFromDataNodeHash(arbilDataNode);
	dataNodeUrisMap.remove(arbilDataNode.getUrlString());
    }

    @Override
    protected Object[][] getData() {
	return this.data;
    }

    @Override
    protected void setData(Object[][] data) {
	this.data = data;
    }

    @Override
    protected String getRenderedText(Object data) {
	return data.toString();
    }

    @Override
    public void requestReloadTableData() {
	// Synchronous table reload
	reloadTableDataPrivate();
    }

    @Override
    protected void updateHiddenColumnsLabel(int hiddenColumnCount) {
	// No such label (at the moment)
    }

    // ArbilDataNodeContainer method implementations
    @Override
    public void dataNodeIconCleared(ArbilDataNode dataNode) {
	requestReloadTableData();
    }

    @Override
    public void dataNodeRemoved(ArbilDataNode dataNode) {
	requestReloadTableData();
    }
}
