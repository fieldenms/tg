package ua.com.fielden.platform.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.DefaultEntityProducerForCompoundMenuItem;
import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarnings;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;
import ua.com.fielden.platform.web.utils.EntityRestorationUtils;

public class EntityResourceContinuationsHelper {
    private final static Logger logger = Logger.getLogger(EntityResourceContinuationsHelper.class);

    /**
     * Saves the <code>entity</code> with its <code>continuations</code>.
     *
     * In case where warnings exist, <code>continuations</code> should include respective warnings acknowledgement continuation if it was accepted by the user. Could throw
     * continuations exceptions, 'no changes' exception or validation exceptions.
     *
     * @param entity
     * @param continuations
     *            -- continuations of the entity to be used during saving
     *
     * @return
     */
    private static <T extends AbstractEntity<?>> T saveWithContinuations(final T entity, final Map<String, IContinuationData> continuations, final CommonEntityDao<T> co) {
        final boolean continuationsPresent = !continuations.isEmpty();

        // iterate over properties in search of the first invalid one (without required checks)
        final java.util.Optional<Result> firstFailure = entity.nonProxiedProperties().filter(mp -> mp.getFirstFailure() != null).findFirst().map(mp -> mp.getFirstFailure());

        // returns first failure if exists or successful result if there was no failure.
        final Result isValid = firstFailure.isPresent() ? firstFailure.get() : Result.successful(entity);

        if (isValid.isSuccessful()) {
            final String acknowledgementContinuationName = "_acknowledgedForTheFirstTime";
            if (entity.hasWarnings() && (!continuationsPresent || continuations.get(acknowledgementContinuationName) == null)) {
                throw new NeedMoreData("Warnings need acknowledgement", AcknowledgeWarnings.class, acknowledgementContinuationName);
            } else if (entity.hasWarnings() && continuationsPresent && continuations.get(acknowledgementContinuationName) != null) {
                entity.nonProxiedProperties().forEach(prop -> prop.clearWarnings());
            }
        }

        // 1) non-persistent entities should always be saved (isDirty will always be true)
        // 2) persistent but not persisted (new) entities should always be saved (isDirty will always be true)
        // 3) persistent+persisted+dirty (by means of dirty properties existence) entities should always be saved
        // 4) persistent+persisted+notDirty+inValid entities should always be saved: passed to companion 'save' method to process validation errors in domain-driven way by companion object itself
        // 5) persistent+persisted+notDirty+valid entities saving should be skipped
        if (!entity.isDirty() && entity.isValid().isSuccessful()) {
            throw Result.failure("There are no changes to save.");
        }

        if (continuationsPresent) {
            co.setMoreData(continuations);
        } else {
            co.clearMoreData();
        }
        final T saved = co.save(entity);
        if (continuationsPresent) {
            co.clearMoreData();
        }
        return saved;
    }

    /**
     * Performs DAO saving of <code>validatedEntity</code>.
     * <p>
     * IMPORTANT: note that if <code>validatedEntity</code> has been mutated during saving in its concrete companion object (for example VehicleStatusChangeDao) or in
     * {@link CommonEntityDao} saving methods -- still that entity instance will be returned in case of exceptional situation and will be bound to respective entity master. The
     * toast message, however, will show the message, that was thrown during saving as exceptional (not first validation error of the entity).
     *
     * @param validatedEntity
     * @param continuations -- continuations of the entity to be used during saving
     *
     * @return if saving was successful -- returns saved entity with no exception if saving was unsuccessful with exception -- returns <code>validatedEntity</code> (to be bound to
     *         appropriate entity master) and thrown exception (to be shown in toast message)
     */
    public static <T extends AbstractEntity<?>> Pair<T, Optional<Exception>> saveWithContinuations(final T validatedEntity, final Map<String, IContinuationData> continuations, final IEntityDao<T> companion) {
        T savedEntity;
        try {
            // try to save the entity with its companion 'save' method
            savedEntity = saveWithContinuations(validatedEntity, continuations, (CommonEntityDao<T>) companion);
        } catch (final Exception exception) {
            // Some exception can be thrown inside 1) its companion 'save' method OR 2) CommonEntityDao 'save' during its internal validation.
            // Return entity back to the client after its unsuccessful save with the exception that was thrown during saving
            return Pair.pair(validatedEntity, Optional.of(exception));
        }
    
        return Pair.pair(savedEntity, Optional.empty());
    }
    
    /**
     * Restores the entity from {@link SavingInfoHolder} and tries to save it.
     * 
     * @param savingInfoHolder
     * @param entityType
     * @param entityFactory
     * @param companionFinder
     * @param critGenerator
     * @param webUiConfig
     * @param serverGdtm
     * @param userProvider
     * @param companion
     * @return
     */
    public static <T extends AbstractEntity<?>> Pair<T, Optional<Exception>> tryToSave(
        final SavingInfoHolder savingInfoHolder,
        final Class<T> entityType,
        final EntityFactory entityFactory,
        final ICompanionObjectFinder companionFinder,
        final IEntityDao<T> companion
    ) {
        final List<IContinuationData> conts = !savingInfoHolder.proxiedPropertyNames().contains("continuations") ? savingInfoHolder.getContinuations() : new ArrayList<>();
        final List<String> contProps = !savingInfoHolder.proxiedPropertyNames().contains("continuationProperties") ? savingInfoHolder.getContinuationProperties() : new ArrayList<>();
        final Map<String, IContinuationData> continuations = conts != null && !conts.isEmpty() ?
                createContinuationsMap(conts, contProps) : new LinkedHashMap<>();
        final T applied = restoreEntityFrom(savingInfoHolder, entityType, entityFactory, companionFinder, 0);

        final Pair<T, Optional<Exception>> potentiallySavedWithException = saveWithContinuations(applied, continuations, companion);
        return potentiallySavedWithException;
    }
    
    /**
     * Creates map of continuations by continuation keys.
     * 
     * @param continuations
     * @param continuationProperties
     * @return
     */
    public static Map<String, IContinuationData> createContinuationsMap(final List<IContinuationData> continuations, final List<String> continuationProperties) {
        final Map<String, IContinuationData> map = new LinkedHashMap<>();
        for (int index = 0; index < continuations.size(); index++) {
            map.put(continuationProperties.get(index), continuations.get(index));
        }
        return map;
    }
    
    private static <T extends AbstractEntity<?>> T restoreEntityFrom(
            final SavingInfoHolder savingInfoHolder,
            final Class<T> functionalEntityType,
            final EntityFactory entityFactory,
            final ICompanionObjectFinder companionFinder,
            final int tabCount) {
        final DateTime start = new DateTime();
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): started.");
        final IEntityProducer<T> producer = createEntityProducer(entityFactory, functionalEntityType, companionFinder);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): producer.");
        final IEntityDao<T> companion = companionFinder.<IEntityDao<T>, T> find(functionalEntityType);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): utils.");
        final Map<String, Object> modifHolder = savingInfoHolder.getModifHolder();

        final Object arrivedIdVal = modifHolder.get(AbstractEntity.ID);
        final Long longId = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");

        final T restored = restoreEntityFrom(savingInfoHolder, functionalEntityType, companion, producer, longId, companionFinder, tabCount + 1);
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): duration: " + pd.getSeconds() + " s " + pd.getMillis() + " ms.");
        return restored;
    }
    
    private static <T extends AbstractEntity<?>> T restoreEntityFrom(
            final SavingInfoHolder savingInfoHolder,
            final Class<T> functionalEntityType,
            final IEntityDao<T> companion,
            final IEntityProducer<T> producer,
            final Long entityId,
            final ICompanionObjectFinder companionFinder,
            final int tabCount) {
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): started.");
        final Map<String, Object> modifiedPropertiesHolder = savingInfoHolder.getModifHolder();
        final T applied;
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder.");
        applied = EntityRestorationUtils.constructEntity(modifiedPropertiesHolder, entityId, companion, producer, companionFinder).getKey();
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder finished.");
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): finished.");
        return applied;
    }
    
    /**
     * Creates an entity producer instance.
     *
     * @param injector
     * @return
     */
    private static <T extends AbstractEntity<?>> IEntityProducer<T> createEntityProducer(final EntityFactory entityFactory, final Class<T> entityType, final ICompanionObjectFinder coFinder) {
//        return entityProducerType == null ? createDefaultEntityProducer(injector.getInstance(EntityFactory.class), this.entityType, this.coFinder)
//                : injector.getInstance(this.entityProducerType);
        
        // TODO actual producer types???
        return createDefaultEntityProducer(entityFactory, entityType, coFinder);
    }

    /**
     * Creates default entity producer instance.
     *
     * @return
     */
    private static <T extends AbstractEntity<?>> IEntityProducer<T> createDefaultEntityProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder coFinder) {
        if (AbstractFunctionalEntityForCompoundMenuItem.class.isAssignableFrom(entityType)) {
            return new DefaultEntityProducerForCompoundMenuItem(factory, entityType, coFinder);
        }
        return new DefaultEntityProducerWithContext<T>(factory, entityType, coFinder);
    }
}
