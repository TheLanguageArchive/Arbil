package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 * Document   : ImdiIcons
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiIcons {

    public ImageIcon linorgIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/arbil128x128.png"));
    public ImageIcon linorgTestingIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/arbilTesting128x128.png"));
    // basic icons used in the gui
    public ImageIcon serverIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/server16x16.png"));
    public ImageIcon directoryIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/directory16x16.png"));
    public ImageIcon computerIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/computer16x16.png"));
    public ImageIcon loadingIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading01.png"));
    // complex icons used for the imdi files
//    private ImageIcon corpusicon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusnode_color.png"));
    private ImageIcon localicon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/local.png"));
    private ImageIcon remoteicon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/remote.png"));
    private ImageIcon localWithArchiveHandle = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/localarchivehandle.png"));
//    private ImageIcon blankIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/blank.png"));
    private ImageIcon writtenresourceIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/writtenresource.png"));
    private ImageIcon videoIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/video.png"));
//    private ImageIcon annotationIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/annotation.png"));
    private ImageIcon audioIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/audio.png"));
//    private ImageIcon mediafileIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/mediafile.png"));
//    private ImageIcon corpuslocal16x16cIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpuslocal16x16c.png"));
//    private ImageIcon metadataIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/metadata.png"));
    private ImageIcon corpusnodeColorIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusnode_color.png"));
    //private ImageIcon missingRedIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/missing-red.png"));
    private ImageIcon missingRedIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/notfound.png"));
//    private ImageIcon corpusnodeIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusnode.png"));
//    private ImageIcon openerClosedBlackIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/Opener_closed_black.png"));
//    private ImageIcon corpusIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpus.png"));
//    private ImageIcon openerOpenBlackIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/Opener_open_black.png"));
//    private ImageIcon corpusserver16x16cIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusserver16x16c.png"));
    private ImageIcon picturesIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/pictures.png"));
//    private ImageIcon corpusserverlocal16x16cIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusserverlocal16x16c.png"));
    private ImageIcon questionRedIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/question-red.png"));
    private ImageIcon dataIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/data.png"));
    private ImageIcon dataemptyIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/dataempty.png"));
//    private ImageIcon server16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/server16x16.png"));
//    private ImageIcon directory16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/directory16x16.png"));
//    private ImageIcon sessionColorLocalIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/session_color-local.png"));
//    private ImageIcon directoryclosed16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/directoryclosed16x16.png"));
    private ImageIcon sessionColorIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/session_color.png"));
    private ImageIcon catalogueColorIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/catalogue.png"));
    private ImageIcon exclamationBlueIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/exclamation-blue.png"));
//    private ImageIcon sessionColorServerlocalIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/session_color-serverlocal.png"));
//    private ImageIcon exclamationGreenIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/exclamation-green.png"));
//    private ImageIcon sessionColorServerIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/session_color-server.png"));
    private ImageIcon exclamationRedIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/exclamation-red.png"));
//    private ImageIcon sessionIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/session.png"));
//    private ImageIcon exclamationYellowIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/exclamation-yellow.png"));
//    private ImageIcon stopIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/stop.png"));
//    private ImageIcon file16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/file16x16.png"));
//    private ImageIcon filelocal16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/filelocal16x16.png"));
    private ImageIcon tickBlueIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/tick-blue.png"));
    private ImageIcon fileIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/file.png"));
    private ImageIcon tickGreenIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/tick-green.png"));
//    private ImageIcon fileserver16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/fileserver16x16.png"));
//    private ImageIcon tickRedIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/tick-red.png"));
//    private ImageIcon fileserverlocal16x16Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/fileserverlocal16x16.png"));
//    private ImageIcon tickYellowIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/tick-yellow.png"));
//    private ImageIcon infofileIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/infofile.png"));
//    private ImageIcon transcriptIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/transcript.png"));
//    private ImageIcon lexiconIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/lexicon.png"));    //  loading icons
//    private ImageIcon loading01Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading01.png"));
//    private ImageIcon loading02Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading02.png"));
//    private ImageIcon loading03Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading03.png"));
//    private ImageIcon loading04Icon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading04.png"));
    public ImageIcon favouriteIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/favourite.png"));
    public ImageIcon lockedIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/lock.png"));
    public ImageIcon unLockedIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/unlock.png"));
//    private ImageIcon templateIcon = new ImageIcon(ImdiIcons.class.getResource("/nl/mpi/arbil/resources/icons/template.png"));
    static private ImdiIcons singleInstance = null;

    static synchronized public ImdiIcons getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new ImdiIcons();
        }
        return singleInstance;
    }

    private ImdiIcons() {
    }

    public ImageIcon getIconForImdi(ImdiTreeObject[] imdiObjectArray) {
        int currentIconXPosition = 0;
        int width = 0;
        int heightMax = 0;
        for (ImdiTreeObject currentImdi : imdiObjectArray) {
            width += currentImdi.getIcon().getIconWidth();
            int height = currentImdi.getIcon().getIconHeight();
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

        if (imdiObject.isLoading()) {
            iconsVector.add(loadingIcon);
        }
        if (imdiObject.isLocal()) {
            if (imdiObject.isImdi()) {
                if (imdiObject.matchesRemote == 0) {
                    if (!imdiObject.hasArchiveHandle) {
                        iconsVector.add(localicon);
                    } else {
                        iconsVector.add(localWithArchiveHandle);
                    }
                } else {
                    iconsVector.add(remoteicon);
                }
            }
        } else {
            iconsVector.add(remoteicon);
            // don't show the corpuslocalservericon until the serverside is done, otherwise the icon will show only after copying a branch but not after a restart
//                            if (matchesLocal == 0) {
//                            } else {
//                                icon = corpuslocalservericon;
//                            }
        }
        if (imdiObject.resourceFileServerResponse == HttpURLConnection.HTTP_OK) {
            iconsVector.add(unLockedIcon);
        } else if (imdiObject.resourceFileServerResponse == HttpURLConnection.HTTP_MOVED_TEMP) {
            iconsVector.add(lockedIcon);
        }
        String mimeTypeForNode = imdiObject.getAnyMimeType();
        if (mimeTypeForNode != null) {
            mimeTypeForNode = mimeTypeForNode.toLowerCase();
            if (mimeTypeForNode.contains("audio")) {
                iconsVector.add(audioIcon);
            } else if (mimeTypeForNode.contains("video")) {
                iconsVector.add(videoIcon);
            } else if (mimeTypeForNode.contains("image")) {// ?????
                iconsVector.add(picturesIcon);
            } else if (mimeTypeForNode.contains("text")) {
                iconsVector.add(writtenresourceIcon);
            } else if (mimeTypeForNode.contains("xml")) {
                iconsVector.add(writtenresourceIcon);
            } else if (mimeTypeForNode.contains("chat")) {
                iconsVector.add(writtenresourceIcon);
            } else if (mimeTypeForNode.contains("pdf")) {
                iconsVector.add(writtenresourceIcon);
            } else if (mimeTypeForNode.contains("kml")) {
                iconsVector.add(writtenresourceIcon);
            } else if (mimeTypeForNode.contains("unspecified")) {
                // no icon for this
            } else if (mimeTypeForNode.length() > 0) {
                iconsVector.add(questionRedIcon);
                GuiHelper.linorgBugCatcher.logError(mimeTypeForNode, new Exception("Icon not found for file type: " + mimeTypeForNode));
            }
        } else if (imdiObject.hasResource()) {
            // the resource is not found so show a unknow resource icon
            iconsVector.add(fileIcon);
        } else if (imdiObject.isImdi()) {
            if (imdiObject.isImdiChild()) {
                if (imdiObject.isMetaNode()) {
                    iconsVector.add(dataemptyIcon);
                } else {
                    iconsVector.add(dataIcon);
                }
            } else if (imdiObject.isSession()) {
                iconsVector.add(sessionColorIcon);
            } else if (imdiObject.isCatalogue()) {
                iconsVector.add(catalogueColorIcon);
            } else if (imdiObject.isCorpus()) {
                iconsVector.add(corpusnodeColorIcon);
            } else {
                // TODO: this icon could be reconsidered since it may not be correct in the case of a session that failed to load
                iconsVector.add(corpusnodeColorIcon);
                //iconsVector.add(blankIcon);
            }
        } else if (imdiObject.isDirectory()) {
            iconsVector.add(UIManager.getIcon("FileView.directoryIcon"));
        } else {
            iconsVector.add(fileIcon);
        }
        // add missing file icon
        if ((imdiObject.fileNotFound) || (imdiObject.hasLocalResource() && imdiObject.hashString == null)) {
            iconsVector.add(missingRedIcon);
        }
        // add a file attached to a session icon
        if (!imdiObject.isImdi() && imdiObject.matchesInCache + imdiObject.matchesRemote > 0) {
            if (imdiObject.matchesRemote > 0) {
                iconsVector.add(tickGreenIcon);
            } else {
                iconsVector.add(tickBlueIcon);
            }
        }
        // add icons for favourites
        if (imdiObject.isFavorite()) {
            iconsVector.add(favouriteIcon);
        }
        // add icons for save state
        if (imdiObject.needsChangesSentToServer()) {
            iconsVector.add(exclamationBlueIcon);
        }
        if (imdiObject.getNeedsSaveToDisk()) {
            iconsVector.add(exclamationRedIcon);
        }
        return compositIcons(iconsVector.toArray());// TODO: here we could construct a string describing the icon and only create if it does not alread exist in a hashtable
    }
}

