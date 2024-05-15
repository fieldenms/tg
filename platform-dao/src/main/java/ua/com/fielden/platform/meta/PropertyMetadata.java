package ua.com.fielden.platform.meta;

import java.util.Optional;

import static java.lang.Boolean.FALSE;

/**
 * Represents property metadata.
 * <p>
 * Each property is classified according to its nature. A nature may specify essential metadata that is attributed to a
 * property. This essential data can be accessed via {@link #data()}.
 * Matching on the property's nature can be performed with the {@code switch} statement or via {@link #match(PropertyMetadataVisitor)}.
 *
 * <h3> Arbitrary Metadata </h3>
 * A property might have arbitrary additional information associated with it which is modelled with key-value pairs.
 * Keys come in 2 forms:
 * <ul>
 *   <li> General-purpose keys that are applicable to properties of all natures: {@link AnyKey}. Access via {@link #get(AnyKey)}.
 *   <li> <s>Specialised keys, applicable to properties of a specific nature: {@link Key}. Access via {@link #get(Key)}</s>.
 * </ul>
 * The purpose of this mechanism is to represent optional information.
 *
 * @see PropertyMetadataKeys
 */
public sealed interface PropertyMetadata extends Comparable<PropertyMetadata> {

    String name();

    PropertyTypeMetadata type();

    Object hibType();

    PropertyNature nature();

    PropertyNature.Data data();

    <R> R match(PropertyMetadataVisitor<R> visitor);

    // NOTE if a need for specialised keys arises, this interface will have to be parameterised with PropertyNature
//    <V> Optional<V> get(Key<V, N> key);

    <V> Optional<V> get(AnyKey<V> key);

//    default boolean is(Key<Boolean, N> key) {
//        return get(key).orElse(FALSE);
//    }

    default boolean is(AnyKey<Boolean> key) {
        return get(key).orElse(FALSE);
    }

    @Override
    default int compareTo(final PropertyMetadata that) {
        return name().compareTo(that.name());
    }

    // ****************************************
    // * Convenient methods as an alternative to a visitor with a single clause

    default Optional<Persistent> asPersistent() {
        return this instanceof Persistent ? Optional.of((Persistent) this) : Optional.empty();
    }

    default Optional<Calculated> asCalculated() {
        return this instanceof Calculated ? Optional.of((Calculated) this) : Optional.empty();
    }

    default Optional<CritOnly> asCritOnly() {
        return this instanceof CritOnly ? Optional.of((CritOnly) this) : Optional.empty();
    }

    default Optional<Transient> asTransient() {
        return this instanceof Transient ? Optional.of((Transient) this) : Optional.empty();
    }

    non-sealed interface Persistent extends PropertyMetadata {
        @Override
        PropertyNature.Persistent nature();

        @Override
        PropertyNature.Persistent.Data data();
    }

    non-sealed interface Calculated extends PropertyMetadata {
        @Override
        PropertyNature.Calculated nature();

        @Override
        PropertyNature.Calculated.Data data();
    }

    non-sealed interface Transient extends PropertyMetadata {
        @Override
        PropertyNature.Transient nature();

        @Override
        PropertyNature.Transient.Data data();
    }

    non-sealed interface CritOnly extends PropertyMetadata {
        @Override
        PropertyNature.CritOnly nature();

        @Override
        PropertyNature.CritOnly.Data data();
    }

    interface Key<V, N extends PropertyNature> extends IKey {}

    interface AnyKey<V> extends IKey {}

    /**
     * Marker interface for property metadata keys.
     */
    interface IKey {}

}
