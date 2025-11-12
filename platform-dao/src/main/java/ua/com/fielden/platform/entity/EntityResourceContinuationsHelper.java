package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarnings;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ua.com.fielden.platform.utils.Pair.pair;

public class EntityResourceContinuationsHelper {

    /// Saves the `entity` with its `continuations`.
    /// In case where warnings exist, `continuations` should include respective warnings acknowledgement continuation if it was accepted by the user.
    /// Could throw continuations exceptions, a "no changes" exception, or validation exceptions.
    ///
    private static <T extends AbstractEntity<?>> T saveWithContinuations(final T entity, final Map<String, IContinuationData> continuations, final CommonEntityDao<T> co) {
        if (validateWithoutCritOnlyRequired(entity)
            && entity.warnings().stream().anyMatch(EntityResourceUtils::isNonConflicting))
        {
            final String acknowledgementContinuationName = "_acknowledgedForTheFirstTime";
            if (continuations.get(acknowledgementContinuationName) == null) {
                throw new NeedMoreData("Warnings need acknowledgement", AcknowledgeWarnings.class, acknowledgementContinuationName);
            } else {
                entity.nonProxiedProperties().forEach(MetaProperty::clearWarnings);
            }
        }
        
        // 1. Non-persistent entities should always be saved (`isDirty` will always be true).
        // 2. Persistent but not persisted (new) entities should always be saved (isDirty will always be true).
        // 3. Persistent+persisted+dirty (by means of dirty properties existence) entities should always be saved.
        // 4. Persistent+persisted+notDirty+inValid entities should always be saved: passed to companion 'save' method to process validation errors in domain-driven way by companion object itself.
        // 5. Persistent+persisted+notDirty+valid entities saving should be skipped – simply return the entity.
        if (!entity.isDirty() && validateWithoutCritOnlyRequired(entity)) { // `validateWithoutCritOnlyRequired` does not perform validation, it only checks the current validation results
            return entity;
        }
        
        try {
            // Set continuations before saving, even when the map is empty.
            co.setMoreData(continuations);
            return co.save(entity);
        } finally {
            // Clear continuations regardless of whether the save operation succeeds or fails.
            co.clearMoreData();
        }
    }
    
    /// Validates entity skipping required checks for its properties that are both [CritOnly] and [Required], and not skipping these checks for other properties.
    ///
    private static <T extends AbstractEntity<?>> boolean validateWithoutCritOnlyRequired(final T entity) {
        // Iterate over properties in search of the first invalid one with required checks, but not for @CritOnly properties.
        final java.util.Optional<Result> firstFailure = entity.nonProxiedProperties()
                .filter(mp -> !mp.isValidWithRequiredCheck(true) && mp.getFirstFailure() != null)
                .findFirst().map(MetaProperty::getFirstFailure);
        
        // Returns the first failure if present or a successful result otherwise.
        return firstFailure.orElseGet(Result::successful).isSuccessful();
    }
    
    /// Performs saving of `validatedEntity`.
    ///
    /// IMPORTANT:
    /// If `validatedEntity` has been mutated during saving by its companion object,
    /// the mutated entity instance is returned in case of an exceptional situation and bound to a corresponding entity master.
    /// The toast message shows the exception, not the first validation error of the entity.
    ///
    /// @return if saving was successful -- returns saved entity without any exception,
    ///         if saving was unsuccessful due to some exception -- returns `validatedEntity` (to be bound to the appropriate entity master)
    ///         and the thrown exception (to be displayed as a toast message)
    ///
    public static <T extends AbstractEntity<?>> Pair<T, Optional<Exception>> saveWithContinuations(final T validatedEntity, final Map<String, IContinuationData> continuations, final IEntityDao<T> companion) {
        T savedEntity;
        try {
            // Try to save the entity with its companion 'save' method.
            savedEntity = saveWithContinuations(validatedEntity, continuations, (CommonEntityDao<T>) companion);
        } catch (final Exception exception) {
            // Some exception can be thrown inside 1) its companion 'save' method OR 2) CommonEntityDao 'save' during its internal validation.
            // Return entity back to the client after its unsuccessful save with the exception that was thrown during saving.
            return pair(validatedEntity, Optional.of(exception));
        }
    
        return pair(savedEntity, Optional.empty());
    }
    
    /// Creates map of continuations by continuation keys.
    ///
    public static Map<String, IContinuationData> createContinuationsMap(final List<IContinuationData> continuations, final List<String> continuationProperties) {
        final Map<String, IContinuationData> map = new LinkedHashMap<>();
        for (int index = 0; index < continuations.size(); index++) {
            map.put(continuationProperties.get(index), continuations.get(index));
        }
        return map;
    }
    
}
