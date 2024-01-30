package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ref_hierarchy.*;
import ua.com.fielden.platform.types.tuples.T2;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import static java.lang.Math.max;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static ua.com.fielden.platform.error.Result.*;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.MessageUtils.singleOrPlural;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

/**
 * A validator for key properties that produces a warning upon modification of the key's value.
 * Applicable only to persisted entities (i.e., saved instances of persistent entity types).
 * <p>
 * It is intended to be used in application to the reference (aka master) data. The rationale is to make the originator of the change aware
 * of the consequences -- namely, that their change will affect all the data referencing the entity being changed.
 * <p>
 * This validator is applied by default. Therefore, it is not required to specify it in property definitions explicitly.
 *
 * @author TG Team
 */
public class KeyMemberChangeValidator extends AbstractBeforeChangeEventHandler<Object> {

    public static final String KEY_MEMBER_CHANGE_MESSAGE = "Saving this change will be reflected in all the data referencing this %s.";

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        final AbstractEntity<?> entity = property.getEntity();
        if (!entity.isPersistent() || !entity.isPersisted()) {
            return successful(newValue);
        }

        final IReferenceHierarchy coReferenceHierarchy = co(ReferenceHierarchy.class);
        final var refChy = coReferenceHierarchy.new_();
        refChy.beginInitialising();
        refChy.setLoadedHierarchyLevel(ReferenceHierarchyLevel.REFERENCE_BY_INSTANCE);
        refChy.setRefEntityId(entity.getId());
        refChy.setRefEntityType(entity.getType().getName());
        refChy.endInitialising();

        final var savedRefChy = coReferenceHierarchy.save(refChy);

        final List<TypeLevelHierarchyEntry> typeEntries = savedRefChy.getGeneratedHierarchy().stream()
                .mapMulti(typeFilter(ReferenceHierarchyEntry.class))
                .filter(entry -> ReferenceHierarchyLevel.REFERENCED_BY == entry.getHierarchyLevel())
                .flatMap(entry -> entry.getChildren().stream())
                .mapMulti(typeFilter(TypeLevelHierarchyEntry.class))
                .toList();

        if (typeEntries.isEmpty()) {
            return successful(newValue);
        }

        final String entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
        return warningEx(newValue, KEY_MEMBER_CHANGE_MESSAGE.formatted(entityTitle), makeMessageEx(entity, typeEntries));
    }

    private static String makeMessageEx(final AbstractEntity<?> entity, final List<TypeLevelHierarchyEntry> entries) {
        final T2<Integer, Integer> lengths = entries.stream()
                .map(entry -> t2(entry.getEntityType().length(), entry.getNumberOfEntities().toString().length()))
                .reduce(t2(0, 0), (accum, val) -> t2(max(accum._1, val._1), max(accum._2, val._2)));
        final String rows = entries.stream()
                .map(entry -> {
                    final Class<? extends AbstractEntity<?>> entityType;
                    try {
                        entityType = (Class<? extends AbstractEntity<?>>) Class.forName(entry.getEntityType());
                    } catch (final ClassNotFoundException|ClassCastException e) {
                        throw failure(new EntityException("Entity type [%s] could not be found.".formatted(entry.getEntityType())));
                    }
                    return makeEntryMsg(getEntityTitleAndDesc(entityType).getKey(), entry.getNumberOfEntities().toString(), lengths);
                })
                .collect(joining("\n<br>"));
        final String columns = makeEntryMsg("Entity", "Qty", lengths);
        final String entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
        final int totalRefs = entries.stream().map(TypeLevelHierarchyEntry::getNumberOfEntities).reduce(0, Integer::sum);
        return format("%s%n<br>%s:%n%n<br><br>%s<hr>%n<br>%s",
                KEY_MEMBER_CHANGE_MESSAGE.formatted(entityTitle),
                "%s [%s] is referenced by %s %s".formatted(entityTitle, entity, totalRefs, singleOrPlural(totalRefs, "entity", "entities")),
                columns, rows);
    }

    private static String makeEntryMsg(final String s1, final String s2, final T2<Integer, Integer> lengths) {
        final String padStr = "\u00A0";
        return format("<tt>%s\u00A0%s</tt>",
                rightPad(s1, lengths._1 + 2, padStr),
                rightPad(s2, lengths._2 + 2, padStr));
    }

}
