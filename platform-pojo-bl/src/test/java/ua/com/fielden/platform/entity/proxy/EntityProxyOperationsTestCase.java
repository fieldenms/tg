package ua.com.fielden.platform.entity.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.proxy.old.ProxyTestCase;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 * 
 * This test case is to replace the test case {@link ProxyTestCase} when the Javassist-based approached to proxing is phased out.
 * 
 * @author TG Team
 *
 */
public class EntityProxyOperationsTestCase {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void access_or_mutation_for_proxied_property_of_entity_type_for_instance_created_via_factory_is_restricted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "entityProp").entityType;
        
        // creation via factory
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
        
        try {
            owner.getEntityProp();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [getEntityProp] is restricted due to unfetched property [entityProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            final TgEntityForProxy value = factory.newByKey(TgEntityForProxy.class, "KEY1");
            owner.setEntityProp(value);
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setEntityProp] is restricted due to unfetched property [entityProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
        
        try {
            final TgEntityForProxy prop1Proxy = factory.newByKey(TgEntityForProxy.class, "KEY1");
            owner.beginInitialising();
            owner.setEntityProp(prop1Proxy);
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setEntityProp] is restricted due to unfetched property [entityProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }


    }

    @Test
    public void access_or_mutation_for_proxied_property_of_entity_type_for_instance_created_without_factory_is_restricted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "entityProp").entityType;
        
        // this is like creation by operator new, but via reflection 
        final TgOwnerEntity owner = ownerType.newInstance(); 
        owner.setKey("OWN1");
        
        try {
            owner.getEntityProp();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [getEntityProp] is restricted due to unfetched property [entityProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            final TgEntityForProxy value = factory.newByKey(TgEntityForProxy.class, "KEY1");
            owner.setEntityProp(value);
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setEntityProp] is restricted due to unfetched property [entityProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            final TgEntityForProxy prop1Proxy = factory.newByKey(TgEntityForProxy.class, "KEY1");
            owner.beginInitialising();
            owner.setEntityProp(prop1Proxy);
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setEntityProp] is restricted due to unfetched property [entityProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

    }

    
    @Test
    public void access_or_mutation_for_proxied_collectional_property_for_instance_created_via_factory_is_restricted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "children").entityType;
        
        // creation via factory
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
        
        try {
            owner.getChildren();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [getChildren] is restricted due to unfetched property [children] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.setChildren(new HashSet<>());
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setChildren] is restricted due to unfetched property [children] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.beginInitialising();
            owner.setChildren(new HashSet<>());
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setChildren] is restricted due to unfetched property [children] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

    }

    @Test
    public void access_or_mutation_for_proxied_collectional_property_for_instance_created_withou_factory_is_restricted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "children").entityType;
        
        // this is like creation by operator new, but via reflection 
        final TgOwnerEntity owner = ownerType.newInstance(); 
        owner.setKey("OWN1");

        
        try {
            owner.getChildren();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [getChildren] is restricted due to unfetched property [children] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.setChildren(new HashSet<>());
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setChildren] is restricted due to unfetched property [children] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.beginInitialising();
            owner.setChildren(new HashSet<>());
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setChildren] is restricted due to unfetched property [children] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

    }

    @Test
    public void access_or_mutation_for_proxied_property_of_Integer_type_for_instance_created_via_factory_is_restricted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "intProp").entityType;
        
        // creation via factory
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
        
        try {
            owner.getIntProp();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [getIntProp] is restricted due to unfetched property [intProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.setIntProp(42);
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setIntProp] is restricted due to unfetched property [intProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
        
        try {
            owner.beginInitialising();
            owner.setIntProp(42);
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setIntProp] is restricted due to unfetched property [intProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
    }

    @Test
    public void access_or_mutation_for_proxied_property_of_Integer_type_for_instance_created_without_factory_is_restricted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "intProp").entityType;
        
        // this is like creation by operator new, but via reflection 
        final TgOwnerEntity owner = ownerType.newInstance(); 
        owner.setKey("OWN1");
        
        try {
            owner.getIntProp();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [getIntProp] is restricted due to unfetched property [intProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.setIntProp(42);
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setIntProp] is restricted due to unfetched property [intProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
        
        try {
            owner.beginInitialising();
            owner.setIntProp(42);
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setIntProp] is restricted due to unfetched property [intProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
    }

    @Test
    public void access_or_mutation_for_proxied_property_of_boolean_type_for_instance_created_via_factory_is_restricted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "booleanProp").entityType;
        
        // creation via factory
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
        
        try {
            owner.isBooleanProp();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [isBooleanProp] is restricted due to unfetched property [booleanProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.setBooleanProp(true);
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setBooleanProp] is restricted due to unfetched property [booleanProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
        
        try {
            owner.beginInitialising();
            owner.setBooleanProp(true);
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setBooleanProp] is restricted due to unfetched property [booleanProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
    }

    @Test
    public void access_or_mutation_for_proxied_property_of_boolean_type_for_instance_created_without_factory_is_restricted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "booleanProp").entityType;
        
        // this is like creation by operator new, but via reflection 
        final TgOwnerEntity owner = ownerType.newInstance(); 
        owner.setKey("OWN1");
        
        try {
            owner.isBooleanProp();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [isBooleanProp] is restricted due to unfetched property [booleanProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.setBooleanProp(true);
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setBooleanProp] is restricted due to unfetched property [booleanProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
        
        try {
            owner.beginInitialising();
            owner.setBooleanProp(true);
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setBooleanProp] is restricted due to unfetched property [booleanProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
    }


    @Test
    public void access_or_mutation_for_proxied_propertes_in_the_same_instance_created_via_factory_is_restricted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "booleanProp", "intProp").entityType;
        
        // creation via factory
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
        
        try {
            owner.isBooleanProp();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [isBooleanProp] is restricted due to unfetched property [booleanProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.getIntProp();
            fail("Accessing proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [getIntProp] is restricted due to unfetched property [intProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }


        try {
            owner.setBooleanProp(true);
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setBooleanProp] is restricted due to unfetched property [booleanProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        try {
            owner.setIntProp(42);
            fail("Setting proxied property should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setIntProp] is restricted due to unfetched property [intProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

        
        try {
            owner.beginInitialising();
            owner.setBooleanProp(true);
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setBooleanProp] is restricted due to unfetched property [booleanProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }
        
        try {
            owner.beginInitialising();
            owner.setIntProp(42);
            owner.endInitialising();
            fail("Setting proxied property in the initialisation mode should result in exception.");
        } catch (final StrictProxyException ex) {
            assertEquals("Invocation of method [setIntProp] is restricted due to unfetched property [intProp] in type [ua.com.fielden.platform.entity.proxy.TgOwnerEntity].", ex.getMessage());
        }

    }
    
    @Test
    public void access_or_mutation_for_non_proxied_propertes_in_the_instance_with_proxied_properties_created_via_factory_is_permitted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "entityProp", "intProp").entityType;
        
        // creation via factory
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
        
        assertFalse(owner.isBooleanProp());
        owner.setBooleanProp(true);
        assertTrue(owner.isBooleanProp());
        assertTrue(owner.getProperty("booleanProp").isDirty());
    }

    @Test
    public void number_of_proxy_interceptor_equals_to_number_of_proxied_properties() throws Exception {
        final EntityProxyContainer<TgOwnerEntity> entityProxy = EntityProxyContainer.proxy(TgOwnerEntity.class, "entityProp", "intProp");
        assertEquals(2, entityProxy.propertyInterceptors.size());
        assertNotNull(entityProxy.propertyInterceptors.get("entityProp"));
        assertNotNull(entityProxy.propertyInterceptors.get("intProp"));
    }

    @Test
    public void method_proxiedPropertyNames_contains_names_of_proxied_properties() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "entityProp", "intProp").entityType;
        
        // creation via factory
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
        
        assertEquals(2, owner.proxiedPropertyNames().size());
        assertTrue(owner.proxiedPropertyNames().contains("entityProp"));
        assertTrue(owner.proxiedPropertyNames().contains("intProp"));
    }

    @Test
    public void method_proxiedPropertyNames_returns_empty_set_for_caes_of_no_proxied_properties() throws Exception {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class).entityType;
        
        // creation via factory
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
        
        assertEquals(0, owner.proxiedPropertyNames().size());
    }

    @Test
    public void mutation_of_proxiedPropertyNames_is_not_permitted() throws Exception {
        final Class<? extends TgOwnerEntity> ownerTypeWithProxiedProps = EntityProxyContainer.proxy(TgOwnerEntity.class, "entityProp", "intProp").entityType;
        final TgOwnerEntity ownerWithProxiedProps = factory.newByKey(ownerTypeWithProxiedProps, "OWN1");
    
        try {
            ownerWithProxiedProps.proxiedPropertyNames().add("value");
            fail("Mutation of proxied property names set should not be permitted.");
        } catch (Exception ex) {
            
        }
        
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class).entityType;
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
    
        try {
            owner.proxiedPropertyNames().add("value");
            fail("Mutation of proxied property names set should not be permitted.");
        } catch (Exception ex) {
        }
    }

    @Test
    public void meta_property_instances_are_identified_as_proxied_only_for_effectively_proxied_properties() {
        final Class<? extends TgOwnerEntity> ownerType = EntityProxyContainer.proxy(TgOwnerEntity.class, "entityProp", "intProp").entityType;
        final TgOwnerEntity owner = factory.newByKey(ownerType, "OWN1");
        
        final List<MetaProperty<?>> proxiedMetaProps = owner.getProperties().values().stream()
                .filter(mp -> mp.isProxy())
                .collect(Collectors.toList());
        
        assertEquals(2, proxiedMetaProps.size());
        assertTrue(owner.getProperty("entityProp").isProxy());
        assertTrue(owner.getProperty("intProp").isProxy());

    }

}
