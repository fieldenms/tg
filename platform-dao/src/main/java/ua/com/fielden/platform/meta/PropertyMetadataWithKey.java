package ua.com.fielden.platform.meta;

/**
 * Wraps {@link PropertyMetadata} that has been proven to have a value associated with a certain key.
 * <p>
 * Instances of this type can be obtained via {@link PropertyMetadata#withKey(PropertyMetadata.AnyKey)}.
 * <p>
 * The limitation of this type is that its proof capacity is limited to a single key.
 *
 * @param <K> the key that has been proven to be present
 * @param <V> the value associated with the key
 */
public interface PropertyMetadataWithKey<K extends PropertyMetadata.AnyKey<V>, V> {

    /**
     * Returns the value associated with the key. This method is total.
     */
    V get();

    /**
     * Returns the wrapped instance.
     */
    PropertyMetadata unwrap();

}
