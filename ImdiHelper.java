/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author petwit
 */
public class ImdiHelper {

    static public ImdiVocabularies imdiVocabularies = new ImdiVocabularies();
    //    static Icon collapsedicon = new ImageIcon("/icons/Opener_open_black.png");
//    static Icon expandedicon = new ImageIcon("/icons/Opener_closed_black.png");
    // TODO: move these icons to the gui section of code, maybe load durring the gui creation and pass to here
    //static Object GuiHelper.linorgSessionStorage;
    static Icon corpusicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/corpusnode_color.png"));
    static Icon corpuslocalicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/corpuslocal16x16c.png"));
    static Icon corpuslocalservericon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/corpusserverlocal16x16c.png"));
    static Icon corpusservericon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/corpusserver16x16c.png"));//    corpusserverlocal16x16c.png corpusserver16x16c.png 
//            corpuslocal16x16c.png
//            file16x16.png
//            fileticka16x16.png
    static Icon serverIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/server16x16.png"));
    static Icon directoryIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/directory16x16.png"));
    static Icon fileIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/file16x16.png"));
    static Icon fileTickIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/filetick16x16.png"));
    static Icon fileCrossIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/filecross16x16.png"));
    static Icon fileUnknown = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/fileunknown16x16.png"));
    static Icon fileUnReadable = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/fileunreadable16x16.png"));
    static Icon fileServerIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/fileserver16x16.png"));
    static Icon fileLocalIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/filelocal16x16.png"));
    static Icon fileServerLocalIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/fileserverlocal16x16.png"));
    static Icon sessionicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/session_color.png"));
    static Icon sessionlocalservericon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/session_color-serverlocal.png"));
    static Icon sessionlocalicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/session_color-local.png"));
    static Icon sessionservericon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/session_color-server.png"));
    static Icon writtenresicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/writtenresource.png"));
    static Icon mediafileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/mediafile.png"));
    static Icon videofileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/video.png"));
    static Icon audiofileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/audio.png"));
    static Icon picturefileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/pictures.png"));
    static Icon infofileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/infofile.png"));
    static Icon unknownnodeicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/file.png"));
    static Icon dataicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/data.png"));
    static Icon stopicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/stop.png"));
    static Icon tickb = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/tick-b16x16.png"));
    static Icon tickg = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/tick-g16x16.png"));
    static Icon ticky = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/tick-y16x16.png"));
    static Icon tickbgy = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/tick-bgy16x16.png"));
    static Icon exclamb = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/exclam-b16x16.png"));
    static Icon exclamg = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/exclam-g16x16.png"));
    static Icon exclamy = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/exclam-y16x16.png"));
    static Icon exclamr = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/exclam-r16x16.png"));    
    
    //static Icon directoryIcon = UIManager.getIcon("FileView.directoryIcon");    
//    static Icon fileIcon = UIManager.getIcon("FileView.fileIcon");
    //                        UIManager.getIcon("FileView.directoryIcon");
//                        UIManager.getIcon("FileView.fileIcon");
//                        UIManager.getIcon("FileView.computerIcon");
//                        UIManager.getIcon("FileView.hardDriveIcon");
//                        UIManager.getIcon("FileView.floppyDriveIcon");
//
//                        UIManager.getIcon("FileChooser.newFolderIcon");
//                        UIManager.getIcon("FileChooser.upFolderIcon");
//                        UIManager.getIcon("FileChooser.homeFolderIcon");
//                        UIManager.getIcon("FileChooser.detailsViewIcon");
//                        UIManager.getIcon("FileChooser.listViewIcon");

//    static Icon idleIcon = new ImageIcon("build/classes/mpi/linorg/resources/busyicons/idle-icon.png");            
//    static Icon corpusicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/corpus.png");
//    static Icon sessionicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/session.png");
//    static Icon writtenresicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/transcript.png");
//    static Icon mediafileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/media.png");
//    static Icon videofileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/media.png");
//    static Icon audiofileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/media.png");
//    static Icon picturefileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/media.png");
//    static Icon infofileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/ifile.png");
//    static Icon unknownnodeicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/file.png");
//    static Icon dataicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/data.png");
//    static Icon stopicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/stop.png");
//    static Icon idleIcon = new ImageIcon("build/classes/mpi/linorg/resources/busyicons/idle-icon.png");
    //ResourceMap resourceMap = getResourceMap();
//    ApplicationContext ctxt = getContext();
//        ResourceManager mgr = ctxt.getResourceManager();
//        //resource = mgr.getResourceMap(HelloWorld.class);
//    static Icon collapsedicon = resourceMap.getIcon ("imdiTree.collapsedicon");
//    static Icon expandedicon = resourceMap.getIcon ("imdiTree.expandedicon");
//    static Icon corpusicon = resourceMap.getIcon ("imdiTree.corpusicon");
//    static Icon sessionicon = resourceMap.getIcon ("imdiTree.sessionicon");
//    static Icon writtenresicon = resourceMap.getIcon ("imdiTree.writtenresicon");
//    static Icon mediafileicon = resourceMap.getIcon ("imdiTree.mediafileicon");
//    static Icon videofileicon = resourceMap.getIcon ("imdiTree.videofileicon");
//    static Icon audiofileicon = resourceMap.getIcon ("imdiTree.audiofileicon");
//    static Icon picturefileicon = resourceMap.getIcon ("imdiTree.picturefileicon");
//    static Icon infofileicon = resourceMap.getIcon ("imdiTree.infofileicon");
//    static Icon unknownnodeicon = resourceMap.getIcon ("imdiTree.unknownnodeicon");
//    static Icon stopicon = resourceMap.getIcon ("imdiTree.stopicon");
//    static Icon idleIcon = resourceMap.getIcon ("imdiTree.idleIcon");


    public boolean isImdiNode(Object unknownObj) {
        if (unknownObj == null) {
            return false;
        }
        return (unknownObj instanceof ImdiTreeObject);
    }

    static public boolean isStringLocal(String urlString) {
        return (!urlString.startsWith("http://"));
    }

    static public boolean isStringImdiHistoryFile(String urlString) {
//        System.out.println("isStringImdiHistoryFile" + urlString);
//        System.out.println("isStringImdiHistoryFile" + urlString.replaceAll(".imdi.[0-9]*$", ".imdi"));
        return isStringImdi(urlString.replaceAll(".imdi.[0-9]*$", ".imdi"));
    }

    static public boolean isStringImdi(String urlString) {
        return urlString.endsWith(".imdi");
    }

    static public boolean isStringImdiChild(String urlString) {
        return urlString.contains("#.METATRANSCRIPT");
    }  
}
