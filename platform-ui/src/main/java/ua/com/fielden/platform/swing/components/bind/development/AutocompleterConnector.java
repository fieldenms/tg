package ua.com.fielden.platform.swing.components.bind.development;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.swing.components.bind.development.Binder.EditableChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.Binder.PropertyConnectorAdapter;
import ua.com.fielden.platform.swing.components.bind.development.Binder.PropertyValidationResultsChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.Binder.Rebinder;
import ua.com.fielden.platform.swing.components.bind.development.Binder.RequiredChangeListener;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterLogic;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * Connects a String typed alueModel and a JTextField or JTextArea. At construction time the text component content is updated with the subject's contents.
 * <p>
 * 
 * This connector has been designed for text components that display a plain String. In case of a JEditorPane, the binding may require more information then the plain String, for
 * example styles. Since this is outside the scope of this connector, the public constructors prevent a construction for general JTextComponents. If you want to establish a one-way
 * binding for a display JEditorPane, use a custom listener instead.
 * <p>
 * 
 * This class provides limited support for handling subject value modifications while updating the subject. If a Document change initiates a subject value update, the subject will
 * be observed and a property change fired by the subject will be handled - if any. In most cases, the subject will notify about a change to the text that was just set by this
 * connector. However, in some cases the subject may decide to modify this text, for example to ensure upper case characters. Since at this moment, this adapter's Document is still
 * write-locked, the Document update is performed later using <code>SwingUtilities#invokeLater</code>.
 * <p>
 * 
 * <strong>Note:</strong> Such an update will typically change the Caret position in JTextField's and other JTextComponent's that are synchronized using this class. Hence, the
 * subject value modifications can be used with commit-on-focus-lost text components, but typically not with a commit-on-key-typed component. For the latter case, you may consider
 * using a custom <code>DocumentFilter</code>.
 * <p>
 * 
 * <strong>Constraints:</strong> The alueModel must be of type <code>String</code>.
 * <p>
 * 
 * <strong>Examples:</strong>
 * 
 * <pre>
 * alueModel lastNameModel = new PropertyAdapter(customer, &quot;lastName&quot;, true);
 * JTextField lastNameField = new JTextField();
 * TextComponentConnector.connect(lastNameModel, lastNameField);
 * alueModel codeModel = new PropertyAdapter(shipment, &quot;code&quot;, true);
 * JTextField codeField = new JTextField();
 * AutocompleterConnector connector = new AutocompleterConnector(codeModel, codeField);
 * connector.updateTextComponent();
 * </pre>
 * 
 * @author Jhou:)
 * @version $Revision: 1.12 $
 * 
 * @see alueModel
 * @see Document
 * @see PlainDocument
 * 
 * @since 1.2
 */
public final class AutocompleterConnector<T> extends PropertyConnectorAdapter {
    /**
     * Refers to the text component that shall be synchronized with the subject.
     */
    private final AutocompleterTextFieldLayer<T> autocompleter;

    private final BoundedValidationLayer<AutocompleterTextFieldLayer<T>> boundedValidationLayer;

    private final JTextField autocompleterTextField;

    private final Logger logger = Logger.getLogger(this.getClass());
    private final static Logger staticLogger = Logger.getLogger(AutocompleterConnector.class);

    /**
     * Holds the text component's current document. Used for the rare case where the text component fires a PropertyChangeEvent for the "document" property with oldValue or
     * newValue == {@code null}.
     */
    private Document document;

    private final DocumentListener textChangeHandler;

    private final PropertyChangeListener documentChangeHandler;

    private final AutocompleterBufferedPropertyWrapper<T> autocompleterBufferedPropertyWrapper;

    // Instance Creation ******************************************************

    /**
     * Constructs a AutocompleterConnector that connects the specified String-typed subject alueModel with the given text area.
     * <p>
     * 
     * In case you don't need the AutocompleterConnector instance, you better use one of the static <code>#connect</code> methods. This constructor may confuse developers, if you
     * just use the side effects performed in the constructor; this is because it is quite unconventional to instantiate an object that you never use.
     * 
     * @param subject
     *            the underlying String typed alueModel
     * @param textArea
     *            the JTextArea to be synchronized with the alueModel
     * 
     * @throws NullPointerException
     *             if the subject or text area is {@code null}
     */
    @SuppressWarnings("unchecked")
    AutocompleterConnector(final AutocompleterBufferedPropertyWrapper<T> entity, final BoundedValidationLayer<AutocompleterTextFieldLayer<T>> boundedAutocompleterValidationLayer) {
        // initiate Entity and PropertyName
        super(entity, entity.getPropertyName());
        this.autocompleterBufferedPropertyWrapper = (AutocompleterBufferedPropertyWrapper<T>) this.entity;

        // initiate boundedValidationLayer
        if (boundedAutocompleterValidationLayer == null) {
            throw new NullPointerException("The validationLayer must not be null.");
        }
        this.boundedValidationLayer = boundedAutocompleterValidationLayer;

        // initiateEditableComponent
        this.autocompleter = boundedAutocompleterValidationLayer.getView();
        if (this.autocompleter == null) {
            throw new NullPointerException("The autocompleter of the validation layer must not be null.");
        }
        autocompleterTextField = autocompleter.getView();
        if (this.autocompleterTextField == null) {
            throw new NullPointerException("The autocompleterTextField in the autocompleter layer must not be null.");
        }

        // initiate Entity specific listeners
        this.subjectValueChangeHandler = new SubjectValueChangeHandler();
        this.propertyValidationResultsChangeListener = new PropertyValidationResultsChangeListener(this.boundedValidationLayer) {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                super.propertyChange(evt);
                logger.trace("DDDDDDDDDDDDDDDDDDDDDDDDDDD->>>>>>>>>>>>>>>>>>> isValid : " + boundedMetaProperty().isValid());
                updateToolTip();
            }
        };
        this.editableChangeListener = new EditableChangeListener(boundedAutocompleterValidationLayer);
        this.requiredChangeListener = new RequiredChangeListener(this.boundedValidationLayer);

        addOwnEntitySpecificListeners();
        Rebinder.initiateReconnectables(this.entity, this, this.boundedValidationLayer);

        // ////////////////////////////////
        this.textChangeHandler = new TextChangeHandler();
        document = autocompleterTextField.getDocument();
        reregisterTextChangeHandler(null, document);

        documentChangeHandler = new DocumentChangeHandler();
        autocompleterTextField.addPropertyChangeListener("document", documentChangeHandler);
        // adds flush event on Esc
        autocompleterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    autocompleterBufferedPropertyWrapper.flush();
                }
            }
        });

        // initial updating :
        this.updateStates();
        // setting OnCommitActionable
        this.initiateOnCommitActionable(boundedValidationLayer);
    }

    /**
     * updates toolTip by actualValue in subject
     */
    @Override
    public void updateToolTip() {
        SwingUtilitiesEx.invokeLater(new Runnable() {
            @Override
            public void run() {
                autocompleterTextField.setToolTipText(Binder.createToolTipByValueAndMetaProperty(entity, propertyName, boundedMetaProperty(), boundedValidationLayer.getOriginalToolTipText(), true, autocompleterBufferedPropertyWrapper.isStringBinding(), autocompleter.isMulti()));
            }
        });
    }

    // Synchronization ********************************************************
    /**
     * Reads the current text from the document and sets it as new value of the subject.
     */
    public void updateSubject() {
        setSubjectText(getDocumentText());
    }

    @Override
    public void updateByActualOrLastIncorrectValue() {
        if (boundedMetaProperty() == null || boundedMetaProperty().isValid()) {
            setDocumentTextSilently(getSubjectText());
        } else {
            setDocumentTextSilently(getIncorrectValueText());
        }
    }

    /**
     * Returns the text contained in the document.
     * 
     * @return the text contained in the document
     */
    private String getDocumentText() {
        return autocompleterTextField.getText();
    }

    /**
     * Sets the text component's contents without notifying the subject about the change. Invoked by the subject change listener. Sets the text, then sets the caret position to 0.
     * 
     * @param newText
     *            the text to be set in the document
     */
    private void setDocumentTextSilently(final String newText) {
        SwingUtilitiesEx.invokeLater(new Runnable() {
            public void run() {
                final int position = autocompleterTextField.getCaretPosition();
                autocompleterTextField.getDocument().removeDocumentListener(textChangeHandler);
                autocompleterTextField.setText(newText);
                autocompleterTextField.getDocument().addDocumentListener(textChangeHandler);
                try {
                    autocompleterTextField.setCaretPosition(position);
                } catch (final Exception e) {
                    System.out.println(e.toString());
                    autocompleterTextField.setCaretPosition(0);
                }
            }
        });
    }

    /**
     * Returns the subject's text value.
     * 
     * @return the subject's text value
     * @throws Exception
     * @throws MissingConverterException
     * @throws ClassCastException
     *             if the subject value is not a String
     */
    private String getSubjectText() {
        return getValueText(autocompleterBufferedPropertyWrapper.get(autocompleterBufferedPropertyWrapper.getPropertyName()));
    }

    /**
     * Returns the subject's incorrect value text.
     * 
     * @return the subject's text value
     * @throws Exception
     * @throws MissingConverterException
     * @throws ClassCastException
     *             if the subject value is not a String
     */
    private String getIncorrectValueText() {
        return getValueText(boundedMetaProperty().getLastInvalidValue());
    }

    /**
     * Returns the text from the value.
     * 
     * @return
     * @throws Exception
     * @throws MissingConverterException
     * @throws ClassCastException
     *             if the subject value is not a String
     */

    @SuppressWarnings("unchecked")
    private String getValueText(final Object propertyValue) {
        if (propertyValue instanceof String) {
            return propertyValue.toString();
        } else if (propertyValue instanceof List) {
            return (!autocompleterBufferedPropertyWrapper.isStringBinding()) ? constructStringFromEntityList((List<T>) propertyValue, autocompleter.getAutocompleter())
                    : (constructStringFromStringList((List<String>) propertyValue, autocompleter.getAutocompleter().getValueSeparator()));
        } else {
            if (propertyValue != null) {
                final T tProperty = (T) propertyValue;
                final Object value = autocompleter.getAutocompleter().value(tProperty);
                return "" + value;
            } else {
                return "";
            }
        }
    }

    /**
     * This method used to construct String representation from list of Entities using ValueSeparator and converting methods incapsulated in {@link AutocompleterLogic} instance
     * 
     * @param <N>
     * @param list
     * @param autocompleterLogic
     * @return
     */
    protected static <N> String constructStringFromEntityList(final List<N> list, final AutocompleterLogic<N> autocompleterLogic) {
        final StringBuffer str = new StringBuffer();
        for (final Iterator<N> iter = list.iterator(); iter.hasNext();) {
            final N entity = iter.next();
            final Object value = autocompleterLogic.value(entity);
            str.append(value.toString());
            if (iter.hasNext()) {
                str.append(autocompleterLogic.getValueSeparator());
            }
        }
        return str.toString();
    }

    /**
     * Converts from autocompleter value of type N to String using autocompleter logic
     * 
     * @param <N>
     * @param val
     * @param autocompleterLogic
     * @return
     */
    @SuppressWarnings("unchecked")
    protected static <N> String constructStringFromSingleValue(final Object val, final AutocompleterLogic<N> autocompleterLogic) {
        final N tNewValue = (N) val;
        final Object value = autocompleterLogic.value(tNewValue);
        return (value != null) ? value.toString() : "";
    }

    /**
     * Constructs List<String> from List<Entity> using "constructStringFromSingleValue()" method
     * 
     * @param <N>
     * @param values
     * @param autocompleterLogic
     * @return
     */
    protected static <N> List<String> constructStringListFromEntityList(final List<N> values, final AutocompleterLogic<N> autocompleterLogic) {
        final ArrayList<String> strings = new ArrayList<String>();
        for (final N value : values) {
            strings.add(constructStringFromSingleValue(value, autocompleterLogic));
        }
        return strings;
    }

    /**
     * Constructs String from list using value separator
     * 
     * @param <N>
     * @param stringList
     * @param autocompleterLogic
     * @return
     */
    protected static String constructStringFromStringList(final List<String> stringList, final String valueSeparator) {
        final StringBuffer str = new StringBuffer();
        for (final Iterator<String> iter = stringList.iterator(); iter.hasNext();) {
            final String s = iter.next();
            str.append(s);
            if (iter.hasNext()) {
                str.append(valueSeparator);
            }
        }
        return str.toString();
    }

    /**
     * splits buffered value string using valueSeparator
     * 
     * @param <N>
     * @param bufferedValue
     * @param valueSeparator
     * @return
     */
    protected static List<String> constructStringListFromStringBufferedValue(final String bufferedValue, final String valueSeparator) {
        final ArrayList<String> strings = new ArrayList<String>();
        if (StringUtils.isEmpty(valueSeparator)) {
            if (!StringUtils.isEmpty(bufferedValue)) {
                strings.add(bufferedValue);
            }
            return strings;
        } else {
            final String[] stringsArray = bufferedValue.split(valueSeparator);
            for (final String s : stringsArray) {
                if (!StringUtils.isEmpty(s)) {
                    strings.add(s);
                }
            }
        }
        return strings;
    }

    /**
     * Sets the given text as new subject value. Since the subject may modify this text, we cannot update silently, i.e. we cannot remove and add the subjectValueChangeHandler
     * before/after the update. Since this change is invoked during a Document write operation, the document is write-locked and so, we cannot modify the document before all
     * document listeners have been notified about the change.
     * <p>
     * 
     * Therefore we listen to subject changes and defer any document changes using <code>SwingUtilities.invokeLater</code>. This mode is activated by setting the subject change
     * handler's <code>updateLater</code> to true.
     * 
     * @param newText
     *            the text to be set in the subject
     */
    private void setSubjectText(final String newText) {
        autocompleterBufferedPropertyWrapper.set(autocompleterBufferedPropertyWrapper.getPropertyName(), newText);
    }

    private void reregisterTextChangeHandler(final Document oldDocument, final Document newDocument) {
        if (oldDocument != null) {
            oldDocument.removeDocumentListener(textChangeHandler);
        }
        if (newDocument != null) {
            newDocument.addDocumentListener(textChangeHandler);
        }
    }

    // Misc *******************************************************************

    /**
     * Removes the internal listeners from the subject, text component, and text component's document. This connector must not be used after calling <code>#release</code>.
     * <p>
     * 
     * To avoid memory leaks it is recommended to invoke this method, if the ValueModel lives much longer than the text component. Instead of releasing a text connector, you
     * typically make the ValueModel obsolete by releasing the PresentionModl or BeanAdapter that has created the ValueModel.
     * <p>
     * 
     * As an alternative you may use ValueModels that in turn use event listener lists implemented using <code>WeakReference</code>.
     * 
     * @see PresetationModel#release()
     * @see java.lang.ref.WeakReference
     */
    public void release() {
        reregisterTextChangeHandler(document, null);
        // subject.removeValueChangeListener(subjectValueChangeHandler);
        autocompleterTextField.removePropertyChangeListener("document", documentChangeHandler);
    }

    // DocumentListener *******************************************************
    /**
     * Updates the subject if the text has changed.
     */
    private final class TextChangeHandler implements DocumentListener {

        /**
         * There was an insert into the document; update the subject.
         * 
         * @param e
         *            the document event
         */
        public void insertUpdate(final DocumentEvent e) {
            if (autocompleterTextField.hasFocus()) {
                updateSubject();
            }
        }

        /**
         * A portion of the document has been removed; update the subject.
         * 
         * @param e
         *            the document event
         */
        public void removeUpdate(final DocumentEvent e) {
            if (autocompleterTextField.hasFocus()) {
                updateSubject();
            }
        }

        /**
         * An attribute or set of attributes has changed; do nothing.
         * 
         * @param e
         *            the document event
         */
        public void changedUpdate(final DocumentEvent e) {
            // Do nothing on attribute changes.
        }
    }

    /**
     * Handles changes in the subject value and updates this document - if necessary.
     * <p>
     * 
     * Document changes update the subject text and result in a subject property change. Most of these changes will just reflect the former subject change. However, in some cases
     * the subject may modify the text set, for example to ensure upper case characters. This method reduces the number of document updates by checking the old and new text. If the
     * old and new text are equal or both null, this method does nothing.
     * <p>
     * 
     * Since subject changes as a result of a document change may not modify the write-locked document immediately, we defer the update if necessary using
     * <code>SwingUtilities.invokeLater</code>.
     * <p>
     * 
     * See the AutocompleterConnector's JavaDoc class comment for the limitations of the deferred document change.
     */
    private final class SubjectValueChangeHandler implements Binder.SubjectValueChangeHandler {
        /**
         * The subject value has changed; updates the document immediately or later - depending on the <code>updateLater</code> state.
         * 
         * @param evt
         *            the event to handle
         * @throws MissingConverterException
         */
        @Override
        @SuppressWarnings("unchecked")
        public void propertyChange(final PropertyChangeEvent evt) {
            System.out.println("AutoCompleter_Connector: subject value changes!! : " + evt.getOldValue() + "  " + evt.getNewValue());
            SwingUtilitiesEx.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final Object newValue = (boundedMetaProperty() == null || boundedMetaProperty().isValid()) ? Rebinder.getActualEntity(entity).get(propertyName)
                            : boundedMetaProperty().getLastInvalidValue();
                    final String newValueString = (newValue == null) //
                    ? "" //
                            : ((newValue instanceof List)//
                            ? (//
                            (!autocompleterBufferedPropertyWrapper.isStringBinding()) //
                            ? constructStringFromEntityList((List<T>) newValue, autocompleter.getAutocompleter()) //
                                    : constructStringFromStringList((List<String>) newValue, autocompleter.getAutocompleter().getValueSeparator()))//

                                    : ((!autocompleterBufferedPropertyWrapper.isStringBinding()) ? //
                                    constructStringFromSingleValue(newValue, autocompleter.getAutocompleter()) //
                                            : ((String) newValue)) //
                            );
                    setDocumentTextSilently(newValueString);

                }
            });
            updateToolTip();
        }
    }

    /**
     * Re-registers the text change handler after document changes.
     */
    private final class DocumentChangeHandler implements PropertyChangeListener {
        public void propertyChange(final PropertyChangeEvent evt) {
            final Document oldDocument = document;
            final Document newDocument = autocompleterTextField.getDocument();
            reregisterTextChangeHandler(oldDocument, newDocument);
            document = newDocument;
        }
    }

    /**
     * updates the editable state of the component based on the Editable state of the bound Property
     */
    @Override
    public void updateEditable() {
        if (boundedMetaProperty() != null) {
            SwingUtilitiesEx.invokeLater(new Runnable() {
                @Override
                public void run() {
                    autocompleterTextField.setEditable(boundedMetaProperty().isEditable());
                }
            });
        }
    }

    /**
     * updates the "required" state of the component based on the "required" state of the bound Property
     */
    public void updateRequired() {
        final MetaProperty property = boundedMetaProperty();
        if (property != null) {
            SwingUtilitiesEx.invokeLater(new Runnable() {
                @Override
                public void run() {
                    boundedValidationLayer.getUI().setRequired(property.isRequired());
                }
            });
        }
    }

    /**
     * updates autocompleter validation Ui by actual last validation result stored in autocompleter boundedMetaProperty
     */
    @Override
    public void updateValidationResult() {
        Binder.updateValidationUIbyMetaPropertyValidationState(boundedMetaProperty(), boundedValidationLayer);
    }
}
