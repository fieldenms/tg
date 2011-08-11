/**
 *
 */
package ua.com.fielden.platform.swing.ei.editors;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicProperty;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;

/**
 * Abstract property editor for entities. Do not contains editor, because it could be created in several ways (i.e. with string binding or without etc.)
 * 
 * @author TG Team
 */
public abstract class AbstractEntityPropertyEditor implements IPropertyEditor {
    private static final long serialVersionUID = 1L;

    private AbstractEntity<?> entity;
    private final String propertyName;

    private final IValueMatcher<?> valueMatcher;

    private final JLabel label;

    public AbstractEntityPropertyEditor(final DynamicEntityQueryCriteria criteria, final String propertyName) {
	this.entity = criteria;
	this.propertyName = propertyName;
	this.valueMatcher = criteria.getValueMatcher(propertyName);

	final DynamicProperty dynamicProperty = criteria.getEditableProperty(propertyName);

	label = DummyBuilder.label(dynamicProperty.getTitle());
	label.setToolTipText(dynamicProperty.getDesc());
    }

    public AbstractEntityPropertyEditor(final AbstractEntity<?> entity, final String propertyName, final IValueMatcher<?> valueMatcher) {
	this.entity = entity;
	this.propertyName = propertyName;
	this.valueMatcher = valueMatcher;

	final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract(propertyName, entity.getType());

	label = DummyBuilder.label(titleAndDesc.getKey());
	label.setToolTipText(titleAndDesc.getValue());
    }

    @Override
    public void bind(final AbstractEntity<?> entity) {
	AbstractEntity<?> bindingEntity = entity;
	if (entity instanceof DynamicEntityQueryCriteria) {
	    final DynamicProperty dynamicProperty = ((DynamicEntityQueryCriteria) entity).getEditableProperty(getPropertyName());
	    if (dynamicProperty.isEntityProperty() && dynamicProperty.isSingle()) {
		bindingEntity = (AbstractEntity<?>) dynamicProperty.getCriteriaValue();
	    } else {
		throw new IllegalArgumentException("The " + propertyName + " proeprty of the " + dynamicProperty.getType() + " type "
			+ (dynamicProperty.isEntityProperty() ? " is not single" : (" is not entity " + (dynamicProperty.isSingle() ? "" : " and not single"))));
	    }
	}
	this.entity = entity;
	getEditor().rebindTo(bindingEntity);
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

    public abstract BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor();

    @Override
    public JPanel getDefaultLayout() {
	final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]5[]", "[c]"));
	panel.add(label);
	panel.add(getEditor(), "growx");
	return panel;
    }

    @Override
    public IValueMatcher<?> getValueMatcher() {
	return valueMatcher;
    }

    @Override
    public boolean isIgnored() {
	throw new RuntimeException("(Is Ignored) checking is not supported for this property editor.");
    }

}
