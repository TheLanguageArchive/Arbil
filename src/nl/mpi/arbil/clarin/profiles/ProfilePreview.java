package nl.mpi.arbil.clarin.profiles;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JDialog;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.ui.ImdiTree;
import nl.mpi.arbil.userstorage.LinorgSessionStorage;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : ProfilePreview
 *  Created on : Jun 22, 2010, 2:51:03 PM
 *  Author     : Peter Withers
 */
public class ProfilePreview {

    public String schemaToTreeView(String uriString) {
        String returnString = "";
        //ArbilTemplateManager.getSingleInstance().getCmdiTemplate(returnString);
        try {
            File tempFile = File.createTempFile("ArbilPreview", ".cmdi", LinorgSessionStorage.getSingleInstance().storageDirectory);
            tempFile.deleteOnExit();
            ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
            URI addedNodePath = componentBuilder.createComponentFile(tempFile.toURI(), new URI(uriString), true);
            ImdiTreeObject demoNode = ImdiLoader.getSingleInstance().getImdiObject(null, addedNodePath);
//            ImdiTreeObject demoNode = ImdiLoader.getSingleInstance().getImdiObject(null, new URI("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/sign_language.imdi"));
            demoNode.waitTillLoaded();
            //add(new LinkTree("tree", myTreeModel)); 
            //DefaultMutableTreeNode demoTreeNode = new DefaultMutableTreeNode(demoNode);
            DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
            //rootTreeNode.add(demoTreeNode);
            DefaultTreeModel demoTreeModel = new DefaultTreeModel(rootTreeNode, true);
            ImdiTree demoTree = new ImdiTree();
            demoTree.rootNodeChildren = new ImdiTreeObject[]{demoNode};
            demoTree.setModel(demoTreeModel);
            demoTree.requestResort();
            JDialog demoDialogue = new JDialog();
            demoDialogue.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            demoDialogue.pack();
            demoDialogue.setContentPane(demoTree);
            demoDialogue.setVisible(true);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            ///GuiHelper.linorgBugCatcher.logError(exception);
        } catch (URISyntaxException exception) {
            System.out.println(exception.getMessage());
            //GuiHelper.linorgBugCatcher.logError(exception);
        }
        return returnString;
    }

    public static void main(String[] args) {
        new ProfilePreview().schemaToTreeView("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438166/xsd");
    }
}
