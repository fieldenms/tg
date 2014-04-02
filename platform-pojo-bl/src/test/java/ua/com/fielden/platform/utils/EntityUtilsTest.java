package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

public class EntityUtilsTest {

    @Test
    @Ignore
    public void testSplitPropByFirstDot() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testIsPropertyPartOfKey() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testIsPropertyRequired() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void testGetPersistedProperties() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetCollectionalProperties() {
        final Field userRolesField = EntityUtils.getCollectionalProperties(User.class).get(0);
        assertEquals("Incorrect field name", "roles", userRolesField.getName());
        assertEquals("Incorrect collectional entity class", UserAndRoleAssociation.class, AnnotationReflector.getAnnotation(userRolesField, IsProperty.class).value());
        assertEquals("Incorrect collectional entity link property", "user", AnnotationReflector.getAnnotation(userRolesField, IsProperty.class).linkProperty());
    }
}
