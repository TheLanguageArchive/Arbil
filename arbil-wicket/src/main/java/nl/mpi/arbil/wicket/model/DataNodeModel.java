package nl.mpi.arbil.wicket.model;

import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DataNodeModel extends LoadableDetachableModel<ArbilDataNode>{

    private URI uri;
    
    public DataNodeModel(ArbilDataNode dataNode){
	super(dataNode);
	this.uri = dataNode.getURI();
    }
    
    public DataNodeModel(URI uri){
	super();
	this.uri = uri;
    }
    
    @Override
    protected ArbilDataNode load() {
	return ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, uri);
    }
}
