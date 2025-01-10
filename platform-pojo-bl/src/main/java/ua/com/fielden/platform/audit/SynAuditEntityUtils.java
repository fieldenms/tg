package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.*;

import static ua.com.fielden.platform.utils.CollectionUtil.concatList;

/**
 * Static utitlies used by generated synthetic audit-entities.
 */
public final class SynAuditEntityUtils {

    /**
     * Assists with construction of a synthetic model.
     * Creates an EQL query for the "current" audit-entity type (the one with the latest type version).
     *
     * @param synAuditEntityType  synthetic audit-entity type
     * @param auditEntityType  current audit-entity type
     * @param nullYields  names of properties into which {@code null} is yielded; these are properties that are present
     *                    in prior audit-entity types but absent in the current one.
     */
    public static <T extends AbstractSynAuditEntity<?>> EntityResultQueryModel<T> mkModelCurrent(
            final Class<T> synAuditEntityType,
            final Class<? extends AbstractAuditEntity<?>> auditEntityType,
            final Set<String> nullYields)
    {
        var part = EntityQueryUtils.select(auditEntityType)
                .yieldAll();
        for (String prop : nullYields) {
            part = part.yield().val(null).as(prop);
        }
        return part.modelAsEntity(synAuditEntityType);
    }

    /**
     * Assists with construction of a synthetic model.
     * Creates an EQL query for a "prior" audit-entity type (whose version is less than the latest one).
     *
     * @param synAuditEntityType  synthetic audit-entity type
     * @param auditEntityType  prior audit-entity type
     * @param nullYields  names of properties into which {@code null} is yielded; these are properties that are present
     *                    in the current audit-entity type but absent in the prior one.
     * @param renamedYields  names of properties that are yielded under different names (keys are old names, values are new names);
     *                       these are properties that are present in the prior audit-entity type but absent in the current one.
     * @param otherYields  names of properties that are yielded as usual; these are properties that are present in both
     *                     prior and current audit-entity types.
     */
    public static <T extends AbstractSynAuditEntity<?>> EntityResultQueryModel<T> mkModelPrior(
            final Class<T> synAuditEntityType,
            final Class<? extends AbstractAuditEntity<?>> auditEntityType,
            final Set<String> nullYields,
            final Map<String, String> renamedYields,
            final Set<String> otherYields)
    {
        final var part = EntityQueryUtils.select(auditEntityType);

        // This part is a bit tricky because we need to yield at least one property to get to the right fluent interface.
        if (!nullYields.isEmpty()) {
            final var nullYieldsIter = nullYields.iterator();
            return mkModelPrior_(part.yield().val(null).as(nullYieldsIter.next()),
                                 nullYieldsIter,
                                 renamedYields.entrySet().iterator(),
                                 otherYields.iterator())
                    .modelAsEntity(synAuditEntityType);
        }
        else if (!renamedYields.isEmpty()) {
            final var renamedYieldsIter = renamedYields.entrySet().iterator();
            final var renamedYield0 = renamedYieldsIter.next();
            return mkModelPrior_(part.yield().prop(renamedYield0.getKey()).as(renamedYield0.getValue()),
                                 nullYields.iterator(),
                                 renamedYieldsIter,
                                 otherYields.iterator())
                    .modelAsEntity(synAuditEntityType);
        }
        if (!otherYields.isEmpty()) {
            final var otherYieldsIter = otherYields.iterator();
            final var otherYields0 = otherYieldsIter.next();
            return mkModelPrior_(part.yield().prop(otherYields0).as(otherYields0),
                                 nullYields.iterator(),
                                 renamedYields.entrySet().iterator(),
                                 otherYieldsIter)
                    .modelAsEntity(synAuditEntityType);
        }
        else {
            return part.modelAsEntity(synAuditEntityType);
        }
    }

    private static ISubsequentCompletedAndYielded<? extends AbstractAuditEntity<?>> mkModelPrior_(
            ISubsequentCompletedAndYielded<? extends AbstractAuditEntity<?>> part,
            final Iterator<String> nullYieldsIter,
            final Iterator<Map.Entry<String, String>> renamedYieldsIter,
            final Iterator<String> otherYieldsIter)
    {
        while (nullYieldsIter.hasNext()) {
            part = part.yield().val(null).as(nullYieldsIter.next());
        }

        while (renamedYieldsIter.hasNext()) {
            final var next = renamedYieldsIter.next();
            part = part.yield().prop(next.getKey()).as(next.getValue());
        }

        while (otherYieldsIter.hasNext()) {
            final var next = otherYieldsIter.next();
            part = part.yield().prop(next).as(next);
        }

        return part;
    }

    /**
     * Assists with construction of a synthetic model.
     * Creates the definitive EQL query to be assigned to the {@code model_} field.
     * The arguments should be queries for the current audit-entity type and prior ones, constructed with
     * {@link #mkModelCurrent(Class, Class, Set)} and {@link #mkModelPrior(Class, Class, Set, Map, Set)}, respectively.
     * Queries for prior audit-entity types <b>must be sorted</b> by their versions in a descending order (this is not checked).
     */
    @SafeVarargs
    public static <T extends AbstractEntity<?>> EntityResultQueryModel<T> combineModels(
            final EntityResultQueryModel<T> currentModel,
            final EntityResultQueryModel<T>... priorModels
    ) {
        final var models = concatList(List.of(currentModel), Arrays.asList(priorModels)).toArray(EntityResultQueryModel[]::new);
        return EntityQueryUtils.select(models).model();
    }

    private SynAuditEntityUtils() {}

}
