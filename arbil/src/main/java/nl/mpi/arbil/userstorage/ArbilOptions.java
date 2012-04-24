/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.userstorage;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilOptions {

    private final SessionStorage sessionStorage;
    private boolean copyNewResourcesToCache;

    public ArbilOptions(SessionStorage sessionStorage) {
	this.sessionStorage = sessionStorage;
	copyNewResourcesToCache = sessionStorage.loadBoolean("copyNewResources", false);
    }

    /**
     * Get the value of copyNewResourcesToCache
     *
     * @return the value of copyNewResourcesToCache
     */
    public synchronized boolean isCopyNewResourcesToCache() {
	return copyNewResourcesToCache;
    }

    /**
     * Set the value of copyNewResourcesToCache
     *
     * @param copyNewResourcesToCache new value of copyNewResourcesToCache
     */
    public synchronized void setCopyNewResourcesToCache(boolean copyNewResourcesToCache) {
	this.copyNewResourcesToCache = copyNewResourcesToCache;
	sessionStorage.saveBoolean("copyNewResources", copyNewResourcesToCache);
    }
}
