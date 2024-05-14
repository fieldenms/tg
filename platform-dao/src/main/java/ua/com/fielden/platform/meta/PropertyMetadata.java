package ua.com.fielden.platform.meta;

import java.util.Optional;

import static java.lang.Boolean.FALSE;

/**
 * Represents property metadata.
 * <p>
 * Each property is classified according to its nature. A nature may specify essential metadata that is attributed to a
 * property. This essential data can be accessed via {@link #data()} but since its type is specific to the property's nature,
 * {@link #match(PropertyMetadataVisitor)} should be used.
 *
 * <h3> Arbitrary Metadata </h3>
 * A property might have arbitrary additional information associated with it which is modelled with key-value pairs.
 * Keys come in 2 forms:
 * <ul>
 *   <li> General-purpose keys that are applicable to properties of all natures: {@link AnyKey}. Access via {@link #get(AnyKey)}.
 *   <li> Specialised keys, applicable to properties of a specific nature: {@link Key}. Access via {@link #get(Key)}.
 * </ul>
 * The purpose of this mechanism is to represent optional information.
 *
 * @param <N> property's nature
 *
 * @see PropertyMetadataKeys
 */
public interface PropertyMetadata<N extends PropertyNature>
        extends Comparable<PropertyMetadata<?>>
{

    String name();

    PropertyTypeMetadata type();

    Object hibType();

    N nature();

    PropertyNature.Data<N> data();

    <R> R match(PropertyMetadataVisitor<R> visitor);

    <V> Optional<V> get(Key<V, N> key);

    <V> Optional<V> get(AnyKey<V> key);

    default boolean is(Key<Boolean, N> key) {
        return get(key).orElse(FALSE);
    }

    default boolean is(AnyKey<Boolean> key) {
        return get(key).orElse(FALSE);
    }

    @Override
    default int compareTo(final PropertyMetadata<?> that) {
        return name().compareTo(that.name());
    }

    // ****************************************
    // * Convenient methods as an alternative to a visitor with a single clause

    default Optional<PropertyMetadata<PropertyNature.Persistent>> asPersistent() {
        return nature() instanceof PropertyNature.Persistent
                ? Optional.of((PropertyMetadata<PropertyNature.Persistent>) this)
                : Optional.empty();
    }

    default Optional<PropertyMetadata<PropertyNature.Calculated>> asCalculated() {
        return nature() instanceof PropertyNature.Calculated
                ? Optional.of((PropertyMetadata<PropertyNature.Calculated>) this)
                : Optional.empty();
    }

    default Optional<PropertyMetadata<PropertyNature.Transient>> asTransient() {
        return nature() instanceof PropertyNature.Transient
                ? Optional.of((PropertyMetadata<PropertyNature.Transient>) this)
                : Optional.empty();
    }

    default Optional<PropertyMetadata<PropertyNature.CritOnly>> asCritOnly() {
        return nature() instanceof PropertyNature.CritOnly
                ? Optional.of((PropertyMetadata<PropertyNature.CritOnly>) this)
                : Optional.empty();
    }

    interface Key<V, N extends PropertyNature> extends IKey {}

    interface AnyKey<V> extends IKey {}

    /**
     * Marker interface for property metadata keys.
     */
    interface IKey {}

}
