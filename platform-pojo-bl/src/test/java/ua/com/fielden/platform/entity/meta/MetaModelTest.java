package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.entities.EntityWithBce;
import ua.com.fielden.platform.entity.meta.test_meta_models.MetaModels;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 *
 * These tests cover the entity meta-model and its use for entity graph traversal in order to reference entity properties.
 *
 * @author TG Team
 *
 */
public class MetaModelTest {

    @Test
    public void non_entity_type_properties_can_be_referenced_directly() {
        String personVehicleInsuranceCost = MetaModels.Person.vehicle.insurance.cost;
        assertEquals("vehicle.insurance.cost", personVehicleInsuranceCost);

        String personHouseInsuranceCost = MetaModels.Person.house.insurance.cost;
        assertEquals("house.insurance.cost", personHouseInsuranceCost);

        String vehiclePropertyNameThatIsErrorProneAndLong = MetaModels.Vehicle.propertyNameThatIsErrorProneAndLong;
        assertEquals("propertyNameThatIsErrorProneAndLong", vehiclePropertyNameThatIsErrorProneAndLong);
    }
    
    @Test
    public void entity_type_properties_can_be_referenced_with_toString_method() {
        String personHouse = MetaModels.Person.house.toString();
        assertEquals("house", personHouse);

        String personVehicle = MetaModels.Person.vehicle.toString();
        assertEquals("vehicle", personVehicle);
    }

}
