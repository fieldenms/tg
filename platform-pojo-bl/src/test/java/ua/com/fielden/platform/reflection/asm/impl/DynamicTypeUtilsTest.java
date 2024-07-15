package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;

/**
 * A test case for verifying assumptions about {@link DynamicEntityClassLoader}.
 * 
 * @author TG Team
 *
 */
public class DynamicTypeUtilsTest {

    @Test
    public void properties_are_ordered_such_that_generated_ones_are_last_ascending_type_hierarchy_of_declaring_class() throws Exception {
        // add a new property
        final var np11 = NewProperty.create("newTestProperty11", String.class, "title", "desc");
        final var np12 = NewProperty.create("newTestProperty12", String.class, "title", "desc");
        final Class<?> newType1 = startModification(EntityBeingEnhanced.class)
                .addProperties(np11, np12)
                .endModification();

        // add a new property
        final var np2 = NewProperty.create("newTestProperty2", Double.class, "title", "desc");
        final Class<?> newType2 = startModification(newType1)
                .addProperties(np2)
                .endModification();

        // modify an existing property to ensure that order is enforced for modified properties too
        final var np3 = NewProperty.create("prop1", List.class, "title", "desc");
        final Class<?> newType3 = startModification(newType2)
                .modifyProperties(np3)
                .endModification();

        final List<Field> allProperties = Finder.findProperties(newType3);
        final List<String> sorted = DynamicTypeUtils.orderedProperties(allProperties).stream()
                .map(Field::getName).toList();
        final int n = sorted.size();

        assertEquals("Incorrect order of properties.", np3.getName(), sorted.get(n - 1));
        assertEquals("Incorrect order of properties.", np2.getName(), sorted.get(n - 2));
        assertEquals("Incorrect order of properties.", np12.getName(), sorted.get(n - 3));
        assertEquals("Incorrect order of properties.", np11.getName(), sorted.get(n - 4));
    }

}