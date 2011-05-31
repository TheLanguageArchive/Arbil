package nl.mpi.arbil.wicket.pages;

import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.TreeHelper;
import nl.mpi.arbil.wicket.components.ArbilTable;
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
    public HomePage(final PageParameters parameters) {
	this(TreeHelper.getSingleInstance().localCorpusNodes);
    }

    public HomePage(ArbilDataNode[] dataNodes) {
	add(new ArbilTable("datatable", dataNodes));
    }
}
