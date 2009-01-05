/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.util.Hashtable;

/**
 * Document   : ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author petwit
 */
public class ImdiLoader {

    private Hashtable imdiHashTable = new Hashtable();

    public ImdiTreeObject getImdiObject(String localNodeText, String localUrlString) {
        ImdiTreeObject currentImdiObject = null;
        if (localUrlString.length() > 0) {
            currentImdiObject = (ImdiTreeObject) imdiHashTable.get(localUrlString);
        }
        if (currentImdiObject == null) {
            currentImdiObject = new ImdiTreeObject(localNodeText, localUrlString);
            imdiHashTable.put(localUrlString, currentImdiObject);
        }
        return currentImdiObject;
    }
}
