package ua.com.fielden.platform.ui.config;

import static ua.com.fielden.platform.companion.PersistentEntitySaver.ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES;
import static ua.com.fielden.platform.utils.EntityUtils.isConflicting;

import java.util.Map;
import java.util.function.Function;

import javax.persistence.OptimisticLockException;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * DAO implementation of {@link EntityCentreConfigCo}.
 * <p>
 * Method {@link #save(EntityCentreConfig)} is intentionally not overridden due to the need to use {@link #quickSave(EntityCentreConfig)}.
 * However, please always use {@link #saveWithoutConflicts(EntityCentreConfig)} instead of save/quickSave.
 * This ensures graceful conflict resolution in cases where simultaneous processes for the same user occur.
 * This method should not be used in another transaction scope.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityCentreConfig.class)
public class EntityCentreConfigDao extends CommonEntityDao<EntityCentreConfig> implements EntityCentreConfigCo {
    
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
    
    @Override
    public <T> T withDbVersion(final Function<DbVersion, T> fun) {
        return fun.apply(getDbVersion());
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
     * What we need here is granular {@link SessionRequired} scope, so we have {@link #quickSave(EntityCentreConfig)} with {@link SessionRequired} and when {@link OptimisticLockException} occurs, only little granular transaction ({@link #quickSave(EntityCentreConfig)}) makes inactive (and further rollbacks in SessionInterceptor).
     * Next recursive invocation of {@link #saveWithoutConflicts(EntityCentreConfig)} method will trigger separate independent {@link SessionRequired} scope for nested call {@link #quickSave(EntityCentreConfig)}.
     */
    @Override
    public Long saveWithoutConflicts(final EntityCentreConfig entity) {
        try {
            // we allow nested transaction scopes here intentionally (quickSave allows it);
            // i.e. there will be rollbacked outer transaction in case of saving conflicts in some rare combinations, but no exception about disallowed nested transaction;
            // the only possible existing combination (very unlikely) is as following:
            // 1. multiple @SessionRequired dao savings with ICriteriaEntityRestorer restoration occur simultaneously for the same user;
            // updateDifferences
            //   updateCentre
            //     createCriteriaEntityForPaginating
            //       createCriteriaEntityForContext
            //         restoreCriteriaEntity
            // 2. somehow FRESH centre configuration was not persisted earlier (extremely unlikely, because action would not be possible to open -- need loaded entity centre configuration on the client);
            // in this situation dao saving would have broken transaction, i.e. if some exception would be thrown after saveWithoutConflicts then full rollback would not be performed
            return quickSave(entity); // must be quickSave(entity), not super.quickSave(entity)!
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
     * Re-fetches <code>entity</code>, re-applies dirty properties from <code>entity</code> against re-fetched instance and saves it.
     * 
     * @param entity
     * @return
     */
    private Long refetchReapplyAndSaveWithoutConflicts(final EntityCentreConfig entity) {
        final EntityCentreConfig persistedEntity = findByEntityAndFetch(null, entity);
        // several properties can be conflicting, e.g. 'configBody' (most cases), 'desc' (rare, but possible), 'preferred' etc.; other properties are the parts of key and will not conflict;
        // for the case of new entity saving, all props would be dirty, however only some - conflicting
        for (final MetaProperty<?> prop : entity.getDirtyProperties()) {
            final String name = prop.getName();
            if (isConflicting(prop.getValue(), prop.getOriginalValue(), persistedEntity.get(name))) {
                persistedEntity.set(name, prop.getValue());
            }
        }
        return saveWithoutConflicts(persistedEntity);
    }
    
}