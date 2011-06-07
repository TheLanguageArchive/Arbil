package nl.mpi.arbil.wicket.pages;

import javax.swing.tree.TreeModel;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.wicket.components.ArbilWicketTree;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;

/**
 * Homepage (test page)
 */
public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor that is invoked when page is invoked without a session.
     * 
     */
    //this(TreeHelper.getSingleInstance().localCorpusNodes);
    //}
    public HomePage(final PageParameters parameters) {
	super(parameters);
//	add(new NodesPanel("testcomponent", new DataNodeDataProvider(DetachableArbilDataNodeCollector.URIsFromNodes(
//		Arrays.asList(ArbilWicketSession.get().getTreeHelper().getLocalCorpusNodes())))));
	ArbilWicketSession.get().getTreeHelper().applyRootLocations();
	TreeModel treeModel = ArbilWicketSession.get().getTreeHelper().getRemoteCorpusTreeModel();
	add(new ArbilWicketTree("testcomponent", new ArbilWicketTreeModel.DetachableArbilWicketTreeModel(treeModel)));
    }
}
