package ua.com.fielden.platform.minheritance;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Extends;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyTypeMetadata;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.either.Right;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.ArrayUtils;
import ua.com.fielden.platform.utils.StreamUtils;

import java.lang.reflect.Method;
import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.minheritance.MultiInheritanceCommon.EXCLUDED_PROPERTIES;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.reflection.Reflector.getMethod;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/// Generates EQL models for generated multi-inheritance synthetic entity types.
///
/// **Not a part of public API**.
///
@Singleton
public class MultiInheritanceEqlModelGenerator {

    private final IDomainMetadata domainMetadata;

    @Inject
    MultiInheritanceEqlModelGenerator(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    public <E extends AbstractEntity<?>> List<EntityResultQueryModel<E>> generate(final Class<E> type) {
        if (!isGeneratedMultiInheritanceEntityType(type)) {
            throw new InvalidArgumentException("[type] must be a generated multi-inheritance entity type: [%s]".formatted(type));
        }

        final var specType = type.getSuperclass();
        final var atExtends = getAnnotation(specType, Extends.class);
        if (atExtends == null) {
            throw new EntityDefinitionException(format(
                    "Specification entity type [%s] is missing required annotation @%s",
                    specType.getCanonicalName(), Extends.class.getCanonicalName()));
        }

        final var supertypes = Arrays.stream(atExtends.value()).map(Extends.Entity::value).toList();
        final var inheritedProperties = allInheritedProperties(type, atExtends);

        final Optional<Method> maybeMethod_modelFor = Try(() -> getMethod(type, "modelFor", Class.class, IFromAlias.class)) instanceof Right<?, Method>(var method)
                ? Optional.of(method).map(it -> { it.setAccessible(true); return it; })
                : Optional.empty();

        return supertypes.stream()
                .map(ty -> {
                    final var part1 = maybeMethod_modelFor
                            .<ISubsequentCompletedAndYielded<?>>map(modelFor -> {
                                // It is an error if `initPart` yields into a property that is in `inheritedProperties`,
                                // because this will result in a query with 2 yields that have the same alias.
                                // Compilation of such a query by the EQL engine will fail with a generic error message.
                                // Ideally, we would validate `initPart` here, but this is not possible at the moment,
                                // because EqlQueryTransformer would be required, which would create a circular dependency:
                                // MultiInheritanceEqlModelGenerator -> EqlQueryTransformer -> QuerySourceInfoProvider -> SyntheticModelProvider -> MultiInheritanceEqlModelGenerator
                                final ISubsequentCompletedAndYielded<?> initPart = invokeStatic(modelFor, ty, select(ty));
                                return yieldProperties(initPart, ty, inheritedProperties);
                            })
                            .orElseGet(() -> yieldProperties(select(ty), ty, inheritedProperties));
                    final var part2 = part1.yield().val(ty.getCanonicalName()).as(atExtends.entityTypeCarrierProperty());
                    return part2.modelAsEntity(type);
                })
                .collect(toImmutableList());
    }

    private ISubsequentCompletedAndYielded<?> yieldProperties(
            final ISubsequentCompletedAndYielded<?> part,
            final Class<? extends AbstractEntity<?>> sourceType,
            final Set<T2<? extends Class<? extends AbstractEntity<?>>, String>> properties)
    {
        return yieldProperties(part, sourceType, properties.iterator());
    }

    private ISubsequentCompletedAndYielded<?> yieldProperties(
            final IFromAlias<?> part,
            final Class<? extends AbstractEntity<?>> sourceType,
            final Set<T2<? extends Class<? extends AbstractEntity<?>>, String>> properties)
    {
        final var propertiesIter = properties.iterator();
        final var prop0 = propertiesIter.next();
        final var nextPart = yieldProp(domainMetadata.forEntity(sourceType), prop0._1, prop0._2, part);
        return yieldProperties(nextPart, sourceType, propertiesIter);
    }

    private ISubsequentCompletedAndYielded<?> yieldProperties(
            final ISubsequentCompletedAndYielded<?> part,
            final Class<? extends AbstractEntity<?>> sourceType,
            final Iterator<T2<? extends Class<? extends AbstractEntity<?>>, String>> properties)
    {
        final var entityMetadata = domainMetadata.forEntity(sourceType);
        var result = part;
        while (properties.hasNext()) {
            final var next = properties.next();
            result = yieldProp(entityMetadata, next._1, next._2, result);
        }
        return result;
    }

    private ISubsequentCompletedAndYielded<?> yieldProp(
            final EntityMetadata sourceEntityMetadata,
            final Class<? extends AbstractEntity<?>> ownerType,
            final String name,
            final ISubsequentCompletedAndYielded<?> part)
    {
        return hasProperty(sourceEntityMetadata, name)
                ? part.yield().prop(name).as(name)
                : part.yield().val(defaultPropertyValue(ownerType, name)).as(name);
    }

    private ISubsequentCompletedAndYielded<?> yieldProp(
            final EntityMetadata sourceEntityMetadata,
            final Class<? extends AbstractEntity<?>> ownerType,
            final String name,
            final IFromAlias<?> part)
    {
        return hasProperty(sourceEntityMetadata, name)
                ? part.yield().prop(name).as(name)
                : part.yield().val(defaultPropertyValue(ownerType, name)).as(name);
    }

    private @Nullable Object defaultPropertyValue(final Class<? extends AbstractEntity<?>> ownerType, final CharSequence prop) {
        final var propTypeMetadata = domainMetadata.forProperty(ownerType, prop).type();
        return switch (propTypeMetadata) {
            case PropertyTypeMetadata.Component $ -> null;
            case PropertyTypeMetadata.Entity $ -> null;
            case PropertyTypeMetadata.Primitive it -> it.javaType().equals(boolean.class) ? false : null;
            default -> throw new InvalidStateException(format(
                    "Cannot determine the default value for inherited property [%s.%s] with type [%s].",
                    ownerType.getCanonicalName(), prop, propTypeMetadata));
        };
    }

    private Set<T2<? extends Class<? extends AbstractEntity<?>>, String>> allInheritedProperties(
            final Class<? extends AbstractEntity<?>> multiInheritanceType,
            final Extends atExtends)
    {
        final var multiInheritanceEntityMetadata = domainMetadata.forEntity(multiInheritanceType);

        return StreamUtils.distinct(
                        Arrays.stream(atExtends.value())
                                .flatMap(atEntity -> domainMetadata.forEntity(atEntity.value())
                                        .properties()
                                        .stream()
                                        .filter(prop -> !EXCLUDED_PROPERTIES.contains(prop.name()))
                                        .filter(prop -> !ArrayUtils.contains(atEntity.exclude(), prop.name()))
                                        // Check if ID is really present.
                                        .filter(prop -> !ID.equals(prop.name()) || hasId(atEntity.value()))
                                        // Currently, this handles only the special case of `desc`.
                                        // The extended entity type could have `desc`, but the spec-entity type could
                                        // lack it, hence the multi-inheritance entity type would also lack it.
                                        .filter(prop -> multiInheritanceEntityMetadata.hasProperty(prop.name()))
                                        .map(prop -> t2(atEntity.value(), prop.name()))),
                        pair -> pair._2)
                .collect(toImmutableSet());
    }

    private boolean hasProperty(final EntityMetadata entityMetadata, final CharSequence propName) {
        if (!entityMetadata.hasProperty(propName.toString())) {
            return false;
        }
        else if (ID.contentEquals(propName)) {
            return hasId(entityMetadata.javaType());
        }
        else return true;
    }

    private boolean hasId(final Class<? extends AbstractEntity<?>> type) {
        if (isPersistentEntityType(type) || isSyntheticBasedOnPersistentEntityType(type)) {
            return true;
        }
        else if (isSyntheticEntityType(type)) {
            return isPropertyPresent(type, ID);
        }
        else return false;
    }

    @SuppressWarnings("unchecked")
    private static <R> R invokeStatic(final Method method, final Object... args) {
        try {
            return (R) method.invoke(null, args);
        } catch (final Exception ex) {
            throw new ReflectionException(ex);
        }
    }

}
