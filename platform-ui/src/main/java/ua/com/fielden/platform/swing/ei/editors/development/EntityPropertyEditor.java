package ua.com.fielden.platform.swing.ei.editors.development;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.ei.editors.LabelAndTooltipExtractor;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

/**
 * Editor for an entity property of non-collectional types.
 * 
 * @author TG Team
 * 
 */
public class EntityPropertyEditor extends AbstractEntityPropertyEditor {

    private final BoundedValidationLayer<AutocompleterTextFieldLayer> editor;

    /**
     * Creates standard {@link EntityPropertyEditor} editor for entity master.
     * 
     * @param entity
     * @param propertyName
     * @param valueMatcher
     * @return
     */
    public static EntityPropertyEditor createEntityPropertyEditorForMaster(final AbstractEntity<?> entity, final String propertyName, final IValueMatcher<?> valueMatcher, final boolean isSingle){
	final MetaProperty metaProp = entity.getProperty(propertyName);
	return new EntityPropertyEditor(entity, propertyName, "", metaProp.getDesc(), valueMatcher, isSingle, false);
    }

    /**
     * Creates standard {@link EntityPropertyEditor} editor for entity locator.
     * 
     * @param criteria
     * @param propertyName
     * @return
     */
    public static EntityPropertyEditor createEntityPropertyEditorForCentre(final EntityQueryCriteria<ICentreDomainTreeManager, ?, ?> criteria, final String propertyName, final boolean isSingle){
	//TODO Refactor after testing.
	final MetaProperty metaProp = criteria.getProperty(propertyName);
	final Class entityType = PropertyTypeDeterminator.determineClass(criteria.getType(), propertyName, true, true);
	final boolean stringBinding = !PropertyDescriptor.class.equals(entityType);
	final IValueMatcher<?> valueMatcher = criteria.getValueMatcher(propertyName);
	return new EntityPropertyEditor(criteria, propertyName, LabelAndTooltipExtractor.createCaption(metaProp.getTitle()), LabelAndTooltipExtractor.createTooltip(metaProp.getDesc()), valueMatcher, isSingle, stringBinding);
    }

    /**
     * Initiates this {@link EntityPropertyEditor} with specified entity, property name, caption, tool tip, and value matcher.
     * 
     * @param entity
     * @param propertyName
     * @param caption
     * @param toolTip
     * @param valueMatcher
     */
    public EntityPropertyEditor(final AbstractEntity<?> entity, final String propertyName, final String caption, final String toolTip, final IValueMatcher<?> valueMatcher, final boolean isSingle, final boolean stringBinding){
	super(entity, propertyName, valueMatcher);
	final MetaProperty property = entity.getProperty(propertyName);
	editor = createEditor(entity, propertyName, property.getType(), caption, toolTip, isSingle, stringBinding);
    }

    @Override
    public IValueMatcher getValueMatcher() {
	// TODO Auto-generated method stub
	return super.getValueMatcher();
    }


    @Override
    public BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor() {
	return editor;
    }

    public void highlightFirstHintValue(final boolean highlight) {
	getEditor().getView().highlightFirstHintValue(highlight);
    }

    public void highlightSecondHintValue(final boolean highlight) {
	getEditor().getView().highlightSecondHintValue(highlight);
    }

    private BoundedValidationLayer<AutocompleterTextFieldLayer> createEditor(final AbstractEntity<?> bindingEntity, final String bindingPropertyName, final Class<?> elementType, final String caption, final String tooltip, final boolean isSingle, final boolean stringBinding) {
	if (!AbstractEntity.class.isAssignableFrom(elementType)) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + elementType + ".");
	}
	return ComponentFactory.createOnFocusLostAutocompleter(bindingEntity, bindingPropertyName, caption, elementType, "key", "desc", isSingle ? null : ",", getValueMatcher(), tooltip, stringBinding);
    }
}
