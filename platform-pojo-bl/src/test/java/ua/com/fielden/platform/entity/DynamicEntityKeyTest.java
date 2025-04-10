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
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;

import com.google.inject.Injector;
import ua.com.fielden.platform.test_entities.CorrectEntityWithDynamicEntityKey;

/**
 * Unit test to ensure correct composition of composite entity keys with DynamicEntityKey.
 *
 * @author TG Team
 *
 */
public class DynamicEntityKeyTest {

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
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
    public void test_instantiation_of_entity_with_dynamic_key_with_values_setting_after_instantiation() {
        final CorrectEntityWithDynamicEntityKey instance = factory.newEntity(CorrectEntityWithDynamicEntityKey.class);
        instance.setProperty1(1);
        instance.setProperty2(2);
        final DynamicEntityKey key = instance.getKey();
        assertEquals("Incorrect key member value.", 1, key.getKeyValues()[0]);
        assertEquals("Incorrect key member value.", 2, key.getKeyValues()[1]);
    }

    @Test
    public void test_instantiation_of_entity_with_dynamic_key_with_values_using_factory() {
        final CorrectEntityWithDynamicEntityKey instance = factory.newByKey(CorrectEntityWithDynamicEntityKey.class, 1, 2);
        final DynamicEntityKey key = instance.getKey();
        assertEquals("Incorrect key member value.", 1, key.getKeyValues()[0]);
        assertEquals("Incorrect key member value.", 2, key.getKeyValues()[1]);
    }

    @Test
    public void test_that_composite_key_is_determined_correctly() {
        final CorrectEntityWithDynamicEntityKey instance = new CorrectEntityWithDynamicEntityKey();
        final DynamicEntityKey key = new DynamicEntityKey(instance);
        assertEquals("Incorrect number of properties.", 2, key.getMemberNames().size());
        assertEquals("Incorrect order of properties.", "property1", key.getMemberNames().get(0));
        assertEquals("Incorrect order of properties.", "property2", key.getMemberNames().get(1));
    }

    @Test
    public void test_that_composite_key_is_determined_correctly_for_entity_with_reversed_declaration_order() {
        final CorrectEntityWithReversedOrder instance = new CorrectEntityWithReversedOrder();
        final DynamicEntityKey key = new DynamicEntityKey(instance);
        assertEquals("Incorrect number of properties.", 2, key.getMemberNames().size());
        assertEquals("Incorrect order of properties.", "property2", key.getMemberNames().get(0));
        assertEquals("Incorrect order of properties.", "property1", key.getMemberNames().get(1));
    }

    @Test
    public void test_that_creation_of_the_key_fails_for_duplicate_order_annotations() {
        try {
            new EntityWithDuplicateOrder();
            fail("Should have failed with duplicate order exception.");
        } catch (final ReflectionException ex) {
            assertEquals("Could not get key members for type [class ua.com.fielden.platform.entity.DynamicEntityKeyTest$EntityWithDuplicateOrder]", ex.getMessage());
            assertEquals("ua.com.fielden.platform.reflection.exceptions.ReflectionException: Annotation [ua.com.fielden.platform.entity.annotation.CompositeKeyMember] in class [ua.com.fielden.platform.entity.DynamicEntityKeyTest$EntityWithDuplicateOrder] for property [property2] has a duplicate order value of [1], which is already present in property [protected java.lang.Long ua.com.fielden.platform.entity.DynamicEntityKeyTest$EntityWithDuplicateOrder.property1].", ex.getCause().getMessage());
        }
    }

    @Test
    public void test_that_creation_of_the_key_fails_for_entity_with_no_key_members() {
        try {
            new EntityWithoutKeyMembers();
            fail("Should have failed for an entity with a simple key.");
        } catch (final EntityDefinitionException ex) {
            assertEquals("Composite key should have at least one member.", ex.getMessage());
        }
    }

    @Test
    public void test_that_creation_of_the_key_failes_for_entity_with_single_key_member() {
        final EntityWithSingleKeyMember instance = new EntityWithSingleKeyMember();
        final DynamicEntityKey key = new DynamicEntityKey(instance);
        assertEquals("Incorrect number of key members.", 1, key.getMemberNames().size());
        assertEquals("Incorrect key member.", "property1", key.getMemberNames().get(0));
    }

    @Test
    public void test_comparison_for_equal_not_null_keys() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(0);
        one.setProperty2(0);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(0);
        two.setProperty2(0);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Keys should be equal", keyOne.compareTo(keyTwo) == 0);
        assertTrue("Keys should be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_has_greater_first_member_is_greater() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(1);
        one.setProperty2(0);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(0);
        two.setProperty2(0);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("First key should be greater", keyOne.compareTo(keyTwo) > 0);
        assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_has_greater_second_member_is_greater() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(0);
        one.setProperty2(1);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(0);
        two.setProperty2(0);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("First key should be greater", keyOne.compareTo(keyTwo) > 0);
        assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_second_key_has_greater_first_member_is_greater() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(0);
        one.setProperty2(0);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(1);
        two.setProperty2(0);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Second key should be greater", keyOne.compareTo(keyTwo) < 0);
        assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_second_key_has_greater_second_member_is_greater() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(0);
        one.setProperty2(0);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(0);
        two.setProperty2(1);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Second key should be greater", keyOne.compareTo(keyTwo) < 0);
        assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_member_is_null() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(null);
        one.setProperty2(0);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(0);
        two.setProperty2(0);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) < 0);
        assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_and_seocnd_key_members_are_null() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(null);
        one.setProperty2(null);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(0);
        two.setProperty2(0);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) < 0);
        assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_member_for_second_key_is_null() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(0);
        one.setProperty2(0);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(null);
        two.setProperty2(0);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) > 0);
        assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_and_second_key_members_for_second_key_are_null() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(0);
        one.setProperty2(0);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(null);
        two.setProperty2(null);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) > 0);
        assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_members_are_not_null_but_second_key_member_for_first_key_is_null() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(0);
        one.setProperty2(null);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(0);
        two.setProperty2(0);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) < 0);
        assertFalse("Keys should not be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_first_key_members_are_not_null_but_second_key_members_for_both_keys_are_null() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(0);
        one.setProperty2(null);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(0);
        two.setProperty2(null);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) == 0);
        assertTrue("Keys should be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_comparison_when_all_key_members_for_both_keys_are_null() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(null);
        one.setProperty2(null);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        final CorrectEntityWithDynamicEntityKey two = new CorrectEntityWithDynamicEntityKey();
        two.setProperty1(null);
        two.setProperty2(null);
        final DynamicEntityKey keyTwo = new DynamicEntityKey(two);

        assertTrue("Comparison result is incorrect", keyOne.compareTo(keyTwo) == 0);
        assertTrue("Keys should be equal", keyOne.equals(keyTwo));
    }

    @Test
    public void test_key_member_values_are_obtained_correctly() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(null);
        one.setProperty2(1);
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        one.setKey(keyOne);
        final Object[] values = one.getKey().getKeyValues();

        assertEquals("Incorrect number of values.", 2, values.length);
        assertNull("Incorrect value for the first key property.", values[0]);
        assertEquals("Incorrect value for the second key property.", 1, values[1]);
    }

    @Test
    public void test_string_representation() {
        final CorrectEntityWithDynamicEntityKey one = new CorrectEntityWithDynamicEntityKey();
        one.setProperty1(null);
        one.setProperty2(null);
        final DynamicEntityKey keyOne = one.getKey();
        assertNotNull(keyOne);

        assertEquals("Incorrect string representation", AbstractEntity.KEY_NOT_ASSIGNED, keyOne.toString());

        one.setProperty1(null);
        one.setProperty2(2);
        assertEquals("Incorrect string representation", "2", keyOne.toString());

        one.setProperty1(1);
        one.setProperty2(null);
        assertEquals("Incorrect string representation", "1", keyOne.toString());

        one.setProperty1(1);
        one.setProperty2(2);
        assertEquals("Incorrect string representation", "1 2", keyOne.toString());
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
    static class EntityWithoutKeyMembers extends AbstractEntity<DynamicEntityKey> {
        private static final long serialVersionUID = 1L;
        protected Long property1;
        protected Long property2;

        public EntityWithoutKeyMembers() {
        }
    }

    @KeyType(DynamicEntityKey.class)
    static class EntityWithSingleKeyMember extends AbstractEntity<DynamicEntityKey> {
        private static final long serialVersionUID = 1L;
        @IsProperty
        @CompositeKeyMember(1)
        protected Long property1;

        @IsProperty
        protected Long property2;

    }

}
