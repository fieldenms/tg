package ua.com.fielden.platform.swing.ei.editors.development;

import static ua.com.fielden.platform.swing.components.bind.ComponentFactory.EditorCase.MIXED_CASE;
import static ua.com.fielden.platform.swing.components.bind.ComponentFactory.EditorCase.UPPER_CASE;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumSet;

import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IPropertyEnum;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.Secrete;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.LabelAndTooltipExtractor;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.RadioButtonPanel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.swing.utils.TabAction;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

/**
 * Editor for an entity property of ordinary types (i.e. not entity and non-collectional).
 *
 * @author TG Team
 *
 */
public class OrdinaryPropertyEditor implements IPropertyEditor {

    private AbstractEntity<?> entity;
    private final String propertyName;

    private final JLabel label;
    private BoundedValidationLayer<?> rebindableEditor;
    private RadioButtonPanel radioEditor;
    private final JComponent editor;


    /**
     * Creates {@link OrdinaryPropertyEditor} for the entity centre and binds it to the specified property.
     * 
     * @param criteria
     * @param propertyName
     * @return
     */
    public static OrdinaryPropertyEditor createOrdinaryPropertyEditorForCentre(final EntityQueryCriteria<ICentreDomainTreeManager, ?, ?> criteria, final String propertyName){
	final Class<?> type = PropertyTypeDeterminator.determinePropertyType(criteria.getType(), propertyName);
	final Long defaultTimePortionMillis = (Date.class.isAssignableFrom(type) && CriteriaReflector.isSecondParam((Class<EntityQueryCriteria>)criteria.getType(), propertyName)) ? DatePickerLayer.defaultTimePortionMillisForTheEndOfDay() : 0L;
	return new OrdinaryPropertyEditor(criteria, propertyName, defaultTimePortionMillis);
    }

    /**
     * Creates {@link OrdinaryPropertyEditor} and binds it to the specified property.
     * 
     * @param entity
     * @param propertyName
     * @return
     */
    public static OrdinaryPropertyEditor createOrdinaryPropertyEditor(final AbstractEntity<?> entity, final String propertyName){
	return new OrdinaryPropertyEditor(entity, propertyName, 0L);
    }

    /**
     * Creates {@link OrdinaryPropertyEditor} and binds it to the specified property in the given entity.
     * 
     * @param entity
     * @param propertyName
     * TODO
     * @param defaultTimePortionMillis - ?
     */
    public OrdinaryPropertyEditor(final AbstractEntity<?> entity, final String propertyName, final Long defaultTimePortionMillis) {

	this.entity = entity;
	this.propertyName = propertyName;

	final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract(propertyName, entity.getType());

	label = DummyBuilder.label(titleAndDesc.getKey());
	label.setToolTipText(titleAndDesc.getValue());
	final MetaProperty metaProperty = entity.getProperty(propertyName);
	editor = createEditor(entity, propertyName, metaProperty.getType(), metaProperty.getTitle(), metaProperty.getDesc(), metaProperty.isUpperCase(), defaultTimePortionMillis);
    }

    @SuppressWarnings("unchecked")
    private JComponent createEditor(final AbstractEntity<?> entity, final String bindingPropertyName, final Class type, final String title, final String desc, final boolean upperCase, final Long defaultTimePortionMillis) {
	final JComponent editor;
	if (Integer.class == type || int.class == type) {
	    // final BoundedValidationLayer<JFormattedTextField> component = ComponentFactory.createIntegerTextField(entity, propertyName, true, desc);
	    final BoundedValidationLayer<JSpinner> component = ComponentFactory.createNumberSpinner(entity, bindingPropertyName, true, desc, 1);
	    rebindableEditor = component;
	    radioEditor = null;
	    editor = component;
	} else if (Money.class.isAssignableFrom(type) || BigDecimal.class == type || Double.class == type || double.class == type) {
	    final BoundedValidationLayer<JFormattedTextField> component = ComponentFactory.createBigDecimalOrMoneyOrDoubleField(entity, bindingPropertyName, true, desc);
	    rebindableEditor = component;
	    radioEditor = null;
	    editor = component;
	} else if (Date.class == type) {
	    // final BoundedValidationLayer<BoundedJXDatePicker> component = ComponentFactory.createBoundedJXDatePicker(entity, propertyName, desc, true);
	    final BoundedValidationLayer<DatePickerLayer> component = ComponentFactory.createDatePickerLayer(entity, bindingPropertyName, desc, "", true, defaultTimePortionMillis/*propertyName.endsWith(DynamicEntityQueryCriteria._TO) ? DatePickerLayer.defaultTimePortionMillisForTheEndOfDay()
																						  : 0L*/);
	    rebindableEditor = component;
	    radioEditor = null;
	    editor = component;
	} else if (String.class == type) {
	    // The appropriate editor for string property is determined based on the Max annotation indicating the maximum value length
	    int length = 0;
	    try {
		final Method setter = Reflector.getMethod(entity/* .getType() */, Mutator.SETTER.getName(bindingPropertyName), String.class);
		if (setter.isAnnotationPresent(Max.class)) {
		    length = setter.getAnnotation(Max.class).value();
		}
	    } catch (final Throwable ex) {
		// TODO log exception... usually it should be a harmless situation where a property was not provided with a setter, which is a legitimate case
	    }

	    if (length > 50) {
		final BoundedValidationLayer<JTextArea> component = ComponentFactory.createStringTextArea(entity, bindingPropertyName, true, true, desc);
		// let's now handle TAB and shift TAB key press to enforce focus traversal instead of \t character insertion
		final JTextArea area = component.getView();
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		final InputMap im = area.getInputMap();
		final KeyStroke tab = KeyStroke.getKeyStroke("TAB");
		area.getActionMap().put(im.get(tab), new TabAction(true));
		final KeyStroke shiftTab = KeyStroke.getKeyStroke("shift TAB");
		im.put(shiftTab, shiftTab);
		area.getActionMap().put(im.get(shiftTab), new TabAction(false));

		component.getView().getDocument().addDocumentListener(DummyBuilder.createInputLimiter(length));
		rebindableEditor = component;
		radioEditor = null;
		final JScrollPane scrollPane = new JScrollPane(rebindableEditor) {
		    private static final long serialVersionUID = -5987466954900293008L;

		    @Override
		    public boolean requestFocusInWindow() {
			return getViewport().getView().requestFocusInWindow(); // should be overridden to provide automatic refocusing from scroll pane to its wrapped component
		    }
		};
		editor = scrollPane;
	    } else {
		boolean isSecrete = false;
		try {
		    isSecrete = AnnotationReflector.getPropertyAnnotation(Secrete.class, entity.getType(), bindingPropertyName) != null;
		} catch (final Exception ex) {
		    // in most cases this exception will be thrown when entity is the DynamicEntityQueryCriteria
		}
		if (!isSecrete) {
		    final BoundedValidationLayer<JTextField> component = ComponentFactory.createStringTextField(entity, bindingPropertyName, true, desc, upperCase ? UPPER_CASE
			    : MIXED_CASE);
		    rebindableEditor = component;
		    radioEditor = null;
		    editor = component;
		} else {
		    final BoundedValidationLayer<JPasswordField> component = ComponentFactory.createPasswordField(entity, bindingPropertyName, true, desc, '*');
		    rebindableEditor = component;
		    radioEditor = null;
		    editor = component;
		}
	    }
	} else if (Boolean.class == type || boolean.class == type) {
	    final BoundedValidationLayer<JCheckBox> component = ComponentFactory.createCheckBox(entity, bindingPropertyName, title, desc);
	    rebindableEditor = component;
	    radioEditor = null;
	    editor = component;
	} else if (type.isEnum()) {
	    final EnumSet values = EnumSet.allOf(type.asSubclass(Enum.class));
	    final RadioButtonPanel radioPanel = new RadioButtonPanel();
	    for (final Object value : values) {
		if (value instanceof IPropertyEnum) {
		    radioPanel.addEditor((Enum) value, ComponentFactory.createRadioButton(entity, bindingPropertyName, value, ((IPropertyEnum) value).getTooltip()));
		}
	    }
	    rebindableEditor = null;
	    radioEditor = radioPanel;
	    editor = radioPanel;
	} else {
	    editor = null;
	}

	return editor;
    }

    @Override
    public void bind(final AbstractEntity<?> entity) {
	this.entity = entity;
	if (rebindableEditor != null) {
	    rebindableEditor.rebindTo(entity);
	}
	if (radioEditor != null) {
	    for (final BoundedValidationLayer<JRadioButton> validationLayer : radioEditor.getEditors()) {
		validationLayer.rebindTo(entity);
	    }
	}
    }

    @Override
    public AbstractEntity<?> getEntity() {
	return entity;
    }

    @Override
    public String getPropertyName() {
	return propertyName;
    }

    public JLabel getLabel() {
	return label;
    }

    public JComponent getEditor() {
	return editor;
    }

    @Override
    public JPanel getDefaultLayout() {
	final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]5[]", "[c]"));
	panel.add(label);
	panel.add(editor, "growx");
	return panel;
    }

    @Override
    public IValueMatcher<?> getValueMatcher() {
	throw new UnsupportedOperationException("Value matcher are not applicable for ordinary properties.");
    }

    //Refactor later after testing consider whether to remove this method from the IPropertyEditor interface.
    @Override
    public boolean isIgnored() {
	if (entity instanceof DynamicEntityQueryCriteria) {
	    final DynamicEntityQueryCriteria dynamicEntity = (DynamicEntityQueryCriteria) entity;
	    return dynamicEntity.isEmptyValue(propertyName);
	} else {
	    return false;
	}
    }
}
