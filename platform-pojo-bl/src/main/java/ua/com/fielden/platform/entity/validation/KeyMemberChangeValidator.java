package ua.com.fielden.platform.entity.validation;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyEntry;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyLevel;
import ua.com.fielden.platform.ref_hierarchy.TypeLevelHierarchyEntry;
import ua.com.fielden.platform.types.tuples.T2;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.Math.max;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static ua.com.fielden.platform.error.Result.*;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.MessageUtils.singleOrPlural;


/**
 * A validator for "key" and key-member properties of persistent entities that produces a warning upon value change.
 * Intended to be used on reference (aka master) data.
 *
 * @author TG Team
 */
public class KeyMemberChangeValidator extends AbstractBeforeChangeEventHandler<Object> {

    public static final String KEY_MEMBER_CHANGE_MESSAGE = "Saving this change will be reflected in all the data referencing this %s.";

    private final ICompanionObjectFinder coFinder;

    @Inject
    public KeyMemberChangeValidator(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        final AbstractEntity<?> entity = property.getEntity();
        if (!entity.isPersistent() || !entity.isPersisted())
            return successful(newValue);

        IEntityDao<ReferenceHierarchy> refHierarchyCo = coFinder.find(ReferenceHierarchy.class);
        ReferenceHierarchy refChy = refHierarchyCo.new_();
        refChy.setLoadedHierarchyLevel(ReferenceHierarchyLevel.REFERENCE_BY_INSTANCE);
        refChy.setRefEntityId(entity.getId());
        refChy.setRefEntityType(entity.getType().getName());
        ReferenceHierarchy savedRefChy = refHierarchyCo.save(refChy);
        List<AbstractEntity<?>> generatedChy = savedRefChy.getGeneratedHierarchy();

        List<TypeLevelHierarchyEntry> typeEntries = generatedChy.stream()
                .map(x -> {
                    if (x instanceof ReferenceHierarchyEntry entry && ReferenceHierarchyLevel.REFERENCED_BY == entry.getHierarchyLevel())
                        return entry;
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(entry -> entry.getChildren().stream())
                .map(entry -> entry instanceof TypeLevelHierarchyEntry typeEntry ? typeEntry : null)
                .filter(Objects::nonNull)
                .toList();

        if (typeEntries.isEmpty()) {
            return successful(newValue);
        }

        final String entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
        return warningEx(newValue, KEY_MEMBER_CHANGE_MESSAGE.formatted(entityTitle), makeMessageEx(entity, typeEntries));
    }

    private String makeMessageEx(final AbstractEntity<?> entity, final List<TypeLevelHierarchyEntry> entries) {
        final T2<Integer, Integer> lengths = entries.stream()
                .map(entry -> t2(entry.getEntityType().length(), entry.getNumberOfEntities().toString().length()))
                .reduce(t2(0, 0), (accum, val) -> t2(max(accum._1, val._1), max(accum._2, val._2)));
        final String rows = entries.stream()
                .map(entry -> {
                    final Class<? extends AbstractEntity<?>> entityType;
                    try {
                        entityType = (Class<? extends AbstractEntity<?>>) Class.forName(entry.getEntityType());
                    } catch (ClassNotFoundException|ClassCastException e) {
                        throw failure(new IllegalStateException("Entity type [%s] could not be found.".formatted(entry.getEntityType())));
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

    private static String makeEntryMsg(final String s1, String s2, final T2<Integer, Integer> lengths) {
        final String padStr = "\u00A0";
        return format("<tt>%s\u00A0%s</tt>",
                rightPad(s1, lengths._1 + 2, padStr),
                rightPad(s2, lengths._2 + 2, padStr));
    }

}
