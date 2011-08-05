package nl.mpi.arbil.search;

import java.io.Serializable;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSimpleNodeSearchTerm implements ArbilNodeSearchTerm, Serializable {

    protected boolean notEqual = false;
    protected boolean booleanAnd = false;
    protected String nodeType = "";
    protected String searchString = "";
    protected String searchFieldName = "";

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

    @Override
    protected ArbilSimpleNodeSearchTerm clone() {
	ArbilSimpleNodeSearchTerm clone = new ArbilSimpleNodeSearchTerm();
	copyTo(clone);
	return clone;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == this) {
	    return true;
	} else if (obj instanceof ArbilSimpleNodeSearchTerm) {
	    return ((ArbilSimpleNodeSearchTerm) obj).getNodeType().equals(getNodeType())
		    && ((ArbilSimpleNodeSearchTerm) obj).getSearchFieldName().equals(getSearchFieldName())
		    && ((ArbilSimpleNodeSearchTerm) obj).getSearchString().equals(getSearchString())
		    && ((ArbilSimpleNodeSearchTerm) obj).isBooleanAnd() == isBooleanAnd()
		    && ((ArbilSimpleNodeSearchTerm) obj).isNotEqual() == isNotEqual();
	} else {
	    return false;
	}
    }

    @Override
    public int hashCode() {
	int hash = 3;
	hash = 79 * hash + (this.notEqual ? 1 : 0);
	hash = 79 * hash + (this.booleanAnd ? 1 : 0);
	hash = 79 * hash + (this.nodeType != null ? this.nodeType.hashCode() : 0);
	hash = 79 * hash + (this.searchString != null ? this.searchString.hashCode() : 0);
	hash = 79 * hash + (this.searchFieldName != null ? this.searchFieldName.hashCode() : 0);
	return hash;
    }

    public void copyTo(ArbilSimpleNodeSearchTerm clone) {
	clone.setBooleanAnd(isBooleanAnd());
	clone.setNodeType(getNodeType());
	clone.setNotEqual(isNotEqual());
	clone.setSearchFieldName(getSearchFieldName());
	clone.setSearchString(getSearchString());
    }
}
