package nl.mpi.arbil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : LinorgFavourites
 * Created on : Mar 3, 2009, 11:19:14 AM
 * @author Peter.Withers@mpi.nl
 */
public class LinorgFavourites {

    private Hashtable<String, ImdiTreeObject> userFavourites;
    static private LinorgFavourites singleInstance = null;

    static synchronized public LinorgFavourites getSingleInstance() {
        System.out.println("LinorgFavourites getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new LinorgFavourites();
        }
        return singleInstance;
    }

    private LinorgFavourites() {
        loadSelectedFavourites();
    }

    private void loadSelectedFavourites() {
        Vector<String> userFavouritesStrings;
        try {
            userFavouritesStrings = (Vector<String>) LinorgSessionStorage.getSingleInstance().loadObject("selectedFavourites");
        } catch (Exception ex) {
            System.out.println("load selectedFavourites failed: " + ex.getMessage());
            userFavouritesStrings = new Vector<String>();
        }
        userFavourites = new Hashtable<String, ImdiTreeObject>();
        // loop favourites and load the imdi objects then set the favourite flags for each
        for (Enumeration<String> favouritesEnum = userFavouritesStrings.elements(); favouritesEnum.hasMoreElements();) {
            ImdiTreeObject currentImdiObject = GuiHelper.imdiLoader.getImdiObject(null, favouritesEnum.nextElement());
            currentImdiObject.setFavouriteStatus(true);
            userFavourites.put(currentImdiObject.getUrlString(), currentImdiObject);
        }
        TreeHelper.getSingleInstance().applyRootLocations();
    }

    public boolean toggleFavouritesList(ImdiTreeObject[] imdiObjectArray, boolean setAsTempate) {
        System.out.println("toggleFavouriteList: " + setAsTempate);
        if (setAsTempate) {
            boolean selectionNeedsSave = false;
            for (ImdiTreeObject currentImdiObject : imdiObjectArray) {
                if (currentImdiObject.needsSaveToDisk) {
                    selectionNeedsSave = true;
                }
            }
            if (selectionNeedsSave) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Changes must be saved before adding to the favourites.", "Add Favourites");
                return false;
            }
        }
        for (ImdiTreeObject currentImdiObject : imdiObjectArray) {
            if (currentImdiObject.getFields().size() == 0) {
                // note: the way that favourites are shown in a table will not show meta nodes but their child nodes instead
                setAsTempate = false;
            }
            if (setAsTempate) {
                addAsFavourite(currentImdiObject.getUrlString());
            } else {
                removeFromFavourites(currentImdiObject.getUrlString());
                currentImdiObject.setFavouriteStatus(false);
            }
        }
        return true;
    }

    private void addAsFavourite(String imdiUrlString) {
        try {
            String[] urlParts = imdiUrlString.split("#");
            File copiedFile = copyToFavouritesDirectory(urlParts[0]);
            String favouriteUrlString = copiedFile.getCanonicalPath();
            if (urlParts.length > 1) {
                favouriteUrlString = favouriteUrlString + "#" + urlParts[1];
            }
            if (!userFavourites.containsKey(favouriteUrlString)) {
                ImdiTreeObject favouriteImdiObject = GuiHelper.imdiLoader.getImdiObject(null, favouriteUrlString);
                userFavourites.put(favouriteUrlString, favouriteImdiObject);
                saveSelectedFavourites();
                loadSelectedFavourites();
                favouriteImdiObject.setFavouriteStatus(true);
            }

        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    private File copyToFavouritesDirectory(String imdiUrlString) throws MalformedURLException, IOException {
        mpi.util.OurURL inUrlLocal = new mpi.util.OurURL(imdiUrlString);
        File destinationFile = File.createTempFile("fav-", ".imdi", LinorgSessionStorage.getSingleInstance().getFavouritesDir());
        mpi.util.OurURL destinationUrl = new mpi.util.OurURL(destinationFile.toURL());

        org.w3c.dom.Document nodDom = ImdiTreeObject.api.loadIMDIDocument(inUrlLocal, false);
        mpi.imdi.api.IMDILink[] links = ImdiTreeObject.api.getIMDILinks(nodDom, inUrlLocal, mpi.imdi.api.WSNodeType.UNKNOWN);
        if (links != null) {
            for (mpi.imdi.api.IMDILink currentLink : links) {
                ImdiTreeObject.api.changeIMDILink(nodDom, destinationUrl, currentLink);
            }
        }
        boolean removeIdAttributes = false;
        ImdiTreeObject.api.writeDOM(nodDom, destinationFile, removeIdAttributes);
        return destinationFile;
    }

    private void removeFromFavourites(String imdiUrlString) {
        while (userFavourites.containsKey(imdiUrlString)) {
            userFavourites.remove(imdiUrlString);
        }
        saveSelectedFavourites();
        loadSelectedFavourites();
    }

    public void saveSelectedFavourites() {
        try {
            LinorgSessionStorage.getSingleInstance().saveObject(new Vector(userFavourites.keySet()), "selectedFavourites");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    public Vector getFavouritesAsUrls() {
        return new Vector(userFavourites.keySet());
    }

    public ImdiTreeObject[] listAllFavourites() {
        return userFavourites.values().toArray(new ImdiTreeObject[userFavourites.size()]);
    }

    public Enumeration listFavouritesFor(Object targetNodeUserObject) {
        System.out.println("listFavouritesFor: " + targetNodeUserObject);
        Vector<String[]> validFavourites = new Vector<String[]>();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            ImdiTreeObject targetImdiObject = (ImdiTreeObject) targetNodeUserObject;
            boolean targetIsCorpus = targetImdiObject.isCorpus();
            boolean targetIsSession = targetImdiObject.isSession();
            boolean targetIsImdiChild = targetImdiObject.isImdiChild();
            for (Enumeration<ImdiTreeObject> imdiObjectEnum = userFavourites.elements(); imdiObjectEnum.hasMoreElements();) {
                ImdiTreeObject currentFavouritesObject = imdiObjectEnum.nextElement();
                boolean addThisFavourites = false;
                if (targetIsCorpus && !currentFavouritesObject.isImdiChild()) {
                    addThisFavourites = true;
                } else if (targetIsSession && currentFavouritesObject.isImdiChild()) {
                    addThisFavourites = GuiHelper.imdiSchema.nodeCanExistInNode(targetImdiObject, currentFavouritesObject);
                } else if (targetIsImdiChild && currentFavouritesObject.isImdiChild()) {
                    addThisFavourites = GuiHelper.imdiSchema.nodeCanExistInNode(targetImdiObject, currentFavouritesObject);
                }
                if (addThisFavourites) {
//                    System.out.println("adding: " + currentFavouritesObject);
                    validFavourites.add(new String[]{currentFavouritesObject.toString(), currentFavouritesObject.getUrlString()});
                } else {
                    // imdi child favourites cannot be added to a corpus
                    // sessions cannot be added to a session
//                    System.out.println("omitting: " + currentFavouriteObject);
                }
            }
        }
        return validFavourites.elements();
    }

    public String getNodeType(ImdiTreeObject favouriteImdiObject, ImdiTreeObject targetImdiObject) {
        // takes the source path and destination path and creates a complete combined path
        // in:
        // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(12).Languages.Language(5)
        // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(27)
        // out: 
        // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(27).Languages.Language

        String favouriteXmlPath = favouriteImdiObject.getURL().getRef();
        String targetXmlPath = targetImdiObject.getURL().getRef();
        System.out.println("getNodeType: \nfavouriteXmlPath: " + favouriteXmlPath + "\ntargetXmlPath:" + targetXmlPath);
        String returnValue;
        if (favouriteImdiObject.isSession()) {
            returnValue = ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session";
        } else if (favouriteImdiObject.isCorpus()) {
            returnValue = ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Corpus";
        } else if (favouriteImdiObject.isImdiChild()) {
            if (targetXmlPath == null) {
                returnValue = favouriteXmlPath.replaceAll("\\(\\d*?\\)$", "");
            } else {
                // pass the (x) values on in the return value
                System.out.println("targetXmlPath: " + targetXmlPath);
                System.out.println("favouriteXmlPath: " + favouriteXmlPath);
                favouriteXmlPath = favouriteXmlPath.replaceAll("\\(\\d*?\\)$", "");
                String[] splitFavouriteXmlPath = favouriteXmlPath.split("\\)");
                String[] splitTargetXmlPath = targetXmlPath.split("\\)");
                System.out.println("splitFavouriteXmlPath: " + splitFavouriteXmlPath.length + " splitTargetXmlPath: " + splitTargetXmlPath.length);
                returnValue = "";
                for (int partCounter = 0; partCounter < splitFavouriteXmlPath.length; partCounter++) {
                    if (splitTargetXmlPath.length > partCounter) {
                        returnValue = returnValue.concat(splitTargetXmlPath[partCounter] + ")");
                    } else {
                        returnValue = returnValue.concat(splitFavouriteXmlPath[partCounter] + ")");
                    }
                }
                returnValue = returnValue.replaceAll("\\)$", "");
            }
        } else {
            returnValue = null;
        }
        System.out.println("getNodeTypeReturnValue: " + returnValue);
        return returnValue;
    }

    public void mergeFromFavourite(ImdiTreeObject targetImdiObject, ImdiTreeObject favouriteImdiObject, boolean overwriteValues) {
//        System.out.println("mergeFromFavourite: " + addedNodeUrl + " : " + imdiTemplateUrl);
        Hashtable<String, ImdiField[]> targetFieldsHash = targetImdiObject.getFields();
        for (Enumeration<ImdiField[]> favouriteFieldEnum = favouriteImdiObject.getFields().elements(); favouriteFieldEnum.hasMoreElements();) {
            ImdiField[] currentFavouriteFields = favouriteFieldEnum.nextElement();
            if (currentFavouriteFields.length > 0) {
                ImdiField[] targetNodeFields = targetFieldsHash.get(currentFavouriteFields[0].getTranslateFieldName());

                System.out.println("TranslateFieldName: " + currentFavouriteFields[0].getTranslateFieldName());
                System.out.println("targetImdiObjectLoading: " + targetImdiObject.isLoading());
                if (targetNodeFields != null) {
                    System.out.println("copy fields");
                    for (int fieldCounter = 0; fieldCounter < currentFavouriteFields.length; fieldCounter++) {
                        ImdiField currentField;
                        if (targetNodeFields.length > fieldCounter) {
                            // copy to the exisiting fields
                            currentField = targetNodeFields[fieldCounter];
                            currentField.setFieldValue(currentFavouriteFields[fieldCounter].getFieldValue(), false, false);
                        } else {
                            // add sub nodes if they dont already exist
                            currentField = new ImdiField(targetImdiObject, currentFavouriteFields[fieldCounter].xmlPath, currentFavouriteFields[fieldCounter].getFieldValue());
                            targetImdiObject.addField(currentField);
                            currentField.fieldNeedsSaveToDisk = true;
                        }
                        String currentLanguageId = currentFavouriteFields[fieldCounter].getLanguageId();
                        if (currentLanguageId != null) {
                            currentField.setLanguageId(currentLanguageId, false);
                        }
                    }
                }
            }
        }
    }
}
