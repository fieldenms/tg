package ua.com.fielden.platform.meta;

import jakarta.annotation.Nullable;

import java.util.Optional;

import static java.lang.Boolean.FALSE;

/**
 * Represents property metadata.
 * <p>
 * Each property gets classified according to its nature.
 * Nature determines the essential metadata attributed to a property.
 * This essential data is accessible via {@link #data()}.
 * Matching on the property's nature can be performed with the {@code switch} statement or via {@link #match(PropertyMetadataVisitor)}.
 *
 * <h3> Arbitrary Metadata </h3>
 * A property might have arbitrary additional information associated with it, which is modelled with key-value pairs.
 * At the moment, only general-purpose keys {@link AnyKey} that are applicable to properties of all natures are supported.
 * Values can be accessed via {@link #get(AnyKey)}.
 * <p>
 * It is possible that at some stage specialised keys, applicable to properties of a specific nature, would become required.
 * There is already some commented out code to support such keys. Please refer to the <i>TODO</i> entries in the source.
 * </ul>
 * The purpose of this mechanism is to represent optional information.
 *
 * @see PropertyNature
 * @see PropertyMetadataKeys
 */
public sealed interface PropertyMetadata extends Comparable<PropertyMetadata> {

    String name();

    PropertyTypeMetadata type();

    /**
     * The Hibernate type of this property.
     * </p>
     * Not all properties have a corresponding Hibernate type. It applies only to those properties that are used in
     * interactions with the database:
     * <ul>
     *   <li> Persistent properties.
     *   <li> Calculated properties whose type can be mapped onto the database. This excludes collectional properties,
     *        for example.
     * </ul>
     */
    // TODO encapsulate Hibernate type access in a separate abstraction once dependency injection is properly configured
    @Nullable Object hibType();

    PropertyNature nature();

    PropertyNature.Data data();

    <R> R match(PropertyMetadataVisitor<R> visitor);

    // TODO: If nature-specific arbitrary key-value metadata would become required, this method would need to be uncommented.
    // NOTE: if a need for specialised keys arises, this interface will have to be parameterised with PropertyNature.
    // <V> Optional<V> get(Key<V, N> key);

    <V> Optional<V> get(AnyKey<V> key);

    /**
     * If a key is present, returns a proof of its presence.
     *
     * @see PropertyMetadataWithKey
     */
    <K extends AnyKey<V>, V> Optional<PropertyMetadataWithKey<K, V>> withKey(K key);

    // TODO: If nature-specific arbitrary key-value metadata would become required, this method would need to be uncommented.
    // default boolean is(Key<Boolean, N> key) {
    //     return get(key).orElse(FALSE);
    // }

    default boolean is(AnyKey<Boolean> key) {
        return get(key).orElse(FALSE);
    }

    default boolean has(AnyKey<?> key) {
        return get(key).isPresent();
    }

    @Override
    default int compareTo(final PropertyMetadata that) {
        return name().compareTo(that.name());
    }

    default boolean isPersistent() {
        return this instanceof Persistent;
    }

    default boolean isCalculated() {
        return this instanceof Calculated;
    }

    default boolean isCritOnly() {
        return this instanceof CritOnly;
    }

    default boolean isTransient() {
        return this instanceof Transient;
    }

    default boolean isPlain() {
        return this instanceof Plain;
    }

    // ****************************************
    // * Convenient methods as an alternative to a visitor with a single clause

    default Optional<Persistent> asPersistent() {
        return this instanceof Persistent p ? Optional.of(p) : Optional.empty();
    }

    default Optional<Calculated> asCalculated() {
        return this instanceof Calculated c ? Optional.of(c) : Optional.empty();
    }

    default Optional<CritOnly> asCritOnly() {
        return this instanceof CritOnly c ? Optional.of(c) : Optional.empty();
    }

    default Optional<Transient> asTransient() {
        return this instanceof Transient t ? Optional.of(t) : Optional.empty();
    }

    default Optional<Plain> asPlain() {
        return this instanceof Plain p ? Optional.of(p) : Optional.empty();
    }

    non-sealed interface Persistent extends PropertyMetadata {
        @Override
        PropertyNature.Persistent nature();

        @Override
        PropertyNature.Persistent.Data data();
    }

    non-sealed interface Calculated extends Transient {
        @Override
        PropertyNature.Calculated nature();

        @Override
        PropertyNature.Calculated.Data data();
    }

    sealed interface Transient extends PropertyMetadata {
        @Override
        PropertyNature.Transient nature();
    }

    non-sealed interface CritOnly extends Transient {
        @Override
        PropertyNature.CritOnly nature();

        @Override
        PropertyNature.CritOnly.Data data();
    }

    non-sealed interface Plain extends Transient {
        @Override
        PropertyNature.Plain nature();
    }

    // TODO: If nature-specific arbitrary key-value metadata would become required, this type should be used to model a key.
    // interface Key<V, N extends PropertyNature> extends IKey {}

    interface AnyKey<V> extends IKey {}

    /**
     * Marker interface for property metadata keys.
     */
    interface IKey {}

}
