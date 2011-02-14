package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;

/**
 * Document   : JListToolTip
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
class JListToolTip extends JToolTip {

    JPanel jPanel;
    Object targetObject;
    String preSpaces = "      ";

    public JListToolTip() {
        this.setLayout(new BorderLayout());
        jPanel = new JPanel();
//        jPanel.setMaximumSize(new Dimension(300, 300));
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        add(jPanel, BorderLayout.CENTER);
        jPanel.setBackground(getBackground());
        jPanel.setBorder(getBorder());
        ToolTipManager.sharedInstance().setDismissDelay(100000);
    }

    private String truncateString(String inputString) {
        if (inputString.length() > 100) {
            inputString = inputString.substring(0, 100) + "...";
        }
        return inputString + " ";// add a space to padd the end of the tooltip
    }

    private void addIconLabel(Object tempObject) {
        JLabel jLabel = new JLabel(truncateString(tempObject.toString()));
        if (tempObject instanceof ImdiTreeObject) {
            jLabel.setIcon(((ImdiTreeObject) tempObject).getIcon());
        }        
        jLabel.doLayout();
//     TODO: fix the tool tip text box bounding size //   jPanel.invalidate();
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBackground(getBackground());
        labelPanel.add(jLabel, BorderLayout.CENTER);
        jPanel.add(labelPanel);
    }

    private void addDetailLabel(String labelString) {
        JTextArea jLabel = new JTextArea(labelString); //truncateString();
        jLabel.setEditable(false);
        jLabel.setBackground(getBackground());
        jLabel.doLayout();
        jPanel.add(jLabel);
    }

    private void addTabbedLabel(String labelString) {
        addDetailLabel(preSpaces + labelString);
    }

    private void addDetailLabel(String prefixString, ArbilField[] tempFieldArray) {
        if (tempFieldArray != null) {
            for (ArbilField tempField : tempFieldArray) {
                String labelString = tempField.toString();
                addTabbedLabel(prefixString + labelString);
            }
        }
    }

    private void addLabelsForImdiObject(ImdiTreeObject tempObject) {
        if (tempObject.isMetaDataNode()) {
            Hashtable<String, ArbilField[]> tempFields = tempObject.getFields();
            addDetailLabel("Name: ", tempFields.get("Name"));
            addDetailLabel("Title: ", tempFields.get("Title"));
            addDetailLabel("Description: ", tempFields.get("Description"));
            addTabbedLabel("Template: " + tempObject.getNodeTemplate().getTemplateName());
            addDetailLabel("Format: ", tempFields.get("Format"));
        } else {
            if (!tempObject.isDirectory()) {
                addTabbedLabel("Unattached file");
                if (tempObject.isArchivableFile()) {
                    addTabbedLabel("Archivable file");
                } else {
                    addTabbedLabel("Not archivable");
                    addTabbedLabel("Type checker message:\n\"" + tempObject.typeCheckerMessage + "\"");
                }
            }
        }
        //if (tempObject.matchesInCache + tempObject.matchesLocalFileSystem + tempObject.matchesRemote > 0){

        if (tempObject.hasResource() || (!tempObject.isMetaDataNode() && !tempObject.isDirectory())) {
            addTabbedLabel("Copies in cache: " + tempObject.matchesInCache);
            addTabbedLabel("Copies on local file system: " + tempObject.matchesLocalFileSystem);
            addTabbedLabel("Copies on server: ?"/* + tempObject.matchesRemote*/);
        }

        if (!tempObject.isLocal()) {
            addTabbedLabel("Remote file (read only)");
        } else if (tempObject.hasResource()) {
            if (tempObject.resourceFileNotFound()) {
                addTabbedLabel("Resource file not found");
            }
        } else if (tempObject.isMetaDataNode()) {
            if (tempObject.isEditable()) {
                addTabbedLabel("Local file (editable)");
            } else {
                addTabbedLabel("Local file (read only)");
            }
            if (tempObject.fileNotFound) {
                addTabbedLabel("File not found");
            }
        }
        if (tempObject.hasHistory()) {
            addTabbedLabel("History of changes are available");
        }
        if (tempObject.getNeedsSaveToDisk(true)) {
            addTabbedLabel("Unsaved changes");
        }
        if (tempObject.isFavorite()) {
            addTabbedLabel("Available in the favourites menu");
        }
        if (tempObject.hasSchemaError) {
            addTabbedLabel("Schema validation error (Check XML Conformance for details)");
        }
    }

    public void updateList() {
//        System.out.println("updateList: " + targetObject);
        jPanel.removeAll();
        if (targetObject != null) {
            if (targetObject instanceof Object[]) {
                for (int childCounter = 0; childCounter < ((Object[]) targetObject).length; childCounter++) {
                    addIconLabel(((Object[]) targetObject)[childCounter]);
                }
                if (((Object[]) targetObject)[0] != null && ((Object[]) targetObject)[0] instanceof ArbilField) {
                    addDetailLabel(((ArbilField) ((Object[]) targetObject)[0]).parentImdi.getNodeTemplate().getHelpStringForField(((ArbilField) ((Object[]) targetObject)[0]).getFullXmlPath()));
                }
            } else if (targetObject instanceof ImdiTreeObject) {
                addIconLabel(targetObject);
                addLabelsForImdiObject((ImdiTreeObject) targetObject);
            } else {
                addDetailLabel(targetObject.toString());
                if (targetObject instanceof ArbilField) {
                    addDetailLabel(((ArbilField) targetObject).parentImdi.getNodeTemplate().getHelpStringForField(((ArbilField) targetObject).getFullXmlPath()));
                }
                //JTextField
//                JTextArea jTextArea = new JTextArea();
//                jTextArea.setText(targetObject.toString());
//                jTextArea.setBackground(getBackground());
//                jTextArea.setLineWrap(true);
//                jTextArea.setMaximumSize(new Dimension(300, 300));
//                jTextArea.setColumns(50);
//                jTextArea.doLayout();
//                jPanel.add(jTextArea);
            }
            jPanel.doLayout();
            doLayout();
//            revalidate();
//            validate();
        }
    }

    public String getTipText() {
//        System.out.println("getTipText");
        // return a zero length string to prevent the tooltip text overlaying the custom tip component
        return "";
    }

    public Dimension getPreferredSize() {
        return jPanel.getPreferredSize();
    }

    public void setTartgetObject(Object targetObjectLocal) {
//        System.out.println("setTartgetObject: " + targetObjectLocal);
        targetObject = targetObjectLocal;
    }
}
