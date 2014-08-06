/*
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.flap.model.PluginDataNode;
import nl.mpi.flap.plugin.PluginArbilDataNodeLoader;
import nl.mpi.flap.plugin.PluginDialogHandler;
import nl.mpi.flap.plugin.WrongNodeTypeException;

/**
 * @since Jul 15, 2014 11:14:37 AM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class BlockingDataNodeLoader implements PluginArbilDataNodeLoader, DataNodeLoader {

    public static BlockingDataNodeLoader getBlockingDataNodeLoader(final String cacheDirectory) {
        final BlockingDataNodeLoader blockingDataNodeLoader = new BlockingDataNodeLoader() {
        };

        final MessageDialogHandler messageDialogHandler = new MessageDialogHandler() {

            @Override
            public File[] showMetadataFileSelectBox(String titleText, boolean multipleSelect) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public File[] showDirectorySelectBox(String titleText, boolean multipleSelect) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public File showEmptyExportDirectoryDialogue(String titleText) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void offerUserToSaveChanges() throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean askUserToSaveChanges(String entityName) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public MessageDialogHandler.DialogBoxResult showDialogBoxRememberChoice(String message, String title, int optionType, int messageType) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void addMessageDialogToQueue(String messageString, String messageTitle) {
                System.out.println(messageTitle);
                System.out.println(messageString);
            }

            @Override
            public boolean showConfirmDialogBox(String messageString, String messageTitle) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int showDialogBox(String message, String title, int optionType, int messageType) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int showDialogBox(String message, String title, int optionType, int messageType, Object[] options, Object initialValue) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, Map<String, FileFilter> fileFilterMap, PluginDialogHandler.DialogueType dialogueType, JComponent customAccessory) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        final SessionStorage sessionStorage = new ArbilSessionStorage() {

            @Override
            protected String[] getLocationOptions() {
                return new String[]{cacheDirectory + "/"};
            }

            @Override
            public void saveString(String filename, String storableValue) {
            }

            @Override
            public void saveBoolean(String filename, boolean storableValue) {
            }

            @Override
            public void saveStringArray(String filename, String[] storableValue) throws IOException {
            }

            @Override
            public void saveObject(Serializable object, String filename) throws IOException {
            }
            
        };
//        final SessionStorage sessionStorage = new SessionStorage() {
//
//            public void changeCacheDirectory(File preferedCacheDirectory, boolean moveFiles) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public File getExportPath(String pathString, String destinationDirectory) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public File getFavouritesDir() {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public URI getNewArbilFileName(File parentDirectory, String nodeType) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public URI getOriginatingUri(URI locationInCacheURI) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public File getSaveLocation(String pathString) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public boolean loadBoolean(String filename, boolean defaultValue) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public Object loadObject(String filename) throws Exception {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public String loadString(String filename) {
//                return null;
//            }
//
//            public String[] loadStringArray(String filename) throws IOException {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public boolean pathIsInFavourites(File fullTestFile) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public boolean pathIsInsideCache(File fullTestFile) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public boolean replaceCacheCopy(String pathString) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public void saveBoolean(String filename, boolean storableValue) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public void saveObject(Serializable object, String filename) throws IOException {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public boolean saveRemoteResource(URL targetUrl, File destinationFile, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirect, DownloadAbortFlag abortFlag, ProgressListener progressLabel) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public void saveString(String filename, String storableValue) {
//            }
//
//            public void saveStringArray(String filename, String[] storableValue) throws IOException {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public File getFromCache(String pathString, boolean followRedirect) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public File updateCache(String pathString, int expireCacheDays, boolean followRedirect) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public File updateCache(String pathString, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirect, DownloadAbortFlag abortFlag, ProgressListener progressLabel) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public File getTypeCheckerConfig() {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public File getApplicationSettingsDirectory() {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            public File getProjectDirectory() {
//                return null;
//            }
//
//            public File getProjectWorkingDirectory() {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//        };
        CmdiProfileReader.setSessionStorage(sessionStorage);
        ArbilTemplateManager.setSessionStorage(sessionStorage);
        CmdiTemplate.setMessageDialogHandler(messageDialogHandler);
        ArbilEntityResolver.setSessionStorage(sessionStorage);
        ArbilField.setSessionStorage(sessionStorage);
        ArbilField.setDataNodeLoader(blockingDataNodeLoader);
        MetadataReader.setDataNodeLoader(blockingDataNodeLoader);
        return blockingDataNodeLoader;
    }

    public BlockingDataNodeLoader() {

    }

    private final MetadataFormat metadataFormat = new MetadataFormat();

    public PluginDataNode getPluginArbilDataNode(Object registeringObject, URI localUri) {
        ArbilDataNode dataNode = new ArbilDataNode(new BlockingDataNodeService(), localUri, metadataFormat.shallowCheck(localUri));
//        dataNode.loadArbilDom();
        dataNode.loadFullArbilDom();
        return (PluginDataNode) dataNode;
    }

    public URI getNodeURI(PluginDataNode dataNode) throws WrongNodeTypeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isNodeLoading(PluginDataNode dataNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void addNodeNeedingSave(ArbilDataNode nodeToSave) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ArbilDataNode getArbilDataNode(Object registeringObject, URI localUri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ArbilDataNode getArbilDataNodeOnlyIfLoaded(URI arbilUri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ArbilDataNode getArbilDataNodeWithoutLoading(URI localUri) {
        return new ArbilDataNode(new BlockingDataNodeService(), localUri, metadataFormat.shallowCheck(localUri));
    }

    public ArbilDataNode[] getNodesNeedSave() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isSchemaCheckLocalFiles() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean nodesNeedSave() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void removeNodesNeedingSave(ArbilDataNode savedNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void requestReload(ArbilDataNode dataNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void requestReload(ArbilDataNode currentDataNode, ArbilDataNodeLoaderCallBack callback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void requestShallowReload(ArbilDataNode dataNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void requestReloadAllNodes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void requestReloadAllMetadataNodes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void requestReloadOnlyIfLoaded(URI arbilUri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void saveNodesNeedingSave(boolean updateIcons) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void startLoaderThreads() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void stopLoaderThreads() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ArbilDataNode createNewDataNode(URI uri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
//        final File tempFile = File.createTempFile("temp", "unitest");
//        tempFile.mkdir();.getAbsolutePath()
        final String cacheDirectory = System.getProperty("user.dir");
        final BlockingDataNodeLoader blockingDataNodeLoader = BlockingDataNodeLoader.getBlockingDataNodeLoader(cacheDirectory);
        ArbilDataNode dataNode = (ArbilDataNode) blockingDataNodeLoader.getPluginArbilDataNode(null, new URI("http://hdl.handle.net/11142/00-74BB450B-4E5E-4EC7-B043-F444C62DB5C0"));
//        if (dataNode.getLoadingState() != ArbilDataNode.LoadingState.LOADED && dataNode.isMetaDataNode()) {
//            dataNode.loadArbilDom();
//            dataNode.loadFullArbilDom();// todo: this has changed undo if can
//        }
        System.out.println("getChildCount:" + dataNode.getChildCount());
        System.out.println("links:" + dataNode.getChildLinks().size());
        System.out.println("field groups:" + dataNode.getFieldGroups().size());
    }
}
