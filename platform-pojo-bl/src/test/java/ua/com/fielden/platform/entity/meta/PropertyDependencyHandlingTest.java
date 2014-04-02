package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.test_entities.EntityWithDependentProperties;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

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
    private EntityWithDependentProperties entity;

    @Before
    public void setUp() {
        module.getDomainValidationConfig().setValidator(EntityWithDependentProperties.class, "one", new IBeforeChangeEventHandler<Object>() {
            @Override
            public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
                ((EntityWithDependentProperties) property.getEntity()).oneCount++;
                return Result.successful(newValue);
            }
        });

        module.getDomainValidationConfig().setValidator(EntityWithDependentProperties.class, "two", new IBeforeChangeEventHandler<Object>() {
            @Override
            public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
                ((EntityWithDependentProperties) property.getEntity()).twoCount++;
                return Result.successful(newValue);
            }
        });

        module.getDomainValidationConfig().setValidator(EntityWithDependentProperties.class, "three", new IBeforeChangeEventHandler<Object>() {
            @Override
            public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
                ((EntityWithDependentProperties) property.getEntity()).threeCount++;
                return Result.successful(newValue);
            }
        });

        module.getDomainValidationConfig().setValidator(EntityWithDependentProperties.class, "four", new IBeforeChangeEventHandler<Object>() {
            @Override
            public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
                ((EntityWithDependentProperties) property.getEntity()).fourCount++;
                return Result.successful(newValue);
            }
        });

        module.getDomainValidationConfig().setValidator(EntityWithDependentProperties.class, "five", new IBeforeChangeEventHandler<Object>() {
            @Override
            public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
                ((EntityWithDependentProperties) property.getEntity()).fiveCount++;
                return new Result(new Exception());
            }
        });

        entity = factory.newByKey(EntityWithDependentProperties.class, "key");
    }

    @Test
    public void test_adding_property_to_dependency_path_when_everything_is_correct_and_there_is_circular_dependency() {
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
        } catch (final Exception ex) {
            fail("No exception is expected.");
        }
    }

    @Test
    public void test_adding_property_to_dependency_path_when_everything_is_correct_and_there_is_no_circular_dependency() {
        try {
            entity.getProperty("one").addToDependencyPath(entity.getProperty("four"));
        } catch (final Exception ex) {
            fail("No exception is expected.");
        }
    }

    @Test
    public void test_adding_null_property_to_dependency_path() {
        try {
            entity.getProperty("two").addToDependencyPath(null);
            fail("Should not be able to add null to the dependency path.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_adding_property_to_dependency_path_when_one_is_already_added() {
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
            fail("Should not be able to add property to the dependency path when another is already added.");
        } catch (final Exception ex) {

        }
    }

    @Test
    public void test_adding_property_not_from_the_dependency_list_in_case_of_circular_dependecy_to_dependency_path() {
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("four"));
            fail("Should not be able to add property not from dependency list to the dependency path.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_adding_property_not_from_the_dependency_list_in_case_of_non_circular_dependecy_to_dependency_path() {
        try {
            entity.getProperty("one").addToDependencyPath(entity.getProperty("four"));
        } catch (final Exception ex) {
            fail("No exception is expected.");
        }
    }

    @Test
    public void test_removing_null_property_from_dependency_path() {
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
            entity.getProperty("two").removeFromDependencyPath(null);
            fail("Should not be able to remove null property the dependency path.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_removing_property_from_dependency_path_when_none_was_added() {
        try {
            entity.getProperty("two").removeFromDependencyPath(entity.getProperty("one"));
            fail("Should not be able to remove property when the dependency path is empty.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_removing_property_from_dependency_path_when_everything_is_ok() {
        try {
            entity.getProperty("two").addToDependencyPath(entity.getProperty("one"));
            entity.getProperty("two").removeFromDependencyPath(entity.getProperty("one"));
        } catch (final Exception ex) {
            fail("Removal should have been successful.");
        }
    }

    @Test
    public void test_dependency_path_construction() {
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
    public void test_correctness_of_setter_ececution_upong_dependency_traversal_when_dependent_properties_were_not_set() {
        // first initialise the values to make original assignment
        entity.setOne("value");
        entity.setTwo("value");
        entity.oneCount = 0;
        entity.twoCount = 0;
        entity.threeCount = 0;
        // this assignment should trigger revalidation for properties one and two
        entity.setThree("value");

        assertEquals("Incorrect number of setter executions", 1, entity.threeCount);
        assertEquals("Incorrect number of setter executions", 2, entity.twoCount);
        assertEquals("Incorrect number of setter executions", 2, entity.oneCount);
    }

    @Test
    public void test_correctness_for_setter_execution_of_property_one_upon_dependency_traversal() {
        // first initialise the values to make original assignment
        entity.setTwo("value");
        entity.setThree("value");
        entity.oneCount = 0;
        entity.twoCount = 0;
        entity.threeCount = 0;

        // this assignment should trigger revalidation for property two, which in turn triggers for three
        entity.setOne("value");

        assertEquals("Incorrect number of setter executions", 1, entity.oneCount);
        assertEquals("Incorrect number of setter executions", 1, entity.twoCount);
        assertEquals("Incorrect number of setter executions", 1, entity.threeCount);

        entity.setTwo("value");
        entity.setThree("value");

        entity.oneCount = 0;
        entity.twoCount = 0;
        entity.threeCount = 0;

        entity.setOne("another value");

        assertEquals("Incorrect number of setter executions", 1, entity.oneCount);
        assertEquals("Incorrect number of setter executions", 1, entity.twoCount);
        assertEquals("Incorrect number of setter executions", 1, entity.threeCount);
    }

    @Test
    public void test_correctness_for_setter_execution_of_property_two_upon_dependency_traversal() {
        entity.setOne("value");
        entity.setTwo("value");
        entity.setThree("value");

        entity.oneCount = 0;
        entity.twoCount = 0;
        entity.threeCount = 0;

        entity.setTwo("another value");

        assertEquals("Incorrect number of setter executions", 2, entity.oneCount);
        assertEquals("Incorrect number of setter executions", 1, entity.twoCount);
        assertEquals("Incorrect number of setter executions", 1, entity.threeCount);
    }

    @Test
    public void test_correctness_for_setter_execution_of_property_three_upon_dependency_traversal() {
        entity.setOne("value");
        entity.setTwo("value");

        entity.oneCount = 0;
        entity.twoCount = 0;
        entity.threeCount = 0;

        entity.setThree("value");

        assertEquals("Incorrect number of setter executions", 1, entity.threeCount);
        assertEquals("Incorrect number of setter executions", 2, entity.twoCount);
        assertEquals("Incorrect number of setter executions", 2, entity.oneCount);
    }

    @Test
    public void test_dependency_traversal_when_prop_four_is_set_and_prop_five_validation_fails() {
        // first initialise the values to make original assignment
        entity.setOne("value");
        entity.setTwo("value");
        entity.setThree("value");
        entity.setFive("value");
        entity.oneCount = 0;
        entity.twoCount = 0;
        entity.threeCount = 0;
        entity.fourCount = 0;
        entity.fiveCount = 0;

        entity.setFour("value");

        assertEquals("Incorrect number of setter executions", 1, entity.oneCount);
        assertEquals("Incorrect number of setter executions", 1, entity.twoCount);
        assertEquals("Incorrect number of setter executions", 1, entity.threeCount);
        assertEquals("Incorrect number of setter executions", 1, entity.fourCount);
        assertEquals("Incorrect number of setter executions", 1, entity.fiveCount);
    }

    @Test
    public void test_dependency_traversal_when_prop_four_is_set_and_no_other_prop_has_value_assigned() {
        entity.setFour("value");

        assertEquals("Incorrect number of setter executions", 0, entity.oneCount);
        assertEquals("Incorrect number of setter executions", 0, entity.twoCount);
        assertEquals("Incorrect number of setter executions", 0, entity.threeCount);
        assertEquals("Incorrect number of setter executions", 1, entity.fourCount);
        assertEquals("Incorrect number of setter executions", 0, entity.fiveCount);
    }

}
