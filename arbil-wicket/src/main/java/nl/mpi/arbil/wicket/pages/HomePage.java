package nl.mpi.arbil.wicket.pages;

import javax.swing.tree.TreeNode;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.wicket.components.ArbilWicketTree;
import nl.mpi.arbil.wicket.components.ArbilWicketTablePanel;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * Homepage (test page)
 */
public class HomePage extends TreePage {

    private static final long serialVersionUID = 1L;
    WebMarkupContainer tableContainer;
    private WebMarkupContainer tablePanel;

    public HomePage(final PageParameters parameters) {
	super(parameters);

	ArbilWicketSession.get().getTreeHelper().applyRootLocations();

	tableContainer = new WebMarkupContainer("tableContainer");
	tableContainer.setOutputMarkupId(true);
	tableContainer.setMarkupId("tableContainer");
	add(tableContainer);

	// Empty placeholder for table panel until a node is selected
	tablePanel = new WebMarkupContainer("tablePanel");
	tableContainer.add(tablePanel);

	add(createRemoteTree());
	add(createLocalTree());
    }

    @Override
    protected void onTreeNodeClicked(ArbilWicketTree tree, TreeNode treeNode, AjaxRequestTarget target) {
	ArbilWicketTableModel model = new ArbilWicketTableModel();
	model.setShowIcons(true);
	if (0 < tree.addSelectedNodesToModel(model)) {
	    // Nodes have been added to model. Show new table
	    tablePanel = new ArbilWicketTablePanel("tablePanel", model);
	    tableContainer.addOrReplace(tablePanel);
	    if (target != null) {
		target.addComponent(tableContainer);
	    }
	} // else nothing to show
    }
}
