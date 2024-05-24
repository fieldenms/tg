package ua.com.fielden.platform.meta;

import com.google.inject.Guice;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.meta.Assertions.EntityA;
import ua.com.fielden.platform.meta.Assertions.PropertyA;
import ua.com.fielden.platform.meta.test_entities.Entity_PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.test.PlatformTestHibernateSetup;

public class PropertyHibernateTypeTest {

    private final TestDomainMetadataGenerator generator = TestDomainMetadataGenerator.wrap(
            new DomainMetadataGenerator(
                    Guice.createInjector(new HibernateUserTypesModule()),
                    PlatformTestHibernateSetup.getHibernateTypes(),
                    DbVersion.MSSQL));

    @Test
    public void hibernate_type_is_attached_to_persistent_properties() {
        EntityA.of(generator.forEntity(TgAuthor.class))
                .assertProperty("id", PropertyA::assertHasHibType)
                .assertProperty("key", PropertyA::assertHasHibType)
                .assertProperty("name", PropertyA::assertHasHibType)
                .assertProperty("surname", PropertyA::assertHasHibType)
                .assertProperty("webpage", PropertyA::assertHasHibType);
    }

    @Test
    public void hibernate_type_is_attached_to_calculated_properties() {
        EntityA.of(generator.forEntity(TgVehicle.class))
                .assertProperty("lastFuelUsage", PropertyA::assertHasHibType)
                .assertProperty("calc0", PropertyA::assertHasHibType);
    }

    @Test
    public void hibernate_type_is_not_attached_to_transient_properties_of_persistent_entities() {
        EntityA.of(generator.forEntity(TgWorkOrder.class))
                .assertProperty("orgUnit1", PropertyA::assertNoHibType)
                .assertProperty("stringSingle", PropertyA::assertNoHibType)
                .assertProperty("moneySingle", PropertyA::assertNoHibType);
    }

    @Test
    public void hibernate_type_is_attached_to_properties_of_synthetic_entities() {
        EntityA.of(generator.forEntity(TgAverageFuelUsage.class))
                .assertProperty("id", PropertyA::assertHasHibType)
                .assertProperty("key", PropertyA::assertHasHibType)
                .assertProperty("qty", PropertyA::assertHasHibType)
                .assertProperty("cost", PropertyA::assertHasHibType);
    }

    @Test
    public void hibernate_type_is_attached_to_properties_with_PropertyDescriptor_type() {
        EntityA.of(generator.forEntity(Entity_PropertyDescriptor.class))
                .assertProperty("pd", PropertyA::assertHasHibType);
    }

}
