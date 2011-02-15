package nl.mpi.arbil.data;

import java.util.Comparator;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * ImdiTreeNodeSorter.java
 * Created on Aug 11, 2009, 11:08:48 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilNodeSorter implements Comparator {

    public int compare(Object object1, Object object2) {
        Object userObject1;
        Object userObject2;
        if (object1 instanceof DefaultMutableTreeNode && object2 instanceof DefaultMutableTreeNode) {
            userObject1 = ((DefaultMutableTreeNode) object1).getUserObject();
            userObject2 = ((DefaultMutableTreeNode) object2).getUserObject();
        } else if (object1 instanceof ArbilNodeObject && object2 instanceof ArbilNodeObject) {
            userObject1 = ((ArbilNodeObject) object1);
            userObject2 = ((ArbilNodeObject) object2);
        } else {
            throw new IllegalArgumentException("not a DefaultMutableTreeNode object");
        }
        if (userObject1 instanceof ArbilNodeObject && userObject2 instanceof ArbilNodeObject) {
            int typeIdex1 = getTypeIndex((ArbilNodeObject) userObject1);
            int typeIdex2 = getTypeIndex((ArbilNodeObject) userObject2);
            // sort by catalogue then corpus then session etc. then by the text order
            if (typeIdex1 == typeIdex2) {
                int resultInt = userObject1.toString().compareToIgnoreCase(((ArbilNodeObject) userObject2).toString());
                if (resultInt == 0) { // make sure that to objects dont get mistaken to be the same just because the string lebels are the same
                    resultInt = ((ArbilNodeObject) userObject1).getUrlString().compareToIgnoreCase(((ArbilNodeObject) userObject2).getUrlString());
                }
                return resultInt;
            } else {
                return typeIdex1 - typeIdex2;
            }
        } else {
            //return userObject1.toString().compareToIgnoreCase(object2.toString());
            throw new IllegalArgumentException("not a ImdiTreeObject object: " + object1.toString() + " : " + object2.toString());
        }
    }

    private int getTypeIndex(ArbilNodeObject targetImdiObject) {
        if (targetImdiObject.isInfoLink) {
            return 1;
        }
        if (targetImdiObject.imdiDataLoaded) { // caution: this sort can cause the tree to collapse when nodes reload because the nodes will be removed if not in order
            if (targetImdiObject.isCorpus()) {
                return 2;
            } else if (targetImdiObject.isCatalogue()) {
                return 3;
            } else if (targetImdiObject.isSession()) {
                return 4;
            } else if (targetImdiObject.isImdiChild()) {
                return 5;
            } else if (targetImdiObject.isDirectory()) {
                return 6;
            } else {
                return 7;
            }
        } else {
            // put the loading nodes at the end to help the tree sorting and rendering process
            return 10;
        }
    }
}
