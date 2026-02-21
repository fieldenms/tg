package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.meta.Assertions.EntityA;
import ua.com.fielden.platform.meta.Assertions.SubPropertiesA;
import ua.com.fielden.platform.meta.PropertyMetadataUtils.SubPropertyNaming;
import ua.com.fielden.platform.meta.test_entities.Entity_VariousMoney;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkshop;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;

public class PropertyMetadataUtilsTest {

    private static final IDbVersionProvider dbVersionProvider = constantDbVersion(DbVersion.MSSQL);
    private static final IDomainMetadata domainMetadata = new DomainMetadataBuilder(new PlatformHibernateTypeMappings.Provider(dbVersionProvider).get(), List.of(), dbVersionProvider).build();
    private static final PropertyMetadataUtils pmUtils = domainMetadata.propertyMetadataUtils();

    @Test
    public void subProperties_for_composite_type_Money_depend_on_its_representation() {
        subPropertiesOf(Entity_VariousMoney.class, "moneyWithTax")
                .assertSubPropertiesAre(List.of("amount", "taxAmount", "currency"));

        subPropertiesOf(Entity_VariousMoney.class, "simpleMoney")
                .assertSubPropertiesAre(List.of("amount"));
    }

    @Test
    public void subProperties_for_persistent_property_with_composite_type_are_persistent() {
        subPropertiesOf(Entity_VariousMoney.class, "moneyWithTax")
                .forEach(p -> p.assertIs(PropertyMetadata.Persistent.class));
    }

    @Test
    public void subProperties_for_calculated_property_with_composite_type_are_calculated() {
        subPropertiesOf(Entity_VariousMoney.class, "calcSimpleMoney")
                .forEach(p -> p.assertIs(PropertyMetadata.Calculated.class));
        subPropertiesOf(Entity_VariousMoney.class, "calcMoneyWithTax")
                .forEach(p -> p.assertIs(PropertyMetadata.Calculated.class));
    }

    @Test
    public void types_of_subProperties_of_a_property_with_composite_type_are_determined_correctly() {
        subPropertiesOf(Entity_VariousMoney.class, "moneyWithTax")
                .assertSubProperty("amount", p -> p.type()
                        .assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(BigDecimal.class))
                .assertSubProperty("taxAmount", p -> p.type()
                        .assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(BigDecimal.class))
                .assertSubProperty("currency", p -> p.type()
                        .assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(Currency.class));
    }

    @Test
    public void subProperties_of_union_entity_typed_property_are_union_members_and_implicitly_calculated_properties() {
        subPropertiesOf(TgBogie.class, "location")
                .assertSubPropertiesAre(List.of("wagonSlot", "workshop", "key", "id", "desc", "fuelType"));
    }

    @Test
    public void subProperties_of_union_entity_typed_property_include_union_members_as_persistent() {
        subPropertiesOf(TgBogie.class, "location")
                .assertSubProperty("wagonSlot", p -> p.assertIs(PropertyMetadata.Persistent.class))
                .assertSubProperty("workshop", p -> p.assertIs(PropertyMetadata.Persistent.class));
    }

    @Test
    public void subProperties_of_union_entity_typed_property_include_implicitly_caclulated_properties_as_calculated() {
        subPropertiesOf(TgBogie.class, "location")
                .assertSubProperty("id", p -> p.assertIs(PropertyMetadata.Calculated.class))
                .assertSubProperty("key", p -> p.assertIs(PropertyMetadata.Calculated.class))
                .assertSubProperty("desc", p -> p.assertIs(PropertyMetadata.Calculated.class))
                .assertSubProperty("fuelType", p -> p.assertIs(PropertyMetadata.Calculated.class));
    }

    @Test
    public void types_of_subProperties_of_union_entity_typed_property_are_preserved() {
        subPropertiesOf(TgBogie.class, "location")
                .assertSubProperty("wagonSlot", p -> p.type()
                        .assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgWagonSlot.class))
                .assertSubProperty("workshop", p -> p.type()
                        .assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgWorkshop.class))
                .assertSubProperty("fuelType", p -> p.type()
                        .assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgFuelType.class))
                .assertSubProperty("id", p -> p.type()
                        .assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(Long.class))
                .assertSubProperty("key", p -> p.type()
                        .assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(String.class))
                .assertSubProperty("desc", p -> p.type()
                        .assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(String.class));
    }

    @Test
    public void subProperties_with_naming_PATH_returns_properties_that_have_paths_in_their_names() {
        // Union entity type
        subPropertiesOf(TgBogie.class, "location", SubPropertyNaming.PATH)
                .assertSubPropertiesAre(Set.of("location.wagonSlot", "location.workshop", "location.fuelType",
                                               "location.id", "location.key", "location.desc"));

        // Component-typed property
        subPropertiesOf(EntityWithRichText.class, "text", SubPropertyNaming.PATH)
                .assertSubPropertiesAre(Set.of("text.formattedText", "text.searchText", "text.coreText"));
    }

    ///////////////////////////////////////////
    /////////////// Utils /////////////////////
    ///////////////////////////////////////////

    private SubPropertiesA subPropertiesOf(final Class<? extends AbstractEntity<?>> entityType, final String propName) {
        final var entityMetadata = domainMetadata.forEntity(entityType);
        return EntityA.of(entityMetadata).getProperty(propName).subProperties(pmUtils);
    }

    private SubPropertiesA subPropertiesOf(
            final Class<? extends AbstractEntity<?>> entityType,
            final String propName,
            final SubPropertyNaming naming)
    {
        final var entityMetadata = domainMetadata.forEntity(entityType);
        return EntityA.of(entityMetadata).getProperty(propName).subProperties(pmUtils, naming);
    }

}
