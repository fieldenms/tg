package ua.com.fielden.platform.ui.config;

import com.google.inject.Inject;
import org.hibernate.Session;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.exceptions.EntityCentreExecutionException;

import javax.persistence.OptimisticLockException;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.EntityUtils.isConflicting;

/**
 * DAO implementation of {@link EntityCentreConfigCo}.
 * <p>
 * Method {@link #save(EntityCentreConfig)} is intentionally not overridden due to the need to use {@link #quickSave(EntityCentreConfig)}.
 * However, please always use {@link #saveWithRetry(EntityCentreConfig)} instead of save/quickSave.
 * This ensures graceful conflict resolution in cases where simultaneous processes for the same user occur.
 *
 * @author TG Team
 *
 */
@EntityType(EntityCentreConfig.class)
public class EntityCentreConfigDao extends CommonEntityDao<EntityCentreConfig> implements EntityCentreConfigCo {

    private static final int SAVING_RETRIES_THRESHOULD = 5;

    public static final String ERR_ALREADY_IN_TRANSACTIONAL_SCOPE = "Saving of an Entity Centre should never occur in an existing transactional scope.";
    public static final String ERR_COULD_NOT_SAVE_CONFIG = "Could not save Entity Centre [%s] after %s attempts.";

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

    /**
     * {@inheritDoc}
     * <p>
     * Implementation details:
     * <p>
     * This method should not manage a transaction scope.
     * Similarly, method {@link #refetchReapplyAndSaveWithRetry(EntityCentreConfig, int, long, RuntimeException)} should also not manage a transaction scope.
     * This is due to a recursive invocation of the same logic inside of method {@link #refetchReapplyAndSaveWithRetry(EntityCentreConfig, int, long, RuntimeException)}.
     * The problem lies in {@link OptimisticLockException} (or other conflict-based exceptions) which, when actioned, rolls back the current transaction;
     * This, in turn, makes it impossible to invoke method {@link #refetchReapplyAndSaveWithRetry(EntityCentreConfig, int, long, RuntimeException)} recursively.
     * What we need here is more granular transaction scoping, which is why we have {@link #quickSave(EntityCentreConfig)} with {@link SessionRequired}. If {@link OptimisticLockException} occurs then
     * only a granular transaction around ({@link #quickSave(EntityCentreConfig)}) is rolled back.
     * Any subsequent recursive invocation of {@link #refetchReapplyAndSaveWithRetry(EntityCentreConfig, int, long, RuntimeException)} would create a separate, independent {@link SessionRequired} scope for nested calls to {@link #quickSave(EntityCentreConfig)}.
     */
    // @SessionRequired -- avoid transaction here; refer the javadoc
    @Override
    public Long saveWithRetry(final EntityCentreConfig entity) {
        // let's determine if there is an active session scope, in which saveWithRetry is invoked
        // if there is then saving should not retry and instead should re-throw an exception in case of a failure during saving to ensure correct transaction handling
        // otherwise, if there is no an active session scope, it is safe to retry saving
        final boolean inOuterSessionScope = hasActiveSessionScope();
        // no session is present, we can proceed with saving
        try {
            // we allow nested transaction scope here intentionally (quickSave allows it);
            // i.e. there will be rollbacked outer transaction in case of saving conflicts in some rare combinations, but no exception about disallowed nested transaction;
            // the only possible existing combination (very unlikely) is as following:
            // 1. multiple @SessionRequired DAO savings with ICriteriaEntityRestorer restoration occur simultaneously for the same user; stack of calls:
            // saveWithRetry (EntityCentreConfigDao)
            //   updateDifferences (CentreUpdater)
            //     updateCentre (CentreUpdater)
            //       createCriteriaEntityForPaginating (CentreResourceUtils)
            //         createCriteriaEntityForContext (CentreResourceUtils)
            //           restoreCriteriaEntity (CriteriaEntityRestorer)
            // 2. somehow FRESH centre configuration was not persisted earlier (extremely unlikely, because action would not be possible to open -- need loaded entity centre configuration on the client);
            // in this situation DAO saving would have broken transaction, i.e. if some exception would be thrown after saveWithRetry then full rollback would not be performed
            return quickSave(entity); // must be quickSave(entity), not super.quickSave(entity)!
            // Need to repeat saving of entity in case of "self conflict": in a concurrent environment the same user on the same entity centre configuration can trigger multiple concurrent validations with different parameters.
        } catch (final RuntimeException ex) {
            // Hibernate StaleStateException/StaleObjectStateException can occur in PersistentEntitySaver.saveModifiedEntity during session flushing ('session.get().flush();').
            // It is always wrapped into javax.persistence.OptimisticLockException by Hibernate (+LockAcquisitionException in modern TG); see ExceptionConverterImpl.wrapStaleStateException for more details.
            // Exactly the same strategy should be used for Hibernate-based conflicts as for TG-based ones.
            // We catch all exceptions including EntityCompanionException, [PersistenceException, ConstraintViolationException, SQLServerException], EntityAlreadyExists, ObjectNotFoundException caused by saving conflicts.
            // If the exception is not legit (non-conflict-nature), then it will be rethrown after several retries.
            if (!inOuterSessionScope) {
                return refetchReapplyAndSaveWithRetry(entity, 1 /* first retry */, 300 /* initial delay in millis */, ex); // repeat the procedure of 'conflict-aware' saving in cases of subsequent conflicts
            } else {
                throw ex;
            }
        }
    }

    /**
     * Determines if there is an active session associated with DAO instance.
     *
     * @return
     */
    private boolean hasActiveSessionScope() {
        try {
            final Session session = getSession(); // throws EntityCompanionException if there is no session
            return session.getTransaction().isActive(); // if there is a session, need to return the active flag of its transaction
        } catch (final EntityCompanionException ex) {
            // an exception is expected in cases where there is no current session, hence returning false
            return false;
        }
    }

    /**
     * Re-fetches Entity Centre configuration based on {@code config}, re-applies dirty properties from {@code config} to the re-fetched instance and saves it.
     * 
     * @param config an Entity Centre Configuration that needs saving.
     * @param retryCount a number of retries to save {@code config} that already took place.
     * @param delayMillis a number of milliseconds to wait before saving is attampted again
     * @param ex an exception that was throws during the last attempt, preventing {@code config} from saving successfully.
     * @return
     */
    private Long refetchReapplyAndSaveWithRetry(final EntityCentreConfig config, final int retryCount, final long delayMillis, final RuntimeException ex) {
        if (retryCount > SAVING_RETRIES_THRESHOULD) {
            final String msg = format(ERR_COULD_NOT_SAVE_CONFIG, config.getTitle(), SAVING_RETRIES_THRESHOULD);
            throw new EntityCentreExecutionException(msg, ex);
        }
        
        // let's make a small delay before re-attempting the saving
        // this should hopefully give enough time for any concurrent operations to conclude
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
        }

        final EntityCentreConfig persistedEntity = findByEntityAndFetch(null, config);
        final EntityCentreConfig entityToSave;
        if (persistedEntity != null) {
            // several properties can be conflicting, e.g. 'configBody' (most cases), 'desc' (rare, but possible), 'preferred' etc.; other properties are the parts of key and will not conflict;
            // for the case of new entity saving, all props would be dirty, however only some - conflicting
            for (final MetaProperty<?> prop : config.getDirtyProperties()) {
                final String name = prop.getName();
                if (isConflicting(prop.getValue(), prop.getOriginalValue(), persistedEntity.get(name))) {
                    persistedEntity.set(name, prop.getValue());
                }
            }
            entityToSave = persistedEntity;
        } else {
            entityToSave = config.copyTo(new_());
        }
        try {
            return quickSave(entityToSave);
        } catch (final RuntimeException nextException) {
            return refetchReapplyAndSaveWithRetry(entityToSave, retryCount + 1, delayMillis + 100, nextException); // repeat the procedure of 'conflict-aware' saving in cases of subsequent conflicts
        }
    }

}