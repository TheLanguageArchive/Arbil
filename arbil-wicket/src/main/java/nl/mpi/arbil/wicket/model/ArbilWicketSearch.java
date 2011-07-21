package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketSearch implements Serializable {

    private List<ArbilWicketNodeSearchTerm> nodeSearchTerms;
    private String remoteSearchTerm;

    public ArbilWicketSearch(List<ArbilWicketNodeSearchTerm> nodeSearchTerms, String remoteSearchTerm) {
	this.nodeSearchTerms = nodeSearchTerms;
	this.remoteSearchTerm = remoteSearchTerm;
    }

    public ArbilWicketSearch() {
	this(new ArrayList<ArbilWicketNodeSearchTerm>(), null);
    }

    public ArbilWicketSearch(ArbilWicketNodeSearchTerm term) {
	this();
	this.nodeSearchTerms.add(term);
    }

    public ArbilWicketSearch(ArbilWicketNodeSearchTerm term, String remoteSearchString) {
	this(term);
	this.remoteSearchTerm = remoteSearchString;
    }

    /**
     * @return the nodeSearchTerms
     */
    public List<ArbilWicketNodeSearchTerm> getNodeSearchTerms() {
	return nodeSearchTerms;
    }

    /**
     * @param nodeSearchTerms the nodeSearchTerms to set
     */
    public void setNodeSearchTerms(List<ArbilWicketNodeSearchTerm> nodeSearchTerms) {
	this.nodeSearchTerms = nodeSearchTerms;
    }

    /**
     * @return the remoteSearchTerm
     */
    public String getRemoteSearchTerm() {
	return remoteSearchTerm;
    }

    /**
     * @param remoteSearchTerm the remoteSearchTerm to set
     */
    public void setRemoteSearchTerm(String remoteSearchTerm) {
	this.remoteSearchTerm = remoteSearchTerm;
    }
}
