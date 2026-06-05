package ua.com.fielden.platform.eql.meta;

import graphql.com.google.common.collect.ImmutableTable;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.dbschema.PropertyInliner;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static graphql.com.google.common.collect.ImmutableTable.toImmutableTable;
import static java.lang.String.join;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.types.RichText.*;

public class DomainMetadataModelGeneratorTest extends AbstractDaoTestCase {

    private final DomainMetadataModelGenerator generator = new DomainMetadataModelGenerator(
            getInstance(IDomainMetadata.class), getInstance(PropertyInliner.class));
    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);

    @Test
    public void DomainPropertyData_is_generated_for_RichText_property_and_its_subproperties() {
        final var asserts = asserts(Set.of(EntityWithRichText.class));
        final var entity = asserts.assertTypeExists(EntityWithRichText.class);
        final var propText = asserts.assertPropertyExists(EntityWithRichText.class, "text");
        asserts.assertProperty(EntityWithRichText.class, "text",
                                   prop -> {
                                       assertEquals(entity, prop.holderAsDomainType());
                                       assertNull(prop.dbColumn());
                                   });
        asserts.assertProperty(EntityWithRichText.class, join(".", "text", CORE_TEXT),
                                   prop -> {
                                       assertNull(prop.holderAsDomainType());
                                       assertEquals(prop.holderAsDomainProperty(), propText);
                                   });
        asserts.assertProperty(EntityWithRichText.class, join(".", "text", SEARCH_TEXT),
                               prop -> {
                                   assertNull(prop.holderAsDomainType());
                                   assertEquals(prop.holderAsDomainProperty(), propText);
                               });
        asserts.assertProperty(EntityWithRichText.class, join(".", "text", FORMATTED_TEXT),
                                   prop -> {
                                       assertNull(prop.holderAsDomainType());
                                       assertEquals(prop.holderAsDomainProperty(), propText);
                                   });
    }

    @Test
    public void union_typed_properties_and_their_sub_properties_are_correctly_represented() {
        final var asserts = asserts(Set.of(TgBogie.class, TgWagonSlot.class, TgWorkshop.class));
        final var domainTypeDataTgBogieLocation = asserts.assertTypeExists(TgBogieLocation.class);
        assertFalse(domainTypeDataTgBogieLocation.isEntity());
        assertNull(domainTypeDataTgBogieLocation.dbTable());
        assertEquals(TgBogieLocation.class.getCanonicalName(), domainTypeDataTgBogieLocation.key());

        final var domainPropertyDataTgBogie_location = asserts.assertPropertyExists(TgBogie.class, "location");

        final var unionMemberProperties = domainMetadata.propertyMetadataUtils()
                .subProperties(domainMetadata.forProperty(TgBogie.class, "location"))
                .stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .toList();

        assertThat(unionMemberProperties).isNotEmpty();
        assertEquals(unionMemberProperties.size(), domainTypeDataTgBogieLocation.propsCount());

        for (final var unionMemberProp : unionMemberProperties) {
            asserts.assertProperty(TgBogie.class, "location.%s".formatted(unionMemberProp.name()), prop -> {
                assertEquals(unionMemberProp.name(), prop.name());
                assertNull(prop.holderAsDomainType());
                assertEquals(domainPropertyDataTgBogie_location, prop.holderAsDomainProperty());
                assertEquals(unionMemberProp.asPersistent().orElseThrow().data().column().name, prop.dbColumn());
            });
        }
    }

    @Override
    protected void populateDomain() {}

    // ------------------------------------------------------------
    // Test utilities

    private Asserts asserts(final Set<Class<? extends AbstractEntity<?>>> entityTypes) {
        return new Asserts(entityTypes);
    }

    private final class Asserts {

        private final ImmutableTable<Class<?>, String, DomainPropertyData> domainProperties;
        private final Map<Class<?>, DomainTypeData> domainTypes;

        private Asserts(final Set<Class<? extends AbstractEntity<?>>> entityTypes) {
            this.domainTypes = generator.generateDomainTypesData(entityTypes);
            this.domainProperties = generator.generateDomainPropsData(domainTypes).stream()
                    .collect(toImmutableTable(
                            prop -> prop.holderAsDomainType() != null
                                    ? prop.holderAsDomainType().type()
                                    : prop.holderAsDomainProperty().holderAsDomainType().type(),
                            prop -> prop.holderAsDomainProperty() == null
                                    ? prop.name()
                                    : join(".", prop.holderAsDomainProperty().name(), prop.name()),
                            identity()));
        }

        public DomainTypeData assertTypeExists(final Class<?> type) {
            final var domainType = domainTypes.get(type);
            assertNotNull("Missing domain type data for [%s]".formatted(type.getTypeName()), domainType);
            return domainType;
        }

        public Asserts assertProperty(final Class<?> type, final String name, final Consumer<? super DomainPropertyData> assertor) {
            final var prop = domainProperties.get(type, name);
            assertNotNull("Missing property data for [%s] in [%s]".formatted(name, type), prop);
            assertor.accept(prop);
            return this;
        }

        public DomainPropertyData assertPropertyExists(final Class<?> type, final String name) {
            final var prop = domainProperties.get(type, name);
            assertNotNull("Missing property [%s] in [%s]".formatted(name, type), prop);
            return prop;
        }
    }

}
