package ua.com.fielden.platform.web.centre;

import static java.util.Optional.of;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.applyCriteria;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.invalidCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrInherited;

import java.util.Map;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.types.tuples.T2;

/**
 * An abstract producer for new instances of {@link AbstractCentreConfigCommitAction} descendants.
 *
 * @author TG Team
 *
 */
public abstract class AbstractCentreConfigCommitActionProducer<T extends AbstractCentreConfigCommitAction> extends DefaultEntityProducerWithContext<T> {
    
    public AbstractCentreConfigCommitActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final Class<T> entityType) {
        super(factory, entityType, companionFinder);
    }
    
    @Override
    protected final T provideDefaultValues(final T entity) {
        if (contextNotEmpty()) {
            // centre context holder is needed to restore criteria entity during saving and to perform appropriate closure
            entity.setCentreContextHolder(selectionCrit().centreContextHolder());
            
            // in most cases the following check will be needed to determine the course of action
            // also it will throw early failure in case where current configuration was deleted
            final boolean isDefaultOrInherited = isDefaultOrInherited(selectionCrit().saveAsNameSupplier().get(), selectionCrit());
            
            // apply criteria entity
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = applyCriteria(selectionCrit());
            entity.setCustomObject(
                // and avoid configuration saveAs / edit if centre is invalid
                invalidCustomObject(selectionCrit(), appliedCriteriaEntity)
                .map(customObj -> {
                    entity.setSkipUi(true);
                    return customObj;
                })
                .orElseGet(() -> performProduce(entity, selectionCrit(), appliedCriteriaEntity, isDefaultOrInherited))
            );
        }
        return entity;
    }
    
    /**
     * Performs actual essence of producing action.
     * 
     * @param entity
     * @param selectionCrit
     * @param appliedCriteriaEntity
     * @param isDefaultOrInherited -- indicates whether current configuration is default or inherited
     * @return
     */
    protected abstract Map<String, Object> performProduce(final T entity, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, boolean isDefaultOrInherited);
    
    /**
     * Initialises <code>title</code> and <code>desc</code> inside <code>entity</code>. Takes them from persisted configuration with concrete name <code>saveAsName</code>.
     * 
     * @param entity
     * @param saveAsName
     * @param selectionCrit
     */
    protected void setTitleAndDesc(final T entity, final String saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        setTitleAndDesc(entity, saveAsName, selectionCrit, "");
    }
    
    /**
     * Initialises <code>title</code> and <code>desc</code> inside <code>entity</code>. Takes them from persisted configuration with concrete name <code>saveAsName</code>.
     * <p>
     * Concatenates <code>suffix</code> to both <code>title</code> and <code>desc</code>.
     * 
     * @param entity
     * @param saveAsName
     * @param selectionCrit
     * @param suffix
     */
    protected void setTitleAndDesc(final T entity, final String saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final String suffix) {
        makeTitleAndDescRequired(entity);
        
        // change values
        final T2<String, String> titleAndDesc = selectionCrit.centreTitleAndDescGetter().apply(of(saveAsName)).get();
        entity.setTitle(titleAndDesc._1 + suffix);
        entity.setDesc(titleAndDesc._2 + suffix);
    }
    
    /**
     * Makes <code>title</code> and <code>desc</code> required inside <code>entity</code>.
     * 
     * @param entity
     */
    protected void makeTitleAndDescRequired(final T entity) {
        // make title and desc required
        entity.getProperty("title").setRequired(true);
        entity.getProperty("desc").setRequired(true);
    }
    
}