package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
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

    private ArbilWizardContent innerContent;

    public ArbilIconContentDecorator(ArbilWizardContent innerContent) {
	this.innerContent = innerContent;

	setLayout(new BorderLayout());

	JPanel iconPanel = new JPanel();
	iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.PAGE_AXIS));
	iconPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 2, 2));
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
