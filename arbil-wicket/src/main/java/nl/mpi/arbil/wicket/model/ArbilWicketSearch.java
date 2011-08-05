package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import org.apache.wicket.PageParameters;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketSearch implements Serializable {

    public final static String PARAM_DELIMITER = ";";
    public final static String PARAM_SEARCH_VALUE = "values";
    public final static String PARAM_SEARCH_FIELD = "fields";
    public final static String PARAM_SEARCH_NODETYPE = "nodeTypes";
    public final static String PARAM_SEARCH_OPERATOR = "operators";
    public final static String PARAM_SEARCH_EQUALS = "equals";
    public final static String PARAM_SEARCH_EQUALS_NOT = "NOT";
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
    
    public ArbilWicketSearch(PageParameters parameters, ArbilWicketNodeSearchTerm defaultNodeSearchTerm){
	this();
	setFromPageParameters(parameters, defaultNodeSearchTerm);
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

    /**
     * Sets parameters from Wicket page parameters
     * @param parameters PageParemeters to set search from. Use parameter names as defined by the constants in this class
     * @return Whether applicable parameters have been detected and applied
     */
    public final void setFromPageParameters(PageParameters parameters, ArbilWicketNodeSearchTerm defaultNodeSearchTerm) {
	int termCount = 0;

	// Get arrays for parameter types
	String[] values = null;
	if (parameters.containsKey(PARAM_SEARCH_VALUE)) {
	    values = parameters.getString(PARAM_SEARCH_VALUE).split(PARAM_DELIMITER);
	    termCount = values.length;
	}
	String[] fields = null;
	if (parameters.containsKey(PARAM_SEARCH_FIELD)) {
	    fields = parameters.getString(PARAM_SEARCH_FIELD).split(PARAM_DELIMITER);
	    termCount = Math.max(termCount, fields.length);
	}
	String[] nodeTypes = null;
	if (parameters.containsKey(PARAM_SEARCH_NODETYPE)) {
	    nodeTypes = parameters.getString(PARAM_SEARCH_NODETYPE).split(PARAM_DELIMITER);
	    termCount = Math.max(termCount, nodeTypes.length);
	}
	String[] nodeOperators = null;
	if (parameters.containsKey(PARAM_SEARCH_OPERATOR)) {
	    nodeOperators = parameters.getString(PARAM_SEARCH_OPERATOR).split(PARAM_DELIMITER);
	    termCount = Math.max(termCount, nodeOperators.length);
	}
	String[] nodeEquals = null;
	if (parameters.containsKey(PARAM_SEARCH_EQUALS)) {
	    nodeEquals = parameters.getString(PARAM_SEARCH_EQUALS).split(PARAM_DELIMITER);
	    termCount = Math.max(termCount, nodeEquals.length);
	}

	if (termCount > 0) {
	    for (int i = 0; i < termCount; i++) {
		// Construct a search term for the parameters
		ArbilWicketNodeSearchTerm nodeSearchTerm = defaultNodeSearchTerm.clone();
		// Set value
		if (values != null && values.length > i) {
		    nodeSearchTerm.setSearchString(values[i]);
		}
		// Set field
		if (fields != null && fields.length > i) {
		    nodeSearchTerm.setSearchFieldName(fields[i]);
		}
		// Set node type unless node type is empty for this term (meaning default)
		if (nodeTypes != null && nodeTypes.length > i && nodeTypes[i].length() > 0) {
		    nodeSearchTerm.setNodeType(nodeTypes[i]);
		}
		// Set boolean operators unless empty for this term (meaning default)
		if (nodeOperators != null && nodeOperators.length > i && nodeOperators[i].length() > 0) {
		    nodeSearchTerm.setBooleanAnd(nodeOperators[i].equalsIgnoreCase(ArbilNodeSearchTerm.BOOLEAN_AND));
		}
		// Set is/isNot unless empty for this term (meaning default)
		if (nodeEquals != null && nodeEquals.length > i && nodeEquals[i].length() > 0) {
		    nodeSearchTerm.setNotEqual(nodeEquals[i].equalsIgnoreCase(PARAM_SEARCH_EQUALS_NOT));
		}
		getNodeSearchTerms().add(nodeSearchTerm);
	    }
	} else {
	    getNodeSearchTerms().add(defaultNodeSearchTerm);
	}
    }
}
