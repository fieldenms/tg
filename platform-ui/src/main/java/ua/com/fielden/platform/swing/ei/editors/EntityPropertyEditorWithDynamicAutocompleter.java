package ua.com.fielden.platform.swing.ei.editors;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.locator.ILocatorConfigurationRetriever;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicProperty;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

public class EntityPropertyEditorWithDynamicAutocompleter extends AbstractEntityPropertyEditor {

    private final BoundedValidationLayer<AutocompleterTextFieldLayer> editor;

    public EntityPropertyEditorWithDynamicAutocompleter(final DynamicEntityQueryCriteria<?, ?> criteria, final String propertyName, final IEntityMasterManager entityMasterFactory, final ILocatorConfigurationController locatorController, final ILocatorConfigurationRetriever locatorRetriever) {
	super(criteria, propertyName);
	final DynamicProperty<?> dynamicProperty = criteria.getEditableProperty(propertyName);
	if (!dynamicProperty.isEntityProperty() || !dynamicProperty.isSingle()) {
	    throw new IllegalArgumentException("The " + propertyName + " proeprty of the " + dynamicProperty.getType() + " type "
		    + (dynamicProperty.isEntityProperty() ? " is not single" : (" is not entity " + (dynamicProperty.isSingle() ? "" : " and not single"))));
	}
	final AbstractEntity<?> bindingEntity = (AbstractEntity) dynamicProperty.getCriteriaValue();
	final String bindingPropertyName = dynamicProperty.getActualPropertyName();
	editor = createEditor(bindingEntity, bindingPropertyName, dynamicProperty.getType(), LabelAndTooltipExtractor.createCaption(dynamicProperty.getTitle()), LabelAndTooltipExtractor.createTooltip(dynamicProperty.getDesc()), criteria.getCriteriaEntityFactory(), entityMasterFactory, criteria.getValueMatcherFactory(), criteria.getDaoFactory(), locatorController, locatorRetriever);

    }

    public EntityPropertyEditorWithDynamicAutocompleter(final AbstractEntity<?> entity, final String propertyName, final IValueMatcher<?> valueMatcher, final IEntityMasterManager entityMasterFactory, final IValueMatcherFactory vmf, final IDaoFactory daoFactory, final ILocatorConfigurationController locatorController, final ILocatorConfigurationRetriever locatorRetriever) {
	super(entity, propertyName, valueMatcher);
	final MetaProperty property = entity.getProperty(propertyName);
	editor = createEditor(entity, propertyName, property.getType(), "", property.getDesc(), entity.getEntityFactory(), entityMasterFactory, vmf, daoFactory, locatorController, locatorRetriever);
    }

    @SuppressWarnings("unchecked")
    private BoundedValidationLayer<AutocompleterTextFieldLayer> createEditor(final AbstractEntity<?> bindingEntity, final String bindingPropertyName, final Class<?> elementType, final String caption, final String tooltip, final EntityFactory entityFactory, final IEntityMasterManager entityMasterFactory, final IValueMatcherFactory vmf, final IDaoFactory daoFactory, final ILocatorConfigurationController locatorController, final ILocatorConfigurationRetriever locatorRetriever) {
	if (!AbstractEntity.class.isAssignableFrom(elementType)) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + elementType + ".");
	}
	return ComponentFactory.createOnFocusLostOptionAutocompleter(bindingEntity, bindingPropertyName, caption, elementType, "key", "desc", null, getValueMatcher(), entityMasterFactory, tooltip, false, entityFactory, vmf, daoFactory, locatorController, locatorRetriever);
    }

    @Override
    public BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor() {
	return editor;
    }

}
