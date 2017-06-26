package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesFive.INVALID;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.test_entities.EntityWithDependentProperties;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithDependentPropertiesFive;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 *
 * This test case is complementary to AbstractEntityTest covering mainly meta-property functionality. A large number of test in AbstractEntityTest also pertain to meta-property
 * functionality.
 *
 * @author TG Team
 *
 */
public class PropertyDependencyHandlingTest {
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_adding_property_to_dependency_path_when_everything_is_correct_and_there_is_circular_dependency() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
        } catch (final Exception ex) {
            fail("No exception is expected.");
        }
    }

    @Test
    public void test_adding_property_to_dependency_path_when_everything_is_correct_and_there_is_no_circular_dependency() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        try {
            entity.getProperty("one").addToDependencyPath(entity.getProperty("four"));
        } catch (final Exception ex) {
            fail("No exception is expected.");
        }
    }

    @Test
    public void test_adding_null_property_to_dependency_path() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        try {
            entity.getProperty("two").addToDependencyPath(null);
            fail("Should not be able to add null to the dependency path.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_adding_property_to_dependency_path_when_one_is_already_added() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
            fail("Should not be able to add property to the dependency path when another is already added.");
        } catch (final Exception ex) {

        }
    }

    @Test
    public void test_adding_property_not_from_the_dependency_list_in_case_of_circular_dependecy_to_dependency_path() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("four"));
            fail("Should not be able to add property not from dependency list to the dependency path.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_adding_property_not_from_the_dependency_list_in_case_of_non_circular_dependecy_to_dependency_path() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        try {
            entity.getProperty("one").addToDependencyPath(entity.getProperty("four"));
        } catch (final Exception ex) {
            fail("No exception is expected.");
        }
    }

    @Test
    public void test_removing_null_property_from_dependency_path() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
            entity.getProperty("two").removeFromDependencyPath(null);
            fail("Should not be able to remove null property the dependency path.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_removing_property_from_dependency_path_when_none_was_added() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        try {
            entity.getProperty("two").removeFromDependencyPath(entity.getProperty("one"));
            fail("Should not be able to remove property when the dependency path is empty.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_removing_property_from_dependency_path_when_everything_is_ok() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
            entity.getProperty("two").removeFromDependencyPath(entity.getProperty("one"));
        } catch (final Exception ex) {
            fail("Removal should have been successful.");
        }
    }

    @Test
    public void test_dependency_path_construction() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        // emulate setting a value into property one
        entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
        entity.getProperty("three").addToDependencyPath(entity.getProperty("two"));
        assertTrue("Two should be on dependency path for three", entity.getProperty("three").onDependencyPath(entity.getProperty("two")));
        assertTrue("One should be on dependency path for three", entity.getProperty("three").onDependencyPath(entity.getProperty("one")));
        assertTrue("One should be on dependency path for two", entity.getProperty("two").onDependencyPath(entity.getProperty("one")));
        assertFalse("Two should not be on dependency path for one", entity.getProperty("one").onDependencyPath(entity.getProperty("two")));
        assertFalse("Three should not be on dependency path for one", entity.getProperty("one").onDependencyPath(entity.getProperty("three")));
        assertFalse("Three should not be on dependency path for two", entity.getProperty("two").onDependencyPath(entity.getProperty("three")));
    }

    @Test
    public void only_assigned_property_one_gets_revalidated_as_part_of_dependency_handling_upon_assignment_of_property_tree() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");

        entity.setOne("value");
        assertEquals(1, entity.oneCount);
        assertEquals(0, entity.twoCount);
        assertEquals(0, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
        
        entity.setThree("value");
        assertEquals(2, entity.oneCount);
        assertEquals(0, entity.twoCount);
        assertEquals(1, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
    }

    @Test
    public void all_assigned_properties_get_revalidated_as_part_of_dependency_handling_upon_assignment_of_property_tree() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");

        entity.setOne("value");
        assertEquals(1, entity.oneCount);
        assertEquals(0, entity.twoCount);
        assertEquals(0, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
        
        entity.setTwo("value");
        assertEquals(2, entity.oneCount);
        assertEquals(1, entity.twoCount);
        assertEquals(0, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
        
        entity.setThree("value");
        assertEquals(3, entity.oneCount);
        assertEquals(2, entity.twoCount);
        assertEquals(1, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
    }

    @Test
    public void dependency_is_traversed_correctly_when_prop_five_validation_fails_and_prop_four_is_assigned_trigerring_revalidation_of_props_one_and_five() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");

        entity.setOne("value");
        assertEquals(1, entity.oneCount);
        assertEquals(0, entity.twoCount);
        assertEquals(0, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
        
        entity.setTwo("value");
        assertEquals(2, entity.oneCount);
        assertEquals(1, entity.twoCount);
        assertEquals(0, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
        
        entity.setThree("value");
        assertEquals(3, entity.oneCount);
        assertEquals(2, entity.twoCount);
        assertEquals(1, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
        
        entity.setFive(INVALID);
        assertFalse(entity.getProperty("five").isValid());
        assertEquals(3, entity.oneCount);
        assertEquals(2, entity.twoCount);
        assertEquals(1, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(1, entity.fiveCount);
        
        entity.setFour("value");
        assertTrue(entity.getProperty("five").isValid());
        assertEquals("Should have been validated due to setting a value and 4 revalidations upon setting propties 2, 3, 4 and revalidation of property 5.", 5, entity.oneCount);
        assertEquals("Should have been validated due to setting a value and 1 revalidations upon setting property 3.", 2, entity.twoCount);
        assertEquals("Should have been validated due to setting a value.", 1, entity.threeCount);
        assertEquals("Should have been validated due to setting a value.", 1, entity.fourCount);
        assertEquals("Should have been validated due to setting a value and 1 revalidation upon setting property 4", 2, entity.fiveCount);
    }

    @Test
    public void unassigned_dependent_properties_do_not_get_revalidated() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        entity.setFour("value");

        assertEquals("Incorrect number of setter executions", 0, entity.oneCount);
        assertEquals("Incorrect number of setter executions", 0, entity.twoCount);
        assertEquals("Incorrect number of setter executions", 0, entity.threeCount);
        assertEquals("Incorrect number of setter executions", 1, entity.fourCount);
        assertEquals("Incorrect number of setter executions", 0, entity.fiveCount);
    }

}
