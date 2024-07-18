package ua.com.fielden.platform.meta;

final class PropertyMetadataWithKeyImpl<K extends PropertyMetadata.AnyKey<V>, V> implements PropertyMetadataWithKey<K, V> {

    private final PropertyMetadata _this_;
    private final V value;

    PropertyMetadataWithKeyImpl(final PropertyMetadata _this_, final V value) {
        this._this_ = _this_;
        this.value = value;
    }

    @Override
    public V get() {
        return value;
    }

    public PropertyMetadata unwrap() {
        return _this_;
    }

}
