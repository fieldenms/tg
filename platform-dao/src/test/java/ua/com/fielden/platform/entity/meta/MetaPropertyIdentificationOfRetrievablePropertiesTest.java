package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.CREATED_BY;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.CREATED_DATE;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.CREATED_TRANSACTION_GUID;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_BY;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_DATE;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_TRANSACTION_GUID;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

/**
 * A test case for testing essential {@link MetaProperty} functionality from perspective of a persisted entity.
 *
 * @author TG Team
 *
 */
public class MetaPropertyIdentificationOfRetrievablePropertiesTest extends AbstractDaoTestCase {

    @Test
    public void identification_of_retrievable_properties_for_non_composite_entity_with_non_persistent_props_other_than_desc() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        assertFalse(cat1.getPropertyOptionally(ID).isPresent());
        assertFalse(cat1.getPropertyOptionally(VERSION).isPresent());


        final List<MetaProperty<?>> retrievableProps = cat1.getProperties().values().stream()
                .filter(p -> p.isRetrievable()).collect(Collectors.toList());

        assertEquals(13, retrievableProps.size());

        final Set<String> names = retrievableProps.stream().map(p -> p.getName()).collect(Collectors.toSet());
        assertTrue(names.contains(KEY));
        assertTrue(names.contains(DESC));
        assertTrue(names.contains(ACTIVE));
        assertTrue(names.contains(REF_COUNT));
        assertTrue(names.contains(CREATED_BY));
        assertTrue(names.contains(CREATED_DATE));
        assertTrue(names.contains(CREATED_TRANSACTION_GUID));
        assertTrue(names.contains(LAST_UPDATED_BY));
        assertTrue(names.contains(LAST_UPDATED_DATE));
        assertTrue(names.contains(LAST_UPDATED_TRANSACTION_GUID));
        assertTrue(names.contains("parent"));
    }

    @Test
    public void identification_of_retrievable_properties_for_composite_entity_with_non_persistent_desc() {
        final TgOrgUnit2 cat1 = co$(TgOrgUnit2.class).findByKey(co$(TgOrgUnit1.class).findByKey("Org1"), "Org1_1");
        assertFalse(cat1.getPropertyOptionally(ID).isPresent());
        assertFalse(cat1.getPropertyOptionally(VERSION).isPresent());


        final List<MetaProperty<?>> retrievableProps = cat1.getProperties().values().stream().
                filter(p -> p.isRetrievable()).collect(Collectors.toList());

        assertEquals(2, retrievableProps.size());

        final Set<String> names = retrievableProps.stream().map(p -> p.getName()).collect(Collectors.toSet());
        assertTrue(names.contains("name"));
        assertTrue(names.contains("parent"));
    }

    @Test
    public void identifycation_of_retrievable_properties_for_entity_with_calcualted_props() {
        final TgVehicle veh = co$(TgVehicle.class).findByKeyAndFetch(fetchAll(TgVehicle.class), "CAR2");

        final List<MetaProperty<?>> retrievableProps = veh.getProperties().values().stream()
                .filter(p -> p.isRetrievable()).collect(Collectors.toList());

        assertEquals(23, retrievableProps.size());

        final Set<String> names = retrievableProps.stream().map(p -> p.getName()).collect(Collectors.toSet());
        assertTrue(names.contains(KEY));
        assertTrue(names.contains(DESC));
        assertTrue(names.contains("active")); // TgVehicle has property active, but is not an activatable entity
        assertTrue(names.contains("initDate"));
        assertTrue(names.contains("replacedBy"));
        assertTrue(names.contains("station"));
        assertTrue(names.contains("model"));
        assertTrue(names.contains("price"));
        assertTrue(names.contains("purchasePrice"));
        assertTrue(names.contains("leased"));
        assertTrue(names.contains("lastMeterReading"));
        assertTrue(names.contains("lastFuelUsage"));
        assertTrue(names.contains("constValueProp"));
        assertTrue(names.contains("calc0"));
        assertTrue(names.contains("lastFuelUsageQty"));
        assertTrue(names.contains("sumOfPrices"));
        assertTrue(names.contains("calc2"));
        assertTrue(names.contains("calc3"));
        assertTrue(names.contains("calc4"));
        assertTrue(names.contains("calc5"));
        assertTrue(names.contains("calc6"));
        assertTrue(names.contains("calcModel"));
        assertTrue(names.contains("finDetails"));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgFuelType petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

        save(new_(TgCategory.class, "Cat1").setActive(true));

        final TgOrgUnit1 org1 = save(new_(TgOrgUnit1.class, "Org1"));
        final TgOrgUnit2 orgUnit2 = save(new_composite(TgOrgUnit2.class, org1, "Org1_1"));
        final TgOrgUnit3 orgUnit3 = save(new_composite(TgOrgUnit3.class, orgUnit2, "orgunit3"));
        final TgOrgUnit4 orgUnit4 = save(new_composite(TgOrgUnit4.class, orgUnit3, "orgunit4"));
        final TgOrgUnit5 orgUnit5 = save(new_composite(TgOrgUnit5.class, orgUnit4, "orgunit5").setFuelType(petrolFuelType));

        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleModel m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));

        save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").setInitDate(date("2007-01-01 00:00:00")).setModel(m316).setPrice(new Money("200")).setPurchasePrice(new Money("100")).setActive(false).setLeased(true).setLastMeterReading(new BigDecimal("105")).setStation(orgUnit5));
    }

}