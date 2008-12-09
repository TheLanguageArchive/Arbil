/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 *
 * @author petwit
 */
public class ImdiIcons {

    private Icon corpusicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/corpusnode_color.png"));
    private Icon corpuslocalicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/corpuslocal16x16c.png"));
    private Icon corpuslocalservericon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/corpusserverlocal16x16c.png"));
    private Icon corpusservericon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/corpusserver16x16c.png"));//    corpusserverlocal16x16c.png corpusserver16x16c.png 
    public Icon serverIcon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/server16x16.png"));
    public Icon directoryIcon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/directory16x16.png"));
    private Icon fileIcon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/file16x16.png"));
    private Icon fileTickIcon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/filetick16x16.png"));
    private Icon fileCrossIcon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/filecross16x16.png"));
    public Icon fileUnknown = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/fileunknown16x16.png"));
    private Icon fileUnReadable = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/fileunreadable16x16.png"));
    private Icon fileServerIcon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/fileserver16x16.png"));
    private Icon fileLocalIcon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/filelocal16x16.png"));
    private Icon fileServerLocalIcon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/fileserverlocal16x16.png"));
    private Icon sessionicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/session_color.png"));
    private Icon sessionlocalservericon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/session_color-serverlocal.png"));
    private Icon sessionlocalicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/session_color-local.png"));
    private Icon sessionservericon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/session_color-server.png"));
    private Icon writtenresicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/writtenresource.png"));
    private Icon mediafileicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/mediafile.png"));
    private Icon videofileicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/video.png"));
    private Icon audiofileicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/audio.png"));
    private Icon picturefileicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/pictures.png"));
    private Icon infofileicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/infofile.png"));
    private Icon unknownnodeicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/file.png"));
    private Icon dataicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/data.png"));
    private Icon stopicon = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/stop.png"));
    private Icon tickb = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/tick-b16x16.png"));
    private Icon tickg = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/tick-g16x16.png"));
    private Icon ticky = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/tick-y16x16.png"));
    private Icon tickbgy = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/tick-bgy16x16.png"));
    private Icon exclamb = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/exclam-b16x16.png"));
    private Icon exclamg = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/exclam-g16x16.png"));
    private Icon exclamy = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/exclam-y16x16.png"));
    private Icon exclamr = new ImageIcon(this.getClass().getResource("/mpi/linorg/resources/icons/exclam-r16x16.png"));

    public ImageIcon getIconForImdi(Object[] imdiObjectArray) {
        int currentIconXPosition = 0;
        int width = corpusicon.getIconWidth() * imdiObjectArray.length;
        int height = corpusicon.getIconHeight();
        if (width == 0) { // make sure that zero length child cells have a width
            width = corpusicon.getIconWidth();
        }
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics().create();
        for (Object childImdiObject : imdiObjectArray) {
            Icon currentIcon = ((ImdiTreeObject) childImdiObject).getIcon();
            currentIcon.paintIcon(null, g2d, currentIconXPosition, 0);
            currentIconXPosition += currentIcon.getIconWidth();
        }
        g2d.dispose();
        return new ImageIcon(bufferedImage);
    }

    public Icon getIconForImdi(ImdiTreeObject imdiObject) {
        // TODO: create compound icons for the imdi nodes
        Icon icon = null;
        if (imdiObject.needsChangesSentToServer()) {
            icon = exclamy;
//                icon = tickb;
//                if (imdiNeedsSaveToDisk) {
//                    
//                }
        }
        if (imdiObject.imdiNeedsSaveToDisk) {
            icon = exclamr;
        }
        if (icon == null) {
            if (imdiObject.mpiMimeType != null) {
                //nodeText = "isImdiChildWithType";
                //String mediaTypeString = typeObject.toString();
                //nodeText = mediaTypeString;
                if (imdiObject.mpiMimeType.contains("audio")) {
                    icon = audiofileicon;
                } else if (imdiObject.mpiMimeType.contains("video")) {
                    icon = videofileicon;
                } else if (imdiObject.mpiMimeType.contains("image")) {// ?????
                    icon = picturefileicon;
                } else if (imdiObject.mpiMimeType.contains("text")) {
                    icon = writtenresicon;
                } else if (imdiObject.mpiMimeType.contains("nonarchivable")) {
                    icon = fileIcon;
                } else if (imdiObject.mpiMimeType.contains("unreadable")) {
                    icon = fileUnReadable;
                } else {
                    icon = fileUnknown; // TODO: add any other required icons; for now if we are not showing a known type then make it known by using an obvious icon
                //imdiObject.nodeText = imdiObject.mpiMimeType + " : " + nodeText;
                }
            } else if (imdiObject.isImdi()) {
                if (imdiObject.isImdiChild()) {
                    if (imdiObject.hasResource() && imdiObject.hashString == null) {
                        icon = fileCrossIcon;
                    } else {
                        icon = dataicon;
                    }
                } else if (imdiObject.isSession()) {
                    if (imdiObject.isLocal()) {
                        if (imdiObject.matchesRemote == 0) {
                            icon = sessionlocalicon;
                        } else {
                            icon = sessionlocalservericon;
                        }
                    } else {
                        icon = sessionservericon;
                    }
                } else {
                    if (imdiObject.isLocal()) {
                        if (imdiObject.matchesRemote == 0) {
                            icon = corpuslocalicon;
                        } else {
                            icon = corpuslocalservericon;
                        }
                    } else {
                        // don't show the corpuslocalservericon until the serverside is done, otherwise the icon will show only after copying a branch but not after a restart
//                            if (matchesLocal == 0) {
                        icon = corpusservericon;
//                            } else {
//                                icon = corpuslocalservericon;
//                            }
                    }
                }
            }
        }
        if (icon == null) {
            if (imdiObject.isDirectory) {
                icon = UIManager.getIcon("FileView.directoryIcon");
            } else {
                if (imdiObject.isLocal()) {

//                        if (mpiMimeType != null) {
//                            nodeText = "[" + mpiMimeType + "]" + nodeText;
//                            icon = mediafileicon;
//                        } else {
//                            if (matchesLocalResource > 0) {
//                                icon = fileTickIcon;
//                            } else /*if (matchesRemote == 0)*/ {
                    icon = fileUnknown;
//                            }
//                        }
//                        else {
//                            icon = fileServerLocalIcon;
//                        }
                } else {
//                        if (matchesLocal == 0) {
                    icon = fileServerIcon;
//                        } else {
//                            icon = fileServerIcon;
//                        }
                }
            }
        }
        return icon;
    }
}
