package ua.com.fielden.platform.swing.ei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.ei.editors.CollectionalPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.EntityPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.OrdinaryPropertyEditor;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.PropertyBinderEnhancer;

/**
 * Property binder which can bind external editors.
 *
 * @author TG Team
 *
 */
public class LightweightPropertyBinder<T extends AbstractEntity> implements ILightweightPropertyBinder<T> {

    private final IValueMatcherFactory valueMatcherFactory;
    private final List<String> propetiesToIgnor = new ArrayList<String>();
    private final IEntityMasterManager entityMasterFactory;

    private final Logger logger = Logger.getLogger(getClass());

    public LightweightPropertyBinder(final IValueMatcherFactory valueMatcherFactory, final IEntityMasterManager entityMasterFactory, final String... propetyToIgnor) {
	this.valueMatcherFactory = valueMatcherFactory;
	propetiesToIgnor.addAll(Arrays.asList(propetyToIgnor));
	this.entityMasterFactory = entityMasterFactory;
    }

    /**
     * It creates the editors bounded to entity.
     */
    @Override
    public Map<String, IPropertyEditor> bind(final T entity) {
	final Date curr = new Date();
	final Map<String, IPropertyEditor> newPropertyEditors = new HashMap<String, IPropertyEditor>();
	final SortedSet<MetaProperty> metaProps = Finder.getMetaProperties(entity);
	// iterate through the meta-properties and create appropriate editors
	for (final MetaProperty metaProp : metaProps) {
	    if (metaProp.isVisible() && !propetiesToIgnor.contains(metaProp.getName())) { // should include only visible properties
		if (AbstractEntity.class.isAssignableFrom(metaProp.getType())) { // property is of entity type
		    final IPropertyEditor editor = createEntityPropertyEditor(entity, metaProp.getName(), valueMatcherFactory.getValueMatcher(entity.getType(), metaProp.getName()));
		    newPropertyEditors.put(metaProp.getName(), editor);
		} else if (metaProp.isCollectional()) {
		    // TODO implement support for collectional properties
		    final IPropertyEditor editor = createCollectionalPropertyEditor(entity, metaProp.getName());
		    newPropertyEditors.put(metaProp.getName(), editor);
		} else { // the only possible case is property of an ordinary type
		    final IPropertyEditor editor = createOrdinaryPropertyEditor(entity, metaProp.getName());

		    newPropertyEditors.put(metaProp.getName(), editor);
		}
	    }
	}

	// enhance titles and descriptions by unified TG way :
	PropertyBinderEnhancer.enhancePropertyEditors(entity.getType(), newPropertyEditors, true);
	logger.debug("Bind in..." + (new Date().getTime() - curr.getTime()) + "ms");
	return newPropertyEditors;
    }

    protected IPropertyEditor createOrdinaryPropertyEditor(final T entity, final String name) {
	return new OrdinaryPropertyEditor(entity, name);
    }

    protected IPropertyEditor createCollectionalPropertyEditor(final T entity, final String name) {
	return new CollectionalPropertyEditor(entity, name);
    }

    protected IPropertyEditor createEntityPropertyEditor(final T entity, final String name, final IValueMatcher<?> valueMatcher) {
	return new EntityPropertyEditor(entity, name, valueMatcherFactory.getValueMatcher(entity.getType(), name));
    }

    @Override
    public void rebind(final Map<String, IPropertyEditor> editors, final T entity) {
	final Date curr = new Date();
	if (editors.isEmpty()) {
	    // create new editors :
	    final Map<String, IPropertyEditor> newPropertyEditors = bind(entity);
	    // put them into "editors"
	    editors.putAll(newPropertyEditors);
	} else {
	    // rebind every editor
	    for (final IPropertyEditor editor : editors.values()) {
		editor.bind(entity);
	    }
	}

	// enhance titles and descriptions by unified TG way :
	PropertyBinderEnhancer.enhancePropertyEditors(entity.getType(), editors, true);
	logger.debug("Rebind in..." + (new Date().getTime() - curr.getTime()) + "ms");
    }

    protected IValueMatcherFactory getValueMatcherFactory() {
	return valueMatcherFactory;
    }

    public IEntityMasterManager getEntityMasterFactory() {
	return entityMasterFactory;
    }

}
