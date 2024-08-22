package ua.com.fielden.platform.eql.dbschema;

import org.junit.Test;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.test_utils.TestUtils.assertEmpty;
import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;

public class PropertyInlinerTest extends AbstractDaoTestCase {

    private final IDomainMetadata metadata = getInstance(IDomainMetadata.class);
    private final PropertyInliner inliner = getInstance(PropertyInliner.class);

    @Test
    public void Money_property_is_replaced_by_properties_of_Money() {
        final var propMoney = metadata.forEntity(EntityWithMoney.class).property("money").asPersistent().orElseThrow();
        assertEquals(Money.class, propMoney.type().javaType());

        assertInlined(propMoney,
                      props -> assertEquals(Set.of("amount"),
                                            props.stream().map(PropertyMetadata::name).collect(toSet())));
    }

    @Test
    public void property_typed_with_union_entity_is_replaced_by_properties_of_the_union_entity() {
        assertTrue(metadata.forEntity(TgBogieLocation.class).isUnion());
        final var propLocation = metadata.forEntity(TgBogie.class).property("location").asPersistent().orElseThrow();
        assertEquals(TgBogieLocation.class, propLocation.type().javaType());

        assertInlined(propLocation,
                      props -> assertEquals(Set.of("wagonSlot", "workshop"),
                                            props.stream().map(PropertyMetadata::name).collect(toSet())));
    }

    @Test
    public void primitive_properties_are_not_inlined() {
        final var propSurname = metadata.forEntity(TgAuthor.class).property("surname").asPersistent().orElseThrow();
        assertTrue(propSurname.type().isPrimitive());
        assertNotInlined(propSurname);
    }

    @Test
    public void properties_typed_with_entities_other_than_union_are_not_inlined() {
        final var propReplacedBy = metadata.forEntity(TgVehicle.class).property("replacedBy").asPersistent().orElseThrow();
        assertTrue(propReplacedBy.type().isEntity());
        assertFalse(metadata.propertyMetadataUtils().isPropEntityType(propReplacedBy, EntityMetadata::isUnion));
        assertNotInlined(propReplacedBy);
    }

    private void assertInlined(final PropertyMetadata.Persistent property,
                               final Consumer<? super List<PropertyMetadata.Persistent>> assertor) {
        assertor.accept(assertPresent(inliner.inline(property)));
    }

    private void assertNotInlined(final PropertyMetadata.Persistent property) {
        assertEmpty(inliner.inline(property));
    }

    @Override
    protected void populateDomain() {}

}
