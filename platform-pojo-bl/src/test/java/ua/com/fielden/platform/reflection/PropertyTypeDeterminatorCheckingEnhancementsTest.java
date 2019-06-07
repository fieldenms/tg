package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker;
import ua.com.fielden.platform.entity.proxy.TgOwnerEntity;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

/**
 * Test case for checking of instrumentation, proxying (in {@link PropertyTypeDeterminator}) and generation (in {@link DynamicEntityClassLoader}) characteristics
 * of newly created entities. Also it checks the method {@link PropertyTypeDeterminator#stripIfNeeded(Class)}.
 * <p>
 * TODO it would be nice to provide unit tests for {@link PropertyTypeDeterminator#isLoadedByHibernate(Class)} characteristic.
 *
 * @author TG Team
 *
 */
public class PropertyTypeDeterminatorCheckingEnhancementsTest {
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private final DynamicEntityClassLoader cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
    
    private final Class<TgOwnerEntity> entityType;
    private final Class<AbstractEntity<?>> entityTypeGenerated;
    
    public PropertyTypeDeterminatorCheckingEnhancementsTest() throws ClassNotFoundException {
        entityType = TgOwnerEntity.class;
        entityTypeGenerated = (Class<AbstractEntity<?>>) cl.startModification(entityType.getName()).modifyTypeName(new DynamicTypeNamingService().nextTypeName(entityType.getName())).endModification();
    }

    //////////////////////////////////// isInstrumented ////////////////////////////////////
    @Test
    public void isIntrumented_is_false_for_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(entityType, 1L);
        assertFalse(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_true_for_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(entityType, 1L);
        assertTrue(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_false_for_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(entityTypeGenerated, 1L);
        assertFalse(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_true_for_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(entityTypeGenerated, 1L);
        assertTrue(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_false_for_proxied_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(entityType, "entityProp"), 1L);
        assertFalse(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_true_for_proxied_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(EntityProxyContainer.proxy(entityType, "entityProp"), 1L);
        assertTrue(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_false_for_proxied_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(entityTypeGenerated, "entityProp"), 1L);
        assertFalse(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    @Test
    public void isIntrumented_is_true_for_proxied_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(EntityProxyContainer.proxy(entityTypeGenerated, "entityProp"), 1L);
        assertTrue(PropertyTypeDeterminator.isInstrumented(entity.getClass()));
    }
    
    //////////////////////////////////// isProxied ////////////////////////////////////
    @Test
    public void isProxied_is_false_for_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(entityType, 1L);
        assertFalse(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_false_for_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(entityType, 1L);
        assertFalse(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_false_for_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(entityTypeGenerated, 1L);
        assertFalse(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_false_for_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(entityTypeGenerated, 1L);
        assertFalse(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_true_for_proxied_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(entityType, "entityProp"), 1L);
        assertTrue(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_true_for_proxied_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(EntityProxyContainer.proxy(entityType, "entityProp"), 1L);
        assertTrue(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_true_for_proxied_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(entityTypeGenerated, "entityProp"), 1L);
        assertTrue(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    @Test
    public void isProxied_is_true_for_proxied_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(EntityProxyContainer.proxy(entityTypeGenerated, "entityProp"), 1L);
        assertTrue(PropertyTypeDeterminator.isProxied(entity.getClass()));
    }
    
    //////////////////////////////////// isGenerated ////////////////////////////////////
    @Test
    public void isGenerated_is_false_for_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(entityType, 1L);
        assertFalse(DynamicEntityClassLoader.isGenerated(entity.getClass()));
    }

    @Test
    public void isGenerated_is_false_for_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(entityType, 1L);
        assertFalse(DynamicEntityClassLoader.isGenerated(entity.getClass()));
    }
    
    @Test
    public void isGenerated_is_true_for_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(entityTypeGenerated, 1L);
        assertTrue(DynamicEntityClassLoader.isGenerated(entity.getClass()));
    }
    
    @Test
    public void isGenerated_is_true_for_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(entityTypeGenerated, 1L);
        assertTrue(DynamicEntityClassLoader.isGenerated(entity.getClass()));
    }
    
    @Test
    public void isGenerated_is_false_for_proxied_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(entityType, "entityProp"), 1L);
        assertFalse(DynamicEntityClassLoader.isGenerated(entity.getClass()));
    }
    
    @Test
    public void isGenerated_is_false_for_proxied_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(EntityProxyContainer.proxy(entityType, "entityProp"), 1L);
        assertFalse(DynamicEntityClassLoader.isGenerated(entity.getClass()));
    }
    
    @Test
    public void isGenerated_is_true_for_proxied_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(entityTypeGenerated, "entityProp"), 1L);
        assertTrue(DynamicEntityClassLoader.isGenerated(entity.getClass()));
    }
    
    @Test
    public void isGenerated_is_true_for_proxied_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(EntityProxyContainer.proxy(entityTypeGenerated, "entityProp"), 1L);
        assertTrue(DynamicEntityClassLoader.isGenerated(entity.getClass()));
    }

    //////////////////////////////////// stripIfNeeded ////////////////////////////////////
    @Test(expected = ReflectionException.class)
    public void stripIfNeeded_throws_exception_if_null_is_provided() {
        PropertyTypeDeterminator.stripIfNeeded(null);
    }

    @Test
    public void stripIfNeeded_returns_original_type_for_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(entityType, 1L);
        assertEquals(entityType, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_original_type_for_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(entityType, 1L);
        assertEquals(entityType, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_generated_type_for_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(entityTypeGenerated, 1L);
        assertEquals(entityTypeGenerated, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_generated_type_for_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(entityTypeGenerated, 1L);
        assertEquals(entityTypeGenerated, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_original_type_for_proxied_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(entityType, "entityProp"), 1L);
        assertEquals(entityType, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_original_type_for_proxied_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(EntityProxyContainer.proxy(entityType, "entityProp"), 1L);
        assertEquals(entityType, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_generated_type_for_proxied_generated_uninstrumented_entity() {
        final AbstractEntity<?> entity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(entityTypeGenerated, "entityProp"), 1L);
        assertEquals(entityTypeGenerated, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }
    
    @Test
    public void stripIfNeeded_returns_generated_type_for_proxied_generated_instrumented_entity() {
        final AbstractEntity<?> entity = factory.newEntity(EntityProxyContainer.proxy(entityTypeGenerated, "entityProp"), 1L);
        assertEquals(entityTypeGenerated, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }

    @Test
    public void stripIfNeeded_returns_original_type_for_mock_not_found_entity() {
        final Class<? extends TgOwnerEntity> mockType = MockNotFoundEntityMaker.mock(TgOwnerEntity.class);
        final AbstractEntity<?> entity = factory.newEntity(mockType, 1L);
        assertEquals(TgOwnerEntity.class, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));
    }

    @Test
    public void baseEntityType_correctly_determines_the_base_type_for_dynamically_generated_entity_types() throws Exception {
        final DynamicEntityClassLoader cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
        final String NEW_PROPERTY_DESC = "Description  for new money property";
        final String NEW_PROPERTY_TITLE = "New money property";
        final String NEW_PROPERTY_EXPRESSION = "2 * 3 - [integerProp]";
        final String NEW_PROPERTY = "newProperty";
        final Calculated calculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();
        final NewProperty pd1 = new NewProperty(NEW_PROPERTY, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, calculated);
        final NewProperty pd2 = new NewProperty(NEW_PROPERTY + 1, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, calculated);
        // first enhancement
        final Class<? extends AbstractEntity<?>> oneTimeEnhancedType = (Class<? extends AbstractEntity<?>>) cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd1).endModification();
        // second enhancement
        final Class<? extends AbstractEntity<?>> twoTimesEnhancedType = (Class<? extends AbstractEntity<?>>) cl.startModification(oneTimeEnhancedType.getName()).addProperties(pd2).endModification();

        // both enhanced types should have EntityBeingEnhanced as their base type
        assertEquals(EntityBeingEnhanced.class, PropertyTypeDeterminator.baseEntityType(oneTimeEnhancedType));
        assertEquals(EntityBeingEnhanced.class, PropertyTypeDeterminator.baseEntityType(twoTimesEnhancedType));
    }
    
    @Test
    public void baseEntityType_correctly_determines_the_base_type_for_enhanced_entity_types() throws Exception {
        final AbstractEntity<?> entity = factory.newEntity(EntityProxyContainer.proxy(entityTypeGenerated, "entityProp"), 1L);
        assertEquals(entityTypeGenerated, PropertyTypeDeterminator.stripIfNeeded(entity.getClass()));

        assertEquals(TgOwnerEntity.class, PropertyTypeDeterminator.baseEntityType(entity.getType()));
    }

}
