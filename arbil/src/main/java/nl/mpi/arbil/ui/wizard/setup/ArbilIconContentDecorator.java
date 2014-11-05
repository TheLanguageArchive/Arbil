/**
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import nl.mpi.arbil.ui.wizard.ArbilWizardContent;

/**
 * Decorator for ArbilWizardContent that shows a 128x128 arbil icon to the WEST 
 * and the component of the inner content in the CENTER
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilIconContentDecorator extends JPanel implements ArbilWizardContent {
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    private ArbilWizardContent innerContent;

    public ArbilIconContentDecorator(ArbilWizardContent innerContent) {
	this.innerContent = innerContent;

	setLayout(new BorderLayout());

	JPanel iconPanel = new JPanel();
	iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.PAGE_AXIS));
	iconPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 2, 2));
        iconPanel.setBackground(Color.WHITE);
	iconPanel.add(new JLabel(new ImageIcon(getClass().getResource("/nl/mpi/arbil/resources/icons/arbil-stable128x128.png"))));
	add(iconPanel, BorderLayout.WEST);
	
	add(innerContent.getContent(), BorderLayout.CENTER);
    }

    public JComponent getContent() {
	return this;
    }

    public Object getNext() {
	return innerContent.getNext();
    }

    public Object getPrevious() {
	return innerContent.getPrevious();
    }

    public void refresh() {
	innerContent.refresh();
	remove(innerContent.getContent());
	add(innerContent.getContent(), BorderLayout.CENTER);
    }

    public void beforeShow() {
	innerContent.beforeShow();
    }

    public boolean beforeNext() {
	return innerContent.beforeNext();
    }

    public boolean beforePrevious() {
	return innerContent.beforePrevious();
    }
}
