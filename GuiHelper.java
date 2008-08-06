/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Button;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author petwit
 */
public class GuiHelper {

    ImdiHelper imdiHelper = new ImdiHelper();
    private Vector selectedNodesList = new Vector();
    private Vector selectedFilesList = new Vector();
    int currentFieldListIndex = 1; // this variable sets the fields from the imdi file that are shown in the grid
    private Vector imdiFieldLists = new Vector();

    public GuiHelper() {
        loadImdiFieldLists();
    }

    public DefaultMutableTreeNode getImdiTreeNode(String urlString) {
        DefaultMutableTreeNode treeNode;
        ImdiHelper.ImdiTreeObject imdiTreeObject = imdiHelper.getTreeNodeObject(urlString);
        treeNode = new DefaultMutableTreeNode(imdiTreeObject);
        treeNode.setAllowsChildren(imdiTreeObject.isImdi() || imdiTreeObject.isDirectory());
        return treeNode;
    }

    public void getImdiChildNodes(DefaultMutableTreeNode itemNode) {
        if (itemNode.getChildCount() == 0) {
            if (String.class != itemNode.getUserObject().getClass()) {
                ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
                if (!imdiTreeObject.isImdi() && !imdiTreeObject.isDirectory()) {
                    System.out.println("file to be opened");
                } else {
                    ImdiHelper.ImdiTreeObject[] childNodes = imdiTreeObject.getChildren(getCurrentFieldArray());
                    for (int childCount = 0; childCount < childNodes.length; childCount++) {
                        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(childNodes[childCount]);
                        treeNode.setAllowsChildren(childNodes[childCount].isImdi() || childNodes[childCount].isDirectory());
                        itemNode.add(treeNode);
                    }
                }
            }
        }
    }
    private String[] previousRowCells; // this is used to add the first row when the table changes from single to multiple mode

    public void addToGridData(DefaultTableModel tableModel, DefaultMutableTreeNode itemNode, JPanel selectedFilesPanel) {
        ImdiHelper.ImdiTreeObject itemImdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
        String hashKey = itemImdiTreeObject.getUrl();
        if (itemImdiTreeObject.isImdi()) {
            String[] rowNames = getCurrentFieldArray();
            System.out.println("rowNames: " + rowNames.toString());
            boolean multipleRowMode = (0 < tableModel.getRowCount());
            // if there is only one node to show then set up the table for single display
            if (!multipleRowMode) {
                // set the column titles only if not already set 
                if (tableModel.getColumnCount() != 2) {
                    tableModel.setColumnCount(2);
                    tableModel.setColumnIdentifiers(new String[]{"Name", "Value"});
                }
                // clear the current rows
                tableModel.setRowCount(0);
            } else {
                // set the column titles only if not already set
                if (tableModel.getColumnCount() != rowNames.length) {
                    tableModel.setColumnCount(rowNames.length);
                    tableModel.setColumnIdentifiers(rowNames);
                    // clear the current rows
                    tableModel.setRowCount(0);
                    tableModel.addRow(previousRowCells);
                }
            }

            if (String.class != itemNode.getUserObject().getClass()) {

                String[] currentRowCells = new String[rowNames.length]; // + 1 /* add one for the url which is not displayed but used to identify the row */];
                for (int rowNameCounter = 0; rowNameCounter < rowNames.length; rowNameCounter++) {
                    if (-1 == rowNames[rowNameCounter].indexOf("(X)")) {
                        String cellValue = itemImdiTreeObject.getField(rowNames[rowNameCounter]);
                        if (cellValue != null && cellValue.length() == 0) {
                            if (!multipleRowMode) {
                                tableModel.addRow(new Object[]{rowNames[rowNameCounter], cellValue});
                            }
                            currentRowCells[rowNameCounter] = cellValue;
                        } else {
                            currentRowCells[rowNameCounter] = "";
                        }
                    }
                }
                //currentRowCells[currentRowCells.length - 1] = hashKey;
                System.out.println("Added node to rows hashtable: " + hashKey);
                System.out.println("selectedRowCells: " + currentRowCells.toString());
                if (multipleRowMode) {
                    // add the current row
                    tableModel.addRow(currentRowCells);
                }
                // store the row index 
                selectedNodesList.add(hashKey);

//                if (!multipleRowMode) {
//                    // add the links
//                    String[] linksArray = imdiHelper.getLinks(itemImdiTreeObject);
//                    if (linksArray != null) {
//                        for (int linkCount = 0; linkCount < linksArray.length /*&& linkCount < 3*/; linkCount++) {
//                            tableModel.addRow(new Object[]{"link:" + linkCount, linksArray[linkCount]});
//                        }
//                    }
//                }
                previousRowCells = (String[]) currentRowCells.clone();
            }
        } else {
            //todo: display non imdi file
            System.out.println("display non imdi file: " + itemImdiTreeObject.getUrl());
            //try {
            if (selectedFilesList.size() > 0) {
                if (selectedFilesList.size() == 1) {
                    // the document is showing so remove it and add an icon for that node
                    selectedFilesPanel.removeAll();
                    selectedFilesPanel.setLayout(new java.awt.FlowLayout());
                    Button button = new Button("Icon:" + selectedFilesPanel.getComponentCount());
                    button.setSize(100, 100);
                    selectedFilesPanel.add(button);
                }
                // add the file to the files panel
                //Button button = new Button("Icon:" + selectedFilesPanel.getComponentCount());
                //button.setSize(100, 100);
                //selectedFilesPanel.add(button);
                selectedFilesPanel.add(new JLabel(new ImageIcon(itemImdiTreeObject.getUrl())), BorderLayout.CENTER);
                System.out.println("added button");
            } else {
                javax.swing.JScrollPane viewerScrollPane = new javax.swing.JScrollPane();
                viewerScrollPane.setName("viewerScrollPane");
                try {
                    if (itemImdiTreeObject.getUrl().endsWith(".gif") || itemImdiTreeObject.getUrl().endsWith(".jpg") || itemImdiTreeObject.getUrl().endsWith(".png")) {
                        JPanel panel = new JPanel(new BorderLayout());
                        panel.add(new JLabel("image", new ImageIcon(itemImdiTreeObject.getUrl()), SwingConstants.CENTER), BorderLayout.CENTER);
                        viewerScrollPane.setViewportView(panel);
                    } else if (itemImdiTreeObject.getUrl().endsWith(".pdf")) {
                        JPanel panel = new JPanel(new BorderLayout());
                        viewerScrollPane.setViewportView(panel);
                        panel.add(new JLabel("PDF"), BorderLayout.NORTH);
                    } else {
                        JTextPane fileViewerPane = new javax.swing.JTextPane();
                        viewerScrollPane.setViewportView(fileViewerPane);
                        fileViewerPane.setName("viewerTextPane");
                        fileViewerPane.setPage(itemImdiTreeObject.getUrl());
                    }

                } catch (Exception ex3) {
                    System.out.println("Could not show page:" + ex3.getMessage());
                }
                selectedFilesPanel.setLayout(new java.awt.BorderLayout());
                selectedFilesPanel.add(viewerScrollPane);
            }
            selectedFilesPanel.validate();
            selectedFilesPanel.repaint();
            selectedFilesList.add(hashKey);
        }
    }

    public DefaultTableModel removeAllFromGridData(DefaultTableModel tableModel, JPanel selectedFilesPanel) {
        selectedFilesPanel.removeAll();
        selectedFilesList.clear();
        tableModel.setRowCount(0);
        selectedNodesList.clear();
        return tableModel;
    }

    public DefaultTableModel removeFromGridData(DefaultTableModel tableModel, DefaultMutableTreeNode itemNode, JPanel selectedFilesPanel) {
        if (String.class != itemNode.getUserObject().getClass()) {
            ImdiHelper.ImdiTreeObject itemImdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
            String hashKey = itemImdiTreeObject.getUrl();
            try {
                if (itemImdiTreeObject.isImdi()) {
                    tableModel.removeRow(selectedNodesList.indexOf(hashKey));
                    selectedNodesList.remove(hashKey);
                } else {
                    selectedFilesPanel.remove(selectedFilesList.indexOf(hashKey));
                    selectedFilesList.remove(hashKey);
                }
            } catch (Exception ex) {
                System.out.println("removeFromGridData failed: " + ex.toString());
            }
        }
        return tableModel;
    }

    public String[] getFieldListLables() {
        String[] labelArray = new String[imdiFieldLists.size()];
        for (int labelCount = 0; labelCount < imdiFieldLists.size(); labelCount++) {
            labelArray[labelCount] = ((Object[]) imdiFieldLists.get(labelCount))[0].toString();
        }
        return labelArray;
    }

    public String[] getCurrentFieldArray() {
        String fieldNamesString = ((Object[]) imdiFieldLists.get(currentFieldListIndex))[1].toString();
        String[] returnArray = fieldNamesString.split(",");
        System.out.println("fieldNamesString: " + fieldNamesString);
        System.out.println("returnArray: " + returnArray.length);
        return (returnArray);
    }

    public void setCurrentFieldListIndex(int currentIndex) {
        currentFieldListIndex = currentIndex;
    //System.out.println("currentFieldListIndex: " + currentFieldListIndex);
    }

    public int getCurrentFieldListIndex() {
        return currentFieldListIndex;
    }

    private void loadImdiFieldLists() {
        imdiFieldLists.add(new Object[]{"Minimal", "Name,Session.Name,Corpus.Name", null});
        imdiFieldLists.add(new Object[]{"Normal", "Name,Session.Name,Corpus.Name,Session.Description,Corpus.Description,Session.Title,Corpus.Title,Session.Date,Session.MDGroup.Location.Continent,Session.MDGroup,Location.Continent,Session.Actors,Session.Actor(1).Name", null});
        imdiFieldLists.add(new Object[]{"Extra", "Name,History,Session.Name,Corpus.Name,Session.Description,Corpus.Description,Session.Title,Corpus.Title,Session.Date,Session.Resources.MediaFile.ResourceLink,Session.MDGroup.Location.Continent", null});
        imdiFieldLists.add(new Object[]{"Everything", "Lexicon.Name,Lexicon.Title,Lexicon.Date,Lexicon.Description,Lexicon.Project.Description,Lexicon.Languages.Description,Lexicon.Content.Description,Lexicon.Actors.Description,Lexicon.Actor.Description,Lexicon.Actor(X).Description,Lexicon.Actor.Languages.Description,Lexicon.Actor(X).Languages.Description,Lexicon.Actor.Languages.Language.Description,Lexicon.Actor(X).Languages.Language.Description,Lexicon.Actor.Languages.Language(X).Description,Lexicon.Actor(X).Languages.Language(X).Description,Lexicon.Resource.MetaLanguages.Description,Lexicon.Resource.Description,Lexicon.Anonyms.Access.Description,Lexicon.References.Description,Lexicon.Entry.HeadWordType,Lexicon.Entry.Orthography,Lexicon.Entry.Morphology,Lexicon.Entry.MorphoSyntax,Lexicon.Entry.Syntax,Lexicon.Entry.Phonology,Lexicon.Entry.Semantics,Lexicon.Entry.Etymology,Lexicon.Entry.Usage,Lexicon.Entry.Frequency,Lexicon.Resource.SubjectLanguages,Lexicon.Resource.SubjectLanguages(X),Lexicon.Resource.DocumentLanguages,Lexicon.Resource.DocumentLanguages(X),Session.Participant.Description,Session.Participant.Description(X),Session.Participant(X).Description,Session.Participant(X).Description(X),SelfHandle,Session,Session.History,Session.Name,Session.Title,Session.Date,Session.Description,Session.Description.LanguageId,Session.Description.Link,Session.Description.Name,Session.Description(X),Session.Description(X).LanguageId,Session.Description(X).Link,Session.Description(X).Name,Session.Location.Continent,Session.Location.Country,Session.Location.Region,Session.Location.Region(X),Session.Location.Address,Session.Location.ExternalResourceReference,Session.Location.ExternalResourceReference.Type,Session.Location.ExternalResourceReference.SubType,Session.Location.ExternalResourceReference.Format,Session.Location.ExternalResourceReference.Link,Session.Location.ExternalResourceReference(X),Session.Location.ExternalResourceReference(X).Type,Session.Location.ExternalResourceReference(X).SubType,Session.Location.ExternalResourceReference(X).Format,Session.Location.ExternalResourceReference(X).Link,Session.Location.Key,Session.Location.Key.Name,Session.Location.Key.Type,Session.Location.Key.Link,Session.Location.Key.DefaultLink,Session.Location.Key(X),Session.Location.Key(X).Name,Session.Location.Key(X).Type,Session.Location.Key(X).Link,Session.Location.Key(X).DefaultLink,Session.Project,Session.Project.Name,Session.Project.Title,Session.Project.Id,Session.Project.Contact.Name,Session.Project.Contact.Address,Session.Project.Contact.Email,Session.Project.Contact.Organisation,Session.Project.Description,Session.Project.Description.LanguageId,Session.Project.Description.Link,Session.Project.Description.Name,Session.Project.Description(X),Session.Project.Description(X).LanguageId,Session.Project.Description(X).Link,Session.Project.Description(X).Name,Session.Project(X),Session.Project(X).Name,Session.Project(X).Title,Session.Project(X).Id,Session.Project(X).Contact.Name,Session.Project(X).Contact.Address,Session.Project(X).Contact.Email,Session.Project(X).Contact.Organisation,Session.Project(X).Description,Session.Project(X).Description.LanguageId,Session.Project(X).Description.Link,Session.Project(X).Description.Name,Session.Project(X).Description(X),Session.Project(X).Description(X).LanguageId,Session.Project(X).Description(X).Link,Session.Project(X).Description(X).Name,Session.Content.Genre,Session.Content.SubGenre,Session.Content.Interactivity,Session.Content.PlanningType,Session.Content.Involvement,Session.Content.SocialContext,Session.Content.EventStructure,Session.Content.Channel,Session.Content.Task,Session.Content.Task(X),Session.Content.Modalities,Session.Content.Modalities(X),Session.Content.Subject,Session.Content.Subject.Type,Session.Content.Subject.DefaultLink,Session.Content.Subject.Link,Session.Content.Subject.Encoding,Session.Content.Subject(X),Session.Content.Subject(X).Type,Session.Content.Subject(X).DefaultLink,Session.Content.Subject(X).Link,Session.Content.Subject(X).Encoding,Session.Content.Languages.Description,Session.Content.Languages.Description.LanguageId,Session.Content.Languages.Description.Link,Session.Content.Languages.Description.Name,Session.Content.Languages.Description(X),Session.Content.Languages.Description(X).LanguageId,Session.Content.Languages.Description(X).Link,Session.Content.Languages.Description(X).Name,Session.Content.Languages.Language,Session.Content.Languages.Language.Id,Session.Content.Languages.Language.ResourceRef,Session.Content.Languages.Language.Name,Session.Content.Languages.Language.Name(X),Session.Content.Languages.Language.MotherTongue,Session.Content.Languages.Language.PrimaryLanguage,Session.Content.Languages.Language.Dominant,Session.Content.Languages.Language.Description,Session.Content.Languages.Language.Description.LanguageId,Session.Content.Languages.Language.Description.Link,Session.Content.Languages.Language.Description.Name,Session.Content.Languages.Language.Description(X),Session.Content.Languages.Language.Description(X).LanguageId,Session.Content.Languages.Language.Description(X).Link,Session.Content.Languages.Language.Description(X).Name,Session.Content.Languages.Language(X),Session.Content.Languages.Language(X).Id,Session.Content.Languages.Language(X).ResourceRef,Session.Content.Languages.Language(X).Name,Session.Content.Languages.Language(X).Name(X),Session.Content.Languages.Language(X).MotherTongue,Session.Content.Languages.Language(X).PrimaryLanguage,Session.Content.Languages.Language(X).Dominant,Session.Content.Languages.Language(X).Description,Session.Content.Languages.Language(X).Description.LanguageId,Session.Content.Languages.Language(X).Description.Link,Session.Content.Languages.Language(X).Description.Name,Session.Content.Languages.Language(X).Description(X),Session.Content.Languages.Language(X).Description(X).LanguageId,Session.Content.Languages.Language(X).Description(X).Link,Session.Content.Languages.Language(X).Description(X).Name,Session.Content.Description,Session.Content.Description.LanguageId,Session.Content.Description.Link,Session.Content.Description.Name,Session.Content.Description(X),Session.Content.Description(X).LanguageId,Session.Content.Description(X).Link,Session.Content.Description(X).Name,Session.Content.Key,Session.Content.Key.Name,Session.Content.Key.Type,Session.Content.Key.Link,Session.Content.Key.DefaultLink,Session.Content.Key(X),Session.Content.Key(X).Name,Session.Content.Key(X).Type,Session.Content.Key(X).Link,Session.Content.Key(X).DefaultLink,Session.Actors.Description,Session.Actors.Description.LanguageId,Session.Actors.Description.Link,Session.Actors.Description.Name,Session.Actors.Description(X),Session.Actors.Description(X).LanguageId,Session.Actors.Description(X).Link,Session.Actors.Description(X).Name,Session.Actor,Session.Actor.ResourceRef,Session.Actor.Role,Session.Actor.FamilySocialRole,Session.Actor.Name,Session.Actor.Name(X),Session.Actor.FullName,Session.Actor.Code,Session.Actor.Languages.Description,Session.Actor.Languages.Description.LanguageId,Session.Actor.Languages.Description.Link,Session.Actor.Languages.Description.Name,Session.Actor.Languages.Description(X),Session.Actor.Languages.Description(X).LanguageId,Session.Actor.Languages.Description(X).Link,Session.Actor.Languages.Description(X).Name,Session.Actor.Language,Session.Actor.Language.Id,Session.Actor.Language.ResourceRef,Session.Actor.Language.Name,Session.Actor.Language.Name(X),Session.Actor.Language.MotherTongue,Session.Actor.Language.PrimaryLanguage,Session.Actor.Language.Dominant,Session.Actor.Language.Description,Session.Actor.Language.Description.LanguageId,Session.Actor.Language.Description.Link,Session.Actor.Language.Description.Name,Session.Actor.Language.Description(X),Session.Actor.Language.Description(X).LanguageId,Session.Actor.Language.Description(X).Link,Session.Actor.Language.Description(X).Name,Session.Actor.Language(X),Session.Actor.Language(X).Id,Session.Actor.Language(X).ResourceRef,Session.Actor.Language(X).Name,Session.Actor.Language(X).Name(X),Session.Actor.Language(X).MotherTongue,Session.Actor.Language(X).PrimaryLanguage,Session.Actor.Language(X).Dominant,Session.Actor.Language(X).Description,Session.Actor.Language(X).Description.LanguageId,Session.Actor.Language(X).Description.Link,Session.Actor.Language(X).Description.Name,Session.Actor.Language(X).Description(X),Session.Actor.Language(X).Description(X).LanguageId,Session.Actor.Language(X).Description(X).Link,Session.Actor.Language(X).Description(X).Name,Session.Actor.EthnicGroup,Session.Actor.Age,Session.Actor.Sex,Session.Actor.Education,Session.Actor.Anonymized,Session.Actor.Contact.Name,Session.Actor.Contact.Address,Session.Actor.Contact.Email,Session.Actor.Contact.Organisation,Session.Actor.Description,Session.Actor.Description.LanguageId,Session.Actor.Description.Link,Session.Actor.Description.Name,Session.Actor.Description(X),Session.Actor.Description(X).LanguageId,Session.Actor.Description(X).Link,Session.Actor.Description(X).Name,Session.Actor.Key,Session.Actor.Key.Name,Session.Actor.Key.Type,Session.Actor.Key.Link,Session.Actor.Key.DefaultLink,Session.Actor.Key(X),Session.Actor.Key(X).Name,Session.Actor.Key(X).Type,Session.Actor.Key(X).Link,Session.Actor.Key(X).DefaultLink,Session.Actor(X),Session.Actor(X).ResourceRef,Session.Actor(X).Role,Session.Actor(X).FamilySocialRole,Session.Actor(X).Name,Session.Actor(X).Name(X),Session.Actor(X).FullName,Session.Actor(X).Code,Session.Actor(X).Languages.Description,Session.Actor(X).Languages.Description.LanguageId,Session.Actor(X).Languages.Description.Link,Session.Actor(X).Languages.Description.Name,Session.Actor(X).Language,Session.Actor(X).Language.Id,Session.Actor(X).Language.ResourceRef,Session.Actor(X).Language.Name,Session.Actor(X).Language.Name(X),Session.Actor(X).Language.MotherTongue,Session.Actor(X).Language.PrimaryLanguage,Session.Actor(X).Language.Dominant,Session.Actor(X).Language.Description,Session.Actor(X).Language.Description.LanguageId,Session.Actor(X).Language.Description.Link,Session.Actor(X).Language.Description.Name,Session.Actor(X).Language.Description(X),Session.Actor(X).Language.Description(X).LanguageId,Session.Actor(X).Language.Description(X).Link,Session.Actor(X).Language.Description(X).Name,Session.Actor(X).Language(X),Session.Actor(X).Language(X).Id,Session.Actor(X).Language(X).ResourceRef,Session.Actor(X).Language(X).Name,Session.Actor(X).Language(X).Name(X),Session.Actor(X).Language(X).MotherTongue,Session.Actor(X).Language(X).PrimaryLanguage,Session.Actor(X).Language(X).Dominant,Session.Actor(X).Language(X).Description,Session.Actor(X).Language(X).Description.LanguageId,Session.Actor(X).Language(X).Description.Link,Session.Actor(X).Language(X).Description.Name,Session.Actor(X).Language(X).Description(X),Session.Actor(X).Language(X).Description(X).LanguageId,Session.Actor(X).Language(X).Description(X).Link,Session.Actor(X).Language(X).Description(X).Name,Session.Actor(X).EthnicGroup,Session.Actor(X).Age,Session.Actor(X).Sex,Session.Actor(X).Education,Session.Actor(X).Anonymized,Session.Actor(X).Contact.Name,Session.Actor(X).Contact.Address,Session.Actor(X).Contact.Email,Session.Actor(X).Contact.Organisation,Session.Actor(X).Description,Session.Actor(X).Description.LanguageId,Session.Actor(X).Description.Link,Session.Actor(X).Description.Name,Session.Actor(X).Description(X),Session.Actor(X).Description(X).LanguageId,Session.Actor(X).Description(X).Link,Session.Actor(X).Description(X).Name,Session.Actor(X).Key,Session.Actor(X).Key.Name,Session.Actor(X).Key.Type,Session.Actor(X).Key.Link,Session.Actor(X).Key.DefaultLink,Session.Actor(X).Key(X),Session.Actor(X).Key(X).Name,Session.Actor(X).Key(X).Type,Session.Actor(X).Key(X).Link,Session.Actor(X).Key(X).DefaultLink,Session.MediaFile,Session.MediaFile.ResourceId,Session.MediaFile.ResourceLink,Session.MediaFile.Type,Session.MediaFile.Size,Session.MediaFile.Format,Session.MediaFile.Quality,Session.MediaFile.RecordingConditions,Session.MediaFile.TimePosition.Start,Session.MediaFile.TimePosition.End,Session.MediaFile.Access.Availability,Session.MediaFile.Access.Description,Session.MediaFile.Access.Description.LanguageId,Session.MediaFile.Access.Description.Link,Session.MediaFile.Access.Description.Name,Session.MediaFile.Access.Description(X),Session.MediaFile.Access.Description(X).LanguageId,Session.MediaFile.Access.Description(X).Link,Session.MediaFile.Access.Description(X).Name,Session.MediaFile.Access.Date,Session.MediaFile.Access.Owner,Session.MediaFile.Access.Publisher,Session.MediaFile.Access.Contact.Name,Session.MediaFile.Access.Contact.Address,Session.MediaFile.Access.Contact.Email,Session.MediaFile.Access.Contact.Organisation,Session.MediaFile.Description,Session.MediaFile.Description.LanguageId,Session.MediaFile.Description.Link,Session.MediaFile.Description.Name,Session.MediaFile.Description(X),Session.MediaFile.Description(X).LanguageId,Session.MediaFile.Description(X).Link,Session.MediaFile.Description(X).Name,Session.MediaFile.Key,Session.MediaFile.Key.Name,Session.MediaFile.Key.Type,Session.MediaFile.Key.Link,Session.MediaFile.Key.DefaultLink,Session.MediaFile.Key(X),Session.MediaFile.Key(X).Name,Session.MediaFile.Key(X).Type,Session.MediaFile.Key(X).Link,Session.MediaFile.Key(X).DefaultLink,Session.MediaFile(X),Session.MediaFile(X).ResourceId,Session.MediaFile(X).ResourceLink,Session.MediaFile(X).Type,Session.MediaFile(X).Size,Session.MediaFile(X).Format,Session.MediaFile(X).Quality,Session.MediaFile(X).RecordingConditions,Session.MediaFile(X).TimePosition.Start,Session.MediaFile(X).TimePosition.End,Session.MediaFile(X).Access.Availability,Session.MediaFile(X).Access.Description,Session.MediaFile(X).Access.Description.LanguageId,Session.MediaFile(X).Access.Description.Link,Session.MediaFile(X).Access.Description.Name,Session.MediaFile(X).Access.Description(X),Session.MediaFile(X).Access.Description(X).LanguageId,Session.MediaFile(X).Access.Description(X).Link,Session.MediaFile(X).Access.Description(X).Name,Session.MediaFile(X).Access.Date,Session.MediaFile(X).Access.Owner,Session.MediaFile(X).Access.Publisher,Session.MediaFile(X).Access.Contact.Name,Session.MediaFile(X).Access.Contact.Address,Session.MediaFile(X).Access.Contact.Email,Session.MediaFile(X).Access.Contact.Organisation,Session.MediaFile(X).Description,Session.MediaFile(X).Description.LanguageId,Session.MediaFile(X).Description.Link,Session.MediaFile(X).Description.Name,Session.MediaFile(X).Description(X),Session.MediaFile(X).Description(X).LanguageId,Session.MediaFile(X).Description(X).Link,Session.MediaFile(X).Description(X).Name,Session.MediaFile(X).Key,Session.MediaFile(X).Key.Name,Session.MediaFile(X).Key.Type,Session.MediaFile(X).Key.Link,Session.MediaFile(X).Key.DefaultLink,Session.MediaFile(X).Key(X),Session.MediaFile(X).Key(X).Name,Session.MediaFile(X).Key(X).Type,Session.MediaFile(X).Key(X).Link,Session.MediaFile(X).Key(X).DefaultLink,Session.WrittenResource,Session.WrittenResource.ResourceId,Session.WrittenResource.ResourceLink,Session.WrittenResource.MediaResourceLink,Session.WrittenResource.Date,Session.WrittenResource.Type,Session.WrittenResource.SubType,Session.WrittenResource.Size,Session.WrittenResource.Format,Session.WrittenResource.Derivation,Session.WrittenResource.ContentEncoding,Session.WrittenResource.CharacterEncoding,Session.WrittenResource.Validation.Type,Session.WrittenResource.Validation.Methodology,Session.WrittenResource.Validation.Level,Session.WrittenResource.Validation.Description,Session.WrittenResource.Validation.Description.LanguageId,Session.WrittenResource.Validation.Description.Link,Session.WrittenResource.Validation.Description.Name,Session.WrittenResource.Validation.Description(X),Session.WrittenResource.Validation.Description(X).LanguageId,Session.WrittenResource.Validation.Description(X).Link,Session.WrittenResource.Validation.Description(X).Name,Session.WrittenResource.LanguageId,Session.WrittenResource.Anonymized,Session.WrittenResource.Access.Availability,Session.WrittenResource.Access.Description,Session.WrittenResource.Access.Description.LanguageId,Session.WrittenResource.Access.Description.Link,Session.WrittenResource.Access.Description.Name,Session.WrittenResource.Access.Description(X),Session.WrittenResource.Access.Description(X).LanguageId,Session.WrittenResource.Access.Description(X).Link,Session.WrittenResource.Access.Description(X).Name,Session.WrittenResource.Access.Date,Session.WrittenResource.Access.Owner,Session.WrittenResource.Access.Publisher,Session.WrittenResource.Access.Contact.Name,Session.WrittenResource.Access.Contact.Address,Session.WrittenResource.Access.Contact.Email,Session.WrittenResource.Access.Contact.Organisation,Session.WrittenResource.Description,Session.WrittenResource.Description.LanguageId,Session.WrittenResource.Description.Link,Session.WrittenResource.Description.Name,Session.WrittenResource.Description(X),Session.WrittenResource.Description(X).LanguageId,Session.WrittenResource.Description(X).Link,Session.WrittenResource.Description(X).Name,Session.WrittenResource.Key,Session.WrittenResource.Key.Name,Session.WrittenResource.Key.Type,Session.WrittenResource.Key.Link,Session.WrittenResource.Key.DefaultLink,Session.WrittenResource.Key(X),Session.WrittenResource.Key(X).Name,Session.WrittenResource.Key(X).Type,Session.WrittenResource.Key(X).Link,Session.WrittenResource.Key(X).DefaultLink,Session.WrittenResource(X),Session.WrittenResource(X).ResourceId,Session.WrittenResource(X).ResourceLink,Session.WrittenResource(X).MediaResourceLink,Session.WrittenResource(X).Date,Session.WrittenResource(X).Type,Session.WrittenResource(X).SubType,Session.WrittenResource(X).Size,Session.WrittenResource(X).Format,Session.WrittenResource(X).Derivation,Session.WrittenResource(X).ContentEncoding,Session.WrittenResource(X).CharacterEncoding,Session.WrittenResource(X).Validation.Type,Session.WrittenResource(X).Validation.Methodology,Session.WrittenResource(X).Validation.Level,Session.WrittenResource(X).Validation.Description,Session.WrittenResource(X).Validation.Description.LanguageId,Session.WrittenResource(X).Validation.Description.Link,Session.WrittenResource(X).Validation.Description.Name,Session.WrittenResource(X).Validation.Description(X),Session.WrittenResource(X).Validation.Description(X).LanguageId,Session.WrittenResource(X).Validation.Description(X).Link,Session.WrittenResource(X).Validation.Description(X).Name,Session.WrittenResource(X).LanguageId,Session.WrittenResource(X).Anonymized,Session.WrittenResource(X).Access.Availability,Session.WrittenResource(X).Access.Description,Session.WrittenResource(X).Access.Description.LanguageId,Session.WrittenResource(X).Access.Description.Link,Session.WrittenResource(X).Access.Description.Name,Session.WrittenResource(X).Access.Description(X),Session.WrittenResource(X).Access.Description(X).LanguageId,Session.WrittenResource(X).Access.Description(X).Link,Session.WrittenResource(X).Access.Description(X).Name,Session.WrittenResource(X).Access.Date,Session.WrittenResource(X).Access.Owner,Session.WrittenResource(X).Access.Publisher,Session.WrittenResource(X).Access.Contact.Name,Session.WrittenResource(X).Access.Contact.Address,Session.WrittenResource(X).Access.Contact.Email,Session.WrittenResource(X).Access.Contact.Organisation,Session.WrittenResource(X).Description,Session.WrittenResource(X).Description.LanguageId,Session.WrittenResource(X).Description.Link,Session.WrittenResource(X).Description.Name,Session.WrittenResource(X).Description(X),Session.WrittenResource(X).Description(X).LanguageId,Session.WrittenResource(X).Description(X).Link,Session.WrittenResource(X).Description(X).Name,Session.WrittenResource(X).Key,Session.WrittenResource(X).Key.Name,Session.WrittenResource(X).Key.Type,Session.WrittenResource(X).Key.Link,Session.WrittenResource(X).Key.DefaultLink,Session.WrittenResource(X).Key(X),Session.WrittenResource(X).Key(X).Name,Session.WrittenResource(X).Key(X).Type,Session.WrittenResource(X).Key(X).Link,Session.WrittenResource(X).Key(X).DefaultLink,Session.LexiconResource,Session.LexiconResource.ResourceLink,Session.LexiconResource.Type,Session.LexiconResource.Format,Session.LexiconResource.Description,Session.LexiconResource.Description.LanguageId,Session.LexiconResource.Description.Link,Session.LexiconResource.Description.Name,Session.LexiconResource.Description(X),Session.LexiconResource.Description(X).LanguageId,Session.LexiconResource.Description(X).Link,Session.LexiconResource.Description(X).Name,Session.LexiconResource(X),Session.LexiconResource(X).ResourceLink,Session.LexiconResource(X).Type,Session.LexiconResource(X).Format,Session.LexiconResource(X).Description,Session.LexiconResource(X).Description.LanguageId,Session.LexiconResource(X).Description.Link,Session.LexiconResource(X).Description.Name,Session.LexiconResource(X).Description(X),Session.LexiconResource(X).Description(X).LanguageId,Session.LexiconResource(X).Description(X).Link,Session.LexiconResource(X).Description(X).Name,Session.Source,Session.Source.Id,Session.Source.ResourceRefs,Session.Source.Format,Session.Source.Quality,Session.Source.TimePosition.Start,Session.Source.TimePosition.End,Session.Source.CounterPosition.Start,Session.Source.CounterPosition.Start,Session.Source.Access.Availability,Session.Source.Access.Description,Session.Source.Access.Description.LanguageId,Session.Source.Access.Description.Link,Session.Source.Access.Description.Name,Session.Source.Access.Description(X),Session.Source.Access.Description(X).LanguageId,Session.Source.Access.Description(X).Link,Session.Source.Access.Description(X).Name,Session.Source.Access.Date,Session.Source.Access.Owner,Session.Source.Access.Publisher,Session.Source.Access.Contact.Name,Session.Source.Access.Contact.Address,Session.Source.Access.Contact.Email,Session.Source.Access.Contact.Organisation,Session.Source.Description,Session.Source.Description.LanguageId,Session.Source.Description.Link,Session.Source.Description.Name,Session.Source.Description(X),Session.Source.Description(X).LanguageId,Session.Source.Description(X).Link,Session.Source.Description(X).Name,Session.Source.Key,Session.Source.Key.Name,Session.Source.Key.Type,Session.Source.Key.Link,Session.Source.Key.DefaultLink,Session.Source.Key(X),Session.Source.Key(X).Name,Session.Source.Key(X).Type,Session.Source.Key(X).Link,Session.Source.Key(X).DefaultLink,Session.Source(X),Session.Source(X).Id,Session.Source(X).ResourceRefs,Session.Source(X).Format,Session.Source(X).Quality,Session.Source(X).TimePosition.Start,Session.Source(X).TimePosition.End,Session.Source(X).CounterPosition.Start,Session.Source(X).CounterPosition.Start,Session.Source(X).Access.Availability,Session.Source(X).Access.Description,Session.Source(X).Access.Description.LanguageId,Session.Source(X).Access.Description.Link,Session.Source(X).Access.Description.Name,Session.Source(X).Access.Description(X),Session.Source(X).Access.Description(X).LanguageId,Session.Source(X).Access.Description(X).Link,Session.Source(X).Access.Description(X).Name,Session.Source(X).Access.Date,Session.Source(X).Access.Owner,Session.Source(X).Access.Publisher,Session.Source(X).Access.Contact.Name,Session.Source(X).Access.Contact.Address,Session.Source(X).Access.Contact.Email,Session.Source(X).Access.Contact.Organisation,Session.Source(X).Description,Session.Source(X).Description.LanguageId,Session.Source(X).Description.Link,Session.Source(X).Description.Name,Session.Source(X).Description(X),Session.Source(X).Description(X).LanguageId,Session.Source(X).Description(X).Link,Session.Source(X).Description(X).Name,Session.Source(X).Key,Session.Source(X).Key.Name,Session.Source(X).Key.Type,Session.Source(X).Key.Link,Session.Source(X).Key.DefaultLink,Session.Source(X).Key(X),Session.Source(X).Key(X).Name,Session.Source(X).Key(X).Type,Session.Source(X).Key(X).Link,Session.Source(X).Key(X).DefaultLink,Session.Anonyms.ResourceLink,Session.Anonyms.Access.Availability,Session.Anonyms.Access.Description,Session.Anonyms.Access.Description.LanguageId,Session.Anonyms.Access.Description.Link,Session.Anonyms.Access.Description.Name,Session.Anonyms.Access.Description(X),Session.Anonyms.Access.Description(X).LanguageId,Session.Anonyms.Access.Description(X).Link,Session.Anonyms.Access.Description(X).Name,Session.Anonyms.Access.Date,Session.Anonyms.Access.Owner,Session.Anonyms.Access.Publisher,Session.Anonyms.Access.Contact.Name,Session.Anonyms.Access.Contact.Address,Session.Anonyms.Access.Contact.Email,Session.Anonyms.Access.Contact.Organisation,Session.References.Description,Session.References.Description.LanguageId,Session.References.Description.Link,Session.References.Description.Name,Session.References.Description(X),Session.References.Description(X).LanguageId,Session.References.Description(X).Link,Session.References.Description(X).Name,Corpus,Corpus.History,Corpus.Name,Corpus.Title,Corpus.Description,Corpus.Description.LanguageId,Corpus.Description.Link,Corpus.Description.Name,Corpus.Description(X),Corpus.Description(X).LanguageId,Corpus.Description(X).Link,Corpus.Description(X).Name,Corpus.Location.Continent,Corpus.Location.Country,Corpus.Location.Region,Corpus.Location.Region(X),Corpus.Location.Address,Corpus.Location.ExternalResourceReference,Corpus.Location.ExternalResourceReference.Type,Corpus.Location.ExternalResourceReference.SubType,Corpus.Location.ExternalResourceReference.Format,Corpus.Location.ExternalResourceReference.Link,Corpus.Location.ExternalResourceReference(X),Corpus.Location.ExternalResourceReference(X).Type,Corpus.Location.ExternalResourceReference(X).SubType,Corpus.Location.ExternalResourceReference(X).Format,Corpus.Location.ExternalResourceReference(X).Link,Corpus.Location.Key,Corpus.Location.Key.Name,Corpus.Location.Key.Type,Corpus.Location.Key.Link,Corpus.Location.Key.DefaultLink,Corpus.Location.Key(X),Corpus.Location.Key(X).Name,Corpus.Location.Key(X).Type,Corpus.Location.Key(X).Link,Corpus.Location.Key(X).DefaultLink,Corpus.Project,Corpus.Project.Name,Corpus.Project.Title,Corpus.Project.Id,Corpus.Project.Contact.Name,Corpus.Project.Contact.Address,Corpus.Project.Contact.Email,Corpus.Project.Contact.Organisation,Corpus.Project.Description,Corpus.Project.Description.LanguageId,Corpus.Project.Description.Link,Corpus.Project.Description.Name,Corpus.Project.Description(X),Corpus.Project.Description(X).LanguageId,Corpus.Project.Description(X).Link,Corpus.Project.Description(X).Name,Corpus.Project(X),Corpus.Project(X).Name,Corpus.Project(X).Title,Corpus.Project(X).Id,Corpus.Project(X).Contact.Name,Corpus.Project(X).Contact.Address,Corpus.Project(X).Contact.Email,Corpus.Project(X).Contact.Organisation,Corpus.Project(X).Description,Corpus.Project(X).Description.LanguageId,Corpus.Project(X).Description.Link,Corpus.Project(X).Description.Name,Corpus.Project(X).Description(X),Corpus.Project(X).Description(X).LanguageId,Corpus.Project(X).Description(X).Link,Corpus.Project(X).Description(X).Name,Corpus.Content.Genre,Corpus.Content.SubGenre,Corpus.Content.Interactivity,Corpus.Content.PlanningType,Corpus.Content.Involvement,Corpus.Content.SocialContext,Corpus.Content.EventStructure,Corpus.Content.Channel,Corpus.Content.Task,Corpus.Content.Task(X),Corpus.Content.Modalities,Corpus.Content.Modalities(X),Corpus.Content.Subject,Corpus.Content.Subject.Type,Corpus.Content.Subject.DefaultLink,Corpus.Content.Subject.Link,Corpus.Content.Subject.Encoding,Corpus.Content.Subject(X),Corpus.Content.Subject(X).Type,Corpus.Content.Subject(X).DefaultLink,Corpus.Content.Subject(X).Link,Corpus.Content.Subject(X).Encoding,Corpus.Content.Languages.Description,Corpus.Content.Languages.Description.LanguageId,Corpus.Content.Languages.Description.Link,Corpus.Content.Languages.Description.Name,Corpus.Content.Languages.Description(X),Corpus.Content.Languages.Description(X).LanguageId,Corpus.Content.Languages.Description(X).Link,Corpus.Content.Languages.Description(X).Name,Corpus.Content.Languages.Language,Corpus.Content.Languages.Language.Id,Corpus.Content.Languages.Language.ResourceRef,Corpus.Content.Languages.Language.Name,Corpus.Content.Languages.Language.Name(X),Corpus.Content.Languages.Language.MotherTongue,Corpus.Content.Languages.Language.PrimaryLanguage,Corpus.Content.Languages.Language.Dominant,Corpus.Content.Languages.Language.Description,Corpus.Content.Languages.Language.Description.LanguageId,Corpus.Content.Languages.Language.Description.Link,Corpus.Content.Languages.Language.Description.Name,Corpus.Content.Languages.Language.Description(X),Corpus.Content.Languages.Language.Description(X).LanguageId,Corpus.Content.Languages.Language.Description(X).Link,Corpus.Content.Languages.Language.Description(X).Name,Corpus.Content.Languages.Language(X),Corpus.Content.Languages.Language(X).Id,Corpus.Content.Languages.Language(X).ResourceRef,Corpus.Content.Languages.Language(X).Name,Corpus.Content.Languages.Language(X).Name(X),Corpus.Content.Languages.Language(X).MotherTongue,Corpus.Content.Languages.Language(X).PrimaryLanguage,Corpus.Content.Languages.Language(X).Dominant,Corpus.Content.Languages.Language(X).Description,Corpus.Content.Languages.Language(X).Description.LanguageId,Corpus.Content.Languages.Language(X).Description.Link,Corpus.Content.Languages.Language(X).Description.Name,Corpus.Content.Languages.Language(X).Description(X),Corpus.Content.Languages.Language(X).Description(X).LanguageId,Corpus.Content.Languages.Language(X).Description(X).Link,Corpus.Content.Languages.Language(X).Description(X).Name,Corpus.Content.Description,Corpus.Content.Description.LanguageId,Corpus.Content.Description.Link,Corpus.Content.Description.Name,Corpus.Content.Description(X),Corpus.Content.Description(X).LanguageId,Corpus.Content.Description(X).Link,Corpus.Content.Description(X).Name,Corpus.Content.Key,Corpus.Content.Key.Name,Corpus.Content.Key.Type,Corpus.Content.Key.Link,Corpus.Content.Key.DefaultLink,Corpus.Content.Key(X),Corpus.Content.Key(X).Name,Corpus.Content.Key(X).Type,Corpus.Content.Key(X).Link,Corpus.Content.Key(X).DefaultLink,Corpus.Actors.Description,Corpus.Actors.Description.LanguageId,Corpus.Actors.Description.Link,Corpus.Actors.Description.Name,Corpus.Actors.Description(X),Corpus.Actors.Description(X).LanguageId,Corpus.Actors.Description(X).Link,Corpus.Actors.Description(X).Name,Corpus.Actor,Corpus.Actor.ResourceRef,Corpus.Actor.Role,Corpus.Actor.FamilySocialRole,Corpus.Actor.Name,Corpus.Actor.Name(X),Corpus.Actor.FullName,Corpus.Actor.Code,Corpus.Actor.Language,Corpus.Actor.Language.Id,Corpus.Actor.Language.ResourceRef,Corpus.Actor.Language.Name,Corpus.Actor.Language.Name(X),Corpus.Actor.Language.MotherTongue,Corpus.Actor.Language.PrimaryLanguage,Corpus.Actor.Language.Dominant,Corpus.Actor.Language.Description,Corpus.Actor.Language.Description.LanguageId,Corpus.Actor.Language.Description.Link,Corpus.Actor.Language.Description.Name,Corpus.Actor.Language.Description(X),Corpus.Actor.Language.Description(X).LanguageId,Corpus.Actor.Language.Description(X).Link,Corpus.Actor.Language.Description(X).Name,Corpus.Actor.Language(X),Corpus.Actor.Language(X).Id,Corpus.Actor.Language(X).ResourceRef,Corpus.Actor.Language(X).Name,Corpus.Actor.Language(X).Name(X),Corpus.Actor.Language(X).MotherTongue,Corpus.Actor.Language(X).PrimaryLanguage,Corpus.Actor.Language(X).Dominant,Corpus.Actor.Language(X).Description,Corpus.Actor.Language(X).Description.LanguageId,Corpus.Actor.Language(X).Description.Link,Corpus.Actor.Language(X).Description.Name,Corpus.Actor.Language(X).Description(X),Corpus.Actor.Language(X).Description(X).LanguageId,Corpus.Actor.Language(X).Description(X).Link,Corpus.Actor.Language(X).Description(X).Name,Corpus.Actor.EthnicGroup,Corpus.Actor.Age,Corpus.Actor.Sex,Corpus.Actor.Education,Corpus.Actor.Anonymized,Corpus.Actor.Contact.Name,Corpus.Actor.Contact.Address,Corpus.Actor.Contact.Email,Corpus.Actor.Contact.Organisation,Corpus.Actor.Description,Corpus.Actor.Description.LanguageId,Corpus.Actor.Description.Link,Corpus.Actor.Description.Name,Corpus.Actor.Description(X),Corpus.Actor.Description(X).LanguageId,Corpus.Actor.Description(X).Link,Corpus.Actor.Description(X).Name,Corpus.Actor.Key,Corpus.Actor.Key.Name,Corpus.Actor.Key.Type,Corpus.Actor.Key.Link,Corpus.Actor.Key.DefaultLink,Corpus.Actor.Key(X),Corpus.Actor.Key(X).Name,Corpus.Actor.Key(X).Type,Corpus.Actor.Key(X).Link,Corpus.Actor.Key(X).DefaultLink,Corpus.Actor(X),Corpus.Actor(X).ResourceRef,Corpus.Actor(X).Role,Corpus.Actor(X).FamilySocialRole,Corpus.Actor(X).Name,Corpus.Actor(X).Name(X),Corpus.Actor(X).FullName,Corpus.Actor(X).Code,Corpus.Actor(X).Language.Id,Corpus.Actor(X).Language.ResourceRef,Corpus.Actor(X).Language.Name,Corpus.Actor(X).Language.Name(X),Corpus.Actor(X).Language.MotherTongue,Corpus.Actor(X).Language.PrimaryLanguage,Corpus.Actor(X).Language.Dominant,Corpus.Actor(X).Language.Description,Corpus.Actor(X).Language.Description.LanguageId,Corpus.Actor(X).Language.Description.Link,Corpus.Actor(X).Language.Description.Name,Corpus.Actor(X).Language.Description(X),Corpus.Actor(X).Language.Description(X).LanguageId,Corpus.Actor(X).Language.Description(X).Link,Corpus.Actor(X).Language.Description(X).Name,Corpus.Actor(X).Language(X),Corpus.Actor(X).Language(X).Id,Corpus.Actor(X).Language(X).ResourceRef,Corpus.Actor(X).Language(X).Name,Corpus.Actor(X).Language(X).Name(X),Corpus.Actor(X).Language(X).MotherTongue,Corpus.Actor(X).Language(X).PrimaryLanguage,Corpus.Actor(X).Language(X).Dominant,Corpus.Actor(X).Language(X).Description,Corpus.Actor(X).Language(X).Description.LanguageId,Corpus.Actor(X).Language(X).Description.Link,Corpus.Actor(X).Language(X).Description.Name,Corpus.Actor(X).Language(X).Description(X),Corpus.Actor(X).Language(X).Description(X).LanguageId,Corpus.Actor(X).Language(X).Description(X).Link,Corpus.Actor(X).Language(X).Description(X).Name,Corpus.Actor(X).EthnicGroup,Corpus.Actor(X).Age,Corpus.Actor(X).Sex,Corpus.Actor(X).Education,Corpus.Actor(X).Anonymized,Corpus.Actor(X).Contact.Name,Corpus.Actor(X).Contact.Address,Corpus.Actor(X).Contact.Email,Corpus.Actor(X).Contact.Organisation,Corpus.Actor(X).Description,Corpus.Actor(X).Description.LanguageId,Corpus.Actor(X).Description.Link,Corpus.Actor(X).Description.Name,Corpus.Actor(X).Description(X),Corpus.Actor(X).Description(X).LanguageId,Corpus.Actor(X).Description(X).Link,Corpus.Actor(X).Description(X).Name,Corpus.Actor(X).Key,Corpus.Actor(X).Key.Name,Corpus.Actor(X).Key.Type,Corpus.Actor(X).Key.Link,Corpus.Actor(X).Key.DefaultLink,Corpus.Actor(X).Key(X),Corpus.Actor(X).Key(X).Name,Corpus.Actor(X).Key(X).Type,Corpus.Actor(X).Key(X).Link,Corpus.Actor(X).Key(X).DefaultLink,Corpus.CorpusLink,Corpus.CorpusLink.Name,Corpus.CorpusLink(X),Corpus.CorpusLink(X).Name,Corpus.SearchService,Corpus.CorpusStructureService,Corpus.CatalogueLink", null});
    }

    public javax.swing.table.DefaultTableModel getImdiFieldListsTableModel() {

        javax.swing.table.DefaultTableModel returnTableModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{
                    {null, null, null}
                },
                new String[]{
                    "View Name", "Display Fields", "Display"
                }) {

            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        returnTableModel.setRowCount(0);
        for (int fieldCount = 0; fieldCount < imdiFieldLists.size(); fieldCount++) {
            returnTableModel.addRow((Object[]) imdiFieldLists.get(fieldCount));
        }
        returnTableModel.setValueAt(new Boolean(true), currentFieldListIndex, 2);
        return returnTableModel;
    }

    public javax.swing.table.DefaultTableModel getLocationsTableModel() {

//        javax.swing.table.DefaultTableModel returnTableModel =  new javax.swing.table.DefaultTableModel(
//            new Object [][] {
//                new Object[imdiFieldLists.size()][2]
//            },
//            new String [] {
//                "Tree", "Location"
//            }
//        ){
//            Class[] types = new Class [] {
//                java.lang.String.class, java.lang.String.class
//            };
//
//            public Class getColumnClass(int columnIndex) {
//                return types [columnIndex];
//            }
//        };
//        for (int fieldCount = 0; fieldCount < imdiFieldLists.size(); fieldCount++){
//            returnTableModel.addRow((Object[])imdiFieldLists.get(fieldCount));
//        }
//        return returnTableModel;
        return new javax.swing.table.DefaultTableModel(
                new Object[][]{
                    {"Remote Corpus", "http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi"},
                    {"Local Corpus", "file://data1/media-archive-copy/Corpusstructure/MPI.imdi"},
                    {"Local Directory", "file://data1/media-archive-copy/TestWorkingDirectory/"}
                },
                new String[]{
                    "Tree", "Location"
                }) {

            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
    }//    new javax.swing.table.DefaultTableModel(
//            new Object [][] {
//                {"Display Fields", "Name,Session.Name,Corpus.Name,Session.Description,Corpus.Description,Session.Title,Corpus.Title", null},
//                {"Short Display Fields", "Name,Session.Name,Corpus.Name,Session.Description,Corpus.Description", null},
//                {null, null, null}
//            },
//            new String [] {
//                "View Name", "Display Fields", "Display"
//            }
//        ) {
//            Class[] types = new Class [] {
//                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
//            };
//
//            public Class getColumnClass(int columnIndex) {
//                return types [columnIndex];
//            }
//        }
}


