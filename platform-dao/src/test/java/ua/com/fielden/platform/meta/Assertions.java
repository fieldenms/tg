package ua.com.fielden.platform.meta;

import java.util.Optional;

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

}

