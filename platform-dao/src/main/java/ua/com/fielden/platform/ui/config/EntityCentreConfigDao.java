package ua.com.fielden.platform.ui.config;

import static ua.com.fielden.platform.utils.EntityUtils.isConflicting;

import java.util.Map;
import java.util.function.Function;

import javax.persistence.OptimisticLockException;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ioc.session.exceptions.SessionScopingException;

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

    private static final int SAVING_RETRIES_THRESHOULD = 10;

    public static final String ERR_ALREADY_IN_TRANSACTIONAL_SCOPE = "Saving of an Entity Centre should never occur in an existing transactional scope.";
    public static final String ERR_COULD_NOT_SAVE_CONFIG = "Could not save Entity Centre [%s] after %s retries.";
    private static final Logger LOGGER = Logger.getLogger(EntityCentreConfigDao.class);

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
     * This method should not manage a transaction scope.
     * Similarly, method {@link #refetchReapplyAndSaveWithoutConflicts(EntityCentreConfig, int, RuntimeException)} should also not manage a transaction scope.
     * This is due to a recursive invocation of the same logic inside of method {@link #refetchReapplyAndSaveWithoutConflicts(EntityCentreConfig, int, RuntimeException)}.
     * The problem lies in {@link OptimisticLockException} (or other conflict-based exceptions) which, when actioned, rolls back the current transaction;
     * This, in turn, makes it impossible to invoke method {@link #refetchReapplyAndSaveWithoutConflicts(EntityCentreConfig, int, RuntimeException)} recursively.
     * What we need here is more granular transaction scoping, which is why we have {@link #quickSave(EntityCentreConfig)} with {@link SessionRequired}. If {@link OptimisticLockException} occurs then
     * only a granular transaction around ({@link #quickSave(EntityCentreConfig)}) is rolled back.
     * Any subsequent recursive invocation of {@link #refetchReapplyAndSaveWithoutConflicts(EntityCentreConfig, int, RuntimeException)} would create a separate, independent {@link SessionRequired} scope for nested calls to {@link #quickSave(EntityCentreConfig)}.
     */
    // @SessionRequired -- avoid transaction here; refer the javadoc
    @Override
    public Long saveWithoutConflicts(final EntityCentreConfig entity) {
        // let's ensure there is no current session – if it exists then throw SessionScopingException
        try {
            final Session session = getSession(); // throws EntityCompanionException if there is no session
            if (session.getTransaction().isActive()) {
                LOGGER.error(ERR_ALREADY_IN_TRANSACTIONAL_SCOPE);
                throw new SessionScopingException(ERR_ALREADY_IN_TRANSACTIONAL_SCOPE);
            }
        } catch (final EntityCompanionException ex) {
            // an exception is expected in cases where there is no current session, which is what we need here
        }
        // no session is present, we can proceed with saving
        try {
            // we allow nested transaction scope here intentionally (quickSave allows it);
            // i.e. there will be rollbacked outer transaction in case of saving conflicts in some rare combinations, but no exception about disallowed nested transaction;
            // the only possible existing combination (very unlikely) is as following:
            // 1. multiple @SessionRequired DAO savings with ICriteriaEntityRestorer restoration occur simultaneously for the same user; stack of calls:
            // saveWithoutConflicts (EntityCentreConfigDao)
            //   updateDifferences (CentreUpdater)
            //     updateCentre (CentreUpdater)
            //       createCriteriaEntityForPaginating (CentreResourceUtils)
            //         createCriteriaEntityForContext (CentreResourceUtils)
            //           restoreCriteriaEntity (CriteriaEntityRestorer)
            // 2. somehow FRESH centre configuration was not persisted earlier (extremely unlikely, because action would not be possible to open -- need loaded entity centre configuration on the client);
            // in this situation DAO saving would have broken transaction, i.e. if some exception would be thrown after saveWithoutConflicts then full rollback would not be performed
            return quickSave(entity); // must be quickSave(entity), not super.quickSave(entity)!
            // Need to repeat saving of entity in case of "self conflict": in a concurrent environment the same user on the same entity centre configuration can trigger multiple concurrent validations with different parameters.
        } catch (final RuntimeException exception) {
            // Hibernate StaleStateException/StaleObjectStateException can occur in PersistentEntitySaver.saveModifiedEntity during session flushing ('session.get().flush();').
            // It is always wrapped into javax.persistence.OptimisticLockException by Hibernate (+LockAcquisitionException in modern TG); see ExceptionConverterImpl.wrapStaleStateException for more details.
            // Exactly the same strategy should be used for Hibernate-based conflicts as for TG-based ones.
            // We catch all exceptions including EntityCompanionException, [PersistenceException, ConstraintViolationException, SQLServerException], EntityAlreadyExists, ObjectNotFoundException caused by saving conflicts.
            // If the exception is not legit (non-conflict-nature), then it will be rethrown after several retries.
            return refetchReapplyAndSaveWithoutConflicts(entity, 1 /* first retry */, exception); // repeat the procedure of 'conflict-aware' saving in cases of subsequent conflicts
        }
    }
    
    /**
     * Re-fetches <code>entity</code>, re-applies dirty properties from <code>entity</code> against re-fetched instance and saves it.
     * 
     * @param entity
     * @param retry
     * @param exception
     * @return
     */
    private Long refetchReapplyAndSaveWithoutConflicts(final EntityCentreConfig entity, final int retry, final RuntimeException exception) {
        if (retry > SAVING_RETRIES_THRESHOULD) {
            LOGGER.debug(String.format(ERR_COULD_NOT_SAVE_CONFIG, entity.getTitle(), SAVING_RETRIES_THRESHOULD), exception);
            throw exception;
        }
        final EntityCentreConfig persistedEntity = findByEntityAndFetch(null, entity);
        final EntityCentreConfig entityToSave;
        if (persistedEntity != null) {
            // several properties can be conflicting, e.g. 'configBody' (most cases), 'desc' (rare, but possible), 'preferred' etc.; other properties are the parts of key and will not conflict;
            // for the case of new entity saving, all props would be dirty, however only some - conflicting
            for (final MetaProperty<?> prop : entity.getDirtyProperties()) {
                final String name = prop.getName();
                if (isConflicting(prop.getValue(), prop.getOriginalValue(), persistedEntity.get(name))) {
                    persistedEntity.set(name, prop.getValue());
                }
            }
            entityToSave = persistedEntity;
        } else {
            entityToSave = entity.copyTo(new_());
        }
        try {
            return quickSave(entityToSave);
        } catch (final RuntimeException nextException) {
            return refetchReapplyAndSaveWithoutConflicts(entityToSave, retry + 1, nextException); // repeat the procedure of 'conflict-aware' saving in cases of subsequent conflicts
        }
    }
    
}