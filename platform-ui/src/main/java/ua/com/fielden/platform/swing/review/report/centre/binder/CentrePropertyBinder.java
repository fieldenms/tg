package ua.com.fielden.platform.swing.review.report.centre.binder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.swing.ei.editors.development.EntityPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.development.EntityPropertyEditorWithLocator;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.development.OrdinaryPropertyEditor;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

/**
 * Binds the {@link EntityQueryCriteria} to the appropriate property editors.
 *
 * @author TG Team
 *
 * @param <T> - The centre's entity type.
 */
public class CentrePropertyBinder<T extends AbstractEntity<?>> implements ILightweightPropertyBinder<EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>> {
    private final static Logger logger = Logger.getLogger(CentrePropertyBinder.class);
    private final EntityCentreType entityCentreType;

    private final ICriteriaGenerator criteriaGenerator;

    /**
     * Creates property binder for entity centre.
     *
     * @param criteriaGenerator
     * @return
     */
    public static <T extends AbstractEntity<?>> CentrePropertyBinder<T> createCentrePropertyBinder(final ICriteriaGenerator criteriaGenerator){
	return new CentrePropertyBinder<T>(EntityCentreType.CENTRE, criteriaGenerator);
    }

    /**
     * Creates property binder for entity locator.
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> CentrePropertyBinder<T> createLocatorPropertyBinder(){
	return new CentrePropertyBinder<T>(EntityCentreType.LOCATOR, null);
    }

    /**
     * Initiates this {@link CentrePropertyBinder} with specific {@link EntityCentreType} and {@link CriteriaGenerator} instance.
     *
     * @param entityCentreType
     */
    private CentrePropertyBinder(final EntityCentreType entityCentreType, final ICriteriaGenerator criteriaGenerator) {
	this.entityCentreType = entityCentreType;
	this.criteriaGenerator = criteriaGenerator;
    }

    @Override
    public Map<String, IPropertyEditor> bind(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> entity) {
	final Map<String, IPropertyEditor> newPropertyEditors = new HashMap<String, IPropertyEditor>();
	final List<Field> fields = CriteriaReflector.getCriteriaProperties(entity.getClass());
	// iterate through the meta-properties and create appropriate editors
	for (final Field field : fields) {
	    newPropertyEditors.put(field.getName(), bindProperty(entity, field.getName()));
	}
	// enhance titles and descriptions by unified TG way :
	PropertyBinderEnhancer.enhancePropertyEditors((Class<? extends EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>>) entity.getType(), newPropertyEditors, false);
	return newPropertyEditors;
    }

    @Override
    public void rebind(final Map<String, IPropertyEditor> editors, final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> entity) {
	final List<Field> fields = CriteriaReflector.getCriteriaProperties(entity.getClass());
	for (final Field field : fields) {
	    final IPropertyEditor propertyEditor = editors.get(field.getName());
	    if (propertyEditor != null) {
		propertyEditor.bind(entity);
	    } else {
		editors.put(field.getName(), bindProperty(entity, field.getName()));
	    }
	}
	// enhance titles and descriptions by unified TG way :
	PropertyBinderEnhancer.enhancePropertyEditors((Class<? extends EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>>)entity.getType(), editors, false);
    }

    private IPropertyEditor bindProperty(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> entity, final String property) {
	logger.debug("\tBinding [" + property + "] property...");
	final MetaProperty metaProp = entity.getProperty(property);
	final IsProperty propertyAnnotation = AnnotationReflector.getPropertyAnnotation(IsProperty.class, entity.getType(), property);
	final EntityType entityTypeAnnotation = AnnotationReflector.getPropertyAnnotation(EntityType.class, entity.getType(), property);
	final boolean isSingle = metaProp.isCollectional() ? false : true;
	final boolean stringBinding = isSingle ? false : String.class.isAssignableFrom(propertyAnnotation.value());
	final Class<?> elementType = isSingle ? metaProp.getType() : (stringBinding ? entityTypeAnnotation.value() : propertyAnnotation.value());

	final IPropertyEditor pe;
	if (AbstractEntity.class.isAssignableFrom(elementType)) { // property is of entity type
	    pe = createAutocompleter(entity, metaProp.getName());
	    logger.debug("\tBinding [" + property + "] property (autocompleter)...done");
	} else { // the only possible case is property of an ordinary type
	    pe = createOrdinaryPropertyEditor(entity, property);
	    logger.debug("\tBinding [" + property + "] property (ordinary)...done");
	}
	return pe;
    }

    private IPropertyEditor createAutocompleter(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> entity, final String propertyName) {
	switch (entityCentreType) {
	case LOCATOR:
	    return EntityPropertyEditor.createEntityPropertyEditorForCentre(entity, propertyName);
	case CENTRE:
	    return EntityPropertyEditorWithLocator.createEntityPropertyEditorWithLocatorForCentre(entity, propertyName, criteriaGenerator);
	}
	return null;
    }

    private IPropertyEditor createOrdinaryPropertyEditor(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> entity, final String property) {
	return OrdinaryPropertyEditor.createOrdinaryPropertyEditorForCentre(entity, property);
    }

    /**
     * Declares two types of entity centres: LOCATOR or CENTRE.
     *
     * @author TG Team
     *
     */
    private enum EntityCentreType {
	CENTRE, LOCATOR;
    }
}
