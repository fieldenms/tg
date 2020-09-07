package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.meta.test_entities.EntityWithDependentProperties.INVALID;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.test_entities.EntityWithCircularDependentPropertiesAndDefiners;
import ua.com.fielden.platform.entity.meta.test_entities.EntityWithDependentProperties;
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

    @Test
    public void handling_of_dependent_properties_can_rectify_errors_in_transitive_dependencies() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");

        entity.setOne(INVALID);
        assertFalse(entity.getProperty("one").isValid());
        assertEquals(1, entity.oneCount);
        assertEquals(0, entity.twoCount);
        assertEquals(0, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
        
        entity.setTwo(INVALID);
        assertFalse(entity.getProperty("one").isValid());
        assertFalse(entity.getProperty("two").isValid());
        assertEquals(1, entity.oneCount);
        assertEquals(1, entity.twoCount);
        assertEquals(0, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
        
        // assigning a valid value to property three triggers dependency handling -- revalidation of properties one and two
        entity.setThree("value");
        assertFalse(entity.getProperty("one").isValid());
        assertFalse(entity.getProperty("two").isValid());
        assertEquals(2, entity.oneCount);
        assertEquals(2, entity.twoCount);
        assertEquals(1, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(0, entity.fiveCount);
        
        entity.setFive(INVALID);
        assertFalse(entity.getProperty("one").isValid());
        assertFalse(entity.getProperty("two").isValid());
        assertFalse(entity.getProperty("five").isValid());
        assertEquals(2, entity.oneCount);
        assertEquals(2, entity.twoCount);
        assertEquals(1, entity.threeCount);
        assertEquals(0, entity.fourCount);
        assertEquals(1, entity.fiveCount);
        
        // assigning a valid value to property four triggers dependency handling
        // 1.     Revalidation of property one, which fails as (oneCount <= 3) == true and the attempted value is INVALID
        // 2.     Revalidation of property five, which succeeds and triggers revalidation of its dependent properties -- namely property one.
        // 2.1.   Revalidation of property one succeeds as (oneCount <= 3) == false and triggers revalidation of its dependent properties -- namely property two.
        // 2.1.1. Revalidation of property two succeeds as (twoCount <= 2) == false.
        // all previously invalid properties should now become valid
        entity.setFour("value");
        assertTrue(entity.getProperty("one").isValid());
        assertTrue(entity.getProperty("two").isValid());
        assertTrue(entity.getProperty("five").isValid());
        assertEquals(4, entity.oneCount);
        assertEquals(3, entity.twoCount);
        assertEquals(2, entity.threeCount);
        assertEquals(1, entity.fourCount);
        assertEquals(2, entity.fiveCount);
    }
    
    @Test
    public void requiredness_revalidation_errors_are_handled() {
        final EntityWithDependentProperties entity = factory.newByKey(EntityWithDependentProperties.class, "key");
        entity.getProperty("one").resetState();
        entity.getProperty("one").setRequired(true);
        assertTrue(entity.getProperty("one").isValid());
        entity.getProperty("two").resetState();
        
        entity.setThree("value");

        assertFalse(entity.getProperty("one").isValid());
        assertEquals("Incorrect number of setter executions", 0, entity.oneCount);
        assertEquals("Incorrect number of setter executions", 1, entity.twoCount);
        assertEquals("Incorrect number of setter executions", 1, entity.threeCount);
        assertEquals("Incorrect number of setter executions", 0, entity.fourCount);
        assertEquals("Incorrect number of setter executions", 0, entity.fiveCount);
    }

    @Test
    public void revalidation_can_handle_a_case_of_circular_dependencies_with_validation_restricting_assignment_of_some_props_before_others_and_requiredness_managed_via_definers() {
        // Property One has property Two and Three as dependent.
        // Property Two has property Three as dependent.
        // Property Three has property Two as dependent.
        //
        // 1 -----> 3
        //  \     //      <- 2 and 3 are mutually dependent  
        //   \   //
        //      2  
        //
        // Property Three cannot be assigned before property One.
        // Property One is required by definition and its definer determines requiredness for properties Two and Three depending on the fact whether either of them is populated.
        final EntityWithCircularDependentPropertiesAndDefiners entity = factory.newByKey(EntityWithCircularDependentPropertiesAndDefiners.class, "key");

        entity.getProperty("two").setRequired(true);
        entity.getProperty("three").setRequired(true);

        // setting null into properties Two makes it invalid, which is required to trigger an error recover process when property One is assigned
        // this is believed to be the culprit causing the problem, reported in issue #xxxx
        entity.setTwo(null);
        assertFalse(entity.getProperty("two").isValid());

        // attempting to set value for property Tree
        // which fails because One is not assigned
        entity.setThree("three");
        assertFalse(entity.getProperty("three").isValid());
        assertTrue(entity.getProperty("two").isRequired());
        assertTrue(entity.getProperty("three").isRequired());

        // at this stage all 3 properties are invalid
        // setting One should trigger a revalidation process resulting in:
        // a) One is assigned
        // b) Two is not assigned and not required
        // c) Three is assigned and not required
        entity.setOne("one");
        assertEquals("one", entity.getOne());
        assertNull(entity.getTwo());
        assertEquals("three", entity.getThree());
        assertTrue(entity.getProperty("three").isValid());
        assertFalse(entity.getProperty("two").isRequired());
        assertFalse(entity.getProperty("three").isRequired());
    }

}
