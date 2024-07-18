package ua.com.fielden.platform.meta;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

/**
 * A wrapper for {@link PropertyTypeMetadataGenerator} for testing purposes.
 */
class TestPropertyMetadataGenerator {

    private final PropertyTypeMetadataGenerator generator;

    private TestPropertyMetadataGenerator(final PropertyTypeMetadataGenerator generator) {
        this.generator = generator;
    }

    public static TestPropertyMetadataGenerator wrap(final PropertyTypeMetadataGenerator generator) {
        return new TestPropertyMetadataGenerator(generator);
    }

    /**
     * Forwards to {@link PropertyTypeMetadataGenerator#generate(Field)} and asserts the result's presence.
     */
    public PropertyTypeMetadata generate(final Field field) {
        return generator.generate(field)
                .orElseThrow(() -> new AssertionError(format("Expected metadata to be generated for [%s]",
                                                             field.toGenericString())));
    }

    /**
     * Forwards to {@link PropertyTypeMetadataGenerator#generate(Type)} and asserts the result's presence.
     */
    public PropertyTypeMetadata generate(final Type type) {
        return generator.generate(type)
                .orElseThrow(() -> new AssertionError(format("Expected metadata to be generated for [%s]", type.getTypeName())));
    }

    public TestPropertyMetadataGenerator assertNotGenerated(final Field field) {
        assertTrue(format("Did not expect property type metadata to be generated for [%s]", field.toGenericString()),
                   generator.generate(field).isEmpty());
        return this;
    }

    public TestPropertyMetadataGenerator assertNotGenerated(final Type type) {
        assertTrue(format("Did not expect property type metadata to be generated for [%s]", type.getTypeName()),
                   generator.generate(type).isEmpty());
        return this;
    }

}
