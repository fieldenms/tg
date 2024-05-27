package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.meta.Assertions.EntityA;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.test.PlatformTestHibernateSetup;

import java.util.List;

import static com.google.inject.Guice.createInjector;
import static ua.com.fielden.platform.test_utils.CollectionTestUtils.assertEqualByContents;

public class EntityMetadataUtilsTest {

    private final IDomainMetadata domainMetadata = new DomainMetadataBuilder(
            PlatformTestHibernateSetup.getHibernateTypes(),
            createInjector(new HibernateUserTypesModule()),
            List.of(),
            DbVersion.MSSQL)
            .build();
    private final EntityMetadataUtils emUtils = domainMetadata.entityMetadataUtils();

    @Test
    public void unionMembers_returns_properties_that_are_union_members_of_a_given_union_entity() {
        EntityA.of(domainMetadata.forEntity(TgBogieLocation.class))
                .assertIs(EntityMetadata.Union.class)
                .peek(em -> assertEqualByContents(List.of("wagonSlot", "workshop"),
                                                  emUtils.unionMembers(em).stream().map(PropertyMetadata::name).toList()));
    }

}
