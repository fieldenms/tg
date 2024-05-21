package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.test_utils.TestUtils.assertInstanceOf;
import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;

interface Assertions {

    class PropertyTypeA<T extends PropertyTypeMetadata> {
        final T typeMetadata;

        private PropertyTypeA(final T typeMetadata) {
            this.typeMetadata = typeMetadata;
        }

        static PropertyTypeA<PropertyTypeMetadata> of(Optional<PropertyTypeMetadata> optTypeMetadata) {
            return new PropertyTypeA<>(assertPresent(optTypeMetadata));
        }

        public T get() { return typeMetadata; }

        public PropertyTypeA<T> assertJavaType(final Class<?> expectedJavaType) {
            assertEquals("Wrong Java Type", expectedJavaType, typeMetadata.javaType());
            return this;
        }

        public <U extends PropertyTypeMetadata> PropertyTypeA<U> assertIs(final Class<U> type) {
            return new PropertyTypeA<>(assertInstanceOf(type, typeMetadata));
        }

        public CollectionalA assertCollectional() {
            return new CollectionalA(assertIs(PropertyTypeMetadata.Collectional.class).get());
        }

        public static class CollectionalA extends PropertyTypeA<PropertyTypeMetadata.Collectional> {
            CollectionalA(final PropertyTypeMetadata.Collectional typeMetadata) {
                super(typeMetadata);
            }

            CollectionalA assertCollectionType(final Class<?> klass) {
                assertEquals(klass, typeMetadata.collectionType());
                return this;
            }

            PropertyTypeA<PropertyTypeMetadata> elementType() {
                return new PropertyTypeA<>(typeMetadata.elementType());
            }
        }
    }

    class EntityA<E extends EntityMetadata> {
        private final E entityMetadata;

        private EntityA(final E entityMetadata) {
            this.entityMetadata = entityMetadata;
        }

        static EntityA<EntityMetadata> of(final EntityMetadata entityMetadata) {
            return new EntityA<>(entityMetadata);
        }

        public E get() { return entityMetadata; }

        <U extends EntityMetadata> EntityA<U> assertIs(final Class<U> type) {
            return new EntityA<>(assertInstanceOf(type, entityMetadata));
        }

        public EntityA<E> assertJavaType(final Class<? extends AbstractEntity<?>> expectedJavaType) {
            assertEquals("Wrong Java Type", expectedJavaType, entityMetadata.javaType());
            return this;
        }

        public EntityA<E> assertProperty(final CharSequence propName, final Consumer<PropertyA<PropertyMetadata>> assertor) {
            final PropertyMetadata propertyMetadata = assertPresent(
                    "Metadata for property [%s] not found in [%s]".formatted(propName, entityMetadata),
                    entityMetadata.property(propName.toString()));
            assertor.accept(PropertyA.of(propertyMetadata));
            return this;
        }

        public EntityA<E> assertPropertyExists(final CharSequence propName) {
            assertPresent("Metadata for property [%s] not found in [%s]".formatted(propName, entityMetadata),
                          entityMetadata.property(propName.toString()));
            return this;
        }

        public EntityA<E> assertPropertiesExist(final Iterable<? extends CharSequence> propName) {
            propName.forEach(this::assertPropertyExists);
            return this;
        }
    }

    class PropertyA<P extends PropertyMetadata> {
        private final P propertyMetadata;

        private PropertyA(final P propertyMetadata) {
            this.propertyMetadata = propertyMetadata;
        }

        static <P extends PropertyMetadata> PropertyA<P> of(final P propertyMetadata) {
            return new PropertyA<>(propertyMetadata);
        }

        public P get() { return propertyMetadata; }

        public <U extends PropertyMetadata> PropertyA<U> assertIs(final Class<U> type) {
            return new PropertyA<>(assertInstanceOf(type, propertyMetadata));
        }

        public PropertyTypeA<PropertyTypeMetadata> type() {
            return new PropertyTypeA<>(propertyMetadata.type());
        }

        public PropertyA<P> assertType(final Consumer<PropertyTypeA<?>> assertor) {
            assertor.accept(type());
            return this;
        }

        public <V> PropertyA<P> assertKey(final PropertyMetadata.AnyKey<V> key, final Consumer<V> assertor) {
            final var v = assertPresent("Key [%s] is absent from [%s]".formatted(key, propertyMetadata), propertyMetadata.get(key));
            assertor.accept(v);
            return this;
        }

        public <V> PropertyA<P> assertKeyEq(final PropertyMetadata.AnyKey<V> key, final V expectedValue) {
            return assertKey(key, v -> assertEquals("Unexpected value for key [%s] in [%s]".formatted(key, propertyMetadata),
                                                    expectedValue, v));
        }

        public PropertyA<P> asserting(final Consumer<? super P> assertor) {
            assertor.accept(propertyMetadata);
            return this;
        }
    }

}

