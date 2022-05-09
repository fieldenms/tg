package ua.com.fielden.platform.processors.meta_model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 *
 * These tests cover the entity meta-model and its use for entity graph traversal in order to reference entity properties.
 *
 * @author TG Team
 *
 */
public class MetaModelTest {

//    @Test
//    public void non_entity_type_properties_can_be_referenced_directly() {
//        String personVehicleInsuranceCost = MetaModels.Person.vehicle.insurance.cost;
//        assertEquals("vehicle.insurance.cost", personVehicleInsuranceCost);
//
//        String personHouseInsuranceCost = MetaModels.Person.house.insurance.cost;
//        assertEquals("house.insurance.cost", personHouseInsuranceCost);
//
//        String vehiclePropertyNameThatIsErrorProneAndLong = MetaModels.Vehicle.propertyNameThatIsErrorProneAndLong;
//        assertEquals("propertyNameThatIsErrorProneAndLong", vehiclePropertyNameThatIsErrorProneAndLong);
//    }
//    
//    @Test
//    public void entity_type_properties_can_be_referenced_with_toString_method() {
//        String personHouse = MetaModels.Person.house.toString();
//        assertEquals("house", personHouse);
//
//        String personVehicle = MetaModels.Person.vehicle.toString();
//        assertEquals("vehicle", personVehicle);
//    }
    
    @Test
    public void person_meta_model() {
        final String generatedSourcesPackageName = this.getClass().getPackageName() + ".meta";
    }

}
