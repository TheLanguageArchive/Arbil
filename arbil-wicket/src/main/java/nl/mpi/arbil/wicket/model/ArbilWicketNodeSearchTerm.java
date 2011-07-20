package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.search.ArbilRemoteSearch;
import nl.mpi.arbil.search.RemoteServerSearchTerm;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketNodeSearchTerm implements ArbilNodeSearchTerm, RemoteServerSearchTerm, Serializable {

    protected boolean notEqual = false;
    protected boolean booleanAnd = false;
    protected String nodeType = "";
    protected String searchString = "";
    protected String searchFieldName = "";
    private String remoteSearchString = null;

    /**
     * @return the nodeType
     */
    public String getNodeType() {
	return nodeType;
    }

    /**
     * @return the searchFieldName
     */
    public String getSearchFieldName() {
	return searchFieldName;
    }

    /**
     * @return the searchString
     */
    public String getSearchString() {
	return searchString;
    }

    /**
     * @return the booleanAnd
     */
    public boolean isBooleanAnd() {
	return booleanAnd;
    }

    /**
     * @return the notEqual
     */
    public boolean isNotEqual() {
	return notEqual;
    }

    /**
     * @param booleanAnd the booleanAnd to set
     */
    public void setBooleanAnd(boolean booleanAnd) {
	this.booleanAnd = booleanAnd;
    }

    /**
     * @param nodeType the nodeType to set
     */
    public void setNodeType(String nodeType) {
	this.nodeType = nodeType;
    }

    /**
     * @param notEqual the notEqual to set
     */
    public void setNotEqual(boolean notEqual) {
	this.notEqual = notEqual;
    }

    /**
     * @param searchFieldName the searchFieldName to set
     */
    public void setSearchFieldName(String searchFieldName) {
	this.searchFieldName = searchFieldName;
    }

    /**
     * @param searchString the searchString to set
     */
    public void setSearchString(String searchString) {
	this.searchString = searchString;
    }

    /**
     * @return the remoteSearchString
     */
    public String getRemoteSearchString() {
	return remoteSearchString;
    }

    /**
     * @param remoteSearchString the remoteSearchString to set
     */
    public void setRemoteSearchString(String remoteSearchString) {
	this.remoteSearchString = remoteSearchString;
    }

    public RemoteServerSearchTerm getRemoteServerSearchTerm() {
	if (!(null == getRemoteSearchString() || "".equals(getRemoteSearchString()))) {
	    return this;
	} else {
	    return null;
	}
    }

    public URI[] getServerSearchResults(ArbilDataNode[] searchNodes) {
	return new ArbilRemoteSearch().getServerSearchResults(getRemoteSearchString(), searchNodes);
    }
}
