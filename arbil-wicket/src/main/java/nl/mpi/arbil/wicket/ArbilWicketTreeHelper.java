package nl.mpi.arbil.wicket;

import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.data.AbstractTreeHelper;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;

/**
 * TreeHelper that is tied to a specific session storage
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTreeHelper extends AbstractTreeHelper {

    private SessionStorage sessionStorage;
    private ArbilWicketTreeModel localCorpusTreeModel;
    private ArbilWicketTreeModel remoteCorpusTreeModel;
    private ArbilWicketTreeModel localDirectoryTreeModel;
    private ArbilWicketTreeModel favouritesTreeModel;

    /**
     * 
     * @param sessionStorage Storage to tie treehelper to
     */
    public ArbilWicketTreeHelper(SessionStorage sessionStorage) {
	super();
	this.sessionStorage = sessionStorage;
	initTrees();
	loadLocationsList();
    }

    @Override
    protected synchronized SessionStorage getSessionStorage() {
	return sessionStorage;
    }

    @Override
    public void applyRootLocations() {
	localCorpusTreeModel.setRootNodeChildren(getLocalCorpusNodes());
	localCorpusTreeModel.requestResort();

	remoteCorpusTreeModel.setRootNodeChildren(getLocalCorpusNodes());
	remoteCorpusTreeModel.requestResort();

	localDirectoryTreeModel.setRootNodeChildren(getLocalCorpusNodes());
	localDirectoryTreeModel.requestResort();

	favouritesTreeModel.setRootNodeChildren(getLocalCorpusNodes());
	favouritesTreeModel.requestResort();

    }

    @Override
    protected void initTreeModels() {
	localCorpusTreeModel = new ArbilWicketTreeModel(localCorpusRootNode);
	remoteCorpusTreeModel = new ArbilWicketTreeModel(remoteCorpusRootNode);
	localDirectoryTreeModel = new ArbilWicketTreeModel(localDirectoryRootNode);
	favouritesTreeModel = new ArbilWicketTreeModel(favouritesRootNode);
    }
}
