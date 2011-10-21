package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * Unit test to ensure correct composition of composite entity keys with DynamicEntityKey.
 *
 * @author TG Team
 *
 */
public class DynamicEntityKeyTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_instantiation_of_entity_with_dynamic_key_with_factory_using_default_constructor() {
	final CorrectEntityWithDynamicEntityKey instance = factory.newEntity(CorrectEntityWithDynamicEntityKey.class);
	assertNotNull("Dynamic entity key should have been created.", instance.getKey());
	final DynamicEntityKey key = instance.getKey();
	assertNull("Incorrect key member value.", key.getKeyValues()[0]);
	assertNull("Incorrect key member value.", key.getKeyValues()[1]);
    }

    @Test
    public void test_instantiation_of_entity_with_dynamic_key_with_values_using_factory() {
	final CorrectEntityWithDynamicEntityKey instance = factory.newEntity(CorrectEntityWithDynamicEntityKey.class);
	instance.setProperty1(1L);
	instance.setProperty2(2L);
	final DynamicEntityKey key = instance.getKey();
	assertEquals("Incorrect key member value.", 1L, key.getKeyValues()[0]);
	assertEquals("Incorrect key member value.", 2L, key.getKeyValues()[1]);
    }

    @Test
    public void test_that_composite_key_is_determined_correctly() {
	final CorrectEntityWithDynamicEntityKey instance = new CorrectEntityWithDynamicEntityKey();
	final DynamicEntityKey key = new DynamicEntityKey(instance);
	assertEquals("Incorrect number of properties.", 2, key.getPropertyExpressions().size());
	assertEquals("Incorrect order of properties.", "entity.property1", key.getPropertyExpressions().get(0).getExpression());
	assertEquals("Incorrect order of properties.", "entity.property2", key.getPropertyExpressions().get(1).getExpression());
    }

    @Test
    public void test_that_composite_key_is_determined_correctly_for_entity_with_reversed_declaration_order() {
	final CorrectEntityWithReversedOrder instance = new CorrectEntityWithReversedOrder();
	final DynamicEntityKey key = new DynamicEntityKey(instance);
	assertEquals("Incorrect number of properties.", 2, key.getPropertyExpressions().size());
	assertEquals("Incorrect order of properties.", "entity.property2", key.getPropertyExpressions().get(0).getExpression());
	assertEquals("Incorrect order of properties.", "entity.property1", key.getPropertyExpressions().get(1).getExpression());
    }

    @Test
    public void test_that_creation_of_the_key_failes_for_duplicate_order_annotations() {
	try {
	    new EntityWithDuplicateOrder();
	    fail("Should have failed with duplicate order exception.");
	} catch (final Exception ex) {
	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void test_that_creation_of_the_key_failes_for_entity_with_no_annotations() {
	final EntityWithNoAnnotation instance = new EntityWithNoAnnotation();
	try {
	    new DynamicEntityKey(instance);
	    fail("Should have failed for an entity with a simple key.");
	} catch (final Exception ex) {
	    System.out.println(ex.getMessage());
	}
    }

    @Test
    public void test_comparison_for_equal_not_null_keys() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = 0L;
	one.property2 = 0L;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = 0L;
	two.property2 = 0L;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Keys should be equal", keyOne.compareTo(keyTwo) == 0);
	assertTrue("Keys should be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_has_greater_first_member_is_greater() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = 1L;
	one.property2 = 0L;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = 0L;
	two.property2 = 0L;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("First key should be greater", keyOne.compareTo(keyTwo) > 0);
	assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_has_greater_second_member_is_greater() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = 0L;
	one.property2 = 1L;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = 0L;
	two.property2 = 0L;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("First key should be greater", keyOne.compareTo(keyTwo) > 0);
	assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_second_key_has_greater_first_member_is_greater() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = 0L;
	one.property2 = 0L;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = 1L;
	two.property2 = 0L;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Second key should be greater", keyOne.compareTo(keyTwo) < 0);
	assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_second_key_has_greater_second_member_is_greater() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = 0L;
	one.property2 = 0L;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = 0L;
	two.property2 = 1L;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Second key should be greater", keyOne.compareTo(keyTwo) < 0);
	assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_member_is_null() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = null;
	one.property2 = 0L;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = 0L;
	two.property2 = 0L;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) < 0);
	assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_and_seocnd_key_members_are_null() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = null;
	one.property2 = null;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = 0L;
	two.property2 = 0L;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) < 0);
	assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_member_for_second_key_is_null() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = 0L;
	one.property2 = 0L;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = null;
	two.property2 = 0L;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) > 0);
	assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_and_second_key_members_for_second_key_are_null() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = 0L;
	one.property2 = 0L;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = null;
	two.property2 = null;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) > 0);
	assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_members_are_not_null_but_second_key_member_for_first_key_is_null() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = 0L;
	one.property2 = null;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = 0L;
	two.property2 = 0L;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) < 0);
	assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_members_are_not_null_but_second_key_members_for_both_keys_are_null() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = 0L;
	one.property2 = null;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = 0L;
	two.property2 = null;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) == 0);
	assertTrue("Keys should be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_all_key_members_for_both_keys_are_null() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = null;
	one.property2 = null;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
	two.property1 = null;
	two.property2 = null;
	final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

	assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) == 0);
	assertTrue("Keys should be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_key_member_values_are_obtained_correctly() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = null;
	one.property2 = 1L;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	one.setKey(keyOne);
	final Object[] values = one.getKey().getKeyValues();

	assertEquals("Incorrect number of values.", 2, values.length);
	assertNull("Incorrect value for the first key property.", values[0]);
	assertEquals("Incorrect value for the second key property.", 1L, values[1]);
    }

    @Test
    public void test_string_representation() {
	final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
	one.property1 = null;
	one.property2 = null;
	final DynamicEntityKey keyOne = new DynamicEntityKey(one);
	one.setKey(keyOne);

	assertEquals("Incorrect string representation", "", "");

	one.property1 = null;
	one.property2 = 2L;
	assertEquals("Incorrect string representation", "2", "2");

	one.property1 = 1L;
	one.property2 = null;
	assertEquals("Incorrect string representation", "1", "1");

	one.property1 = 1L;
	one.property2 = 2L;
	assertEquals("Incorrect string representation", "12", "12");
    }

    @KeyType(DynamicEntityKey.class)
    static class CorrectEntityWithReversedOrder extends AbstractEntity<DynamicEntityKey> {
	private static final long serialVersionUID = 1L;
	@IsProperty
	@CompositeKeyMember(2)
	protected Long property1;
	@IsProperty
	@CompositeKeyMember(1)
	protected Long property2;

	public CorrectEntityWithReversedOrder() {
	}
    }

    @KeyType(DynamicEntityKey.class)
    static class EntityWithDuplicateOrder extends AbstractEntity<DynamicEntityKey> {
	private static final long serialVersionUID = 1L;
	@IsProperty
	@CompositeKeyMember(1)
	protected Long property1;
	@IsProperty
	@CompositeKeyMember(1)
	protected Long property2;

	public EntityWithDuplicateOrder() {
	}
    }

    @KeyType(DynamicEntityKey.class)
    static class EntityWithNoAnnotation extends AbstractEntity<DynamicEntityKey> {
	private static final long serialVersionUID = 1L;
	protected Long property1;
	protected Long property2;

	public EntityWithNoAnnotation() {
	}
    }
}
