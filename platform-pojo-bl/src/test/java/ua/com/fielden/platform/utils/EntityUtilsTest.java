package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.utils.EntityUtils.getCollectionalProperties;
import static ua.com.fielden.platform.utils.EntityUtils.safeCompare;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

public class EntityUtilsTest {
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);


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
    public void copy_copies_all_properties_if_non_are_skipped() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = factory.newEntity(Entity.class); 
        EntityUtils.copy(entity, copy);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Entity with no id should be recognized as drity.", copy.isDirty());
        assertEquals("Id should have been copied", entity.getId(), copy.getId());
        assertEquals("Version should have been copied", entity.getVersion(), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
    }

    @Test
    public void copy_does_not_copy_skipped_VERSION_and_ID() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = factory.newEntity(Entity.class); 
        EntityUtils.copy(entity, copy, VERSION, ID);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Entity with no id should be recognized as drity.", copy.isDirty());
        assertNull("Id should have not been copied", copy.getId());
        assertEquals("Version should have not been copied", Long.valueOf(0), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
    }

    @Test
    public void copy_does_not_copy_skipped_properties() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = factory.newEntity(Entity.class); 
        EntityUtils.copy(entity, copy, "money", DESC);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Entity with no id should be recognized as drity.", copy.isDirty());
        assertEquals("Id should have been copied", entity.getId(), copy.getId());
        assertEquals("Version should have been copied", entity.getVersion(), copy.getVersion());
        assertNull("Property desc should have not been copied", copy.getDesc());
        assertNull("Property money should have not been copied", copy.getMoney());
    }

    @Test
    public void copy_does_not_occur_in_the_initialisation_mode_by_default() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = factory.newEntity(Entity.class);
        EntityUtils.copy(entity, copy);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Copy is not instrumented", copy.isInstrumented());
        assertTrue("Copy should be dirty", copy.isDirty());
        assertTrue("Property key is copied and should be recognised as dirty", copy.getProperty("key").isDirty());
        assertEquals("IDs do not match", entity.getId(), copy.getId());
        assertEquals("Versions do not match.", Long.valueOf(42L), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertTrue("Property desc is copied and should be recognised as dirty", copy.getProperty("desc").isDirty());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
        assertTrue("Property money is copied and should be recognised as dirty", copy.getProperty("money").isDirty());
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
    
    @Test
    public void two_nulls_are_comparible_and_equal() {
        assertEquals(0, EntityUtils.compare(null, null));
    }
    
    @Test
    public void null_is_smaller_than_non_null() {
        assertTrue(EntityUtils.compare(null, factory.newEntity(Entity.class)) < 0);
    }
    
    @Test
    public void non_null_is_greater_than_null() {
        assertTrue(EntityUtils.compare(factory.newEntity(Entity.class), null) > 0);
    }
    
    @Test
    public void the_result_of_comparing_two_non_nulls_matches_the_result_of_comparing_them_with_compareTo() {
        final Entity entity1 = factory.newByKey(Entity.class, "1");
        final Entity entity2 = factory.newByKey(Entity.class, "2");
        
        assertEquals(entity1.compareTo(entity2), EntityUtils.compare(entity1, entity2));
        assertEquals(entity2.compareTo(entity1), EntityUtils.compare(entity2, entity1));
        assertEquals(entity1.compareTo(entity1), EntityUtils.compare(entity1, entity1));
    }
}
