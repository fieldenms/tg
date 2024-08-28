package ua.com.fielden.platform.meta;

import com.google.inject.Guice;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.meta.Assertions.ComponentA;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Primitive;
import ua.com.fielden.platform.test.PlatformTestHibernateSetup;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Currency;

import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;

public class ComponentTypeMetadataTest {

    private final DomainMetadataGenerator generator = new DomainMetadataGenerator(
            Guice.createInjector(new HibernateUserTypesModule()),
            PlatformTestHibernateSetup.getHibernateTypes(),
            DbVersion.MSSQL
    );

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
