package ua.com.fielden.platform.swing.components.bind;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.calendar.CalendarUtils;
import org.jdesktop.swingx.calendar.DatePickerFormatter.DatePickerFormatterUIResource;
import org.jdesktop.swingx.calendar.DateUtils;
import org.jdesktop.swingx.calendar.SingleDaySelectionModel;
import org.jdesktop.swingx.event.DateSelectionEvent.EventType;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;

import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.bind.Binder.EditableChangeListener;
import ua.com.fielden.platform.swing.components.bind.Binder.IPropertyConnector;
import ua.com.fielden.platform.swing.components.bind.Binder.IRebindable;
import ua.com.fielden.platform.swing.components.bind.Binder.IUpdatable;
import ua.com.fielden.platform.swing.components.bind.Binder.PropertyValidationResultsChangeListener;
import ua.com.fielden.platform.swing.components.bind.Binder.Rebinder;
import ua.com.fielden.platform.swing.components.bind.Binder.RequiredChangeListener;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory.IOnCommitActionable;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

public class BoundedJXDatePicker extends JXDatePicker implements IOnCommitActionable, IUpdatable, IRebindable, IPropertyConnector {

    private static final long serialVersionUID = 8828485302066073780L;

    private final BoundedValidationLayer<BoundedJXDatePicker> boundedValidationLayer;

    private final List<IOnCommitAction> onCommitActions = new ArrayList<IOnCommitAction>();

    @SuppressWarnings("unchecked")
    private IBindingEntity entity;
    private String propertyName;

    // entity specific listeners :
    private SubjectValueChangeHandler subjectValueChangeHandler;
    private PropertyValidationResultsChangeListener propertyValidationResultsChangeListener;
    private EditableChangeListener editableChangeListener;
    private RequiredChangeListener requiredChangeListener;

    private final Long defaultTimePortionMillis;

    public class BoundedBasicDatePickerUI extends BasicDatePickerUI {

	private final IBindingEntity entity;
	private final String propertyName;

	public BoundedBasicDatePickerUI(final IBindingEntity entity, final String propertyName) {
	    super();
	    this.entity = entity;
	    this.propertyName = propertyName;
	}

	/**
	 * This method invokes when the editor value changed!!!
	 */
	@Override
	protected void updateFromValueChanged(final Date oldDate, final Date newDate) {
	    // ================================ the code from super class =================
	    if ((newDate != null) && datePicker.getMonthView().isUnselectableDate(newDate)) {
		datePicker.getEditor().setValue(oldDate);
		return;
	    }
	    // the other place to interrupt the update spiral
	    if (!CalendarUtils.areEqual(newDate, datePicker.getMonthView().getSelectionDate())) {
		datePicker.getMonthView().setSelectionDate(newDate);
	    }
	    // ================================
	    if (entity != null && propertyName != null) {
		final Date actualDate = (Date) entity.get(propertyName);
		if (newDate == null) {
		    if (actualDate == null) {
			// the error recovery logic should handle this situation
		    } else {
			datePicker.setDate(newDate);
		    }
		} else if (!newDate.equals(actualDate)) { // new Date Was not Successfully Setted In Subject Bean
		    datePicker.setDate(newDate);
		} else {
		    // the error recovery logic should handle this situation
		}
	    } else {
		datePicker.setDate(newDate);
	    }
	}

	/**
	 * this method invokes, when the MonthView selection changed!!!
	 */
	@Override
	protected void updateFromSelectionChanged(final EventType eventType, final boolean adjusting) {
	    if (adjusting) {
		return;
	    }
	    updateEditorValueForMonthViewSelectionChanged();
	}

	/**
	 * If the monthView selection changed - set the date modified by the time portion to the editor! The change of the editor value causes "updateFromValueChanged" method, and
	 * it updates the datePicker value setting, that causes binding value setting!
	 */
	private void updateEditorValueForMonthViewSelectionChanged() {
	    // System.out.println("+++++++++++++++++++++ updateEditorValue1()");
	    // System.out.println("***************************** " + datePicker.getMonthView().getSelectionDate());
	    final Date newDateWithTheTimePortion = modifyDateByTheTimePortion(datePicker.getMonthView().getSelectionDate(), getDate(), BoundedJXDatePicker.this.defaultTimePortionMillis);
	    // System.out.println("***************************** " + newDateWithTheTimePortion);
	    datePicker.getEditor().setValue(newDateWithTheTimePortion);
	}

	/**
	 * Creates the editor used to edit the date selection. The editor is configured with the default DatePickerFormatter marked as UIResource.
	 *
	 * @return an instance of a JFormattedTextField
	 */
	@Override
	protected JFormattedTextField createEditor() {
	    final JFormattedTextField f = new DefaultEditor1(new DatePickerFormatterUIResource(datePicker.getLocale()));
	    f.setName("dateField");
	    // this produces a fixed pref widths, looking a bit funny
	    // int columns = UIManagerExt.getInt("JXDatePicker.numColumns", null);
	    // if (columns > 0) {
	    // f.setColumns(columns);
	    // }
	    // that's always 0 as it comes from the resourcebundle
	    // f.setColumns(UIManager.getInt("JXDatePicker.numColumns"));
	    final Border border = UIManager.getBorder("JXDatePicker.border");
	    if (border != null) {
		f.setBorder(border);
	    }
	    return f;
	}

	private class DefaultEditor1 extends JFormattedTextField implements UIResource {
	    private static final long serialVersionUID = 1L;
	    private Dimension prefSizeCache;
	    private int prefEmptyInset;

	    public DefaultEditor1(final AbstractFormatter formatter) {
		super(formatter);
	    }

	    @Override
	    protected void processFocusEvent(final FocusEvent e) {
		super.processFocusEvent(e);
		if (e.getID() == FocusEvent.FOCUS_GAINED) {
		    selectAll();
		}
	    }

	    @Override
	    public Dimension getPreferredSize() {
		final Dimension preferredSize = super.getPreferredSize();
		if (getColumns() <= 0) {
		    if (getValue() == null) {
			if (prefSizeCache != null) {
			    preferredSize.width = prefSizeCache.width;
			    preferredSize.height = prefSizeCache.height;
			} else {
			    prefEmptyInset = preferredSize.width;
			    preferredSize.width = prefEmptyInset + getNullWidth();
			}
		    } else {
			preferredSize.width += Math.max(prefEmptyInset, 4);
			prefSizeCache = new Dimension(preferredSize);
		    }
		}
		return preferredSize;
	    }

	    /**
	     * @return
	     */
	    private int getNullWidth() {
		final JFormattedTextField field = new JFormattedTextField(getFormatter());
		field.setMargin(getMargin());
		field.setBorder(getBorder());
		field.setFont(getFont());
		field.setValue(new Date());
		return field.getPreferredSize().width;
	    }

	}

    }

    // Instance Creation *****************************************************

    /**
     * Constructor to with no binding capabilities providing, but with overriding the default BasicDatePickerUI, to get correct editor updating behaviour. This code is important -
     * not to set entity values when multiple component bounded to one property
     */
    public BoundedJXDatePicker(final String originalToolTipText, final boolean useTheTimePortion, final Long defaultTimePortionMillis) {
	super();
	setFormats(useTheTimePortion ? "dd/MM/yyyy hh:mma" : "dd/MM/yyyy");

	this.defaultTimePortionMillis = defaultTimePortionMillis;

	// Ui based on entity and propertyName !! entity or propertyName can be null!!
	this.setUI(new BoundedBasicDatePickerUI(this.entity, this.propertyName));

	// need to use dates with ability of getting time!!!!
	this.getMonthView().setSelectionModel(new SingleDaySelectionModel());
	this.boundedValidationLayer = ComponentFactory.createBoundedValidationLayer(this, originalToolTipText);
    }

    /**
     * Constructor with setting/adding listeners/changeHandlers and validation layer initializing
     *
     * @param entity
     * @param propertyName
     * @param originalToolTipText
     * @param actions
     */
    public static void bindJXDatePicker(final BoundedJXDatePicker boundedJXDatePicker, final IBindingEntity entity, final String propertyName, final String originalToolTipText, final IOnCommitAction... actions) {
	if (entity == null) {
	    throw new NullPointerException("The entity must not be null.");
	}
	if (propertyName == null) {
	    throw new NullPointerException("The propertyName must not be null.");
	}
	boundedJXDatePicker.entity = entity;
	boundedJXDatePicker.propertyName = propertyName;

	// initiate boundedValidationLayer
	if (boundedJXDatePicker.boundedValidationLayer == null) {
	    throw new NullPointerException("The validationLayer must not be null.");
	}
	// ---------------------------this.boundedValidationLayer = boundedValidationLayer;
	// initiate Entity specific listeners
	boundedJXDatePicker.subjectValueChangeHandler = new SubjectValueChangeHandler(boundedJXDatePicker);
	boundedJXDatePicker.propertyValidationResultsChangeListener = new PropertyValidationResultsChangeListener(boundedJXDatePicker.boundedValidationLayer);
	boundedJXDatePicker.editableChangeListener = new EditableChangeListener(boundedJXDatePicker.boundedValidationLayer);
	boundedJXDatePicker.requiredChangeListener = new RequiredChangeListener(boundedJXDatePicker.boundedValidationLayer);

	boundedJXDatePicker.addOwnEntitySpecificListeners();
	Rebinder.initiateReconnectables(boundedJXDatePicker.entity, boundedJXDatePicker, boundedJXDatePicker.boundedValidationLayer);

	// -------------------------------------------------------
	boundedJXDatePicker.boundedValidationLayer.setOnCommitActionable(boundedJXDatePicker);
	for (int i = 0; i < actions.length; i++) {
	    boundedJXDatePicker.addOnCommitAction(actions[i]);
	}

	// initial updating :
	boundedJXDatePicker.updateStates();
	// setting OnCommitActionable
	boundedJXDatePicker.initiateOnCommitActionable(boundedJXDatePicker.boundedValidationLayer);
    }

    @Override
    public void initiateOnCommitActionable(final BoundedValidationLayer<?> boundedValidationLayer) {
	Rebinder.initiateIOnCommitActionable(this, boundedValidationLayer, entity);
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
     * Since rebinding is supported, the entity can be changed
     *
     * @param entity
     */
    protected void setEntity(final IBindingEntity entity) {
	this.entity = entity;
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
    public boolean isOnKeyTyped() {
	return !(entity instanceof BufferedPropertyWrapper) && (!(entity instanceof AutocompleterBufferedPropertyWrapper));
    }

    /**
     * updates the state of {@link BoundedDateComboBoxFrozen} based upon actual value in the {@link IBindingEntity}'s property
     */
    public void updateByActualOrLastIncorrectValue() {
	if (entity != null && propertyName != null) {
	    final Object updatingValue = (boundedMetaProperty() == null || boundedMetaProperty().isValid()) //
	    ? entity.get(propertyName) //
		    : boundedMetaProperty().getLastInvalidValue();
	    if (updatingValue == null || (updatingValue != null && updatingValue instanceof Date)) {
		if (getDate() == null || (getDate() != null && !getDate().equals(updatingValue))) {
		    super.setDate((Date) updatingValue);
		}
	    }
	} else {
	    new Exception("updating state -> invokes with NULL entity or NULL propertyName").printStackTrace();
	}
    }

    /**
     * Actual setting the value for entity's property on SwingWorker
     */
    @Override
    public void setDate(final Date date) {
	if (entity == null || propertyName == null) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {

		@Override
		public void run() {
		    BoundedJXDatePicker.super.setDate(date);
		}

	    });
	} else {
	    if (isOnKeyTyped()) {
		// lock if the "entity" is not BPW. if "entity" is BPW - it locks inside BPW's "commit" method
		// lock subject bean, even if the setter will not be perfomed (it is more safe)
		entity.lock();
	    }
	    new SwingWorkerCatcher<Result, Void>() {

		private boolean setterPerformed = false;

		@Override
		protected Result tryToDoInBackground() {
		    entity.set(propertyName, date);
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
    }

    /**
     * Returns a modified "newDate" based on previous "oldDate" or default time-portion value.
     *
     * @param newDate
     * @return
     */
    public static Date modifyDateByTheTimePortion(final Date newDate, final Date oldDate, final Long defaultTimePortionMillis) {
	final long newDateTimePortionMillis = timePortionMillis(newDate);
	final long oldDateTimePortionMillis = timePortionMillis(oldDate);

	if (newDate == null || newDateTimePortionMillis != 0){ // newDate remains unchanged if it is null or if it has non-empty time-portion.
	    return newDate;
	} else if (oldDate != null && oldDateTimePortionMillis != 0){ // modify non-null with empty time-portion "new date" by "old date"'s non-empty time-portion:
	    return new Date(newDate.getTime() + oldDateTimePortionMillis);
	} else { // modify non-null with empty time-portion "new date" by default (empty or not) time-portion:
	    return new Date(newDate.getTime() + defaultTimePortionMillis);
	}
    }

    /**
     * returns the milliseconds of the day of the specified Date (HOURS * MILLIS_PER_HOUR + MINUTES * MILLIS_PER_MINUTE + SECONDS * MILLIS_PER_SECOND)
     *
     * @param date
     * @return
     */
    public static long timePortionMillis(final Date date) {
	return (date == null) ? 0 : date.getTime() - DateUtils.startOfDay(date).getTime();
    }

    /**
     * Handles changes in the subject's value.
     */
    private static final class SubjectValueChangeHandler implements Binder.SubjectValueChangeHandler {

	private final BoundedJXDatePicker boundedJXDatePicker;

	protected SubjectValueChangeHandler(final BoundedJXDatePicker boundedJXDatePicker) {
	    this.boundedJXDatePicker = boundedJXDatePicker;
	}

	/**
	 * The subject value has changed. Updates this adapter's selected state to reflect whether the subject holds the selected value or not.
	 *
	 * @param evt
	 *            the property change event fired by the subject
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
	    this.boundedJXDatePicker.updateByActualOrLastIncorrectValue();
	    if (this.boundedJXDatePicker.boundedMetaProperty() != null) {
		this.boundedJXDatePicker.updateToolTip();
	    }

	}
    }

    /**
     * updates the editable state of the component based on the Editable state of the bound Property
     */
    public void updateEditable() {
	final MetaProperty property = boundedMetaProperty();
	if (property != null) {
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    boundedValidationLayer.getView().setEditable(property.isEditable());
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

    @Override
    public final MetaProperty boundedMetaProperty() {
	return Rebinder.getActualEntity(entity).getProperty(propertyName);
    }

    protected BoundedValidationLayer<BoundedJXDatePicker> getBoundedValidationLayer() {
	return boundedValidationLayer;
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
		boundedValidationLayer.getView().setToolTipText(Binder.createToolTipByValueAndMetaProperty(entity, propertyName, boundedMetaProperty(), boundedValidationLayer.getOriginalToolTipText(), true));
	    }
	});
    }

    @Override
    public void updateValidationResult() {
	// implemented in other methods
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		boundedMetaProperty().getChangeSupport().firePropertyChange("validationResults", null, new Result(entity, "here the result updates to goodd!!!!"));
	    }
	});
    }

    public static void main(final String[] args) {
	try {
	    final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mma");
	    System.out.println(dateFormat.parse("21/01/2009 00:00AM"));
	    System.out.println(dateFormat.parse("21/01/2009 00:01AM"));
	    System.out.println(dateFormat.parse("21/01/2009 11:59AM"));
	    System.out.println(dateFormat.parse("21/01/2009 12:00AM"));
	    System.out.println(dateFormat.parse("21/01/2009 12:01AM"));
	    System.out.println(dateFormat.parse("21/01/2009 00:00PM"));
	    System.out.println(dateFormat.parse("21/01/2009 00:01PM"));
	    System.out.println(dateFormat.parse("21/01/2009 11:59PM"));
	    System.out.println(dateFormat.parse("21/01/2009 12:00PM"));
	    System.out.println(dateFormat.parse("21/01/2009 12:01PM"));
	} catch (final ParseException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public IBindingEntity getSubjectBean() {
        return entity;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }
}
