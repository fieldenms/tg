package ua.com.fielden.platform.swing.components.bind.development;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.event.DocumentListener;

import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.swing.components.bind.development.Binder.Rebinder;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.types.Money;

/**
 * This is the value based connector for BigDecimal/Money/Integer spinner. Can use OnKeyTyped binding with direct entity's property or Triggered binding with
 * BufferedPropertyWrapper.
 * 
 * This connector is very similar to FormattedFieldConnector, but it plays commonly with spinner's inner formatted field.
 * 
 * @author Jhou
 * 
 */
public final class SpinnerConnector extends FormattedFieldConnector {
    private final JSpinner spinner;

    /**
     * Initializes all needed listeners, textChangeHandlers, onCommitActions and so on.. All listeners adds to appropriate object, whether it is BufferedPropertyWrapper or direct
     * IBindingEntity
     * 
     * @param entity
     * @param propertyName
     * @param boundedValidationLayer
     * @param actions
     */
    SpinnerConnector(final IBindingEntity entity, final String propertyName, final BoundedValidationLayer<? extends JSpinner> boundedValidationLayer, final IOnCommitAction... actions) {
	// initiate Entity and PropertyName
	super(entity, propertyName, boundedValidationLayer, spinnerEditor(boundedValidationLayer.getView()), actions);

	// initiateEditableComponent
	this.spinner = boundedValidationLayer.getView();
	if (spinner == null) {
	    logger.error("ValidationLayer is null.");
	    throw new NullPointerException("The spinner must not be null.");
	}

	// this is necessary to perform update after "spinner" field initiated. (logic depends on it)
	updateByActualOrLastIncorrectValue();

	boundedValidationLayer.addFocusListener(new FocusAdapter() {
	    @Override
	    public void focusGained(final FocusEvent e) {
		getFormattedField().requestFocusInWindow();
	    }
	});
    }

    @Override
    protected DocumentListener createTextChangeHandler() {
	return new TextChangeHandler() {
	    @Override
	    protected void update() {
		if (getFormattedField().hasFocus()) {
		    try {
			// 1) remove change listener;
			((SpinnerDefaultEditor) spinner.getEditor()).dismiss(spinner);
			// 2) commit edited value only for inner text field
			spinner.commitEdit();

			logger.debug("\t\tCOMMITTED VALUE == [" + getFormattedField().getValue() + "] of type ["
				+ (getFormattedField().getValue() == null ? "null" : getFormattedField().getValue().getClass()) + "]");
			// 3) add listener again:
			((SpinnerDefaultEditor) spinner.getEditor()).addAgain(spinner);
			// 4)
			spinner.commitEdit();

			updateSubject();
		    } catch (final ParseException e) {
			e.printStackTrace();
		    }
		}
	    }
	};
    }

    private static JFormattedTextField spinnerEditor(final JSpinner spinner) {
	final JComponent editor = spinner.getEditor();
	if (editor instanceof JSpinner.DefaultEditor) {
	    return ((JSpinner.DefaultEditor) editor).getTextField();
	} else {
	    throw new RuntimeException("Unexpected editor type: " + spinner.getEditor().getClass() + " isn't a descendant of DefaultEditor.");
	}
    }

    /**
     * Initializes all needed listeners, textChangeHandlers, onCommitActions and so on.. All listeners adds to the specified BufferedPropertyWrapper and other FormattedField
     * related objects (document etc.)
     * 
     * @param bufferedPropertyWrapper
     * @param validationLayer
     */
    SpinnerConnector(final BufferedPropertyWrapper bufferedPropertyWrapper, final BoundedValidationLayer<? extends JSpinner> validationLayer) {
	this(bufferedPropertyWrapper, bufferedPropertyWrapper.getPropertyName(), validationLayer);
    }

    // Synchronization ********************************************************

    /**
     * Converts formatted field's value (that was previously committed) in accordance of property type. Could be used to update subject value or spinner component value etc.
     */
    public static Object convertNumberValueObtainedFromFormattedField(final Object obtainedValue, final Class<?> propertyType) {
	final Object value = obtainedValue;
	if (value == null) {
	    return null;
	}
	final BigDecimal bd = Long.class.equals(value.getClass()) ? new BigDecimal(((Long) value).doubleValue()) : //
	    Integer.class.equals(value.getClass()) ? new BigDecimal(((Integer) value).doubleValue()) : //
		Double.class.equals(value.getClass()) ? new BigDecimal(((Double) value).toString()) : //
		    BigDecimal.class.equals(value.getClass()) ? (BigDecimal) value : //
			Money.class.equals(value.getClass()) ? ((Money) value).getAmount() : //
			    null;
			final boolean isMoney = Money.class.isAssignableFrom(propertyType);
			final boolean isInteger = Integer.class.isAssignableFrom(propertyType);
			final boolean isDouble = Double.class.isAssignableFrom(propertyType);
			final boolean isBigDecimal = BigDecimal.class.isAssignableFrom(propertyType);
			final Object valueToSet = isMoney ? new Money(bd) : //
			    (isInteger ? ((Integer) bd.intValue()) : //
				(isDouble ? (bd.doubleValue()) : (isBigDecimal ? bd : value)));
			return valueToSet;
    }

    @Override
    public void updateByActualOrLastIncorrectValue() {
	if (spinner != null) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		public void run() {
		    final Object value = (boundedMetaProperty() == null || boundedMetaProperty().isValid()) ? entity.get(propertyName)
			    : boundedMetaProperty().getLastInvalidValue();
		    convertAndSetAppropriateValueSilently(value);

		    try {
			spinner.setValue(SpinnerConnector.convertNumberValueObtainedFromFormattedField(value, Rebinder.getPropertyType(entity, propertyName)));
		    } catch (final IllegalArgumentException e) {
			//		    e.printStackTrace();
		    }
		}
	    });
	}
    }

    /**
     * converts and set specified value silently
     * 
     * @param value
     */
    @Override
    protected void convertAndSetAppropriateValueSilently(final Object value) {
	if (value instanceof Date) {
	    logger.error("Subject value type " + value.getClass() + " is not supported by " + getClass().getSimpleName());
	    throw new RuntimeException("Subject value type " + value.getClass() + " is not supported by " + getClass().getSimpleName());
	}
	super.convertAndSetAppropriateValueSilently(value);
    }

}
