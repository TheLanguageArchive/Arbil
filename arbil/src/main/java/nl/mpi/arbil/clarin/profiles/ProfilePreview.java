package nl.mpi.arbil.clarin.profiles;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JDialog;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Document : ProfilePreview
 * Created on : Jun 22, 2010, 2:51:03 PM
 * Author : Peter Withers
 */
public class ProfilePreview {

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
        sessionStorage = sessionStorageInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
        dataNodeLoader = dataNodeLoaderInstance;
    }

    public String schemaToTreeView(String uriString) {
        String returnString = "";
        //ArbilTemplateManager.getSingleInstance().getCmdiTemplate(returnString);
        try {
            File tempFile = File.createTempFile("ArbilPreview", ".cmdi", sessionStorage.getApplicationSettingsDirectory());
            tempFile.deleteOnExit();
            ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
            URI addedNodePath = componentBuilder.createComponentFile(tempFile.toURI(), new URI(uriString), true);
            ArbilDataNode demoNode = dataNodeLoader.getArbilDataNode(null, addedNodePath);
//            ImdiTreeObject demoNode = ImdiLoader.getSingleInstance().getImdiObject(null, new URI("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/sign_language.imdi"));
            demoNode.waitTillLoaded();
            //add(new LinkTree("tree", myTreeModel));
            //DefaultMutableTreeNode demoTreeNode = new DefaultMutableTreeNode(demoNode);
            DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(demoNode);
            //rootTreeNode.add(demoTreeNode);
            DefaultTreeModel demoTreeModel = new DefaultTreeModel(rootTreeNode, true);
            ArbilTree demoTree = new ArbilTree();
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
