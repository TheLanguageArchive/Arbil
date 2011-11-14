package nl.mpi.arbil.ui.wizard;

import javax.swing.JComponent;

/**
 * Interface for ArbilWizard contents (wizard 'pages')
 * @see ArbilWizard
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilWizardContent {

    JComponent getContent();

    Object getNext();

    Object getPrevious();
    
}
