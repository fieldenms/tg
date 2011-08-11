package ua.com.fielden.platform.swing.ei;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.ei.editors.CollectionalPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.EntityPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.IPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.OrdinaryPropertyEditor;
import ua.com.fielden.platform.swing.review.PropertyBinderEnhancer;

/**
 * Implements the binding logic for entity properties. This implementation is not thread safe. Therefore, each separate instance should be used strictly for a single instance of
 * entity inspector.
 *
 * @author 01es
 *
 */
@SuppressWarnings("unchecked")
public class PropertyBinder<T extends AbstractEntity> implements IPropertyBinder<T> {

    private final IValueMatcherFactory valueMatcherFactory;

    /**
     * Contains property editors mapped to corresponding property names.
     */
    private final Map<String, IPropertyEditor> propertyEditors = new HashMap<String, IPropertyEditor>();

    public PropertyBinder(final IValueMatcherFactory valueMatcherFactory) {
	this.valueMatcherFactory = valueMatcherFactory;
    }

    @Override
    public Map<String, IPropertyEditor> bind(final T entity) {
	if (propertyEditors.isEmpty()) {
	    final SortedSet<MetaProperty> metaProps = Finder.getMetaProperties(entity);
	    // iterate through the meta-properties and create appropriate editors
	    for (final MetaProperty metaProp : metaProps) {
		if (metaProp.isVisible()) { // should include only visible properties
		    if (AbstractEntity.class.isAssignableFrom(metaProp.getType())) { // property is of entity type
			final IPropertyEditor editor = new EntityPropertyEditor(entity, metaProp.getName(), valueMatcherFactory.getValueMatcher(entity.getType(), metaProp.getName()));
			propertyEditors.put(metaProp.getName(), editor);
		    } else if (metaProp.isCollectional()) {
			// TODO implement support for collectional properties
			final IPropertyEditor editor = new CollectionalPropertyEditor(entity, metaProp.getName());
			propertyEditors.put(metaProp.getName(), editor);
		    } else { // the only possible case is property of an ordinary type
			final IPropertyEditor editor = new OrdinaryPropertyEditor(entity, metaProp.getName());
			propertyEditors.put(metaProp.getName(), editor);
		    }
		}
	    }
	} else {
	    rebind(entity);
	}

	// enhance titles and descriptions by unified TG way :
	PropertyBinderEnhancer.enhancePropertyEditors(entity.getType(), propertyEditors, true);
	return propertyEditors;
    }

    private void rebind(final AbstractEntity<?> entity) {
	for (final IPropertyEditor editor : propertyEditors.values()) {
	    editor.bind(entity);
	}
    }

}
