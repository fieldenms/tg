package ua.com.fielden.platform.entity.annotation.factory;

import static org.junit.Assert.*;


import org.junit.Test;

import ua.com.fielden.platform.test_entities.Entity;
import ua.com.fielden.platform.entity.annotation.IsProperty;

/**
 * A test case for a factory class designed for {@link IsProperty} annotation.
 * @author TG Team
 *
 */
public class IsPropertyAnnotationTest {
    
    private static final IsProperty atIsProp = new IsPropertyAnnotation(Entity.class, "LINK_PROPERTY", true, 255, 10, 2, false,
            "DISPLAY_AS").newInstance();

    @Test
    public void factory_produced_instances_have_correct_equals_implementation() {
        assertNotEquals("Non-null IsProperty instance should NOT be equal to null.", atIsProp, null);

        final IsProperty equalInstance = new IsPropertyAnnotation(Entity.class, "LINK_PROPERTY", true, 255, 10, 2, false,
                "DISPLAY_AS").newInstance();
        assertEquals("IsProperty instances with different contents should NOT be equal.", atIsProp, equalInstance);

        final IsProperty withDifferentValue = new IsPropertyAnnotation(String.class, "LINK_PROPERTY", true, 255, 10, 2, false,
                "DISPLAY_AS").newInstance();
        assertNotEquals("IsProperty instances with different contents should NOT be equal.", atIsProp, withDifferentValue);

        final IsProperty withDifferentLinkProperty = new IsPropertyAnnotation(Entity.class, "DIFFERENT", true, 255, 10, 2, false,
                "DISPLAY_AS").newInstance();
        assertNotEquals("IsProperty instances with different contents should NOT be equal.", atIsProp, withDifferentLinkProperty);

        final IsProperty withDifferentAssignBeforeSave = new IsPropertyAnnotation(Entity.class, "LINK_PROPERTY", false, 255, 10, 2, false,
                "DISPLAY_AS").newInstance();
        assertNotEquals("IsProperty instances with different contents should NOT be equal.", atIsProp, withDifferentAssignBeforeSave);

        final IsProperty withDifferentLength = new IsPropertyAnnotation(Entity.class, "LINK_PROPERTY", true, 1, 10, 2, false,
                "DISPLAY_AS").newInstance();
        assertNotEquals("IsProperty instances with different contents should NOT be equal.", atIsProp, withDifferentLength);

        final IsProperty withDifferentPrecision = new IsPropertyAnnotation(Entity.class, "LINK_PROPERTY", true, 255, 20, 2, false,
                "DISPLAY_AS").newInstance();
        assertNotEquals("IsProperty instances with different contents should NOT be equal.", atIsProp, withDifferentPrecision);

        final IsProperty withDifferentScale = new IsPropertyAnnotation(Entity.class, "LINK_PROPERTY", true, 255, 10, 10, false,
                "DISPLAY_AS").newInstance();
        assertNotEquals("IsProperty instances with different contents should NOT be equal.", atIsProp, withDifferentScale);

        final IsProperty withDifferentTrailingZeros = new IsPropertyAnnotation(Entity.class, "LINK_PROPERTY", true, 255, 10, 2, true,
                "DISPLAY_AS").newInstance();
        assertNotEquals("IsProperty instances with different contents should NOT be equal.", atIsProp, withDifferentTrailingZeros);

        final IsProperty withDifferentDisplayAs = new IsPropertyAnnotation(Entity.class, "LINK_PROPERTY", true, 255, 10, 2, false,
                "DIFFERENT").newInstance();
        assertNotEquals("IsProperty instances with different contents should NOT be equal.", atIsProp, withDifferentDisplayAs);
    }
}
