package ua.com.fielden.platform.eql.retrieval;


import org.junit.Ignore;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.*;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// This test suite covers EQL expressions for implicitly calculated properties.
///
/// EQL expressions for composite keys are covered separately in [CompositeKeyEqlExpressionGenerator].
///
public class ImplicitlyCalculatedPropertiesTest extends AbstractEqlShortcutTest {

    @Test
    public void expression_for_id_of_synthetic_entity_is_correct() {
        final EntityResultQueryModel<TgAverageFuelUsage> act = select(TgAverageFuelUsage.class).where().prop("id").isNotNull().model();
        final EntityResultQueryModel<TgAverageFuelUsage> exp = select(TgAverageFuelUsage.class).where().prop("key").isNotNull().model();
        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_id_as_common_subproperty_in_union_property_of_persistent_entity_is_correct() {
        final EntityResultQueryModel<TgBogie> act = select(TgBogie.class).where().
                prop("location.id").
                eq().val(100).model();

        final EntityResultQueryModel<TgBogie> exp = select(TgBogie.class).where().
                caseWhen().prop("location.wagonSlot").isNotNull().then().prop("location.wagonSlot.id").
                    when().prop("location.workshop").isNotNull().then().prop("location.workshop.id").
                    end().
                    eq().val(100).model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_id_as_common_subproperty_in_union_property_of_synthetic_entity_is_correct() {
        final EntityResultQueryModel<TgReBogieWithHighLoad> act = select(TgReBogieWithHighLoad.class).where().
                prop("location.id").
                eq().val(100).model();

        final EntityResultQueryModel<TgReBogieWithHighLoad> exp = select(TgReBogieWithHighLoad.class).where().
                caseWhen().prop("location.wagonSlot").isNotNull().then().prop("location.wagonSlot.id").
                    when().prop("location.workshop").isNotNull().then().prop("location.workshop.id").
                    end().
                    eq().val(100).model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_key_as_common_subproperty_in_union_property_of_persistent_entity_is_correct() {
        final EntityResultQueryModel<TgBogie> act = select(TgBogie.class).where().
                prop("location.key").
                eq().val(100).model();

        final EntityResultQueryModel<TgBogie> exp = select(TgBogie.class).where().
                caseWhen().prop("location.wagonSlot").isNotNull().then().prop("location.wagonSlot.key").
                    when().prop("location.workshop").isNotNull().then().prop("location.workshop.key").
                    end().
                    eq().val(100).model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_key_as_common_subproperty_in_union_property_of_synthetic_entity_is_correct() {
        final EntityResultQueryModel<TgReBogieWithHighLoad> act = select(TgReBogieWithHighLoad.class).where().
                prop("location.key").
                isNotNull().model();

        final EntityResultQueryModel<TgReBogieWithHighLoad> exp = select(TgReBogieWithHighLoad.class).where().
                caseWhen().prop("location.wagonSlot").isNotNull().then().prop("location.wagonSlot.key").
                    when().prop("location.workshop").isNotNull().then().prop("location.workshop.key").
                    end().
                    isNotNull().model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    @Ignore // FIXME
    public void expression_for_desc_as_common_subproperty_in_union_property_of_persistent_entity_is_correct() {
        final EntityResultQueryModel<TgBogie> act = select(TgBogie.class).where().prop("location.desc").isNotNull().model();

        final EntityResultQueryModel<TgBogie> exp = select(TgBogie.class).where().val(null).isNotNull().model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    @Ignore //FIXME
    public void expression_for_desc_as_common_subproperty_in_union_property_of_synthetic_entity_is_correct() {
        final EntityResultQueryModel<TgReBogieWithHighLoad> act = select(TgReBogieWithHighLoad.class).where().prop("location.desc").isNotNull().model();

        final EntityResultQueryModel<TgReBogieWithHighLoad> exp = select(TgReBogieWithHighLoad.class).where().val(null).isNotNull().model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_one_of_other_common_subproperties_in_union_property_of_persistent_entity_is_correct() {
        final EntityResultQueryModel<TgBogie> act = select(TgBogie.class).where().
                prop("location.fuelType").
                isNotNull().model();

        final EntityResultQueryModel<TgBogie> exp = select(TgBogie.class).where().
                caseWhen().prop("location.wagonSlot").isNotNull().then().prop("location.wagonSlot.fuelType").
                    when().prop("location.workshop").isNotNull().then().prop("location.workshop.fuelType").
                    end().
                    isNotNull().model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_one_of_other_common_subproperties_in_union_property_of_synthetic_entity_is_correct() {
        final EntityResultQueryModel<TgReBogieWithHighLoad> act = select(TgReBogieWithHighLoad.class).where().prop("location.fuelType").isNotNull().model();

        final EntityResultQueryModel<TgReBogieWithHighLoad> exp = select(TgReBogieWithHighLoad.class).where().
                caseWhen().prop("location.wagonSlot").isNotNull().then().prop("location.wagonSlot.fuelType").
                    when().prop("location.workshop").isNotNull().then().prop("location.workshop.fuelType").
                    end().isNotNull().model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_one_2_one_property_of_persistent_entity_type_declared_and_available_at_persistent_entity_is_correct() {
        final EntityResultQueryModel<TgVehicle> act = select(TgVehicle.class).where().
                prop("finDetails").
                isNotNull().
                model();

        final EntityResultQueryModel<TgVehicle> exp = select(TgVehicle.class).where().
                model(select(TgVehicleFinDetails.class).where().prop("key").eq().extProp("id").model()).
                isNotNull().
                model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_one_2_one_property_of_synthetic_entity_type_declared_and_available_at_persistent_entity_is_correct() {
        final EntityResultQueryModel<TgVehicle> act = select(TgVehicle.class).where().
                prop("maxReading").
                isNotNull().
                model();

        final EntityResultQueryModel<TgVehicle> exp = select(TgVehicle.class).where().
                model(select(TgReMaxVehicleReading.class).where().prop("key").eq().extProp("id").model()).
                isNotNull().
                model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_one_2_one_property_of_persistent_entity_type_declared_at_persistent_entity_and_available_at_descendant_synthetic_entity_is_correct() {
        final EntityResultQueryModel<TgReVehicleWithHighPrice> act = select(TgReVehicleWithHighPrice.class).where().
                prop("finDetails").
                isNotNull().
                model();

        final EntityResultQueryModel<TgReVehicleWithHighPrice> exp = select(TgReVehicleWithHighPrice.class).where().
                model(select(TgVehicleFinDetails.class).where().prop("key").eq().extProp("id").model()).
                isNotNull().
                model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_one_2_one_property_of_synthetic_entity_type_declared_at_persistent_entity_and_available_at_descendant_synthetic_entity_is_correct() {
        final EntityResultQueryModel<TgReVehicleWithHighPrice> act = select(TgReVehicleWithHighPrice.class).where().
                prop("maxReading").
                isNotNull().
                model();

        final EntityResultQueryModel<TgReVehicleWithHighPrice> exp = select(TgReVehicleWithHighPrice.class).where().
                model(select(TgReMaxVehicleReading.class).where().prop("key").eq().extProp("id").model()).
                isNotNull().
                model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_one_2_one_property_of_persistent_entity_type_declared_and_available_at_synthetic_entity_with_id_is_correct() {
        final EntityResultQueryModel<TgReVehicleWithHighPrice> act = select(TgReVehicleWithHighPrice.class).where().
                prop("techDetails").
                isNotNull().
                model();

        final EntityResultQueryModel<TgReVehicleWithHighPrice> exp = select(TgReVehicleWithHighPrice.class).where().
                model(select(TgVehicleTechDetails.class).where().prop("key").eq().extProp("id").model()).
                isNotNull().
                model();

        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void expression_for_one_2_one_property_of_synthetic_entity_type_declared_and_available_at_synthetic_entity_with_id_is_correct() {
        final EntityResultQueryModel<TgReVehicleWithHighPrice> act = select(TgReVehicleWithHighPrice.class).where().
                prop("averageFuelUsage").
                isNotNull().
                model();

        final EntityResultQueryModel<TgReVehicleWithHighPrice> exp = select(TgReVehicleWithHighPrice.class).where().
                model(select(TgAverageFuelUsage.class).where().prop("key").eq().extProp("id").model()).
                isNotNull().
                model();

        assertModelResultsAreEqual(exp, act);
    }
}
