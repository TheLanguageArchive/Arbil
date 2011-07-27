package nl.mpi.arbil.templates;

import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import java.io.File;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;

/**
 * Document   : ArbilFavourites
 * Created on : Mar 3, 2009, 11:19:14 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilFavourites {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }

    //    private Hashtable<String, ImdiTreeObject> userFavourites;
    static private ArbilFavourites singleInstance = null;

    static synchronized public ArbilFavourites getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilFavourites();
	}
	return singleInstance;
    }

    // this will load any favourites in the old format and delete the old format file
    // this is not used now but should be kept in case some users need to import locations from olde versions of the application
    public void convertOldFormatLocationListsX() {
	try {
	    File oldLocationsFile = new File(sessionStorage.getStorageDirectory(), "locationsList");
	    File oldFavouritesFile = new File(sessionStorage.getStorageDirectory(), "selectedFavourites");
	    if (oldLocationsFile.exists()) {
		Vector<String> locationsList = (Vector<String>) sessionStorage.loadObject("locationsList");
		if (oldFavouritesFile.exists()) {
		    Vector<String> userFavouritesStrings = (Vector<String>) sessionStorage.loadObject("selectedFavourites");
		    locationsList.addAll(userFavouritesStrings);
		    sessionStorage.saveStringArray("locationsList", locationsList.toArray(new String[]{}));
		    oldFavouritesFile.deleteOnExit();
		}
		if (null == sessionStorage.loadStringArray("locationsList")) {
		    sessionStorage.saveStringArray("locationsList", locationsList.toArray(new String[]{}));
		}
	    }
	} catch (Exception ex) {
	    bugCatcher.logError(ex);
	}
    }

    public boolean toggleFavouritesList(ArbilDataNode[] imdiObjectArray, boolean setAsTempate) {
	System.out.println("toggleFavouriteList: " + setAsTempate);
	if (setAsTempate) {
	    boolean selectionNeedsSave = false;
	    for (ArbilDataNode currentImdiObject : imdiObjectArray) {
		if (currentImdiObject.getNeedsSaveToDisk(false)) {
		    selectionNeedsSave = true;
		}
	    }
	    if (selectionNeedsSave) {
		messageDialogHandler.addMessageDialogToQueue("Changes must be saved before adding to the favourites.", "Add Favourites");
		return false;
	    }
	}
	for (ArbilDataNode currentImdiObject : imdiObjectArray) {
	    if (setAsTempate && currentImdiObject.isEmptyMetaNode()) {
		// don't add this node, but do add its children (if there are any)
		if (currentImdiObject.getChildArray().length > 0) {
		    toggleFavouritesList(currentImdiObject.getChildArray(), true);
		}
	    } else {
		if (setAsTempate) {
		    addAsFavourite(currentImdiObject.getURI());
		} else {
		    removeFromFavourites(currentImdiObject.getURI());
		    // TODO: remove from any tables and update the tree roots
//                currentImdiObject.setFavouriteStatus(false);
		}
	    }
	}
	return true;
    }

    private void addAsFavourite(URI imdiUri) {
	try {
	    URI baseUri = new URI(imdiUri.toString().split("#")[0]);
	    String fileSuffix = imdiUri.getPath().substring(imdiUri.getPath().lastIndexOf("."));
	    File destinationFile = File.createTempFile("fav-", fileSuffix, sessionStorage.getFavouritesDir());
	    ArbilDataNode.getMetadataUtils(baseUri.toString()).copyMetadataFile(baseUri, destinationFile, null, true);

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
	    treeHelper.addLocation(favouriteUri);
	    treeHelper.applyRootLocations();
	} catch (Exception ex) {
	    bugCatcher.logError(ex);
	}
    }

    private void removeFromFavourites(URI imdiUri) {
	treeHelper.removeLocation(imdiUri);
	treeHelper.applyRootLocations();
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
	if (targetNodeUserObject instanceof ArbilDataNode) {
	    ArbilDataNode targetImdiObject = (ArbilDataNode) targetNodeUserObject;
	    boolean targetIsCorpus = targetImdiObject.isCorpus();
	    boolean targetIsSession = targetImdiObject.isSession();
	    boolean targetIsImdiChild = targetImdiObject.isChildNode();
	    for (ArbilDataNode currentFavouritesObject : treeHelper.getFavouriteNodes()) {
		boolean addThisFavourites = false;
		if (targetIsCorpus && !currentFavouritesObject.isChildNode()) {
		    addThisFavourites = true;
		} else if (targetIsSession && currentFavouritesObject.isChildNode()) {
		    addThisFavourites = MetadataReader.getSingleInstance().nodeCanExistInNode(targetImdiObject, currentFavouritesObject);
		} else if (targetIsImdiChild && currentFavouritesObject.isChildNode()) {
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

    public String getNodeType(ArbilDataNode favouriteImdiObject, ArbilDataNode targetImdiObject) {
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
	} else if (favouriteImdiObject.isChildNode()) {
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