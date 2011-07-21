package nl.mpi.arbil.wicket.model;

import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketSearchModel extends Model<ArbilWicketSearch> {
    
    public ArbilWicketSearchModel(){
	super(new ArbilWicketSearch());
    }
    
    public ArbilWicketSearchModel(ArbilWicketNodeSearchTerm term){
	super(new ArbilWicketSearch(term));
    }
    
    public ArbilWicketSearchModel(ArbilWicketSearch object){
	super(object);
    }
}
