package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import java.math.BigDecimal;
import java.util.List;

import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;
import static ua.com.fielden.platform.meta.TestDomainMetadata.wrap;
import static ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings.PLATFORM_HIBERNATE_TYPE_MAPPINGS;

public class DomainMetadataTest {

    private final TestDomainMetadata domainMetadata = wrap(
            new DomainMetadataBuilder(PLATFORM_HIBERNATE_TYPE_MAPPINGS, List.of(), constantDbVersion(DbVersion.MSSQL))
                    .build());

    @Test
    public void entity_property_metadata_can_be_retrieved_using_dot_notation() {
        domainMetadata.forProperty(TgAuthor.class, "name.key")
                .assertIs(PropertyMetadata.Persistent.class)
                .type().assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(String.class);
        domainMetadata.forProperty(TgSystem.class, "category.parent.parent.parent")
                .assertIs(PropertyMetadata.Persistent.class)
                .type().assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgCategory.class);
    }

    @Test
    public void composite_type_property_metadata_can_be_retrieved_using_dot_notation() {
        domainMetadata.forProperty(TgVehicle.class, "price.amount")
                .assertIs(PropertyMetadata.Persistent.class)
                .type().assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(BigDecimal.class);
        domainMetadata.forProperty(TgVehicle.class, "replacedBy.price.amount")
                .assertIs(PropertyMetadata.Persistent.class)
                .type().assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(BigDecimal.class);
    }

}