/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
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
    public ArbilSimpleNodeSearchTerm clone() {
	ArbilSimpleNodeSearchTerm clone = new ArbilSimpleNodeSearchTerm();
	copyTo(clone);
	return clone;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == this) {
	    return true;
	} else if (obj instanceof ArbilSimpleNodeSearchTerm) {
	    return stringsEqual(((ArbilSimpleNodeSearchTerm) obj).getNodeType(), (getNodeType()))
		    && stringsEqual(((ArbilSimpleNodeSearchTerm) obj).getSearchFieldName(), (getSearchFieldName()))
		    && stringsEqual(((ArbilSimpleNodeSearchTerm) obj).getSearchString(), (getSearchString()))
		    && ((ArbilSimpleNodeSearchTerm) obj).isBooleanAnd() == isBooleanAnd()
		    && ((ArbilSimpleNodeSearchTerm) obj).isNotEqual() == isNotEqual();
	} else {
	    return false;
	}
    }

    private static boolean stringsEqual(String a, String b) {
	if (a == null) {
	    return b == null;
	} else {
	    return a.equals(b);
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
