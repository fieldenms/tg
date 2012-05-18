package ua.com.fielden.platform.swing.components.bind.development;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXDatePicker;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.EntityDescriptor;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitActionable;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.ReadOnlyLabel;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.ConverterFactory;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.EntityUtils.ShowingStrategy;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.PropertyChangeOrIncorrectAttemptListener;

import com.jgoodies.binding.value.Trigger;
import com.jgoodies.binding.value.ValueModel;

/**
 * Binds components (e.g. textFields, radiobuttons, chechBoxes) with AbstractEntitie's properties or with their wrappers
 *
 * @author jhou
 *
 */
public final class Binder {

    private final static Logger logger = Logger.getLogger(ComponentFactory.class);

    private Binder() {
    }

    // ===================== Utility interfaces and classes :

    /**
     * This interface should implemented by OnKeyTyped adapters/connectors and also [Autocompleter]BufferedPropertyWrapper. Also every BoundedValidationLayer must have appropriate
     * Reconnectable aggregated inside, to have the opportunity to reconnect itself to another AbstractEntity instance
     */
    public interface IRebindable {
	String getPropertyName();

	IBindingEntity getSubjectBean();

	void rebindTo(final IBindingEntity entity);

	void unbound();

	/**
	 * updates ToolTip by the actual value of the property
	 */
	void updateToolTip();
    }

    /**
     * Need to be implemented by adapters/connectors to correctly update UI by the actual values
     *
     *
     * @author jhou
     *
     */
    public interface IUpdatable {
	/**
	 * Updates inner bounded component by the last incorrect value, that have been attempted to set, or by the actual value of the property.
	 *
	 * IMPORTANT : note that if metaProperty is null, the value which should update component, seems to be correct (the actual value should be used).
	 */
	public abstract void updateByActualOrLastIncorrectValue();

	/**
	 * updates ToolTip by the actual value of the property
	 */
	public abstract void updateToolTip();

	/**
	 * updates Editable state by the actual value in bounded MetaProperty
	 */
	public abstract void updateEditable();

	/**
	 * updates Required state by the actual value in bounded MetaProperty
	 */
	public abstract void updateRequired();

	/**
	 * updates validation UI by actual validation result in bounded MetaProperty
	 */
	public abstract void updateValidationResult();
    }

    /**
     * PropertyConnector has the ability of adding/removing the listeners related to the entity (for e.g. SubjectValueChangeHandler, [Editable/ValidationResults]ChangeListener).
     * See also PropertyConnectorAdapter
     *
     * @author jhou
     *
     */
    public interface IPropertyConnector {
	/**
	 * adds own listeners related to the entity's property. Need to be initialised before
	 */
	void addOwnEntitySpecificListeners();

	/**
	 * removes own listeners related to the entity's property. Need to be initialised before
	 */
	public void removeOwnEntitySpecificListeners();

	/**
	 * this method updates all needed states of the component(validation, editable, actualValue and toolTips)
	 */
	void updateStates();

	/**
	 * initiates correct IOnCommitActionable whether it is BufferedPropertyWrapper or this connector
	 */
	void initiateOnCommitActionable(final BoundedValidationLayer<?> boundedValidationLayer);

	/**
	 * returns the bounded Property (gets from BufferedPropertyWrapper or straight from Connector)
	 *
	 * @return
	 */
	MetaProperty boundedMetaProperty();

	boolean isOnKeyTyped();

	void updateToolTip();
    }

    /**
     * This is the base class for PropertyConnectors(if you need to have similar logic in adapters, then use PropertyConnector interface). It initializes Entity/PropertyName and
     * can add/remove entity related listeners. These listeners need to be initialized in concrete PropertyConnectorAdapter implementation in constructor. See below the pattern of
     * the connector's construction :
     *
     * 1. initiate Entity and PropertyName (by the super constructor of PropertyConnectorAdapter)
     * <p>
     * 2. initiate boundedValidationLayer
     * <p>
     * 3. initiateEditableComponent
     * <p>
     * 4. initiate Entity specific listeners, using "boundedValidationLayer" and "editableComponent" initiated before
     * <p>
     * 5. add created entity specific listeners
     * <p>
     * 6. initiate reconnectables (based upon type of the entity or BufferedPropertyWrapper)
     * <p>
     * 7. initiate and assign component specific listeners ...
     *
     * @author jhou
     *
     */
    static abstract class PropertyConnectorAdapter implements IUpdatable, IPropertyConnector {

	protected IBindingEntity entity;
	protected final String propertyName;

	// entity specific listeners :
	protected SubjectValueChangeHandler subjectValueChangeHandler;
	protected PropertyValidationResultsChangeListener propertyValidationResultsChangeListener;
	protected EditableChangeListener editableChangeListener;
	protected RequiredChangeListener requiredChangeListener;

	public PropertyConnectorAdapter(final IBindingEntity entity, final String propertyName) {
	    if (entity == null) {
		throw new NullPointerException("The entity must not be null.");
	    }
	    if (propertyName == null) {
		throw new NullPointerException("The propertyName must not be null.");
	    }
	    this.entity = entity;
	    this.propertyName = propertyName;
	}

	/**
	 * Since rebinding is supported, the entity can be changed
	 *
	 * @param entity
	 */
	protected void setEntity(final IBindingEntity entity) {
	    this.entity = entity;
	}

	public IBindingEntity getSubjectBean(){
	    return entity;
	}

	public String getPropertyName() {
	    return propertyName;
	}

	public void updateStates() {
	    updateByActualOrLastIncorrectValue();
	    if (boundedMetaProperty() != null) {
		updateEditable();
		updateRequired();
	    }
	    updateToolTip();
	    if (boundedMetaProperty() != null) {
		updateValidationResult();
	    }
	}

	public void addOwnEntitySpecificListeners() {
	    Rebinder.addPropertySpecificListener(this.entity, this.propertyName, this.subjectValueChangeHandler);
	    Rebinder.addMetaPropertySpecificListeners(this.entity, this.propertyName, this.propertyValidationResultsChangeListener, this.editableChangeListener, this.requiredChangeListener);
	}

	public void removeOwnEntitySpecificListeners() {
	    Rebinder.removePropertySpecificListener(this.entity, this.propertyName, this.subjectValueChangeHandler);
	    Rebinder.removeMetaPropertySpecificListeners(this.entity, this.propertyName, this.propertyValidationResultsChangeListener, this.editableChangeListener, this.requiredChangeListener);
	}

	@Override
	public void initiateOnCommitActionable(final BoundedValidationLayer<?> boundedValidationLayer) {
	    Rebinder.initiateIOnCommitActionable(this, boundedValidationLayer, entity);
	}

	public MetaProperty boundedMetaProperty() {
	    return Rebinder.getActualEntity(entity).getProperty(propertyName);
	}

	@Override
	public boolean isOnKeyTyped() {
	    return !(entity instanceof BufferedPropertyWrapper) && (!(entity instanceof AutocompleterBufferedPropertyWrapper));
	}
    }

    /**
     * it can remove Entity specific listeners from the AbstractEntity's property and its corresponding MetaProperty and reassign its listeners to another AbstractEntity
     */
    public static class Rebinder {

	private static void checkNullability(final Object object, final String name) {
	    if (object == null) {
		new Exception("the " + name + "is null!").printStackTrace();
	    }
	}

	/**
	 * Initiates appropriate reconnectables(only once in connector/adapter constructor) for
	 *
	 * 1. OnFocusLost and OnTriggerCommit : if thisEntity is BPW -> adds as PropertyConnector and sets thisBPW as Reconnectable(!!!)
	 *
	 * 2. OnKeyTyped : sets as Reconnectable
	 *
	 * @param thisEntity
	 * @param thisReconnectableOrPropertyConnector
	 * @param correspondingBoundedValidationLayer
	 */
	@SuppressWarnings("unchecked")
	public static void initiateReconnectables(final IBindingEntity thisEntity, final IUpdatable thisReconnectableOrPropertyConnector, final BoundedValidationLayer<?> correspondingBoundedValidationLayer) {
	    if (thisEntity instanceof BufferedPropertyWrapper) {
		((BufferedPropertyWrapper) thisEntity).addPropertyConnector((IPropertyConnector) thisReconnectableOrPropertyConnector);
		correspondingBoundedValidationLayer.setRebindable((BufferedPropertyWrapper) thisEntity);
	    } else if (thisEntity instanceof AutocompleterBufferedPropertyWrapper) {
		((AutocompleterBufferedPropertyWrapper<?>) thisEntity).addPropertyConnector((IPropertyConnector) thisReconnectableOrPropertyConnector);
		correspondingBoundedValidationLayer.setRebindable((AutocompleterBufferedPropertyWrapper<?>) thisEntity);
	    } else {
		correspondingBoundedValidationLayer.setRebindable((IRebindable) thisReconnectableOrPropertyConnector);
	    }
	}

	/**
	 * adds SubjectValueChangeHandler to the ActualEntity based on entityOrBufferedWrapper
	 *
	 * @param entityOrBufferedWrapper
	 * @param propertyName
	 * @param subjectValueChangeHandler
	 */
	public static void addPropertySpecificListener(final IBindingEntity entityOrBufferedWrapper, final String propertyName, final Binder.SubjectValueChangeHandler subjectValueChangeHandler) {
	    checkNullability(subjectValueChangeHandler, "subjectValueChangeHandler");
	    // add the subject value change listener to entity's property (can contain toolTipValueChangeListener inside!!)
	    getActualEntity(entityOrBufferedWrapper).addPropertyChangeListener(propertyName, subjectValueChangeHandler);
	}

	/**
	 * adds propertyValidationResultsChangeListener and editableChangeListener and requiredChangeListener to the ActualEntity's MetaProperty based on entityOrBufferedWrapper
	 *
	 * @param entityOrBufferedWrapper
	 * @param propertyName
	 * @param propertyValidationResultsChangeListener
	 * @param editableChangeListener
	 */
	public static void addMetaPropertySpecificListeners(final IBindingEntity entityOrBufferedWrapper, final String propertyName, //
		final PropertyValidationResultsChangeListener propertyValidationResultsChangeListener, //
		final EditableChangeListener editableChangeListener, //
		final RequiredChangeListener requiredChangeListener) {
	    // add the property change listener to corresponding MetaProperty, to handle validation results
	    final MetaProperty metaProperty = getActualEntity(entityOrBufferedWrapper).getProperty(propertyName);
	    if (metaProperty != null) {
		if (propertyValidationResultsChangeListener != null) {
		    metaProperty.addValidationResultsChangeListener(propertyValidationResultsChangeListener);
		}
		if (editableChangeListener != null) {
		    metaProperty.addEditableChangeListener(editableChangeListener);
		}
		if (requiredChangeListener != null) {
		    metaProperty.addRequiredChangeListener(requiredChangeListener);
		}
	    }
	}

	/**
	 * removes SubjectValueChangeHandler from the ActualEntity based on entityOrBufferedWrapper
	 *
	 * @param entityOrBufferedWrapper
	 * @param propertyName
	 * @param subjectValueChangeHandler
	 */
	public static void removePropertySpecificListener(final IBindingEntity entityOrBufferedWrapper, final String propertyName, final PropertyChangeListener subjectValueChangeHandler) {
	    checkNullability(subjectValueChangeHandler, "subjectValueChangeHandler");
	    // remove the subject value change listener to entity's property
	    getActualEntity(entityOrBufferedWrapper).removePropertyChangeListener(propertyName, subjectValueChangeHandler);
	}

	/**
	 * removes propertyValidationResultsChangeListener and editableChangeListener from the ActualEntity's MetaProperty based on entityOrBufferedWrapper
	 *
	 * @param entityOrBufferedWrapper
	 * @param propertyName
	 * @param propertyValidationResultsChangeListener
	 * @param editableChangeListener
	 */
	public static void removeMetaPropertySpecificListeners(final IBindingEntity entityOrBufferedWrapper, final String propertyName, //
		final PropertyValidationResultsChangeListener propertyValidationResultsChangeListener, //
		final EditableChangeListener editableChangeListener, //
		final RequiredChangeListener requiredChangeListener) {
	    // remove the property change listener to corresponding MetaProperty, to handle validation results
	    final MetaProperty metaProperty = getActualEntity(entityOrBufferedWrapper).getProperty(propertyName);
	    if (metaProperty != null) {
		if (propertyValidationResultsChangeListener != null) {
		    metaProperty.removeValidationResultsChangeListener(propertyValidationResultsChangeListener);
		}
		if (editableChangeListener != null) {
		    metaProperty.removeEditableChangeListener(editableChangeListener);
		}
		if (requiredChangeListener != null) {
		    metaProperty.removeRequiredChangeListener(requiredChangeListener);
		}
	    }
	}

	/**
	 * returns the "entityOrBufferedWrapper" if the "entityOrBufferedWrapper" is simple AbstractEntity descendant, or the SubjectBean of the BufferedPropertyWrapper, if the
	 * "entityOrBufferedWrapper" is BufferedPropertyWrapper
	 *
	 * @param entityOrBufferedWrapper
	 * @return
	 */
	public static IBindingEntity getActualEntity(final IBindingEntity entityOrBufferedWrapper) {
	    if (entityOrBufferedWrapper instanceof AutocompleterBufferedPropertyWrapper) {
		return ((AutocompleterBufferedPropertyWrapper<?>) entityOrBufferedWrapper).getSubjectBean();
	    } else if (entityOrBufferedWrapper instanceof BufferedPropertyWrapper) {
		return ((BufferedPropertyWrapper) entityOrBufferedWrapper).getSubjectBean();
	    } else {
		return entityOrBufferedWrapper;
	    }
	}

	/**
	 * Returns the type of property for specified entity (or its buffered wrapper).
	 *
	 * @param entityOrBufferedWrapper
	 * @param propertyName
	 * @return
	 */
	public static Class<?> getPropertyType(final IBindingEntity entityOrBufferedWrapper, final String propertyName) {
	    final IBindingEntity actualEntity = getActualEntity(entityOrBufferedWrapper);
	    return (actualEntity.getProperty(propertyName) == null) ? actualEntity.getPropertyType(propertyName) : actualEntity.getProperty(propertyName).getType();
	}

	/**
	 * Initiates the specified BoundedValidationLayer by its IOnCommitActionable. If the "entity" is buffered Property wrapper, sets it as IOnCommitActionable. Else sets
	 * thisPropertyConnector as IOnCommitActionable(this method should be invoked from thisPropertyConnector constructor
	 *
	 * @param thisPropertyConnector
	 * @param boundedValidationLayer
	 * @param entity
	 */
	public static void initiateIOnCommitActionable(final IPropertyConnector thisPropertyConnector, final BoundedValidationLayer<?> boundedValidationLayer, final IBindingEntity entity) {
	    if (entity instanceof BufferedPropertyWrapper) {
		boundedValidationLayer.setOnCommitActionable((BufferedPropertyWrapper) entity);
	    } else if (entity instanceof AutocompleterBufferedPropertyWrapper) {
		boundedValidationLayer.setOnCommitActionable((AutocompleterBufferedPropertyWrapper<?>) entity);
	    } else if (thisPropertyConnector instanceof IOnCommitActionable) {
		boundedValidationLayer.setOnCommitActionable((IOnCommitActionable) thisPropertyConnector);
	    } else {
		logger.error("Incorrect thisPropertyConnector type.");
	    }
	}
    }

    // ==================== Updating utils :

    /**
     * Creates tooltip for the editor based on a) {@link MetaProperty}'s state and/or b) property value and/or c) original tooltip text.
     */
    public static String createToolTipByValueAndMetaProperty(final IBindingEntity entity, final String propertyName, final MetaProperty boundedMetaProperty, final String originalToolTipText, final boolean byValue) {
	return createToolTipByValueAndMetaProperty(entity, propertyName, boundedMetaProperty, originalToolTipText, byValue, false, false);
    }

    /**
     * Creates tooltip for the editor based on a) {@link MetaProperty}'s state and/or b) property value and/or c) original tooltip text.
     *
     * @param entity
     * @param propertyName
     * @param boundedMetaProperty
     * @param originalToolTipText
     * @param byValue
     * @param stringBinding
     * @param isMulti
     * @return
     */
    public static String createToolTipByValueAndMetaProperty(final IBindingEntity entity, final String propertyName, final MetaProperty boundedMetaProperty, final String originalToolTipText, final boolean byValue, final boolean stringBinding, final boolean isMulti) {
	final Object subjectValue = Rebinder.getActualEntity(entity).get(propertyName);
	if (boundedMetaProperty != null) {
	    return !boundedMetaProperty.isValid() ? boundedMetaProperty.getFirstFailure().getMessage()
		    : (boundedMetaProperty.hasWarnings() ? boundedMetaProperty.getFirstWarning().getMessage()
			    : (!byValue ? originalToolTipText : createToolTipByValue(boundedMetaProperty.getType(), boundedMetaProperty.getPropertyAnnotationType(), subjectValue, originalToolTipText, stringBinding, isMulti)));
	} else {
	    final AbstractEntity eeeentity = (AbstractEntity) Rebinder.getActualEntity(entity);
	    final Class<AbstractEntity> actualEntityType = (eeeentity instanceof EntityQueryCriteria ? ((Class<AbstractEntity>) ((EntityQueryCriteria) eeeentity).getEntityClass()) : eeeentity.getType());
	    final String actualPropertyName = EntityDescriptor.getPropertyNameWithoutKeyPart(EntityDescriptor.enhanceDynamicCriteriaPropertyEditorKey(propertyName, actualEntityType));
	    final IsProperty isProperty = AnnotationReflector.getPropertyAnnotation(IsProperty.class, actualEntityType, actualPropertyName);
	    final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(actualEntityType, actualPropertyName);
	    final Class<?> collectionalElementType = isProperty == null ? null : isProperty.value();

	    return !byValue ? originalToolTipText : createToolTipByValue(propertyType, collectionalElementType, subjectValue, originalToolTipText, stringBinding, isMulti);
	}
    }

    private static String createToolTipByValue(final Class<?> propertyType, final Class<?> collectionalElementType, final Object subjectValue, final String originalToolTipText, final boolean stringBinding, final boolean isMulti) {
	final Converter converter = !stringBinding ? EntityUtils.chooseConverterBasedUponPropertyType(propertyType, collectionalElementType, EntityUtils.ShowingStrategy.DESC_ONLY)
		: createStringBindingConverter(isMulti);
	final String s = (converter == null) //
		? (subjectValue == null ? "" : ("" + subjectValue)) //
			: converter.convertToString(subjectValue);
		return StringUtils.isEmpty(s) ? originalToolTipText : TitlesDescsGetter.addHtmlTag("<i>" + TitlesDescsGetter.removeHtmlTag(s) + "</i>");
    }

    private static Converter createStringBindingConverter(final boolean isMulti) {
	return isMulti ? ConverterFactory.createStringListConverter() : ConverterFactory.createTrivialConverter();
    }

    /**
     * updates validation Ui by actual last validation result stored in MetaProperty
     *
     * @param boundedMetaProperty
     * @param boundedValidationlayer
     */
    protected static void updateValidationUIbyMetaPropertyValidationState(final MetaProperty boundedMetaProperty, final BoundedValidationLayer<?> boundedValidationlayer) {
	if (boundedMetaProperty != null) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    boundedValidationlayer.setResult(!boundedMetaProperty.isValid() ? boundedMetaProperty.getFirstFailure()
			    : new Result(boundedMetaProperty.getEntity(), "everything is cool"));
		    boundedValidationlayer.updateTooltip();
		}
	    });
	}
    }

    /**
     * the interface for all SubjectValueChangeHandlers in Bind API. Implement it in all adapters/connectors. It is descendant of PropertyChangeOrIncorrectAttemptListener that
     * fires not only after the value setting, but also after validation fails! And it updates the state of the component even if the value wasn't set!
     *
     * @see PropertyChangeOrIncorrectAttemptListener PropertyChangeOrIncorrectAttemptListener
     *
     * @author jhou
     *
     */
    protected interface SubjectValueChangeHandler extends PropertyChangeOrIncorrectAttemptListener {
    }

    /**
     * This listener updates the validation UI by the new validation result. Have to be added to all bounded MetaProperties by the MetaProperty.addValidationResultsChangeListener()
     * method
     *
     * @author jhou
     *
     */
    protected static class PropertyValidationResultsChangeListener implements PropertyChangeListener {

	private final BoundedValidationLayer<?> validationLayer;

	private final Logger logger = Logger.getLogger(this.getClass());

	public PropertyValidationResultsChangeListener(final BoundedValidationLayer<?> validationLayer) {
	    this.validationLayer = validationLayer;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
	    if (evt.getNewValue() instanceof Result) {
		final Result result = (Result) evt.getNewValue();
		SwingUtilitiesEx.invokeLater(new Runnable() {
		    @Override
		    public void run() {
			logger.debug("[" + validationLayer.getView().getClass().getSimpleName() + "] updates by result == " + result);
			validationLayer.setResult(result);
			validationLayer.updateTooltip();
		    }
		});
	    }
	}

    }

    /**
     * Listener to be added to the bounded MetaProperties, to change the state of the bounded component based on the MetaProperty "editable" state
     *
     * @author jhou
     *
     */
    protected static class EditableChangeListener implements PropertyChangeListener {

	private final BoundedValidationLayer<?> validationLayer;

	public EditableChangeListener(final BoundedValidationLayer<?> validationLayer) {
	    this.validationLayer = validationLayer;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
	    final boolean b = (Boolean) evt.getNewValue();
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    validationLayer.setEnabled(b);
		}
	    });
	}
    }

    /**
     * Listener to be added to the bounded MetaProperties, to change the state of the bounded component based on the MetaProperty "required" state
     *
     * @author jhou
     *
     */
    protected static class RequiredChangeListener implements PropertyChangeListener {
	private final BoundedValidationLayer<?> validationLayer;
	private final Logger logger = Logger.getLogger(this.getClass());

	public RequiredChangeListener(final BoundedValidationLayer<?> validationLayer) {
	    this.validationLayer = validationLayer;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
	    final boolean required = (Boolean) evt.getNewValue();
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    logger.debug("[" + validationLayer.getView().getClass().getSimpleName() + "] updates by required state == " + required);
		    validationLayer.getUI().setRequired(required);
		}
	    });
	}
    }

    /**
     * A JTextComponent client property key used to store and retrieve the BufferedValueModel associated with text components that commit on focus lost.
     *
     * @see #bindTriggeredAutocompleter(JTextArea, ValueModel, boolean)
     * @see #bindTriggeredAutocompleter(JTextField, ValueModel, boolean)
     * @see #flushImmediately()
     *
     */
    public static final String COMMIT_ON_FOCUS_LOST_MODEL_KEY = "commitOnFocusListModel";

    // /**
    // * Holds a weak trigger that is shared by BufferedValueModels that commit on
    // * permanent focus change.
    // *
    // * @see #createCommitOnFocusLostModel(ValueModel, Component)
    // */
    // /**
    // *
    // */
    // private static final WeakTrigger FOCUS_LOST_TRIGGER = new WeakTrigger();

    /**
     * Creates and returns a ValueModel that commits its value if the given component looses the focus permanently. It wraps the underlying ValueModel with a BufferedValueModel and
     * delays the value commit until this class' shared FOCUS_LOST_TRIGGER commits. This happens, because this class' shared FOCUS_LOST_HANDLER is registered with the specified
     * component.
     *
     * @param valueModel
     *            the model that provides the value
     * @param validationLayer
     *            the component that looses the focus
     * @return a buffering ValueModel that commits on focus lost
     *
     * @throws NullPointerException
     *             if the value model is {@code null}
     */
    static BufferedPropertyWrapper createCommitOnFocusLostModel(final IBindingEntity entity, final String propertyName, final BoundedValidationLayer<? extends JComponent> validationLayer, final IOnCommitAction... actions) {
	// The new instance of the trigger creates for every onFocusLostComponent! It provided for correct external commit action, using method BoundedValidationLayer.commit()
	final ValueModel trigger = new WeakTrigger(); // FOCUS_LOST_TRIGGER;
	final BufferedPropertyWrapper model = new BufferedPropertyWrapper(entity, propertyName, /* FOCUS_LOST_TRIGGER */trigger, actions);
	if (validationLayer.getView() instanceof DatePickerLayer) {
	    ((DatePickerLayer) validationLayer.getView()).getView().addFocusListener(new FocusLostCustomHandler(trigger));
	} else if (validationLayer.getView() instanceof JXDatePicker) {
	    ((JXDatePicker) validationLayer.getView()).getEditor().addFocusListener(new FocusLostCustomHandler(trigger));
	} else if (validationLayer.getView() instanceof JSpinner) {
	    ((JSpinner.DefaultEditor) ((JSpinner) validationLayer.getView()).getEditor()).getTextField().addFocusListener(new FocusLostCustomHandler(trigger));
	} else {
	    validationLayer.getView().addFocusListener(new FocusLostCustomHandler(trigger));
	}
	// this will set the trigger to the BoundedValidationLayer to make it manually committable!
	validationLayer.setTrigger(trigger);
	return model;
    }

    private static AutocompleterBufferedPropertyWrapper createCommitOnFocusLostAutocompleterModel(final IBindingEntity entity, final String propertyName, final BoundedValidationLayer<? extends AutocompleterTextFieldLayer> autocompleterValidationLayer, final boolean stringBinding, final IOnCommitAction... actions) {
	// The new instance of the trigger creates for every onFocusLostComponent! It provided for correct external commit action, using method BoundedValidationLayer.commit()
	final ValueModel trigger = new WeakTrigger();
	final AutocompleterBufferedPropertyWrapper model = new AutocompleterBufferedPropertyWrapper(entity, propertyName, trigger, autocompleterValidationLayer.getView(), stringBinding, actions);
	autocompleterValidationLayer.getView().getView().addFocusListener(new FocusLostCustomHandler(trigger));
	// this will set the trigger to the BoundedValidationLayer to make it manually committable!
	autocompleterValidationLayer.setTrigger(trigger);
	return model;
    }

    /**
     * Triggers a commit event on permanent focus lost.
     */
    private static final class FocusLostCustomHandler extends FocusAdapter {

	final private ValueModel trigger;

	public FocusLostCustomHandler(final ValueModel trigger) {
	    this.trigger = trigger;
	}

	/**
	 * Triggers a commit event if the focus lost is permanent.
	 *
	 * @param evt
	 *            the focus lost event
	 */
	@Override
	public void focusLost(final FocusEvent evt) {
	    if (!evt.isTemporary()) {
		if (trigger instanceof WeakTrigger) {
		    ((WeakTrigger) trigger).triggerCommit();
		} else {
		    ((Trigger) trigger).triggerCommit();
		}
	    }
	}
    }

    /**
     * Checks and answers whether the focus owner is a component that buffers a pending edit. Useful to enable or disable a text component Action that resets the edited value.
     * <p>
     *
     * See also the JFormattedTextField's internal {@code CancelAction}.
     *
     * @return {@code true} if the focus owner is a JTextComponent that commits on focus-lost and is buffering
     *
     * @see #flushImmediately()
     *
     * @since 2.0.1
     */
    @SuppressWarnings("unchecked")
    public static boolean isFocusOwnerBuffering() {
	final IBindingEntity commitOnFocusLostModel = getBufferedPropertyWrapperFromFocusOwner();
	return (commitOnFocusLostModel != null) ? ((commitOnFocusLostModel instanceof BufferedPropertyWrapper) ? ((BufferedPropertyWrapper) commitOnFocusLostModel).isBuffering()
		: ((AutocompleterBufferedPropertyWrapper) commitOnFocusLostModel).isBuffering()) : false;
    }

    /**
     * This method returns [Autocompleter]BufferedPropertyWrapper of the bounded OnFocusLost focus owner. If the focus owner is not the bounded component, or is the Bounded
     * component but with OnKeyTyped or OnTriggerCommit strategy -> it returns null.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private static IBindingEntity getBufferedPropertyWrapperFromFocusOwner() {
	final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
	if (!(focusOwner instanceof JComponent)) {
	    return null;
	}
	final Object value = ((JComponent) focusOwner).getClientProperty(COMMIT_ON_FOCUS_LOST_MODEL_KEY);
	if (value == null) {
	    logger.error(new RuntimeException("the focusOwner is not OnFocusLost bounded component! (maybe OnKeyTyped or OnTriggerCommit)"));
	    return null;
	} else if (!(value instanceof BufferedPropertyWrapper) && !(value instanceof AutocompleterBufferedPropertyWrapper)) {
	    throw new RuntimeException("the client property COMMIT_ON_FOCUS_LOST_MODEL_KEY was unsuccessfully setted!");
	}
	return (value instanceof BufferedPropertyWrapper) ? (BufferedPropertyWrapper) value : (AutocompleterBufferedPropertyWrapper) value;
    }

    /**
     * Commits the buffered OnFocusLost bounded component that have the focus in the current window.
     */
    @SuppressWarnings("unchecked")
    public static void commitFocusOwner() {
	if (isFocusOwnerBuffering()) {
	    final IBindingEntity commitOnFocusLostModel = getBufferedPropertyWrapperFromFocusOwner();
	    final ValueModel trigger = (commitOnFocusLostModel instanceof BufferedPropertyWrapper) ? //
		    ((BufferedPropertyWrapper) commitOnFocusLostModel).getTriggerChannel() //
		    : ((AutocompleterBufferedPropertyWrapper) commitOnFocusLostModel).getTriggerChannel();
		    if (trigger instanceof WeakTrigger) {
			((WeakTrigger) trigger).triggerCommit();
		    } else {
			((Trigger) trigger).triggerCommit();
		    }
	}
    }

    /**
     * binds ReadOnlyLabel using determination of the appropriate converter. And updates text and editable state
     *
     * @param validationLayer
     * @param entity
     * @param propertyName
     */
    protected static void bindLabel(final BoundedValidationLayer<? extends ReadOnlyLabel> validationLayer, final IBindingEntity entity, final String propertyName, final ShowingStrategy showingStrategy) {
	new LabelConnector(entity, propertyName, validationLayer, showingStrategy);
    }

    /**
     * Binds textField with entity's property using default converter
     *
     * @param textField
     * @param entity
     * @param propertyName
     * @param commitOnFocusLost
     *            - true to commit on focus lost, false - to commit on key typed!!
     */
    protected static void bindStringTextAreaOrField(final BoundedValidationLayer<? extends JTextComponent> boundedValidationLayer, final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final IOnCommitAction... actions) {
	if (commitOnFocusLost) {
	    final JTextComponent textComponent = boundedValidationLayer.getView();
	    if (textComponent == null) {
		throw new NullPointerException("The textComponent of the Validationlayer must not be null.");
	    }
	    final BufferedPropertyWrapper propertyWrapper = createCommitOnFocusLostModel(entity, propertyName, boundedValidationLayer, actions);
	    textComponent.putClientProperty(COMMIT_ON_FOCUS_LOST_MODEL_KEY, propertyWrapper);
	    new TextComponentConnector(propertyWrapper, boundedValidationLayer);
	} else {
	    new TextComponentConnector(entity, propertyName, boundedValidationLayer, actions);
	}
    }

    protected static <T extends AbstractEntity> void bindCollectionalPropertyWithEGI(final BoundedValidationLayer<? extends EntityGridInspector<T>> boundedValidationLayer, final IBindingEntity entity, final String propertyName, final IOnCommitAction... actions) {
	new EgiConnector<T>(entity, propertyName, boundedValidationLayer, actions);
    }

    /**
     * Binds bigDecimal or Money (used correct type determination) field. onFocusLost - true to create onFocusLost committing field, false - to create OnKeyTyped committing field
     *
     * @param boundedValidationLayer
     * @param entity
     * @param propertyName
     * @param actions
     * @return
     */
    protected static void bindBigDecimalOrMoneyOrIntegerOrDouble(final BoundedValidationLayer<? extends JFormattedTextField> boundedValidationLayer, final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final IOnCommitAction... actions) {
	final JFormattedTextField textField = boundedValidationLayer.getView();
	if (textField == null) {
	    throw new NullPointerException("The textField of the ValidationLayer must not be null.");
	}
	if (commitOnFocusLost) {
	    final BufferedPropertyWrapper propertyWrapper = createCommitOnFocusLostModel(entity, propertyName, boundedValidationLayer, actions);
	    textField.putClientProperty(COMMIT_ON_FOCUS_LOST_MODEL_KEY, propertyWrapper);
	    new FormattedFieldConnector(propertyWrapper, boundedValidationLayer, textField);
	} else {
	    new FormattedFieldConnector(entity, propertyName, boundedValidationLayer, textField, actions);
	}
    }

    /**
     * Binds date picker layer. onFocusLost - true to create onFocusLost committing field, false - to create OnKeyTyped committing field
     *
     * @param boundedValidationLayer
     * @param entity
     * @param propertyName
     * @param actions
     * @return
     */
    protected static void bindDate(final BoundedValidationLayer<DatePickerLayer> boundedValidationLayer, final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final IOnCommitAction... actions) {
	final JFormattedTextField textField = boundedValidationLayer.getView().getView();
	if (textField == null) {
	    throw new NullPointerException("The textField of the ValidationLayer must not be null.");
	}
	if (commitOnFocusLost) {
	    final BufferedPropertyWrapper propertyWrapper = createCommitOnFocusLostModel(entity, propertyName, boundedValidationLayer, actions);
	    textField.putClientProperty(COMMIT_ON_FOCUS_LOST_MODEL_KEY, propertyWrapper);
	    new FormattedFieldConnector(propertyWrapper, boundedValidationLayer, textField);
	} else {
	    new FormattedFieldConnector(entity, propertyName, boundedValidationLayer, textField, actions);
	}
    }

    /**
     * Binds bigDecimal or Money (used correct type determination) field. onFocusLost - true to create onFocusLost committing field, false - to create OnKeyTyped committing field
     *
     * @param boundedValidationLayer
     * @param entity
     * @param propertyName
     * @param actions
     * @return
     */
    protected static SpinnerConnector bindBigDecimalOrMoneyOrIntegerOrDoubleWithSpinner(final BoundedValidationLayer<? extends JSpinner> boundedValidationLayer, final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final IOnCommitAction... actions) {
	if (commitOnFocusLost) {
	    final BufferedPropertyWrapper propertyWrapper = createCommitOnFocusLostModel(entity, propertyName, boundedValidationLayer, actions);
	    final JSpinner spinner = boundedValidationLayer.getView();
	    if (spinner == null) {
		throw new NullPointerException("The spinner must not be null.");
	    }
	    spinner.putClientProperty(COMMIT_ON_FOCUS_LOST_MODEL_KEY, propertyWrapper);
	    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().putClientProperty(COMMIT_ON_FOCUS_LOST_MODEL_KEY, propertyWrapper);
	    return new SpinnerConnector(propertyWrapper, boundedValidationLayer);
	} else {
	    return new SpinnerConnector(entity, propertyName, boundedValidationLayer, actions);
	}
    }

    protected static void bindTriggeredBigDecimalOrMoneyOrIntegerOrDouble(final BoundedValidationLayer<? extends JFormattedTextField> boundedValidationLayer, final IBindingEntity entity, final String propertyName, final ValueModel triggerChannel, final IOnCommitAction... actions) {
	// create BufferedPropertyWrapper:
	final BufferedPropertyWrapper bufferedPropertyWrapper = new BufferedPropertyWrapper(entity, propertyName, triggerChannel, actions);
	// bind BufferedPropertyWrapper with the boundedValidationLayer:
	new FormattedFieldConnector(bufferedPropertyWrapper, boundedValidationLayer, boundedValidationLayer.getView());
    }

    /**
     * creates connection between propertyWrapper and autocompleter, and updates the component text
     *
     * @param <T>
     * @param boundedValidationLayer
     * @param autocompleterBufferedPropertyWrapper
     */
    protected static <T> void bindTriggeredAutocompleter(final BoundedValidationLayer<AutocompleterTextFieldLayer<T>> boundedValidationLayer, final IBindingEntity entity, final String propertyName, final ValueModel triggerChannel, final boolean stringBinding, final IOnCommitAction... actions) {
	final AutocompleterBufferedPropertyWrapper<T> autocompleterPropertyWrapper = new AutocompleterBufferedPropertyWrapper<T>(entity, propertyName, triggerChannel, boundedValidationLayer.getView(), stringBinding, actions);
	new AutocompleterConnector<T>(autocompleterPropertyWrapper, boundedValidationLayer);
    }

    /**
     * creates commitOnFocusLost autocompleter property wrapper and binds it with autocompleter by creating {@link AutocompleterConnector}. (and updates the component text)
     *
     * @param <T>
     * @param autocompleterValidationLayer
     * @param entity
     * @param propertyName
     */
    protected static void bindOnFocusLostAutocompleter(final BoundedValidationLayer<? extends AutocompleterTextFieldLayer> autocompleterValidationLayer, final IBindingEntity entity, final String propertyName, final boolean stringBinding, final String originalToolTipText, // final boolean manuallyComittable,
	    final IOnCommitAction... actions) {
	final AutocompleterBufferedPropertyWrapper autocompleterBufferedPropertyWrapper = createCommitOnFocusLostAutocompleterModel(entity, propertyName, autocompleterValidationLayer, stringBinding, actions);
	autocompleterValidationLayer.getView().getView().putClientProperty(COMMIT_ON_FOCUS_LOST_MODEL_KEY, autocompleterBufferedPropertyWrapper);
	new AutocompleterConnector(autocompleterBufferedPropertyWrapper, autocompleterValidationLayer);
    }

    /**
     * Binds textField with propertyWrapper using default converter
     *
     * @param textField
     * @param bufferedPropertyWrapper
     */
    protected static void bindTriggeredStringTextAreaOrField(final BoundedValidationLayer<? extends JTextComponent> boundedValidationLayer, final IBindingEntity entity, final String propertyName, final ValueModel triggerChannel, final IOnCommitAction... actions) {
	// create BufferedPropertyWrapper:
	final BufferedPropertyWrapper bufferedPropertyWrapper = new BufferedPropertyWrapper(entity, propertyName, triggerChannel, actions);
	// bind BufferedPropertyWrapper with the boundedValidationLayer:
	new TextComponentConnector(bufferedPropertyWrapper, boundedValidationLayer);

    }

    /**
     * Binds checkBox with Boolean entity's property
     *
     * @param boundedCheckBoxValidationLayer
     * @param entity
     * @param propertyName
     */
    protected static void bindCheckBox(final BoundedValidationLayer<JCheckBox> boundedCheckBoxValidationLayer, final IBindingEntity entity, final String propertyName, final boolean readOnly, final IOnCommitAction... actions) {
	final boolean enabled = boundedCheckBoxValidationLayer.getView().getModel().isEnabled();
	final ButtonModel tba = new ToggleButtonAdapter(entity, propertyName, boundedCheckBoxValidationLayer, readOnly, actions);
	boundedCheckBoxValidationLayer.getView().setModel(tba);
	boundedCheckBoxValidationLayer.getView().setEnabled(enabled);
    }

    /**
     * Binds checkBox with propertyWrapper around Boolean property
     *
     * @param boundedValidationLayer
     * @param bufferedPropertyWrapper
     */
    protected static void bindTriggeredCheckBox(final BoundedValidationLayer<JCheckBox> boundedValidationLayer, final boolean readOnly, final IBindingEntity entity, final String propertyName, final ValueModel triggerChannel, final IOnCommitAction... actions) {
	// create BufferedPropertyWrapper:
	final BufferedPropertyWrapper bufferedPropertyWrapper = new BufferedPropertyWrapper(entity, propertyName, triggerChannel, actions);
	// bind BufferedPropertyWrapper with the boundedValidationLayer:
	final boolean enabled = boundedValidationLayer.getView().getModel().isEnabled();
	final ToggleButtonAdapter tba = new ToggleButtonAdapter(bufferedPropertyWrapper, boundedValidationLayer, false);
	boundedValidationLayer.getView().setModel(tba);
	boundedValidationLayer.getView().setEnabled(enabled);
    }

    /**
     * Binds <code>radioButton</code> with entity's property and with choice
     *
     *
     * @param radioButtonValidationLayer
     * @param entity
     * @param propertyName
     * @param choice
     */
    protected static void bindRadioButton(final BoundedValidationLayer<JRadioButton> radioButtonValidationLayer, final IBindingEntity entity, final String propertyName, final Object choice, final IOnCommitAction... actions) {
	final boolean enabled = radioButtonValidationLayer.getView().getModel().isEnabled();
	final RadioButtonAdapter rba = new RadioButtonAdapter(entity, propertyName, choice, radioButtonValidationLayer, actions);
	radioButtonValidationLayer.getView().setModel(rba);
	radioButtonValidationLayer.getView().setEnabled(enabled);
    }

    /**
     * Binds <code>radioButton</code> with propertyWrapper and with choice
     *
     * @param radioButton
     * @param bufferedPropertyWrapper
     * @param choice
     */
    protected static void bindTriggeredRadioButton(final BoundedValidationLayer<JRadioButton> radioButtonValidationLayer, final BufferedPropertyWrapper bufferedPropertyWrapper, final Object choice) {
	final boolean enabled = radioButtonValidationLayer.getView().getModel().isEnabled();
	final RadioButtonAdapter rba = new RadioButtonAdapter(bufferedPropertyWrapper, choice, radioButtonValidationLayer);
	radioButtonValidationLayer.getView().setModel(rba);
	radioButtonValidationLayer.getView().setEnabled(enabled);
    }

    // Helper Code for a Weak Trigger *****************************************

    /**
     * Unlike the Trigger class, this implementation uses WeakReferences to store value change listeners.
     */
    static final class WeakTrigger implements ValueModel {

	private final transient WeakPropertyChangeSupport changeSupport;

	private Boolean value;

	// Instance Creation ******************************************************

	/**
	 * Constructs a WeakTrigger set to neutral.
	 */
	WeakTrigger() {
	    value = null;
	    changeSupport = new WeakPropertyChangeSupport(this);
	}

	// ValueModel Implementation **********************************************

	/**
	 * Returns a Boolean that indicates the current trigger state.
	 *
	 * @return a Boolean that indicates the current trigger state
	 */
	public Object getValue() {
	    return value;
	}

	/**
	 * Sets a new Boolean value and rejects all non-Boolean values. Fires no change event if the new value is equal to the previously set value.
	 * <p>
	 *
	 * This method is not intended to be used by API users. Instead you should trigger commit and flush events by invoking <code>#triggerCommit</code> or
	 * <code>#triggerFlush</code>.
	 *
	 * @param newValue
	 *            the Boolean value to be set
	 * @throws IllegalArgumentException
	 *             if the newValue is not a Boolean
	 */
	public void setValue(final Object newValue) {
	    if ((newValue != null) && !(newValue instanceof Boolean)) {
		throw new IllegalArgumentException("Trigger values must be of type Boolean.");
	    }

	    final Object oldValue = value;
	    value = (Boolean) newValue;
	    fireValueChange(oldValue, newValue);
	}

	// Change Management ****************************************************

	/**
	 * Registers the given PropertyChangeListener with this model. The listener will be notified if the value has changed.
	 * <p>
	 *
	 * The PropertyChangeEvents delivered to the listener have the name set to "value". In other words, the listeners won't get notified when a PropertyChangeEvent is fired
	 * that has a null object as the name to indicate an arbitrary set of the event source's properties have changed.
	 * <p>
	 *
	 * In the rare case, where you want to notify a PropertyChangeListener even with PropertyChangeEvents that have no property name set, you can register the listener with
	 * #addPropertyChangeListener, not #addValueChangeListener.
	 *
	 * @param listener
	 *            the listener to add
	 *
	 * @see ValueModel
	 */
	public void addValueChangeListener(final PropertyChangeListener listener) {
	    if (listener == null) {
		return;
	    }
	    changeSupport.addPropertyChangeListener("value", listener);
	}

	/**
	 * Removes the given PropertyChangeListener from the model.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeValueChangeListener(final PropertyChangeListener listener) {
	    if (listener == null) {
		return;
	    }
	    changeSupport.removePropertyChangeListener("value", listener);
	}

	/**
	 * Notifies all listeners that have registered interest for notification on this event type. The event instance is lazily created using the parameters passed into the fire
	 * method.
	 *
	 * @param oldValue
	 *            the value before the change
	 * @param newValue
	 *            the value after the change
	 *
	 * @see java.beans.PropertyChangeSupport
	 */
	private void fireValueChange(final Object oldValue, final Object newValue) {
	    changeSupport.firePropertyChange("value", oldValue, newValue);
	}

	// Triggering *************************************************************

	/**
	 * Triggers a commit event in models that share this Trigger. Sets the value to {@code Boolean.TRUE} and ensures that listeners are notified about a value change to this
	 * new value. If necessary the value is temporarily set to {@code null}. This way it minimizes the number of PropertyChangeEvents fired by this Trigger.
	 */
	void triggerCommit() {
	    if (Boolean.TRUE.equals(getValue())) {
		setValue(null);
	    }
	    setValue(Boolean.TRUE);
	}

	/**
	 * Triggers a flush event in models that share this Trigger. Sets the value to {@code Boolean.FALSE} and ensures that listeners are notified about a value change to this
	 * new value. If necessary the value is temporarily set to {@code null}. This way it minimizes the number of PropertyChangeEvents fired by this Trigger.
	 */
	void triggerFlush() {
	    if (Boolean.FALSE.equals(getValue())) {
		setValue(null);
	    }
	    setValue(Boolean.FALSE);
	}

    }

    /**
     * Differs from its superclass {@link PropertyChangeSupport} in that it uses WeakReferences for registering listeners. It wraps registered PropertyChangeListeners with
     * instances of WeakPropertyChangeListener and cleans up a list of stale references when firing an event.
     * <p>
     *
     */
    private static final class WeakPropertyChangeSupport extends PropertyChangeSupport {

	// Instance Creation ******************************************************
	private static final long serialVersionUID = 4988226888912487644L;

	/**
	 * Constructs a WeakPropertyChangeSupport object.
	 *
	 * @param sourceBean
	 *            The bean to be given as the source for any events.
	 */
	WeakPropertyChangeSupport(final Object sourceBean) {
	    super(sourceBean);
	}

	// Managing Property Change Listeners **********************************

	/** {@inheritDoc} */
	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener) {
	    if (listener == null) {
		return;
	    }
	    if (listener instanceof PropertyChangeListenerProxy) {
		final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
		// Call two argument add method.
		addPropertyChangeListener(proxy.getPropertyName(), (PropertyChangeListener) proxy.getListener());
	    } else {
		super.addPropertyChangeListener(new WeakPropertyChangeListener(listener));
	    }
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
	    if (listener == null) {
		return;
	    }
	    super.addPropertyChangeListener(propertyName, new WeakPropertyChangeListener(propertyName, listener));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
	    if (listener == null) {
		return;
	    }
	    if (listener instanceof PropertyChangeListenerProxy) {
		final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
		// Call two argument remove method.
		removePropertyChangeListener(proxy.getPropertyName(), (PropertyChangeListener) proxy.getListener());
		return;
	    }
	    final PropertyChangeListener[] listeners = getPropertyChangeListeners();
	    WeakPropertyChangeListener wpcl;
	    for (int i = listeners.length - 1; i >= 0; i--) {
		if (listeners[i] instanceof PropertyChangeListenerProxy) {
		    continue;
		}
		wpcl = (WeakPropertyChangeListener) listeners[i];
		if (wpcl.get() == listener) {
		    super.removePropertyChangeListener(wpcl);
		    break;
		}
	    }
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
	    if (listener == null) {
		return;
	    }
	    final PropertyChangeListener[] listeners = getPropertyChangeListeners(propertyName);
	    WeakPropertyChangeListener wpcl;
	    for (int i = listeners.length - 1; i >= 0; i--) {
		wpcl = (WeakPropertyChangeListener) listeners[i];
		if (wpcl.get() == listener) {
		    super.removePropertyChangeListener(propertyName, wpcl);
		    break;
		}
	    }
	}

	// Firing Events **********************************************************

	/**
	 * Fires the specified PropertyChangeEvent to any registered listeners.
	 *
	 * @param evt
	 *            The PropertyChangeEvent object.
	 *
	 * @see PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)
	 */
	@Override
	public void firePropertyChange(final PropertyChangeEvent evt) {
	    cleanUp();
	    super.firePropertyChange(evt);
	}

	/**
	 * Reports a bound property update to any registered listeners.
	 *
	 * @param propertyName
	 *            The programmatic name of the property that was changed.
	 * @param oldValue
	 *            The old value of the property.
	 * @param newValue
	 *            The new value of the property.
	 *
	 * @see PropertyChangeSupport#firePropertyChange(String, Object, Object)
	 */
	@Override
	public void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
	    cleanUp();
	    super.firePropertyChange(propertyName, oldValue, newValue);
	}

	static final ReferenceQueue<PropertyChangeListener> QUEUE = new ReferenceQueue<PropertyChangeListener>();

	private static void cleanUp() {
	    WeakPropertyChangeListener wpcl;
	    while ((wpcl = (WeakPropertyChangeListener) QUEUE.poll()) != null) {
		wpcl.removeListener();
	    }
	}

	void removeWeakPropertyChangeListener(final WeakPropertyChangeListener l) {
	    if (l.propertyName == null) {
		super.removePropertyChangeListener(l);
	    } else {
		super.removePropertyChangeListener(l.propertyName, l);
	    }
	}

	/**
	 * Wraps a PropertyChangeListener to make it weak.
	 */
	private final class WeakPropertyChangeListener extends WeakReference<PropertyChangeListener> implements PropertyChangeListener {

	    final String propertyName;

	    private WeakPropertyChangeListener(final PropertyChangeListener delegate) {
		this(null, delegate);
	    }

	    private WeakPropertyChangeListener(final String propertyName, final PropertyChangeListener delegate) {
		super(delegate, QUEUE);
		this.propertyName = propertyName;
	    }

	    /** {@inheritDoc} */
	    public void propertyChange(final PropertyChangeEvent evt) {
		final PropertyChangeListener delegate = get();
		if (delegate != null) {
		    delegate.propertyChange(evt);
		}
	    }

	    void removeListener() {
		removeWeakPropertyChangeListener(this);
	    }
	}
    }
}
