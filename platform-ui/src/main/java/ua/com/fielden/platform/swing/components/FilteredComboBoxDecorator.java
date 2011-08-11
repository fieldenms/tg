package ua.com.fielden.platform.swing.components;

import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;

/**
 * This class provides one static method named {@link #decorate(JComboBox, ua.com.fielden.platform.swing.components.FilteredComboBoxDecorator.StringAutocompleter) decorate}, which
 * provides JComboBox instances with filtering capabilities based on the typed in value.
 * 
 * Class {@link ua.com.fielden.platform.swing.components.examples.FilteredComboBoxDecorator} provides an example how to use decorator.
 * 
 * @author Yura
 * 
 */
public class FilteredComboBoxDecorator {

    private JComboBox comboBox = null;

    private JTextComponent editor = null;

    private StringAutocompleter autoCompleter;

    /*
     * Constructs instance of FilteredComboBoxDecorator class that contains references to comboBox, autoCompleter, comboBox' editor and two listeners. This constructor is doing all
     * the work that is mentioned in decorate(...) method's JavaDocs
     */
    private FilteredComboBoxDecorator(final JComboBox comboBox, final StringAutocompleter autoCompleter) {
	// initialising references
	if (comboBox == null) {
	    throw new NullPointerException();
	}
	this.comboBox = comboBox;
	this.editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
	if (autoCompleter == null) {
	    throw new NullPointerException();
	}
	this.autoCompleter = autoCompleter;

	// comboBox should be editable to enable filtering while user types into comboBox' editor
	comboBox.setEditable(true);

	// removing all items from comboBox - its items should be obtained through StringAutoCompleter instance
	comboBox.removeAllItems();

	// adding listeners for managing user operations on comboBox
	comboBox.getModel().addListDataListener(listDataListener);
	editor.getDocument().addDocumentListener(documentListener);
	editor.addKeyListener(keyListener);
    }

    /**
     * Decorates comboBox, passed as parameter, in the following way : <br>
     * 1. Makes comboBox editable <br>
     * 2. Removes all items from it <br>
     * 3. Adds two listeners - ListDataListener instance to the model and DocumentListener instance to the document of comboBox editor <br>
     * comboBox items would be obtained ONLY through StringAutocompleter instance, specifically through its single method getStringsStartingWith(...) <br>
     * <br>
     * Note : this implementation does not support substitution of StringAutocompleter instance via subsequent calls to this method on the same JComboBox instance, so the result of
     * such actions may be unexpected. <br>
     * In order to achieve this try replacing an existing JComboBox instance with a new one and decorate it with required parameters.
     * 
     * @param comboBox
     *            - comboBox that will be decorated to enable filtering
     * @param autoCompleter
     *            - corresponding StringAutocompleter instance, that would be associated with this comboBox and used to obtain it's items. Its sole method 'String[]
     *            getStringsCorrespondingTo(String value)' will be called each time user changed the contents of comboBox' editor. So this method should return the list of items,
     *            that user should see in comboBox' drop-down list, when he had just entered text passed as 'value' parameter. If there are no items, corresponding to value
     *            parameter return empty(BUT NOT NULL) array of strings.
     */
    public static void decorate(final JComboBox comboBox, final StringAutocompleter autoCompleter) {
	// creating instance of FilteredComboBoxDecorator that implicitly associates comboBox with autoCompleter and makes all the decorations
	new FilteredComboBoxDecorator(comboBox, autoCompleter);
    }

    /**
     * This list data listener temporary disables the jComboBox editor's document listener, while user is selecting values from the drop-down list using arrow keys or mouse
     */
    private ListDataListener listDataListener = new ListDataListener() {

	public void contentsChanged(ListDataEvent e) {
	    // contents changed - user selected some other value using arrow keys, mouse or something else
	    // this will lead to changing text inside comboBox' editor, and this will fire event on document of comboBox' editor and this will lead to calling
	    // documentListener.contentsChanged(...) method - we should disallow this, because this is undesired behaviour
	    // we'll temporarily remove this listener from document
	    editor.getDocument().removeDocumentListener(documentListener);

	    // then we'll set text to the same as in selected item - and this will not cause calling of documentListener.contentsChanged(...) method, because documentListener is
	    // not in the document's list of listeners
	    editor.setText(comboBox.getSelectedItem().toString());

	    // then we add documentListener again to document of comboBox' editor
	    editor.getDocument().addDocumentListener(documentListener);
	}

	public void intervalAdded(ListDataEvent e) {
	}

	public void intervalRemoved(ListDataEvent e) {
	}
    };

    /*
     * This list data listener changes list model every time text in document of comboBox' editor changes.
     */
    private DocumentListener documentListener = new DocumentListener() {

	private String previousText = "";

	public void changedUpdate(DocumentEvent e) {
	}

	public void insertUpdate(DocumentEvent e) {
	    contentsChanged(e);
	}

	public void removeUpdate(DocumentEvent e) {
	    contentsChanged(e);
	}

	private void contentsChanged(DocumentEvent e) {
	    // contents of comboBox' editor changed - we should define whether there are some items corresponding to new text, and change comboBox' model
	    // because we are in listener, we should do this after this DocumentEvent will be dispatched, so we'll invoke this code later on the EDT ...
	    EventQueue.invokeLater(new Runnable() {
		public void run() {
		    if ("".equals(editor.getText())) {
			// lets allow user to delete text

			// there are some items corresponding to entered text, that is why we will have to change comboBox' model
			// while we are changing model, some Document or ListDataModel events may be fired, and our listeners may react, what may lead to undesirable behaviour
			// so we'll temporarily disable them
			comboBox.getModel().removeListDataListener(listDataListener);
			editor.getDocument().removeDocumentListener(documentListener);

			comboBox.hidePopup();
			// storing current text, because after model is substituted, it will change again
			comboBox.setModel(new DefaultComboBoxModel(new Object[] {}));
			// changing editor text back to what we had

			// enabling event listeners again
			editor.getDocument().addDocumentListener(documentListener);
			comboBox.getModel().addListDataListener(listDataListener);

			// this text in editor is considered as 'successful', because there are some items corresponding to it
			// lets save this text in private field, so when user tries to enter 'unsuccessful' text, we will set it again to 'successful' one
			// this behaviour disallow user to enter other items than those obtained from autoCompleter
			previousText = "";
			return;
		    }

		    // getting values for entered text
		    final String[] values = autoCompleter.getStringsCorrespondingTo(editor.getText());
		    if (values.length > 0) {
			// there are some items corresponding to entered text, that is why we will have to change comboBox' model
			// while we are changing model, some Document or ListDataModel events may be fired, and our listeners may react, what may lead to undesirable behaviour
			// so we'll temporarily disable them
			comboBox.getModel().removeListDataListener(listDataListener);
			editor.getDocument().removeDocumentListener(documentListener);

			comboBox.hidePopup();
			// storing current text, because after model is substituted, it will change again
			String editorText = editor.getText();
			// setting new comboBox' model with items
			comboBox.setModel(new DefaultComboBoxModel(values));
			// selecting first of items
			comboBox.setSelectedIndex(0);
			// changing editor text back to what we had
			editor.setText(editorText);
			comboBox.showPopup();

			// enabling event listeners again
			editor.getDocument().addDocumentListener(documentListener);
			comboBox.getModel().addListDataListener(listDataListener);

			// this text in editor is considered as 'successful', because there are some items corresponding to it
			// lets save this text in private field, so when user tries to enter 'unsuccessful' text, we will set it again to 'successful' one
			// this behaviour disallow user to enter other items than those obtained from autoCompleter
			previousText = editorText;
		    } else {
			// there are no items corresponding to entered text, that means it is 'unsuccessful' and that means we should return to last 'successful' text
			editor.setText(previousText);
		    }
		}
	    });
	}

    };

    /**
     * Additional key listener that is added to editor and it handles situations when there are some items in drop-down list and user didn't specified exactly what item he wants to
     * select, but he presses enter or escape. In such situation, this listener will automatically select first item from the list.
     */
    private KeyListener keyListener = new KeyAdapter() {
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_ENTER || event.getKeyCode() == KeyEvent.VK_ESCAPE) {
		// enter or esc is pressed so we should set text to first item in drop-down list if there is any
		// or otherwise to an empty string
		if (comboBox.getModel().getSize() > 0) {
		    int index = 0;
		    for (int i = 0; i < comboBox.getModel().getSize(); i++) {
			if (comboBox.getModel().getElementAt(i).equals(editor.getText())) {
			    index = i;
			    break;
			}
		    }
		    comboBox.setSelectedIndex(index);
		    editor.setText(comboBox.getModel().getSelectedItem().toString());
		} else {
		    editor.setText("");
		}
	    }
	}
    };

    /**
     * Interface with single method 'getStringsCorrespondingTo(...)' that should return array of strings that corresponds to passed String pattern
     * 
     * @author Yura
     */
    public static interface StringAutocompleter {

	public String[] getStringsCorrespondingTo(String value);

    }

}
