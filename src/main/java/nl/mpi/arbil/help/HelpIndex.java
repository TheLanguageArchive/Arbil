package nl.mpi.arbil.help;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HelpIndex {

    private final List<HelpItem> subItems;

    public HelpIndex() {
	this.subItems = new ArrayList<HelpItem>();
    }

    public void addSubItem(HelpItem subItem) {
	subItems.add(subItem);
    }

    public List<HelpItem> getSubItems() {
	return Collections.unmodifiableList(subItems);
    }
}
