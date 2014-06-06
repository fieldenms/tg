package ua.com.fielden.platform.swing.components.bind.development;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

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
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.SpecialFormattedField;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.types.Money;

/**
 * This is the value based connector for BigDecimal/Money/Integer formatted field. Can use OnKeyTyped binding with direct entity's property or Triggered binding with
 * BufferedPropertyWrapper
 * 
 * @author jhou
 * 
 */
public class FormattedFieldConnector extends PropertyConnectorAdapter implements IOnCommitActionable, IRebindable {

    protected final Logger logger = Logger.getLogger(this.getClass());

    private final JFormattedTextField formattedField;

    private Document document;

    private final DocumentListener textChangeHandler;

    private final PropertyChangeListener documentChangeHandler;

    private final BoundedValidationLayer boundedValidationLayer;

    private final List<IOnCommitAction> onCommitActions = new ArrayList<IOnCommitAction>();

    /**
     * Initializes all needed listeners, textChangeHandlers, onCommitActions and so on.. All listeners adds to appropriate object, whether it is BufferedPropertyWrapper or direct
     * IBindingEntity
     * 
     * @param entity
     * @param propertyName
     * @param boundedValidationLayer
     * @param actions
     */
    FormattedFieldConnector(final IBindingEntity entity, final String propertyName, final BoundedValidationLayer boundedValidationLayer, final JFormattedTextField formattedField, final IOnCommitAction... actions) {
        // initiate Entity and PropertyName
        super(entity, propertyName);

        // initiate boundedValidationLayer
        if (boundedValidationLayer == null) {
            logger.error("ValidationLayer is null.");
            throw new NullPointerException("The validationLayer must not be null.");
        }
        this.boundedValidationLayer = boundedValidationLayer;

        // initiateEditableComponent
        this.formattedField = formattedField;
        if (formattedField == null) {
            throw new NullPointerException("The formatted field must not be null.");
        }

        // initiate Entity specific listeners
        this.subjectValueChangeHandler = new SubjectValueChangeHandler();
        this.propertyValidationResultsChangeListener = new PropertyValidationResultsChangeListener(this.boundedValidationLayer);
        this.editableChangeListener = new EditableChangeListener(this.boundedValidationLayer);
        this.requiredChangeListener = new RequiredChangeListener(this.boundedValidationLayer);

        addOwnEntitySpecificListeners();
        Rebinder.initiateReconnectables(this.entity, this, this.boundedValidationLayer);

        // initiate and assign component specific listeners
        this.textChangeHandler = createTextChangeHandler();
        document = formattedField.getDocument();
        reregisterTextChangeHandler(null, document);

        ////////// !!!!!!!!!!!!!!!!!! /////////////////
        if (formattedField instanceof SpecialFormattedField) {
            ((SpecialFormattedField) formattedField).setTextChangeHandler(textChangeHandler);
        }
        ////////// !!!!!!!!!!!!!!!!!! /////////////////

        documentChangeHandler = new DocumentChangeHandler();
        formattedField.addPropertyChangeListener("document", documentChangeHandler);

        // if component is buffered, adds flush event on Esc
        formattedField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && (FormattedFieldConnector.this.entity instanceof BufferedPropertyWrapper)) {
                    ((BufferedPropertyWrapper) FormattedFieldConnector.this.entity).flush();
                }
            }
        });

        for (int i = 0; i < actions.length; i++) {
            addOnCommitAction(actions[i]);
        }

        // initial updating :
        this.updateStates();
        // setting OnCommitActionable
        this.initiateOnCommitActionable(boundedValidationLayer);
    }

    /**
     * Creates text change handler that updates subject when component content changed.
     * 
     * @return
     */
    protected DocumentListener createTextChangeHandler() {
        return new TextChangeHandler();
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

    /**
     * Initializes all needed listeners, textChangeHandlers, onCommitActions and so on.. All listeners adds to the specified BufferedPropertyWrapper and other FormattedField
     * related objects (document etc.)
     * 
     * @param bufferedPropertyWrapper
     * @param validationLayer
     */
    FormattedFieldConnector(final BufferedPropertyWrapper bufferedPropertyWrapper, final BoundedValidationLayer validationLayer, final JFormattedTextField formattedField) {
        this(bufferedPropertyWrapper, bufferedPropertyWrapper.getPropertyName(), validationLayer, formattedField);
    }

    // Synchronization ********************************************************

    /**
     * gets the actual FormattedField value and set it to subject
     */
    public void updateSubject() {
        if (isOnKeyTyped()) {
            // lock if the "entity" is not BPW. if "entity" is BPW - it locks inside BPW's "commit" method
            // lock subject bean, even if the setter will not be perfomed (it is more safe)
            entity.lock();
        }
        new SwingWorkerCatcher<Result, Void>() {

            private boolean setterPerformed = false;

            @Override
            protected Result tryToDoInBackground() {
                entity.set(propertyName, SpinnerConnector.convertNumberValueObtainedFromFormattedField(formattedField.getValue(), Rebinder.getPropertyType(entity, propertyName)));
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

    @Override
    public void updateByActualOrLastIncorrectValue() {
        SwingUtilitiesEx.invokeLater(new Runnable() {
            public void run() {
                convertAndSetAppropriateValueSilently((boundedMetaProperty() == null || boundedMetaProperty().isValid()) ? entity.get(propertyName)
                        : boundedMetaProperty().getLastInvalidValue());
            }
        });
    }

    /**
     * Converts and set specified value silently.
     * 
     * @param value
     */
    protected void convertAndSetAppropriateValueSilently(final Object value) {
        if (value != null) {
            if (value instanceof Money) {
                setValueSilently(((Money) value).getAmount());
            } else if (value instanceof BigDecimal || value instanceof Integer || value instanceof Double || value instanceof Date) {
                setValueSilently(value);
            } else {
                logger.error("Subject value type " + value.getClass() + " is not supported by " + getClass().getSimpleName());
                throw new RuntimeException("Subject value type " + value.getClass() + " is not supported by " + getClass().getSimpleName());
            }
        } else {
            setValueSilently(null);
        }
    }

    /**
     * sets the value of the formattedField without any listener notifications. Also updates the caret position for the OnKeyTyped strategy
     * 
     * @param newObject
     */
    private void setValueSilently(final Object newObject) {
        // if the OnKeyTyped strategy is used, need to update correct caret position
        int position = -1;
        if (!(entity instanceof BufferedPropertyWrapper)) {
            position = formattedField.getCaretPosition();
        }

        formattedField.getDocument().removeDocumentListener(textChangeHandler);
        formattedField.setValue(newObject);
        formattedField.getDocument().addDocumentListener(textChangeHandler);

        // if the OnKeyTyped strategy is used, need to update correct caret position
        if (!(entity instanceof BufferedPropertyWrapper)) {
            try {
                formattedField.setCaretPosition(position);
            } catch (final Exception e) {
                System.out.println(e.toString());
                formattedField.setCaretPosition(0);
            }
        }
    }

    /**
     * reregisters document listener from old document to new one
     * 
     * @param oldDocument
     * @param newDocument
     */
    private void reregisterTextChangeHandler(final Document oldDocument, final Document newDocument) {
        if (oldDocument != null) {
            oldDocument.removeDocumentListener(textChangeHandler);
        }
        if (newDocument != null) {
            newDocument.addDocumentListener(textChangeHandler);
        }
    }

    /**
     * Removes the internal listeners from the subject, text component, and text component's document.
     */
    public void release() {
        reregisterTextChangeHandler(document, null);
        formattedField.removePropertyChangeListener("document", documentChangeHandler);
    }

    /**
     * Adds the internal listeners to the subject, text component, and text component's document again.
     */
    public void reAssign() {
        formattedField.addPropertyChangeListener("document", documentChangeHandler);
        reregisterTextChangeHandler(null, document);
    }

    /**
     * Updates the subject if the text has changed.
     */
    protected class TextChangeHandler implements DocumentListener {

        /**
         * There was an insert into the document; First : commit edit in formattedField (it sets its value), Next : update the subject.
         * 
         * @param e
         *            the document event
         */
        public void insertUpdate(final DocumentEvent e) {
            update();
        }

        /**
         * There was a change performed upon the document; First : commit edit in formattedField (it sets its value), Next : update the subject.
         */
        protected void update() {
            if (formattedField.hasFocus()) {
                try {
                    formattedField.commitEdit();
                    updateSubject();
                } catch (final ParseException e1) {
                    // Note that for date formatted field this is legal situation (because it allows invalid input).
                    // Number connectors do not allow invalid input - so this situation should not be appeared.
                    logger.error("Formatted field text [" + formattedField.getText() + "] parsing failed. ");
                }
            }
        }

        /**
         * A portion of the document has been removed; First : commit edit in formattedField (it sets its value), Next : update the subject.
         * 
         * @param e
         *            the document event
         */
        public void removeUpdate(final DocumentEvent e) {
            update();
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
     * 
     */
    private final class SubjectValueChangeHandler implements Binder.SubjectValueChangeHandler {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            logger.debug("Subject value changes from [" + evt.getOldValue() + "] to [" + evt.getNewValue() + "]");
            SwingUtilitiesEx.invokeLater(new Runnable() {
                public void run() {
                    convertAndSetAppropriateValueSilently(evt.getNewValue());
                    updateToolTip();
                }
            });
        }
    }

    /**
     * Re-registers the text change handler after document changes.
     */
    private final class DocumentChangeHandler implements PropertyChangeListener {
        public void propertyChange(final PropertyChangeEvent evt) {
            final Document oldDocument = document;
            final Document newDocument = formattedField.getDocument();
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
                    formattedField.setEditable(boundedMetaProperty().isEditable());
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

    //    @Override
    //    public void updateToolTip() {
    //	SwingUtilitiesEx.invokeLater(new Runnable() {
    //	    @Override
    //	    public void run() { // boundedValidationLayer.getView()
    //		if (boundedMetaProperty() == null) {
    //		    formattedField.setToolTipText(boundedValidationLayer.getOriginalToolTipText());
    //		    return;
    //		}
    //		formattedField.setToolTipText((boundedMetaProperty().isValid()) ? (boundedMetaProperty().hasWarnings() ? boundedMetaProperty().getFirstWarning().getMessage()
    //			: boundedValidationLayer.getOriginalToolTipText()) : boundedMetaProperty().getFirstFailure().getMessage());
    //	    }
    //	});
    //    }

    @Override
    public void updateToolTip() {
        SwingUtilitiesEx.invokeLater(new Runnable() {
            public void run() {
                formattedField.setToolTipText(Binder.createToolTipByValueAndMetaProperty(entity, propertyName, boundedMetaProperty(), boundedValidationLayer.getOriginalToolTipText(), true));
            }
        });
    }

    @Override
    public void updateValidationResult() {
        Binder.updateValidationUIbyMetaPropertyValidationState(boundedMetaProperty(), this.boundedValidationLayer);
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

    protected JFormattedTextField getFormattedField() {
        return formattedField;
    }
}
