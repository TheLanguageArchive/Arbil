/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.ui;

import javax.swing.JOptionPane;
import junit.framework.AssertionFailedError;
import nl.mpi.arbil.util.MessageDialogHandler.DialogBoxResult;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWindowManagerTester {

    public static void main(String[] args) {
	try {
	    ArbilWindowManagerTester tester = new ArbilWindowManagerTester();
	    tester.testShowDialogBoxRememberChoice();
	} catch (AssertionFailedError err) {
	    JOptionPane.showMessageDialog(null, err.getMessage(), err.getClass().toString(), JOptionPane.ERROR_MESSAGE);
	}
    }

    public void testShowDialogBoxRememberChoice() throws AssertionFailedError {
	ArbilWindowManager windowManager = new ArbilWindowManager();

	DialogBoxResult result = windowManager.showDialogBoxRememberChoice("Please select YES and leave box unselected", "Title", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	assertFalse(result.isRememberChoice());
	assertEquals(JOptionPane.YES_OPTION, result.getResult());

	result = windowManager.showDialogBoxRememberChoice("Please select NO and select box", "Title", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	assertTrue(result.isRememberChoice());
	assertEquals(JOptionPane.NO_OPTION, result.getResult());
    }
}
