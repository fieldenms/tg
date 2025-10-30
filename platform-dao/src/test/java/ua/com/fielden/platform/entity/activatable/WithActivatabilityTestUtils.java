package ua.com.fielden.platform.entity.activatable;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.utils.ArrayUtils;

import java.util.Arrays;
import java.util.function.Supplier;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// A mixin that provides utilities for testing entity activatability.
///
interface WithActivatabilityTestUtils {

    <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co(final Class<E> type);

    <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co$(final Class<E> type);

    default <E extends AbstractEntity<?>> E refetch$(final E entity, final fetch<E> fetch) {
        return co$((Class<E>) entity.getType()).findByEntityAndFetch(fetch, entity);
    }

    default <E extends AbstractEntity<?>> E refetch$(final E entity, final fetch.FetchCategory category) {
        return refetch$(entity, new fetch<E>((Class<E>) entity.getType(), category));
    }

    default <E extends AbstractEntity<?>> E refetch$(final E entity) {
        return refetch$(entity, fetch.FetchCategory.DEFAULT);
    }

    /// Refetches `entity` with property `refCount` and asserts that it is equal to the `expected` value.
    ///
    default void assertRefCount(final int expected, final ActivatableAbstractEntity<?> entity) {
        assertRefCount(() -> "refCount for [%s] %s".formatted(getEntityTitleAndDesc(entity).getKey(), entity),
                       expected, entity);
    }

    default void assertRefCount(final String message, final int expected, final ActivatableAbstractEntity<?> entity) {
        assertRefCount(() -> message, expected, entity);
    }

    default void assertRefCount(final Supplier<String> messageSupplier, final int expected, final ActivatableAbstractEntity<?> entity) {
        assertNotNull(entity);
        final Class<ActivatableAbstractEntity<?>> entityType = (Class<ActivatableAbstractEntity<?>>) entity.getType();
        final int actual = co(entityType).findByEntityAndFetch(fetchNone(entityType).with(REF_COUNT), entity).getRefCount();
        assertThat(actual)
                .describedAs(messageSupplier)
                .isEqualTo(expected);
    }

    default void assertRefCount(final int expected, final Class<? extends ActivatableAbstractEntity<?>> entityType, final Object... key) {
        assertRefCount(() -> "refCount for [%s] %s".formatted(getEntityTitleAndDesc(entityType).getKey(), Arrays.stream(key).map(Object::toString).collect(joining(" "))),
                       expected, entityType, key);
    }

    default void assertRefCount(final String message, final int expected, final Class<? extends ActivatableAbstractEntity<?>> entityType, final Object... key) {
        assertRefCount(() -> message, expected, entityType, key);
    }

    default <E extends ActivatableAbstractEntity<?>> void assertRefCount(final Supplier<String> messageSupplier, final int expected, final Class<E> entityType, final Object... key) {
        assertNotNull(key);
        assertFalse("Key must not contain nulls.", ArrayUtils.contains(key, null));

        final int actual = co(entityType).findByKeyAndFetch(fetchNone(entityType).with(REF_COUNT), key).getRefCount();
        assertThat(actual)
                .describedAs(messageSupplier)
                .isEqualTo(expected);
    }
    @SuppressWarnings("unchecked")
    static <E extends AbstractEntity<?>> E setProperties(final ICanSetProperty setter, final E entity, final CharSequence prop1, final Object val1, final Object... rest) {
        if (rest.length % 2 != 0) {
            throw new InvalidArgumentException("[rest] must have even length: %s".formatted(rest.length));
        }

        setter.setProperty(entity, prop1, val1);

        for (var i = 0; i < rest.length; i+=2) {
            final CharSequence prop = (CharSequence) rest[i];
            setter.setProperty(entity, prop, rest[i+1]);
        }

        return entity;
    }

    interface ICanSetProperty {

        default <E extends AbstractEntity<?>> E setProperty(final E entity, final CharSequence prop, final Object value) {
            entity.set(prop.toString(), value);
            return entity;
        }

    }

}
