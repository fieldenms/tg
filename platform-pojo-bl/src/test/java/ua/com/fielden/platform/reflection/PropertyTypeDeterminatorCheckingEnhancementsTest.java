package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.EntityForProxy;
import ua.com.fielden.platform.entity.proxy.EntityProxyFactory;
import ua.com.fielden.platform.entity.proxy.OwnerEntity;
import ua.com.fielden.platform.entity.proxy.ProxyMode;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.serialisation.jackson.entities.EmptyEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 * Test case for {@link PropertyTypeDeterminator}.
 *
 * @author TG Team
 *
 */
public class PropertyTypeDeterminatorCheckingEnhancementsTest {
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private final DynamicEntityClassLoader cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
    private final Class<AbstractEntity<?>> emptyEntityTypeEnhanced;
    
    public PropertyTypeDeterminatorCheckingEnhancementsTest() throws ClassNotFoundException {
        emptyEntityTypeEnhanced = (Class<AbstractEntity<?>>) cl.startModification(EmptyEntity.class.getName()).modifyTypeName(new DynamicTypeNamingService().nextTypeName(EmptyEntity.class.getName())).endModification();
    }

    //////////////////////////////////// isInstrumented ////////////////////////////////////
    @Test
    public void isIntrumented_is_false_for_uninstrumented_entity() {
        final EmptyEntity entity = factory.newPlainEntity(EmptyEntity.class, 1L);
        assertFalse(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_true_for_instrumented_entity() {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, 1L);
        assertTrue(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_false_for_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = factory.newPlainEntity(emptyEntityTypeEnhanced, 1L);
        assertFalse(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_true_for_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(emptyEntityTypeEnhanced, 1L);
        assertTrue(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_false_for_proxied_entity() {
        final EntityProxyFactory<EntityForProxy> epf = new EntityProxyFactory<>(EntityForProxy.class);
        final OwnerEntity owner = factory.newPlainEntity(OwnerEntity.class, 1L);
        owner.setProp1(epf.create(10L, owner, "prop1", null, ProxyMode.STRICT));
    
        final AbstractEntity<?> entity = (AbstractEntity<?>) owner.get("prop1");
        assertFalse(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    //////////////////////////////////// isProxied ////////////////////////////////////
    @Test
    public void isProxied_is_false_for_uninstrumented_entity() {
        final EmptyEntity entity = factory.newPlainEntity(EmptyEntity.class, 1L);
        assertFalse(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_false_for_instrumented_entity() {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, 1L);
        assertFalse(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_false_for_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = factory.newPlainEntity(emptyEntityTypeEnhanced, 1L);
        assertFalse(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_false_for_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(emptyEntityTypeEnhanced, 1L);
        assertFalse(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_true_for_proxied_entity() {
        final EntityProxyFactory<EntityForProxy> epf = new EntityProxyFactory<>(EntityForProxy.class);
        final OwnerEntity owner = factory.newPlainEntity(OwnerEntity.class, 1L);
        owner.setProp1(epf.create(10L, owner, "prop1", null, ProxyMode.STRICT));
    
        final AbstractEntity<?> entity = (AbstractEntity<?>) owner.get("prop1");
        assertTrue(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }

    //////////////////////////////////// stripIfNeeded ////////////////////////////////////
    @Test
    public void stripIfNeeded_returns_original_type_for_uninstrumented_entity() {
        final EmptyEntity entity = factory.newPlainEntity(EmptyEntity.class, 1L);
        assertEquals(EmptyEntity.class, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_original_type_for_instrumented_entity() {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, 1L);
        assertEquals(EmptyEntity.class, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_original_type_for_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = factory.newPlainEntity(emptyEntityTypeEnhanced, 1L);
        assertEquals(emptyEntityTypeEnhanced, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_original_type_for_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(emptyEntityTypeEnhanced, 1L);
        assertEquals(emptyEntityTypeEnhanced, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_original_type_for_proxied_entity() {
        final EntityProxyFactory<EntityForProxy> epf = new EntityProxyFactory<>(EntityForProxy.class);
        final OwnerEntity owner = factory.newPlainEntity(OwnerEntity.class, 1L);
        owner.setProp1(epf.create(10L, owner, "prop1", null, ProxyMode.STRICT));
    
        final AbstractEntity<?> entity = (AbstractEntity<?>) owner.get("prop1");
        assertEquals(EntityForProxy.class, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
}
