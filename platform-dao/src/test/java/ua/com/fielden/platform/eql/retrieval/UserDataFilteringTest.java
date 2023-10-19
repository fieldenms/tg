package ua.com.fielden.platform.eql.retrieval;


import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;

public class UserDataFilteringTest extends AbstractEqlShortcutTest {
    
    @Test
    public void udf_with_single_condition_applied_to_query_with_no_conditions_works() {
        final EntityResultQueryModel<TgAuthor> act = select(TgAuthor.class).model();
        act.setFilterable(true);
        final EntityResultQueryModel<TgAuthor> exp = select(TgAuthor.class).where().prop("key").isNotNull().model();
        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void udf_with_single_condition_applied_to_query_with_single_condition_works() {
        final EntityResultQueryModel<TgAuthor> act = select(TgAuthor.class).where().prop("name.key").eq().val("Paul").model();
        act.setFilterable(true);
        final EntityResultQueryModel<TgAuthor> exp = select(TgAuthor.class).where().prop("key").isNotNull().and().prop("name.key").eq().val("Paul").model();
        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void udf_with_single_condition_applied_to_query_with_compound_condition_works() {
        final EntityResultQueryModel<TgAuthor> act = select(TgAuthor.class).where().prop("name.key").eq().val("Paul").or().prop("name.key").eq().val("John").model();
        act.setFilterable(true);
        final EntityResultQueryModel<TgAuthor> exp = select(TgAuthor.class).where().prop("key").isNotNull().and().begin().prop("name.key").eq().val("Paul").or().prop("name.key").eq().val("John").end().model();
        assertModelResultsAreEqual(exp, act);
    }

    @Test 
    public void udf_with_compound_condition_applied_to_query_with_no_conditions_works() {
        final EntityResultQueryModel<TeVehicle> act = select(VEHICLE).model();
        act.setFilterable(true);
        final EntityResultQueryModel<TeVehicle> exp = select(TeVehicle.class).where().begin().prop("key").isNotNull().or().prop("desc").isNotNull().end().model();
        assertModelResultsAreEqual(exp, act);
    }

    @Test 
    public void udf_with_compound_condition_applied_to_query_with_single_condition_works() {
        final EntityResultQueryModel<TeVehicle> act = select(VEHICLE).where().prop("model.make.key").eq().val("MERC").model();
        act.setFilterable(true);
        final EntityResultQueryModel<TeVehicle> exp = select(TeVehicle.class).where().begin().prop("key").isNotNull().or().prop("desc").isNotNull().end().and().prop("model.make.key").eq().val("MERC").model();
        assertModelResultsAreEqual(exp, act);
    }
    
    @Test 
    public void udf_with_compound_condition_applied_to_query_with_compound_condition_works() {
        final EntityResultQueryModel<TeVehicle> act = select(VEHICLE).where().prop("model.make.key").eq().val("MERC").or().prop("model.desc").isNull().model();
        act.setFilterable(true);
        final EntityResultQueryModel<TeVehicle> exp = select(TeVehicle.class).where().
                begin().prop("key").isNotNull().or().prop("desc").isNotNull().end().
                and().
                begin().prop("model.make.key").eq().val("MERC").or().prop("model.desc").isNull().end().model();
        assertModelResultsAreEqual(exp, act);
    }

    @Test // case of join without aliases and absence of ambiguity during filter condition resolution
    public void ambiguous_prop_from_udf_condition_works() {
        final EntityResultQueryModel<TeVehicle> act = select(VEHICLE).join(TgOrgUnit5.class).on().prop("station").eq().prop("fuelType").model();
        act.setFilterable(true);
        final EntityResultQueryModel<TeVehicle> exp = select(VEHICLE).as("v").join(TgOrgUnit5.class).on().prop("station").eq().prop("fuelType").where().begin().prop("v.key").isNotNull().or().prop("v.desc").isNotNull().end().model();
        assertModelResultsAreEqual(exp, act);
    }

    @Test
    public void filtered_source_query_works() {
        final EntityResultQueryModel<TgAuthor> filteredSourceQry = select(TgAuthor.class).model();
        filteredSourceQry.setFilterable(true);
        final EntityResultQueryModel<TgAuthor> actQry = select(filteredSourceQry).model();
        final EntityResultQueryModel<TgAuthor> expQry = select(select(TgAuthor.class).where().prop("key").isNotNull().model()).model();
        assertModelResultsAreEqual(expQry, actQry);
    }

    @Test
    public void udf_with_calc_prop_in_its_condition_works() {
        final EntityResultQueryModel<TeVehicleModel> act = select(TeVehicleModel.class).where().prop("desc").eq().val("DDD").model();
        act.setFilterable(true);
        final EntityResultQueryModel<TeVehicleModel> exp = select(TeVehicleModel.class).where().begin().
                model(select(TeVehicleMake.class).where().prop("id").eq().extProp("make").yield().prop("key").modelAsPrimitive()).isNotNull().
                or().
                prop("desc").isNotNull().
                end().
                and().
                prop("desc").eq().val("DDD").
                model();
        assertModelResultsAreEqual(exp, act);
    }
    
    // case of SE with UDF in its model
    
    // case of SE being included into UDF list
    
    // case of using calc prop(s) (with/without UDF)  within filtering condition

    // case of using subqueries/sourcequeries within UDF filtering condition
    
    // case of UDF used while composing condition in UDF
 }