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
package nl.mpi.arbil.ui.fieldeditors;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 *  Document   : ArbilFieldEditor
 *  Created on : Sep 14, 2010, 1:52:57 PM
 *  Author     : Peter Withers
 */
public class ArbilFieldEditor extends JTextField {

    public ArbilFieldEditor(String initialValue) {
        super(initialValue);
        this.setBorder(null);
        this.setMinimumSize(new Dimension(50, (int) this.getMinimumSize().getHeight()));

        this.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                // the caret position must be set here so that the mac version does not loose the last typed char when entering edit mode
                ArbilFieldEditor.this.setCaretPosition(ArbilFieldEditor.this.getText().length());
            }

            public void focusLost(FocusEvent e) {
            }
        });
    }
}
