package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.*;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static ua.com.fielden.platform.entity.exceptions.NoSuchPropertyException.noSuchPropertyException;

final class CompositeTypeMetadataImpl implements TypeMetadata.Composite {

    private final Class<?> javaType;
    private final Map<String, ? extends PropertyMetadata> properties;

    CompositeTypeMetadataImpl(final Builder builder) {
        this.javaType = builder.javaType;
        this.properties = builder.properties.stream()
                .collect(toImmutableMap(PropertyMetadata::name, identity()));
    }

    @Override
    public Class<?> javaType() {
        return javaType;
    }

    @Override
    public Collection<? extends PropertyMetadata> properties() {
        return properties.values();
    }

    @Override
    public PropertyMetadata property(final String name) {
        final var property = properties.get(name);
        if (property == null) {
            throw noSuchPropertyException(javaType, name);
        }
        return property;
    }

    @Override
    public Optional<PropertyMetadata> propertyOpt(final String name) {
        return Optional.ofNullable(properties.get(name));
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this
               || obj instanceof CompositeTypeMetadataImpl that
                  && Objects.equals(this.javaType, that.javaType)
                  && Objects.equals(this.properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaType, properties);
    }

    @Override
    public String toString() {
        return "CompositeTypeMetadata[" +
               "javaType=" + javaType + ", " +
               "properties=" + properties + ']';
    }

    static final class Builder {
        private Class<?> javaType;
        private final Collection<PropertyMetadata> properties = new ArrayList<>();

        Builder(final Class<?> javaType) {
            this.javaType = javaType;
        }

        public Class<?> getJavaType() {
            return javaType;
        }

        public Builder javaType(final Class<? extends AbstractEntity<?>> javaType) {
            this.javaType = javaType;
            return this;
        }

        public Builder properties(final Iterable<? extends PropertyMetadata> properties) {
            properties.forEach(this.properties::add);
            return this;
        }

        public Builder properties(final PropertyMetadata... properties) {
            Collections.addAll(this.properties, properties);
            return this;
        }

        public TypeMetadata.Composite build() {
            return new CompositeTypeMetadataImpl(this);
        }

    }

}
