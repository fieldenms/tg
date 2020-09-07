package ua.com.fielden.platform.entity.query.fetching;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.dao.ISecurityRoleAssociation;
import ua.com.fielden.platform.dao.IUserAndRoleAssociation;
import ua.com.fielden.platform.dao.IUserRole;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.ITgAuthor;
import ua.com.fielden.platform.sample.domain.ITgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.ITgBogie;
import ua.com.fielden.platform.sample.domain.ITgBogieLocation;
import ua.com.fielden.platform.sample.domain.ITgEntityWithComplexSummaries;
import ua.com.fielden.platform.sample.domain.ITgFuelType;
import ua.com.fielden.platform.sample.domain.ITgFuelUsage;
import ua.com.fielden.platform.sample.domain.ITgMakeCount;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit5;
import ua.com.fielden.platform.sample.domain.ITgPersonName;
import ua.com.fielden.platform.sample.domain.ITgPublishedYearly;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.ITgWagon;
import ua.com.fielden.platform.sample.domain.ITgWagonSlot;
import ua.com.fielden.platform.sample.domain.ITgWorkshop;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgEntityWithComplexSummaries;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgMakeCount;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgPublishedYearly;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

public class EntityQueryExecutionTest extends AbstractDaoTestCase {

    private final ITgPersonName personNameDao = getInstance(ITgPersonName.class);
    private final ITgAuthor authorDao = getInstance(ITgAuthor.class);
    private final ITgBogie bogieDao = getInstance(ITgBogie.class);
    private final ITgBogieLocation bogieLocationDao = getInstance(ITgBogieLocation.class);
    private final ITgWagon wagonDao = getInstance(ITgWagon.class);
    private final ITgWorkshop workshopDao = getInstance(ITgWorkshop.class);
    private final ITgWagonSlot wagonSlotDao = getInstance(ITgWagonSlot.class);
    private final ITgVehicleModel vehicleModelDao = getInstance(ITgVehicleModel.class);
    private final ITgVehicleMake vehicleMakeDao = getInstance(ITgVehicleMake.class);
    private final ITgVehicle coVehicle = getInstance(ITgVehicle.class);
    private final ITgFuelUsage fuelUsageDao = getInstance(ITgFuelUsage.class);
    private final ITgFuelType coFuelType = getInstance(ITgFuelType.class);
    private final IUser userDao = getInstance(IUser.class);
    private final IUserRole userRoleDao = getInstance(IUserRole.class);
    private final IUserAndRoleAssociation userAndRoleAssociationDao = getInstance(IUserAndRoleAssociation.class);
    private final IEntityAggregatesOperations aggregateDao = getInstance(IEntityAggregatesOperations.class);
    private final EntityWithMoneyDao entityWithMoneyDao = getInstance(EntityWithMoneyDao.class);
    private final ISecurityRoleAssociation secRolAssociationDao = getInstance(ISecurityRoleAssociation.class);
    private final ITgMakeCount makeCountDao = getInstance(ITgMakeCount.class);
    private final ITgAverageFuelUsage averageFuelUsageDao = getInstance(ITgAverageFuelUsage.class);
    private final ITgOrgUnit5 orgUnit5Dao = getInstance(ITgOrgUnit5.class);
    private final ITgEntityWithComplexSummaries coEntityWithComplexSummaries = getInstance(ITgEntityWithComplexSummaries.class);
    private final ICompoundCondition0<TgVehicle> singleResultQueryStub = select(TgVehicle.class).where().prop("key").eq().val("CAR1");
    private final ITgPublishedYearly coPublishedYearly = getInstance(ITgPublishedYearly.class);

    /////////////////////////////////////// WITHOUT ASSERTIONS /////////////////////////////////////////

    @Test
    public void test_query_based_entities_with_composite_props() {
        final EntityResultQueryModel<TgMakeCount> qry = select(TgMakeCount.class).model();
        makeCountDao.getAllEntities(from(qry).with(fetch(TgMakeCount.class)).model());
    }

    @Test
    public void test_ordering_by_yielded_prop() {
        final AggregatedResultQueryModel qry = select(TgVehicle.class).groupBy().prop("model.make.key"). //
                yield().prop("model.make.key").as("makeKey"). //
                yield().countAll().as("count"). //
                modelAsAggregate();

        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(qry).with(orderBy().yield("count").desc().model()).model());
    }

    @Ignore
    @Test
    public void test_query_query_with_grouping_and_aggregation1() {
        final EntityResultQueryModel<TgVehicleModel> qry = select(TgVehicleModel.class).as("AAA").where().prop("make.key").eq().val("AA").model();
        final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(qry).model());
    }

    @Test
    public void test_query_union_entity_implied_query_model_with_ordering() {
        final EntityResultQueryModel<TgBogieLocation> qry = select(TgBogieLocation.class).model();
        //System.out.println(bogieLocationDao.getAllEntities(from(qry).with(fetchAll(TgBogieLocation.class)).with(orderBy().prop("key").asc().model()).model()));
    }

    @Test
    public void test_query_union_entity_implied_query_model_with_ordering_by_yield() {
        final EntityResultQueryModel<TgBogieLocation> qry = select(TgBogieLocation.class).model();
        //System.out.println(bogieLocationDao.getAllEntities(from(qry).with(fetchAll(TgBogieLocation.class).with("key")).with(orderBy().yield("key").asc().model()).model()));
    }

    @Test
    public void test_query_union_entityA_() {
        final EntityResultQueryModel<TgBogieLocation> qry1 = select(TgWagonSlot.class).as("a").yield().prop("a").as("wagonSlot").modelAsEntity(TgBogieLocation.class);
        final List<TgBogieLocation> models = bogieLocationDao.getAllEntities(from(qry1).with(fetchAll(TgBogieLocation.class)).model());
    }

    @Test
    public void test_case_when_function() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --").then().prop("key").end().isNotNull().model();
        final IFunctionCompoundCondition0<IComparisonOperator0<TgVehicle>, TgVehicle> qry2 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --");
        final EntityResultQueryModel<TgVehicle> qry3 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --"). //
        then().prop("key").//
        when().prop("a").isNotNull().then().prop("a").end().isNotNull().model();

        final EntityResultQueryModel<TgVehicle> qry4 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --"). //
        then().prop("key").//
        when().prop("a").isNotNull().then().prop("a").otherwise().prop("a").end(). //
        isNotNull().model();

        final EntityResultQueryModel<TgVehicle> qry5 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --"). //
        then().prop("key").//
        when().prop("a").isNotNull().then().prop("a").otherwise().prop("a").end(). //
        isNotNull().model();

        final EntityResultQueryModel<TgVehicle> qry6 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x"). //
        then().prop("key").otherwise().prop("a").end(). //
        isNotNull().model();
    }

    @Test
    public void select_without_from_clause_can_be_used_for_returning_a_tuple_of_values_as_aggregates() {
        final AggregatedResultQueryModel a = select().
                yield().caseWhen().exists(select(TgVehicle.class).where().prop("key").eq().val("CAR1").model()).then().val(true).otherwise().val(false).endAsBool().as("exists").
                yield().val("aaa").as("prop2").
                modelAsAggregate();
        final EntityAggregates result = aggregateDao.getAllEntities(from(a).model()).get(0);
        assertEquals(Character.valueOf('Y'), result.get("exists"));
        assertEquals("aaa", result.get("prop2"));
    }
    
    @Test
    public void exists_can_be_used_to_determine_that_query_result_exists() {
        final EntityResultQueryModel<TgVehicle> a = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        assertTrue(coVehicle.exists(a));
    }

    @Test
    public void exists_can_be_used_to_determine_that_query_result_does_not_exist() {
        final EntityResultQueryModel<TgVehicle> a = select(TgVehicle.class).where().prop("key").eq().val("CIR1").model();
        assertFalse(coVehicle.exists(a));
    }

    @Test
    public void exists_can_be_used_to_determine_that_parameterised_query_result_exists() {
        final EntityResultQueryModel<TgVehicle> a = select(TgVehicle.class).where().prop("key").eq().param("car_name").model();
        assertTrue(coVehicle.exists(a, mapOf(t2("car_name", "CAR1"))));
    }
    
    @Test
    public void exists_can_be_used_to_determine_that_parameterised_query_result_does_not_exist() {
        final EntityResultQueryModel<TgVehicle> a = select(TgVehicle.class).where().prop("key").eq().param("car_name").model();
        assertFalse(coVehicle.exists(a, mapOf(t2("car_name", "CIR1"))));
    }
    
    /////////////////////////////////////// TEST SQL FUNCTIONS ///////////////////////////////////////////////////////////////////

    @Test
    public void day_of_week_function_correctly_attributes_range_1_7_to_days_against_h2_database() {
        final AggregatedResultQueryModel qryMonday = singleResultQueryStub.yield().dayOfWeekOf().val(date("2018-04-02 00:00:00")).as("result").modelAsAggregate();
        assertEquals(valueOf(1), aggregateDao.getEntity(from(qryMonday).model()).get("result"));

        final AggregatedResultQueryModel qryTuesday = singleResultQueryStub.yield().dayOfWeekOf().val(date("2018-04-03 00:00:00")).as("result").modelAsAggregate();
        assertEquals(valueOf(2), aggregateDao.getEntity(from(qryTuesday).model()).get("result"));

        final AggregatedResultQueryModel qryWednesday = singleResultQueryStub.yield().dayOfWeekOf().val(date("2018-04-04 00:00:00")).as("result").modelAsAggregate();
        assertEquals(valueOf(3), aggregateDao.getEntity(from(qryWednesday).model()).get("result"));

        final AggregatedResultQueryModel qryThursday = singleResultQueryStub.yield().dayOfWeekOf().val(date("2018-04-05 00:00:00")).as("result").modelAsAggregate();
        assertEquals(valueOf(4), aggregateDao.getEntity(from(qryThursday).model()).get("result"));

        final AggregatedResultQueryModel qryFriday = singleResultQueryStub.yield().dayOfWeekOf().val(date("2018-04-06 00:00:00")).as("result").modelAsAggregate();
        assertEquals(valueOf(5), aggregateDao.getEntity(from(qryFriday).model()).get("result"));

        final AggregatedResultQueryModel qrySaturday = singleResultQueryStub.yield().dayOfWeekOf().val(date("2018-04-07 00:00:00")).as("result").modelAsAggregate();
        assertEquals(valueOf(6), aggregateDao.getEntity(from(qrySaturday).model()).get("result"));

        final AggregatedResultQueryModel qrySunday = singleResultQueryStub.yield().dayOfWeekOf().val(date("2018-04-08 00:00:00")).as("result").modelAsAggregate();
        assertEquals(valueOf(7), aggregateDao.getEntity(from(qrySunday).model()).get("result"));
    }

    @Test
    public void day_of_week_function_can_be_used_for_data_querying_against_h2_database() {
        assertEquals(1, co(TgFuelUsage.class).count(select(TgFuelUsage.class).where().dayOfWeekOf().prop("date").eq().val(4).model()));
        assertEquals(1, co(TgFuelUsage.class).count(select(TgFuelUsage.class).where().dayOfWeekOf().addTimeIntervalOf().val(1).days().to().prop("date").eq().val(5).model()));
    }

    @Test
    public void add_seconds_function_works_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().addTimeIntervalOf().val(10).seconds().to().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        assertEquals(date("2007-01-01 00:00:10"), aggregateDao.getEntity(from(qry).model()).get("result"));
    }

    @Test
    public void add_minutes_function_works_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().addTimeIntervalOf().val(10).minutes().to().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        assertEquals(date("2007-01-01 00:10:00"), aggregateDao.getEntity(from(qry).model()).get("result"));
    }
    
    @Test
    public void add_hours_function_works_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().addTimeIntervalOf().val(10).hours().to().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        assertEquals(date("2007-01-01 10:00:00"), aggregateDao.getEntity(from(qry).model()).get("result"));
    }

    @Test
    public void add_days_function_works_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().addTimeIntervalOf().val(10).days().to().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        assertEquals(date("2007-01-11 00:00:00"), aggregateDao.getEntity(from(qry).model()).get("result"));
    }
    
    @Test
    public void add_months_function_works_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().addTimeIntervalOf().val(10).months().to().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        assertEquals(date("2007-11-01 00:00:00"), aggregateDao.getEntity(from(qry).model()).get("result"));
    }
    
    @Test
    public void add_years_function_works_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().addTimeIntervalOf().val(10).years().to().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        assertEquals(date("2017-01-01 00:00:00"), aggregateDao.getEntity(from(qry).model()).get("result"));
    }
    
    @Test
    public void can_query_date_prop_using_add_time_interval_operations_against_h2_database() {
        final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class)
                .where().prop("key").eq().val("CAR2")
                .and()  .prop("initDate").le().addTimeIntervalOf().val(10).hours().to().prop("initDate").model();
        assertEquals(1, co(TgVehicle.class).count(query));
    }

    @Test
    public void can_query_date_prop_using_add_time_interval_operations_with_negative_values_against_h2_database() {
        final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class)
                .where().prop("initDate").le().addTimeIntervalOf().val(-5).years().to().val(date("2007-01-01 00:00:00")).model();
        assertEquals(1, co(TgVehicle.class).count(query));
    }

    @Test
    public void count_seconds_function_works_correctly_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().count().seconds().between().val(date("2007-01-01 00:01:00")).and().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        final EntityAggregates result = aggregateDao.getEntity(from(qry).model());
        assertEquals("Incorrect duration in seconds", BigInteger.valueOf(60l), result.get("result"));
    }
    
    @Test
    public void count_minutes_function_works_correctly_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().count().minutes().between().val(date("2007-01-01 01:00:00")).and().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        final EntityAggregates result = aggregateDao.getEntity(from(qry).model());
        assertEquals("Incorrect duration in minutes", BigInteger.valueOf(60l), result.get("result"));
    }

    @Test
    public void count_hours_function_works_correctly_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().count().hours().between().val(date("2007-01-01 23:00:00")).and().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        final EntityAggregates result = aggregateDao.getEntity(from(qry).model());
        assertEquals("Incorrect duration in hours", BigInteger.valueOf(23l), result.get("result"));
    }

    @Test
    public void count_days_function_works_correctly_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().count().days().between().val(date("2007-01-10 00:00:00")).and().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        final EntityAggregates result = aggregateDao.getEntity(from(qry).model());
        assertEquals("Incorrect duration in days", BigInteger.valueOf(9l), result.get("result"));
    }
    
    @Test
    public void count_months_function_works_correctly_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().count().months().between().val(date("2007-10-01 00:00:00")).and().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        final EntityAggregates result = aggregateDao.getEntity(from(qry).model());
        assertEquals("Incorrect duration in months", BigInteger.valueOf(9l), result.get("result"));
    }
    
    @Test
    public void count_years_function_works_correctly_against_h2_database() {
        final AggregatedResultQueryModel qry = singleResultQueryStub.yield().count().years().between().val(date("2008-01-01 00:00:00")).and().val(date("2007-01-01 00:00:00")).as("result").modelAsAggregate();
        final EntityAggregates result = aggregateDao.getEntity(from(qry).model());
        assertEquals("Incorrect duration in years", BigInteger.valueOf(1l), result.get("result"));
    }
    
    
    /////////////////////////////////////// QUERING KEYS OF ENTITIES WITH COMPOSITE KEYS /////////////////////////////////////////

    @Test
    public void test_composite_key_virtual_representation_in_eql_search() {
        final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().prop("key").eq().val("WAGON2 1").model();
        final TgWagonSlot wagonSlot = wagonSlotDao.getEntity(from(qry).with(fetchAll(TgWagonSlot.class)).model());
        assertEquals("Incorrect key 1", "WAGON2", wagonSlot.getWagon().getKey());
        assertEquals("Incorrect key 2", "1", wagonSlot.getPosition().toString());
    }

    @Test
    public void test_composite_key_virtual_representation_as_pojo() {
        final String wagonSlotKey = "WAGON2 1";
        final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().prop("key").eq().val(wagonSlotKey).model();
        final TgWagonSlot wagonSlot = wagonSlotDao.getEntity(from(qry).with(fetchAll(TgWagonSlot.class)).model());
        assertEquals("Incorrect key 2", "WAGON2 1", wagonSlot.getKey().toString());
    }

    @Test
    public void test_query_for_correct_fetch_model_autoenhancement_in_case_of_composite_user_type_with_single_subproperty() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(qry).with(fetchOnly(TgVehicle.class). //
        with("key"). //
        with("purchasePrice")).model());
        assertEquals(new Money(new BigDecimal(10)), vehicle.getPurchasePrice());

        //  final TgVehicle vehicle2 = vehicleDao.getEntity(from(qry). //
        //      with(fetchOnly(TgVehicle.class). //
        //          with("aggregated"). //
        //          without("id"). //
        //          without("version")). //
        //      model());
        //  assertEquals(new BigDecimal("10.0000"), vehicle2.getAggregated());
    }

    @Test
    public void test_query_for_correct_fetch_model_autoenhancement_in_case_of_composite_user_type_with_single_subproperty_in() {
        final EntityResultQueryModel<TgVehicle> qry = select(select(TgVehicle.class).model()).
                where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(qry).with(fetchOnly(TgVehicle.class). //
        with("key"). //
        with("purchasePrice")).model());
        assertEquals(new Money(new BigDecimal(10)), vehicle.getPurchasePrice());
    }

    @Test
    public void test_ordering_by_non_yielded_prop() {
        final AggregatedResultQueryModel qry = select(TgVehicle.class).
                groupBy().prop("model.make.key").
                yield().prop("model.make.key").as("makeKey").
                yield().countAll().as("count").
                modelAsAggregate();
        try {
            aggregateDao.getAllEntities(from(qry).with(orderBy().yield("count1").desc().model()).model());
            fail("Should have failed while trying to order by not yielden property");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_query_query_with_grouping_and_subproperties() {
        final AggregatedResultQueryModel qry1 = select(TgVehicleModel.class).where().condition(null).or().allOfProps("1", "@").ge().val(222).and(). //
        condition(null).and().begin().condition(null).end().and().beginExpr().caseWhen().condition(null).then().now().when().condition(null).then().val(1).end().endExpr().isNotNull().modelAsAggregate();

        final AggregatedResultQueryModel qry = select(TgVehicleModel.class).
                groupBy().prop("make").
                yield().countOf().prop("make").as("dmakes"). //
                yield().prop("make.key").as("key").
                modelAsAggregate();
        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(qry).model());

        final ConditionModel c1 = cond().prop("aaa").eq().val(111).or().prop("bbb").isNotNull().model();
        final List<Pair<TokenCategory, Object>> expected = new ArrayList<>();
        expected.add(new Pair(TokenCategory.PROP, "aaa"));
        expected.add(new Pair(TokenCategory.COMPARISON_OPERATOR, ComparisonOperator.EQ));
        expected.add(new Pair(TokenCategory.VAL, 111));
        expected.add(new Pair(TokenCategory.LOGICAL_OPERATOR, LogicalOperator.OR));
        expected.add(new Pair(TokenCategory.PROP, "bbb"));
        expected.add(new Pair(TokenCategory.NULL_OPERATOR, true));
        assertEquals(expected, c1.getTokens());

        final IStandAloneConditionOperand<AbstractEntity<?>> s = cond();

        final IStandAloneConditionComparisonOperator<AbstractEntity<?>> d = s.prop("aaa");
        final ConditionModel c2 = d.eq().val(111).or().prop("bbb").isNotNull().model();
        assertEquals(expected, c2.getTokens());

        //System.out.println(cond().round().prop("a").to(3).eq().val(0).and().beginExpr().prop("a").endExpr().isNotNull().and().now().eq().val(1).and().exists(null).and().condition(null).and().concat().prop("a").with().prop("b").with().prop("c").end().eq().all(null).and().condition(null).model().getTokens());
    }

    ////////////////////////////////////////////////////////////////   UNION ENTITIES ////////////////////////////////////////////////////////////
    @Test
    public void test_query_union_entity_implied_query_model() {
        final EntityResultQueryModel<TgBogieLocation> qry = select(TgBogieLocation.class).model();
        assertEquals(workshopDao.count(select(TgWorkshop.class).model()) + wagonSlotDao.count(select(TgWagonSlot.class).model()), bogieLocationDao.count(qry));
    }

    @Test
    public void test_query_with_union_property_being_null() {
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("key").eq().val("BOGIE2").model();
        final TgBogie bogie = bogieDao.getEntity(from(qry).with(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class).with("wagonSlot").with("workshop"))).model());
        assertFalse(bogie.getProperty("location").isProxy());
        assertNull(bogie.getLocation());
    }

    @Test
    public void test_query_with_union_property_subproperties() {
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.workshop.key").eq().val("WSHOP1").or().prop("location.wagonSlot.wagon.key").eq().val("WAGON1").model();
        assertEquals(bogieDao.findByKey("BOGIE1"), bogieDao.getEntity(from(qry).model()));
    }

    @Test
    public void test_query_with_union_property_null_test_condition() {
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location").isNotNull().model();
        assertEquals(2, bogieDao.count(qry));
    }

    @Test
    public void can_retrieve_entities_with_union_type_property_with_different_activeProperty() {
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location").isNotNull().model();
        final List<TgBogie> bogies = bogieDao.getAllEntities(from(qry).with(fetchAll(TgBogie.class)).with(orderBy().prop("key").asc().model()).model());
        assertTrue(bogies.get(0).getLocation().activeEntity() instanceof TgWorkshop);
        assertTrue(bogies.get(1).getLocation().activeEntity() instanceof TgWagonSlot);
    }
    
    @Test
    public void test_query_with_union_property_subproperties_via_query_based_source() {
        final EntityResultQueryModel<TgBogie> qry = select(select(TgBogie.class).model()).where().prop("location.workshop.key").eq().val("WSHOP1").or().prop("location.wagonSlot.wagon.key").eq().val("WAGON1").model();
        assertEquals(bogieDao.findByKey("BOGIE1"), bogieDao.getEntity(from(qry).model()));
    }

    @Test
    public void can_query_union_entity_common_subprop_id() {
        final Long workshopId = workshopDao.findByKey("WSHOP1").getId();
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.id").eq().val(workshopId).model();
        final EntityResultQueryModel<TgBogie> expQry = select(TgBogie.class).where().prop("location.workshop.id").eq().val(workshopId).model();
        assertEquals(bogieDao.getEntity(from(expQry).model()), bogieDao.getEntity(from(qry).model()));
    }

    @Test
    public void can_query_union_entity_common_subprop_key() {
        final String workshopKey = "WSHOP1";
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.key").eq().val(workshopKey).model();
        final EntityResultQueryModel<TgBogie> expQry = select(TgBogie.class).where().prop("location.workshop.key").eq().val(workshopKey).model();
        assertEquals(bogieDao.getEntity(from(expQry).model()), bogieDao.getEntity(from(qry).model()));
    }

    @Test
    public void can_query_union_entity_common_subprop_desc() {
        final String workshopDesc = "Workshop 1";
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.desc").eq().val(workshopDesc).model();
        final EntityResultQueryModel<TgBogie> expQry = select(TgBogie.class).where().prop("location.workshop.desc").eq().val(workshopDesc).model();
        assertEquals(bogieDao.getEntity(from(expQry).model()), bogieDao.getEntity(from(qry).model()));
    }
    
    @Test
    public void can_query_union_entity_common_subprop_fuel_type() {
        final TgFuelType fuelType = coFuelType.findByKey("P");
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.fuelType").eq().val(fuelType).model();
        final EntityResultQueryModel<TgBogie> expQry = select(TgBogie.class).where().prop("location.wagonSlot.fuelType").eq().val(fuelType).model();
        assertEquals(bogieDao.getEntity(from(expQry).model()), bogieDao.getEntity(from(qry).model()));
    }

    @Test
    public void cannot_query_union_entity_common_subprop_fuel_type_key() {
        final String fuelTypeKey = "P";
        try {
            bogieDao.getEntity(from(select(TgBogie.class).where().prop("location.fuelType.key").eq().val(fuelTypeKey).model()).model());
            fail("Should have failed while trying to query union prop common prop subprop.");
            //TODO remove such limitation in EQL3.
        } catch (final Exception e) {
        }   
    }

    @Test
    public void test_query_with_union_entity_property_fetching_keys() {
        final String workshopKey = "WSHOP1";
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.key").eq().val(workshopKey).model();
        final QueryExecutionModel<TgBogie, EntityResultQueryModel<TgBogie>> model = from(qry).with(fetchOnly(TgBogie.class).with("id").with("key").with("location", fetchOnly(TgBogieLocation.class).with("key"))).model();
        final TgBogieLocation location = bogieDao.getEntity(model).getLocation();
        assertNotNull(location);
        assertNotNull(location.getKey());
        assertNotNull(location.getWorkshop());
        assertNotNull(location.getWorkshop().getId());
        assertNotNull(location.getWorkshop().getDesc());
        assertEquals(workshopKey, location.getWorkshop().getKey());
    }

    @Test
    public void test_query_with_union_entity_property_fetching() {
        final String workshopKey = "WSHOP1";
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.key").eq().val(workshopKey).model();
        final QueryExecutionModel<TgBogie, EntityResultQueryModel<TgBogie>> model = from(qry).with(fetchOnly(TgBogie.class).with("id").with("key").with("location", fetchOnly(TgBogieLocation.class).with("workshop"))).model();
        final TgBogieLocation location = bogieDao.getEntity(model).getLocation();
        assertNotNull(location);
        assertNotNull(location.getWorkshop());
        assertEquals(workshopKey, location.getWorkshop().getKey());
    }

    @Test
    public void test_query_with_usual_entity_property_fetching() {
        final String workshopKey = "orgunit5";
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("station.name").eq().val(workshopKey).model();
        final QueryExecutionModel<TgVehicle, EntityResultQueryModel<TgVehicle>> model = from(qry).with(fetchOnly(TgVehicle.class).with("id").with("key").with("station", fetchOnly(TgOrgUnit5.class).with("name"))).model();
        final TgOrgUnit5 location = coVehicle.getEntity(model).getStation();
        assertNotNull(location);
        assertEquals(workshopKey, location.getName());
    }

    @Test
    public void test_query_with_union_entity_property_fetching2() {
        final String workshopKey = "WSHOP1";
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.key").eq().val(workshopKey).model();
        final QueryExecutionModel<TgBogie, EntityResultQueryModel<TgBogie>> model = from(qry).with(fetchOnly(TgBogie.class).with("id").with("key").with("location", fetch(TgBogieLocation.class).with("workshop"))).model();
        final TgBogieLocation location = bogieDao.getEntity(model).getLocation();
        assertNotNull(location);
        assertNotNull(location.getWorkshop());
        assertEquals(workshopKey, location.getWorkshop().getKey());
    }

    @Test
    public void test_query_with_union_property0b() {
        final ExpressionModel idModel = expr().caseWhen().prop("wagonSlot").isNotNull().then().prop("wagonSlot"). //
        when().prop("workshop").isNotNull().then().prop("workshop").otherwise().val(null).end().model();

        final EntityResultQueryModel<TgBogieLocation> qry = select(TgBogieLocation.class).where().expr(idModel).eq().val(workshopDao.findByKey("WSHOP1")).model();
        final List<TgBogieLocation> models = bogieLocationDao.getAllEntities(from(qry).with(fetchAll(TgBogieLocation.class)).model());
        assertEquals("Incorrect key 1", "WSHOP1", models.get(0).getKey());
    }

    @Test
    public void test_query_with_union_property2() {
        final EntityResultQueryModel<TgWorkshop> qry = select(select(TgBogie.class).model()).where().prop("location.workshop.key").eq().val("WSHOP1").yield().prop("location.workshop").modelAsEntity(TgWorkshop.class);
        final List<TgWorkshop> models = workshopDao.getAllEntities(from(qry).with(fetch(TgWorkshop.class)).model());
        assertEquals("Incorrect key 1", "WSHOP1", models.get(0).getKey());
    }

    @Test
    public void test_query_union_entity_() {
        final EntityResultQueryModel<TgBogieLocation> qry1 = select(TgWagonSlot.class).as("a").yield().prop("a").as("wagonSlot").yield().val(null).as("workshop").modelAsEntity(TgBogieLocation.class);
        final EntityResultQueryModel<TgBogieLocation> qry2 = select(TgWorkshop.class).as("a").yield().val(null).as("wagonSlot").yield().prop("a").as("workshop").modelAsEntity(TgBogieLocation.class);
        final EntityResultQueryModel<TgBogieLocation> qry3 = select(qry1, qry2).model();

        bogieLocationDao.getAllEntities(from(qry1).with(fetchAll(TgBogieLocation.class)).model());
        bogieLocationDao.getAllEntities(from(qry2).with(fetchAll(TgBogieLocation.class)).model());

        final List<TgBogieLocation> models = bogieLocationDao.getAllEntities(from(qry3).with(fetchAll(TgBogieLocation.class)).model());
        assertEquals("Incorrect key", 13, models.size());
    }

    ////////////////////////////////////////////////////////////////   SYNTHETIC ENTITIES ////////////////////////////////////////////////////////////
    @Test
    public void synthetic_entity_with_first_query_source_having_null_value_in_place_of_entity_type_correctly_identifies_the_resultant_query_type() {
        final EntityResultQueryModel<TgPublishedYearly> qry = select(TgPublishedYearly.class).where().prop("author").isNull().model();
        final TgPublishedYearly summaryEntity = coPublishedYearly.getEntity(from(qry). //
        with(fetchAll(TgPublishedYearly.class)).model());
        assertNull(summaryEntity.getAuthor());
        assertEquals("Incorrect key", 4l, summaryEntity.getQty().longValue());
    }
    
    @Test
    public void test_retrieval_of_synthetic_entity6() {
        final EntityResultQueryModel<TgAverageFuelUsage> qry = select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2").model();
        final List<TgAverageFuelUsage> models = averageFuelUsageDao.getAllEntities(from(qry). //
        with(fetchAll(TgAverageFuelUsage.class).with("id")). //
        with("datePeriod.from", new DateTime(2008, 01, 01, 0, 0).toDate()). //
        with("datePeriod.to", new DateTime(2010, 01, 01, 0, 0).toDate()). //
        model());
        assertEquals("Incorrect key", 1, models.size());
        assertEquals("Incorrect key", coVehicle.findByKey("CAR2").getId(), models.get(0).getId());
    }

    @Test
    public void test_retrieval_of_synthetic_entity5() {
        final EntityResultQueryModel<TgAverageFuelUsage> qry = select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2").model();
        final List<TgAverageFuelUsage> models = averageFuelUsageDao.getAllEntities(from(qry). //
        with("datePeriod.from", new DateTime(2008, 01, 01, 0, 0).toDate()). //
        with("datePeriod.to", new DateTime(2010, 01, 01, 0, 0).toDate()). //
        with(fetch(TgAverageFuelUsage.class).with("qty").with("key", fetchKeyAndDescOnly(TgVehicle.class))). //
        model());
        assertEquals("Incorrect key", 1, models.size());
        assertEquals("Incorrect key", new BigDecimal("120.00"), models.get(0).getQty());
        assertEquals("Incorrect key", "CAR2", models.get(0).getKey().getKey());
    }

    @Test
    public void test_retrieval_of_synthetic_entity4() {
        final EntityResultQueryModel<TgAverageFuelUsage> qry = select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2").model();
        final List<TgAverageFuelUsage> models = averageFuelUsageDao.getAllEntities(from(qry). //
        with("datePeriod.from", null). //
        with("datePeriod.to", null). //
        with(fetch(TgAverageFuelUsage.class).with("qty").with("key", fetchKeyAndDescOnly(TgVehicle.class))). //
        model());
        assertEquals("Incorrect key", 1, models.size());
        assertEquals("Incorrect key", new BigDecimal("220.00"), models.get(0).getQty());
        assertEquals("Incorrect key", "CAR2", models.get(0).getKey().getKey());
    }

    @Test
    public void test_retrieval_of_synthetic_entity3() {
        final EntityResultQueryModel<TgMakeCount> qry = select(TgMakeCount.class).where().prop("key.key").in().values("MERC", "BMW").model();
        final List<TgMakeCount> models = makeCountDao.getAllEntities(from(qry).with(fetchOnly(TgMakeCount.class).with("key").with("count")).model());
        assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_retrieval_of_synthetic_entity2() {
        final EntityResultQueryModel<TgMakeCount> qry = select(TgMakeCount.class).where().prop("key.key").in().values("MERC", "BMW"). //
        yield().prop("key").as("key").yield().prop("count").as("count").modelAsEntity(TgMakeCount.class);

        final List<TgMakeCount> models = makeCountDao.getAllEntities(from(qry).with(fetch(TgMakeCount.class)).model());
        assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_retrieval_of_synthetic_entity() {
        final AggregatedResultQueryModel model = select(TgMakeCount.class).where().prop("key.key").in().values("MERC", "BMW").yield().prop("key").as("make").modelAsAggregate();
        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).with(fetchAggregates().with("make", fetchAll(TgVehicleMake.class))).model());
        assertEquals("Incorrect key", 2, models.size());
    }

    ////////////////////////////////////////////////////////////////   CALCULATED PROPS ////////////////////////////////////////////////////////////

    @Test
    public void test_calculated_entity_props_in_condition() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100).model();
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(qry).model());
        assertEquals("Incorrect count", 1, vehicles.size());
        final TgVehicle vehicle = vehicles.get(0);
        assertEquals("Incorrect key", "CAR2", vehicle.getKey());
    }

    @Test
    public void test_calculated_entity_prop_in_fetching() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100).model();
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(qry).with(fetch(TgVehicle.class).with("lastFuelUsage", fetchAll(TgFuelUsage.class))).model());
        assertEquals("Incorrect count", 1, vehicles.size());
        final TgVehicle vehicle = vehicles.get(0);
        assertEquals("Incorrect key", "CAR2", vehicle.getKey());
        assertEquals("Incorrect key", "P", vehicle.getLastFuelUsage().getFuelType().getKey());
    }

    @Test
    public void test_calculated_entity_props_in_condition_() {
        final AggregatedResultQueryModel qry = select(TgVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100).yield().countAll().as("aa").modelAsAggregate();
        final List<EntityAggregates> vehicles = aggregateDao.getAllEntities(from(qry).model());
        assertEquals("Incorrect count", 1, vehicles.size());
        //	final TgVehicle vehicle = vehicles.get(0);
        //	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
    }

    @Test
    public void test_calculated_entity_props_in_condition2() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).leftJoin(TgFuelUsage.class).as("lastFuelUsage").on().prop("lastFuelUsage").eq().prop("lastFuelUsage.id").where().prop("lastFuelUsage.qty").gt().val(100).model();
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(qry).model());
        assertEquals("Incorrect count", 1, vehicles.size());
        final TgVehicle vehicle = vehicles.get(0);
        assertEquals("Incorrect key", "CAR2", vehicle.getKey());

    }

    @Test
    public void test_calculated_entity_props_in_condition2a() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).leftJoin(TgFuelUsage.class).as("lastFuelUsage").on().condition(cond().prop("lastFuelUsage").eq().prop("lastFuelUsage.id").model()).where().condition(cond().prop("lastFuelUsage.qty").gt().val(100).model()).model();
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(qry).model());
        assertEquals("Incorrect count", 1, vehicles.size());
        final TgVehicle vehicle = vehicles.get(0);
        assertEquals("Incorrect key", "CAR2", vehicle.getKey());

    }

    @Test
    @Ignore
    public void test_query_for_complex_calc_prop() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).
                where().prop("calcMake.key").eq().val("MERC").
                model();
        final TgVehicle vehicle1 = coVehicle.getEntity(from(qry).with(fetchOnly(TgVehicle.class). //
        with("key"). //
        with("desc"). //
        with("model", fetchOnly(TgVehicleModel.class).with("key").with("make", fetchOnly(TgVehicleMake.class).with("key")))).model());
        assertEquals("316", vehicle1.getModel().getKey());
    }

    @Test
    public void test_query_for_complex_calc_prop2() {
        final EntityResultQueryModel<TgVehicle> qry = select(select(TgVehicle.class).where().prop("key").notLike().val("A%").model()). //
        join(TgVehicleModel.class).as("model").on().prop("model").eq().prop("model.id"). //
        join(TgVehicleMake.class).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
        where().prop("model.make.key").eq().val("MERC").model();
        final TgVehicle vehicle1 = coVehicle.getEntity(from(qry).with(fetchOnly(TgVehicle.class). //
        with("key"). //
        with("desc"). //
        with("model", fetchOnly(TgVehicleModel.class).with("key").with("make", fetchOnly(TgVehicleMake.class).with("key")))).model());
        assertEquals("316", vehicle1.getModel().getKey());
    }

    @Test
    public void test0_0() {
        final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("lastFuelUsageQty").as("lq").modelAsAggregate();
        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
        final EntityAggregates item = models.get(0);
        assertEquals("Incorrect key", new BigDecimal("120.00"), item.get("lq"));
    }

    @Test
    public void test0_0a() {
        final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("lastFuelUsageQty").eq().val(120).model();
        final AggregatedResultQueryModel model = select(vehSubqry).where().prop("key").eq().val("CAR2").yield().prop("lastFuelUsageQty").as("lq").modelAsAggregate();
        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
        final EntityAggregates item = models.get(0);
        assertEquals("Incorrect key", new BigDecimal("120.00"), item.get("lq"));
    }

    @Test
    public void test0_1() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("sumOfPrices.amount").ge().val("100").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(model).with(fetch(TgVehicle.class).with("sumOfPrices").with("constValueProp").with("calc0")).model());
        final TgVehicle vehicle = models.get(0);
        assertEquals("Incorrect key", "CAR2", vehicle.getKey());
        assertTrue("Values of props sumOfPrices [" + vehicle.getSumOfPrices().getAmount() + "] and calc0 [" + vehicle.getCalc0() + "] should be equal", vehicle.getSumOfPrices().getAmount().compareTo(vehicle.getCalc0()) == 0);
        assertEquals("Incorrect key", new Integer(30), vehicle.getConstValueProp());
    }

    @Test
    public void test0_2() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc2").ge().val("100").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(model).model());
        assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test0_4() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc3").ge().val("100").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(model).model());
        assertEquals("Incorrect key", 1, models.size());
    }

    @Test
    public void test0_6() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc4").ge().val("100").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(model).model());
        assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test0_7() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc5").ge().val("100").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(model).model());
        final TgVehicle vehicle = models.get(0);
        assertEquals("Incorrect key", "CAR2", vehicle.getKey());
    }

    @Test
    public void test0_8() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc6").ge().val("100").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(model).model());
        assertEquals("Incorrect key", 0, models.size());
    }

    ////////////////////////////////////////////////////////////////   FUNCTIONS  ////////////////////////////////////////////////////////////

    @Test
    public void test_query_with_concat_function() {
        final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().concat().prop("wagon.key").with().val("-").with().prop("wagon.desc").end().eq().val("WAGON2-Wagon 2").model();
        final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).model());
        assertEquals("Incorrect key", 3, models.size());
    }

    @Test
    public void test_query_with_concat_function2() {
        final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().concat().ifNull().prop("wagon.key").then().val("NULL").with().val("-").with().prop("wagon.desc").end().eq().val("WAGON2-Wagon 2").model();
        final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).model());
        assertEquals("Incorrect key", 3, models.size());
    }

    @Test
    public void test_query_with_concat_function_with_non_string_argument() {
        final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().concat().prop("wagon.key").with().val(2).end().eq().val("WAGON22").model();
        final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).model());
        assertEquals("Incorrect key", 3, models.size());
    }

    ////////////////////////////////////////////////////////////////   OTHERS  ////////////////////////////////////////////////////////////

    @Test
    public void test_condition_on_121_property() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(qry).with(fetch(TgVehicle.class)).model());
        assertEquals("Incorrect key", 0, models.size());
    }

    @Test
    public void test_fetching_of_121_property() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(qry).with(fetch(TgVehicle.class).with("finDetails")).model());
        assertEquals("Incorrect key", "CAP_NO1", models.get(0).getFinDetails().getCapitalWorksNo());
    }

    @Test
    public void test_validation_of_duplicate_yields() {
        final AggregatedResultQueryModel model = select(TgVehicle.class). //
        yield().prop("id").as("id"). //
        yield().prop("key").as("key"). //
        yield().prop("version").as("id"). //
        modelAsAggregate();
        try {
            aggregateDao.getAllEntities(from(model).model());
            fail("Should have failed while trying to yield duplicates");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_retrieval_of_non_persisted_prop_from_type() {
        final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("npProp").eq().val("val").model();
        try {
            vehicleMakeDao.getAllEntities(from(qry).model());
            fail("Should have failed while trying to resolve property [npProp]");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_retrieval_of_non_persisted_prop_from_model() {
        final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class). //
        yield().prop("id").as("id"). //
        yield().prop("version").as("version"). //
        yield().prop("key").as("key"). //
        yield().prop("desc").as("desc"). //
        yield().val("val").as("npProp"). //
        modelAsEntity(TgVehicleMake.class);
        assertEquals("Incorrect key", 4, vehicleMakeDao.getAllEntities(from(qry).model()).size());
    }

    @Test
    @Ignore("Support for yielding values as ordinary properties for non-synthetic entities is no longer available.")
    public void test_retrieval_of_non_persisted_entity_prop_from_model() {
        final EntityResultQueryModel<TgVehicleMake> makeQry = select(TgVehicleMake.class). //
        where().prop("key").eq().val("MERC"). //
        yield().prop("id").as("id"). //
        yield().prop("version").as("version"). //
        yield().prop("key").as("key"). //
        yield().prop("desc").as("desc"). //
        yield().beginExpr().val(vehicleMakeDao.findByKey("BMW")).add().val(1).sub().val(1).endExpr().as("competitor"). //
        modelAsEntity(TgVehicleMake.class);
        final EntityResultQueryModel<TgVehicleMake> qry = select(makeQry). //
        where().prop("competitor.key").eq().val("BMW").model();
        final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(qry).with(fetchAll(TgVehicleMake.class).with("competitor")).model());
        assertEquals("Incorrect size", 1, models.size());
        assertEquals("Incorrect key", "BMW", models.get(0).getCompetitor().getKey());
    }

    @Test
    public void test_fetch_with_sorted_collection() {
        final EntityResultQueryModel<TgWagon> qry = select(TgWagon.class).where().prop("key").eq().val("WAGON1").model();
        // final List<TgWagon> models = wagonDao.getAllEntities(from(qry).with(fetch(TgWagon.class).with("slots", fetch(TgWagonSlot.class).with("bogie", fetchAll(TgBogie.class)))).model());
        final List<TgWagon> models = wagonDao.getAllEntities(from(qry).with(fetch(TgWagon.class).with("slots", fetch(TgWagonSlot.class).with("bogie"))).model());
        assertEquals("Incorrect key", 1, models.size());
        assertEquals("Incorrect key", 8, models.get(0).getSlots().size());
        assertEquals("Incorrect slot position", new Integer("1"), models.get(0).getSlots().iterator().next().getPosition());
        assertNotNull("Bogie should be present", models.get(0).getSlots().iterator().next().getBogie());
        assertEquals("Incorrect key", "BOGIE4", models.get(0).getSlots().iterator().next().getBogie().getKey());
    }

    @Test
    public void test_sql_injection() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("desc").eq().val("x'; DROP TABLE members; --").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(qry).with(fetch(TgVehicle.class)).model());
        assertEquals("Incorrect key", 0, models.size());
    }

    @Test
    public void test_yielding_const_value() {
        final AggregatedResultQueryModel makeModel = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").yield().prop("key").as("key").yield().val("MERC").as("konst").modelAsAggregate();
        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(makeModel).model());
        assertEquals("Incorrect key", 1, models.size());
    }

    @Test
    public void test_nested_uncorrelated_subqueries() {
        final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("model").eq().extProp("id").model();
        final EntityResultQueryModel<TgVehicleModel> vehModelSubqry = select(TgVehicleModel.class).where().prop("key").eq().val("316").and().exists(vehSubqry).model();
        final EntityResultQueryModel<TgVehicleMake> makeModel = select(TgVehicleMake.class).where().exists(vehModelSubqry).model();
        final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(makeModel).model());
        assertEquals("Incorrect key", 4, models.size());
    }

    @Test
    public void test_nested_subqueries_with_ext_props() {
        final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("model").eq().extProp("id").model();
        final EntityResultQueryModel<TgVehicleModel> vehModelSubqry = select(TgVehicleModel.class).where().prop("make").eq().extProp("id").and().exists(vehSubqry).model();
        final EntityResultQueryModel<TgVehicleMake> makeModel = select(TgVehicleMake.class).where().exists(vehModelSubqry).model();
        final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(makeModel).model());
        assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_111() {
        final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("MERC", "AUDI").yield().val(1).as("1").modelAsAggregate();
        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
        assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_112() {
        final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("model.make.key").eq().val("MERC").or().prop("model.make.key").eq().val("AUDI").yield().val(1).as("1").modelAsAggregate();
        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
        assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_113() {
        final AggregatedResultQueryModel model = select(TgVehicle.class).where().begin().prop("model.make.key").eq().val("MERC").or().prop("model.make.key").eq().val("AUDI").end().yield().val(1).as("1").modelAsAggregate();
        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
        assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test0_3() {
        final EntityResultQueryModel<TgFuelUsage> model = select(TgFuelUsage.class).where().prop("vehicle.sumOfPrices").ge().val("100").model();
        final List<TgFuelUsage> models = fuelUsageDao.getAllEntities(from(model).with(fetch(TgFuelUsage.class)).model());
        final TgFuelUsage fuelUsage = models.get(0);
        assertEquals("Incorrect key", "CAR2", fuelUsage.getVehicle().getKey());
    }

    @Test
    public void test0_5() {
        final EntityResultQueryModel<TgFuelUsage> model = select(TgFuelUsage.class).where().prop("vehicle.calc3").ge().val("100").model();
        final List<TgFuelUsage> models = fuelUsageDao.getAllEntities(from(model).with(fetch(TgFuelUsage.class)).model());
        final TgFuelUsage fuelUsage = models.get(0);
        assertEquals("Incorrect key", "CAR2", fuelUsage.getVehicle().getKey());
    }

    @Test
    public void vehicle_make_should_be_fetched_1() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();

        final TgVehicleModel vehModel = vehicleModelDao.getEntity(from(model).with(fetch(TgVehicleModel.class).with("make")).model());

        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    ///////////////////////////////////////////////////// EXPERIMENTING WITH RE-YIELDING (FULL/PARTIAL) OF PERSISTED ENTITY TYPES ////////////////////////////////////////////////

    @Test
    public void reyielded_vehicle_make_should_be_fetched() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316"). //
        yield().prop("id").as("id"). //
        yield().prop("version").as("version"). //
        yield().prop("key").as("key"). //
        yield().prop("desc").as("desc"). //
        yield().prop("make").as("make"). //
        yield().prop("make.id").as("make.id"). //
        yield().prop("make.version").as("make.version"). //
        yield().prop("make.key").as("make.key"). //
        yield().prop("make.desc").as("make.desc"). //
        modelAsEntity(TgVehicleModel.class);

        final TgVehicleModel vehModel = vehicleModelDao.getEntity(from(model).with(fetch(TgVehicleModel.class).with("make")).model());

        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void reyielded_vehicle_model_should_work() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).//
        yield().prop("id").as("id"). //
        yield().prop("version").as("version"). //
        yield().prop("key").as("key"). //
        yield().prop("desc").as("desc"). //
        modelAsEntity(TgVehicleModel.class);

        final EntityResultQueryModel<TgVehicleModel> model2 = select(model).where().prop("key").eq().val("316").model();

        final TgVehicleModel vehModel = vehicleModelDao.getEntity(from(model2).with(fetchAll(TgVehicleModel.class)).model());

        assertEquals("Incorrect key", "316", vehModel.getKey());
    }

    @Test
    public void reyielded_vehicle_model_with_make_as_entity_aggregates_should_work() {
        final AggregatedResultQueryModel model = select(TgVehicleModel.class).where().prop("key").eq().val("316"). //
        yield().prop("id").as("id"). //
        yield().prop("version").as("version"). //
        yield().prop("key").as("key"). //
        yield().prop("desc").as("desc"). //
        yield().prop("make.key").as("make.key"). //
        yield().prop("make.desc").as("make.desc"). //
        modelAsAggregate();

        final EntityAggregates vehModel = aggregateDao.getEntity(from(model).model());
        assertEquals("Incorrect key", "316", vehModel.get("key"));
        assertEquals("Incorrect key", "MERC", ((EntityAggregates) vehModel.get("make")).get("key"));
    }

    @Test
    @Ignore
    public void test2_() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316"). //
        yield().prop("id").as("id"). //
        yield().prop("version").as("version"). //
        yield().prop("key").as("key"). //
        yield().prop("desc").as("desc"). //
        modelAsEntity(TgVehicleModel.class);

        final TgVehicleModel vehModel = vehicleModelDao.getEntity(from(model).with(fetchAll(TgVehicleModel.class)).model());
        assertEquals("Incorrect key", "316", vehModel.getKey());
        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    @Ignore
    public void test_partial_fetching() {
        final EntityResultQueryModel<TgVehicle> model = select(select(TgVehicle.class).where().prop("key").eq().val("CAR1").model()). //
        yield().prop("id").as("id"). //
        yield().prop("version").as("version"). //
        yield().prop("key").as("key"). //
        yield().prop("desc").as("desc"). //
        yield().prop("model").as("model"). //
        yield().prop("model.id").as("model.id"). //
        yield().prop("model.version").as("model.version"). //
        yield().prop("model.key").as("model.key"). //
        yield().prop("model.desc").as("model.desc"). //
        yield().prop("model.make").as("model.make"). //
        modelAsEntity(TgVehicle.class);

        final TgVehicle vehModel = coVehicle.getEntity(from(model).with(fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make"))).model());
        assertEquals("Incorrect key", "318", vehModel.getModel().getKey());
        assertEquals("Incorrect key", "AUDI", vehModel.getModel().getMake().getKey());
    }

    @Test
    public void test3() {
        final EntityResultQueryModel<TgVehicleModel> model = select(select(TgVehicleModel.class).where().prop("make.key").eq().val("MERC").model()). //
        yield().prop("id").as("id"). //
        yield().prop("version").as("version"). //
        yield().prop("key").as("key").yield(). //
        prop("desc").as("desc"). //
        yield().prop("make").as("make"). //
        yield().prop("make.id").as("make.id"). //
        yield().prop("make.version").as("make.version"). //
        yield().prop("make.key").as("make.key"). //
        yield().prop("make.desc").as("make.desc"). //
        modelAsEntity(TgVehicleModel.class);

        final TgVehicleModel vehModel = vehicleModelDao.getEntity(from(model).with(fetch(TgVehicleModel.class).with("make")).model());

        assertEquals("Incorrect key", "316", vehModel.getKey());
        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test4() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make").eq().model(select(TgVehicleMake.class).where().prop("key").eq().val("MERC").yield().prop("id").modelAsPrimitive()). //
        modelAsEntity(TgVehicleModel.class);
        final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(fetch(TgVehicleModel.class).with("make")).model());
        final TgVehicleModel vehModel = models.get(0);
        assertEquals("Incorrect key", "316", vehModel.getKey());
        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test5() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make.key").eq().val("MERC"). //
        modelAsEntity(TgVehicleModel.class);
        final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(fetch(TgVehicleModel.class).with("make")).model());
        final TgVehicleModel vehModel = models.get(0);
        assertEquals("Incorrect key", "316", vehModel.getKey());
        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test6() {
        final EntityResultQueryModel<TgVehicleMake> model = select(TgVehicleMake.class).where().prop("key").in().params("param1", "param2").model();
        final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(model).with("param1", "MERC").with("param2", "BMW").model());
        assertEquals("Incorrect count", 2, models.size());
    }

    @Test
    public void test_now() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("initDate").lt().now().model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 2, models.size());
    }

    @Test
    @Ignore
    public void test_with_empty_values() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("key").in().values().model();
        final List<TgVehicle> values = coVehicle.getAllEntities(from(model).model());
        // TODO should get exception here
        assertEquals("Incorrect count", 2, values.size());
    }

    @Test
    public void test7() {
        final AggregatedResultQueryModel model = select(TgVehicle.class).as("v").where().prop("v.key").in().values("CAR1", "CAR2").and().prop("v.price.amount").ge().val(100). //
        yield().prop("v.price.amount").as("pa").yield().prop("v.lastMeterReading").as("lmr").modelAsAggregate();
        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 1, values.size());
        //    	assertEquals("Incorrect value", new BigDecimal("105.75"), values.get(0).get("lmr"));
        //    	assertEquals("Incorrect value", new BigDecimal("200.00"), values.get(0).get("pa"));
    }

    @Test
    public void test8() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("price.amount").ge().val(100).model();
        final List<TgVehicle> values = coVehicle.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect count", new Money("100.00"), values.get(0).getPurchasePrice());
    }

    @Test
    public void test8a() {
        final EntityResultQueryModel<EntityWithMoney> model = select(EntityWithMoney.class).where().prop("money").isNotNull().model();
        final List<EntityWithMoney> values = entityWithMoneyDao.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 0, values.size());
    }

    @Test
    public void test9() {
        final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
        avgOf().beginExpr().prop("price.amount").add().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "165.0", values.get(0).get("aa").toString());
    }

    @Test
    public void test_all_quantified_condition() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().val(100).lt().all(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").yield().prop("qty").modelAsPrimitive()).model();
        final List<TgVehicle> values = coVehicle.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 1, values.size());
    }

    @Test
    public void test_any_quantified_condition() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().val(100).lt().any(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").yield().prop("qty").modelAsPrimitive()).model();
        final List<TgVehicle> values = coVehicle.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 1, values.size());
    }

    @Test
    public void test10() {
        final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
        beginExpr().avgOf().prop("price.amount").add().avgOf().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "165.0", values.get(0).get("aa").toString());
    }

    @Test
    public void test11() {
        final AggregatedResultQueryModel model = select(TgVehicleModel.class).yield().countOfDistinct().prop("make").as("aa").modelAsAggregate();
        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "3", values.get(0).get("aa").toString());
    }
    
    @Test
    public void test12() {
        final AggregatedResultQueryModel model = select(TgVehicleModel.class). //
        yield().countAll().as("aa"). //
        yield().countOfDistinct().prop("make").as("bb"). //
        yield().now().as("cc"). //
        modelAsAggregate();
        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "7", values.get(0).get("aa").toString());
        assertEquals("Incorrect value", "3", values.get(0).get("bb").toString());
    }

    @Test
    public void test13() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").model();
        final AggregatedResultQueryModel countModel = select(model). //
        yield().countAll().as("aa"). //
        modelAsAggregate();

        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test13a() {
        final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("model.make.key").eq().val("MERC").yield().prop("id").as("aa").modelAsAggregate();
        final AggregatedResultQueryModel countModel = select(model). //
        yield().countAll().as("aa"). //
        modelAsAggregate();

        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test14() {
        final PrimitiveResultQueryModel subQry = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").yield().prop("key").modelAsPrimitive();
        final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("key").in().model(subQry). //
        yield().countAll().as("aa"). //
        modelAsAggregate();

        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test15_() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("model.make").eq().prop("make").model();

        try {
            coVehicle.getAllEntities(from(qry).model());
            fail("Prop make should not be resolved and lead to exception");
        } catch (final RuntimeException e) {
        }

    }

    @Test
    public void test15() {
        final PrimitiveResultQueryModel subQry = select(TgVehicle.class).where().prop("model.make").eq().prop("make.id")/*extProp("id")*/.yield().countAll().modelAsPrimitive();
        final AggregatedResultQueryModel countModel = select(TgVehicleMake.class).as("make").yield().prop("key").as("make"). //
        yield().model(subQry).as("vehicleCount"). //
        modelAsAggregate();

        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
        assertEquals("Incorrect count", 4, values.size());
        for (final EntityAggregates result : values) {
            if (result.get("make").equals("MERC") || result.get("make").equals("AUDI")) {
                assertEquals("Incorrect value for make " + result.get("make"), "1", result.get("vehicleCount").toString());
            } else {
                assertEquals("Incorrect value", "0", result.get("vehicleCount").toString());
            }
        }
    }

    @Test
    public void test16() {
        final PrimitiveResultQueryModel makeQry = select(TgVehicleMake.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").yield().prop("key").modelAsPrimitive();
        final PrimitiveResultQueryModel modelQry = select(TgVehicleModel.class).where().prop("make.key").in().model(makeQry).yield().prop("key").modelAsPrimitive();
        final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("model.key").in().model(modelQry). //
        yield().countAll().as("aa"). //
        modelAsAggregate();

        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test17() {
        final PrimitiveResultQueryModel makeQry = select(TgVehicleMake.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").and().prop("key").eq().prop("model.make.key").yield().prop("key").modelAsPrimitive();
        final PrimitiveResultQueryModel modelQry = select(TgVehicleModel.class).where().prop("make.key").in().model(makeQry).and().prop("key").eq().param("model_param").yield().prop("key").modelAsPrimitive();
        final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("model.key").in().model(modelQry). //
        yield().countAll().as("aa"). //
        modelAsAggregate();

        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).with("model_param", "316").model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test18() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
        final OrderingModel ordering = orderBy().prop("model.make.key").desc().model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(qry).with(ordering).model());
        final TgVehicle veh = models.get(0);
        assertEquals("Incorrect key", "CAR2", veh.getKey());
    }

    @Test
    public void test19() {
        final AggregatedResultQueryModel model = select(TgVehicle.class). //
        where(). //
        prop("model.make.key").eq().val("MERC"). //
        and(). //
        prop("active").eq().val(false). //
        and(). //
        prop("leased").eq().val(true). //
        yield().lowerCase().prop("model.make.key").as("make"). //
        yield().ifNull().prop("replacedBy").then().val(1).as("not-replaced-yet"). //
        yield().ifNull().prop("model.make.key").then().val("unknown").as("make-key"). //
        yield().count().days().between().now().and().now().as("zero-days"). //
        yield().count().months().between().now().and().now().as("zero-months"). //
        yield().count().years().between().now().and().now().as("zero-years"). //
        yield().caseWhen().prop("price.amount").ge().prop("purchasePrice.amount").then().beginExpr().beginExpr().prop("price.amount").add().prop("purchasePrice.amount").endExpr().div().val(2).endExpr().end().as("avgPrice"). //
        yield().round().beginExpr().prop("price.amount").div().val(3).endExpr().to(1).as("third-of-price"). //
        modelAsAggregate();

        final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
        assertEquals("Incorrect count", 1, values.size());
        assertEquals("Incorrect value", "merc", values.get(0).get("make"));
        assertNotSame("Incorrect value", "1", values.get(0).get("not-replaced-yet").toString());
        assertEquals("Incorrect value", "MERC", values.get(0).get("make-key"));
        assertEquals("Incorrect value", "0", values.get(0).get("zero-days").toString());
        assertEquals("Incorrect value", "0", values.get(0).get("zero-months").toString());
        assertEquals("Incorrect value", "0", values.get(0).get("zero-years").toString());
        assertEquals(BigDecimal.class, values.get(0).get("avgPrice").getClass());
        assertEquals("Incorrect value", 0, ((BigDecimal) values.get(0).get("avgPrice")).compareTo(new BigDecimal("150")));
        assertEquals("Incorrect value", "66.7", values.get(0).get("third-of-price").toString());
    }

    @Test
    public void test20() {
        final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").model();
        final List<TgVehicleMake> makes = vehicleMakeDao.getAllEntities(from(qry).model());
        final TgVehicleMake make = makes.get(0);

        final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().val(make).model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(qry2).model());
        assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test21() {
        final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").model();
        final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().model(qry).model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(qry2).model());
        assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test22() {
        final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("model.make").modelAsEntity(TgVehicleMake.class);
        final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().model(qry).model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(qry2).model());
        assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test22a() {
        final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("model.make").modelAsEntity(TgVehicleMake.class);
        final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(qry).with(fetch(TgVehicleMake.class)).model());
        assertEquals("Incorrect key", "MERC", models.get(0).getKey());
    }

    @Test
    public void test23() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("model.make.key").iLike().val("me%").and().prop("key").iLike().val("%2").model();
        final List<TgVehicle> models = coVehicle.getAllEntities(from(qry).model());
        assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test24() {
        final EntityResultQueryModel<UserAndRoleAssociation> model = select(UserAndRoleAssociation.class).where().prop("user.key").eq().val("user1").and().prop("userRole.key").eq().val("MANAGER").model();
        final List<UserAndRoleAssociation> entities = userAndRoleAssociationDao.getAllEntities(from(model).with(fetch(UserAndRoleAssociation.class)).model());
        assertEquals("Incorrect count", 1, entities.size());
        assertEquals("Incorrect user", "user1", entities.get(0).getUser().getKey());
    }

    @Test
    public void test_vehicle_fetching() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final fetch<TgVehicle> fetchModel = fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make"));
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(qry).with(fetchModel).model());
        final TgVehicle vehicle = vehicles.get(0);
        assertEquals("Incorrect key", "CAR2", vehicle.getKey());
        assertEquals("Incorrect key", "316", vehicle.getModel().getKey());
        assertEquals("Incorrect key", "MERC", vehicle.getModel().getMake().getKey());
    }

    @Test
    public void test_vehicle_with_collection_fetching() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final fetch<TgVehicle> fetchModel = fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make")).with("fuelUsages", fetch(TgFuelUsage.class));
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(qry).with(fetchModel).model());
        final TgVehicle vehicle = vehicles.get(0);
        assertEquals("Incorrect key", "CAR2", vehicle.getKey());
        assertEquals("Incorrect key", "316", vehicle.getModel().getKey());
        assertEquals("Incorrect key", "MERC", vehicle.getModel().getMake().getKey());
        assertEquals("Incorrect number of fuel-usages", 2, vehicle.getFuelUsages().size());
    }
    
    @Test
    public void vehicle_is_fetched_with_persisted_collectional_association() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final fetch<TgVehicle> fetchModel = fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make")).with("vehicleFuelUsages", fetchAll(TgVehicleFuelUsage.class));
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(qry).with(fetchModel).model());
        final TgVehicle vehicle = vehicles.get(0);
        assertEquals("Incorrect number of vehicle-fuel-usages", 2, vehicle.getVehicleFuelUsages().size());
    }

    @Test
    public void vehicle_is_fetched_with_synthetic_parameterised_collectional_association() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
        final fetch<TgVehicle> fetchModel = fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make")).with("vehicleFuelUsages", fetchAll(TgVehicleFuelUsage.class));
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(qry).with(fetchModel)
                .with("datePeriod.from", new DateTime(2008, 01, 01, 0, 0).toDate()) //
                .with("datePeriod.to", new DateTime(2010, 01, 01, 0, 0).toDate()) //
                .model());
        final TgVehicle vehicle = vehicles.get(0);
        assertEquals("Incorrect number of vehicle-fuel-usages", 1, vehicle.getVehicleFuelUsages().size());
    }

    @Test
    public void test_aggregates_fetching() {
        final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("model").as("model").modelAsAggregate();
        final fetch<EntityAggregates> fetchModel = fetchAggregates().with("model", fetch(TgVehicleModel.class).with("make"));
        final EntityAggregates value = aggregateDao.getAllEntities(from(model).with(fetchModel).model()).get(0);
        assertEquals("Incorrect key", "316", ((TgVehicleModel) value.get("model")).getKey());
        assertEquals("Incorrect key", "MERC", ((TgVehicleModel) value.get("model")).getMake().getKey());
    }

    @Test
    public void test_aggregates_fetching_with_nullable_props() {
        final AggregatedResultQueryModel model = select(TgFuelUsage.class).yield().prop("vehicle.station").as("station").yield().sumOf().prop("qty").as("totalQty").modelAsAggregate();
        final fetch<EntityAggregates> fetchModel = fetchAggregates().with("station", fetch(TgOrgUnit5.class));
        final EntityAggregates value = aggregateDao.getAllEntities(from(model).with(fetchModel).model()).get(0);
        assertEquals("Incorrect key", "orgunit1 orgunit2 orgunit3 orgunit4 orgunit5", ((TgOrgUnit5) value.get("station")).getKey().toString());
    }

    @Test
    public void test_parameter_setting() {
        final EntityResultQueryModel<TgVehicleMake> queryModel = select(TgVehicleMake.class).where().prop("key").eq().param("makeParam").model();

        assertEquals("Mercedes", vehicleMakeDao.getEntity(from(queryModel).with("makeParam", "MERC").model()).getDesc());
        assertEquals("Audi", vehicleMakeDao.getEntity(from(queryModel).with("makeParam", "AUDI").model()).getDesc());

        //	try {
        //	    vehicleMakeDao.getAllEntities(from(queryModel).with("wrongParam", "AUDI").model());
        //	    fail("Setting param value with wrong param name should not lead to exception");
        //	} catch (final RuntimeException e) {
        //	}
    }

    @Test
    public void test_that_can_query_with_list_params() {
        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param1", "param2", "param3").model();
        assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param1", "316").with("param2", "317").with("param3", "318").model()).size());
    }

    @Test
    public void test_that_can_query_with_primitive_boolean_params() {
        final EntityResultQueryModel<TgVehicle> queryModel = select(TgVehicle.class).where().prop("active").eq().param("param").model();
        assertEquals("Incorrect key.", 1, coVehicle.getAllEntities(from(queryModel).with("param", true).model()).size());
    }

    @Test
    public void test_that_can_query_with_boolean_params() {
        final EntityResultQueryModel<TgVehicle> queryModel = select(TgVehicle.class).where().prop("active").eq().param("param").model();
        assertEquals("Incorrect key.", 1, coVehicle.getAllEntities(from(queryModel).with("param", Boolean.TRUE).model()).size());
    }

    @Test
    public void test_that_can_query_with_entity_params() {
        final TgVehicleModel m316 = vehicleModelDao.findByKey("316");
        final EntityResultQueryModel<TgVehicle> queryModel = select(TgVehicle.class).where().prop("model").eq().param("param").model();
        assertEquals("Incorrect key.", 1, coVehicle.getAllEntities(from(queryModel).with("param", m316).model()).size());
    }

    @Test
    public void test_that_query_count_model_works() {
        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().values("316", "317", "318").model();
        assertEquals("Incorrect number of veh models.", 3, vehicleModelDao.count(queryModel));
    }

    @Test
    public void test_that_can_query_with_arrays() {
        final String[] modelKeys = new String[] { "316", "317", "318", "318" };
        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().values(modelKeys).model();
        assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).model()).size());
    }

    @Test
    public void test_that_can_query_with_array_param() {
        final String[] modelKeys = new String[] { "316", "317", "318", "318" };
        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param").model();
        assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_that_can_query_with_nested_list_param() {
        final List<Object> modelKeysLevel1 = new ArrayList<Object>() {
            {
                add("316");
                add("317");
                add("318");
                add("318");
            }
        };
        final List<Object> modelKeys = new ArrayList<Object>() {
            {
                add("316");
                add("317");
                add("318");
                add("318");
                add(modelKeysLevel1);
            }
        };

        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param").model();
        assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_that_can_query_with_deeply_nested_list_param() {
        final Set<Object> modelKeysLevel2 = new HashSet<Object>() {
            {
                add("316");
                add("317");
                add("318");
                add("318");
            }
        };
        final List<Object> modelKeysLevel1 = new ArrayList<Object>() {
            {
                add("316");
                add("317");
                add("318");
                add("318");
                add(modelKeysLevel2);
            }
        };
        final List<Object> modelKeys = new ArrayList<Object>() {
            {
                add("316");
                add("317");
                add("318");
                add("318");
                add(modelKeysLevel1);
            }
        };

        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param").model();
        assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_that_can_query_with_list_param() {
        final List<String> modelKeys = new ArrayList<String>() {
            {
                add("316");
                add("317");
                add("318");
                add("318");
            }
        };
        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param").model();
        assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_that_can_query_with_list_param_in_anyOfParams() {
        final List<String> modelKeys = new ArrayList<String>() {
            {
                add("316");
                add("317");
                add("318");
                add("318");
            }
        };
        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").eq().anyOfParams("param").model();
        assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_that_can_query_with_list_param_in_anyOfIParams() {
        final List<String> modelKeys = new ArrayList<String>() {
            {
                add("316");
                add("317");
                add("318");
                add(null);
            }
        };
        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").eq().anyOfIParams("param").model();
        assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_that_can_query_with_list_param_in_anyOfIParams2() {
        final List<String> modelKeys = new ArrayList<String>() {
            {
                add(null);
                add(null);
            }
        };
        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").eq().anyOfIParams("param").model();
        assertEquals("Incorrect number of retrieved veh models.", 7, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_that_can_query_with_set_param() {
        final Set<String> modelKeys = new HashSet<String>() {
            {
                add("316");
                add("317");
                add("318");
                add("318");
            }
        };
        final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param").model();
        assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_mutiple_queries_as_query_source() {
        final EntityResultQueryModel<TgVehicleModel> sourceModel1 = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
        final EntityResultQueryModel<TgVehicleModel> sourceModel2 = select(TgVehicleModel.class).where().prop("key").eq().val("317").model();
        final EntityResultQueryModel<TgVehicleModel> model = select(sourceModel1, sourceModel2).where().prop("key").in().values("316", "317").model();
        final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(orderBy().prop("key").asc().model()).model());
        assertEquals("Incorrect key", "316", models.get(0).getKey());
        assertEquals("Incorrect key", "317", models.get(1).getKey());
    }

    @Test
    public void test_subqueries_in_yield_stmt() {
        final PrimitiveResultQueryModel avgPriceModel = select(TgVehicle.class).yield().avgOf().prop("price.amount").modelAsPrimitive();
        final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().beginExpr().model(avgPriceModel).add().prop("price.amount").endExpr().ge().val(10).model();
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(query).with(orderBy().prop("key").asc().model()).model());
        assertEquals("Incorrect key", "CAR1", vehicles.get(0).getKey());
        assertEquals("Incorrect key", "CAR2", vehicles.get(1).getKey());
    }

    @Test
    public void test_subqueries_in_yield_stmt2() {
        final PrimitiveResultQueryModel sumPriceModel = select(TgVehicle.class).yield().sumOf().prop("price.amount").modelAsPrimitive();
        final PrimitiveResultQueryModel avgPriceModel = select(TgVehicle.class).yield().beginExpr().avgOf().prop("price.amount").div().model(sumPriceModel).endExpr().modelAsPrimitive();
        final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().beginExpr().model(avgPriceModel).add().prop("price.amount").endExpr().ge().val(10).model();
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(query).with(orderBy().prop("key").asc().model()).model());
        assertEquals("Incorrect key", "CAR1", vehicles.get(0).getKey());
        assertEquals("Incorrect key", "CAR2", vehicles.get(1).getKey());
    }

    @Test
    public void test_subqueries_in_yield_stmt3() {
        final PrimitiveResultQueryModel sumPriceModel = select(TgVehicle.class).yield().sumOf().prop("price.amount").modelAsPrimitive();
        final PrimitiveResultQueryModel avgPriceModel = select(TgVehicle.class).yield().sumOf().model(sumPriceModel).modelAsPrimitive();
        final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().beginExpr().model(avgPriceModel).add().prop("price.amount").endExpr().ge().val(10).model();
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(query).with(orderBy().prop("key").asc().model()).model());
        assertEquals("Incorrect key", "CAR1", vehicles.get(0).getKey());
        assertEquals("Incorrect key", "CAR2", vehicles.get(1).getKey());
    }

    @Test
    @Ignore
    public void test_yielding_entity_itself() {
        final AggregatedResultQueryModel subquery = select(TgVehicle.class).yield().prop("id").as("vehicle").modelAsAggregate();
        final EntityResultQueryModel<TgVehicle> query = select(subquery).where().prop("vehicle.key").eq().val("CAR1").yield().prop("vehicle").modelAsEntity(TgVehicle.class);
        assertEquals("Incorrect key", "CAR1", coVehicle.getEntity(from(query).model()).getKey());
    }

    @Test
    public void test_negated_standalone_condition() {
        final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().negatedCondition(cond().prop("model.make.key").eq().val("MERC").model()).model();
        final TgVehicle vehicle = coVehicle.getEntity(from(query).model());
        assertEquals("Incorrect key", "CAR1", vehicle.getKey());
    }

    @Test
    public void check_for_correct_formula_for_121_prop() {
        final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("finDetails").isNull().model();
        final List<TgVehicle> vehicles = coVehicle.getAllEntities(from(model).model());
        assertEquals("Only 1 car without finDetails should be found", 1, vehicles.size());
        assertEquals("Incorrect car", "CAR2", vehicles.get(0).getKey());
    }

    ///////////////////////////////////////////// DEPRECATED FEATURE IN NEXT EQL //////////////////////////////////////////
    @Test
    public void test0_0c() {
        final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("lastFuelUsageQty").eq().val(120).//
        yield().prop("key").as("key"). //
        yield().prop("id").as("id"). //
        modelAsEntity(TgVehicle.class);
        final List<TgVehicle> models = coVehicle.getAllEntities(from(vehSubqry).model());
        final TgVehicle item = models.get(0);
        assertEquals("Incorrect key", "CAR2", item.getKey());
    }

    @Test
    public void test_query_for_correct_fetching_adjustment() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).
                where().prop("key").eq().val("CAR1").
                yield().prop("key").as("key").
                yield().prop("desc").as("desc").
                yield().prop("model").as("model").
                yield().prop("model.make").as("model.make").
                yield().prop("model.make.key").as("model.make.key").
                modelAsEntity(TgVehicle.class);
        final TgVehicle vehicle1 = coVehicle.getEntity(from(qry).with(fetch(TgVehicle.class).
                with("key").
                with("desc").
                with("model", fetchOnly(TgVehicleModel.class).
                        with("key").
                        with("make", fetchOnly(TgVehicleMake.class).
                                with("key")))).model());
        assertNotNull(vehicle1.getModel().getMake().getKey());
    }

    @Test
    public void test0_0b() {
        // FIXME
        final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("lastFuelUsageQty").eq().val(120).//
        yield().prop("key").as("key"). //
        yield().prop("id").as("id"). //
        modelAsEntity(TgVehicle.class);
        final AggregatedResultQueryModel model = select(vehSubqry).where().prop("key").eq().val("CAR2").yield().prop("lastFuelUsageQty").as("lq").modelAsAggregate();
        final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
        final EntityAggregates item = models.get(0);
        assertEquals("Incorrect key", new BigDecimal("120.00"), item.get("lq"));
    }
 
    ///////////////////////////////////////////////////// BATCH DELETION  ////////////////////////////////////////////////

    @Test
    public void test_batch_deletion() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").eq().val("Date").model();
        assertEquals(1, authorDao.batchDelete(qry));
    }
    
    @Test
    public void test_batch_deletion_of_whole_table() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).model();
        final int count = authorDao.count(qry);
        assertEquals(count, authorDao.batchDelete(qry));
    }
    
    @Test
    public void test_batch_deletion_by_entities() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).model();
        final int count = authorDao.count(qry);
        final List<TgAuthor> authors = authorDao.getAllEntities(from(qry).model());
        assertEquals(count, authorDao.batchDelete(authors));
    }
    
    @Test
    public void test_batch_deletion_by_entities_ids() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).model();
        final int count = authorDao.count(qry);
        final List<TgAuthor> authors = authorDao.getAllEntities(from(qry).model());
        final Set<Long> ids = new HashSet<>();
        for (final TgAuthor author : authors) {
            ids.add(author.getId());
        }
        assertEquals(count, authorDao.batchDelete(ids));
    }
    
    ///////////////////////////////////////////////////// CASE EXPRESSION IN SUMMARY PROPS  ////////////////////////////////////////////////
    @Test
    public void test_case_when_expression_in_summary_property_when_condition_is_satisfied() {
        final EntityResultQueryModel<TgEntityWithComplexSummaries> qry = select(TgEntityWithComplexSummaries.class).model();
        final TgEntityWithComplexSummaries summaryEntity = coEntityWithComplexSummaries.getEntity(from(qry).with(fetchOnly(TgEntityWithComplexSummaries.class).with("costPerKm").without("id").without("version")).model());
        assertEquals(new BigDecimal("1"), summaryEntity.getCostPerKm());
    }
    
    @Test
    public void test_case_when_expression_in_summary_property_when_condition_is_not_satisfied() {
        final EntityResultQueryModel<TgEntityWithComplexSummaries> qry = select(TgEntityWithComplexSummaries.class).where().prop("kms").eq().val(0).model();
        final TgEntityWithComplexSummaries summaryEntity = coEntityWithComplexSummaries.getEntity(from(qry).with(fetchOnly(TgEntityWithComplexSummaries.class).with("costPerKm").without("id").without("version")).model());
        assertNull(summaryEntity.getCostPerKm());
    }

    ///////////////////////////////////////////////////// ENTITY INSTRUMENTATION WHILE RETRIEVING WITH EQL ///////////////////////////////////////
    @Test
    public void vehicle_retrieved_with_default_query_is_instrumented() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(qry).model());
        assertTrue(isEntityInstrumented(vehicle));
    }
    
    @Test
    public void vehicle_retrieved_with_lightweight_query_is_not_instrumented() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final TgVehicle vehicle = coVehicle.getEntity(from(qry).lightweight().model());
        assertFalse(isEntityInstrumented(vehicle));
    }

    @Test
    public void vehicle_model_property_retrieved_with_default_fetch_is_not_instrumented() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final fetch<TgVehicle> fetch = fetch(TgVehicle.class).with("model");
        final TgVehicle vehicle = coVehicle.getEntity(from(qry).with(fetch).model());
        assertFalse(isEntityInstrumented(vehicle.getModel()));
    }

    @Test
    public void vehicle_model_property_retrieved_with_instrumented_fetch_is_instrumented() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final fetch<TgVehicle> fetch = fetch(TgVehicle.class).with("model", fetchAndInstrument(TgVehicleModel.class));
        final TgVehicle vehicle = coVehicle.getEntity(from(qry).with(fetch).model());
        assertTrue(isEntityInstrumented(vehicle.getModel()));
    }
    
    @Test
    public void vehicle_model_property_retrieved_with_instrumented_fetch_with_make_subproperty_is_instrumented() {
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
        final fetch<TgVehicle> fetch = fetch(TgVehicle.class).with("model", fetchAndInstrument(TgVehicleModel.class).with("make"));
        final TgVehicle vehicle = coVehicle.getEntity(from(qry).with(fetch).model());
        assertTrue(isEntityInstrumented(vehicle.getModel()));
    }

    @Test
    public void vehicle_property_retrieved_with_instrumented_fetch_and_being_part_of_fuel_usage_composite_key_is_instrumented() {
        final EntityResultQueryModel<TgFuelUsage> qry = select(TgFuelUsage.class).where().prop("vehicle.key").eq().val("CAR2").and().prop("fuelType.key").eq().val("P").model();
        final fetch<TgFuelUsage> fetch = fetch(TgFuelUsage.class).with("vehicle", fetchAndInstrument(TgVehicle.class));
        final TgFuelUsage fuelUsage = fuelUsageDao.getEntity(from(qry).with(fetch).model());
        assertTrue(isEntityInstrumented(fuelUsage.getVehicle()));
    }

    public static boolean isPropertyInstrumented(final AbstractEntity<?> entity, final String propName) {
        final Object value = entity.get(propName);
        return isEntityInstrumented(value);
    }
    
    public static boolean isEntityInstrumented(final Object entity) {
        return entity == null ? false : entity.getClass().getName().contains("$$Enhancer");
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgFuelType unleadedFuelType = save(new_(TgFuelType.class, "U", "Unleaded"));
        final TgFuelType petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

        final TgWorkshop workshop1 = save(new_(TgWorkshop.class, "WSHOP1", "Workshop 1"));
        final TgWorkshop workshop2 = save(new_(TgWorkshop.class, "WSHOP2", "Workshop 2"));

        final TgBogieLocation location = co$(TgBogieLocation.class).new_();
        location.setWorkshop(workshop1);
        final TgBogie bogie1 = save(new_(TgBogie.class, "BOGIE1", "Bogie 1").setLocation(location));
        final TgBogie bogie2 = save(new_(TgBogie.class, "BOGIE2", "Bogie 2"));
        final TgBogie bogie3 = save(new_(TgBogie.class, "BOGIE3", "Bogie 3"));
        final TgBogie bogie4 = save(new_(TgBogie.class, "BOGIE4", "Bogie 4"));
        final TgBogie bogie5 = save(new_(TgBogie.class, "BOGIE5", "Bogie 5"));
        final TgBogie bogie6 = save(new_(TgBogie.class, "BOGIE6", "Bogie 6"));
        final TgBogie bogie7 = save(new_(TgBogie.class, "BOGIE7", "Bogie 7"));
        
        final TgWagon wagon1 = save(new_(TgWagon.class, "WAGON1", "Wagon 1"));
        final TgWagon wagon2 = save(new_(TgWagon.class, "WAGON2", "Wagon 2"));

        save(new_composite(TgWagonSlot.class, wagon1, 5));
        save(new_composite(TgWagonSlot.class, wagon1, 6));
        save(new_composite(TgWagonSlot.class, wagon1, 7));
        save(new_composite(TgWagonSlot.class, wagon1, 8));
        save(new_composite(TgWagonSlot.class, wagon1, 4).setBogie(bogie1));
        save(new_composite(TgWagonSlot.class, wagon1, 3).setBogie(bogie2));
        save(new_composite(TgWagonSlot.class, wagon1, 2).setBogie(bogie3));
        save(new_composite(TgWagonSlot.class, wagon1, 1).setBogie(bogie4));

        save(new_composite(TgWagonSlot.class, wagon2, 1).setBogie(bogie5));
        save(new_composite(TgWagonSlot.class, wagon2, 2).setBogie(bogie6));
        
        final TgWagonSlot wagonSlot = save(new_composite(TgWagonSlot.class, wagon2, 3).setBogie(bogie7).setFuelType(petrolFuelType));
        final TgBogieLocation slotLocation = co$(TgBogieLocation.class).new_();
        slotLocation.setWagonSlot(wagonSlot);
        save(bogie7.setLocation(slotLocation));
        
        final TgOrgUnit1 orgUnit1 = save(new_(TgOrgUnit1.class, "orgunit1", "desc orgunit1"));
        final TgOrgUnit2 orgUnit2 = save(new_composite(TgOrgUnit2.class, orgUnit1, "orgunit2"));
        final TgOrgUnit3 orgUnit3 = save(new_composite(TgOrgUnit3.class, orgUnit2, "orgunit3"));
        final TgOrgUnit4 orgUnit4 = save(new_composite(TgOrgUnit4.class, orgUnit3, "orgunit4"));
        final TgOrgUnit5 orgUnit5 = save(new_composite(TgOrgUnit5.class, orgUnit4, "orgunit5"));

        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleMake audi = save(new_(TgVehicleMake.class, "AUDI", "Audi"));
        final TgVehicleMake bmw = save(new_(TgVehicleMake.class, "BMW", "BMW"));
        final TgVehicleMake subaro = save(new_(TgVehicleMake.class, "SUBARO", "Subaro"));

        final TgVehicleModel m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        final TgVehicleModel m317 = save(new_(TgVehicleModel.class, "317", "317").setMake(audi));
        final TgVehicleModel m318 = save(new_(TgVehicleModel.class, "318", "318").setMake(audi));
        final TgVehicleModel m319 = save(new_(TgVehicleModel.class, "319", "319").setMake(bmw));
        final TgVehicleModel m320 = save(new_(TgVehicleModel.class, "320", "320").setMake(bmw));
        final TgVehicleModel m321 = save(new_(TgVehicleModel.class, "321", "321").setMake(bmw));
        final TgVehicleModel m322 = save(new_(TgVehicleModel.class, "322", "322").setMake(bmw));

        final TgVehicle car1 = save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").setInitDate(date("2001-01-01 00:00:00")).setModel(m318).setPrice(new Money("20")).setPurchasePrice(new Money("10")).setActive(true).setLeased(false));
        final TgVehicle car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").setInitDate(date("2007-01-01 00:00:00")).setModel(m316).setPrice(new Money("200")).setPurchasePrice(new Money("100")).setActive(false).setLeased(true).setLastMeterReading(new BigDecimal("105")).setStation(orgUnit5).setReplacedBy(car1));

        save(new_(TgVehicleFinDetails.class, car1).setCapitalWorksNo("CAP_NO1"));

        save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleadedFuelType));
        save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(petrolFuelType));

        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));

        final UserRole managerRole = save(new_(UserRole.class, "MANAGER", "Managerial role"));
        final UserRole dataEntryRole = save(new_(UserRole.class, "DATAENTRY", "Data entry role"));
        final UserRole analyticRole = save(new_(UserRole.class, "ANALYTIC", "Analytic role"));
        final UserRole fleetOperatorRole = save(new_(UserRole.class, "FLEET_OPERATOR", "Fleet operator role"));
        final UserRole workshopOperatorRole = save(new_(UserRole.class, "WORKSHOP_OPERATOR", "Workshop operator role"));
        final UserRole warehouseOperatorRole = save(new_(UserRole.class, "WAREHOUSE_OPERATOR", "Warehouse operator role"));

        final User baseUser1 = save(new_(User.class, "base_user1", "base user1").setBase(true));
        final User user1 = save(new_(User.class, "user1", "user1 desc").setBase(false).setBasedOnUser(baseUser1));
        final User user2 = save(new_(User.class, "user2", "user2 desc").setBase(false).setBasedOnUser(baseUser1));
        final User user3 = save(new_(User.class, "user3", "user3 desc").setBase(false).setBasedOnUser(baseUser1));

        save(new_composite(UserAndRoleAssociation.class, user1, managerRole));
        save(new_composite(UserAndRoleAssociation.class, user1, analyticRole));
        save(new_composite(UserAndRoleAssociation.class, user2, dataEntryRole));
        save(new_composite(UserAndRoleAssociation.class, user2, fleetOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user2, warehouseOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user3, dataEntryRole));
        save(new_composite(UserAndRoleAssociation.class, user3, fleetOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user3, warehouseOperatorRole));

        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        final TgAuthor chrisDate = save(new_composite(TgAuthor.class, chris, "Date", null));

        final TgPersonName yurij = save(new_(TgPersonName.class, "Yurij", "Yurij"));
        final TgAuthor yurijShcherbyna = save(new_composite(TgAuthor.class, yurij, "Shcherbyna", "Mykolajovych"));

        save(new_composite(TgAuthorship.class, chrisDate, "An Introduction to Database Systems").setYear(2003));
        save(new_composite(TgAuthorship.class, chrisDate, "Database Design and Relational Theory").setYear(2012));
        save(new_composite(TgAuthorship.class, chrisDate, "SQL and Relational Theory").setYear(2015));
        save(new_composite(TgAuthorship.class, yurijShcherbyna, "Дискретна математика").setYear(2007));

        save(new_(TgEntityWithComplexSummaries.class, "veh1").setKms(200).setCost(100));
        save(new_(TgEntityWithComplexSummaries.class, "veh2").setKms(0).setCost(100));
        save(new_(TgEntityWithComplexSummaries.class, "veh3").setKms(300).setCost(100));
        save(new_(TgEntityWithComplexSummaries.class, "veh4").setKms(0).setCost(200));
    }

}