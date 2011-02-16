package mpi.metadata.tools.mdeditor.app.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;


/**
 * This class is used as an editor component within a combobox.
 *
 * @author Don Willems
 * @version 1.0
 */
public class ComboBoxLookAheadEditor extends LookAheadTextField
    implements ActionListener {
    /**
     * Creates an empty text field with a context that determines the look
     * ahead content.
     *
     * @param context The look ahead context.
     * @param cb The combobox.
     */
    public ComboBoxLookAheadEditor(LookAheadContext context, JComboBox cb) {
        super(context);
        cb.addActionListener(this);
    }

    /**
     * Creates a text field with a context that determines the look ahead
     * content and initialized with the specified text.
     *
     * @param text The content of the text field.
     * @param context The look ahead context.
     * @param cb The combobox.
     */
    public ComboBoxLookAheadEditor(String text, LookAheadContext context,
        JComboBox cb) {
        super(text, context);
        cb.addActionListener(this);
    }

    /**
     * Creates a text field with a context that determines the look ahead
     * content and initialized with the specified width.
     *
     * @param columns The width of the text field.
     * @param context The look ahead context.
     * @param cb The combobox.
     */
    public ComboBoxLookAheadEditor(int columns, LookAheadContext context,
        JComboBox cb) {
        super(columns, context);
        cb.addActionListener(this);
    }

    /**
     * Creates a text field with a context that determines the look ahead
     * content and initialized with the specified text and width.
     *
     * @param text The content of the text field.
     * @param columns The width of the text field.
     * @param context The look ahead context.
     * @param cb The combobox.
     */
    public ComboBoxLookAheadEditor(String text, int columns,
        LookAheadContext context, JComboBox cb) {
        super(text, columns, context);
        cb.addActionListener(this);
    }

    /**
     * Sets the combobox which is the parent component of this editor.
     *
     * @param cb The combobox.
     */
    public void setComboBox(JComboBox cb) {
        super.setComboBox(cb);
        cb.addActionListener(this);
    }

    /**
     * Sets the text in the editor [DISABLED]. This method disables the
     * <CODE>setText</CODE> method. To set the text use the setSelectedItem()
     * method in the combobox. If you really want to set the text in the
     * editor itself, use <CODE>setSuperText()</CODE>.
     *
     * @param text The text.
     *
     * @see javax.swing.JComboBox#setSelectedItem
     * @see #setSuperText
     */
    public void setText(String text) {
        //System.out.println("ComboLookAheadTextField: text="+text);
    }

    /**
     * Sets the text in the editor itself (not the selected item in the
     * combobox.
     *
     * @param text The text.
     *
     * @see javax.swing.JComboBox#setSelectedItem
     * @see #setText
     */
    public void setSuperText(String text) {
        //System.out.println("ComboLookAheadTextField: supertext="+text);
        super.setText(text);
    }

    /**
     * Called when the selection in the combobox is changed.
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        String item = (String) cb.getSelectedItem();

        if (cb.getSelectedIndex() >= 0) {
            super.setTextAtCursorPosition(item);
        }
    }
}
