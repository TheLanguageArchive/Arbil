/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 *
 * @author petwit
 */
public class ImdiIcons {
    // basic icons used in the gui
    public static ImageIcon serverIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/server16x16.png"));
    public static ImageIcon directoryIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/directory16x16.png"));
    public static ImageIcon loadingIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/loading01.png"));
    // complex icons used for the imdi files
    private ImageIcon corpusicon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/corpusnode_color.png"));
    private ImageIcon localicon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/local.png"));
    private ImageIcon remoteicon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/remote.png"));
    private ImageIcon blankIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/blank.png"));
    private ImageIcon writtenresourceIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/writtenresource.png"));
    private ImageIcon videoIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/video.png"));
    private ImageIcon annotationIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/annotation.png"));
    private ImageIcon audioIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/audio.png"));
    private ImageIcon mediafileIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/mediafile.png"));
//    private ImageIcon corpuslocal16x16cIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/corpuslocal16x16c.png"));
    private ImageIcon metadataIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/metadata.png"));
    private ImageIcon corpusnodeColorIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/corpusnode_color.png"));
    //private ImageIcon missingRedIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/missing-red.png"));
    private ImageIcon missingRedIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/notfound.png"));
//    private ImageIcon corpusnodeIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/corpusnode.png"));
//    private ImageIcon openerClosedBlackIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/Opener_closed_black.png"));
//    private ImageIcon corpusIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/corpus.png"));
//    private ImageIcon openerOpenBlackIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/Opener_open_black.png"));
//    private ImageIcon corpusserver16x16cIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/corpusserver16x16c.png"));
    private ImageIcon picturesIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/pictures.png"));
//    private ImageIcon corpusserverlocal16x16cIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/corpusserverlocal16x16c.png"));
//    private ImageIcon questionRedIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/question-red.png"));
    private ImageIcon dataIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/data.png"));
    private ImageIcon dataemptyIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/dataempty.png"));
//    private ImageIcon server16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/server16x16.png"));
//    private ImageIcon directory16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/directory16x16.png"));
//    private ImageIcon sessionColorLocalIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/session_color-local.png"));
//    private ImageIcon directoryclosed16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/directoryclosed16x16.png"));
    private ImageIcon sessionColorIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/session_color.png"));
    private ImageIcon exclamationBlueIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/exclamation-blue.png"));
//    private ImageIcon sessionColorServerlocalIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/session_color-serverlocal.png"));
//    private ImageIcon exclamationGreenIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/exclamation-green.png"));
//    private ImageIcon sessionColorServerIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/session_color-server.png"));
    private ImageIcon exclamationRedIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/exclamation-red.png"));
//    private ImageIcon sessionIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/session.png"));
//    private ImageIcon exclamationYellowIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/exclamation-yellow.png"));
//    private ImageIcon stopIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/stop.png"));
//    private ImageIcon file16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/file16x16.png"));
//    private ImageIcon filelocal16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/filelocal16x16.png"));
//    private ImageIcon tickBlueIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/tick-blue.png"));
    private ImageIcon fileIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/file.png"));
//    private ImageIcon tickGreenIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/tick-green.png"));
//    private ImageIcon fileserver16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/fileserver16x16.png"));
//    private ImageIcon tickRedIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/tick-red.png"));
//    private ImageIcon fileserverlocal16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/fileserverlocal16x16.png"));
//    private ImageIcon tickYellowIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/tick-yellow.png"));
//    private ImageIcon infofileIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/infofile.png"));
//    private ImageIcon transcriptIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/transcript.png"));
//    private ImageIcon lexiconIcon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/lexicon.png"));    //  loading icons
    private ImageIcon loading01Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/loading01.png"));
//    private ImageIcon loading02Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/loading02.png"));
//    private ImageIcon loading03Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/loading03.png"));
//    private ImageIcon loading04Icon = new ImageIcon(ImdiIcons.class.getResource("/mpi/linorg/resources/icons/loading04.png"));

    public ImageIcon getIconForImdi(Object[] imdiObjectArray) {
        int currentIconXPosition = 0;
        int width = 0;
        int heightMax = 0;
        for (Object currentImdi : imdiObjectArray) {
            width += ((ImdiTreeObject) currentImdi).getIcon().getIconWidth();
            int height = ((ImdiTreeObject) currentImdi).getIcon().getIconHeight();
            if (heightMax < height) {
                heightMax = height;
            }
        }

        BufferedImage bufferedImage = new BufferedImage(width, heightMax, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics().create();
        for (Object childImdiObject : imdiObjectArray) {
            ImageIcon currentIcon = ((ImdiTreeObject) childImdiObject).getIcon();
            currentIcon.paintIcon(null, g2d, currentIconXPosition, 0);
            currentIconXPosition += currentIcon.getIconWidth();
        }
        g2d.dispose();
        return new ImageIcon(bufferedImage);
    }

    public ImageIcon compositIcons(Object[] iconArray) {
        int widthTotal = 0;
        int heightMax = 0;
        for (Object currentIcon : iconArray) {
            int width = ((Icon) currentIcon).getIconWidth();
            int height = ((Icon) currentIcon).getIconHeight();
            if (currentIcon != missingRedIcon) {
                widthTotal += width;
            }
            if (heightMax < height) {
                heightMax = height;
            }
        }
        int currentIconXPosition = 0;

        BufferedImage bufferedImage = new BufferedImage(widthTotal, heightMax, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics().create();
        for (Object currentIcon : iconArray) {
            int yPos = (heightMax - ((Icon) currentIcon).getIconHeight()) / 2;
            if (currentIcon != missingRedIcon) { // the missing icon always overlays the previous icon
                ((Icon) currentIcon).paintIcon(null, g2d, currentIconXPosition, yPos);
                currentIconXPosition += ((Icon) currentIcon).getIconWidth();
            } else {
                ((Icon) currentIcon).paintIcon(null, g2d, currentIconXPosition - missingRedIcon.getIconWidth(), yPos);
            }
        }
        g2d.dispose();
        return new ImageIcon(bufferedImage);
    }

    public ImageIcon getIconForImdi(ImdiTreeObject imdiObject) {
        Vector iconsVector = new Vector();

        if (imdiObject.isLocal()) {
            if (imdiObject.isImdi()) {
                if (imdiObject.matchesRemote == 0) {
                    iconsVector.add(localicon);
                } else {
                    iconsVector.add(remoteicon);
                }
            }
        } else {
            // don't show the corpuslocalservericon until the serverside is done, otherwise the icon will show only after copying a branch but not after a restart
//                            if (matchesLocal == 0) {
            iconsVector.add(remoteicon);
//                            } else {
//                                icon = corpuslocalservericon;
//                            }
        }
        if (imdiObject.mpiMimeType != null) {
            if (imdiObject.mpiMimeType.contains("audio")) {
                iconsVector.add(audioIcon);
            } else if (imdiObject.mpiMimeType.contains("video")) {
                iconsVector.add(videoIcon);
            } else if (imdiObject.mpiMimeType.contains("image")) {// ?????
                iconsVector.add(picturesIcon);
            } else if (imdiObject.mpiMimeType.contains("text")) {
                iconsVector.add(writtenresourceIcon);
            }
        } else if (imdiObject.hasResource()) {
            // the resource is not found so show a unknow resource icon
            iconsVector.add(fileIcon);
        } else if (imdiObject.isImdi()) {
            if (imdiObject.isImdiChild()) {
                if (imdiObject.getFields().size() > 0) {
                    iconsVector.add(dataIcon);
                } else {
                    iconsVector.add(dataemptyIcon);
                }
            } else if (imdiObject.isSession()) {
                iconsVector.add(sessionColorIcon);
            } else if (imdiObject.isCorpus()) {
                iconsVector.add(corpusnodeColorIcon);
            } else {
                // TODO: this icon could be reconsidered since it may not be correct in the case of a session that failed to load
                iconsVector.add(corpusnodeColorIcon);
            //iconsVector.add(blankIcon);
            }
        } else if (imdiObject.isDirectory) {
            iconsVector.add(UIManager.getIcon("FileView.directoryIcon"));
        } else {
            iconsVector.add(fileIcon);
        }
        // add missing file icon
        if ((imdiObject.fileNotFound) || (imdiObject.hasResource() && imdiObject.hashString == null)) {
            iconsVector.add(missingRedIcon);
        }
        // add icons for save state
        if (imdiObject.needsChangesSentToServer()) {
            iconsVector.add(exclamationBlueIcon);
        }
        if (imdiObject.imdiNeedsSaveToDisk) {
            iconsVector.add(exclamationRedIcon);
        }
        return compositIcons(iconsVector.toArray());// TODO: here we could construct a string describing the icon and only create if it does not alread exist in a hashtable
    }
}

