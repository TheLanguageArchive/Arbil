package nl.mpi.arbil.wicket.pages;

import java.util.Arrays;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.wicket.components.NodesPanel;
import nl.mpi.arbil.wicket.model.DataNodeDataProvider;
import nl.mpi.arbil.wicket.model.DetachableArbilDataNodeCollector;
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
	add(new NodesPanel("nodespanel", new DataNodeDataProvider(DetachableArbilDataNodeCollector.URIsFromNodes(
		Arrays.asList(ArbilWicketSession.get().getTreeHelper().getLocalCorpusNodes())))));
	//ArbilWicketSession.get().getTreeHelper().applyRootLocations();
	//add(new Tree("tree", ArbilWicketSession.get().getTreeHelper().getRemoteCorpusTreeModel()));
    }
}
