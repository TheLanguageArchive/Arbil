/*
 * LookAheadTextField.java
 *
 * Created on December 18, 2000, 9:22 AM
 */
package mpi.metadata.tools.mdeditor.app.gui;

import java.awt.IllegalComponentStateException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;

import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


/**
 * This class implements a text field with look ahead. A lookahead context has
 * to be provided.
 *
 * @author Don Willems
 * @version 0.2
 *
 * @see LookAheadContext
 */
/*
 *Made changes to support wizard string: everything starting with '%%'
 *is accepted in the textfield
 *DGB. march 2007
 */
public class LookAheadTextField extends JTextField implements ComboBoxEditor,
    FocusListener {
    private LookAheadContext context;
    private Vector listeners = new Vector();
    private boolean validation = false;
    private Vector aliasses = new Vector();
    private boolean doAlias = true;
    private boolean doCorrect = true;
    private JComboBox combobox;
    private String command;
    private boolean doComboBoxUpdate = true;
    private boolean updatingComboBox = false;

    /**
     * Creates an empty text field with a context that determines the look
     * ahead content.
     *
     * @param context The look ahead context.
     */
    public LookAheadTextField(LookAheadContext context) {
        super();
        addFocusListener(this);
        setActionCommand("LOOKAHEADSET");
        this.context = context;
    }

    /**
     * Creates a text field with a context that determines the look ahead
     * content and initialized with the specified text.
     *
     * @param text The content of the text field.
     * @param context The look ahead context.
     */
    public LookAheadTextField(String text, LookAheadContext context) {
        super(text);
        //System.out.println("LookAheadTextField: constr txt=" + text);
        addFocusListener(this);
        setActionCommand("LOOKAHEADSET");
        this.context = context;
    }

    /**
     * Creates a text field with a context that determines the look ahead
     * content and initialized with the specified width.
     *
     * @param columns The width of the text field.
     * @param context The look ahead context.
     */
    public LookAheadTextField(int columns, LookAheadContext context) {
        super(columns);
        addFocusListener(this);
        setActionCommand("LOOKAHEADSET");
        this.context = context;
    }

    /**
     * Creates a text field with a context that determines the look ahead
     * content and initialized with the specified text and width.
     *
     * @param text The content of the text field.
     * @param columns The width of the text field.
     * @param context The look ahead context.
     */
    public LookAheadTextField(String text, int columns, LookAheadContext context) {
        super(text, columns);
        addFocusListener(this);
        setActionCommand("LOOKAHEADSET");
        this.context = context;
    }

    /**
     * Sets the combobox that is used to present the user with possible
     * choices.
     *
     * @param box The combobox.
     *
     * @see #getComboBox
     */
    public void setComboBox(JComboBox box) {
        combobox = box;
    }

    /**
     * Returns the combobox used to present the user with possible choices.
     *
     * @return The combobox.
     *
     * @see #setComboBox
     */
    public JComboBox getComboBox() {
        return combobox;
    }

    /**
     * Changes the content and selection in the text field. The new content is
     * determined by the look ahead context which starts with the old content.
     * The selection is set nfrom the end of the old content to the end of the
     * new content. The caret is set at the end of the old content. Therefore
     * if the user types a letter, the selection is replaced by that letter,
     * and a new context is determined. This method is not meant to be called
     * by other classes than the classes provided by Java.
     *
     * @param content The old content.
     */
    public void replaceSelection(String content) {
        //System.out.println("-------------------- replaceSelection content="+content);
        super.replaceSelection(content);
        //try{ throw new Exception("test"); } catch(Exception e){e.printStackTrace();}
        //if(context==null) System.out.println("NO CONTEXT");
        if (!isEnabled() || !isEditable() || (context == null)) {
            return;
        }

        Document doc = getDocument();

        try {
            doAlias = false;

            String oldContent = doc.getText(0, doc.getLength());
            Context foundcontext = context.getContext(oldContent,
                    getCaretPosition());

            if (combobox != null) {
                fillComboBox(oldContent, foundcontext);
            }

            if (doc.getLength() <= 0) {
                return;
            }

            String newContent = foundcontext.getContext();

            // FIX
            String comm = "";

            // FIX
            String comm2 = "";

            if (combobox != null) {
                comm = combobox.getActionCommand();
                comm2 = getActionCommand();
                combobox.setActionCommand("updating combobox");
                setActionCommand("updating combobox");
            }

            //
            ActionEvent ev = new ActionEvent(this, 9209, oldContent);

            //for(int i=0;i<listeners.size();i++) ((ActionListener)listeners.elementAt(i)).actionPerformed(ev);
            // FIX
            if (combobox != null) {
                combobox.setActionCommand(comm);
                setActionCommand(comm2);
            }

            //
            //           int caret = getCaretPosition();
            //         int l = newContent.length();
            if (combobox != null) {
                fillComboBox(oldContent, foundcontext);
            }

            super.setText(newContent);

            //System.out.println(newContent+"    "+getText()+"  "+oldContent);
            setCaretPosition(foundcontext.getSelectionInterval().end);
            moveCaretPosition(foundcontext.getSelectionInterval().start);
            doAlias = true;
        } catch (BadLocationException e) {
        }
         //never happens!
    }

    private void fillComboBox(String content, Context foundcontext) {
        //	if(!doComboBoxUpdate||updatingComboBox) return;
        updatingComboBox = true;
        try {
            String comm = combobox.getActionCommand();
            String comm2 = getActionCommand();
            combobox.setActionCommand("updating combobox");
            setActionCommand("updating combobox");
            String item = foundcontext.getContextAtPosition();
            combobox.setSelectedItem(item);
            try {
                combobox.showPopup();
            } catch (IllegalComponentStateException e) {
            }

            combobox.setActionCommand(comm);
            setActionCommand(comm2);
            super.setText(content);
        } catch (Exception e) {
        }
//try{ throw new Exception("test"); }catch (Exception e){e.printStackTrace(); }
        updatingComboBox = false;
    }

    /**
     * Returns the action command for this component.
     *
     * @return The action command.
     *
     * @see #setActionCommand
     */
    public String getActionCommand() {
        return command;
    }

    /**
     * Sets the command string used for action events.
     *
     * @param command The action command string.
     *
     * @see #getActionCommand
     */
    public void setActionCommand(String command) {
        super.setActionCommand(command);
        this.command = command;
    }

    /**
     * Returns the content of this component and checks whether aliasses have
     * been used. Aliasses that have been found are replaced by their source.
     *
     * @return The text in this LookAheadTextField.
     *
     * @see #setText
     */
    public String getText() {
        String text = super.getText();

        if (aliasses != null) {
            for (int i = 0; i < aliasses.size(); i++) {
                LookAheadAlias alias = (LookAheadAlias) aliasses.elementAt(i);

                for (int o = 0; o < text.length(); o++) {
                    if (text.regionMatches(o, alias.getAlias(), 0,
                                alias.getAlias().length()) &&
                            (alias.getSource().length() > 0)) {
                        text = text.substring(0, o) + alias.getSource() +
                            text.substring(o + alias.getAlias().length());
                    }
                }
            }
        }

        return text;
    }

    /**
     * Sets the text of the field.
     *
     * @param text The text.
     *
     * @see #getText
     */
    public void setText(String text) {
        text = text.replace('\n', ' ');
        doCorrect = false;
        super.setText(text);
        doCorrect = true;
    }

    /**
     * Sets the text starting from the current cursor position.
     *
     * @param text The text.
     */
    public void setTextAtCursorPosition(String text) {
        //	if(updatingComboBox||!doComboBoxUpdate) return;
        if (context == null) {
            super.setText(text);
        } else {
            int pos = getCaretPosition();
            SelectionInterval si = context.getItemSelectionAtPosition(getText(),
                    this.getCaretPosition());
            doComboBoxUpdate = false;

            String tx = this.getText();
            String tx1 = tx.substring(0, si.start);
            String tx2 = tx.substring(si.end);
            super.setText(tx1 + text + tx2);
            doComboBoxUpdate = true;

            int posit = si.start + text.length();

            if (posit < 0) {
                posit = 0;
            }

            if (posit > (tx1 + text + tx2).length()) {
                posit = (tx1 + text + tx2).length();
            }

            setCaretPosition(si.start + text.length());

            //	moveCaretPosition(si.start);
        }
    }

    /**
     * Returns the LookAheadContext for this LookAheadTextField.
     *
     * @return The LookAheadContext.
     *
     * @see #setLookAheadContext
     */
    public LookAheadContext getLookAheadContext() {
        return context;
    }

    /**
     * Sets the LookAheadContext for this LookAheadTextField.
     *
     * @param context The new LookAheadContext.
     *
     * @see #getLookAheadContext
     */
    public void setLookAheadContext(LookAheadContext context) {
        this.context = context;
    }

    /**
     * Returns a new instance of the default model for this text field, a
     * LookAheadDocument. It is used at construction time.
     *
     * @return the default document model.
     */
    protected Document createDefaultModel() {
        LookAheadDocument doc = new LookAheadDocument(validation);
        doc.addDocumentListener(new LookAheadDocumentListener());

        return doc;
    }

    /**
     * Returns the validation policy which determines the fact whether the user
     * can enter any data or is only allowed to add data which is available in
     * the context.
     *
     * @return the validation policy.
     *
     * @see #setValidationPolicy
     */
    public boolean getValidationPolicy() {
        return validation;
    }

    /**
     * Sets the validation policy which determines the fact whether the user
     * can enter any data or is only allowed to add data which is available in
     * the context.
     *
     * @param pol the validation policy.
     *
     * @see #getValidationPolicy
     */
    public void setValidationPolicy(boolean pol) {
        validation = pol;
        ((LookAheadDocument) getDocument()).setValidationPolicy(pol);
    }

    /**
     * Adds an alias to the set of aliasses.
     *
     * @param alias The alias to be added.
     *
     * @see #removeAlias
     */
    public void addAlias(LookAheadAlias alias) {
        aliasses.addElement(alias);
    }

    /**
     * Removes an alias from the set of aliasses.
     *
     * @param alias The alias to be removed.
     *
     * @see #addAlias
     */
    public void removeAlias(LookAheadAlias alias) {
        aliasses.removeElement(alias);
    }

    /**
     * Checks the content for aliasses and replaces when thy occur.
     */
    public void checkForAliasses() {
        setText(getText()); /*
           if(aliasses!=null)if(aliasses.size()>0 && doAlias){
               String oldContent = getText();
               for(int i=0;i<aliasses.size();i++){
                       LookAheadAlias alias = (LookAheadAlias)aliasses.elementAt(i);
                       for(int o=0;o<oldContent.length()-alias.getSource().length()+1;o++){
                               if(oldContent.regionMatches(o,alias.getSource(),0,alias.getSource().length())){
                                   oldContent = oldContent.substring(0,o)+alias.getAlias()+oldContent.substring(o+alias.getSource().length());
                                   setText(oldContent);
                               }
                       }
               }
           } */
    }

    /**
     * Returns the component used as editor.
     *
     * @return The editor component.
     */
    public java.awt.Component getEditorComponent() {
        return this;
    }

    /**
     * Returns the selected item.
     *
     * @return The item.
     *
     * @see #setItem
     */
    public java.lang.Object getItem() {
        return getText();
    }

    /**
     * Removes the specified action listener.
     *
     * @param p1 The listener.
     *
     * @see #addActionListener
     */
    public void removeActionListener(final java.awt.event.ActionListener p1) {
        listeners.removeElement(p1);
        super.removeActionListener(p1);
    }

    /**
     * Selects all the text.
     */
    public void selectAll() {
        super.selectAll();
    }

    /**
     * Adds an action listener to be notified when an action event is generated
     * in this component.
     *
     * @param p1 The listener.
     *
     * @see #removeActionListener
     */
    public void addActionListener(final java.awt.event.ActionListener p1) {
        listeners.addElement(p1);
        super.addActionListener(p1);
    }

    /**
     * Adds an action listener to the super class to be notified when an action
     * event is generated in this component.
     *
     * @param p1 The listener.
     *
     * @see #removeActionListener
     */
    public void addActionListenerToSuper(final java.awt.event.ActionListener p1) {
        super.addActionListener(p1);
    }

    /**
     * Sets the selected item.
     *
     * @param p1 The item.
     *
     * @see #getItem
     */
    public void setItem(final java.lang.Object p1) {
        String p = "";

        if (p1 == null) {
            return;
        }

        if (!(p1 instanceof String)) {
            p = p1.toString();
        } else {
            p = (String) p1;
        }

        setText((String) p);
    }

    /**
     * Called when the component gains focus.
     *
     * @param p1 The focus event.
     */
    public void focusGained(final java.awt.event.FocusEvent p1) {
    }

    /**
     * Called when the component looses focus.
     *
     * @param p1 The focus event.
     */
    public void focusLost(final java.awt.event.FocusEvent p1) {
        if (validation) {
            Context fcon = context.getContext(getText(), getCaretPosition());

            if (context != null) {
                setText(fcon.getContext());
            }
        }
    }

    /**
     * DOCUMENT ME!
     * $Id$
     * @author $Author$
     * @version $Revision$
     */
    class LookAheadDocumentListener implements DocumentListener {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void changedUpdate(DocumentEvent e) {
        }

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void insertUpdate(DocumentEvent e) {
        }

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void removeUpdate(DocumentEvent e) {
            try {
            } catch (Exception er) {
            }
        }
    }

    /**
     * DOCUMENT ME!
     * $Id$
     * @author $Author$
     * @version $Revision$
     */
    class LookAheadDocument extends PlainDocument {
        private boolean validation = false;

        /**
         * Creates a new LookAheadDocument instance
         */
        public LookAheadDocument() {
            super();
        }

        /**
         * Creates a new LookAheadDocument instance
         *
         * @param policy DOCUMENT ME!
         */
        public LookAheadDocument(boolean policy) {
            validation = policy;
        }

        /**
         * Returns the validation policy which determines the fact whether the
         * user can enter any data or is only allowed to add data which is
         * available in the context.
         *
         * @return the validation policy.
         */
        public boolean getValidationPolicy() {
            return validation;
        }

        /**
         * Sets the validation policy which determines the fact whether the
         * user can enter any data or is only allowed to add data which is
         * available in the context.
         *
         * @param pol the validation policy.
         */
        public void setValidationPolicy(boolean pol) {
            validation = pol;

            if (pol && (context != null)) {
                Context fcon = context.getContext("", getCaretPosition());
                setText(fcon.getContext());
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param offs DOCUMENT ME!
         * @param str DOCUMENT ME!
         * @param a DOCUMENT ME!
         *
         * @throws BadLocationException DOCUMENT ME!
         */
        public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException {
            super.insertString(offs, str, a);

      //System.out.println("--------------------- insert string: "+str);
            if (validation && (context != null)) {
                String oldContent = this.getText(0, getLength());
                boolean cleanup = !oldContent.equals("%") && !oldContent.startsWith("%%");
                if (cleanup && getLength() > 0) {
                    if (!context.hasContext(oldContent, getCaretPosition())) {
                        Toolkit.getDefaultToolkit().beep();

                        if (doCorrect) {
                            super.remove(offs, str.length());
                        }
                    }
                }
            }

            /*
               if(aliasses!=null)if(aliasses.size()>0 && doAlias){
                   String oldContent = this.getText(0,getCaretPosition());
                   for(int i=0;i<aliasses.size();i++){
                           LookAheadAlias alias = (LookAheadAlias)aliasses.elementAt(i);
                           for(int o=0;o<oldContent.length()-alias.getSource().length()+1;o++){
                                   if(oldContent.regionMatches(o,alias.getSource(),0,alias.getSource().length())){
                                       if(!alias.getSource().equals("")){
                                           super.remove(o,alias.getSource().length());
                                           super.insertString(o,alias.getAlias(),a);
                                       }
                                   }
                           }
                   }
               }*/
        }

        /**
         * DOCUMENT ME!
         *
         * @param offs DOCUMENT ME!
         * @param len DOCUMENT ME!
         *
         * @throws BadLocationException DOCUMENT ME!
         */
        public void remove(int offs, int len) throws BadLocationException {
            String rem = this.getText(offs, len);

            if (validation && (context != null)) {
                String oldContent = this.getText(0, offs) +
                    this.getText(offs + len, getLength() - offs - len);
                    
                boolean wizardstr = oldContent.equals("%") || oldContent.startsWith("%%");
                if (getLength() > 0 && !wizardstr) {
                    if (!context.hasContext(oldContent, getCaretPosition())) {
                        Toolkit.getDefaultToolkit().beep();
                        System.out.println("********* ERROR IN CONTEXT: " +
                            oldContent);

                        return;
                    }
                }
            }

            super.remove(offs, len);
        }
    }
}
