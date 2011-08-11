package ua.com.fielden.platform.swing.ei.editors;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicProperty;

/**
 * Editor for an entity property of non-collectional types.
 * 
 * @author TG Team
 * 
 */
public class EntityPropertyEditor extends AbstractEntityPropertyEditor {

    private final BoundedValidationLayer<AutocompleterTextFieldLayer> editor;

    public EntityPropertyEditor(final DynamicEntityQueryCriteria<?, ?> criteria, final String propertyName) {
	super(criteria, propertyName);
	final DynamicProperty<?> dynamicProperty = criteria.getEditableProperty(propertyName);
	if (!dynamicProperty.isEntityProperty() || !dynamicProperty.isSingle()) {
	    throw new IllegalArgumentException("The " + propertyName + " proeprty of the " + dynamicProperty.getType() + " type "
		    + (dynamicProperty.isEntityProperty() ? " is not single" : " is not entity " + (dynamicProperty.isSingle() ? "" : " and not single")));
	}
	final AbstractEntity<?> bindingEntity = (AbstractEntity) dynamicProperty.getCriteriaValue();
	final String bindingPropertyName = dynamicProperty.getActualPropertyName();
	this.editor = createEditor(bindingEntity, bindingPropertyName, dynamicProperty.getType(), LabelAndTooltipExtractor.createCaption(dynamicProperty.getTitle()), LabelAndTooltipExtractor.createTooltip(dynamicProperty.getDesc()));
    }

    public EntityPropertyEditor(final AbstractEntity<?> entity, final String propertyName, final IValueMatcher<?> valueMatcher) {
	super(entity, propertyName, valueMatcher);
	final MetaProperty property = entity.getProperty(propertyName);
	editor = createEditor(entity, propertyName, property.getType(), "", property.getDesc());
    }

    @Override
    public BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor() {
	return editor;
    }

    protected BoundedValidationLayer<AutocompleterTextFieldLayer> createEditor(final AbstractEntity<?> bindingEntity, final String bindingPropertyName, final Class<?> elementType, final String caption, final String tooltip) {
	if (!AbstractEntity.class.isAssignableFrom(elementType)) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + elementType + ".");
	}
	return ComponentFactory.createOnFocusLostAutocompleter(bindingEntity, bindingPropertyName, caption, elementType, "key", "desc", null, getValueMatcher(), tooltip, false);
    }

    public void highlightFirstHintValue(final boolean highlight) {
	getEditor().getView().highlightFirstHintValue(highlight);
    }

    public void highlightSecondHintValue(final boolean highlight) {
	getEditor().getView().highlightSecondHintValue(highlight);
    }

}
