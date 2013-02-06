package ua.com.fielden.platform.swing.ei.editors.development;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
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
    public static EntityPropertyEditor createEntityPropertyEditorForMaster(final AbstractEntity<?> entity, final String propertyName, final IValueMatcher<?> valueMatcher){
	final MetaProperty metaProp = entity.getProperty(propertyName);
	return new EntityPropertyEditor(entity, propertyName, "", metaProp.getDesc(), valueMatcher);
    }

    /**
     * Creates standard {@link EntityPropertyEditor} editor for entity locator.
     *
     * @param criteria
     * @param propertyName
     * @return
     */
    public static EntityPropertyEditor createEntityPropertyEditorForCentre(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, ?, ?> criteria, final String propertyName){
	final MetaProperty metaProp = criteria.getProperty(propertyName);
	final IValueMatcher<?> valueMatcher = criteria.getValueMatcher(propertyName);
	return new EntityPropertyEditor(criteria, propertyName, LabelAndTooltipExtractor.createCaption(metaProp.getTitle()), LabelAndTooltipExtractor.createTooltip(metaProp.getDesc()), valueMatcher);
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
    public EntityPropertyEditor(final AbstractEntity<?> entity, final String propertyName, final String caption, final String toolTip, final IValueMatcher<?> valueMatcher){
	super(entity, propertyName, valueMatcher);
	final MetaProperty metaProp = entity.getProperty(propertyName);
	final IsProperty propertyAnnotation = AnnotationReflector.getPropertyAnnotation(IsProperty.class, entity.getType(), propertyName);
	final EntityType entityTypeAnnotation = AnnotationReflector.getPropertyAnnotation(EntityType.class, entity.getType(), propertyName);
	final boolean isSingle = metaProp.isCollectional() ? false : true;
	final boolean stringBinding = isSingle ? false : String.class.isAssignableFrom(propertyAnnotation.value());
	final Class<?> elementType = isSingle ? metaProp.getType() : (stringBinding ? DynamicEntityClassLoader.getOriginalType(entityTypeAnnotation.value()) : propertyAnnotation.value());
	if(!AbstractEntity.class.isAssignableFrom(elementType)){
	    throw new IllegalArgumentException("The property: " + propertyName + " of " + entity.getType().getSimpleName() + " type, can not be bind to the autocompleter!");
	}
	editor = createEditor(entity, propertyName, elementType, caption, toolTip, isSingle, stringBinding);
    }

    @Override
    public IValueMatcher getValueMatcher() {
	return super.getValueMatcher();
    }

    @Override
    public BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor() {
	return editor;
    }

    private BoundedValidationLayer<AutocompleterTextFieldLayer> createEditor(final AbstractEntity<?> bindingEntity, final String bindingPropertyName, final Class<?> elementType, final String caption, final String tooltip, final boolean isSingle, final boolean stringBinding) {
	if (!AbstractEntity.class.isAssignableFrom(elementType)) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + elementType + ".");
	}
	return ComponentFactory.createOnFocusLostAutocompleter(bindingEntity, bindingPropertyName, caption, elementType, "key", "desc", isSingle ? null : ",", getValueMatcher(), tooltip, stringBinding);
    }

    public void highlightFirstHintValue(final boolean highlight) {
	getEditor().getView().highlightFirstHintValue(highlight);
    }

    public void highlightSecondHintValue(final boolean highlight) {
	getEditor().getView().highlightSecondHintValue(highlight);
    }
}
