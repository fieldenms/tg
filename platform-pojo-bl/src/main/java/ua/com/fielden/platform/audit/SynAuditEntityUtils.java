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
     * @param customYields  map of yields, of the form {alias : value};
     *                      these are properties that are present in prior audit-entity types but absent in the current one.
     */
    public static <T extends AbstractSynAuditEntity<?>> EntityResultQueryModel<T> mkModelCurrent(
            final Class<T> synAuditEntityType,
            final Class<? extends AbstractAuditEntity<?>> auditEntityType,
            final Set<String> yields,
            final Map<String, Object> customYields)
    {
        final var part = EntityQueryUtils.select(auditEntityType);

        // This part is a bit tricky because we need to yield at least one property to get to the right fluent interface.
        if (!yields.isEmpty()) {
            final var yieldsIter = yields.iterator();
            final var yield0 = yieldsIter.next();
            return mkModelCurrent_(part.yield().prop(yield0).as(yield0),
                                 yieldsIter,
                                 customYields.entrySet().iterator())
                    .modelAsEntity(synAuditEntityType);
        }
        else if (!customYields.isEmpty()) {
            final var customYieldsIter = customYields.entrySet().iterator();
            final var customYield0 = customYieldsIter.next();
            return mkModelCurrent_(part.yield().val(customYield0.getValue()).as(customYield0.getKey()),
                                 yields.iterator(),
                                 customYieldsIter)
                    .modelAsEntity(synAuditEntityType);
        }
        else {
            return part.modelAsEntity(synAuditEntityType);
        }
    }

    private static ISubsequentCompletedAndYielded<? extends AbstractAuditEntity<?>> mkModelCurrent_(
            ISubsequentCompletedAndYielded<? extends AbstractAuditEntity<?>> part,
            final Iterator<String> yieldsIter,
            final Iterator<Map.Entry<String, Object>> customYieldsIter)
    {
        while (yieldsIter.hasNext()) {
            final var next = yieldsIter.next();
            part = part.yield().prop(next).as(next);
        }

        while (customYieldsIter.hasNext()) {
            final var next = customYieldsIter.next();
            part = part.yield().val(next.getValue()).as(next.getKey());
        }

        return part;
    }

    /**
     * Assists with construction of a synthetic model.
     * Creates an EQL query for a "prior" audit-entity type (whose version is less than the latest one).
     *
     * @param synAuditEntityType  synthetic audit-entity type
     * @param auditEntityType  prior audit-entity type
     * @param yields  map of yields, of the form {alias : value};
     *                these are properties that are present in the current audit-entity type but absent in the prior one.
     * @param otherYields  names of properties that are yielded as usual; these are properties that are present in both
     *                     prior and current audit-entity types.
     */
    public static <T extends AbstractSynAuditEntity<?>> EntityResultQueryModel<T> mkModelPrior(
            final Class<T> synAuditEntityType,
            final Class<? extends AbstractAuditEntity<?>> auditEntityType,
            final Map<String, Object> yields,
            final Set<String> otherYields)
    {
        final var part = EntityQueryUtils.select(auditEntityType);

        // This part is a bit tricky because we need to yield at least one property to get to the right fluent interface.
        if (!yields.isEmpty()) {
            final var yieldsIter = yields.entrySet().iterator();
            final var yield0 = yieldsIter.next();
            return mkModelPrior_(part.yield().val(yield0.getValue()).as(yield0.getKey()),
                                 yieldsIter,
                                 otherYields.iterator())
                    .modelAsEntity(synAuditEntityType);
        }
        if (!otherYields.isEmpty()) {
            final var otherYieldsIter = otherYields.iterator();
            final var otherYields0 = otherYieldsIter.next();
            return mkModelPrior_(part.yield().prop(otherYields0).as(otherYields0),
                                 yields.entrySet().iterator(),
                                 otherYieldsIter)
                    .modelAsEntity(synAuditEntityType);
        }
        else {
            return part.modelAsEntity(synAuditEntityType);
        }
    }

    private static ISubsequentCompletedAndYielded<? extends AbstractAuditEntity<?>> mkModelPrior_(
            ISubsequentCompletedAndYielded<? extends AbstractAuditEntity<?>> part,
            final Iterator<Map.Entry<String, Object>> yieldsIter,
            final Iterator<String> otherYieldsIter)
    {
        while (yieldsIter.hasNext()) {
            final var next = yieldsIter.next();
            part = part.yield().val(next.getValue()).as(next.getKey());
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
     * {@link #mkModelCurrent(Class, Class, Set, Map)} and {@link #mkModelPrior(Class, Class, Map, Set)}, respectively.
     * Queries for prior audit-entity types <b>must be sorted</b> by their versions in a descending order (this is not checked).
     */
    @SafeVarargs
    public static <T extends AbstractEntity<?>> EntityResultQueryModel<T> combineModels(
            final Class<? extends AbstractSynAuditProp<?>> synAuditPropType,
            final EntityResultQueryModel<T> currentModel,
            final EntityResultQueryModel<T>... priorModels)
    {
        return EntityQueryUtils.select(concatList(List.of(currentModel), Arrays.asList(priorModels)).toArray((EntityResultQueryModel<T>[]) new EntityResultQueryModel[] {}))
                .where()
                .critCondition(EntityQueryUtils.select(synAuditPropType).where().prop(AbstractSynAuditProp.AUDIT_ENTITY).eq().extProp(AbstractEntity.ID),
                               AbstractSynAuditProp.PROPERTY, AbstractSynAuditEntity.CHANGED_PROPS_CRIT)
                .model();
    }

    private SynAuditEntityUtils() {}

}
