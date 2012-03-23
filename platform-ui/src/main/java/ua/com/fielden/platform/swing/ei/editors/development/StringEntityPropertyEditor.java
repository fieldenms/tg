/**
 *
 */
package ua.com.fielden.platform.swing.ei.editors.development;

import ua.com.fielden.platform.basic.IValueMatcher2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;

/**
 * Property editor for entities, that functions with string binding
 *
 * @author TG Team
 */

public class StringEntityPropertyEditor extends AbstractEntityPropertyEditor {

    private final BoundedValidationLayer<AutocompleterTextFieldLayer> editor;

    public StringEntityPropertyEditor(final Class<?> lookupClass, final AbstractEntity<?> entity, final String propertyName, final IValueMatcher2<?> valueMatcher) {
	super(entity, propertyName, valueMatcher);
	this.editor = createEditor(entity, entity.getProperty(propertyName), lookupClass, valueMatcher);
    }

    private BoundedValidationLayer<AutocompleterTextFieldLayer> createEditor(final AbstractEntity entity, final MetaProperty metaProperty, final Class<?> lookupClass, final IValueMatcher2 valueMatcher) {
	if (!String.class.isAssignableFrom(metaProperty.getType())) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + metaProperty.getType() + ".");
	}

	final BoundedValidationLayer<AutocompleterTextFieldLayer> component = ComponentFactory.createOnFocusLostAutocompleter(entity, getPropertyName(), "", lookupClass, "key", "desc", null, valueMatcher, metaProperty.getDesc(), true);
	return component;
    }

    @Override
    public BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor() {
	return editor;
    }

}
