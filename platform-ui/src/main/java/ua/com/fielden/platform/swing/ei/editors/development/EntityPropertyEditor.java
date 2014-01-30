package ua.com.fielden.platform.swing.ei.editors.development;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

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
    public static EntityPropertyEditor createEntityPropertyEditorForMaster(final AbstractEntity<?> entity, final String propertyName, final IValueMatcher<?> valueMatcher, final Pair<String, String>... titleExprToDisplay){
	final MetaProperty metaProp = entity.getProperty(propertyName);
	return new EntityPropertyEditor(entity, propertyName, "", metaProp.getDesc(), valueMatcher, EditorCase.MIXED_CASE, titleExprToDisplay);
    }

    /**
     * Creates standard {@link EntityPropertyEditor} editor for entity locator.
     *
     * @param criteria
     * @param propertyName
     * @return
     */
    public static EntityPropertyEditor createEntityPropertyEditorForCentre(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, ?, ?> criteria, final String propertyName, final Pair<String, String>... titleExprToDisplay){
	final MetaProperty metaProp = criteria.getProperty(propertyName);
	final IValueMatcher<?> valueMatcher = criteria.getValueMatcher(propertyName);
	return new EntityPropertyEditor(criteria, propertyName, LabelAndTooltipExtractor.createCaption(metaProp.getTitle()), LabelAndTooltipExtractor.createTooltip(metaProp.getDesc()), valueMatcher, EditorCase.MIXED_CASE, titleExprToDisplay);
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
    private EntityPropertyEditor(final AbstractEntity<?> entity, final String propertyName, final String caption, final String toolTip, final IValueMatcher<?> valueMatcher, final EditorCase editorCase, final Pair<String, String>... titleExprToDisplay){
	super(entity, propertyName, valueMatcher);
	final MetaProperty metaProp = entity.getProperty(propertyName);
	final IsProperty propertyAnnotation = AnnotationReflector.getPropertyAnnotation(IsProperty.class, entity.getType(), propertyName);
	final EntityType entityTypeAnnotation = AnnotationReflector.getPropertyAnnotation(EntityType.class, entity.getType(), propertyName);
	final boolean isSingle = metaProp.isCollectional() ? false : true;
	final boolean stringBinding = isSingle ? false : String.class.isAssignableFrom(propertyAnnotation.value());
	final Class elementType = isSingle ? metaProp.getType() : (stringBinding ? DynamicEntityClassLoader.getOriginalType(entityTypeAnnotation.value()) : propertyAnnotation.value());
	if(!AbstractEntity.class.isAssignableFrom(elementType)){
	    throw new IllegalArgumentException("The property: " + propertyName + " of " + entity.getType().getSimpleName() + " type, can not be bind to the autocompleter!");
	}
	editor = createEditor(entity, propertyName, elementType, caption, toolTip, isSingle, stringBinding, editorCase, titleExprToDisplay);
    }

    @Override
    public IValueMatcher getValueMatcher() {
	return super.getValueMatcher();
    }

    @Override
    public BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor() {
	return editor;
    }

    private BoundedValidationLayer<AutocompleterTextFieldLayer> createEditor(final AbstractEntity<?> bindingEntity, //
	    final String bindingPropertyName, final Class elementType, final String caption, final String tooltip, //
	    final boolean isSingle, final boolean stringBinding, final EditorCase editorCase, final Pair<String, String>... titleExprToDisplay) {
	if (!AbstractEntity.class.isAssignableFrom(elementType)) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + elementType + ".");
	}
	return ComponentFactory.createOnFocusLostAutocompleter(bindingEntity, bindingPropertyName, caption, elementType, //
		"key", secondaryExpressions(elementType), highlightProperties(elementType), //
		isSingle ? null : ",", getValueMatcher(), tooltip, stringBinding, editorCase);
    }

    private Set<String> highlightProperties(final Class entityType) {
	final List<Field> keyMembers = Finder.getKeyMembers(entityType);
	final Set<String> highlightProps = new HashSet<>();
	highlightProps.add("key");
	for(final Field keyMember : keyMembers) {
	    highlightProps.add(keyMember.getName());
	}
	return highlightProps;
    }

    @SuppressWarnings("unchecked")
    private Pair<String, String>[] secondaryExpressions(final Class entityType) {
	final List<Pair<String, String>> props = new ArrayList<>();
	final List<Field> keyMembers = Finder.getKeyMembers(entityType);
	if(keyMembers.size() > 1) {
	    for (final Field keyMember : keyMembers) {
		props.add(new Pair<String, String>(TitlesDescsGetter.getTitleAndDesc(keyMember.getName(), entityType).getKey(), keyMember.getName()));
	    }
	}
	if (EntityUtils.hasDescProperty(entityType)) {
	    props.add(new Pair<String, String>(TitlesDescsGetter.getTitleAndDesc("desc", entityType).getKey(), "desc"));
	}
	return props.toArray(new Pair[0]);
    }

    public void setPropertyToHighlight(final String property, final boolean highlight) {
	getEditor().getView().setPropertyToHighlight(property, highlight);
    }
}
