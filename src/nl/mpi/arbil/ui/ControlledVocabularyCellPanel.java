/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilField;

/**
 * Panel that wraps any component and adds an icon that represents
 * the vocabulary type of an ArbilField
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ControlledVocabularyCellPanel extends JPanel {

    /**
     * @param component Component to wrap
     * @param field Field that has vocabulary
     */
    public ControlledVocabularyCellPanel(Component component, ArbilField field) {
        this(component, field, BorderLayout.LINE_END);
    }

    /**
     * @param component Component to wrap
     * @param field Field that has vocabulary
     * @param iconLocation BorderLayout location of icon
     */
    public ControlledVocabularyCellPanel(Component component, ArbilField field, String iconLocation) {
        super();
        setLayout(new BorderLayout());
        add(component, BorderLayout.CENTER);
        Icon icon = ArbilIcons.getSingleInstance().getIconForVocabulary(field);
        if (icon != null) {
            add(new JLabel(icon), iconLocation);
        }

        setBackground(component.getBackground());
    }

    /**
     * @param cellObject Object contained in cell to get additional width for
     * @return The width that will be added to the decorated component. Safe to
     * call for any object - if not applicable, 0 will be returned
     */
    public static int getAddedWidth(Object cellObject) {
        if (cellObject instanceof ArbilField && ((ArbilField) cellObject).hasVocabulary()) {
            return ArbilIcons.getSingleInstance().getIconForVocabulary((ArbilField) cellObject).getIconWidth();
        }
        return 0;
    }
}
