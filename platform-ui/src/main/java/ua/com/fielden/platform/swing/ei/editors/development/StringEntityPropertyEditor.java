/**
 *
 */
package ua.com.fielden.platform.swing.ei.editors.development;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Property editor for entities, that functions with string binding
 *
 * @author TG Team
 */

public class StringEntityPropertyEditor extends AbstractEntityPropertyEditor {

    private final BoundedValidationLayer<AutocompleterTextFieldLayer> editor;

    public StringEntityPropertyEditor(//
	    final Class<?> lookupClass, //
	    final AbstractEntity<?> entity, //
	    final String propertyName, //
	    final IValueMatcher<?> valueMatcher, //
	    final EditorCase editorCase, //
	    final Pair<String, String>... titleExprToDisplay) {
	super(entity, propertyName, valueMatcher);
	this.editor = createEditor(entity, entity.getProperty(propertyName), lookupClass, valueMatcher, editorCase);
    }

    private BoundedValidationLayer<AutocompleterTextFieldLayer> createEditor(//
	    final AbstractEntity<?> entity, //
	    final MetaProperty metaProperty, //
	    final Class lookupClass, //
	    final IValueMatcher<?> valueMatcher, //
	    final EditorCase editorCase, //
	    final Pair<String, String>... titleExprToDisplay) {
	if (!String.class.isAssignableFrom(metaProperty.getType())) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + metaProperty.getType() + ".");
	}
	final BoundedValidationLayer<AutocompleterTextFieldLayer> component = ComponentFactory.createOnFocusLostAutocompleter(entity, getPropertyName(), "", lookupClass, "key", secondaryExpressions(lookupClass), highlightProperties(lookupClass), null, valueMatcher, metaProperty.getDesc(), true, editorCase);
	return component;
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

    @Override
    public BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor() {
	return editor;
    }

}
