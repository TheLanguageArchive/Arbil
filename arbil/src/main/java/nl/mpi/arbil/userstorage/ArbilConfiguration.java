/**
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.userstorage;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilConfiguration {

    private boolean verbatimXmlTreeStructure = false;
    public boolean copyNewResourcesToCache = false;

    public boolean isVerbatimXmlTreeStructure() {
	return verbatimXmlTreeStructure;
    }

    public void setVerbatimXmlTreeStructure(boolean verbatimXmlTreeStructure) {
	this.verbatimXmlTreeStructure = verbatimXmlTreeStructure;
    }

    public boolean isCopyNewResourcesToCache() {
	return copyNewResourcesToCache;
    }

    public void setCopyNewResourcesToCache(boolean copyNewResourcesToCache) {
	this.copyNewResourcesToCache = copyNewResourcesToCache;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(String.format("Verbatim XML tree structure: %b\n", verbatimXmlTreeStructure));
	sb.append(String.format("Copy new resources to cache: %b\n", copyNewResourcesToCache));
	return sb.toString();
    }
}
