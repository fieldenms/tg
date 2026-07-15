package ua.com.fielden.platform.eql.stage3;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.STRING_PROP_TYPE;

/// Tests for [Prop3], whose equality is based on the property name, the source id, and the property type.
///
public class Prop3Test extends EqlStage3TestCase {

    @Test
    public void props_with_the_same_name_and_source_are_equal_and_have_equal_hash_codes() {
        final var source = source(TgVehicle.class, 1, 1);
        final var prop1 = new Prop3("key", source, STRING_PROP_TYPE);
        final var prop2 = new Prop3("key", source, STRING_PROP_TYPE);

        assertEquals(prop1, prop2);
        assertEquals(prop1.hashCode(), prop2.hashCode());
    }

    @Test
    public void props_with_sources_that_have_different_ids_are_not_equal() {
        final var prop1 = new Prop3("key", source(TgVehicle.class, 1, 1), STRING_PROP_TYPE);
        final var prop2 = new Prop3("key", source(TgVehicle.class, 2, 2), STRING_PROP_TYPE);

        assertNotEquals(prop1, prop2);
    }

    @Test
    public void props_with_different_names_are_not_equal() {
        final var source = source(TgVehicle.class, 1, 1);
        final var prop1 = new Prop3("key", source, STRING_PROP_TYPE);
        final var prop2 = new Prop3("desc", source, STRING_PROP_TYPE);

        assertNotEquals(prop1, prop2);
    }

    @Test
    public void props_with_different_types_are_not_equal() {
        final var source = source(TgVehicle.class, 1, 1);
        final var prop1 = new Prop3("key", source, STRING_PROP_TYPE);
        final var prop2 = new Prop3("key", source, LONG_PROP_TYPE);

        assertNotEquals(prop1, prop2);
    }

    @Test
    public void a_prop_cannot_be_constructed_without_a_name() {
        assertThrows(NullPointerException.class, () -> new Prop3(null, source(TgVehicle.class, 1, 1), STRING_PROP_TYPE));
    }

    @Test
    public void a_prop_cannot_be_constructed_without_a_source() {
        assertThrows(NullPointerException.class, () -> new Prop3("key", null, STRING_PROP_TYPE));
    }

}
