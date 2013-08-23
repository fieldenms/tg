package ua.com.fielden.platform.entity.common_ancestor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * Unit test to ensure that entities composed from common abstract ancestor with common part of dynamic key will work.
 *
 * @author TG Team
 *
 */
public class AbstractCommonEntityTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_instantiation_of_entity_with_dynamic_key_with_factory_using_default_constructor1() {
	final DescendantEntity instance = new DescendantEntity();
	assertNotNull("Dynamic entity key should have been created.", instance.getKey());
	final DynamicEntityKey key = instance.getKey();
	assertNull("Incorrect key member value.", key.getKeyValues()[0]);
	assertNull("Incorrect key member value.", key.getKeyValues()[1]);
    }

    @Test
    public void test_instantiation_of_entity_with_dynamic_key_with_factory_using_default_constructor2() {
	final DescendantEntity instance = factory.newEntity(DescendantEntity.class);
	assertNotNull("Dynamic entity key should have been created.", instance.getKey());
	final DynamicEntityKey key = instance.getKey();
	assertNull("Incorrect key member value.", key.getKeyValues()[0]);
	assertNull("Incorrect key member value.", key.getKeyValues()[1]);
    }

    @Test
    public void test_instantiation_of_entity_with_dynamic_key_with_values_setting_after_instantiation() {
	final DescendantEntity instance = factory.newEntity(DescendantEntity.class);
	instance.setProperty1(factory.newEntity(JustEntity.class));
	instance.setProperty2(2L);
	final DynamicEntityKey key = instance.getKey();
	assertEquals("Incorrect key member value.", factory.newEntity(JustEntity.class), key.getKeyValues()[0]);
	assertEquals("Incorrect key member value.", 2L, key.getKeyValues()[1]);
    }

    @Test
    public void test_instantiation_of_entity_with_dynamic_key_with_values_using_factory() {
	final DescendantEntity instance = factory.newByKey(DescendantEntity.class, factory.newEntity(JustEntity.class), 2L);
	final DynamicEntityKey key = instance.getKey();
	assertEquals("Incorrect key member value.", factory.newEntity(JustEntity.class), key.getKeyValues()[0]);
	assertEquals("Incorrect key member value.", 2L, key.getKeyValues()[1]);
    }

}
