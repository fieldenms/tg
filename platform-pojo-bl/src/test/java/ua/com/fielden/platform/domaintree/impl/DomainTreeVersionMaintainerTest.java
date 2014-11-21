package ua.com.fielden.platform.domaintree.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer0;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManagerAndEnhancer0;
import ua.com.fielden.platform.domaintree.master.impl.MasterDomainTreeManager0;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.domaintree.testing.EnhancingMasterEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.kryo.TgKryo;
import ua.com.fielden.platform.serialisation.kryo.TgKryo0;

/**
 * A test case for {@link DomainTreeVersionMaintainer}.
 * 
 * @author TG Team
 * 
 */
public class DomainTreeVersionMaintainerTest extends AbstractDomainTreeTest {
    private static ISerialiser0 serialiser0;
    private static ISerialiser serialiser;

    //    protected GlobalDomainTreeManager createManagerForBaseUser() {
    //	return createGlobalDomainTreeManager("USER1");
    //    }
    //
    //    private GlobalDomainTreeManager createGlobalDomainTreeManager(final String userName) {
    //	return new GlobalDomainTreeManager(serialiser, serialiser0, entityFactory, createUserProvider(userName), getInstance(IMainMenuItemController.class), getInstance(IEntityCentreConfigController.class), getInstance(IEntityCentreAnalysisConfig.class), getInstance(IEntityMasterConfigController.class), getInstance(IEntityLocatorConfigController.class)) {
    //	    @Override
    //	    protected void validateMenuItemType(final Class<?> menuItemType) { // no menu item validation due to non-existence of MiWithConfigurationSupport at platform-dao (it exists only on platform-ui level)
    //	    }
    //	};
    //    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
        initialiseDomainTreeTest(DomainTreeVersionMaintainerTest.class);
        serialiser0 = createSerialiser0(factory());
        serialiser = createSerialiser(factory());
    }

    protected static Object createDtm_for_DomainTreeVersionMaintainerTest() {
        return null;
    }

    protected static Object createIrrelevantDtm_for_DomainTreeVersionMaintainerTest() {
        return null;
    }

    protected static Set<Class<?>> createRootTypes_for_DomainTreeVersionMaintainerTest() {
        final Set<Class<?>> rootTypes = new HashSet<Class<?>>();
        rootTypes.add(EnhancingMasterEntity.class);
        return rootTypes;
    }

    protected static void manageTestingDTM_for_DomainTreeVersionMaintainerTest(final Object obj) {
    }

    protected static void performAfterDeserialisationProcess_for_DomainTreeVersionMaintainerTest(final Object obj) {
    }

    protected static void assertInnerCrossReferences_for_DomainTreeVersionMaintainerTest(final Object obj) {
    }

    private static ISerialiser0 createSerialiser0(final EntityFactory factory) {
        return new TgKryo0(factory, new ClassProviderForTestingPurposes());
    }

    private static ISerialiser createSerialiser(final EntityFactory factory) {
        return new TgKryo(factory, new ClassProviderForTestingPurposes());
    }

    public DomainTreeVersionMaintainerTest() {
    }

    protected static Set<Class<?>> createRootTypes() {
        final Set<Class<?>> rootTypes = new HashSet<Class<?>>();
        rootTypes.add(EnhancingMasterEntity.class);
        return rootTypes;
    }

    @Test
    public void testMaintainCentreVersion() throws Exception {
        final CentreDomainTreeManagerAndEnhancer0 centre0 = new CentreDomainTreeManagerAndEnhancer0(serialiser(), createRootTypes());
        final byte[] array0 = serialiser0.serialise(centre0);
        DomainTreeVersionMaintainer.retrieveCentre("ecc key", array0, serialiser, serialiser0);
    }

    @Test
    public void testMaintainCentreVersion_for_centre_with_locators() throws Exception {
        // final GlobalDomainTreeManager mgr = createManagerForBaseUser();

        final CentreDomainTreeManagerAndEnhancer0 centre0 = new CentreDomainTreeManagerAndEnhancer0(serialiser(), createRootTypes());

        //	GlobalDomainTreeManager.initLocatorManagerCrossReferences((LocatorManager0) centre0.getFirstTick(), mgr.getGlobalRepresentation());
        //
        //	centre0.getFirstTick().check(EnhancingMasterEntity.class, "slaveEntityProp", true);
        //	centre0.getFirstTick().refreshLocatorManager(EnhancingMasterEntity.class, "slaveEntityProp");
        //	centre0.getFirstTick().acceptLocatorManager(EnhancingMasterEntity.class, "slaveEntityProp");

        final byte[] array0 = serialiser0.serialise(centre0);
        DomainTreeVersionMaintainer.retrieveCentre("ecc key", array0, serialiser, serialiser0);
    }

    @Test
    public void testMaintainLocatorVersion() throws Exception {
        final LocatorDomainTreeManagerAndEnhancer0 locator0 = new LocatorDomainTreeManagerAndEnhancer0(serialiser(), createRootTypes());
        final byte[] array0 = serialiser0.serialise(locator0);
        DomainTreeVersionMaintainer.retrieveLocator("elc key", array0, serialiser, serialiser0);
    }

    @Test
    public void testMaintainMasterVersion() throws Exception {
        final MasterDomainTreeManager0 locator0 = new MasterDomainTreeManager0(serialiser(), createRootTypes());
        final byte[] array0 = serialiser0.serialise(locator0);
        DomainTreeVersionMaintainer.retrieveMaster("emc key", array0, serialiser, serialiser0);
    }
}
