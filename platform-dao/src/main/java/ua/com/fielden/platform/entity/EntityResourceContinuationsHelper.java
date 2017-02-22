package ua.com.fielden.platform.entity;

import java.util.Map;

import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarnings;
import ua.com.fielden.platform.error.Result;

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
    public static <T extends AbstractEntity<?>> T saveWithContinuations(final T entity, final Map<String, IContinuationData> continuations, final CommonEntityDao<T> co) {
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

}
