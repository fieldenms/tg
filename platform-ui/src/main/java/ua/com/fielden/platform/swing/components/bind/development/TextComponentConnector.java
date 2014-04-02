package ua.com.fielden.platform.swing.components.bind.development;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.bind.development.Binder.EditableChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IRebindable;
import ua.com.fielden.platform.swing.components.bind.development.Binder.PropertyConnectorAdapter;
import ua.com.fielden.platform.swing.components.bind.development.Binder.PropertyValidationResultsChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.Binder.Rebinder;
import ua.com.fielden.platform.swing.components.bind.development.Binder.RequiredChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitActionable;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;

import com.jgoodies.binding.formatter.EmptyNumberFormatter;

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
 * TextComponentConnector connector = new TextComponentConnector(codeModel, codeField);
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
public final class TextComponentConnector extends PropertyConnectorAdapter implements IOnCommitActionable, IRebindable {
    /**
     * Refers to the text component that shall be synchronized with the subject.
     */
    private final JTextComponent textComponent;

    /**
     * Holds the text component's current document. Used for the rare case where the text component fires a PropertyChangeEvent for the "document" property with oldValue or
     * newValue == {@code null}.
     */
    private Document document;

    private final DocumentListener textChangeHandler;

    private final PropertyChangeListener documentChangeHandler;

    private Converter converter;

    private final BoundedValidationLayer<? extends JTextComponent> boundedValidationLayer;

    private final List<IOnCommitAction> onCommitActions = new ArrayList<IOnCommitAction>();

    // Instance Creation ******************************************************

    /**
     * Constructs a TextComponentConnector that connects the specified String-typed subject alueModel with the given text field.
     * <p>
     * 
     * In case you don't need the TextComponentConnector instance, you better use one of the static <code>#connect</code> methods. This constructor may confuse developers, if you
     * just use the side effects performed in the constructor; this is because it is quite unconventional to instantiate an object that you never use.
     * 
     * @param subject
     *            the underlying String typed alueModel
     * @param textField
     *            the JTextField to be synchronized with the VlueModel
     * 
     * @throws NullPointerException
     *             if the subject or text field is {@code null}
     */

    TextComponentConnector(final BufferedPropertyWrapper bufferedPropertyWrapper, final BoundedValidationLayer<? extends JTextComponent> validationLayer) {
        this(bufferedPropertyWrapper, bufferedPropertyWrapper.getPropertyName(), validationLayer);
    }

    TextComponentConnector(final IBindingEntity entity, final String propertyName, final BoundedValidationLayer<? extends JTextComponent> validationLayer, final Converter converter, final IOnCommitAction... actions) {
        this(entity, propertyName, validationLayer, actions);
        this.converter = converter;
    }

    TextComponentConnector(final BufferedPropertyWrapper bufferedPropertyWrapper, final BoundedValidationLayer<? extends JTextComponent> validationLayer, final Converter converter) {
        this(bufferedPropertyWrapper, validationLayer);
        this.converter = converter;
    }

    /**
     * Constructs a TextComponentConnector that connects the specified String-typed subject alueModel with the given JTextComponent.
     * <p>
     * 
     * In case you don't need the TextComponentConnector instance, you better use one of the static <code>#connect</code> methods. This constructor may confuse developers, if you
     * just use the side effects performed in the constructor; this is because it is quite unconventional to instantiate an object that you never use.
     * 
     * @param subject
     *            the underlying String typed ValueModel
     * @param textComponent
     *            the JTextComponent to be synchronized with the ValueModel
     * 
     * @throws NullPointerException
     *             if the subject or text component is {@code null}
     */
    TextComponentConnector(final IBindingEntity entity, final String propertyName, final BoundedValidationLayer<? extends JTextComponent> boundedValidationLayer, final IOnCommitAction... actions) {
        // initiate Entity and PropertyName
        super(entity, propertyName);

        // initiate boundedValidationLayer
        if (boundedValidationLayer == null) {
            throw new NullPointerException("The validationLayer must not be null.");
        }
        this.boundedValidationLayer = boundedValidationLayer;

        // initiateEditableComponent
        this.textComponent = this.boundedValidationLayer.getView();
        if (textComponent == null) {
            throw new NullPointerException("The text component must not be null.");
        }

        // initiate Entity specific listeners
        this.subjectValueChangeHandler = new SubjectValueChangeHandler();
        this.propertyValidationResultsChangeListener = new PropertyValidationResultsChangeListener(this.boundedValidationLayer);
        this.editableChangeListener = new EditableChangeListener(this.boundedValidationLayer);
        this.requiredChangeListener = new RequiredChangeListener(this.boundedValidationLayer);

        addOwnEntitySpecificListeners();
        Rebinder.initiateReconnectables(this.entity, this, this.boundedValidationLayer);

        // initiate and assign component specific listeners
        // ==================  add component specific listeners :
        this.textChangeHandler = new TextChangeHandler();
        document = textComponent.getDocument();
        reregisterTextChangeHandler(null, document);
        documentChangeHandler = new DocumentChangeHandler();
        textComponent.addPropertyChangeListener("document", documentChangeHandler);

        // if component is buffered, adds flush event on Esc
        textComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && (TextComponentConnector.this.entity instanceof BufferedPropertyWrapper)) {
                    ((BufferedPropertyWrapper) TextComponentConnector.this.entity).flush();
                }
            }
        });

        // add on Commit Actions
        for (int i = 0; i < actions.length; i++) {
            addOnCommitAction(actions[i]);
        }

        // initial updating :
        this.updateStates();
        // setting OnCommitActionable
        this.initiateOnCommitActionable(boundedValidationLayer);
    }

    @Override
    public void rebindTo(final IBindingEntity entity) {
        if (entity == null) {
            new IllegalArgumentException("the component cannot be reconnected to the Null entity!!").printStackTrace();
        } else {
            unbound();
            setEntity(entity);
            addOwnEntitySpecificListeners();
            updateStates();
        }
    }

    @Override
    public void unbound() {
        removeOwnEntitySpecificListeners();
    }

    // Synchronization ********************************************************
    /**
     * Reads the current text from the document and sets it as new value of the subject.
     */
    public void updateSubject() {
        setSubjectText(getDocumentText());
    }

    public void updateByActualOrLastIncorrectValue() {
        SwingUtilitiesEx.invokeLater(new Runnable() {
            public void run() {
                try {
                    setDocumentTextSilently((boundedMetaProperty() == null || boundedMetaProperty().isValid()) ? getSubjectText()
                            : getValueText(boundedMetaProperty().getLastInvalidValue()));
                } catch (final MissingConverterException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Returns the text contained in the document.
     * 
     * @return the text contained in the document
     */
    private String getDocumentText() {
        return textComponent.getText();
    }

    /**
     * Sets the text component's contents without notifying the subject about the change. This method is invoked by the subject change listener.
     * <p>
     * Sets the text, then restores the caret position and selection in case when text has not been changed.
     * 
     * @param newText
     *            the text to be set in the document
     */
    private void setDocumentTextSilently(final String newText) {
        final int selectionStart = textComponent.getSelectionStart();
        final int selectionEnd = textComponent.getSelectionEnd();
        final String oldText = textComponent.getText();

        textComponent.getDocument().removeDocumentListener(textChangeHandler);
        textComponent.setText(newText);
        textComponent.getDocument().addDocumentListener(textChangeHandler);

        if (StringUtils.equals(newText, oldText)) {
            textComponent.setCaretPosition(selectionStart);
            textComponent.moveCaretPosition(selectionEnd);
        }
    }

    /**
     * Returns the subject's text value.
     * 
     * @return the subject's text value
     * @throws MissingConverterException
     * @throws ClassCastException
     *             if the subject value is not a String
     */

    private String getValueText(final Object value) throws MissingConverterException {
        if (value != null && !String.class.equals(value.getClass()) && converter == null) {
            throw new MissingConverterException();
        }
        final String str = (converter != null) ? converter.convertToString(value) : (value != null ? value.toString() : null);
        return str == null ? "" : str;
    }

    /**
     * Returns the subject's text value.
     * 
     * @return the subject's text value
     * @throws MissingConverterException
     * @throws ClassCastException
     *             if the subject value is not a String
     */

    private String getSubjectText() throws MissingConverterException {
        return getValueText(entity.get(propertyName));
    }

    private class MissingConverterException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static final NumberFormat displayNumberFormat = new DecimalFormat("#,##0.00"), editNumberFormat = new DecimalFormat("0.0###################");;
    public static final NumberFormatter editNumberFormatter = new EmptyNumberFormatter(editNumberFormat, new BigDecimal(-1.0));
    public static final NumberFormatter displayNumberFormatter = new EmptyNumberFormatter(displayNumberFormat, new BigDecimal(-2.0));

    static {
        editNumberFormatter.setAllowsInvalid(false);
        displayNumberFormatter.setAllowsInvalid(false);
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
        if (isOnKeyTyped()) {
            // lock if the "entity" is not BPW. if "entity" is BPW - it locks inside BPW's "commit" method
            // lock subject bean, even if the setter will not be perfomed (it is more safe)
            entity.lock();
        }
        new SwingWorkerCatcher<Result, Void>() {
            private boolean setterPerformed = false;

            @Override
            protected Result tryToDoInBackground() {
                entity.set(propertyName, (converter != null) ? converter.convertToObject(newText) : newText);
                setterPerformed = true;
                return null;
            }

            @Override
            protected void tryToDone() {
                if (setterPerformed) {
                    for (int i = 0; i < onCommitActions.size(); i++) {
                        if (onCommitActions.get(i) != null) {
                            onCommitActions.get(i).postCommitAction();
                            if (boundedMetaProperty() == null || boundedMetaProperty().isValid()) {
                                onCommitActions.get(i).postSuccessfulCommitAction();
                            } else {
                                onCommitActions.get(i).postNotSuccessfulCommitAction();
                            }
                        }
                    }
                }
                if (isOnKeyTyped()) {
                    // need to unlock subjectBean in all cases:
                    // 1. setter not performed - exception throwed
                    // 2. setter not performed - the committing logic didn't invoke setter
                    // 3. setter performed correctly
                    entity.unlock();
                }
            }
        }.execute();
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
        textComponent.removePropertyChangeListener("document", documentChangeHandler);
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
            if (textComponent.hasFocus()) {
                updateSubject();
            } else {
                System.out.println("textComponent.has not Focus()!");
            }
        }

        /**
         * A portion of the document has been removed; update the subject.
         * 
         * @param e
         *            the document event
         */
        public void removeUpdate(final DocumentEvent e) {
            if (textComponent.hasFocus()) {
                updateSubject();
            } else {
                System.out.println("textComponent.has not Focus()!");
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
     * See the TextComponentConnector's JavaDoc class comment for the limitations of the deferred document change.
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
        public void propertyChange(final PropertyChangeEvent evt) {
            try {
                final String newText = (converter != null) ? //
                (evt.getNewValue() == null ? getSubjectText() : converter.convertToString(evt.getNewValue())) //
                        : (evt.getNewValue() == null ? getSubjectText() : (String) evt.getNewValue());
                SwingUtilitiesEx.invokeLater(new Runnable() {
                    public void run() {
                        setDocumentTextSilently(newText);
                        updateToolTip();
                    }
                });
            } catch (final MissingConverterException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Re-registers the text change handler after document changes.
     */
    private final class DocumentChangeHandler implements PropertyChangeListener {
        public void propertyChange(final PropertyChangeEvent evt) {
            final Document oldDocument = document;
            final Document newDocument = textComponent.getDocument();
            reregisterTextChangeHandler(oldDocument, newDocument);
            document = newDocument;
        }
    }

    /**
     * updates the editable state of the component based on the Editable state of the bound Property
     */
    public void updateEditable() {
        final MetaProperty property = boundedMetaProperty();
        if (property != null) {
            SwingUtilitiesEx.invokeLater(new Runnable() {
                @Override
                public void run() {
                    textComponent.setEditable(property.isEditable());
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
     * adds OnCommitAction to use it at On Key Typed commit model
     * 
     * @param onCommitAction
     * @return
     */
    public synchronized boolean addOnCommitAction(final IOnCommitAction onCommitAction) {
        return onCommitActions.add(onCommitAction);
    }

    /**
     * removes OnCommitAction to remove its usage at On Key Typed commit model
     * 
     * @param onCommitAction
     * @return
     */
    public synchronized boolean removeOnCommitAction(final IOnCommitAction onCommitAction) {
        return onCommitActions.remove(onCommitAction);
    }

    /**
     * gets all assigned "On Key Typed" OnCommitActions
     * 
     * @return
     */
    public List<IOnCommitAction> getOnCommitActions() {
        return Collections.unmodifiableList(onCommitActions);
    }

    @Override
    public void updateToolTip() {
        SwingUtilitiesEx.invokeLater(new Runnable() {
            public void run() {
                boundedValidationLayer.getView().setToolTipText(Binder.createToolTipByValueAndMetaProperty(entity, propertyName, boundedMetaProperty(), boundedValidationLayer.getOriginalToolTipText(), !(textComponent instanceof JPasswordField)));
            }
        });
    }

    @Override
    public void updateValidationResult() {
        Binder.updateValidationUIbyMetaPropertyValidationState(boundedMetaProperty(), this.boundedValidationLayer);
    }
}
