/**
 *
 */
package ua.com.fielden.platform.swing.review;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.OrdinaryPropertyEditor;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * 
 * Implementation of the {@link IPropertyBinder} interface trageted specifically at the custom entity reviews. It provides a special treatment of collection properties with
 * {@link EntityType} annotation recognising them as multi-valued selection criteria.
 * 
 * @author TG Team
 * 
 */
public class CriteriaPropertyBinder<T extends EntityQueryCriteria> implements IPropertyBinder<T> {

    private final Map<String, IPropertyEditor> editors = new HashMap<String, IPropertyEditor>();

    /**
     * creates the CriteriaPropertyBinder instance with the property details map. and uses it to create property editors
     * 
     * @param propertyDetails
     */
    public CriteriaPropertyBinder() {
    }

    @Override
    public Map<String, IPropertyEditor> bind(final T entity) {
	if (editors.isEmpty()) {
	    for (final Object metaPropAsObject : entity.getVisibleProperties()) {
		// YN 27/05/2009 : cast needed because of Java's compiler limitation - if EntityQueryCriteria is not parameterized, then all methods return non-parameterized
		// results
		final MetaProperty metaProp = (MetaProperty) metaPropAsObject;
		if (AbstractEntity.class.isAssignableFrom(metaProp.getType())) { // property is of entity type
		    throw new UnsupportedOperationException("Currently not supported by " + CriteriaPropertyBinder.class);
		} else if (metaProp.isCollectional()) {
		    // TODO implement support for collectional properties
		    final Field field = Finder.getFieldByName(entity.getClass(), metaProp.getName());
		    final IsProperty propertyAnnotation = field.getAnnotation(IsProperty.class);
		    final EntityType typeAnnotation = field.getAnnotation(EntityType.class);
		    if (typeAnnotation != null) {
			final CritOnly critAnnotation = field.getAnnotation(CritOnly.class);
			editors.put(metaProp.getName(), createAutocompleter(entity, metaProp, propertyAnnotation, typeAnnotation, (critAnnotation != null && critAnnotation.value() == Type.SINGLE)));
		    } else {
			throw new UnsupportedOperationException("Please annotate field '" + metaProp.getName() + "' with annotation " + EntityType.class.getName());
		    }
		} else { // the only possible case is property of an ordinary type
		    final IPropertyEditor editor = new OrdinaryPropertyEditor(entity, metaProp.getName());
		    editors.put(metaProp.getName(), editor);
		}
	    }
	} else {
	    throw new UnsupportedOperationException("rebinding is not yet supported by this binder");
	}
	return Collections.unmodifiableMap(editors);
    }

    protected IPropertyEditor createAutocompleter(final T entity, final MetaProperty metaProp, final IsProperty propertyAnnotation, final EntityType typeAnnotation, final boolean isSingle) {
	final Class elementType = propertyAnnotation.value() == PropertyDescriptor.class ? PropertyDescriptor.class : typeAnnotation.value();
	final String label = metaProp.getTitle() != null ? metaProp.getTitle().substring(0, 1).toUpperCase() + metaProp.getTitle().substring(1) : "title not set";
	final String tooltip = metaProp.getDesc();
	final String autocompleterCaption = metaProp.getDesc();// "filter by " + metaProp.getTitle() + "...";
	final IValueMatcher valueMatcher = entity.getValueMatcher(metaProp.getName());
	final IPropertyEditor editor = new CollectionalPropertyEditor(entity, metaProp.getName(), elementType, autocompleterCaption, label, tooltip, valueMatcher, isSingle);
	return editor;
    }

}
