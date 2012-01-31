package nl.mpi.arbil.templates;

import java.net.URISyntaxException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader.CmdiResourceLink;
import nl.mpi.arbil.data.ArbilNode;
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

    public boolean toggleFavouritesList(ArbilDataNode[] dataNodeArray, boolean setAsTempate) {
	System.out.println("toggleFavouriteList: " + setAsTempate);
	if (setAsTempate) {
	    boolean selectionNeedsSave = false;
	    for (ArbilDataNode currentNode : dataNodeArray) {
		if (currentNode.getNeedsSaveToDisk(false)) {
		    selectionNeedsSave = true;
		}
	    }
	    if (selectionNeedsSave) {
		messageDialogHandler.addMessageDialogToQueue("Changes must be saved before adding to the favourites.", "Add Favourites");
		return false;
	    }
	}
	for (ArbilDataNode currentNode : dataNodeArray) {
	    if (setAsTempate && currentNode.isContainerNode()) {
		// don't add this node, but do add its children (if there are any)
		if (currentNode.getChildArray().length > 0) {
		    toggleFavouritesList(currentNode.getChildArray(), true);
		}
	    } else {
		if (setAsTempate) {
		    addAsFavourite(currentNode.getURI());
		} else {
		    removeFromFavourites(currentNode.getURI());
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
	    ArbilDataNode.getMetadataUtils(baseUri.toString()).copyMetadataFile(baseUri, destinationFile, makeLinksAbsolute(imdiUri), true);

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

    /**
     * Creates update array for CMDI resource links that need to be made absolute before being put into favorites
     * @param imdiUri 
     * @return update array for resource links with items for links that currently are not absolute. For IMDI will return null.
     */
    private URI[][] makeLinksAbsolute(URI imdiUri) {
	List<URI[]> relativeLinks = null;
	if (ArbilDataNode.isPathCmdi(imdiUri.toString())) {
	    relativeLinks = new ArrayList<URI[]>();
	    ArrayList<CmdiResourceLink> resourceLinks = new CmdiComponentLinkReader().readLinks(imdiUri);
	    for (CmdiResourceLink link : resourceLinks) {
		try {
		    final URI linkUri = link.getLinkUri();
		    if (!linkUri.isAbsolute()) {
			relativeLinks.add(new URI[]{linkUri, link.getResolvedLinkUri()});
		    }
		} catch (URISyntaxException ex) {
		    // resource proxy has syntax error in link. skip
		    bugCatcher.logError(ex);
		}
	    }
	}
	if (relativeLinks != null) {
	    return relativeLinks.toArray(new URI[][]{});
	} else {
	    return null;
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
	if (targetNodeUserObject instanceof ArbilNode) {
	    ArbilDataNode targetDataNode = null;

	    boolean targetIsDataNode = (targetNodeUserObject instanceof ArbilDataNode);
	    boolean targetIsLocalCorpusRoot;
	    if (targetIsDataNode) {
		targetDataNode = (ArbilDataNode) targetNodeUserObject;
		targetIsLocalCorpusRoot = false;
	    } else {
		targetIsLocalCorpusRoot = targetNodeUserObject == ((DefaultMutableTreeNode) treeHelper.getLocalCorpusTreeModel().getRoot()).getUserObject();
	    }

	    boolean targetIsCorpus = targetIsDataNode && targetDataNode.isCorpus();
	    boolean targetIsSession = targetIsDataNode && targetDataNode.isSession();
	    boolean targetIsChildNode = targetIsDataNode && targetDataNode.isChildNode();
	    for (ArbilDataNode currentFavouritesObject : treeHelper.getFavouriteNodes()) {
		boolean addThisFavourites = false;
		if (targetIsLocalCorpusRoot) {
		    // Local corpus root can only contain non-child nodes
		    addThisFavourites = !currentFavouritesObject.isChildNode();
		} else if (targetIsCorpus && !currentFavouritesObject.isChildNode()) {
		    addThisFavourites = true;
		} else if (targetIsSession && currentFavouritesObject.isChildNode()) {
		    addThisFavourites = MetadataReader.getSingleInstance().nodeCanExistInNode(targetDataNode, currentFavouritesObject);
		} else if (targetIsChildNode && currentFavouritesObject.isChildNode()) {
		    addThisFavourites = MetadataReader.getSingleInstance().nodeCanExistInNode(targetDataNode, currentFavouritesObject);
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

    public String getNodeType(ArbilDataNode favouriteNode, ArbilDataNode targetDataNode) {
	// takes the source path and destination path and creates a complete combined path
	// in:
	// .METATRANSCRIPT.Session.MDGroup.Actors.Actor(12).Languages.Language(5)
	// .METATRANSCRIPT.Session.MDGroup.Actors.Actor(27)
	// or in:
	// favouriteXmlPath: .METATRANSCRIPT.Session.MDGroup.Actors.Actor(1)
	// targetXmlPath:.METATRANSCRIPT.Session.MDGroup.Actors.Actor(3)
	// out:
	// .METATRANSCRIPT.Session.MDGroup.Actors.Actor(27).Languages.Language

	String favouriteXmlPath = favouriteNode.getURI().getFragment();
	String targetXmlPath = targetDataNode.getURI().getFragment();
	System.out.println("getNodeType: \nfavouriteXmlPath: " + favouriteXmlPath + "\ntargetXmlPath:" + targetXmlPath);
	String returnValue;
	if (favouriteNode.isSession()) {
	    returnValue = MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session";
	} else if (favouriteNode.isCorpus()) {
	    returnValue = MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Corpus";
	} else if (favouriteNode.isChildNode()) {
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
