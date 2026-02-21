package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.test_utils.CollectionTestUtils.assertEqualByContents;
import static ua.com.fielden.platform.test_utils.TestUtils.assertInstanceOf;
import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;

interface Assertions {

    class PropertyTypeA<T extends PropertyTypeMetadata> {
        final T typeMetadata;

        private PropertyTypeA(final T typeMetadata) {
            this.typeMetadata = typeMetadata;
        }

        static PropertyTypeA<PropertyTypeMetadata> of(PropertyTypeMetadata typeMetadata) {
            return new PropertyTypeA<>(typeMetadata);
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

            public CollectionalA assertCollectionType(final Class<?> klass) {
                assertEquals(klass, typeMetadata.collectionType());
                return this;
            }

            public PropertyTypeA<PropertyTypeMetadata> elementType() {
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

        public EntityA<E> peek(final Consumer<? super E> fn) {
            fn.accept(entityMetadata);
            return this;
        }

        public <U extends EntityMetadata> EntityA<U> assertIs(final Class<U> type) {
            return new EntityA<>(assertInstanceOf(type, entityMetadata));
        }

        public EntityA<E> assertJavaType(final Class<? extends AbstractEntity<?>> expectedJavaType) {
            assertEquals("Wrong Java Type", expectedJavaType, entityMetadata.javaType());
            return this;
        }

        public EntityA<E> assertProperty(final CharSequence propName, final Consumer<PropertyA<PropertyMetadata>> assertor) {
            final PropertyMetadata propertyMetadata = assertPresent(
                    "Metadata for property [%s] not found in [%s]".formatted(propName, entityMetadata),
                    entityMetadata.propertyOpt(propName.toString()));
            assertor.accept(PropertyA.of(propertyMetadata));
            return this;
        }

        public EntityA<E> assertPropertyExists(final CharSequence propName) {
            assertPresent("Metadata for property [%s] not found in [%s]".formatted(propName, entityMetadata),
                          entityMetadata.propertyOpt(propName.toString()));
            return this;
        }

        public PropertyA<PropertyMetadata> getProperty(final CharSequence propName) {
            assertPropertyExists(propName);
            return PropertyA.of(entityMetadata.property(propName.toString()));
        }

        public EntityA<E> assertPropertiesExist(final Iterable<? extends CharSequence> propNames) {
            propNames.forEach(this::assertPropertyExists);
            return this;
        }

        public EntityA<E> assertPropertyNotExists(final CharSequence propName) {
            assertTrue("Unexpected metadata found for property [%s] in [%s]".formatted(propName, entityMetadata),
                       entityMetadata.propertyOpt(propName.toString()).isEmpty());
            return this;
        }

        public EntityA<E> assertPropertiesNotExist(final Iterable<? extends CharSequence> propNames) {
            propNames.forEach(this::assertPropertyNotExists);
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

        public PropertyA<P> assertHibType(final Consumer<Object> assertor) {
            assertor.accept(propertyMetadata.hibType());
            return this;
        }

        public PropertyA<P> assertHasHibType() {
            assertHibType(t -> assertNotNull("Hibernate type is missing for property [%s]".formatted(propertyMetadata),
                                             t));
            return this;
        }

        public PropertyA<P> assertNoHibType() {
            assertHibType(t -> assertNull("Hibernate type should not be present in property [%s]".formatted(propertyMetadata),
                                          t));
            return this;
        }

        public PropertyA<P> assertHibTypeEq(final Object expectedHibType) {
            return assertHibType(t -> assertEquals("Unexpected Hibernate type of property [%s]".formatted(propertyMetadata),
                                                   expectedHibType, t));
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

        public <V> PropertyA<P> assertHasKey(final PropertyMetadata.AnyKey<V> key) {
            return assertKey(key, $ -> {});
        }

        public <V> PropertyA<P> assertKeyEq(final PropertyMetadata.AnyKey<V> key, final V expectedValue) {
            return assertKey(key, v -> assertEquals("Unexpected value for key [%s] in [%s]".formatted(key, propertyMetadata),
                                                    expectedValue, v));
        }

        public PropertyA<P> asserting(final Consumer<? super P> assertor) {
            assertor.accept(propertyMetadata);
            return this;
        }

        public SubPropertiesA subProperties(final PropertyMetadataUtils pmUtils) {
            return new SubPropertiesA(propertyMetadata, pmUtils.subProperties(propertyMetadata));
        }

        public SubPropertiesA subProperties(final PropertyMetadataUtils pmUtils, final PropertyMetadataUtils.SubPropertyNaming naming) {
            return new SubPropertiesA(propertyMetadata, pmUtils.subProperties(propertyMetadata, naming));
        }
    }

    class SubPropertiesA {
        private final PropertyMetadata parent;
        private final Map<String, PropertyMetadata> subProperties;

        public SubPropertiesA(final PropertyMetadata parent, final Collection<? extends PropertyMetadata> subProperties) {
            this.parent = parent;
            this.subProperties = subProperties.stream().collect(Collectors.toMap(PropertyMetadata::name, identity()));
        }

        public SubPropertiesA forEach(final Consumer<PropertyA<PropertyMetadata>> fn) {
            subProperties.values().stream().map(PropertyA::of).forEach(fn);
            return this;
        }

        public SubPropertiesA assertSubProperty(final CharSequence propName, final Consumer<PropertyA<PropertyMetadata>> assertor) {
            final var subProp = subProperties.get(propName);
            assertNotNull("Sub-property [%s] not found for property [%s]".formatted(propName, parent), subProp);
            assertor.accept(PropertyA.of(subProp));
            return this;
        }

        public SubPropertiesA assertSubPropertyExists(final CharSequence propName) {
            return assertSubProperty(propName, $ -> {});
        }

        public SubPropertiesA assertSubPropertiesExist(final Iterable<? extends CharSequence> propNames) {
            propNames.forEach(this::assertSubPropertyExists);
            return this;
        }

        /**
         * Unlike {@link #assertSubPropertiesExist(Iterable)}, which assert set membership, this method asserts set equality.
         */
        public SubPropertiesA assertSubPropertiesAre(final Collection<? extends CharSequence> propNames) {
            assertEqualByContents(propNames, subProperties.values().stream().map(PropertyMetadata::name).toList());
            return this;
        }

        public SubPropertiesA assertSubProperties(final Consumer<? super Collection<? extends PropertyMetadata>> assertor) {
            assertor.accept(subProperties.values());
            return this;
        }
    }

    class ComponentA {
        private final TypeMetadata.Component typeMetadata;

        public ComponentA(final TypeMetadata.Component typeMetadata) {
            this.typeMetadata = typeMetadata;
        }

        static ComponentA of(final TypeMetadata.Component typeMetadata) {
            return new ComponentA(typeMetadata);
        }

        static ComponentA of(final Optional<TypeMetadata.Component> typeMetadata) {
            return new ComponentA(assertPresent("Expected metadata to be generated for a component type.", typeMetadata));
        }

        public TypeMetadata.Component get() { return typeMetadata; }

        public ComponentA assertJavaType(final Class<?> expectedJavaType) {
            assertEquals("Wrong Java Type", expectedJavaType, typeMetadata.javaType());
            return this;
        }

        public ComponentA assertProperty(final CharSequence propName, final Consumer<PropertyA<PropertyMetadata>> assertor) {
            final PropertyMetadata propertyMetadata = assertPresent(
                    "Metadata for property [%s] not found in [%s]".formatted(propName, typeMetadata),
                    typeMetadata.propertyOpt(propName.toString()));
            assertor.accept(PropertyA.of(propertyMetadata));
            return this;
        }

        public ComponentA assertPropertyExists(final CharSequence propName) {
            assertPresent("Metadata for property [%s] not found in [%s]".formatted(propName, typeMetadata),
                          typeMetadata.propertyOpt(propName.toString()));
            return this;
        }

        public ComponentA assertPropertiesExist(final Iterable<? extends CharSequence> propName) {
            propName.forEach(this::assertPropertyExists);
            return this;
        }
    }

}

