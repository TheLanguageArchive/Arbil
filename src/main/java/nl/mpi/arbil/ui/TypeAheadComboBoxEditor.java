package nl.mpi.arbil.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.Timer;
import nl.mpi.arbil.ui.fieldeditors.ControlledVocabularyComboBoxEditor;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class TypeAheadComboBoxEditor implements ComboBoxEditor {

    /**
     * Gets requested item from vocabulary by index
     * @param index
     * @return Requested item
     */
    protected abstract String getItemAt(int index);

    /**
     * 
     * @return Total number of items
     */
    protected abstract int getItemsCount();

    /**
     * 
     * @return Whether multiple items can be entered
     */
    protected abstract boolean isList();

    /**
     * 
     * @return Whether arbitrary items can be entered
     */
    protected abstract boolean isOpen();

    protected boolean isItemsDeletable() {
	return false;
    }
    
    protected boolean deleteItem(Object item){
	return false;
    }

    /**
     * Constructor. Call init() after this!
     * @param editor Editor component
     * @param initialValue Initial value for editor
     * @param originalValue Original value for editor (will revert to this at escape)
     * @param comboBox Combobox this will be editor for
     * @see init()
     */
    protected TypeAheadComboBoxEditor(JTextField editor, String initialValue, String originalValue, JComboBox comboBox) {
	this.editor = editor;
	if (comboBox != null) {
	    setComboBox(comboBox);
	    // Also set combobox item to initial value because it will feed its selected item
	    // back into the setItem method of this object
	    comboBox.setSelectedItem(initialValue);
	}

	this.originalValue = originalValue;
    }

    /**
     * Initializes editor. Initializes key and focus listeners and the timer. Must be called in constructor! 
     */
    protected final void init() {
	getTextField().addKeyListener(keyListener);
	getTextField().addFocusListener(focusListener);

	initTypeaheadTimer();
    }

    public void addActionListener(ActionListener l) {
	getTextField().addActionListener(l);
    }

    public String getCurrentValue() {
	return getCurrentValueString();
    }

    /**
     * Implements getEditorComponent
     */
    public Component getEditorComponent() {
	return getTextField();
    }

    /**
     * Implements ComboBoxEditor getItem
     * @param anObject
     */
    public Object getItem() {
	return getTextField().getText();
    }

    /**
     * @return the editor
     */
    public JTextField getTextField() {
	return editor;
    }

    public void removeActionListener(ActionListener l) {
	getTextField().removeActionListener(l);
    }

    public void selectAll() {
	getTextField().selectAll();
    }

    public final void setComboBox(JComboBox comboBox) {
	this.comboBox = comboBox;
	comboBox.setEditable(true);
    }

    /**
     * Implements ComboBoxEditor setItem
     * @param item
     */
    public void setItem(Object item) {
	if (!typingAhead) {
	    String itemString = item.toString();
	    // Decide what to do, depending on whether there are multiple values
	    if (itemString.indexOf(ControlledVocabularyComboBoxEditor.SEPARATOR()) >= 0) {
		// Set item value for entire text
		getTextField().setText(itemString);
	    } else {
		// Set only value currently being edited
		setEditorValue(itemString);
	    }
	}
    }

    private void handleTextEntryKey(KeyEvent e) {
	if (!typingAhead) {
	    if (!e.isActionKey()
		    && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED
		    && e.getKeyCode() != KeyEvent.VK_BACK_SPACE
		    && e.getKeyCode() != KeyEvent.VK_DELETE) {
		// Text is being typed. Start (or restart) timer, so that typeahead
		// is executed after last keystroke within delay
		startTypeaheadTimer(TYPEAHEAD_DELAY_SHORT());
	    } else if (!isOpen()
		    && (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
		    || e.getKeyCode() == KeyEvent.VK_DELETE)) {
		// In closed list, also autocomplete on backspace/delete but use
		// a longer delay so as not to make it impossible to remove characters
		// or even submit the (non-existent) entry
		startTypeaheadTimer(TYPEAHEAD_DELAY_LONG());
	    }
	}
    }

    private void handleNavigateComboKey(KeyEvent e) {
	if (e.getModifiers() == 0) {
	    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
		moveSelectedIndex(+1);
		e.consume();
	    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
		moveSelectedIndex(-1);
		e.consume();
	    } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
		moveSelectedIndex(+5);
		e.consume();
	    } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
		moveSelectedIndex(-5);
		e.consume();
	    }
	}
    }

    private synchronized void handleAutocompleteKey(KeyEvent e) {
	typingAhead = true;

	if (typeaheadTimer.isRunning()) {
	    typeaheadTimer.stop();
	}

	if (autoComplete()) {
	    // Completed current item, do not perform any more actions
	    // on this key event
	    e.consume();
	}
	typingAhead = false;
    }

    // TYPE-AHEAD AND AUTO-COMPLETE METHODS
    /**
     * Types ahead current item. If a match is found, remaining part of the
     * target string is selected.
     */
    private synchronized void typeAhead() {
	typingAhead = true;
	String currentEditorValue = getEditorValue();
	int matchIndex = getMatchingItem(currentEditorValue);
	if (matchIndex >= 0) {
	    // Match found
	    String match = getItemAt(matchIndex);
	    if (comboBox != null) {
		// Make combobox jump to the selected item
		comboBox.setSelectedItem(match);
	    }

	    int position = getTextField().getCaretPosition();
	    // Insert match into editor
	    setEditorValue(match);
	    // Set caret back to original position
	    getTextField().setCaretPosition(position);
	    // Select remaining part of match
	    getTextField().setSelectionStart(position);
	    getTextField().setSelectionEnd(position + match.length() - currentEditorValue.length());
	}
	typingAhead = false;
    }

    /**
     * Performs auto complete on current item. In effect, current selection is
     * skipped. If nothing is selected, nothing is skipped and false is returned
     *
     * @return Whether any action was taken
     */
    private boolean autoComplete() {

	if (getTextField().getSelectionEnd() > getTextField().getSelectionStart()) {
	    getTextField().setCaretPosition(getTextField().getSelectionEnd());
	    return true;
	}
	return false;
    }
    private int previousMatch = -1;

    /**
     * Tries to find an item that matches the given text
     * @param text Text to find match for
     * @return Index of matching item, guaranteed to be a String; -1 if no match
     */
    private int getMatchingItem(String text) {
	if (null != text && text.length() > 0) {
	    int itemsCount = getItemsCount();

	    // Try previous match first, in many cases it will match and there's
	    // no need to iterate over all items
	    if (previousMatch >= 0
		    && previousMatch < itemsCount
		    && ((String) getItemAt(previousMatch)).toLowerCase().startsWith(text.toLowerCase())) {
		return previousMatch;
	    }

	    for (int i = 0; i < itemsCount; i++) {
		String item = getItemAt(i);
		if (item != null) {
		    if (item.regionMatches(true, 0, text, 0, text.length())) {
			previousMatch = i;
			return i;
		    }
		}
	    }
	}
	return -1;
    }

    // EDITOR MANIPULATION & ACCESS HELPER METHODS
    /**
     * Creates a sanitized string of the current value(s) of the editor
     * @return
     */
    private String getCurrentValueString() {
	StringTokenizer st = new StringTokenizer(getTextField().getText(), Character.toString(SEPARATOR()));
	StringBuilder sb = new StringBuilder(st.countTokens());
	while (st.hasMoreTokens()) {
	    String token = st.nextToken().trim();
	    if (token.length() > 0) {
		sb.append(token);
		if (st.hasMoreTokens()) {
		    sb.append(SEPARATOR());
		}
	    }
	}
	return sb.toString();
    }

    /**
     * Gets value of the current (determined by caret position) text
     * @return
     */
    private String getEditorValue() {
	String value = getTextField().getText();
	int lastIndex = value.length();
	String separator = Character.toString(SEPARATOR());

	if (!value.contains(separator)) {
	    return value;
	} else {
	    int[] startEnd = getEditorCurrentStartEnd();
	    return value.substring(Math.min(startEnd[0], lastIndex), Math.min(startEnd[1], lastIndex));
	}
    }

    /**
     * Set the value of the CURRENT editor sub-item (i.e. whatever is between
     * the separators closest to the caret position
     * @param value
     */
    private void setEditorValue(String value) {
	int[] startEnd = getEditorCurrentStartEnd();
	String text = getTextField().getText();
	getTextField().setText(
		text.substring(0, startEnd[0]) // everything before
		.concat(value) // insert value
		.concat(text.substring(startEnd[1]))); // everything after
	getTextField().setCaretPosition(startEnd[0] + value.length());

    }

    /**
     * @return Array containing indices of [start, end] of current editor value
     */
    private int[] getEditorCurrentStartEnd() {

	String value = getTextField().getText();

	if (!isList()) {
	    // Single valued vocabulary is treated as single entry
	    return new int[]{0, value.length()};
	} else {
	    // Find bounds of item of current caret position
	    int lastIndex = value.length();
	    String separator = Character.toString(SEPARATOR());

	    int currentIndex = getEditorIndex();
	    int start = 0;

	    // Traverse editor value to find current start position
	    if (currentIndex > 0) {
		int current = 0;
		do {
		    start = value.indexOf(separator, Math.min(start, lastIndex)) + 1;
		    current++;
		} while (current < currentIndex);
	    }

	    // Find first bound after start position
	    int end = value.indexOf(separator, Math.min(start, lastIndex));
	    if (end <= 0) {
		end = lastIndex;
	    }
	    return new int[]{start, end};
	}
    }

    /**
     * Gets index for currentValues array that corresponds with current caret position in editor
     * @return
     */
    private int getEditorIndex() {
	String value = getTextField().getText();
	int position = getTextField().getCaretPosition();
	int index = 0;
	for (int i = 0; i < position; i++) {
	    if (value.charAt(i) == SEPARATOR()) {
		index++;
	    }
	}
	return index;
    }

    private void moveSelectedIndex(int delta) {
	int target = comboBox.getSelectedIndex() + delta;
	// Don't move up to the first item, as it contains the previous value of the field
	target = Math.max(target, 0);
	// Don't try to mobe below final item
	target = Math.min(target, comboBox.getItemCount() - 1);

	// Target should be in list and not equal to current target
	if (target >= 0 && target != comboBox.getSelectedIndex()) {
	    // Target should not be multi-valued
	    if (comboBox.getItemAt(target).toString().indexOf(SEPARATOR()) >= 0) {
		target = Math.min(target + 1, comboBox.getItemCount() - 1);
	    }
	    comboBox.setSelectedIndex(target);
	}
    }

    private void startTypeaheadTimer(int delay) {
	if (typeaheadTimer.isRunning()) {
	    typeaheadTimer.stop();
	}
	//typeaheadTimer.setDelay(delay);
	//typeaheadTimer.restart();
	typeaheadTimer.setInitialDelay(delay);
	typeaheadTimer.start();

    }

    private void initTypeaheadTimer() {
	typeaheadTimer = new Timer(TYPEAHEAD_DELAY_SHORT(), new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		if (!typingAhead) {
		    typeaheadTimer.stop();
		    typeAhead();
		}
	    }
	});
	typeaheadTimer.setRepeats(false);
    }
    private final KeyListener keyListener = new KeyAdapter() {

	@Override
	public void keyPressed(KeyEvent e) {
	    if (isItemsDeletable() && comboBox.isPopupVisible() && KeyEvent.SHIFT_DOWN_MASK == (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) && e.getKeyCode() == KeyEvent.VK_DELETE) {
		deleteItem(getItem());
	    } else {
		if (!comboBox.isPopupVisible()) {
		    comboBox.setPopupVisible(true);
		} else {
		    if (e.getKeyCode() == KeyEvent.VK_ENTER || (isList() && e.getKeyChar() == ControlledVocabularyComboBoxEditor.SEPARATOR())) {
			// ENTER pressed or SEPARATOR in list field.
			// Autocomplete current item
			handleAutocompleteKey(e);
		    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			getTextField().setText(originalValue);
		    } else if (e.isActionKey()) {
			// Navigate combo items
			handleNavigateComboKey(e);
		    } else {
			// Probably text entry
			handleTextEntryKey(e);
		    }
		}
	    }
	}
    };
    private final FocusListener focusListener = new FocusListener() {

	public void focusGained(FocusEvent e) {
	    startTypeaheadTimer(ControlledVocabularyComboBoxEditor.TYPEAHEAD_DELAY_SHORT());
	}

	public void focusLost(FocusEvent e) {
	}
    };
    // Private members
    private JComboBox comboBox;
    private Timer typeaheadTimer;
    private String originalValue;
    private JTextField editor;
    /**
     * Flag indicating whether type ahead is in process
     */
    private boolean typingAhead = false;
    /**
     * Character that separates items in a list-type field
     */
    private static final char SEPARATOR = ',';
    /**
     * Response rate limit in milliseconds
     */
    private static final int TYPEAHEAD_DELAY_SHORT = 200;
    private static final int TYPEAHEAD_DELAY_LONG = 1000;

    /**
     * @return Item separator. Can be overridden, default is comma
     */
    protected static char SEPARATOR() {
	return SEPARATOR;
    }

    /**
     * Delay used for auto complete, except in closed lists where long delay is used
     * @return Short typeahead delay, can be overridden
     * @see TYPEAHEAD_DELAY_LONG()
     */
    protected static int TYPEAHEAD_DELAY_SHORT() {
	return TYPEAHEAD_DELAY_SHORT;
    }

    /**
     * Delay used for auto complete in <em>closed lists</em>, given users a chance to enforce their non-matching input
     * @return Long typeahead delay, can be overridden. 
     * @see TYPEAHEAD_DELAY_SHORT()
     */
    protected static int TYPEAHEAD_DELAY_LONG() {
	return TYPEAHEAD_DELAY_LONG;
    }
}
