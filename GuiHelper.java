/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author petwit
 */
public class GuiHelper {

    private ImdiHelper imdiHelper;
    public ImdiFieldViews imdiFieldViews; // TODO: decide if this should be private
    private LinorgSessionStorage linorgSessionStorage;
    private Hashtable selectedFilesList = new Hashtable(); // this is a list of the files currently displayed in the files window
    private Vector locationsList; // this is the list of locations seen in the tree and the location settings
    private Hashtable locationTreeNodes = new Hashtable(); // this is used to find the location tree node when it is to be removed via the ulr
//    MapView mapView;
    JPanel selectedFilesPanel;
    LinorgWindowManager linorgWindowManager;
    // create a clip board owner for copy and paste actions
    ClipboardOwner clipboardOwner = new ClipboardOwner() {

        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            System.out.println("lost clipboard ownership");
        //throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    public GuiHelper(LinorgSessionStorage tempLinorgSessionStorage) {
        linorgSessionStorage = tempLinorgSessionStorage;
        imdiHelper = new ImdiHelper(linorgSessionStorage);
        imdiFieldViews = new ImdiFieldViews(linorgSessionStorage);
        loadLocationsList();
    }

    public void setWindowManager(LinorgWindowManager localLinorgWindowManager) {
        linorgWindowManager = localLinorgWindowManager;
    }

    public void saveState() {
        imdiHelper.saveMd5sumIndex();
        imdiFieldViews.saveViewsToFile();
        try {
            linorgSessionStorage.saveObject(locationsList, "locationsList");
            System.out.println("saved locationsList");
        } catch (Exception ex) {
            System.out.println("save locationsList exception: " + ex.getMessage());
        }
    }

    public int addDefaultCorpusLocations() {
        int addedCount = 0;
        if (addLocation("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi")) {
            addedCount++;
        }
        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi")) {
            addedCount++;
        }
        return addedCount;
    }

    private void loadLocationsList() {
        try {
            locationsList = (Vector) linorgSessionStorage.loadObject("locationsList");
        } catch (Exception ex) {
            System.out.println("load locationsList exception: " + ex.getMessage());
        }
        if (locationsList == null) {
            locationsList = new Vector();
//            locationsList.add("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi");
//            locationsList.add("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi");
//            //locationsList.add("file:///data1/media-archive-copy/Corpusstructure/MPI.imdi");
//            locationsList.add("file:///data1/media-archive-copy/TestWorkingDirectory/");
//            //locationsList.add("http://lux16.mpi.nl/corpora/ac-ESF/Info/ladfc2.txt");
//            //locationsList.add("file:///data1/media-archive-copy/Corpusstructure/MPI.imdi");
//            locationsList.add("http://corpus1.mpi.nl/qfs1/media-archive/Comprehension/Elizabeth_Johnson/Corpusstructure/1.imdi");
//            //locationsList.add("file:///data1/media-archive-copy/TestWorkingDirectory/");
            addDefaultCorpusLocations();
            System.out.println("created new locationsList");
        }
    }

    public void initViewMenu(javax.swing.JMenu viewMenu) {
        ButtonGroup viewMenuButtonGroup = new javax.swing.ButtonGroup();
        //String[] viewLabels = guiHelper.imdiFieldViews.getSavedFieldViewLables();
        for (Enumeration menuItemName = imdiFieldViews.getSavedFieldViewLables(); menuItemName.hasMoreElements();) {
            String currentMenuName = menuItemName.nextElement().toString();
            javax.swing.JRadioButtonMenuItem viewLabelRadioButtonMenuItem;
            viewLabelRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
            viewMenuButtonGroup.add(viewLabelRadioButtonMenuItem);
            viewLabelRadioButtonMenuItem.setSelected(imdiFieldViews.getCurrentGlobalViewName().equals(currentMenuName));
            viewLabelRadioButtonMenuItem.setText(currentMenuName);
            viewLabelRadioButtonMenuItem.setName(currentMenuName);
            viewLabelRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    imdiFieldViews.setCurrentGlobalViewName(((Component) evt.getSource()).getName());
                }
            });
            viewMenu.add(viewLabelRadioButtonMenuItem);
        }
    }

    public DefaultMutableTreeNode getImdiTreeNode(String urlString) {
        DefaultMutableTreeNode treeNode;
        ImdiHelper.ImdiTreeObject imdiTreeObject = imdiHelper.getTreeNodeObject(urlString);
        treeNode = new DefaultMutableTreeNode(imdiTreeObject);
        treeNode.setAllowsChildren(imdiTreeObject.isImdi() || imdiTreeObject.isDirectory());
        return treeNode;
    }
// date filter code
    public void updateDateSlider(JSlider dateSlider) {
        if (imdiHelper.minNodeDate == null) {
            System.out.println("global node date is null");
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(imdiHelper.minNodeDate);
        int startYear = calendar.get(Calendar.YEAR);
        dateSlider.setMinimum(startYear);
        calendar.setTime(imdiHelper.maxNodeDate);
        int endYear = calendar.get(Calendar.YEAR);
        dateSlider.setMaximum(endYear);
    }

    public void filterByDate(DefaultMutableTreeNode itemNode, int sliderValue) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, sliderValue);
        Enumeration childNodes = itemNode.children();
        while (childNodes.hasMoreElements()) {
            Object tempNodeObject = ((DefaultMutableTreeNode) childNodes.nextElement()).getUserObject();
            System.out.println("filterByDate: " + tempNodeObject.toString());
            if (imdiHelper.isImdiNode(tempNodeObject)) {
                ((ImdiHelper.ImdiTreeObject) tempNodeObject).setMinDate(calendar.getTime());
            } else {
                System.out.println("not an imdi node: " + tempNodeObject.toString());
            }
        }
    }
// end date filter code
    public void getImdiChildNodes(DefaultMutableTreeNode itemNode) {
        if (itemNode.getChildCount() == 0) {
            if (imdiHelper.isImdiNode(itemNode.getUserObject())) {
                ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
                if (!imdiTreeObject.isImdi() && !imdiTreeObject.isDirectory()) {
                    System.out.println("file to be opened");
                } else {
                    //ImdiHelper.ImdiTreeObject[] childNodes = imdiTreeObject.getChildren(imdiFieldViews, imdiFieldViews.getCurrentFieldArray());
                    ImdiHelper.ImdiTreeObject[] childNodes = imdiTreeObject.loadChildNodes();
                    for (int childCount = 0; childCount < childNodes.length; childCount++) {
                        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(childNodes[childCount]);
                        treeNode.setAllowsChildren(childNodes[childCount].isImdi() || childNodes[childCount].isDirectory());
                        itemNode.add(treeNode);
                    }
                }
            }
        }
    }

    public void copyBranchToCashe(JDesktopPane destinationComp, Object selectedNodeUserObject) {
        String dialogTitle = "Copy Brach";
        if (imdiHelper.isImdiNode(selectedNodeUserObject)) {
            boolean moreToLoad = true;
            while (moreToLoad) {
                int[] tempChildCountArray = ((ImdiHelper.ImdiTreeObject) selectedNodeUserObject).getChildCount();
                System.out.println("children not loaded: " + tempChildCountArray[0] + " loaded:" + tempChildCountArray[1]);
                moreToLoad = (tempChildCountArray[0] != 0);
                if (moreToLoad) {
                    if (0 != JOptionPane.showConfirmDialog(destinationComp, tempChildCountArray[0] + " out of " + (tempChildCountArray[0] + tempChildCountArray[1]) + "nodes are not loaded\ndo you want to continue?", "Loading Children", 0)) {
                        return;
                    }
                    ((ImdiHelper.ImdiTreeObject) selectedNodeUserObject).loadNextLevelOfChildren(System.currentTimeMillis() + 100 * 5);
                }
            }
            //String mirrorNameString = JOptionPane.showInputDialog(destinationComp, "Enter a tile for the local mirror");
            String destinationDirectory = linorgSessionStorage.storageDirectory + File.separatorChar + "imdicache";
            File destinationFile = new File(destinationDirectory);
            boolean cacheDirExists = destinationFile.exists();
            if (!cacheDirExists) {
                cacheDirExists = destinationFile.mkdir();
            }
            //destinationDirectory = destinationDirectory + File.separator + mirrorNameString;
            //boolean brachDirCreated = (new File(destinationDirectory)).mkdir();
            // TODO: remove the branch directory and replace it with a named node in the locations settings or just a named imdinode
            if (cacheDirExists) {
                destinationDirectory = destinationDirectory + File.separatorChar;
                JOptionPane.showMessageDialog(destinationComp, "Saving to: " + destinationDirectory, dialogTitle, 0);
                String newNodeLocation = ((ImdiHelper.ImdiTreeObject) selectedNodeUserObject).saveBrachToLocal(destinationDirectory);
                if (newNodeLocation != null) {
                    addLocation("file://" + newNodeLocation);
                // TODO: create an imdinode to contain the name and point to the location
                }
            } else {
                JOptionPane.showMessageDialog(destinationComp, "Could not create the local directory", dialogTitle, 0);
            }
        }
    }

    public void searchSelectedNodes(Vector selectedNodes, String searchString, JPopupMenu jPopupMenu) {
        int[] childCountArray = new int[]{0, 0};
        int messageIconIndex = 0;
        if (selectedNodes.size() == 0) {
            JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "No nodes are selected", "Search", messageIconIndex);
            return;
        } else {
            SearchDialog searchDialog = new SearchDialog(JOptionPane.getFrameForComponent(linorgWindowManager.desktopPane), selectedNodes, searchString);
        //Hashtable foundNodes = searchDialog.getFoundNodes();
//            if (foundNodes.size() > 0) {
//                String frameTitle;
//                if (selectedNodes.size() == 1) {
//                    frameTitle = "Found: " + searchString + " x " + foundNodes.size() + " in " + selectedNodes.get(0).toString();
//                } else {
//                    frameTitle = "Found: " + searchString + " x " + foundNodes.size() + " in " + selectedNodes.size() + " nodes";
//                }
//                openFloatingTable(foundNodes.elements(), frameTitle, jPopupMenu);
//            } else {
//                JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "\"" + searchString + "\" not found", "Search", messageIconIndex);
//            }
        }

    // count selected nodes and then their child node indicating unopened nodes
    // iterate over allthe selected nodes in the localCorpusTree
//        Enumeration selectedNodesEnum = selectedNodes.elements();
//        while (selectedNodesEnum.hasMoreElements()) {
//            Object currentElement = selectedNodesEnum.nextElement();
//            if (imdiHelper.isImdiNode(currentElement)) {
//                int[] tempChildCountArray = ((ImdiHelper.ImdiTreeObject) currentElement).getChildCount();
//                childCountArray[0] += tempChildCountArray[0];
//                childCountArray[1] += tempChildCountArray[1];
//                System.out.println("children not loaded: " + childCountArray[0] + " loaded:" + childCountArray[1]);
//            }
//        }

//        if (childCountArray[0] > 0 || childCountArray[1] == 0) {
//            if (selectedNodes.size() == 0) {
//                JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "No nodes are selected", "Search", messageIconIndex);
//                return;
//            }
//            if (childCountArray[1] == 0) {
//                JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "Of the selected nodes none have been loaded", "Search", messageIconIndex);
//                return;
//            }
//            if (childCountArray[0] > 0) {
//                if (0 != JOptionPane.showConfirmDialog(linorgWindowManager.desktopPane, childCountArray[0] + " out of " + (childCountArray[0] + childCountArray[1]) + "nodes are not loaded\ndo you want to continue?", "Search", messageIconIndex)) {
//                    return;
//                }
//            }
//        }

//        if (searchString == null) {
//            searchString = JOptionPane.showInputDialog(linorgWindowManager.desktopPane, "Enter search term");
//        }

//        // iterate over all the selected nodes in the localCorpusTree
//        Hashtable foundNodes = new Hashtable();
//        selectedNodesEnum = selectedNodes.elements();
//        // show a progress dialog
////        int lengthOfTask = selectedNodes.size();
////        int progressInTask = 0;
////        ProgressMonitor progressMonitor = new ProgressMonitor(destinationComp, "Searching selected nodes and (loaded)subnodes", "", 0, lengthOfTask);
//        while (selectedNodesEnum.hasMoreElements()) {
//            Object currentElement = selectedNodesEnum.nextElement();
//            if (imdiHelper.isImdiNode(currentElement)) {
//                System.out.println("parentNode: " + currentElement);
//                ((ImdiHelper.ImdiTreeObject) currentElement).searchNodes(foundNodes, searchString);
//            // update the progress dialog
////                String message = String.format("done " + progressInTask + " of " + lengthOfTask);//"Completed %d%%.\n", progressInTask);
////                progressMonitor.setNote(message);
////                progressMonitor.setProgress(progressInTask);
////                progressInTask++;
////                if (progressMonitor.isCanceled()) {
////                    progressMonitor.close();
////                    break;
////                }
////            JOptionPane.showMessageDialog(destinationComp, "done " + progressInTask + " of " + lengthOfTask);
//            }
//        }
//        //progressMonitor.close();

//        System.out.println("done");
    }

    public void openImdiXmlWindow(Object userObject) {
        if (imdiHelper.isImdiNode(userObject)) {
            String nodeUrl = ((ImdiHelper.ImdiTreeObject) (userObject)).getUrl();
            String nodeName = ((ImdiHelper.ImdiTreeObject) (userObject)).toString();
            linorgWindowManager.openUrlWindow(nodeName, nodeUrl);
        }
    }
    private JTable targetTable = null; // this is used to track the originator of the table's menu actions, this method is not preferable however the menu event does not pass on the originator
    //private int targetRow;
    private int targetColumn;

    public void openFloatingTable(Enumeration rowNodesEnum, String frameTitle, final JPopupMenu jPopupMenu /* this final is not originally declared final and should be checked */) {
        javax.swing.JTable jTable1;
        javax.swing.JScrollPane jScrollPane6;
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable() {

//            public TableCellEditor getCellEditor(int row, int column) {
//                if (column == 2) {
//                    JComboBox comboBox = new JComboBox();
//                    comboBox.addItem("Item 1");
//                    comboBox.addItem("Item 2");
//                    comboBox.addItem("Item 3");
//                    comboBox.addItem("Item 4");
//                    comboBox.addItem("Item 5");
//                    comboBox.addItem("Item 6");
//                    return new DefaultCellEditor(comboBox);
//                }
//                if (column == 3) {
//                    //openFloatingTable(this, getImdiTableModel({new ImdiHelper.ImdiTreeObject("one", null), new ImdiHelper.ImdiTreeObject("one", null)}), frameTitle, jPopupMenu);
//                }
//                return super.getCellEditor(row, column);
//            }
            // this cell renderer may have caused redraw issues
//            public TableCellRenderer getCellRenderer(int row, int column) {
//                if (column == 3 && (row == 2 || row == 0)) {
//                    TableCellRenderer listTableCellRenderer = new TableCellRenderer() {
//
//                        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
//                            JList cellList;
//                            if (row == 0) {
//                                cellList = new JList(new Object[]{"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"});
//                            } else {
//                                cellList = new JList(new Object[]{"Item 1", "Item 2", "Item 5"});
//                            }
//                            //cellListHeight = cellList.getPreferredSize().height;
//                            table.setRowHeight(row, cellList.getPreferredSize().height);
//                            return cellList;
//                        }
//                    };
////                    if (cellListHeight > super.getRowHeight()) {
////                        super.setRowHeight(row, cellListHeight);
////                    }
////                        public Component getTableCellRendererComponent(JTable jTable,
////                                Object obj, boolean isSelected, boolean hasFocus, int row,
////                                int column) {
////                            setText((String) obj);
////                            int height_wanted = (int) getPreferredSize().getHeight();
////                            if (height_wanted != jTable.getRowHeight(row)) {
////                                jTable.setRowHeight(row, height_wanted);
////                            }
////                            return this;
////                        }
//                    return listTableCellRenderer;
//                } //                if (row == 5) {
//                //                    setBackground(Color.green);
//                //                }
//                return super.getCellRenderer(row, column);
//            }
            //Implement table cell tool tips.
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);
                tip = getValueAt(rowIndex, colIndex).toString();
                return tip;
            }
            //Implement table header tool tips.
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {

                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        //int realIndex = columnModel.getColumn(index).getModelIndex();
                        return "Description of how to use the " + getColumnName(index) + " colomn.";
                    }
                };
            }
        };

        //jTable1.setAutoCreateRowSorter(true);

        ImdiHelper.ImdiTableModel imdiTableModel = imdiHelper.getImdiTableModel();
        imdiTableModel.setShowIcons(true);
        imdiTableModel.addImdiObjects(rowNodesEnum);
        jTable1.setModel(imdiTableModel);
        jTable1.setName("jTable1");
        //jTable1.doLayout();
        //jTable1.getModel().
        //jTable1.invalidate();

        setColumnWidths(jTable1, imdiTableModel);

        jTable1.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.out.println("table header click");
                targetColumn = ((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY()));
                targetTable = ((JTableHeader) evt.getComponent()).getTable();
                if (evt.getButton() == MouseEvent.BUTTON1) {
                    ((ImdiHelper.ImdiTableModel) targetTable.getModel()).sortByColumn(targetColumn);
                }
                System.out.println("columnIndex: " + targetColumn);
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    targetTable = ((JTableHeader) evt.getComponent()).getTable();
                    System.out.println("columnIndex: " + targetColumn);

                    JPopupMenu popupMenu = new JPopupMenu();

                    JMenuItem hideColumnMenuItem = new JMenuItem("Hide column: \"" + targetTable.getColumnName(targetColumn) + "\"");
                    hideColumnMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            System.out.println("hideColumnMenuItem: " + targetTable.toString());
                            ((ImdiHelper.ImdiTableModel) targetTable.getModel()).hideColumn(targetColumn);
                        }
                    });

                    JMenuItem saveViewMenuItem = new JMenuItem("Save this view");
                    saveViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            System.out.println("saveViewNenuItem: " + targetTable.toString());
                            String fieldViewName = (String) JOptionPane.showInputDialog(null, "Enter a name to save this view as", "Save View", JOptionPane.PLAIN_MESSAGE);
                            // if the user did not cancel
                            if (fieldViewName != null) {
                                if (!imdiFieldViews.addImdiFieldView(fieldViewName, ((ImdiHelper.ImdiTableModel) targetTable.getModel()).getFieldView())) {
                                    JOptionPane.showMessageDialog(null, "A View with the same name already exists, nothing saved");
                                }
                            }
                        }
                    });

                    JMenuItem editViewMenuItem = new JMenuItem("Edit this view");
                    editViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            System.out.println("editViewNenuItem: " + targetTable.toString());
                            JDialog editViewsDialog = new JDialog(JOptionPane.getFrameForComponent(linorgWindowManager.desktopPane), true);
                            Container dialogcontainer = editViewsDialog.getContentPane();
                            dialogcontainer.setLayout(new BorderLayout());
                            editViewsDialog.setSize(600, 400);
                            editViewsDialog.setBounds(50, 50, 600, 400);
                            TableModel tableModel = imdiFieldViews.getImdiFieldViewTableModel(((ImdiHelper.ImdiTableModel) targetTable.getModel()).getFieldView());
                            tableModel.addTableModelListener(new TableModelListener() {

                                private JTable dilalogTargetTable = targetTable;

                                public void tableChanged(TableModelEvent e) {
                                    TableModel localTableModel = (TableModel) e.getSource();
                                    String targetColumnName = localTableModel.getValueAt(e.getFirstRow(), 0).toString();
                                    boolean booleanState = localTableModel.getValueAt(e.getFirstRow(), e.getColumn()).equals(true);
//                                    System.out.println("name: " + targetColumnName);
//                                    System.out.println("value: " + booleanState);
//                                    System.out.println("pos: " + e.getColumn());
                                    switch (e.getColumn()) {
                                        case 2:
                                            if (booleanState) {
                                                ((ImdiHelper.ImdiTableModel) targetTable.getModel()).getFieldView().addShowOnlyColumn(targetColumnName);
                                            } else {
                                                ((ImdiHelper.ImdiTableModel) targetTable.getModel()).getFieldView().removeShowOnlyColumn(targetColumnName);
                                            }
                                        case 3:
                                            if (booleanState) {
                                                ((ImdiHelper.ImdiTableModel) targetTable.getModel()).getFieldView().addHiddenColumn(targetColumnName);
                                            } else {
                                                ((ImdiHelper.ImdiTableModel) targetTable.getModel()).getFieldView().removeHiddenColumn(targetColumnName);
                                            }
                                        case 4:
                                    }
                                    ((ImdiHelper.ImdiTableModel) targetTable.getModel()).reloadTableData();
                                //                                  throw new UnsupportedOperationException("Not supported yet.");
                                }
                            });
                            JTable ordertable = new JTable(tableModel);
                            JScrollPane js = new JScrollPane(ordertable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                            js.setBounds(10, 10, 550, 350);
                            dialogcontainer.add(js);
                            editViewsDialog.add(js);
                            editViewsDialog.setVisible(true);
                        }
                    });

                    JMenuItem showOnlyCurrentViewMenuItem = new JMenuItem("Show only the current columns");
                    showOnlyCurrentViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            System.out.println("saveViewNenuItem: " + targetTable.toString());
                            ((ImdiHelper.ImdiTableModel) targetTable.getModel()).showOnlyCurrentColumns();
                        }
                    });

                    popupMenu.add(showOnlyCurrentViewMenuItem);
                    //popupMenu.add(applyViewNenuItem);
                    //popupMenu.add(saveViewMenuItem);
                    popupMenu.add(editViewMenuItem);
                    popupMenu.add(saveViewMenuItem);
                    popupMenu.add(hideColumnMenuItem);
                    // create the views sub menu
                    JMenu fieldViewsMenuItem = new JMenu("Apply Saved View");
                    ButtonGroup viewMenuButtonGroup = new javax.swing.ButtonGroup();
                    String currentGlobalViewLabel = imdiFieldViews.currentGlobalViewName;
                    for (Enumeration savedViewsEnum = imdiFieldViews.getSavedFieldViewLables(); savedViewsEnum.hasMoreElements();) {
                        String currentViewLabel = savedViewsEnum.nextElement().toString();
                        javax.swing.JRadioButtonMenuItem viewLabelRadioButtonMenuItem;
                        viewLabelRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
                        viewMenuButtonGroup.add(viewLabelRadioButtonMenuItem);
                        viewLabelRadioButtonMenuItem.setSelected(currentGlobalViewLabel.equals(currentViewLabel));
                        viewLabelRadioButtonMenuItem.setText(currentViewLabel);
                        viewLabelRadioButtonMenuItem.setName(currentViewLabel);
                        viewLabelRadioButtonMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {

                            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                                // TODO: this should change the data grid view
                                //imdiFieldViews.setCurrentGlobalViewName(((Component) evt.getSource()).getName());
                            }
                        });
                        fieldViewsMenuItem.add(viewLabelRadioButtonMenuItem);
                    }
                    popupMenu.add(fieldViewsMenuItem);
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    targetTable = (JTable) evt.getComponent();
//                    System.out.println("set the current table");
                    jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });
        jScrollPane6.setViewportView(jTable1);
        linorgWindowManager.createWindow(frameTitle, jScrollPane6);
    }

    private void setColumnWidths(JTable localTable, ImdiHelper.ImdiTableModel localImdiTableModel) {
        int charPixWidth = 100; // this does not need to be accurate but must be more than the number of pixels used to render each character
        for (int columnCount = 0; columnCount < localImdiTableModel.getColumnCount(); columnCount++) {
            localTable.getColumnModel().getColumn(columnCount).setPreferredWidth(localImdiTableModel.getColumnLength(columnCount) * charPixWidth);
            System.out.println("preferedWidth: " + localImdiTableModel.getColumnLength(columnCount));
        }
    }

    // TODO: this could be merged witht the add row function
    public AbstractTableModel getImdiTableModel(Hashtable rowNodes) {
        ImdiHelper.ImdiTableModel searchTableModel = imdiHelper.getImdiTableModel();
        searchTableModel.setShowIcons(true);
        searchTableModel.addImdiObjects(rowNodes.elements());
        //Enumeration rowNodeEnum = rowNodes.elements();
        //while (rowNodeEnum.hasMoreElements()) {
        //searchTableModel.addImdiObject((ImdiHelper.ImdiTreeObject) rowNodeEnum.nextElement());
        //}
        return searchTableModel;
    }

    public AbstractTableModel getImdiTableModel() {
        ImdiHelper.ImdiTableModel tempModel = imdiHelper.getImdiTableModel();
        tempModel.setShowIcons(true);
        return tempModel;
    }

    public void showRowChildData(JDesktopPane destinationComp) {
        Object[] possibilities = ((ImdiHelper.ImdiTableModel) targetTable.getModel()).getChildNames();
        String selectionResult = (String) JOptionPane.showInputDialog(destinationComp, "Select the child node type to display", "Show child nodes", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);

        if ((selectionResult != null) && (selectionResult.length() > 0)) {
            ((ImdiHelper.ImdiTableModel) targetTable.getModel()).addChildTypeToDisplay(selectionResult);
        }
    }

    public void addToGridData(TableModel tableModel, Vector nodesToAdd) {
        for (Enumeration nodesToAddEnum = nodesToAdd.elements(); nodesToAddEnum.hasMoreElements();) {
            // iterate over the and add supplied nodes
            addToGridData(tableModel, nodesToAddEnum.nextElement());
//            Object currentObject = nodesToAddEnum.nextElement();
//            if (imdiHelper.isImdiNode(currentObject)) {
//                String hashKey = ((ImdiHelper.ImdiTreeObject) currentObject).getUrl();
//                if (selectedFilesList.containsKey(hashKey)) {
//                    // remove any image nodes from the image window                
//                    //System.out.println("removing from images");
//                    selectedFilesPanel.remove((Component) selectedFilesList.remove(hashKey));
//                    selectedFilesPanel.revalidate();
//                    selectedFilesPanel.repaint();
//                    // remove any map layers
//                    if (mapView.isGisFile(hashKey)) {
//                        mapView.removeLayer(hashKey);
//                    }
//                }
//            }
        }
    }

    public void addToGridData(TableModel tableModel, Object itemNode) {
        // there is no point loading the child nodes to display the parent node in a grid, however if the child nodes are requested for display then at that point they will need to be loaded but not at this point
        //getImdiChildNodes(itemNode); // load the child nodes and the fields for each
        if (imdiHelper.isImdiNode(itemNode)) {
            ImdiHelper.ImdiTreeObject itemImdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode;
            String hashKey = itemImdiTreeObject.getUrl();
            System.out.println("hashkey: " + hashKey);
            if (itemImdiTreeObject.isImdi()) {
                ((ImdiHelper.ImdiTableModel) tableModel).addSingleImdiObject(itemImdiTreeObject);
            }
            if (!itemImdiTreeObject.isImdi() || itemImdiTreeObject.getResource() != null) {
                // TODO: display non imdi file
                // TODO: move the display of resources and files into a separate class
                // TODO: replace selectedFilesList but using the name propetry of the added component for the purpose of removing it later
                System.out.println("display non imdi file: " + itemImdiTreeObject.getUrl());
                String imageFileName;
                if (itemImdiTreeObject.getResource() != null) {
                    imageFileName = itemImdiTreeObject.getResource();
                } else {
                    imageFileName = itemImdiTreeObject.getUrl();
                }
                if (selectedFilesPanel == null) {
                    selectedFilesPanel = new JPanel();
                    selectedFilesPanel.setLayout(new java.awt.GridLayout(6, 6));
                    linorgWindowManager.createWindow("Selected Files", selectedFilesPanel);
                }
                imageFileName = imageFileName.replace("file://", "");
                ImageIcon nodeImage = new ImageIcon(imageFileName);
                JLabel imageLabel = new JLabel(itemImdiTreeObject.toString(), nodeImage, JLabel.CENTER);
                //Set the position of the text, relative to the icon:
                imageLabel.setVerticalTextPosition(JLabel.BOTTOM);
                imageLabel.setHorizontalTextPosition(JLabel.CENTER);
                selectedFilesPanel.add(imageLabel);
                selectedFilesList.put(hashKey, imageLabel);
                selectedFilesPanel.revalidate();
                selectedFilesPanel.repaint();
//                if (mapView == null) {
//                    mapView = new MapView();
//                    linorgWindowManager.createWindow("GIS Viewer", mapView);
//                }
//                if (mapView.isGisFile(hashKey)) {
//                    mapView.addLayer(hashKey, imageFileName);
//                    mapView.setVisible(true);
//                }
            }
        }
    }

    public void removeAllFromGridData(TableModel tableModel) {
        System.out.println("removing all images");
        if (selectedFilesPanel != null) {
            selectedFilesPanel.removeAll();
            selectedFilesPanel.revalidate();
            selectedFilesPanel.repaint();
        }
//        if (mapView != null) {
//            mapView.removeAll();
//        }
        selectedFilesList.clear();
        ((ImdiHelper.ImdiTableModel) tableModel).removeAllImdiRows();
    }

    public void removeFromGridData(TableModel tableModel, Vector nodesToRemove) {
        // remove the supplied nodes from the grid
        ((ImdiHelper.ImdiTableModel) tableModel).removeImdiObjects(nodesToRemove.elements());
        for (Enumeration nodesToRemoveEnum = nodesToRemove.elements(); nodesToRemoveEnum.hasMoreElements();) {
            // iterate over the supplied nodes
            Object currentObject = nodesToRemoveEnum.nextElement();
            if (imdiHelper.isImdiNode(currentObject)) {
                String hashKey = ((ImdiHelper.ImdiTreeObject) currentObject).getUrl();
                if (selectedFilesList.containsKey(hashKey)) {
                    // remove any image nodes from the image window                
                    //System.out.println("removing from images");
                    selectedFilesPanel.remove((Component) selectedFilesList.remove(hashKey));
                    selectedFilesPanel.revalidate();
                    selectedFilesPanel.repaint();
                    // remove any map layers
//                    if (mapView.isGisFile(hashKey)) {
//                        mapView.removeLayer(hashKey);
//                    }
                }
            }
        }
    }

    private void addNodeOnce(DefaultMutableTreeNode localDirectoryNode, String currentLocation) {
        boolean nodeExists = false;
        Enumeration localCorpusChildren = localDirectoryNode.children();
        while (localCorpusChildren.hasMoreElements()) {
            if (currentLocation.equals(((ImdiHelper.ImdiTreeObject) ((DefaultMutableTreeNode) localCorpusChildren.nextElement()).getUserObject()).getUrl())) {
                nodeExists = true;
            }
        }
        if (nodeExists) {
            //localDirectoryNode.add(getImdiTreeNode("duplicate"));
        } else {
            DefaultMutableTreeNode currentTreeNode = getImdiTreeNode(currentLocation);
            locationTreeNodes.put(currentLocation, currentTreeNode);
            localDirectoryNode.add(currentTreeNode);
        }
    }

    private void removeExtraneousNodes() {
        Enumeration locationNodesEnum = locationTreeNodes.keys();
        while (locationNodesEnum.hasMoreElements()) {
            String currentLocation = (String) locationNodesEnum.nextElement();
            if (!locationsList.contains(currentLocation)) {
                System.out.println("removing location: " + currentLocation);
                ((DefaultMutableTreeNode) locationTreeNodes.get(currentLocation)).removeFromParent();
                locationTreeNodes.remove(currentLocation);
            }
        }
    }

    public void copyNodeUrlToClipboard(DefaultMutableTreeNode selectedNode) {
        if (imdiHelper.isImdiNode(selectedNode.getUserObject())) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(((ImdiHelper.ImdiTreeObject) selectedNode.getUserObject()).getUrl());
            clipboard.setContents(stringSelection, clipboardOwner);
        }
    }

    public void copySelectedTableRowsToClipBoard(Component destinationComp) {
        int[] selectedRows = targetTable.getSelectedRows();
        // only copy if there is at lease one row selected
        if (selectedRows.length > 0) {
            ((ImdiHelper.ImdiTableModel) targetTable.getModel()).copyImdiRows(selectedRows, clipboardOwner);
        } else {
            JOptionPane.showMessageDialog(destinationComp, "Nothing to copy");
        }
    }

    public void removeSelectedRowsFromTable() {
        int[] selectedRows = targetTable.getSelectedRows();
        ((ImdiHelper.ImdiTableModel) targetTable.getModel()).removeImdiRows(selectedRows);
    }

    public void highlightMatchingRows(Component destinationComp) {
        int selectedRow = targetTable.getSelectedRow();
        ImdiHelper.ImdiTableModel tempImdiTableModel = (ImdiHelper.ImdiTableModel) (targetTable.getModel());
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(destinationComp, "No rows have been selected");
            return;
        }
        Vector foundRows = tempImdiTableModel.getMatchingRows(selectedRow);
        targetTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        targetTable.getSelectionModel().clearSelection();
        JOptionPane.showMessageDialog(destinationComp, "Found " + foundRows.size() + " matching rows");
        for (int foundCount = 0; foundCount < foundRows.size(); foundCount++) {
            for (int coloumCount = 0; coloumCount < targetTable.getColumnCount(); coloumCount++) {
                // TODO: this could be more efficient if the array was converted into selection intervals rather than individual rows (although the SelectionModel might already do this)
                targetTable.getSelectionModel().addSelectionInterval((Integer) foundRows.get(foundCount), (Integer) foundRows.get(foundCount));
            }
        }
    }

    public void applyRootLocations(DefaultMutableTreeNode localDirectoryNode, DefaultMutableTreeNode localCorpusNode, DefaultMutableTreeNode remoteCorpusNode) {
        Enumeration locationEnum = locationsList.elements();
        while (locationEnum.hasMoreElements()) {
            String currentLocation = locationEnum.nextElement().toString();
            System.out.println("currentLocation: " + currentLocation);
            if (imdiHelper.isStringLocal(currentLocation)) {
                // is local
                if (imdiHelper.isStringImdi(currentLocation)) {
                    // is an imdi
                    addNodeOnce(localCorpusNode, currentLocation);
                } else {
                    // not an imdi
                    addNodeOnce(localDirectoryNode, currentLocation);
                }
            } else {
                // is a remote file or imdi
                addNodeOnce(remoteCorpusNode, currentLocation);
            }
        }
        removeExtraneousNodes();
    }

    public javax.swing.table.DefaultTableModel getLocationsTableModel() {
        Object[][] tableObjectAray = new Object[locationsList.size()][2];
        Enumeration locationEnum = locationsList.elements();
        int rowCounter = 0;
        while (locationEnum.hasMoreElements()) {
            tableObjectAray[rowCounter][1] = locationEnum.nextElement();
            if (imdiHelper.isStringImdi(tableObjectAray[rowCounter][1].toString())) {
                // is an imdi
                if (imdiHelper.isStringLocal(tableObjectAray[rowCounter][1].toString())) {
                    tableObjectAray[rowCounter][0] = (Object) ImdiHelper.corpuslocalicon;
                } else {
                    tableObjectAray[rowCounter][0] = ImdiHelper.corpusservericon;
                }
            } else {
                // is not an imdi
                if (imdiHelper.isStringLocal(tableObjectAray[rowCounter][1].toString())) {
                    tableObjectAray[rowCounter][0] = ImdiHelper.directoryIcon;
                } else {
                    tableObjectAray[rowCounter][0] = ImdiHelper.stopicon;
                }
            }
            rowCounter++;
        }
        return new javax.swing.table.DefaultTableModel(tableObjectAray, new String[]{"", "Location"}) {

            Class[] types = new Class[]{
                javax.swing.Icon.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
    }

    public boolean addLocation(String addedLocation) {
        System.out.println("addLocation" + addedLocation);
        if (!locationsList.contains(addedLocation)) {
            locationsList.add(addedLocation);
            return true;
        }
        return false;
    }

    public void removeLocation(Object removeObject) {
        if (imdiHelper.isImdiNode(removeObject)) {
            removeLocation(((ImdiHelper.ImdiTreeObject) removeObject).getUrl()); //.replace("file://", "")
        }
    }

    public void removeLocation(String removeLocation) {
        System.out.println("removeLocation: " + removeLocation);
        locationsList.remove(removeLocation);
    }

//    public void updateLocationsFromModel(javax.swing.table.DefaultTableModel changedTableModel) {
//        Vector updatedLocations = new Vector();
//        for (int rowCounter = 0; rowCounter < changedTableModel.getRowCount(); rowCounter++) {
//            updatedLocations.add(changedTableModel.getValueAt(rowCounter, 1));
//        }
//        locationsList = updatedLocations;
//    }
    public ImdiTreeRenderer getImdiTreeRenderer() {
        return new ImdiTreeRenderer();
    }

    public class ImdiTreeRenderer extends DefaultTreeCellRenderer {

        public ImdiTreeRenderer() {
        }

        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (imdiHelper.isImdiNode(node.getUserObject())) {
                ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) node.getUserObject();

                setIcon(imdiTreeObject.getIcon());
                setToolTipText(imdiTreeObject.toString());
                setEnabled(imdiTreeObject.getNodeEnabled());
            //setVisible(imdiTreeObject.getNodeEnabled());
            }
            return this;
        }
    }
}


