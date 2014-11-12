package ua.com.fielden.platform.eql.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.dao.PropertyCategory.COLLECTIONAL;
import static ua.com.fielden.platform.dao.PropertyCategory.COMPONENT_DETAILS;
import static ua.com.fielden.platform.dao.PropertyCategory.ENTITY;
import static ua.com.fielden.platform.dao.PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.dao.PropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.dao.PropertyCategory.PRIMITIVE;
import static ua.com.fielden.platform.dao.PropertyCategory.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyColumn;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgMakeCount;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

public class DomainMetadataPPIsTest extends BaseEntQueryTCase {
    @Test
    public void test_vehicle_model_entity_props_metadata() {
        final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
        expected.add(ppi("id", LONG, false, H_LONG, "_ID", PRIMITIVE));
        expected.add(ppi("version", LONG, false, H_LONG, "_VERSION", PRIMITIVE));
        expected.add(ppi("key", STRING, false, H_STRING, "KEY_", PRIMITIVE));
        expected.add(ppi("desc", STRING, true, H_STRING, "DESC_", PRIMITIVE));
        expected.add(ppi("make", MAKE, false, H_LONG, "MAKE_", ENTITY));

        final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
        actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(MODEL));
        assertEquals("Incorrect result type", expected, actual);
    }
    
    @Test
    public void test_author_entity_props_metadata() {
        final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
        expected.add(ppi("id", LONG, false, H_LONG, "_ID", PRIMITIVE));
        expected.add(ppi("version", LONG, false, H_LONG, "_VERSION", PRIMITIVE));
        expected.add(ppi("name", PERSON_NAME, false, H_LONG, "NAME_", ENTITY_MEMBER_OF_COMPOSITE_KEY));
        expected.add(ppi("surname", STRING, false, H_STRING, "SURNAME_", PRIMITIVE_MEMBER_OF_COMPOSITE_KEY));
        expected.add(ppi("patronymic", STRING, true, H_STRING, "PATRONYMIC_", PRIMITIVE_MEMBER_OF_COMPOSITE_KEY));
        final ExpressionModel exp = expr().caseWhen().model(select(TgAuthorship.class).where().prop("author").eq().extProp("id").yield().countAll().modelAsPrimitive()). //
        gt().val(1).then().val(true).otherwise().val(false).endAsBool().model();
        expected.add(ppi("hasMultiplePublications", BOOLEAN, exp, H_BOOLEAN, EXPRESSION, false));

        final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
        actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(AUTHOR));

        assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test_vehicle_entity_props_metadata() {
        final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
        expected.add(ppi("id", LONG, false, H_LONG, "_ID", PRIMITIVE));
        expected.add(ppi("version", LONG, false, H_LONG, "_VERSION", PRIMITIVE));
        expected.add(ppi("key", STRING, false, H_STRING, "KEY_", PRIMITIVE));
        expected.add(ppi("desc", STRING, true, H_STRING, "DESC_", PRIMITIVE));
        expected.add(ppi("model", MODEL, false, H_LONG, "MODEL_", ENTITY));
        expected.add(ppi("price.amount", BIG_DECIMAL, true, H_BIG_DECIMAL, "PRICE_", COMPONENT_DETAILS));
        expected.add(ppi("purchasePrice.amount", BIG_DECIMAL, true, H_BIG_DECIMAL, "PURCHASEPRICE_", COMPONENT_DETAILS));
        expected.add(ppi("fuelUsages", FUEL_USAGE, true, null, Collections.<PropertyColumn> emptyList(), COLLECTIONAL));

        final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
        actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(VEHICLE));
        assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test_fuel_usage_entity_props_metadata() {
        final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
        expected.add(ppi("id", LONG, false, H_LONG, "_ID", PRIMITIVE));
        expected.add(ppi("version", LONG, false, H_LONG, "_VERSION", PRIMITIVE));
        expected.add(ppi("vehicle", VEHICLE, false, H_LONG, "VEHICLE_", ENTITY_MEMBER_OF_COMPOSITE_KEY));
        expected.add(ppi("date", DATE, false, DOMAIN_METADATA_ANALYSER.getHibTypesDefaults().get(Date.class), "DATE_", PRIMITIVE_MEMBER_OF_COMPOSITE_KEY));

        final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
        actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(FUEL_USAGE));

        assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test_user_entity_props_metadata() {
        final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
        expected.add(ppi("id", LONG, false, H_LONG, "_ID", PRIMITIVE));
        expected.add(ppi("version", LONG, false, H_LONG, "_VERSION", PRIMITIVE));
        expected.add(ppi("key", STRING, false, H_STRING, "USER_NAME", PRIMITIVE));
        expected.add(ppi("roles", UserAndRoleAssociation.class, true, null, Collections.<PropertyColumn> emptyList(), COLLECTIONAL));

        final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
        actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(User.class));

        assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test_entity_props_nullability() {
        assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "station"));
        assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "station.key"));

        assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model"));
        assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.id"));
        assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.key"));
        assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.desc"));
        assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make"));
        assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.id"));
        assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.key"));
        assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.desc"));
        assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "price.amount"));
        try {
            assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "price.currency"));
            fail("Should have failed!");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_getting_of_leaf_props_from_first_level_props() {
        final Set<String> firstLevelProps = new HashSet<String>();
        firstLevelProps.add("leased");
        firstLevelProps.add("lastMeterReading");
        firstLevelProps.add("lastFuelUsage");

        final Set<String> actual = DOMAIN_METADATA_ANALYSER.getLeafPropsFromFirstLevelProps(null, VEHICLE, firstLevelProps);
        final Set<String> expected = new HashSet<String>();
        expected.add("leased");
        expected.add("lastMeterReading");
        expected.add("lastFuelUsage.vehicle");
        expected.add("lastFuelUsage.date");
        assertEquals(expected, actual);
    }
    
    @Test
    @Ignore
    public void test_synthetic_entity_props_metadata() {
        // FIXME
        final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
        expected.add(ppi("id", LONG, false, H_LONG, "_ID", PRIMITIVE));
        expected.add(ppi("version", LONG, false, H_LONG, "_VERSION", PRIMITIVE));
        expected.add(ppi("key", STRING, false, H_STRING, "USER_NAME", PRIMITIVE));
        expected.add(ppi("roles", UserAndRoleAssociation.class, false, null, Collections.<PropertyColumn> emptyList(), COLLECTIONAL));

        final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
        actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(TgMakeCount.class));
        //final PropertyMetadata ppi = DOMAIN_METADATA_ANALYSER.getPropPersistenceInfoExplicitly(TgMakeCount.class, "key");
        assertTrue(actual.containsAll(expected));
    }

    @Test
    @Ignore
    public void test_bogie_entity_props_metadata() {
        // FIXME
        final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
        actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(TgBogie.class));
    }
}