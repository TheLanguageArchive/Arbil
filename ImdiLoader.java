/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author petwit
 */
public class ImdiLoader {

    private boolean continueThread = false;
    private Vector imdiNodesToInit = new Vector();
    private Hashtable imdiHashTable = new Hashtable();
    private Vector nodesNeedingSave = new Vector();

    public ImdiLoader() {
        System.out.println("ImdiLoader init");
        continueThread = true;
        new Thread() {

            public void run() {
                while (continueThread) {
                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException ie) {
                        GuiHelper.linorgBugCatcher.logError(ie);
                    //System.err.println("run ImdiLoader: " + ie.getMessage());
                    }
                    while (imdiNodesToInit.size() > 0) {
                        ImdiTreeObject currentImdiObject = (ImdiTreeObject) imdiNodesToInit.remove(0);
                        System.out.println("run ImdiLoader processing: " + currentImdiObject.getUrlString());
                        currentImdiObject.loadImdiDom();
                        currentImdiObject.isLoading = false;
                        currentImdiObject.clearIcon();
                    }
//                    for (Enumeration nodesToCheck = imdiNodesToInit.elements(); nodesToCheck.hasMoreElements();) {
//                        ImdiTreeObject currentImdiObject = (ImdiTreeObject) nodesToCheck.nextElement();
//                        System.out.println("run ImdiLoader processing: " + currentImdiObject.getUrlString());
//                        currentImdiObject.loadImdiDom(false);
//                        currentImdiObject.clearIcon();
//                        imdiNodesToInit.remove(currentImdiObject);
//                    }
                }
            }
        }.start();
    }

    public ImdiTreeObject getImdiObject(String localNodeText, String localUrlString) {
//        System.out.println("getImdiObject: " + localNodeText + " : " + localUrlString);
        ImdiTreeObject currentImdiObject = null;
        if (localUrlString.length() > 0) {
            // correct any variations in the url string
            localUrlString = new ImdiTreeObject(localNodeText, localUrlString).getUrlString();
            currentImdiObject = (ImdiTreeObject) imdiHashTable.get(localUrlString);
            if (currentImdiObject == null) {
                currentImdiObject = new ImdiTreeObject(localNodeText, localUrlString);
                imdiHashTable.put(localUrlString, currentImdiObject);
                if (ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString()) || ImdiTreeObject.isStringImdiHistoryFile(currentImdiObject.getUrlString())) {
                    currentImdiObject.isLoading = true;
                    imdiNodesToInit.add(currentImdiObject);
                }
            }
        }
        return currentImdiObject;
    }

    @Override
    protected void finalize() throws Throwable {
        // stop the thread
        continueThread = false;
        super.finalize();
    }

    public void addNodeNeedingSave(ImdiTreeObject nodeToSave) {
        if (!nodesNeedingSave.contains(nodeToSave)) {
            System.out.println("addNodeNeedingSave: " + nodeToSave);
            nodesNeedingSave.add(nodeToSave);
        }
    }

    public void removeNodesNeedingSave(ImdiTreeObject savedNode) {
        System.out.println("removeNodesNeedingSave: " + savedNode);
        nodesNeedingSave.remove(savedNode);
    }

    public boolean nodesNeedSave() {
        return nodesNeedingSave.size() > 0;
    }

    public void saveNodesNeedingSave() {
        while (nodesNeedingSave.size() > 0){
            ((ImdiTreeObject)nodesNeedingSave.get(0)).saveChangesToCache(); // saving removes the node from the nodesNeedingSave vector via removeNodesNeedingSave
        }
    }
}
