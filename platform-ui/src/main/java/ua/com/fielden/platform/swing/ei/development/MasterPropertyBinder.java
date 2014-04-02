package ua.com.fielden.platform.swing.ei.development;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.ei.editors.development.CollectionalPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.development.EntityPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.development.EntityPropertyEditorWithLocator;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.development.OrdinaryPropertyEditor;
import ua.com.fielden.platform.swing.review.report.centre.binder.PropertyBinderEnhancer;

/**
 * Property binder which can bind external editors.
 * 
 * @author TG Team
 * 
 */
public class MasterPropertyBinder<T extends AbstractEntity> implements ILightweightPropertyBinder<T> {

    private final IValueMatcherFactory valueMatcherFactory;
    private final List<String> propertiesToIgnor = new ArrayList<String>();
    private final PropertyBinderType propertyBinderType;
    //private final IEntityMasterManager entityMasterFactory;

    private final IMasterDomainTreeManager masterManager;
    private final ICriteriaGenerator criteriaGenerator;

    private final Logger logger = Logger.getLogger(getClass());

    public static <T extends AbstractEntity> MasterPropertyBinder<T> createPropertyBinderWithLocatorSupport(final IValueMatcherFactory valueMatcherFactory, final IMasterDomainTreeManager masterManager, final ICriteriaGenerator criteriaGenerator, final String... propetiesToIgnor) {
        return new MasterPropertyBinder<T>(PropertyBinderType.WITH_LOCATOR, valueMatcherFactory, masterManager, criteriaGenerator, propetiesToIgnor);
    }

    public static <T extends AbstractEntity> MasterPropertyBinder<T> createPropertyBinderWithoutLocatorSupport(final IValueMatcherFactory valueMatcherFactory, final String... propetiesToIgnor) {
        return new MasterPropertyBinder<T>(PropertyBinderType.WITHOUT_LOCATOR, valueMatcherFactory, null, null, propetiesToIgnor);
    }

    private MasterPropertyBinder(final PropertyBinderType propertyBinderType, final IValueMatcherFactory valueMatcherFactory, final IMasterDomainTreeManager masterManager, final ICriteriaGenerator criteriaGenerator, /*final IEntityMasterManager entityMasterFactory,*/final String... propetiesToIgnor) {
        this.propertyBinderType = propertyBinderType;
        this.valueMatcherFactory = valueMatcherFactory;
        this.masterManager = masterManager;
        this.criteriaGenerator = criteriaGenerator;
        this.propertiesToIgnor.addAll(Arrays.asList(propetiesToIgnor));
        //this.entityMasterFactory = entityMasterFactory;
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
            if (metaProp.isVisible() && !propertiesToIgnor.contains(metaProp.getName())) { // should include only visible properties
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
        PropertyBinderEnhancer.enhancePropertyEditors((Class<T>) entity.getType(), newPropertyEditors, true);
        logger.debug("Bind in..." + (new Date().getTime() - curr.getTime()) + "ms");
        return newPropertyEditors;
    }

    protected IPropertyEditor createOrdinaryPropertyEditor(final T entity, final String name) {
        return OrdinaryPropertyEditor.createOrdinaryPropertyEditorForMaster(entity, name);
    }

    protected IPropertyEditor createCollectionalPropertyEditor(final T entity, final String name) {
        return new CollectionalPropertyEditor(entity, name);
    }

    protected IPropertyEditor createEntityPropertyEditor(final T entity, final String name, final IValueMatcher<?> valueMatcher) {
        if (PropertyBinderType.WITH_LOCATOR == propertyBinderType) {
            return EntityPropertyEditorWithLocator.createEntityPropertyEditorWithLocatorForMaster(entity, name, masterManager, criteriaGenerator, valueMatcherFactory.getValueMatcher(entity.getType(), name));
        } else if (PropertyBinderType.WITHOUT_LOCATOR == propertyBinderType)
            return EntityPropertyEditor.createEntityPropertyEditorForMaster(entity, name, valueMatcherFactory.getValueMatcher(entity.getType(), name));
        return null;
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
        PropertyBinderEnhancer.enhancePropertyEditors((Class<T>) entity.getType(), editors, true);
        logger.debug("Rebind in..." + (new Date().getTime() - curr.getTime()) + "ms");
    }

    //    protected IValueMatcherFactory getValueMatcherFactory() {
    //	return valueMatcherFactory;
    //    }

    //    public IEntityMasterManager getEntityMasterFactory() {
    //	return entityMasterFactory;
    //    }

    private enum PropertyBinderType {
        WITH_LOCATOR, WITHOUT_LOCATOR;
    }

}
