package ua.com.fielden.platform.ui.config.controller;

import static ua.com.fielden.platform.companion.PersistentEntitySaver.ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES;
import static ua.com.fielden.platform.utils.EntityUtils.isConflicting;

import java.util.Map;

import javax.persistence.OptimisticLockException;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;

/**
 * DAO implementation of {@link IEntityCentreConfig}.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityCentreConfig.class)
public class EntityCentreConfigDao extends CommonEntityDao<EntityCentreConfig> implements IEntityCentreConfig {
    
    @Inject
    protected EntityCentreConfigDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public void delete(final EntityCentreConfig entity) {
        defaultDelete(entity);
    }
    
    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<EntityCentreConfig> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final EntityResultQueryModel<EntityCentreConfig> model) {
        return defaultBatchDelete(model);
    }
    
    @SessionRequired
    @Override
    public EntityCentreConfig save(final EntityCentreConfig entity) {
        throw new EntityCentreConfigDaoException("Please use saveWithoutConflicts or saveWithConflicts instead.");
    }
    
    ///////////////////////////////// GRACEFULL CONFLICT RESOLUTION /////////////////////////////////
    /**
     * {@inheritDoc}
     * <p>
     * Implementation details:
     * <p>
     * This method can not have {@link SessionRequired} scope.
     * This is due to recursive invocation of the same {@link #saveWithoutConflicts(EntityCentreConfig)} method down inside {@link #refetchReapplyAndSaveWithoutConflicts(EntityCentreConfig)}.
     * The problem lies in {@link OptimisticLockException} which, when actioned, makes transaction inactive internally; that makes impossible to invoke {@link #saveWithoutConflicts(EntityCentreConfig)} method recursively again.
     * What we need here is granular {@link SessionRequired} scope, so we annotate {@link #saveNotAllowingNestedScope(EntityCentreConfig)} with {@link SessionRequired} and when {@link OptimisticLockException} occurs, only little granular transaction ({@link #saveNotAllowingNestedScope(EntityCentreConfig)}) makes inactive (and further rollbacks in SessionInterceptor).
     * Next recursive invocation of {@link #saveWithoutConflicts(EntityCentreConfig)} method will trigger separate independent {@link SessionRequired} scope for nested call {@link #saveNotAllowingNestedScope(EntityCentreConfig)}.
     */
    @Override
    public EntityCentreConfig saveWithoutConflicts(final EntityCentreConfig entity) {
        try {
            return saveNotAllowingNestedScope(entity);
            // Need to repeat saving of entity in case of "self conflict": in a concurrent environment the same user on the same entity centre configuration can trigger multiple concurrent validations with different parameters.
        } catch (final EntityCompanionException companionException) {
            if (companionException.getMessage() != null && companionException.getMessage().contains(ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES)) { // conflict could occur for concrete user, miType and surrogate+saveAs name
                return refetchReapplyAndSaveWithoutConflicts(entity); // repeat the procedure of 'conflict-aware' saving in cases of subsequent conflicts
            } else {
                throw companionException;
            }
        } catch (final OptimisticLockException optimisticLockException) {
            // Hibernate StaleStateException can occur in PersistentEntitySaver.saveModifiedEntity during session flushing ('session.get().flush();').
            // It is always wrapped into javax.persistence.OptimisticLockException by Hibernate (see ExceptionConverterImpl.wrapStaleStateException for more details).
            // Exactly the same strategy should be used for Hibernate-based conflicts as for TG-based ones.
            return refetchReapplyAndSaveWithoutConflicts(entity); // repeat the procedure of 'conflict-aware' saving in cases of subsequent conflicts
        }
    }
    
    /**
     * Regular entity saving process with transaction scope but not allowing to nest that scope inside another scope.
     * 
     * @param entity
     * @return
     */
    @SessionRequired(allowNestedScope = false)
    public EntityCentreConfig saveNotAllowingNestedScope(final EntityCentreConfig entity) { // must be 'public' (or perhaps 'protected') for SessionInterceptor to take effect
        return super.save(entity);
    }
    
    /**
     * Re-fetches <code>entity</code>, re-applies dirty properties from <code>entity</code> against re-fetched instance and saves it.
     * 
     * @param entity
     * @return
     */
    private EntityCentreConfig refetchReapplyAndSaveWithoutConflicts(final EntityCentreConfig entity) {
        final EntityCentreConfig persistedEntity = findByEntityAndFetch(null, entity);
        for (final MetaProperty<?> prop : entity.getDirtyProperties()) { // the only two properties that can be conflicting: 'configBody' (most cases) and 'desc' (rare, but possible); other properties are the parts of key and will not conflict
            final String name = prop.getName();
            if (isConflicting(prop.getValue(), prop.getOriginalValue(), persistedEntity.get(name))) {
                persistedEntity.set(name, prop.getValue());
            }
        }
        return saveWithoutConflicts(persistedEntity);
    }
    
    ///////////////////////////////// CONFLICTS UNRESOLVED /////////////////////////////////
    /**
     * {@inheritDoc}
     */
    @Override
    @SessionRequired
    public EntityCentreConfig saveWithConflicts(final EntityCentreConfig entity) {
        return super.save(entity);
    }
    
}