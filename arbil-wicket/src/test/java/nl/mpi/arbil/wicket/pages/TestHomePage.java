package nl.mpi.arbil.wicket.pages;

import nl.mpi.arbil.wicket.ArbilWicketInjector;
import nl.mpi.arbil.wicket.ArbilWicketApplication;
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
	tester = new WicketTester(new ArbilWicketApplication());
	ArbilWicketInjector.injectHandlers();
    }

    @Test
    public void testRenderMyPage() {
	tester.startPage(HomePage.class);

	tester.assertRenderedPage(HomePage.class);
	//tester.assertComponent("nodespanel", NodesPanel.class);
	//tester.assertComponent("nodespanel:datatable", DataTable.class);
    }
}