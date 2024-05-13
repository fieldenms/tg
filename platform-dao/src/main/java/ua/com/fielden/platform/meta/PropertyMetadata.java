package ua.com.fielden.platform.meta;

import java.util.Optional;

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

    @Override
    default int compareTo(final PropertyMetadata<?> that) {
        return name().compareTo(that.name());
    }

    interface Key<V, N extends PropertyNature> extends IKey {}

    interface AnyKey<V> extends IKey {}

    /**
     * Marker interface for property metadata keys.
     */
    interface IKey {}

}
