package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import nl.mpi.arbil.search.ArbilSimpleNodeSearchTerm;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketNodeSearchTerm extends ArbilSimpleNodeSearchTerm implements Serializable {

    @Override
    public ArbilWicketNodeSearchTerm clone() {
	ArbilWicketNodeSearchTerm clone = new ArbilWicketNodeSearchTerm();
	copyTo(clone);
	return clone;
    }
}
