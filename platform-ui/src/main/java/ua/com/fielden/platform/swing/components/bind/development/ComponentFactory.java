package ua.com.fielden.platform.swing.components.bind.development;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import javax.swing.text.NumberFormatter;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.components.EditableCheckBox;
import ua.com.fielden.platform.swing.components.EditableSpinner;
import ua.com.fielden.platform.swing.components.bind.development.Binder.Rebinder;
import ua.com.fielden.platform.swing.components.bind.development.FilterFactory.AbstractDocumentFilter;
import ua.com.fielden.platform.swing.components.bind.formatters.DateFormatter1;
import ua.com.fielden.platform.swing.components.bind.formatters.EmptyDateFormatterWithIndependentTimePortion;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayerWithEntityLocator;
import ua.com.fielden.platform.swing.components.smart.autocompleter.renderer.development.TwoPropertyListCellRenderer;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;
import ua.com.fielden.platform.swing.components.textfield.UpperCaseTextField;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.report.centre.configuration.EntityLocatorDialog;
import ua.com.fielden.platform.swing.review.report.centre.configuration.LocatorConfigurationModel;
import ua.com.fielden.platform.utils.EntityUtils.ShowingStrategy;
import ua.com.fielden.platform.utils.Pair;

import com.jgoodies.binding.formatter.EmptyNumberFormatter;
import com.jgoodies.binding.value.ValueModel;

/**
 * The factory to create components from entity's properties or from propertyWrappers
 *
 * @author TG Team
 *
 */
public class ComponentFactory {

    private final static Logger logger = Logger.getLogger(ComponentFactory.class);

    protected ComponentFactory() {
	// Reduce the visibility of the default constructor.
    }

    // ============================ creation methods without binding logic ================================
    // ====================================================================================================

    /**
     * creates simple {@link JTextField} with the specified DocumentFilter. Use FilterFactory
     *
     * @param abstractDocumentFilter
     * @param upperCase
     *            - determines whether {@link UpperCaseTextField} or simple {@link JTextField} should be created
     * @return
     */
    private static JTextField createTextField(final AbstractDocumentFilter abstractDocumentFilter, final EditorCase editorCase) {
	final JTextField textField = !EditorCase.UPPER_CASE.equals(editorCase) ? new JTextField() {
	    private static final long serialVersionUID = -54713246930625222L;

	    @Override
	    protected Document createDefaultModel() {
		final PlainDocument bdd = new PlainDocument();
		bdd.setDocumentFilter(abstractDocumentFilter);
		return bdd;
	    }
	} : new UpperCaseTextField();

	enhanceTextFieldByDeprecatingPreferredSize(textField);
	return textField;
    }

    public static JTextArea createTextArea(final AbstractDocumentFilter abstractDocumentFilter) {
	final JTextArea textArea = new JTextArea() {
	    private static final long serialVersionUID = -54713246930625222L;

	    @Override
	    protected Document createDefaultModel() {
		final PlainDocument bdd = new PlainDocument();
		bdd.setDocumentFilter(abstractDocumentFilter);
		return bdd;
	    }
	};
	return textArea;
    }

    /**
     * This method makes text field preferred size independent from text changes.
     * <p>
     * Please look at note taken from http://java.sun.com/docs/books/tutorial/uiswing/components/textfield.html :
     * <p>
     * Note: We encourage you to specify the number of columns for each text field. If you do not specify the number of columns or a preferred size, then the field's preferred size
     * changes whenever the text changes, which can result in unwanted layout updates.
     * <p>
     *
     * @param textField
     */
    private static void enhanceTextFieldByDeprecatingPreferredSize(final JTextField textField) {
	textField.setColumns(1);
    }

    /**
     * This formatted field manages its focus gaining in the way of
     * <p>
     * 1. it selects all content after focus gains.
     * <p>
     * 2. it prevents buffering when focus gains (by temporary removing textChangeHandler).
     * <p>
     * 3. Added external implementation of method <code>composedTextExists</code> from JTextComponent class.
     *
     * @author Jhou
     *
     */
    public static class SpecialFormattedField extends JFormattedTextField implements UIResource {
	private static final long serialVersionUID = 1L;
	private DocumentListener textChangeHandler = null;

	private Position composedTextStartCopy;

	public SpecialFormattedField(final AbstractFormatterFactory aff) {
	    super(aff);
	    enhanceTextFieldByDeprecatingPreferredSize(this);

	    addPropertyChangeListener("composedTextStart", new PropertyChangeListener() {
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
		    composedTextStartCopy = (Position) evt.getNewValue();
		}
	    });
	}

	public boolean composedTextExists1() {
	    return (composedTextStartCopy != null);
	}

	@Override
	protected void processFocusEvent(final FocusEvent e) {
	    if (e.getID() == FocusEvent.FOCUS_GAINED) {
		// This action prevents initial buffering when the focus gains!
		getDocument().removeDocumentListener(textChangeHandler);
	    }

	    // This action proceeds all formatting and other inner stuff.
	    super.processFocusEvent(e);

	    if (e.getID() == FocusEvent.FOCUS_GAINED) {
		selectAll();
		// As soon as all of the formatting etc. stuff will be be performed, the documentListener should be added again!
		getDocument().addDocumentListener(textChangeHandler);
	    }
	}

	void setTextChangeHandler(final DocumentListener textChangeHandler) {
	    if (this.textChangeHandler != null) {
		throw new RuntimeException("The textChangeHandler can not be setted more than once.");
	    }
	    this.textChangeHandler = textChangeHandler;
	}

    }

    /**
     * Creates formatter factory for number formatted text field based on provided display/edit number formats.
     *
     * @param displayNumberFormat
     * @param editNumberFormat
     * @return
     */
    public static AbstractFormatterFactory createNumberFormatterFactory(final NumberFormat displayNumberFormat, final NumberFormat editNumberFormat) {
	final NumberFormatter displayNumberFormatter = new EmptyNumberFormatter(displayNumberFormat, null);
	displayNumberFormatter.setAllowsInvalid(false);
	final NumberFormatter editNumberFormatter = new EmptyNumberFormatter(editNumberFormat, null);
	editNumberFormatter.setAllowsInvalid(true); // decided to allow user to type invalid (from formatter point of view) text. For e.g. : "-" could be typed and continued by "-3" and "-3." and "-3.5" and so on. -> Flexibility increased, typing control decreased.
	return new DefaultFormatterFactory(null, displayNumberFormatter, editNumberFormatter, null);
    }

    /**
     * Creates formatter factory for date formatted text field based on provided display/edit date formats.
     *
     * @param displayDateFormat
     * @param editDateFormat
     * @return
     */
    public static AbstractFormatterFactory createDateFormatterFactory(final DateFormat displayDateFormat, final DateFormat editDateFormat) {
	final DateFormatter1 displayDateFormatter = new EmptyDateFormatterWithIndependentTimePortion(displayDateFormat, null);
	displayDateFormatter.setAllowsInvalid(true);
	final DateFormatter1 editDateFormatter = new EmptyDateFormatterWithIndependentTimePortion(editDateFormat, null);
	editDateFormatter.setAllowsInvalid(true);
	return new DefaultFormatterFactory(null, displayDateFormatter, editDateFormatter, null);
    }

    /**
     * Creates numberFormattedField using specified formatters(edit/display).
     *
     * @param displayNumberFormat
     * @param editNumberFormat
     * @return
     */
    private static JFormattedTextField createNumberFormattedField(final NumberFormat displayNumberFormat, final NumberFormat editNumberFormat) {
	final JFormattedTextField numberTextField = new SpecialFormattedField(createNumberFormatterFactory(displayNumberFormat, editNumberFormat));
	return numberTextField;
    }

    /**
     * Creates formatted field with Integer edit/display formatters.
     *
     * @return
     */
    private static JFormattedTextField createFormattedFieldIntegerType() {
	final Pair<NumberFormat, NumberFormat> formats = createNumberFormats(false);
	return createNumberFormattedField(formats.getKey(), formats.getValue());
    }

    /**
     * Creates a pair of display/edit formats for number types.
     *
     * @param decimal
     *            - true for decimal types, false for integer types.
     * @return
     */
    public static Pair<NumberFormat, NumberFormat> createNumberFormats(final boolean decimal) {
	final NumberFormat editIntFormat = new DecimalFormat("##########");
	editIntFormat.setMaximumIntegerDigits(10);
	editIntFormat.setParseIntegerOnly(true);
	return decimal ? //
		(new Pair<NumberFormat, NumberFormat>(new DecimalFormat("#,##0.00"), new DecimalFormat("0.0###################")))
		: //
		    (new Pair<NumberFormat, NumberFormat>(NumberFormat.getIntegerInstance(), editIntFormat));
    }

    /**
     * Creates formatted field with BigDecimal/Money edit/display formatters.
     *
     * @return
     */
    private static JFormattedTextField createFormattedFieldForDecimalTypes() {
	final Pair<NumberFormat, NumberFormat> formats = createNumberFormats(true);
	return createNumberFormattedField(formats.getKey(), formats.getValue()); // "0.0###################;-0.0###################"
    }

    /**
     * The Label which text cannot be changed directly (only from binding)
     *
     * @author Jhou
     *
     */
    public static class ReadOnlyLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	public void setToDefaults() {
	    setForeground((Color) UIManager.get("Label.foreground"));
	    setBackground((Color) UIManager.get("Label.background"));
	    setBorder((Border) UIManager.get("Label.border"));
	}

	/**
	 * the method cannot be used, coz it's a read-only JLabel
	 */
	@Override
	public final void setText(final String text) {
	    // this method does nothing
	}

	/**
	 * this text setter is for internal purposes only
	 *
	 * @param text
	 */
	final void setTextFromBinding(final String text) {
	    super.setText(text);
	}
    }

    /**
     * Creates not assigned to any property Autocompleter.
     *
     * @param <T>
     * @param acceptableValues
     *            -
     * @param lookupClass
     * @param expression
     * @param secExpression
     * @param valueSeparator
     * @return
     */
    private static <T> AutocompleterTextFieldLayer<T> createUnBoundAutocompleter(final String caption, final Class<T> lookupClass, final String expression, final String secExpression, final IValueMatcher<T> valueMatcher, final String valueSeparator) {
	final TwoPropertyListCellRenderer<T> cellRenderer = new TwoPropertyListCellRenderer<T>(expression, secExpression);
	final JTextField textField = new UpperCaseTextField();
	enhanceTextFieldByDeprecatingPreferredSize(textField);
	final AutocompleterTextFieldLayer<T> autocompleter = new AutocompleterTextFieldLayer<T>(textField, valueMatcher, lookupClass, expression, cellRenderer, caption, valueSeparator);
	cellRenderer.setAuto(autocompleter.getAutocompleter());
	return autocompleter;
    }

    //    /**
    //     * Creates not assigned to any property Autocompleter. This autocompleter is ctrl+Click action sensitive.
    //     *
    //     * @param lookupClass
    //     * @param expression
    //     * @param secExpression
    //     * @param valueSeparator
    //     * @param entityFactory
    //     *            TODO
    //     * @param acceptableValues
    //     *            -
    //     *
    //     * @param <T>
    //     * @return
    //     */
    //    private static <T> AutocompleterTextFieldLayer<T> createUnBoundOptionAutocompleter(final IBindingEntity entity, final String caption, final Class<T> lookupClass, final String expression, final String secExpression, final IValueMatcher<T> valueMatcher, final IEntityMasterManager entityMasterFactory, final String valueSeparator, final EntityFactory entityFactory, final IValueMatcherFactory vmf, final IDaoFactory daoFactory, final ILocatorConfigurationController locatorController, final ILocatorConfigurationRetriever locatorRetriever, final String propertyName) {
    //	final TwoPropertyListCellRenderer<T> cellRenderer = new TwoPropertyListCellRenderer<T>(expression, secExpression);
    //	final JTextField textField = new UpperCaseTextField();
    //	enhanceTextFieldByDeprecatingPreferredSize(textField);
    //	final LocatorManager locatorManager = new LocatorManager(locatorController, locatorRetriever, lookupClass, entity.getClass(), propertyName);
    //	final OptionAutocompleterTextFieldLayer<T> autocompleter = new OptionAutocompleterTextFieldLayer<T>(entity, propertyName, textField, entityFactory, valueMatcher, entityMasterFactory, expression, cellRenderer, caption, valueSeparator, vmf, daoFactory, locatorManager);
    //	cellRenderer.setAuto(autocompleter.getAutocompleter());
    //	return autocompleter;
    //    }


    /**
     * Creates not assigned to any property Autocompleter with entity locator.
     *
     * @param locatorManager
     * @param entityFactory
     * @param criteriaGenerator
     * @param valueMatcher
     * @param entityType
     * @param rootType
     * @param propertyName
     * @param expression
     * @param secExpression
     * @param caption
     * @param valueSeparator
     * @return
     */
    @SuppressWarnings("rawtypes")
    private static <VT extends AbstractEntity<?>, RT extends AbstractEntity<?>> AutocompleterTextFieldLayerWithEntityLocator<VT> createUnBoundAutocompleterWithEntityLocator(//
	    final LocatorConfigurationModel<VT, RT> locatorConfigurationModel,//
	    final IValueMatcher<VT> valueMatcher,//
	    final Class<VT> entityType,//
	    final String propertyName,//
	    final String expression,//
	    final String secExpression,//
	    final String caption,//
	    final String valueSeparator){
	final TwoPropertyListCellRenderer<VT> cellRenderer = new TwoPropertyListCellRenderer<VT>(expression, secExpression);
	final JTextField textField = new UpperCaseTextField();
	enhanceTextFieldByDeprecatingPreferredSize(textField);
	final EntityLocatorDialog<VT, RT> entityLocatorDialog = new EntityLocatorDialog<VT, RT>(locatorConfigurationModel, !StringUtils.isEmpty(valueSeparator));
	final AutocompleterTextFieldLayerWithEntityLocator<VT> autocompleter = new AutocompleterTextFieldLayerWithEntityLocator<VT>(//
		entityLocatorDialog,//
		entityType,//
		textField,//
		valueMatcher,//
		expression,//
		cellRenderer,//
		caption,//
		valueSeparator);
	cellRenderer.setAuto(autocompleter.getAutocompleter());
	return autocompleter;
    }

    /**
     * Creates unBounded ValidationLayer wrapped around component. "onCommitActionable" and "trigger" property are not initialized!
     *
     * @param <T>
     * @param component
     * @param originalToolTipText
     * @return
     */
    static <T extends JComponent> BoundedValidationLayer createBoundedValidationLayer(final T component, final String originalToolTipText) {
	return new BoundedValidationLayer<T>(component, originalToolTipText);
    }

    static <T extends JComponent> BoundedValidationLayer createBoundedValidationLayer(final T component, final String originalToolTipText, final boolean selectAfterFocusGained) {
	return new BoundedValidationLayer<T>(component, originalToolTipText, selectAfterFocusGained);
    }

    // ================================== creation methods with binding logic! ======================================
    // ==================================================================================================================

    /**
     *
     */
    public static <T extends AbstractEntity<?>> BoundedValidationLayer<EntityGridInspector<T>> createEGI(final IBindingEntity entity, final String propertyName, final PropertyTableModelBuilder<T> propertyTableModelBuilder, final IOnCommitAction... actions) {
	if (entity.getProperty(propertyName) == null) {
	    throw new RuntimeException("EGI can not be bounded to custom IBindingEntity implementation.");
	}
	if (!entity.getProperty(propertyName).isCollectional()) {
	    throw new IllegalArgumentException("Only collectional property can be bounded to EGI.");
	}
	// not bounded EGI shows empty list :
	final EntityGridInspector<T> entityGridInspector = new EntityGridInspector<T>(propertyTableModelBuilder.build(new ArrayList<T>()));
	final BoundedValidationLayer<EntityGridInspector<T>> boundedValidationLayer = createBoundedValidationLayer(entityGridInspector, "EGI tooltip");
	Binder.bindCollectionalPropertyWithEGI(boundedValidationLayer, entity, propertyName, actions);
	return boundedValidationLayer;
    }

    /**
     * creates textField and binds it with Integer property. if you create onFocusLost model binding, you can manually commit()/flush() it from BoundedValidationLayer methods
     * commit()/flush().
     *
     * @param entity
     * @param propertyName
     * @param commitOnFocusLost
     *            - true to commit on focus lost,false - to commit on key typed!
     * @return
     */
    public static BoundedValidationLayer<JFormattedTextField> createIntegerTextField(final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final String originalToolTipText, final IOnCommitAction... actions) {
	final JFormattedTextField integerTextField = createFormattedFieldIntegerType();
	final BoundedValidationLayer<JFormattedTextField> boundedValidationLayer = createBoundedValidationLayer(integerTextField, originalToolTipText);
	Binder.bindBigDecimalOrMoneyOrIntegerOrDouble(boundedValidationLayer, entity, propertyName, commitOnFocusLost, actions);
	return boundedValidationLayer;
    }

    /**
     * Creates JSpinner and binds it with Integer property. if you create onFocusLost model binding, you can manually commit()/flush() it from BoundedValidationLayer methods
     * commit()/flush().
     *
     * @param entity
     * @param propertyName
     * @param decimal
     *            - indicates if should be used decimal binding (Money, BigDecimal, Double properties) or integer (when "decimal" == true).
     * @param commitOnFocusLost
     *            - true to commit on focus lost,false - to commit on key typed!
     * @return
     */
    public static <T extends Number> BoundedValidationLayer<JSpinner> createNumberSpinner(final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final String originalToolTipText, final T step, final IOnCommitAction... actions) {
	if (step == null) {
	    throw new RuntimeException("Please specify the step to increase/decrease values in bounded spinner.");
	}
	final boolean decimal = !(step instanceof Integer) && !(step instanceof Long);
	final Pair<Comparable, Comparable> minMax = Reflector.extractValidationLimits((AbstractEntity) entity, propertyName);
	if (minMax == null) {
	    logger.error("Min max values could not be extracted from entity [" + entity + "] of type [" + ((AbstractEntity) entity).getType() + "]");
	    throw new RuntimeException("Min max values could not be extracted from entity [" + entity + "] of type [" + ((AbstractEntity) entity).getType() + "]");
	}
	final Integer min = (Integer) minMax.getKey(), max = (Integer) minMax.getValue();
	final Comparable<?> actualMin, actualMax;
	final Number actualValue;
	if (decimal) {
	    final double dMin = new BigDecimal(min).doubleValue();
	    final double dMax = new BigDecimal(max).doubleValue();
	    final double dVal = 0.0d;

	    actualMin = dMin;
	    actualMax = dMax;
	    actualValue = (dVal < dMin) ? (dMin) : ((dVal > dMax) ? dMax : dVal); // (Double) actualMin; // min + max / 2.0; // (Double) Rebinder.getActualEntity(entity).get(propertyName);
	} else {
	    final Integer iMin = min;
	    final Integer iMax = max;
	    final Integer iVal = new Integer(0);

	    actualMin = iMin;
	    actualMax = iMax;
	    actualValue = (iVal < iMin) ? (iMin) : ((iVal > iMax) ? iMax : iVal); // (Integer) actualMin; // min + max / 2; // (Integer) Rebinder.getActualEntity(entity).get(propertyName);
	}
	logger.debug("Spinner model values are actualValue = " + actualValue + ", actualMin = " + actualMin + ", actualMax = " + actualMax);
	final JSpinner numberSpinner = new EditableSpinner(new SpinnerNumberModel(actualValue, actualMin, actualMax, step), actualValue);
	numberSpinner.setEditor(new SpinnerDefaultEditor(numberSpinner, decimal, Rebinder.getPropertyType(entity, propertyName)));

	final BoundedValidationLayer<JSpinner> boundedValidationLayer = createBoundedValidationLayer(numberSpinner, originalToolTipText);
	Binder.bindBigDecimalOrMoneyOrIntegerOrDoubleWithSpinner(boundedValidationLayer, entity, propertyName, commitOnFocusLost, actions);
	return boundedValidationLayer;
    }

    /**
     * creates textField and binds it with Integer propertyWrapper. Use for delaying commits.
     *
     * @param bufferedPropertyWrapper
     * @return
     */
    public static BoundedValidationLayer<JFormattedTextField> createTriggeredIntegerTextField(final IBindingEntity entity, final String propertyName, final ValueModel triggerChannel, final String originalToolTipText, final IOnCommitAction... actions) {
	final JFormattedTextField integerTextField = createFormattedFieldIntegerType();
	final BoundedValidationLayer<JFormattedTextField> boundedValidationLayer = createBoundedValidationLayer(integerTextField, originalToolTipText);
	Binder.bindTriggeredBigDecimalOrMoneyOrIntegerOrDouble(boundedValidationLayer, entity, propertyName, triggerChannel, actions);
	return boundedValidationLayer;
    }

    /**
     * Creates and binds FormattedField with BigDecimal/Money property. If you create onFocusLost model binding, you can manually commit()/flush() it from BoundedValidationLayer
     * methods commit()/flush().
     *
     * @param entity
     * @param propertyName
     * @param commitOnFocusLost
     *            - true to commit on focus lost, false - to commit on key typed!!
     * @param originalToolTipText
     * @param actions
     * @return
     */
    public static BoundedValidationLayer<JFormattedTextField> createBigDecimalOrMoneyOrDoubleField(final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final String originalToolTipText, final IOnCommitAction... actions) {
	final JFormattedTextField textField = createFormattedFieldForDecimalTypes();
	final BoundedValidationLayer<JFormattedTextField> boundedValidationLayer = createBoundedValidationLayer(textField, originalToolTipText);
	Binder.bindBigDecimalOrMoneyOrIntegerOrDouble(boundedValidationLayer, entity, propertyName, commitOnFocusLost, actions);
	return boundedValidationLayer;
    }

    /**
     * Creates the formatted field for BigDecimal/Money type with the commit/flush actions controlled by the specified Trigger.
     *
     * @param entity
     * @param propertyName
     * @param triggerChannel
     *            - trigger to commit/flush buffered component value
     * @param originalToolTipText
     * @param actions
     * @return
     */
    public static BoundedValidationLayer<JFormattedTextField> createTriggeredBigDecimalOrMoneyOrDoubleField(final IBindingEntity entity, final String propertyName, final ValueModel triggerChannel, final String originalToolTipText, final IOnCommitAction... actions) {
	final JFormattedTextField bigDecimalTextField = createFormattedFieldForDecimalTypes();
	final BoundedValidationLayer<JFormattedTextField> boundedValidationLayer = createBoundedValidationLayer(bigDecimalTextField, originalToolTipText);
	Binder.bindTriggeredBigDecimalOrMoneyOrIntegerOrDouble(boundedValidationLayer, entity, propertyName, triggerChannel, actions);
	return boundedValidationLayer;
    }

    /**
     * creates textField and binds it with String property. If you create onFocusLost model binding, you can manually commit()/flush() it from BoundedValidationLayer methods
     * commit()/flush().
     *
     * @param entity
     * @param propertyName
     * @param commitOnFocusLost
     *            - true to commit on focus lost, false - to commit on key typed!!
     * @param originalToolTipText
     * @param upperCase
     *            - determines whether {@link UpperCaseTextField} or simple {@link JTextField} can be created
     * @param actions
     * @return
     */
    public static BoundedValidationLayer<JTextField> createStringTextField(final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final String originalToolTipText, final EditorCase editorCase, final IOnCommitAction... actions) {
	final JTextField textField = createTextField(FilterFactory.createStringDocumentFilter(), editorCase);
	final BoundedValidationLayer<JTextField> boundedValidationLayer = createBoundedValidationLayer(textField, originalToolTipText);
	Binder.bindStringTextAreaOrField(boundedValidationLayer, entity, propertyName, commitOnFocusLost, actions);
	return boundedValidationLayer;
    }

    /**
     * Creates password field and binds it to string property.
     *
     * @param entity
     * @param propertyName
     * @param commitOnFocusLost
     * @param originalToolTipText
     * @param actions
     * @return
     */
    public static BoundedValidationLayer<JPasswordField> createPasswordField(final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final String originalToolTipText, final char echoChar, final IOnCommitAction... actions) {
	final JPasswordField passwordField = new JPasswordField();
	enhanceTextFieldByDeprecatingPreferredSize(passwordField);
	passwordField.setEchoChar(echoChar);
	final BoundedValidationLayer<JPasswordField> boundedValidationLayer = createBoundedValidationLayer(passwordField, originalToolTipText);
	Binder.bindStringTextAreaOrField(boundedValidationLayer, entity, propertyName, commitOnFocusLost, actions);
	return boundedValidationLayer;
    }

    /**
     * creates READ-ONLY JLabel and binds it with any supported property, e.g. autocompleter entities, strings, etc. You cannot use setText() method of the inner JLabel
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static BoundedValidationLayer<ReadOnlyLabel> createLabel(final IBindingEntity entity, final String propertyName, final String originalToolTipText, final ShowingStrategy showingStrategy) {
	final ReadOnlyLabel label = new ReadOnlyLabel();
	final BoundedValidationLayer<ReadOnlyLabel> validationLayer = createBoundedValidationLayer(label, originalToolTipText);
	Binder.bindLabel(validationLayer, entity, propertyName, showingStrategy);
	return validationLayer;
    }

    /**
     * creates READ-ONLY JLabel and binds it with any supported property, e.g. autocompleter entities, strings, etc. You cannot use setText() method of the inner JLabel
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static BoundedValidationLayer<ReadOnlyLabel> createLabel(final IBindingEntity entity, final String propertyName, final String originalToolTipText) {
	return createLabel(entity, propertyName, originalToolTipText, ShowingStrategy.KEY_ONLY); // ShowingStrategy.KEY_AND_DESC
    }

    /**
     * creates textField and binds it with String propertyWrapper. Use for delaying commits.
     *
     * @param bufferedPropertyWrapper
     * @return
     */
    public static BoundedValidationLayer<JTextField> createTriggeredStringTextField(final IBindingEntity entity, final String propertyName, final ValueModel triggerChannel, final boolean selectAfterFocusGained,final String originalToolTipText, final IOnCommitAction... actions) {
	final JTextField textField = createTextField(FilterFactory.createStringDocumentFilter(), EditorCase.MIXED_CASE);
	final BoundedValidationLayer<JTextField> boundedValidationLayer = createBoundedValidationLayer(textField, originalToolTipText, selectAfterFocusGained);
	Binder.bindTriggeredStringTextAreaOrField(boundedValidationLayer, entity, propertyName, triggerChannel, actions);
	return boundedValidationLayer;
    }

    /**
     * creates textArea and binds it with String property
     *
     * @param entity
     * @param propertyName
     * @param commitOnFocusLost
     *            - true to commit on focus lost, false - to commit on key typed!!
     * @return
     */
    public static BoundedValidationLayer<JTextArea> createStringTextArea(final IBindingEntity entity, final String propertyName, final boolean commitOnFocusLost, final boolean selectAfterFocusGained, final String toolTip, final IOnCommitAction... actions) {
	final JTextArea textArea = createTextArea(FilterFactory.createStringWithEnterDocumentFilter());
	final BoundedValidationLayer<JTextArea> boundedValidationLayer = createBoundedValidationLayer(textArea, toolTip, selectAfterFocusGained);
	Binder.bindStringTextAreaOrField(boundedValidationLayer, entity, propertyName, commitOnFocusLost, actions);
	return boundedValidationLayer;
    }

    /**
     * creates textArea and binds it with String propertyWrapper. Use for delaying commits.
     *
     * @return
     */
    public static BoundedValidationLayer<JTextArea> createTriggeredStringTextArea(final IBindingEntity entity, final String propertyName, final ValueModel triggerChannel, final String originalToolTipText, final IOnCommitAction... actions) {
	final JTextArea textArea = createTextArea(FilterFactory.createStringWithEnterDocumentFilter());
	final BoundedValidationLayer<JTextArea> boundedValidationLayer = createBoundedValidationLayer(textArea, originalToolTipText);
	Binder.bindTriggeredStringTextAreaOrField(boundedValidationLayer, entity, propertyName, triggerChannel, actions);
	return boundedValidationLayer;
    }

    /**
     * creates checkBox and binds it with Boolean property
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static BoundedValidationLayer<JCheckBox> createReadOnlyCheckBox(final IBindingEntity entity, final String propertyName, final String label, final String toolTip, final IOnCommitAction... actions) {
	final JCheckBox booleanCheckBox = new EditableCheckBox(label);
	final BoundedValidationLayer<JCheckBox> boundedValidationLayer = createBoundedValidationLayer(booleanCheckBox, toolTip);
	Binder.bindCheckBox(boundedValidationLayer, entity, propertyName, true, actions);
	return boundedValidationLayer;
    }

    /**
     * creates checkBox and binds it with Boolean property
     *
     * @param entity
     * @param propertyName
     * @return
     */
    public static BoundedValidationLayer<JCheckBox> createCheckBox(final IBindingEntity entity, final String propertyName, final String label, final String toolTip, final IOnCommitAction... actions) {
	final JCheckBox booleanCheckBox = new EditableCheckBox(label);
	final BoundedValidationLayer<JCheckBox> validationLayer = createBoundedValidationLayer(booleanCheckBox, toolTip);
	Binder.bindCheckBox(validationLayer, entity, propertyName, false, actions);
	return validationLayer;
    }

    /**
     * creates checkBox and binds it with Boolean propertyWrapper. Use for delaying commits.
     *
     * @param bufferedPropertyWrapper
     * @return
     */
    public static BoundedValidationLayer<JCheckBox> createTriggeredCheckBox(final IBindingEntity entity, final String propertyName, final String label, final String toolTip, final ValueModel triggerChannel, final IOnCommitAction... actions) {
	final JCheckBox booleanCheckBox = new EditableCheckBox(label);
	final BoundedValidationLayer<JCheckBox> validationLayer = createBoundedValidationLayer(booleanCheckBox, toolTip);
	Binder.bindTriggeredCheckBox(validationLayer, false, entity, propertyName, triggerChannel, actions);
	return validationLayer;
    }

    /**
     * creates radioButton and binds it with enum'like property
     *
     * @param entity
     * @param propertyName
     * @param choice
     * @return
     */
    public static BoundedValidationLayer<JRadioButton> createRadioButton(final IBindingEntity entity, final String propertyName, final Object choice, final String title, final String originalToolTipText, final IOnCommitAction... actions) {
	final JRadioButton radioButton = new JRadioButton(title);
	final BoundedValidationLayer<JRadioButton> validationLayer = createBoundedValidationLayer(radioButton, originalToolTipText);
	Binder.bindRadioButton(validationLayer, entity, propertyName, choice, actions);
	return validationLayer;
    }

    /**
     * This method should be used to create propertyWrapper for Enum property. see createTriggeredRadioButton() method!
     *
     * @param entity
     * @param propertyName
     * @param triggerChannel
     * @param actions
     * @return
     */
    public static BufferedPropertyWrapper createPropertyWrapper(final IBindingEntity entity, final String propertyName, final ValueModel triggerChannel, final IOnCommitAction... actions) {
	return new BufferedPropertyWrapper(entity, propertyName, triggerChannel, actions);
    }

    /**
     * creates radioButton and binds it with enum'like propertyWrapper. Use for delaying commits.
     *
     * @param bufferedPropertyWrapper
     * @param choice
     *            - choice
     * @return
     */
    public static BoundedValidationLayer<JRadioButton> createTriggeredRadioButton(final BufferedPropertyWrapper bufferedPropertyWrapper, final Object choice, final String originalToolTipText) {
	final JRadioButton radioButton = new JRadioButton(choice.toString());
	final BoundedValidationLayer<JRadioButton> validationLayer = createBoundedValidationLayer(radioButton, originalToolTipText);
	Binder.bindTriggeredRadioButton(validationLayer, bufferedPropertyWrapper, choice);
	return validationLayer;
    }

    /**
     * Creates not assigned to any property Autocompleter and binds it with property.
     * <p>
     * You can manually commit()/flush() returned BoundedValidationLayer. This is useful behaviour when need to commit autocompleter contents not on focusLost,
     * <p>
     * Note : internally used OnFocusLostPropertyWrapper to get committing on focus lost event
     *
     * @param <T>
     * @param entity
     * @param propertyName
     * @param acceptableValues
     * @param lookupClass
     * @param expression
     * @param secExpression
     * @param valueSeparator
     * @param manuallyComittable
     *            - if true -> you can use commit()/flush() methods in {@link BoundedValidationLayer}. else - these methods will throw exceptions.
     * @return
     */
    public static BoundedValidationLayer<AutocompleterTextFieldLayer> createOnFocusLostAutocompleter(final IBindingEntity entity, final String propertyName, final String caption, final Class lookupClass, final String expression, final String secExpression, final String valueSeparator, final IValueMatcher valueMatcher, final String originalToolTipText, final boolean stringBinding, final IOnCommitAction... actions) {
	final AutocompleterTextFieldLayer autocompleter = createUnBoundAutocompleter(caption, lookupClass, expression, secExpression, valueMatcher, valueSeparator);
	final BoundedValidationLayer<AutocompleterTextFieldLayer> autocompleterValidationLayer = createBoundedValidationLayer(autocompleter, originalToolTipText);
	Binder.bindOnFocusLostAutocompleter(autocompleterValidationLayer, entity, propertyName, stringBinding, originalToolTipText, actions);
	if (!autocompleterValidationLayer.canCommit()) {
	    throw new RuntimeException("boundedValiLayer cannot commit!!! trigger == null!!! ");
	}
	return autocompleterValidationLayer;
    }

    //    /**
    //     * Creates not assigned to any property ctrl+click action sensitive Autocompleter and binds it with property.
    //     * <p>
    //     * You can manually commit()/flush() returned BoundedValidationLayer. This is useful behaviour when need to commit autocompleter contents not on focusLost,
    //     * <p>
    //     * Note : internally used OnFocusLostPropertyWrapper to get committing on focus lost event
    //     *
    //     * @param <T>
    //     * @param entity
    //     * @param propertyName
    //     * @param lookupClass
    //     * @param expression
    //     * @param secExpression
    //     * @param valueSeparator
    //     * @param entityFactory
    //     *            TODO
    //     * @param acceptableValues
    //     * @param manuallyComittable
    //     *            - if true -> you can use commit()/flush() methods in {@link BoundedValidationLayer}. else - these methods will throw exceptions.
    //     * @return
    //     */
    //    public static BoundedValidationLayer<AutocompleterTextFieldLayer> createOnFocusLostOptionAutocompleter(final IBindingEntity entity, final String propertyName, final String caption, final Class lookupClass, final String expression, final String secExpression, final String valueSeparator, final IValueMatcher valueMatcher, final IEntityMasterManager entityMasterFactory, final String originalToolTipText, final boolean stringBinding, final EntityFactory entityFactory, final IValueMatcherFactory vmf, final IDaoFactory daoFactory, final ILocatorConfigurationController locatorController, final ILocatorConfigurationRetriever locatorRetriever, final IOnCommitAction... actions) {
    //	final AutocompleterTextFieldLayer<T> autocompleter = createUnBoundOptionAutocompleter(entity, caption, lookupClass, expression, secExpression, valueMatcher, entityMasterFactory, valueSeparator, entityFactory, vmf, daoFactory, locatorController, locatorRetriever, propertyName);
    //	final BoundedValidationLayer<AutocompleterTextFieldLayer> autocompleterValidationLayer = createBoundedValidationLayer(autocompleter, originalToolTipText);
    //	Binder.bindOnFocusLostAutocompleter(autocompleterValidationLayer, entity, propertyName, stringBinding, originalToolTipText, actions);
    //	if (!autocompleterValidationLayer.canCommit()) {
    //	    throw new RuntimeException("boundedValiLayer cannot commit!!! trigger == null!!! ");
    //	}
    //	return autocompleterValidationLayer;
    //    }

    /**
     * Creates not assigned to any property Autocompleter with entity locator and binds it with property.
     * <p>
     * You can manually commit()/flush() returned BoundedValidationLayer. This is useful behaviour when need to commit autocompleter contents not on focusLost,
     * <p>
     * Note : internally used OnFocusLostPropertyWrapper to get committing on focus lost event
     *
     * @param entity
     * @param propertyName
     * @param entityType
     * @param rootType
     * @param locatorManager
     * @param entityFactory
     * @param criteriaGenerator
     * @param valueMatcher
     * @param expression
     * @param secExpression
     * @param caption
     * @param valueSeparator
     * @param originalToolTipText
     * @param stringBinding
     * @param actions
     * @return
     */
    public static <VT extends AbstractEntity<?>, RT extends AbstractEntity<?>> BoundedValidationLayer<AutocompleterTextFieldLayerWithEntityLocator<VT>> createOnFocusLostAutocompleterWithEntityLocator(//
	    //Autocomplter related parameters
	    final IBindingEntity entity,//
	    final String propertyName,//
	    final LocatorConfigurationModel<VT, RT> locatorConfigurationModel,//
	    final Class<VT> entityType,//
	    final IValueMatcher<VT> valueMatcher, //
	    final String expression,//
	    final String secExpression,//
	    final String caption,//
	    final String valueSeparator,//
	    //Binder related properties
	    final String originalToolTipText,//
	    final boolean stringBinding,//
	    final IOnCommitAction... actions){
	final AutocompleterTextFieldLayerWithEntityLocator<VT> autocompleter = createUnBoundAutocompleterWithEntityLocator(locatorConfigurationModel,//
		valueMatcher,//
		entityType,//
		propertyName,//
		expression,//
		secExpression,//
		caption,//
		valueSeparator);
	final BoundedValidationLayer<AutocompleterTextFieldLayerWithEntityLocator<VT>> autocompleterValidationLayer = createBoundedValidationLayer(autocompleter, originalToolTipText);
	Binder.bindOnFocusLostAutocompleter(autocompleterValidationLayer, entity, propertyName, stringBinding, originalToolTipText, actions);
	if (!autocompleterValidationLayer.canCommit()) {
	    throw new RuntimeException("boundedValiLayer cannot commit!!! trigger == null!!! ");
	}
	return autocompleterValidationLayer;
    }

    /**
     * Creates not assigned to any property Autocompleter and binds it with property. Note : used AutocompleterBufferedPropertyWrapper to get commiting on triggerChanel commit.
     *
     * @param <T>
     * @param entity
     * @param propertyName
     * @param triggerChannel
     *            - trigger to be externally created to handle commiting whenever you want
     * @param acceptableValues
     * @param lookupClass
     * @param expression
     * @param secExpression
     * @param valueSeparator
     * @return
     */
    public static <T> BoundedValidationLayer<AutocompleterTextFieldLayer<T>> createTriggeredAutocompleter(final IBindingEntity entity, final String propertyName, final String caption, final ValueModel triggerChannel, final Class<T> lookupClass, final String expression, final String secExpression, final String valueSeparator, final IValueMatcher<T> valueMatcher, final String originalToolTipText, final boolean stringBinding, final IOnCommitAction... actions) {

	final AutocompleterTextFieldLayer<T> autocompleter = ComponentFactory.createUnBoundAutocompleter(caption, lookupClass, expression, secExpression, valueMatcher, valueSeparator);
	final BoundedValidationLayer<AutocompleterTextFieldLayer<T>> autocompleterValidationLayer = createBoundedValidationLayer(autocompleter, originalToolTipText);
	Binder.bindTriggeredAutocompleter(autocompleterValidationLayer, entity, propertyName, triggerChannel, stringBinding, actions);
	return autocompleterValidationLayer;
    }

    /**
     * Creates BoundedJXDatePicker. This is On Key Typed Commit component (in sense of binding - coz no BuffPropertyWrappers used). It means that component commits when when
     * commits its JFormattedTextField (e.g. on Enter press), or when date was selected from JXMothView, or when JFormattedTextField looses its focus
     *
     * @param entity
     * @param propertyName
     * @param originalToolTipText
     * @param useTheTimePortion
     *            - if true the DatePicker shows the time portion of the Date, otherwise - simply the date
     * @param defaultTimePortionMillis
     *            - defines a default value of time-portion millis to be used when picking-up/typing a brand new date; if previous date has non-empty time-portion millis - newDate
     *            will be altered by them (default value will be ignored).
     * @param actions
     * @return
     */
    public static BoundedValidationLayer<BoundedJXDatePicker> createBoundedJXDatePicker(final IBindingEntity entity, final String propertyName, final String originalToolTipText, final boolean useTheTimePortion, final Long defaultTimePortionMillis, final IOnCommitAction... actions) {
	final BoundedJXDatePicker boundedJXDatePicker = new BoundedJXDatePicker(originalToolTipText, useTheTimePortion, defaultTimePortionMillis);
	BoundedJXDatePicker.bindJXDatePicker(boundedJXDatePicker, entity, propertyName, originalToolTipText, actions);
	return boundedJXDatePicker.getBoundedValidationLayer();
    }

    /**
     * Creates DatePickerLayer and and bounds to date property. This component commits when when commits its JFormattedTextField (e.g. on Enter press), or when date was selected
     * from JXMothView, or when JFormattedTextField looses its focus.
     *
     * @param entity
     * @param propertyName
     * @param originalToolTipText
     * @param useTheTimePortion
     *            - if true the DatePicker shows the time portion of the Date, otherwise - simply the date
     * @param defaultTimePortionMillis
     *            - defines a default value of time-portion millis to be used when picking-up/typing a brand new date; if previous date has non-empty time-portion millis - newDate
     *            will be altered by them (default value will be ignored).
     *
     * @param actions
     * @return
     */
    public static BoundedValidationLayer<DatePickerLayer> createDatePickerLayer(final IBindingEntity entity, final String propertyName, final String originalToolTipText, final String caption, final boolean useTheTimePortion, final Long defaultTimePortionMillis, final IOnCommitAction... actions) {
	final DatePickerLayer datePickerLayer = new DatePickerLayer(caption, Locale.getDefault(), useTheTimePortion, null, defaultTimePortionMillis);
	final BoundedValidationLayer<DatePickerLayer> boundedValidationLayer = createBoundedValidationLayer(datePickerLayer, originalToolTipText);
	Binder.bindDate(boundedValidationLayer, entity, propertyName, true, actions);

	return boundedValidationLayer;
    }

    /**
     * This interface can be added at trigger binded components (on Focus lost too). The action postCommitAction invokes after commit action(even it is invalid)
     *
     * @author jhou
     *
     */
    public interface IOnCommitAction {
	/**
	 * this action invokes in all commit situations, either they have successful result or not
	 */
	void postCommitAction();

	void postSuccessfulCommitAction();

	void postNotSuccessfulCommitAction();
    }

    public interface IOnCommitActionable {

	boolean addOnCommitAction(final IOnCommitAction onCommitAction);

	boolean removeOnCommitAction(final IOnCommitAction onCommitAction);

	List<IOnCommitAction> getOnCommitActions();

    }

    /**
     * Enumeration indicating a register setting to be used in an editor.
     *
     * @author TG Team
     */
    public static enum EditorCase {
	UPPER_CASE, MIXED_CASE;
    }
}
