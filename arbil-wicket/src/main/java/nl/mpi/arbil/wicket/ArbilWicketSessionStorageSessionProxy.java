package nl.mpi.arbil.wicket;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import javax.swing.JLabel;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.DownloadAbortFlag;

/**
 * Proxy for the ArbilWicketSessionStorage that is contained in the session that makes
 * the request. To be injected into Arbil core classes.
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketSessionStorageSessionProxy implements SessionStorage {

    private SessionStorage getSessionStorage() {
	// Session storage is retrieved from the session - each session has its own
	return ArbilWicketSession.get().getSessionStorage();
    }

    public void changeCacheDirectory(File preferedCacheDirectory, boolean moveFiles) {
	getSessionStorage().changeCacheDirectory(preferedCacheDirectory, moveFiles);
    }

    public File getCacheDirectory() {
	return getSessionStorage().getCacheDirectory();
    }

    public File getExportPath(String pathString, String destinationDirectory) {
	return getSessionStorage().getExportPath(pathString, destinationDirectory);
    }

    public File getFavouritesDir() {
	return getSessionStorage().getFavouritesDir();
    }

    public String[] getLocationOptions() {
	return getSessionStorage().getLocationOptions();
    }

    public URI getNewArbilFileName(File parentDirectory, String nodeType) {
	return getSessionStorage().getNewArbilFileName(parentDirectory, nodeType);
    }

    public URI getOriginatingUri(URI locationInCacheURI) {
	return getSessionStorage().getOriginatingUri(locationInCacheURI);
    }

    public File getSaveLocation(String pathString) {
	return getSessionStorage().getSaveLocation(pathString);
    }

    public boolean loadBoolean(String filename, boolean defaultValue) {
	return getSessionStorage().loadBoolean(filename, defaultValue);
    }

    public Object loadObject(String filename) throws Exception {
	return getSessionStorage().loadObject(filename);
    }

    public String loadString(String filename) {
	return getSessionStorage().loadString(filename);
    }

    public String[] loadStringArray(String filename) throws IOException {
	return getSessionStorage().loadStringArray(filename);
    }

    public boolean pathIsInFavourites(File fullTestFile) {
	return getSessionStorage().pathIsInFavourites(fullTestFile);
    }

    public boolean pathIsInsideCache(File fullTestFile) {
	return getSessionStorage().pathIsInsideCache(fullTestFile);
    }

    public boolean replaceCacheCopy(String pathString) {
	return getSessionStorage().replaceCacheCopy(pathString);
    }

    public void saveBoolean(String filename, boolean storableValue) {
	getSessionStorage().saveBoolean(filename, storableValue);
    }

    public void saveObject(Serializable object, String filename) throws IOException {
	getSessionStorage().saveObject(object, filename);
    }

    public boolean saveRemoteResource(URL targetUrl, File destinationFile, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, DownloadAbortFlag abortFlag, JLabel progressLabel) {
	return getSessionStorage().saveRemoteResource(targetUrl, destinationFile, shibbolethNegotiator, expireCacheCopy, abortFlag, progressLabel);
    }

    public void saveString(String filename, String storableValue) {
	getSessionStorage().saveString(filename, storableValue);
    }

    public void saveStringArray(String filename, String[] storableValue) throws IOException {
	getSessionStorage().saveStringArray(filename, storableValue);
    }

    public File updateCache(String pathString, int expireCacheDays) {
	return getSessionStorage().updateCache(pathString, expireCacheDays);
    }

    public File updateCache(String pathString, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, DownloadAbortFlag abortFlag, JLabel progressLabel) {
	return getSessionStorage().updateCache(pathString, shibbolethNegotiator, expireCacheCopy, abortFlag, progressLabel);
    }

    public File getStorageDirectory() {
	return getSessionStorage().getStorageDirectory();
    }

    public boolean isTrackTableSelection() {
	return getSessionStorage().isTrackTableSelection();
    }

    public void setTrackTableSelection(boolean trackTableSelection) {
	getSessionStorage().setTrackTableSelection(trackTableSelection);
    }

    public boolean isUseLanguageIdInColumnName() {
	return getSessionStorage().isUseLanguageIdInColumnName();
    }

    public void setUseLanguageIdInColumnName(boolean useLanguageIdInColumnName) {
	getSessionStorage().setUseLanguageIdInColumnName(useLanguageIdInColumnName);
    }

    public File getTypeCheckerConfig() {
	return getSessionStorage().getTypeCheckerConfig();
    }
}
