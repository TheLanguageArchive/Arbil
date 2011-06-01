package nl.mpi.arbil.wicket.pages;

import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.TreeHelper;
import nl.mpi.arbil.wicket.ArbilWicketInjector;
import nl.mpi.arbil.wicket.WicketApplication;
import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.util.tester.ITestPageSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestHomePage {

    private WicketTester tester;

    @Before
    public void setUp() {
	tester = new WicketTester(new WicketApplication());
	ArbilWicketInjector.injectHandlers();
    }

    @Test
    public void testRenderMyPage() {
	tester.startPage(getTestPageSource(TreeHelper.getSingleInstance().localCorpusNodes));

	tester.assertRenderedPage(HomePage.class);
	tester.assertComponent("datatable", DataTable.class);
    }

    private ITestPageSource getTestPageSource(final ArbilDataNode[] dataNodes) {
	return new ITestPageSource() {

	    public Page getTestPage() {
		return new HomePage(dataNodes);
	    }
	};
    }
}
