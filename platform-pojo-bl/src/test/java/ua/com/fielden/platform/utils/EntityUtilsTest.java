package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.utils.EntityUtils.getCollectionalProperties;
import static ua.com.fielden.platform.utils.EntityUtils.safeCompare;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

public class EntityUtilsTest {

    @Test
    public void safe_comparison_considers_two_null_values_equal() {
        assertTrue(safeCompare(null, null) == 0);
    }

    @Test
    public void safe_comparison_considers_null_smaller_than_non_null() {
        assertTrue(EntityUtils.safeCompare(42, null) > 0);
        assertTrue(EntityUtils.safeCompare(null, 42) < 0);
    }

    @Test
    public void safe_comparison_of_non_null_values_equals_to_the_result_of_comparing_values_directly() {
        assertEquals(Integer.valueOf(42).compareTo(Integer.valueOf(13)), EntityUtils.safeCompare(42, 13));
        assertEquals(Integer.valueOf(13).compareTo(Integer.valueOf(42)), EntityUtils.safeCompare(13, 42));
    }

    @Test
    public void collectional_properties_are_correctly_identifiable() {
        final List<Field> collectionalProperties = getCollectionalProperties(User.class);
        assertEquals(1, collectionalProperties.size());

        final Field userRolesField = collectionalProperties.get(0);
        assertEquals("Incorrect field name", "roles", userRolesField.getName());
        assertEquals("Incorrect collectional entity class", UserAndRoleAssociation.class, AnnotationReflector.getAnnotation(userRolesField, IsProperty.class).value());
        assertEquals("Incorrect collectional entity link property", "user", AnnotationReflector.getAnnotation(userRolesField, IsProperty.class).linkProperty());
    }
}
