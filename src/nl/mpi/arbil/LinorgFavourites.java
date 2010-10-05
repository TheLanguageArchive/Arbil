package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
import nl.mpi.arbil.MetadataFile.MetadataReader;
import java.io.File;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : LinorgFavourites
 * Created on : Mar 3, 2009, 11:19:14 AM
 * @author Peter.Withers@mpi.nl
 */
public class LinorgFavourites {
//    private Hashtable<String, ImdiTreeObject> userFavourites;
    static private LinorgFavourites singleInstance = null;

    static synchronized public LinorgFavourites getSingleInstance() {
//        System.out.println("LinorgFavourites getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new LinorgFavourites();
        }
        return singleInstance;
    }

    // this will load any favourites in the old format and delete the old format file
    // this is not used now but should be kept in case some users need to import locations from olde versions of the application
    public void convertOldFormatLocationListsX() {
        try {
            File oldLocationsFile = new File(LinorgSessionStorage.getSingleInstance().storageDirectory, "locationsList");
            File oldFavouritesFile = new File(LinorgSessionStorage.getSingleInstance().storageDirectory, "selectedFavourites");
            if (oldLocationsFile.exists()) {
                Vector<String> locationsList = (Vector<String>) LinorgSessionStorage.getSingleInstance().loadObject("locationsList");
                if (oldFavouritesFile.exists()) {
                    Vector<String> userFavouritesStrings = (Vector<String>) LinorgSessionStorage.getSingleInstance().loadObject("selectedFavourites");
                    locationsList.addAll(userFavouritesStrings);
                    LinorgSessionStorage.getSingleInstance().saveStringArray("locationsList", locationsList.toArray(new String[]{}));
                    oldFavouritesFile.deleteOnExit();
                }
                if (null == LinorgSessionStorage.getSingleInstance().loadStringArray("locationsList")) {
                    LinorgSessionStorage.getSingleInstance().saveStringArray("locationsList", locationsList.toArray(new String[]{}));
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    public boolean toggleFavouritesList(ImdiTreeObject[] imdiObjectArray, boolean setAsTempate) {
        System.out.println("toggleFavouriteList: " + setAsTempate);
        if (setAsTempate) {
            boolean selectionNeedsSave = false;
            for (ImdiTreeObject currentImdiObject : imdiObjectArray) {
                if (currentImdiObject.getNeedsSaveToDisk()) {
                    selectionNeedsSave = true;
                }
            }
            if (selectionNeedsSave) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Changes must be saved before adding to the favourites.", "Add Favourites");
                return false;
            }
        }
        for (ImdiTreeObject currentImdiObject : imdiObjectArray) {
            if (currentImdiObject.isEmptyMetaNode()) {
                // note: the way that favourites are shown in a table will not show meta nodes but their child nodes instead
                setAsTempate = false;
            }
            if (setAsTempate) {
                addAsFavourite(currentImdiObject.getURI());
            } else {
                removeFromFavourites(currentImdiObject.getURI());
                // TODO: remove from any tables and update the tree roots
//                currentImdiObject.setFavouriteStatus(false);
            }
        }
        return true;
    }

    private void addAsFavourite(URI imdiUri) {
        try {
            URI baseUri = new URI(imdiUri.toString().split("#")[0]);
            String fileSuffix = imdiUri.getPath().substring(imdiUri.getPath().lastIndexOf("."));
            File destinationFile = File.createTempFile("fav-", fileSuffix, LinorgSessionStorage.getSingleInstance().getFavouritesDir());
            ImdiTreeObject.getMetadataUtils(baseUri.toString()).copyMetadataFile(baseUri, destinationFile, null, true);

            URI copiedFileURI = destinationFile.toURI();
            // creating a uri with separate parameters could cause the url to be reencoded
            // hence this has been converted to use the string URI constuctor
//            URI favouriteUri = new URI(copiedFileURI.getScheme(), copiedFileURI.getUserInfo(), copiedFileURI.getHost(), copiedFileURI.getPort(), copiedFileURI.getPath(), copiedFileURI.getQuery(),
//                    imdiUri.getFragment());
            String uriString = copiedFileURI.toString().split("#")[0] /* fragment removed */;
            if (imdiUri.getFragment() != null) {
                uriString = uriString + "#" + imdiUri.getFragment();
            }
            URI favouriteUri = new URI(uriString);
//            if (!userFavourites.containsKey(favouriteUrlString)) {
//                ImdiTreeObject favouriteImdiObject = GuiHelper.imdiLoader.getImdiObject(null, favouriteUrlString);
//                userFavourites.put(favouriteUrlString, favouriteImdiObject);
//                saveSelectedFavourites();
//                loadSelectedFavourites();
//                favouriteImdiObject.setFavouriteStatus(true);
//            }
            TreeHelper.getSingleInstance().addLocation(favouriteUri);
            TreeHelper.getSingleInstance().applyRootLocations();
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    private void removeFromFavourites(URI imdiUri) {
        TreeHelper.getSingleInstance().removeLocation(imdiUri);
        TreeHelper.getSingleInstance().applyRootLocations();
    }

//    public void saveSelectedFavourites() {
//        try {
//            LinorgSessionStorage.getSingleInstance().saveObject(new Vector(userFavourites.keySet()), "selectedFavourites");
//        } catch (Exception ex) {
//            GuiHelper.linorgBugCatcher.logError(ex);
//        }
//    }
//    public Vector getFavouritesAsUrls() {
//        return new Vector(userFavourites.keySet());
//    }
//
//    public ImdiTreeObject[] listAllFavourites() {
//        return userFavourites.values().toArray(new ImdiTreeObject[userFavourites.size()]);
//    }

    public Enumeration listFavouritesFor(Object targetNodeUserObject) {
        System.out.println("listFavouritesFor: " + targetNodeUserObject);
        Vector<String[]> validFavourites = new Vector<String[]>();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            ImdiTreeObject targetImdiObject = (ImdiTreeObject) targetNodeUserObject;
            boolean targetIsCorpus = targetImdiObject.isCorpus();
            boolean targetIsSession = targetImdiObject.isSession();
            boolean targetIsImdiChild = targetImdiObject.isImdiChild();
            for (ImdiTreeObject currentFavouritesObject : TreeHelper.getSingleInstance().favouriteNodes) {
                boolean addThisFavourites = false;
                if (targetIsCorpus && !currentFavouritesObject.isImdiChild()) {
                    addThisFavourites = true;
                } else if (targetIsSession && currentFavouritesObject.isImdiChild()) {
                    addThisFavourites = MetadataReader.getSingleInstance().nodeCanExistInNode(targetImdiObject, currentFavouritesObject);
                } else if (targetIsImdiChild && currentFavouritesObject.isImdiChild()) {
                    addThisFavourites = MetadataReader.getSingleInstance().nodeCanExistInNode(targetImdiObject, currentFavouritesObject);
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
        // or in:
        // favouriteXmlPath: .METATRANSCRIPT.Session.MDGroup.Actors.Actor(1)
        // targetXmlPath:.METATRANSCRIPT.Session.MDGroup.Actors.Actor(3)
        // out: 
        // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(27).Languages.Language

        String favouriteXmlPath = favouriteImdiObject.getURI().getFragment();
        String targetXmlPath = targetImdiObject.getURI().getFragment();
        System.out.println("getNodeType: \nfavouriteXmlPath: " + favouriteXmlPath + "\ntargetXmlPath:" + targetXmlPath);
        String returnValue;
        if (favouriteImdiObject.isSession()) {
            returnValue = MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session";
        } else if (favouriteImdiObject.isCorpus()) {
            returnValue = MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Corpus";
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
                returnValue = returnValue.replaceAll("\\(\\d*?$", "");
            }
        } else {
            returnValue = null;
        }
        System.out.println("getNodeTypeReturnValue: " + returnValue);
        return returnValue;
    }

//    public void mergeFromFavourite(ImdiTreeObject targetImdiObject, ImdiTreeObject favouriteImdiObject, boolean overwriteValues) {
////        System.out.println("mergeFromFavourite: " + addedNodeUrl + " : " + imdiTemplateUrl);
//        Hashtable<String, ImdiField[]> targetFieldsHash = targetImdiObject.getFields();
//        for (Enumeration<ImdiField[]> favouriteFieldEnum = favouriteImdiObject.getFields().elements(); favouriteFieldEnum.hasMoreElements();) {
//            ImdiField[] currentFavouriteFields = favouriteFieldEnum.nextElement();
//            if (currentFavouriteFields.length > 0) {
//                ImdiField[] targetNodeFields = targetFieldsHash.get(currentFavouriteFields[0].getTranslateFieldName());
//
//                System.out.println("TranslateFieldName: " + currentFavouriteFields[0].getTranslateFieldName());
//                System.out.println("targetImdiObjectLoading: " + targetImdiObject.isLoading());
//                if (targetNodeFields != null) {
//                    System.out.println("copy fields");
//                    for (int fieldCounter = 0; fieldCounter < currentFavouriteFields.length; fieldCounter++) {
//                        ImdiField currentField;
//                        if (targetNodeFields.length > fieldCounter) {
//                            // copy to the exisiting fields
//                            currentField = targetNodeFields[fieldCounter];
//                            currentField.setFieldValue(currentFavouriteFields[fieldCounter].getFieldValue(), false, false);
//                        } else {
//                            // add sub nodes if they dont already exist
//                            currentField = new ImdiField(0, targetImdiObject, currentFavouriteFields[fieldCounter].xmlPath, "", 0); // this is not correct but this section should be simplified asap
//                            currentField.setFieldValue(currentFavouriteFields[fieldCounter].getFieldValue(), false, true); // this is done separatly to trigger the needs save to disk flag
//                            targetImdiObject.addField(currentField);
////                            currentField.fieldNeedsSaveToDisk = true;
//                        }
//                        String currentLanguageId = currentFavouriteFields[fieldCounter].getLanguageId();
//                        if (currentLanguageId != null) {
//                            currentField.setLanguageId(currentLanguageId, false, true);
//                        }
//                    }
//                }
//            }
//        }
//    }
}
