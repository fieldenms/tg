/*
 * @(#)AbstractListIntelliHints.java 7/24/2005
 *
 * Copyright 2002 - 2005 JIDE Software Inc. All rights reserved.
 */
package ua.com.fielden.platform.swing.components.smart.autocompleter;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;

import ua.com.fielden.platform.swing.components.smart.AbstractIntelliHints;
import ua.com.fielden.platform.swing.components.smart.State;
import ua.com.fielden.platform.swing.components.textfield.caption.CaptionTextFieldLayer;

/**
 * <code>AutocompleterLogic</code> extends {@link AbstractListIntelliHints} by providing custom list with renderer for instances of AbstractEntity interface.
 * 
 * Method {@link #findMatches(String)} needs to be implemented in order to provide the list of values used for autocompletion. Method {@link #cancelFindMatches(String)} should be
 * implemented to provide a custom logic for cancelling findMatches operation. For example, for Hibernate it can be something like session.cancelQuery(); Please note that this
 * method executes on EDT and therefore should not contain lengthy operations.
 * 
 * However, it should be a rare need to directly extends <code>AutocompleterLogic</code>. See {@link CaptionTextFieldLayer} for more details.
 * 
 * @author 01es
 */
public abstract class AutocompleterLogic<T> extends AbstractListIntelliHints implements FocusListener {
    /**
     * Indicates whether comparison is case sensitive during the composition of matching values.
     */
    private boolean caseSensitive;
    /**
     * This variable is used to hold the most appropriate text component caret position. It is updated in method {@link #getSelectedHint()} and used in method
     * {@link #acceptHint(Object)}.
     */
    private Integer caretPosition = null;
    private final String valueSeparator;

    private boolean whildcardSupport = true;

    /**
     * This is the component responsible for rendering and handling events for smart button
     */
    private final AutocompleterTextFieldLayer<T> layeredTextComponent;

    /**
     * Class representing the type of the instances used for autocomplition.
     */
    private final Class<T> lookupCass;
    /**
     * List of property expressions used during autocomplition. The first expression is always used for selection -- it appears in the text filed upon selection.
     */
    private final Expression propertyExpression;

    /**
     * Creates a completion for OverlayableTextField with the specified value separator.
     * 
     * @param layeredTextComponent
     *            -- layer, which implements smart button behaviour
     * @param separator
     *            -- used for value separation in case of multi-value selection support; if this values is empty or null then multi-selection is turned off.
     * @param customListCellRenderer
     *            -- the name says it all
     * @param lookupCass
     *            -- representing the type of the instances used for autocomplition
     * @param expression
     *            -- a property from class <code>lookupCass</code> that is used for display when lookup value(s) is(are) selected; if it is null then toString() is is used on the
     *            lookup value(e) itself.
     */
    public AutocompleterLogic(final AutocompleterTextFieldLayer<T> layeredTextComponent, final String separator, final ListCellRenderer customListCellRenderer, final Class<T> lookupCass, final String expression) {
	super(layeredTextComponent.getView(), customListCellRenderer);
	this.layeredTextComponent = layeredTextComponent;

	this.lookupCass = lookupCass;
	try {
	    // here "entity" is used as a place holder for the provided entity instance
	    propertyExpression = StringUtils.isEmpty(expression.trim()) ? null : ExpressionFactory.createExpression(("entity." + expression.trim()));
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalArgumentException("Failed to create expression " + expression + " for type " + lookupCass.getName() + ": " + e.getMessage());
	}

	// When popup is displayed and user clicks on the text component then popup automatically hides, which leads to stale Accept state of the SmartButton.
	// In order to handle this situation textComponent needs a MouseListener to update SmartButton state.
	//
	// Previously this implementation was not taking into account the fact that textComponent receives mouse click also when the smart button is clicked.
	// This has been corrected by testing isMouseOver.
	getTextComponent().addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		super.mouseClicked(e);
		if (!isHintsPopupVisible() && !layeredTextComponent.getUi().isMouseOver()) {
		    State.NONE.next(layeredTextComponent.getUi());
		}
	    }
	});

	valueSeparator = separator;

	layeredTextComponent.getView().addFocusListener(this);
	// initialise selection model based on isMultiValued() value
	// this code could not be put into createList() method simply because valueSeparator is not initialise yet when super constructor is invoked
	final ListSelectionModel model = getList().getSelectionModel();
	model.setSelectionMode(isMultiValued() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public JTextField getTextComponent() {
	return (JTextField) super.getTextComponent();
    }

    public void focusGained(final FocusEvent e) {
	State.NONE.next(layeredTextComponent.getUi());
    }

    public void focusLost(final FocusEvent e) {
	cancelSwingWorker(findMatchesWorker); // cancel any retrieval if control looses focus
	updatePreValue("");
	if ("*".equals(getTextComponent().getText())) {
	    getTextComponent().setText("");
	}
	layeredTextComponent.getUi().getState().next(layeredTextComponent.getUi());
	clear();
    }

    /**
     * Sets list data to an empty list.
     */
    public void clear() {
	setListData(new ArrayList<T>());
    }

    /**
     * This method should be implemented to provide logic for obtaining a list of matching to <code>value</code> entities.
     * 
     * @param value
     * @return
     */
    protected abstract List<T> findMatches(final String value);

    /**
     * This method should be implemented to provide a custom logic for cancelling findMatches operation. For example, for Hibernate it can be something like session.cancelQuery();
     * 
     * Please note that this method executes on EDT and therefore should not contain lengthy operations.
     * 
     * @param value
     * @return
     */
    protected void cancelFindMatches() {
    }

    /**
     * This field is used as part of {@link #updateHints(Object)} to identifies situations where previously typed value and the current one are equal. In such cases there is no
     * need to find matches again.
     */
    private String prevValue = "";

    private SwingWorker<List<T>, Void> findMatchesWorker = null;

    /**
     * Provides support for wild cards Taken out of updateHints() method to make this code reusable Created 12.09.2008 YN
     */
    public static String wrapTypedWord(final String typedWord, final boolean hasWildcardSupport) {
	return hasWildcardSupport && typedWord.contains("*") ? typedWord.replaceAll("\\*", "%") : (hasWildcardSupport ? typedWord + "%" : typedWord);
    }

    /**
     * <code>updateHints</code> incorporates the logic to determine the matching values for the provided context, and whether these values should be displayed. It also handles life
     * cycle of the progress indicator.
     */
    public boolean updateHints(final Object context) {
	final JTextField components = getTextComponent();
	if (context == null || StringUtils.isEmpty(context.toString())) {
	    cancelSwingWorker(findMatchesWorker);
	    updatePreValue("");
	    return false;
	}
	// obtained the word to be used for lookup
	final IntRange wordRange = wordRange(context.toString(), getTextComponent().getCaretPosition());
	final String text = context.toString().substring(wordRange.getMinimumInteger(), getTextComponent().getCaretPosition());
	if (StringUtils.isEmpty(text)) {
	    cancelSwingWorker(findMatchesWorker);
	    updatePreValue(text);
	    components.repaint();
	    return false;
	}
	// provide support for wild card
	/*
	 * 12.08.2008 YN - substituted this line final String typedWord = hasWhildcardSupport() && text.contains("*") ? text.replaceAll("\\*", "%") : (hasWhildcardSupport() ? text
	 * + "%" : text); with following
	 */
	final String typedWord = wrapTypedWord(text, hasWhildcardSupport());
	if (!typedWord.equals(prevValue)) {
	    cancelSwingWorker(findMatchesWorker);
	    updatePreValue(typedWord);
	    clear();// empty the list as the re-matching is about to begin.
	    // do the search for lookup values
	    findMatchesWorker = new SwingWorker<List<T>, Void>() {
		@Override
		protected List<T> doInBackground() throws Exception {
		    // kick in progress indicator
		    nextStage();
		    final List<T> result = findMatches(typedWord);
		    // remove null references
		    for (final Iterator<T> iter = result.iterator(); iter.hasNext();) {
			if (iter.next() == null) {
			    iter.remove();
			}
		    }
		    return result;
		}

		@Override
		protected void done() {
		    if (!isCancelled()) { // there is no need to proceed with popup update if SwingWorker was cancelled
			try {
			    final List<T> result = get();
			    setListData(result == null ? new ArrayList<T>() : result);

			    if (result.size() > 0) { // there are matching values
				nextStage();
				showHintsPopup();
			    } else {
				State.NONE.next(layeredTextComponent.getUi());
				hideHintsPopup();
			    }
			} catch (final Exception e) {
			    e.printStackTrace();
			}
		    }
		}
	    };
	    findMatchesWorker.execute();
	}
	// need to show the list of hints only if there are some lookup values
	return getList().getModel() != null && getList().getModel().getSize() > 0;
    }

    private void nextStage() {
	layeredTextComponent.getUi().getState().next(layeredTextComponent.getUi());
    }

    /**
     * Cancels SwingWorkder performing value matching.
     * 
     * @param worker
     * @param otfField
     */
    private void cancelSwingWorker(final SwingWorker<?, ?> worker) {
	if (worker != null && !worker.isDone() && !worker.isCancelled()) {
	    worker.cancel(false); // was true
	    State.NONE.next(layeredTextComponent.getUi());
	    cancelFindMatches();
	}
    }

    private synchronized void updatePreValue(final String value) {
	if (StringUtils.isEmpty(value) && getTextComponent().hasFocus()) {
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    State.NONE.next(layeredTextComponent.getUi());
		}
	    });
	}
	prevValue = value;
    }

    /**
     * Creates the list to display the hints with a custom renderer.
     * 
     * @return the list.
     */
    @Override
    protected JList createList(final ListCellRenderer cellRenderer) {
	final JList list = new JList() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public int getVisibleRowCount() {
		final int size = getModel().getSize();
		return size < super.getVisibleRowCount() ? size : super.getVisibleRowCount();
	    }

	    @Override
	    public Dimension getPreferredScrollableViewportSize() {
		if (getModel().getSize() == 0) {
		    return new Dimension(0, 0);
		} else {
		    return super.getPreferredScrollableViewportSize();
		}
	    }
	};
	if (cellRenderer != null) {
	    list.setCellRenderer(cellRenderer);
	}
	return list;
    }

    /**
     * Gets the delegate keystrokes. Since we know the hints popup is a JList, we return twelve keystrokes so that they can be delegate to the JList. Those keystrokes are DOWN, UP,
     * PAGE_DOWN, PAGE_UP, HOME, END and the same ones but with SHIFT_DOWN_MASK.
     * 
     * @return the keystokes that will be delegated to the JList when hints popup is visible.
     */
    @Override
    public KeyStroke[] getDelegateKeyStrokes() {
	if (_keyStrokes == null) {
	    _keyStrokes = new KeyStroke[19];
	    _keyStrokes[0] = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
	    _keyStrokes[1] = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
	    _keyStrokes[2] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
	    _keyStrokes[3] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
	    _keyStrokes[4] = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);
	    _keyStrokes[5] = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);

	    _keyStrokes[6] = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK);
	    _keyStrokes[7] = KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK);
	    _keyStrokes[8] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.SHIFT_DOWN_MASK);
	    _keyStrokes[9] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.SHIFT_DOWN_MASK);
	    _keyStrokes[10] = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_DOWN_MASK);
	    _keyStrokes[11] = KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.SHIFT_DOWN_MASK);

	    _keyStrokes[12] = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK);
	    _keyStrokes[13] = KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK);
	    _keyStrokes[14] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_DOWN_MASK);
	    _keyStrokes[15] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_DOWN_MASK);
	    _keyStrokes[16] = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_DOWN_MASK);
	    _keyStrokes[17] = KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_DOWN_MASK);
	    _keyStrokes[18] = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK);
	}
	return _keyStrokes;
    }

    @Override
    protected void hideHintsPopup() {
	super.hideHintsPopup();
	if (!isKeyTyped()) { // this indicates key ESC
	    State.NONE.next(layeredTextComponent.getUi());
	}
    }

    /**
     * Sets the list data.
     * 
     * @param entities
     */
    @SuppressWarnings("unchecked")
    protected void setListData(final List<T> entities) {
	super.setListData(entities.toArray((T[]) new Object[] {}));
    }

    /**
     * Returns the text composed based on the already existing one and selected from the list value. Also, it updates the position of the caret in the text component associated
     * with this autocompleter.
     */
    @Override
    public Object getSelectedHint() {
	if (getList().getSelectedValues().length > 0) {
	    final StringBuffer buffer = new StringBuffer();
	    for (final Object element : getList().getSelectedValues()) {
		buffer.append(value(lookupCass.cast(element)) + (isMultiValued() ? valueSeparator : ""));
	    }
	    final String selectedWord = buffer.toString().substring(0, isMultiValued() ? buffer.toString().length() - 1 : buffer.toString().length());
	    // calculate caret position
	    caretPosition = wordRange(getTextComponent().getText(), getTextComponent().getCaretPosition()).getMinimumInteger() + selectedWord.length();
	    return assembleWords(getTextComponent().getText(), selectedWord, getTextComponent().getCaretPosition());
	} else {
	    return null;
	}
    }

    /**
     * Returns text that is composed based on the existing one and selected from the entity locator dialog box. Also it updates the caret position in the text component.
     * 
     * @param selectedEntities
     *            - entities selected in the entity locator.
     * @param startSelectedIndex
     *            - the start index of the selected text in the text component.
     * @param endSelectedIndex
     *            - the end index of the selected text in the text component.
     * @param previousCaretPosition
     *            - previous caret position in the text component.
     * @return
     */
    public Object getSelectedHint(final List<?> selectedEntities, final int startSelectedIndex, final int endSelectedIndex, final int previousCaretPosition) {
	if (selectedEntities.size() > 0) {
	    final StringBuffer buffer = new StringBuffer();
	    for (final Object element : selectedEntities) {
		buffer.append(value(lookupCass.cast(element)) + (isMultiValued() ? valueSeparator : ""));
	    }
	    final String selectedWord = buffer.toString().substring(0, isMultiValued() ? buffer.toString().length() - 1 : buffer.toString().length());
	    // calculate caret position
	    if (!isMultiValued()) {
		caretPosition = selectedWord.length();
	    } else {
		if (startSelectedIndex == endSelectedIndex) {
		    caretPosition = previousCaretPosition + selectedWord.length();
		} else {
		    caretPosition = startSelectedIndex + selectedWord.length();
		}
	    }
	    return assembleWords(getTextComponent().getText(), selectedWord, startSelectedIndex, endSelectedIndex, previousCaretPosition);
	} else {
	    return null;
	}
    }

    /**
     * Returns text that consists of parts of text and selectedWord. When the startSelectedIndex and endSelectedIndex are equal then selectedWord will be insert at
     * previousCaretPosition otherwise the text that is between startSelectedIndex and endSelectedIndex in the text parameter will be replaced with selectedWord.
     * 
     * @param text
     * @param selectedWord
     * @param startSelectedIndex
     * @param endSelectedIndex
     * @param previousCaretPosition
     * @return
     */
    private Object assembleWords(final String text, final String selectedWord, final int startSelectedIndex, final int endSelectedIndex, final int previousCaretPosition) {
	if (!isMultiValued()) {
	    return selectedWord;
	}
	String textBefore, textAfter;
	if (startSelectedIndex == endSelectedIndex) {
	    textBefore = text.substring(0, previousCaretPosition);
	    textAfter = text.substring(previousCaretPosition);
	} else {
	    textBefore = text.substring(0, startSelectedIndex);
	    textAfter = text.substring(endSelectedIndex);
	}
	return textBefore + selectedWord + textAfter;
    }

    /**
     * Evaluates property expression for the passed instance.
     * 
     * If <code>propertyExpression</code> is null then it return instance itself. This is useful when it is necessary to make autocompleter for enumerations etc.
     * 
     * This method can be conveniently used to obtain value of the property, which is associated with this instance of the autocompleter logic.
     * 
     * @param expressionIndex
     * @return
     */
    @SuppressWarnings("unchecked")
    public final Object value(final T entity) {
	if (propertyExpression == null) {
	    return entity;
	} else {
	    final JexlContext jc = JexlHelper.createContext();
	    jc.getVars().put("entity", entity);
	    try {
		return propertyExpression.evaluate(jc);
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new RuntimeException("Failed to evaluate expression " + propertyExpression + ": " + e.getMessage());
	    }
	}
    }

    /**
     * Overridden to clear selection.
     */
    @Override
    public void showHints() {
	resetSelection();
	super.showHints();
	if (isHintsPopupVisible()) { // if popup is shown then this is the result of the search
	    State.SEARCH.next(layeredTextComponent.getUi());
	}
    }

    /**
     * Overrides {@link AbstractIntelliHints#acceptHint(Object)} in order to position caret at the end of the inserted word rather than the end of the whole text (default
     * implementation).
     */
    @Override
    public void acceptHint(final Object selected) {
	super.acceptHint(selected); // super implementation has to be invoked as it does all the magic with updating of the text component associated with this autocompleter
	if (caretPosition != null) { // just in case if this variable has not been updated
	    final int pos = getTextComponent().getCaretPosition();
	    try {
		getTextComponent().setCaretPosition(caretPosition);
	    } catch (final Exception e) {
		getTextComponent().setCaretPosition(pos);
	    }
	}
    }

    /**
     * Assembles words based on current list of words, just selected word and caret position, which determines where selected word needs to be placed.
     * 
     * @param words
     *            -- a string representing list of words separate by the specified separator.
     * @param selectedWord
     *            -- word selected from the list and needs to be prepended/inserted/appended into the current list of words.
     * @param caretPos
     *            -- position within in the list of words indicating in what word we're interested.
     * @param separator
     *            -- separator, can be "," or anything else.
     * @return
     */
    public String assembleWords(final String words, final String selectedWord, final int caretPos, final String separator) {
	final IntRange wordRange = wordRange(words, caretPos, separator);
	final String textBefore = words.substring(0, wordRange.getMinimumInteger());
	final String textAfter = words.substring(wordRange.getMaximumInteger());
	return textBefore + selectedWord + textAfter;
    }

    /**
     * This is just a convenience method to utilise value separator provided upon instance construction.
     */
    public String assembleWords(final String words, final String selectedWord, final int caretPos) {
	return assembleWords(words, selectedWord, caretPos, valueSeparator);
    }

    /**
     * Calculates the position of the word in the list of words pointed to by the specified caret position.
     * 
     * @param words
     *            -- a string representing list of words separate by the specified separator
     * @param caretPos
     *            -- position within in the list of words indicating in what word we're interested.
     * @param separator
     *            -- separator, can be "," or anything else.
     * @return the IntRange, where getMinimumInteger() is the index of the first character in the word and getMaximumInteger() point to right index of the character next to the
     *         last character in the word (this is convenient as all string routines use the right non-inclusive right index).
     * 
     *         Example 1:</br> range = wordRange("01234;567;89", 3, ";"); In this case range.getMinimumInteger() == 0, and range.getMaximumInteger() == 5. So,
     *         "01234;567;89".substring(range.getMinimumInteger(), range.getMaximumInteger()) equals "0123" </br> Example 2:</br> range = wordRange("01234,567,89", 6, ","); In this
     *         case range.getMinimumInteger() == 6, and range.getMaximumInteger() == 9. So, "01234,567,89".substring(range.getMinimumInteger(), range.getMaximumInteger()) equals
     *         "567".
     */
    public IntRange wordRange(final String words, final int caretPos, final String separator) {
	int leftCommaPos = StringUtils.lastIndexOf(words.substring(0, caretPos), separator);
	int rightCommaPos = StringUtils.indexOf(words, separator, caretPos); // indexOf return the index of the first comma occurrence
	// if leftCommaPos points to a comma then need to point to the next character, otherwise start with very first character
	leftCommaPos = leftCommaPos >= 0 ? leftCommaPos + 1 : 0;
	// if rightCommaPos >= 0 then it points to a comma, otherwise it can be considered that the word is everything from left comma position to the end of the text
	rightCommaPos = rightCommaPos >= 0 ? rightCommaPos : words.length();
	return new IntRange(leftCommaPos, rightCommaPos);
    }

    /**
     * This is just a convenience method to utilise value separator provided upon instance construction.
     */
    public IntRange wordRange(final String words, final int caretPos) {
	return wordRange(words, caretPos, valueSeparator);
    }

    /**
     * This is a convenience method for extracting a word at the specified caret position.
     * 
     * @param words
     * @param caretPos
     * @param separator
     * @return
     */
    public String extractWord(final String words, final int caretPos, final String separator) {
	final IntRange wordRange = wordRange(words, caretPos, separator);
	return words.substring(wordRange.getMinimumInteger(), wordRange.getMaximumInteger());
    }

    /**
     * Checks if it used case sensitive search. By default it's false.
     * 
     * @return if it's case sensitive.
     */
    public boolean isCaseSensitive() {
	return caseSensitive;
    }

    /**
     * Sets the case sensitive flag. By default, it's false meaning it's a case insensitive search.
     * 
     * @param caseSensitive
     */
    public void setCaseSensitive(final boolean caseSensitive) {
	this.caseSensitive = caseSensitive;
    }

    /**
     * Returns true if the component was configured to support wiled card (default).
     * 
     * @return
     */
    public boolean hasWhildcardSupport() {
	return whildcardSupport;
    }

    /**
     * Allows overriding of the default support for wild card.
     * 
     * @param whildcardSupport
     */
    public void setWhildcardSupport(final boolean whildcardSupport) {
	this.whildcardSupport = whildcardSupport;
    }

    /**
     * Determines whether autocompleter was configured to serve for multiple value selection.
     * 
     * @return
     */
    public boolean isMultiValued() {
	return !StringUtils.isEmpty(valueSeparator);
    }

    public String getPrevValue() {
	return prevValue;
    }

    public String getValueSeparator() {
	return valueSeparator;
    }

    protected AutocompleterTextFieldLayer<T> getLayeredTextComponent() {
	return layeredTextComponent;
    }

    public Class<T> getLookupCass() {
	return lookupCass;
    }
}