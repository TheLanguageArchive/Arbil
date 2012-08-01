package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.templates.ArbilFavourites;

/**
 * Document : FavouriteSelectBox
 * Created on : May 10, 2012, 5:28:11 PM
 * Author : Peter Withers
 */
public class FavouriteSelectBox extends JPanel {

    final ArbilNode targetNode;
    final JCheckBox metadataFilePerResourceCheckBox;
    final JCheckBox copyDirectoryStructureCheckBox;
    final JList favouriteList;

    public FavouriteSelectBox(ArbilNode targetNode) {
        this.targetNode = targetNode;
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder("Suitable Favourites"));
        favouriteList = new JList(ArbilFavourites.getSingleInstance().listFavouritesFor(targetNode));
        favouriteList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                final JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String labelString = value.toString();
                if (labelString.length() > 50) {
                    labelString = labelString.substring(0, 48) + "...";
                }
                listCellRendererComponent.setText(labelString);
                listCellRendererComponent.setIcon(((ArbilDataNode) value).getIcon());
                return listCellRendererComponent;
            }
        });
        this.add(new JScrollPane(favouriteList), BorderLayout.CENTER);
//        this.add(new JLabel(targetNode.toString(), targetNode.getIcon(), JLabel.LEFT), BorderLayout.PAGE_END);
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
        metadataFilePerResourceCheckBox = new JCheckBox("Metadata for each resource", false);
        optionsPanel.add(metadataFilePerResourceCheckBox);
        copyDirectoryStructureCheckBox = new JCheckBox("Copy directory structure", false);
        optionsPanel.add(copyDirectoryStructureCheckBox);
        this.add(optionsPanel, BorderLayout.PAGE_END);
    }

    public ArbilDataNode getSelectedFavouriteNode() {
        return (ArbilDataNode) favouriteList.getSelectedValue();
    }

    public ArbilNode getTargetNode() {
        return targetNode;
    }

    public boolean getCopyDirectoryStructure() {
        return this.copyDirectoryStructureCheckBox.isSelected();
    }

    public boolean getMetadataFilePerResource() {
        return this.metadataFilePerResourceCheckBox.isSelected();
    }
}
