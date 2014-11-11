package ua.com.fielden.platform.entity.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javassist.util.proxy.ProxyFactory;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

public class ProxyTestCase {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void creation_of_entity_proxy_in_strict_mode_should_succeed() {
        final EntityProxyFactory<EntityForProxy> epf = new EntityProxyFactory<>(EntityForProxy.class);

        final OwnerEntity owner = factory.newByKey(OwnerEntity.class, "OWN1");

        final EntityForProxy prop1Proxy = epf.create(10L, owner, "prop1", null, ProxyMode.STRICT);

        owner.setInitialising(true);
        owner.setProp1(prop1Proxy);
        owner.setInitialising(false);

        assertTrue(ProxyFactory.isProxyClass(owner.getProp1().getClass()));
    }

    @Test
    public void accessing_id_for_entity_proxy_in_strict_mode_should_succeed() {
        final EntityProxyFactory<EntityForProxy> epf = new EntityProxyFactory<>(EntityForProxy.class);

        final OwnerEntity owner = factory.newByKey(OwnerEntity.class, "OWN1");

        final EntityForProxy prop1Proxy = epf.create(10L, owner, "prop1", null, ProxyMode.STRICT);

        owner.setInitialising(true);
        owner.setProp1(prop1Proxy);
        owner.setInitialising(false);

        assertEquals(10L, owner.getProp1().getId(), 0);
    }

    @Test(expected = StrictProxyException.class)
    public void accessing_non_id_properties_for_entity_proxy_in_strict_mode_should_fail() {
        final EntityProxyFactory<EntityForProxy> epf = new EntityProxyFactory<>(EntityForProxy.class);

        final OwnerEntity owner = factory.newByKey(OwnerEntity.class, "OWN1");

        final EntityForProxy prop1Proxy = epf.create(10L, owner, "prop1", null, ProxyMode.STRICT);

        owner.setInitialising(true);
        owner.setProp1(prop1Proxy);
        owner.setInitialising(false);

        // the following line should result in proxy strict mode exception
        owner.getProp1().getDesc();
    }

    @Test(expected = StrictProxyException.class)
    public void accessing_other_methods_for_entity_proxy_in_strict_mode_should_fail() {
        final EntityProxyFactory<EntityForProxy> epf = new EntityProxyFactory<>(EntityForProxy.class);

        final OwnerEntity owner = factory.newByKey(OwnerEntity.class, "OWN1");

        final EntityForProxy prop1Proxy = epf.create(10L, owner, "prop1", null, ProxyMode.STRICT);

        owner.setInitialising(true);
        owner.setProp1(prop1Proxy);
        owner.setInitialising(false);

        // the following line should result in proxy strict mode exception
        owner.getProp1().toString();
    }

    @Test
    public void accessing_lazy_proxy_should_result_in_successful_method_invocation_and_de_proxing() {
        final EntityProxyFactory<EntityForProxy> epf = new EntityProxyFactory<>(EntityForProxy.class);

        final OwnerEntity owner = factory.newByKey(OwnerEntity.class, "OWN1");
        
        // need to mock a companion object
        final IEntityForProxy coEntityForProxy = mock(IEntityForProxy.class);
        when(coEntityForProxy.lazyLoad(10L)).thenReturn(factory.newByKey(EntityForProxy.class, "REAL!"));

        final EntityForProxy prop1Proxy = epf.create(10L, owner, "prop1", coEntityForProxy, ProxyMode.LAZY);

        owner.setInitialising(true);
        owner.setProp1(prop1Proxy);
        owner.setInitialising(false);
        
        assertEquals("REAL!", owner.getProp1().getKey());
        assertFalse(ProxyFactory.isProxyClass(owner.getProp1().getClass()));
    }

}
