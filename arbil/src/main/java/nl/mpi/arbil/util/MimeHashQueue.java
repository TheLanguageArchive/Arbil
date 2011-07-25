package nl.mpi.arbil.util;

import java.net.URISyntaxException;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilDataNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.bcarchive.typecheck.DeepFileType;
import nl.mpi.bcarchive.typecheck.FileType;

/**
 * Resource nodes can be added to this queue. A low priority thread then
 * processes the queue and sets file meta data, such as MIME type, EXIF data
 * and file size. MIME type is being cached on disk, as determining it is rather
 * costly and should only be done when it is unknown or has potentially changed.
 *
 * Document   : MimeHashQueue
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class MimeHashQueue {

    /**
     * @return the fileType
     */
    private static synchronized FileType getFileType() {
	if (fileType == null) {
	    try {
		File configFile = sessionStorage.getTypeCheckerConfig();
		if (configFile != null) {
		    // User user's custom configuration
		    fileType = new FileType(configFile);
		}
	    } catch (Exception ex) {
		messageDialogHandler.addMessageDialogToQueue("A custom typechecker file types configuration was found. However, it cannot be processed, therefore the default configuration will be used.", "Type checker configuration error");
		bugCatcher.logError("Error while retrieving or applying file checker configuration. Using default configuration.", ex);
	    }
	    if (fileType == null) {
		// Use default (included) configuration
		fileType = new FileType();
	    }
	    fileType._cmdlineMode = true; // Not preferred but we've got to find out what's causing the typechecker message: null issues
	}
	return fileType;
    }
    // stored across sessions
    private Hashtable/*<String, Long>*/ processedFilesMTimes; // make this a vector and maybe remove or maybe make file path and file mtime
    private Hashtable<String, String[]> knownMimeTypes; // imdi path/file path, mime type : maybe sould only be file path
    private Hashtable<String, Vector<String>> md5SumToDuplicates;
    private Hashtable<String, String> pathToMd5Sums;
    // not stored across sessions
    private final Vector<ArbilDataNode> dataNodeQueue = new Vector();
//    private Hashtable<String, ImdiTreeObject> currentlyLoadedImdiObjects;
    private boolean continueThread = false;
    static private MimeHashQueue singleInstance = null;
    private boolean checkResourcePermissions = true;
    private static boolean allowCookies = false; // this is a silly place for this and should find a better home, but the cookies are only dissabled for the permissions test in this class
    private Thread mimeHashQueueThread;
    private static FileType fileType;  //  used to check the file type
    private static DeepFileType deepFileType = new DeepFileType();
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }

    static synchronized public MimeHashQueue getSingleInstance() {
//        System.out.println("MimeHashQueue getSingleInstance");
	if (singleInstance == null) {
	    if (!allowCookies) {
		CookieHandler.setDefault(new ShibCookieHandler());
	    }
	    singleInstance = new MimeHashQueue();
	    singleInstance.startMimeHashQueueThread();
//            System.out.println("CookieHandler: " + java.net.CookieHandler.class.getResource("/META-INF/MANIFEST.MF"));
//            System.out.println("CookieHandler: " + java.net.CookieHandler.class.getResource("/java/net/CookieHandler.class"));
	}
	return singleInstance;
    }

    public MimeHashQueue() {
	System.out.println("MimeHashQueue init");
	checkResourcePermissions = sessionStorage.loadBoolean("checkResourcePermissions", true);
	continueThread = true;
    }

    @Override
    protected void finalize() throws Throwable {
	// stop the thread
	continueThread = false;
//        // save to disk
//        saveMd5sumIndex(); // this is called by guihelper
	//        ImdiTreeObject.mimeHashQueue.saveMd5sumIndex();
	super.finalize();
    }

    /**
     * Adds a node to the queue for processing. Only nodes with resources will
     * actually be processed
     * @param dataNode Data node to be processed
     */
    public void addToQueue(ArbilDataNode dataNode) {
	System.out.println("MimeHashQueue addToQueue: " + dataNode.getUrlString());
	// TODO: when removing a directory from the local woking directories or deleting a resource all records of the file should be removed from the objects in this class to prevent bloating
	if (((dataNode.isLocal() && !dataNode.isMetaDataNode() && !dataNode.isDirectory()) || (dataNode.isChildNode() && dataNode.hasResource()))) {
//            System.out.println("addToQueue: " + getFilePath(imdiObject));
//            System.out.println("addToQueue session: " + imdiObject.isSession());
//            System.out.println("addToQueue directory: " + imdiObject.isDirectory());
//            System.out.println("addToQueue: " + getFilePath(imdiObject));
//            if (new File(new URL(getFilePath(imdiObject)).getFile().exists()) {// here also check that the destination file exists

	    synchronized (dataNodeQueue) {
		if (!dataNodeQueue.contains(dataNode)) {
//                imdiObject.updateLoadingState(+1); // Loading state change dissabled due to performance issues when offline
		    dataNodeQueue.add(dataNode);
		    dataNodeQueue.notifyAll();
		}
	    }
	}
    }

    /**
     * Makes sure the mime hash queue thread is started
     */
    private synchronized void startMimeHashQueueThread() {
	if (mimeHashQueueThread == null) {
	    mimeHashQueueThread = new Thread(new MimeHashQueueRunner(), "MimeHashQueue");
	    mimeHashQueueThread.setPriority(Thread.MIN_PRIORITY);
	    mimeHashQueueThread.start();
	}
    }

    /**
     * Runnable that processes the nodes in the hash queue
     */
    private class MimeHashQueueRunner implements Runnable {

	private boolean changedSinceLastSave = false;
	private ArbilDataNode currentDataNode;
	private int serverPermissionsChecked = 0;

	public void run() {
	    System.out.println("MimeHashQueue run");
	    // load from disk
	    loadMd5sumIndex();

	    while (continueThread) {
		waitForNode();
		try {
		    processQueue();
		} catch (Exception ex) {
		    bugCatcher.logError(ex);
		}
		//TODO: take one file from the list and check it is still there and that it has the same mtime and maybe check the md5sum
		//TODO: when deleting resouce or removing a session or corpus branch containing a session check for links
		// TODO: add check for url in list with different hash which would indicate a modified file and require a red x on the icon
		// TODO: add check for mtime change and update accordingly
	    }
	    System.out.println("MimeHashQueue stop");
	}

	private void waitForNode() {
	    // Wait for nodes to appear in the queue
	    synchronized (dataNodeQueue) {
		try {
		    while (dataNodeQueue.isEmpty()) {
			dataNodeQueue.wait(60 * 1000);
			if (dataNodeQueue.isEmpty()) {
			    // Had nothing to do and queue still empty. Check and save changes
			    checkSaveChanges();
			    // Now wait until notification, no changes guaranteed so no need to save
			    dataNodeQueue.wait();
			}
		    }
		} catch (InterruptedException ie) {
		    bugCatcher.logError(ie);
		}
	    }
	}

	private void checkSaveChanges() {
	    if (changedSinceLastSave) {
		saveMd5sumIndex();
		changedSinceLastSave = false;
	    }
	}

	private void processQueue() {
	    while (!dataNodeQueue.isEmpty()) {
		// Fetch node from queue
		synchronized (dataNodeQueue) {
		    currentDataNode = dataNodeQueue.remove(0);
		}
		//System.out.println("MimeHashQueue checking: " + currentImdiObject.getUrlString());
		if (!currentDataNode.isMetaDataNode()) {
		    System.out.println("checking file and exif");
		    addFileAndExifFields();
		}
		if (currentDataNode.hasResource() && !currentDataNode.hasLocalResource()) {
		    System.out.println("checking server permissions " + serverPermissionsChecked++);
		    checkServerPermissions();
		} else {
		    checkMimeTypeForCurrentNode();
		}
//                        currentImdiObject.updateLoadingState(-1); // Loading state change dissabled due to performance issues when offline
		currentDataNode.clearIcon();
	    }
	}

	private void checkMimeTypeForCurrentNode() {

	    System.out.println("checking mime type etc");
	    URI currentPathURI = getNodeURI(currentDataNode);
	    if (currentPathURI != null && currentPathURI.toString().length() > 0) {
//                                try {
		// check if this file has been process before and then check its mtime
		File currentFile = new File(currentPathURI);
		if (currentFile.exists()) {
		    long previousMTime = 0;
		    if (processedFilesMTimes.containsKey(currentPathURI.toString())) {
			previousMTime = (Long) processedFilesMTimes.get(currentPathURI.toString());
		    }
		    long currentMTime = currentFile.lastModified();
//                                System.out.println("run MimeHashQueue mtime: " + currentPathString);
		    String[] lastCheckedMimeArray = knownMimeTypes.get(currentPathURI.toString());

		    final Object currentNodeObject = currentDataNode;
		    synchronized (currentNodeObject) {
			if (previousMTime != currentMTime || lastCheckedMimeArray == null) {
//                                    System.out.println("run MimeHashQueue processing: " + currentPathString);
			    currentDataNode.setMimeType(getMimeType(currentPathURI));
			    currentDataNode.hashString = getHash(currentPathURI, currentDataNode.getURI());
			    processedFilesMTimes.put(currentPathURI.toString(), currentMTime); // avoid issues of the file being modified between here and the last mtime check
			    changedSinceLastSave = true;
			} else {
			    currentDataNode.hashString = pathToMd5Sums.get(currentPathURI.toString());
			    currentDataNode.setMimeType(lastCheckedMimeArray);
			}
			updateAutoFields(currentDataNode, currentFile);
			updateIconsToMatchingFileNodes(currentPathURI); //for each node relating to the found sum run getMimeHashResult() or quivalent to update the nodes for the found md5
		    }
		}
//                                } catch (MalformedURLException e) {
//                                    //bugCatcher.logError(currentPathString, e);
//                                    System.out.println("MalformedURLException: " + currentPathString + " : " + e);
//                                }
	    }
	}

	private void addFileAndExifFields() {
	    if (!currentDataNode.isMetaDataNode()) {
		File fileObject = currentDataNode.getFile();
		if (fileObject != null && fileObject.exists()) {
		    try {
//TODO: consider adding the mime type field here as a non mull value and updating it when available so that the field order is tidy
			int currentFieldId = 1;
			ArbilField sizeField = new ArbilField(currentFieldId++, currentDataNode, "Size", getFileSizeString(fileObject), 0);
			currentDataNode.addField(sizeField);
			// add the modified date
			Date mtime = new Date(fileObject.lastModified());
			String mTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mtime);
			ArbilField dateField = new ArbilField(currentFieldId++, currentDataNode, "last modified", mTimeString, 0);
			currentDataNode.addField(dateField);
			// get exif tags
//                System.out.println("get exif tags");
			ArbilField[] exifFields = new BinaryMetadataReader().getExifMetadata(currentDataNode, currentFieldId);
			for (ArbilField currentField : exifFields) {
			    currentDataNode.addField(currentField);
//                    System.out.println(currentField.fieldValue);
			}
		    } catch (Exception ex) {
			bugCatcher.logError(currentDataNode.getUrlString() + "\n" + fileObject.getAbsolutePath(), ex);
		    }
		}
	    }
	}

	private void checkServerPermissions() {
	    if (checkResourcePermissions) {
		try {
//            System.out.println("imdiObject: " + imdiObject);
		    HttpURLConnection resourceConnection = (HttpURLConnection) currentDataNode.getFullResourceURI().toURL().openConnection();
		    resourceConnection.setRequestMethod("HEAD");
		    resourceConnection.setRequestProperty("Connection", "Close");
//            System.out.println("conn: " + resourceConnection.getURL());
		    currentDataNode.resourceFileServerResponse = resourceConnection.getResponseCode();
		    if (currentDataNode.resourceFileServerResponse == HttpURLConnection.HTTP_NOT_FOUND
			    || currentDataNode.resourceFileServerResponse == HttpURLConnection.HTTP_FORBIDDEN
			    || currentDataNode.resourceFileServerResponse == HttpURLConnection.HTTP_MOVED_PERM // Many not founds turn up as a 301
			    ) {
			currentDataNode.fileNotFound = true;
		    } else {
			currentDataNode.fileNotFound = false;
		    }
//            System.out.println("ResponseCode: " + resourceConnection.getResponseCode());
		} catch (Exception e) {
		    System.err.println(e.getMessage());
		}
	    }
	}

	private void loadMd5sumIndex() {
	    System.out.println("MimeHashQueue loadMd5sumIndex");
	    try {
		knownMimeTypes = (Hashtable<String, String[]>) sessionStorage.loadObject("knownMimeTypesV2");
		pathToMd5Sums = (Hashtable<String, String>) sessionStorage.loadObject("pathToMd5Sums");
		md5SumToDuplicates = (Hashtable<String, Vector<String>>) sessionStorage.loadObject("md5SumToDuplicates");
		processedFilesMTimes = (Hashtable/*<String, Long>*/) sessionStorage.loadObject("processedFilesMTimesV2");
		System.out.println("loaded md5 and mime from disk");
	    } catch (Exception ex) {
		knownMimeTypes = new Hashtable<String, String[]>();
		pathToMd5Sums = new Hashtable<String, String>();
		processedFilesMTimes = new Hashtable/*<String, Long>*/();
		md5SumToDuplicates = new Hashtable<String, Vector<String>>();
		System.out.println("load loadMd5sumIndex failed: " + ex.getMessage());
	    }
	}

	private void saveMd5sumIndex() {
	    System.out.println("MimeHashQueue saveMd5sumIndex");
	    try {
		sessionStorage.saveObject(knownMimeTypes, "knownMimeTypesV2");
		sessionStorage.saveObject(pathToMd5Sums, "pathToMd5Sums");
		sessionStorage.saveObject(processedFilesMTimes, "processedFilesMTimesV2");
		sessionStorage.saveObject(md5SumToDuplicates, "md5SumToDuplicates");
		System.out.println("saveMd5sumIndex");
	    } catch (IOException ex) {
		bugCatcher.logError(ex);
//            System.out.println("saveMap exception: " + ex.getMessage());
	    }
	}

	private void updateAutoFields(ArbilDataNode currentDataNode, File resourceFile) {
	    // Copy into array to prevent concurrency issues
	    String[] currentNodeFieldNames = currentDataNode.getFields().keySet().toArray(new String[0]);

	    // loop over the auto fields from the template
	    for (String[] autoFields : currentDataNode.getNodeTemplate().autoFieldsArray) {
		String fieldPath = autoFields[0];
		String fileAttribute = autoFields[1];
		String autoValue = null;
		if (fileAttribute.equals("Size")) {
		    if (!currentDataNode.resourceFileNotFound()) {
			autoValue = getFileSizeString(resourceFile);
		    }
		} else if (fileAttribute.equals("MpiMimeType")) {
		    autoValue = currentDataNode.mpiMimeType;
		} else if (fileAttribute.equals("FileType")) {
		    autoValue = FileType.resultToMimeType(currentDataNode.typeCheckerMessage);
		    // todo: consider checking that the mime type matches the node type such as written resource or media file, such that a server sending html (with 200 response) rather than a media file would be discovered, although that could only be detected for media files not written resources
		    if (autoValue != null) {
			int indexOfChar = autoValue.indexOf("/");
			if (indexOfChar > 0) {
			    autoValue = autoValue.substring(0, indexOfChar); // TODO: does the type checker not provide this???
			}
		    }
		}
//            if (autoValue == null) {
//                autoValue = ""; // clear any fields that have no new data but may be out of date
//            }
		if (autoValue != null) {
		    // loop over the field names in the imdi tree node
		    for (String currentKeyString : currentNodeFieldNames) {
			// look for the field name at the end of the auto field path
			if (fieldPath.endsWith(currentKeyString)) {
			    ArbilField[] currentFieldArray = currentDataNode.getFields().get(currentKeyString);
			    if (currentFieldArray != null) {
				// verify that the full field path is the same as the auto field path
				if (currentFieldArray[0].getGenericFullXmlPath().equals(fieldPath)) {
				    // set the value of the fields with the requested data
				    // note that there will usually only be one of each so we could just use the first in the array
				    for (ArbilField currentField : currentFieldArray) {
					currentField.setFieldValue(autoValue, true, true);
				    }
				}
			    }
			}
		    }
		}
	    }
	}

	private void updateIconsToMatchingFileNodes(URI currentPathURI) {//for each node relating to the found sum run getMimeHashResult() or quivalent to update the nodes for the found md5
	    int matchesInCache = 0;
	    int matchesLocalFileSystem = 0;
	    int matchesRemote = 0;
	    // get the md5sum from the path
	    String currentMd5Sum = pathToMd5Sums.get(currentPathURI.toString());
	    if (currentMd5Sum != null) {
		// loop the paths for the md5sum
		Vector<String> duplicatesPaths = md5SumToDuplicates.get(currentMd5Sum);
		Vector<ArbilDataNode> relevantDataNodes = new Vector();
		for (Enumeration<String> duplicatesPathEnum = duplicatesPaths.elements(); duplicatesPathEnum.hasMoreElements();) {
		    String currentDupPath = duplicatesPathEnum.nextElement();
		    try {
			File currentFile = new File(new URI(currentDupPath));
			if (currentFile.exists()) { // check that the file still exists and has the same mtime otherwise rescan
			    // get the currently loaded imdiobjects for the paths
			    ArbilDataNode currentDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNodeOnlyIfLoaded(new URI(currentDupPath)); // TODO: is this the file uri or the node uri???
			    if (currentDataNode != null) {
				relevantDataNodes.add(currentDataNode);
			    }
			    if (sessionStorage.pathIsInsideCache(currentFile)) {
				matchesInCache++;
			    } else {
				matchesLocalFileSystem++;
			    }
			    matchesRemote = 0;// TODO: set up the server md5sum query
			}
		    } catch (Exception e) {
		    }
		}
		for (Enumeration<ArbilDataNode> relevantNodeEnum = relevantDataNodes.elements(); relevantNodeEnum.hasMoreElements();) {
		    ArbilDataNode currentDataNode = relevantNodeEnum.nextElement();
		    // update the values
		    currentDataNode.matchesInCache = matchesInCache;
		    currentDataNode.matchesLocalFileSystem = matchesLocalFileSystem;
		    currentDataNode.matchesRemote = matchesRemote;
		    currentDataNode.clearIcon();
		}
	    }
	}

	private String[] getMimeType(URI fileUri) {
//        System.out.println("getMimeType: " + fileUrl);
	    String mpiMimeType;
	    String typeCheckerMessage;
	    // here we also want to check the magic number but the mpi api has a function similar to that so we
	    // use the mpi.api to get the mime type of the file, if the mime type is not a valid archive format the api will return null
	    // because the api uses null to indicate non archivable we cant return other strings
	    mpiMimeType = null;//"unreadable";
	    typeCheckerMessage = null;
	    boolean deep = false;
	    if (!new File(fileUri).exists()) {
//            System.out.println("File does not exist: " + fileUrl);
	    } else {
		InputStream inputStream = null;
		try {
		    // this will choke on strings that look url encoded but are not. because it erroneously decodes them
		    inputStream = fileUri.toURL().openStream();
		    if (inputStream != null) {
//                    String pamperUrl = fileUrl.getFile().replace("//", "/");
			// Node that the type checker will choke if the path includes "//"
			if (deep) {
			    typeCheckerMessage = deepFileType.checkStream(inputStream, fileUri.toString());
			} else {
			    typeCheckerMessage = getFileType().checkStream(inputStream, fileUri.toString());
			}
//                    System.out.println("mpiMimeType: " + typeCheckerMessage);
		    }
		    mpiMimeType = FileType.resultToMPIType(typeCheckerMessage);
		} catch (Exception ioe) {
//                bugCatcher.logError(ioe);
		    System.out.println("Cannot read file at URL: " + fileUri + " ioe: " + ioe.getMessage());
		    bugCatcher.logError(ioe);
		    if (typeCheckerMessage == null) {
			typeCheckerMessage = "I/O Exception: " + ioe.getMessage();
		    }
		} finally {
		    if (inputStream != null) {
			try {
			    inputStream.close();
			} catch (IOException ex) {
			    bugCatcher.logError(ex);
			}
		    }
		}
		System.out.println(mpiMimeType);
	    }
	    String[] resultArray = new String[]{mpiMimeType, typeCheckerMessage};
	    // if non null then it is an archivable file type
//        if (mpiMimeType != null) {
	    knownMimeTypes.put(fileUri.toString(), resultArray);
//        } else {
	    // because the api uses null to indicate non archivable we cant return other strings
	    //knownMimeTypes.put(filePath, "nonarchivable");
//        }
	    return resultArray;
	}

	private String getHash(URI fileUri, URI nodeUri) {
	    long startTime = System.currentTimeMillis();
	    System.out.println("getHash: " + fileUri);
//        File targetFile = new URL(filePath).getFile();
	    String hashString = null;
	    // TODO: add hashes for session links
	    // TODO: organise a way to get the md5 sum of files on the server
	    FileInputStream is = null;
	    try {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		StringBuilder hexString = new StringBuilder();
		is = new FileInputStream(new File(fileUri));
		byte[] buff = new byte[1024];
		byte[] md5sum;
		int i = 0;
		while ((i = is.read(buff)) > 0) {
		    digest.update(buff, 0, i);
		    long downloadDelay = System.currentTimeMillis() - startTime;
//                System.out.println("Download delay: " + downloadDelay);
		    if (downloadDelay > 100) {
			throw new Exception("reading file for md5sum is taking too long (" + downloadDelay + ") skipping the file: " + fileUri);
		    }
		    startTime = System.currentTimeMillis();
		}
		md5sum = digest.digest();
		for (i = 0; i < md5sum.length; ++i) {
		    hexString.append(Integer.toHexString(0x0100 + (md5sum[i] & 0x00FF)).substring(1));
		}
		hashString = hexString.toString();
//                    debugOut("file: " + this.getFile().getAbsolutePath());
//                    debugOut("location: " + getUrl());
//                    debugOut("digest: " + digest.toString());
	    } catch (Exception ex) {
//            bugCatcher.logMessage("getHash: " + targetFile);
//            bugCatcher.logError("getHash: " + fileUrl, ex);
		System.out.println("failed to created hash: " + ex.getMessage());
	    } finally {
		if (is != null) {
		    try {
			is.close();
		    } catch (IOException ioe) {
			System.out.println("Failed to close input stream for: " + fileUri);
		    }
		}
	    }
	    // store the url to node mapping. Note that; in the case of a resource line the session node is mapped against the resource url not the imdichildnode for the file
//                urlToNodeHashtable.put(nodeLocation, this);

//        String filePath = fileUrl.getPath();
	    if (hashString != null) {
		pathToMd5Sums.put(fileUri.toString(), hashString);
		Object matchingNodes = md5SumToDuplicates.get(hashString);
		if (matchingNodes != null) {
//                        debugOut("checking vector for: " + hashString);
		    if (!((Vector) matchingNodes).contains(nodeUri.toString())) {
//                            debugOut("adding to vector: " + hashString);
			Enumeration otherNodesEnum = ((Vector) matchingNodes).elements();
			while (otherNodesEnum.hasMoreElements()) {
			    Object currentElement = otherNodesEnum.nextElement();
			    Object currentNode = processedFilesMTimes.get(currentElement.toString());
			    if (currentNode instanceof ArbilDataNode) {
				//debugOut("updating icon for: " + ((ImdiTreeObject) currentNode).getUrl());
				// clear the icon of the other copies so that they will be updated to indicate the commonality
				System.out.println("Clearing icon for other node: " + currentNode.toString());
				((ArbilDataNode) currentNode).clearIcon();
			    }
			}
			((Vector) matchingNodes).add(fileUri.toString());
		    }
		} else {
		    System.out.println("creating new vector for: " + hashString);
		    Vector nodeVector = new Vector(1);
		    nodeVector.add(nodeUri.toString());
		    md5SumToDuplicates.put(hashString, nodeVector);
		}
	    }
//            }
	    System.out.println("hashString: " + hashString);
	    return hashString;
	}
    }

    private static URI getNodeURI(ArbilDataNode dataNode) {
	if (dataNode.isResourceSet()) {
	    return dataNode.getFullResourceURI();
	} else {
	    // Non-resource data node
	    try {
		// Remove fragment from URI to get reference to actual file
		return new URI(dataNode.getURI().getScheme(), dataNode.getURI().getSchemeSpecificPart(), null);
	    } catch (URISyntaxException ex) {
		bugCatcher.logError(ex);
		return null;
	    }
	}
    }

    private static String getFileSizeString(File targetFile) {
	return (targetFile.length() / 1024) + "KB";
    }

    /**
     * @return Whether resource permissions are checked
     */
    public boolean isCheckResourcePermissions() {
	return checkResourcePermissions;
    }

    /**
     * @param checkResourcePermissions Whether to check resource permissions
     */
    public void setCheckResourcePermissions(boolean checkResourcePermissions) {
	this.checkResourcePermissions = checkResourcePermissions;
    }

    /**
     * @param aAllowCookies the allowCookies to set
     */
    public static void setAllowCookies(boolean aAllowCookies) {
	allowCookies = aAllowCookies;
    }
}
