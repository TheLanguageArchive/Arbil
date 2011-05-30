package nl.mpi.arbil.wicket.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.data.ListDataProvider;

/**
 * Homepage
 */
public class HomePage extends WebPage {

	private static final long serialVersionUID = 1L;

	// TODO Add any page properties or variables here

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public HomePage(final PageParameters parameters) {
	add(createTable("datatable"));
    }
    
    private Component createTable(String name){
	DataTable<Object> table = new DataTable(name, null, new ListDataProvider(null)  , 100);
	return table;
    }
}
