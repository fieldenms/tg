package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarnings;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;

public class EntityResourceContinuationsHelper {
    
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

        // iterate over properties in search of the first invalid one with required checks, but not for @CritOnly properties
        final java.util.Optional<Result> firstFailure = entity.nonProxiedProperties()
                .filter(mp -> !mp.isValidWithRequiredCheck(true) && mp.getFirstFailure() != null)
                .findFirst().map(mp -> mp.getFirstFailure());
        
        // returns first failure if exists or successful result if there was no failure.
        final Result isValid = firstFailure.isPresent() ? firstFailure.get() : Result.successful(entity);
        
        if (isValid.isSuccessful()) {
            if (entity.warnings().stream().anyMatch(EntityResourceUtils::isNonConflicting)) {
                final String acknowledgementContinuationName = "_acknowledgedForTheFirstTime";
                if (!continuationsPresent || continuations.get(acknowledgementContinuationName) == null) {
                    throw new NeedMoreData("Warnings need acknowledgement", AcknowledgeWarnings.class, acknowledgementContinuationName);
                } else if (continuationsPresent && continuations.get(acknowledgementContinuationName) != null) {
                    entity.nonProxiedProperties().forEach(prop -> prop.clearWarnings());
                }
            }
        }

        // 1) non-persistent entities should always be saved (isDirty will always be true)
        // 2) persistent but not persisted (new) entities should always be saved (isDirty will always be true)
        // 3) persistent+persisted+dirty (by means of dirty properties existence) entities should always be saved
        // 4) persistent+persisted+notDirty+inValid entities should always be saved: passed to companion 'save' method to process validation errors in domain-driven way by companion object itself
        // 5) persistent+persisted+notDirty+valid entities saving should be skipped
        if (!entity.isDirty() && entity.isValid().isSuccessful()) { // this isValid validation does not really do additional validation (but, perhaps, cleared warnings could appear again), but is provided for additional safety
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
     * Performs saving of <code>validatedEntity</code>.
     * <p>
     * IMPORTANT: note that if <code>validatedEntity</code> has been mutated during saving in its companion object (for example <code>VehicleStatusChangeDao</code>) or in
     * {@link CommonEntityDao}, the mutated entity instance is returned in case of an exceptional situation and bound to a corresponding entity master. The
     * toast message shows the exception, not the first validation error of the entity.
     *
     * @param validatedEntity
     * @param continuations -- continuations of the entity to be used during saving
     *
     * @return if saving was successful -- returns saved entity without any exception, if saving was unsuccessful due to an exception -- returns <code>validatedEntity</code> (to be bound to
     *         the appropriate entity master) and the thrown exception (to be displayed as a toast message)
     */
    public static <T extends AbstractEntity<?>> Pair<T, Optional<Exception>> saveWithContinuations(final T validatedEntity, final Map<String, IContinuationData> continuations, final IEntityDao<T> companion) {
        T savedEntity;
        try {
            // try to save the entity with its companion 'save' method
            savedEntity = saveWithContinuations(validatedEntity, continuations, (CommonEntityDao<T>) companion);
        } catch (final Exception exception) {
            // Some exception can be thrown inside 1) its companion 'save' method OR 2) CommonEntityDao 'save' during its internal validation.
            // Return entity back to the client after its unsuccessful save with the exception that was thrown during saving
            return pair(validatedEntity, Optional.of(exception));
        }
    
        return pair(savedEntity, Optional.empty());
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
    
}
