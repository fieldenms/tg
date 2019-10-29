package ua.com.fielden.platform.entity.proxy;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 * Tests core mock-not-found functionality.
 *
 * @author TG Team
 */
public class MockNotFoundEntityTestCase {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void instances_of_mocked_types_are_recognise_as_mock_not_found_values() throws Exception {
        final Class<? extends TgOwnerEntity> mockType = MockNotFoundEntityMaker.mock(TgOwnerEntity.class);
        assertTrue(PropertyTypeDeterminator.isMockNotFoundType(mockType));
        assertTrue(MockNotFoundEntityMaker.isMockNotFoundValue(factory.newPlainEntity(mockType, null)));
    }

    @Test
    public void mocked_types_are_not_remocked() throws Exception {
        final Class<? extends TgOwnerEntity> mockType1 = MockNotFoundEntityMaker.mock(TgOwnerEntity.class);
        final Class<? extends TgOwnerEntity> mockType2 = MockNotFoundEntityMaker.mock(mockType1);
        assertTrue(mockType1 == mockType2);
    }

    @Test
    public void mocked_types_are_cached() throws Exception {
        final Class<? extends TgOwnerEntity> mockType1 = MockNotFoundEntityMaker.mock(TgOwnerEntity.class);
        final Class<? extends TgOwnerEntity> mockType2 = MockNotFoundEntityMaker.mock(TgOwnerEntity.class);
        assertTrue(mockType1 == mockType2);
    }

}
