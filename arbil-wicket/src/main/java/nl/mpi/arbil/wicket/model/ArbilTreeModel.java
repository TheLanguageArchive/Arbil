package nl.mpi.arbil.wicket.model;

import java.net.URI;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTreeModel implements TreeModel {

    // TODO: Make list of URI's, data nodes on rendering (or something)
    
    public ArbilTreeModel(List<URI> uris){
    }
    
    
    public Object getRoot() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getChild(Object parent, int index) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getChildCount(Object parent) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isLeaf(Object node) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getIndexOfChild(Object parent, Object child) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addTreeModelListener(TreeModelListener l) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeTreeModelListener(TreeModelListener l) {
	throw new UnsupportedOperationException("Not supported yet.");
    }
   
}
