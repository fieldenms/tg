package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.Assertions.ComponentA;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Primitive;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Currency;

import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;
import static ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings.PLATFORM_HIBERNATE_TYPE_MAPPINGS;
import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;

public class ComponentTypeMetadataTest {

    private final DomainMetadataGenerator generator = new DomainMetadataGenerator(
            PLATFORM_HIBERNATE_TYPE_MAPPINGS, constantDbVersion(DbVersion.MSSQL));

    @Test
    public void metadata_is_generated_for_type_Money() {
        ComponentA.of(assertPresent("Expected metadata to have been generated.", generator.forComponent(Money.class)))
                .assertProperty("amount", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .type().assertIs(Primitive.class).assertJavaType(BigDecimal.class))
                .assertProperty("exTaxAmount", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .type().assertIs(Primitive.class).assertJavaType(BigDecimal.class))
                .assertProperty("taxAmount", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .type().assertIs(Primitive.class).assertJavaType(BigDecimal.class))
                .assertProperty("taxPercent", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .type().assertIs(Primitive.class).assertJavaType(Integer.class))
                .assertProperty("currency", p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .type().assertIs(Primitive.class).assertJavaType(Currency.class))
        ;
    }

}
