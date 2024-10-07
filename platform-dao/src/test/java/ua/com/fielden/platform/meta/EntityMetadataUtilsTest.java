package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.Assertions.EntityA;
import ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgCategory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;
import static ua.com.fielden.platform.test_utils.CollectionTestUtils.assertEqualByContents;

public class EntityMetadataUtilsTest {

    private final IDomainMetadata domainMetadata;
    private final EntityMetadataUtils emUtils;

    public EntityMetadataUtilsTest() {
        final var dbVersionProvider = constantDbVersion(DbVersion.MSSQL);
        domainMetadata = new DomainMetadataBuilder(
                new PlatformHibernateTypeMappings.Provider(dbVersionProvider).get(), List.of(), dbVersionProvider)
                .build();
        emUtils =  domainMetadata.entityMetadataUtils();
    }

    @Test
    public void unionMembers_returns_properties_that_are_union_members_of_a_given_union_entity() {
        EntityA.of(domainMetadata.forEntity(TgBogieLocation.class))
                .assertIs(EntityMetadata.Union.class)
                .peek(em -> assertEqualByContents(List.of("wagonSlot", "workshop"),
                                                  emUtils.unionMembers(em).stream().map(PropertyMetadata::name).toList()));
    }

    @Test
    public void compositeKeyMembers_returns_sequence_of_composite_key_member_properties_ordered_by_key_member_order() {
        EntityA.of(domainMetadata.forEntity(TgAuthor.class))
                .peek(em -> assertEquals(List.of("name", "surname", "patronymic"),
                                         emUtils.compositeKeyMembers(em).stream().map(PropertyMetadata::name).toList()));
    }

    @Test
    public void compositeKeyMembers_returns_empty_sequence_for_entity_with_simple_key() {
        EntityA.of(domainMetadata.forEntity(TgCategory.class))
                .peek(em -> assertEquals(List.of(),
                                         emUtils.compositeKeyMembers(em).stream().map(PropertyMetadata::name).toList()));
    }

    @Test
    public void keyMembers_returns_sequence_of_composite_key_member_properties_ordered_by_key_member_order() {
        EntityA.of(domainMetadata.forEntity(TgAuthor.class))
                .peek(em -> assertEquals(List.of("name", "surname", "patronymic"),
                                         emUtils.keyMembers(em).stream().map(PropertyMetadata::name).toList()));
    }

    @Test
    public void keyMembers_returns_sequence_of_property_key_for_entity_with_simple_key() {
        EntityA.of(domainMetadata.forEntity(TgCategory.class))
                .peek(em -> assertEquals(List.of("key"),
                                         emUtils.keyMembers(em).stream().map(PropertyMetadata::name).toList()));
    }

}
