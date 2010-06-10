package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;

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
        jPanel.add(jLabel);
    }

    private void addDetailLabel(String labelString) {
        JLabel jLabel = new JLabel(truncateString(labelString));
        jLabel.doLayout();
        jPanel.add(jLabel);
    }

    private void addTabbedLabel(String labelString) {
        addDetailLabel(preSpaces + labelString);
    }

    private void addDetailLabel(String prefixString, ImdiField[] tempFieldArray) {
        if (tempFieldArray != null) {
            for (ImdiField tempField : tempFieldArray) {
                String labelString = tempField.toString();
                addTabbedLabel(prefixString + labelString);
            }
        }
    }

    private void addLabelsForImdiObject(ImdiTreeObject tempObject) {
        if (tempObject.isMetaDataNode()) {
            Hashtable<String, ImdiField[]> tempFields = tempObject.getFields();
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
        if (tempObject.getNeedsSaveToDisk()) {
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
                if (((Object[]) targetObject)[0] != null && ((Object[]) targetObject)[0] instanceof ImdiField) {
                    addDetailLabel(((ImdiField) ((Object[]) targetObject)[0]).parentImdi.getNodeTemplate().getHelpStringForField(((ImdiField) ((Object[]) targetObject)[0]).getFullXmlPath()));
                }
            } else if (targetObject instanceof ImdiTreeObject) {
                addIconLabel(targetObject);
                addLabelsForImdiObject((ImdiTreeObject) targetObject);
            } else {
                addDetailLabel(targetObject.toString());
                if (targetObject instanceof ImdiField) {
                    addDetailLabel(((ImdiField) targetObject).parentImdi.getNodeTemplate().getHelpStringForField(((ImdiField) targetObject).getFullXmlPath()));
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
