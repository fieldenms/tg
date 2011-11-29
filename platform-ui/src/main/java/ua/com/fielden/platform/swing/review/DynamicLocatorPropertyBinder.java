package ua.com.fielden.platform.swing.review;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.ei.editors.EntityPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.OrdinaryPropertyEditor;

import com.google.inject.Inject;

public class DynamicLocatorPropertyBinder<T extends AbstractEntity, DAO extends IEntityDao<T>> implements ILightweightPropertyBinder<DynamicEntityQueryCriteria<T, DAO>> {

    private final IEntityMasterManager entityMasterFactory;

    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    public DynamicLocatorPropertyBinder(final IEntityMasterManager entityMasterFactory) {
	this.entityMasterFactory = entityMasterFactory;
    }

    @Override
    public void rebind(final Map<String, IPropertyEditor> editors, final DynamicEntityQueryCriteria<T, DAO> entity) {
	final Date curr = new Date();
	final Set<String> keys = entity.getKeySet();
	for (final String key : keys) {
	    final IPropertyEditor propertyEditor = editors.get(key);
	    if (propertyEditor != null) {
		propertyEditor.bind(entity);
	    } else {
		editors.put(key, bindProperty(entity, key));
	    }
	}
	// enhance titles and descriptions by unified TG way :
	PropertyBinderEnhancer.enhancePropertyEditors(entity.getEntityClass(), editors, false);
	logger.debug("Rebind in..." + (new Date().getTime() - curr.getTime()) + "ms");
    }

    @Override
    public Map<String, IPropertyEditor> bind(final DynamicEntityQueryCriteria<T, DAO> entity) {
	final Map<String, IPropertyEditor> newPropertyEditors = new HashMap<String, IPropertyEditor>();
	final Set<String> props = entity.getKeySet();
	// iterate through the meta-properties and create appropriate editors
	for (final String prop : props) {
	    newPropertyEditors.put(prop, bindProperty(entity, prop));
	}
	// enhance titles and descriptions by unified TG way :
	PropertyBinderEnhancer.enhancePropertyEditors(entity.getEntityClass(), newPropertyEditors, false);
	return newPropertyEditors;
    }

    private IPropertyEditor bindProperty(final DynamicEntityQueryCriteria<T, DAO> entity, final String property) {
	final Date curr = new Date();
	logger.debug("Property binding ...");
	final DynamicProperty dynamicProperty = entity.getEditableProperty(property);
	final IPropertyEditor editor;
	if (AbstractEntity.class.isAssignableFrom(dynamicProperty.getType())) { // property is of entity type
	    editor = createAutocompleter(entity, property);
	} else if (Collection.class.isAssignableFrom(dynamicProperty.getType())) {
	    throw new UnsupportedOperationException("Collectional types aren't supported yet by the Dynamic criteria property binder");
	} else { // the only possible case is property of an ordinary type
	    editor = createOrdinaryPropertyEditor(entity, property);
	}
	logger.debug("Property bounded in..." + (new Date().getTime() - curr.getTime()) + "ms");
	return editor;
    }

    /**
     * Creates autocompleter's {@link IPropertyEditor} for the specified property name
     *
     * @param entity
     * @param property
     * @return
     */
    protected IPropertyEditor createAutocompleter(final DynamicEntityQueryCriteria entity, final String property) {
	final DynamicProperty editableProperty = entity.getEditableProperty(property);
	if (editableProperty.isEntityProperty() && editableProperty.isSingle()) {
	    return new EntityPropertyEditor(entity, property);
	}
	return new CollectionalPropertyEditor(entity, property);
    }

    /**
     * Creates {@link IPropertyEditor} for ordinary propertyTypes
     *
     * @param entity
     * @param property
     * @return
     */
    protected IPropertyEditor createOrdinaryPropertyEditor(final DynamicEntityQueryCriteria entity, final String property) {
	return new OrdinaryPropertyEditor(entity, property);
    }

    public IEntityMasterManager getEntityMasterFactory() {
	return entityMasterFactory;
    }
}
