package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.Assertions.ComponentA;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Primitive;
import ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;

import java.math.BigDecimal;
import java.util.Currency;

import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;
import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;

public class ComponentTypeMetadataTest {

    private final DomainMetadataGenerator generator;

    public ComponentTypeMetadataTest() {
        final var dbVersionProvider = constantDbVersion(DbVersion.MSSQL);
        generator = new DomainMetadataGenerator(new PlatformHibernateTypeMappings.Provider(dbVersionProvider).get(),
                                                dbVersionProvider);
    }

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

    @Test
    public void metadata_for_RichText() {
        ComponentA.of(generator.forComponent(RichText.class))
                .assertProperty(RichText._coreText, p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .type().assertIs(Primitive.class).assertJavaType(String.class))
                .assertProperty(RichText._formattedText, p -> p
                        .assertIs(PropertyMetadata.Persistent.class)
                        .type().assertIs(Primitive.class).assertJavaType(String.class));
    }

}
