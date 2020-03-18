package ua.com.fielden.platform.eql.stage2;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.eql.meta.QueryCategory.RESULT_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SUB_QUERY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBy2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ComparisonTest2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.NullTest2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntValue2;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.elements.sources.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorRoyalty;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgEntityWithLoopedCalcProps;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgWorkshop;

public class QmToStage2TransformationTest extends EqlStage2TestCase {
    
    @Test
    public void test01() {
        final EntityResultQueryModel<TeVehicleModel> qry = select(MODEL).where().prop("make.key").isNotNull().model();
        
        final QrySource2BasedOnPersistentType source = source("1", MODEL);
        final Sources2 sources = sources(source);
        final EntProp2 makeProp2 = prop(source, pi(MODEL, "make"), pi(MAKE, "key"));
        final Conditions2 conditions = cond(isNotNull(makeProp2));
        final EntQuery2 expQry2 = new EntQuery2(qb2(sources, conditions), MODEL, RESULT_QUERY);

        final TransformationResult<EntQuery2> trQry2 = transform(qry);
        assertEquals(expQry2, trQry2.item);
    }
    
    @Test
    public void test02() {
        final EntityResultQueryModel<TeVehicleModel> qry = select(MODEL).where().prop("make").isNotNull().model();
        
        final QrySource2BasedOnPersistentType source = source("1", MODEL);
        final Sources2 sources = sources(source);
        final EntProp2 makeProp2 = prop(source, pi(MODEL, "make"));
        final Conditions2 conditions = cond(isNotNull(makeProp2));
        final EntQuery2 expQry2 = new EntQuery2(qb2(sources, conditions), MODEL, RESULT_QUERY);

        final TransformationResult<EntQuery2> trQry2 = entResultQry2(qry, new PropsResolutionContext(metadata));
        assertEquals(expQry2, trQry2.item);
    }
    
    @Test
    public void prop_paths_are_correctly_resolved() {
        final EntityResultQueryModel<TeVehicle> qry = select(VEHICLE).where().
                anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull().model();
        
        final TransformationResult<EntQuery2> a = transform(qry);
        final Map<String, List<AbstractPropInfo<?>>> paths = getResolvedProps(a.item.collectProps());

        assertEquals(asList(pi(VEHICLE, "initDate")), paths.get("initDate"));
        assertEquals(asList(pi(VEHICLE, "station"), pi(ORG5, "name")), paths.get("station.name"));
        assertEquals(asList(pi(VEHICLE, "station"), pi(ORG5, "parent"), pi(ORG4, "name")), paths.get("station.parent.name"));
        assertEquals(asList(pi(VEHICLE, "replacedBy"), pi(VEHICLE, "initDate")), paths.get("replacedBy.initDate"));
    }
    
    @Test
    public void prop_paths_without_aliases_with_aliased_source_are_correctly_resolved() {
        final EntityResultQueryModel<TeVehicle> qry = select(VEHICLE).as("v").where().
                anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull().model();
        
        final TransformationResult<EntQuery2> a = transform(qry);
        final Map<String, List<AbstractPropInfo<?>>> paths = getResolvedProps(a.item.collectProps());

        assertEquals(asList(pi(VEHICLE, "initDate")), paths.get("initDate"));
        assertEquals(asList(pi(VEHICLE, "station"), pi(ORG5, "name")), paths.get("station.name"));
        assertEquals(asList(pi(VEHICLE, "station"), pi(ORG5, "parent"), pi(ORG4, "name")), paths.get("station.parent.name"));
        assertEquals(asList(pi(VEHICLE, "replacedBy"), pi(VEHICLE, "initDate")), paths.get("replacedBy.initDate"));
    }

    @Test
    public void prop_paths_with_some_aliases_with_aliased_source_are_correctly_resolved() {
        final EntityResultQueryModel<TeVehicle> qry = select(VEHICLE).as("v").where().
                anyOfProps("v.initDate", "station.name", "station.parent.name", "v.replacedBy.initDate").isNotNull().model();
        
        final TransformationResult<EntQuery2> a = transform(qry);
        final Map<String, List<AbstractPropInfo<?>>> paths = getResolvedProps(a.item.collectProps());

        assertEquals(asList(pi(VEHICLE, "initDate")), paths.get("initDate"));
        assertEquals(asList(pi(VEHICLE, "station"), pi(ORG5, "name")), paths.get("station.name"));
        assertEquals(asList(pi(VEHICLE, "station"), pi(ORG5, "parent"), pi(ORG4, "name")), paths.get("station.parent.name"));
        assertEquals(asList(pi(VEHICLE, "replacedBy"), pi(VEHICLE, "initDate")), paths.get("replacedBy.initDate"));
    }
    
    @Test
    public void prop_paths_in_qry_with_two_sources_are_correctly_resolved() {
        final EntityResultQueryModel<TeVehicle> qry = select(VEHICLE).as("v").join(VEHICLE).as("rv").on().prop("v.replacedBy").eq().prop("rv.id").
                where().anyOfProps("v.initDate", "rv.station.name", "v.station.parent.name", "rv.replacedBy.initDate").isNotNull().model();
        
        final TransformationResult<EntQuery2> a = transform(qry);
        final Map<String, List<AbstractPropInfo<?>>> paths = getResolvedProps(a.item.collectProps());

        assertEquals(asList(pi(VEHICLE, "initDate")), paths.get("initDate"));
        assertEquals(asList(pi(VEHICLE, "station"), pi(ORG5, "name")), paths.get("station.name"));
        assertEquals(asList(pi(VEHICLE, "station"), pi(ORG5, "parent"), pi(ORG4, "name")), paths.get("station.parent.name"));
        assertEquals(asList(pi(VEHICLE, "replacedBy"), pi(VEHICLE, "initDate")), paths.get("replacedBy.initDate"));
    }
    
    @Test
    public void test_18_1() {
        final EntityResultQueryModel<TgOrgUnit1> qry = select(ORG1).where().exists( //
                select(ORG2).where().prop("parent").eq().extProp("id"). //
                model()). //
                model();  
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));
        System.out.println(qry2.item.collectProps());
    }

    @Test
    public void test_00() {
        transform(select(AUTHOR).where().prop("lastRoyalty").isNotNull().model());
    }

    @Test
    public void test_01() {
        transform(select(AUTHOR).as("aa").where().prop("lastRoyalty").isNotNull().and().prop("aa.surname").isNull().model());
    }

    @Test
    public void test_02() {
        transform(select(TgAuthorship.class).where().exists(select(AUTHOR).where().prop("lastRoyalty").isNotNull().model()).model());
    }

    @Test
    public void test_03() {
        transform(select(TgAuthorship.class).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model());
    }

    @Test
    public void test_04() {
        transform(select(TgAuthorRoyalty.class).as("ar").where().prop("authorship.author.surname").eq().val("Date").or().prop("ar.authorship.author.name.key").eq().val("Chris").model());
    }

    @Test
    public void test_05() {
        transform(select(AUTHOR).where().exists(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("id").model()).model());
    }

    @Test
    public void test_06() {
        transform(select(TgAuthorRoyalty.class).as("ar").where().exists(select(TgAuthorship.class).where().prop("id").eq().extProp("authorship").and().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model()).model());
    }

    @Test
    public void test_07() {
        transform(select(select(TgAuthorship.class).where().prop("title").isNotNull().model()).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model());
    }

    @Test
    public void test_08() {
        transform(select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().model()).as("ar").where().prop("authorship.author.surname").eq().val("Date").or().prop("ar.authorship.author.name.key").eq().val("Chris").model());
    }

    @Test
    public void test09() {
        transform(select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().yield().prop("authorship.author").modelAsEntity(TgAuthorship.class)).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model());
    }

    @Test
    public void test_10() {
        transform(select(select(TgAuthorship.class).where().prop("title").isNotNull().yield().prop("author").as("author").yield().prop("title").as("bookTitle").modelAsAggregate()).where().prop("bookTitle").isNotNull().or().begin().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").end().yield().prop("author.name.key").as("name").yield().prop("bookTitle").as("titel").modelAsAggregate());
    }

    @Test
    public void test_11() {
        transform(select(TgAuthorship.class).where().beginExpr().val(100).mult().model(select(AUTHOR).yield().countAll().modelAsPrimitive()).endExpr().ge().val(1000).model());
    }


    @Test
    @Ignore
    public void test_13() {
        transform(select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2").model());
    }

    @Test
    public void test_14() {
        transform(select(AUTHOR).where().prop("hasMultiplePublications").eq().val(true).model());
    }

    @Test
    @Ignore
    public void test_15() {
        // TODO EQL.3
        transform(select(TgEntityWithLoopedCalcProps.class).where().prop("calc1").gt().val(25).model());
    }

    @Test
    public void test_16() {
        transform(select(AUTHOR).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn").where().prop("lastRoyalty").eq().val(1).model());
    }

    @Test
    public void test_17() {
        transform(select(AUTHOR).where().prop("name").isNotNull().groupBy().prop("name").yield().prop("name").modelAsEntity(TgPersonName.class));
    }

    @Test
    public void test_22() {
        transform(select(TgAuthorRoyalty.class).where().prop("payment").isNotNull().model());
    }

    @Test
    @Ignore
    public void test_23() {
        transform(select(TgAuthorRoyalty.class).where().prop("payment.amount").isNotNull().model());
    }

    @Test
    public void test_24() {
        transform(select(select(VEHICLE).yield().prop("key").as("key").yield().prop("desc").as("desc").yield().prop("model.make").as("model-make").modelAsAggregate()).where().prop("model-make").isNotNull().model());
    }
    
    @Test
    public void test_q21_s1s3() {
        final AggregatedResultQueryModel qry = select(VEHICLE).as("veh").leftJoin(VEHICLE).as("rbv").
                on().prop("veh.replacedBy").eq().prop("rbv.id").or().prop("veh.replacedBy").ne().prop("rbv.id").
                yield().prop("veh.key").as("vehicle-key").
                yield().prop("rbv.key").as("replacedByVehicle-key").
                modelAsAggregate();
//        final ua.com.fielden.platform.eql.stage2.elements.TransformationResult<EntQuery3> qry3 = entResultQry3(qry,  new PropsResolutionContext(metadata), tables);
//        System.out.println(qry3.item.sql(DbVersion.H2));
    }
    
    @Test
    public void test_18() {
        transform(//
        select(ORG1).where().exists( //
        select(ORG2).where().prop("parent").eq().extProp("id").and().exists( //
        select(ORG3).where().prop("parent").eq().extProp("id").and().exists( // 
        select(ORG4).where().prop("parent").eq().extProp("id").and().exists( //
        select(ORG5).where().prop("parent").eq().extProp("id").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()). //
        model());
    }

    @Test
    public void test_19() {
        transform(//
        select(ORG1).as("L1").where().exists( //
        select(ORG2).as("L2").where().prop("parent").eq().extProp("L1.id").and().exists( //
        select(ORG3).as("L3").where().prop("parent").eq().extProp("L2.id").and().exists( // 
        select(ORG4).as("L4").where().prop("parent").eq().extProp("L3.id").and().exists( //
        select(ORG5).as("L5").where().prop("parent").eq().extProp("L4.id").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()). //
        model());
    }

    @Test
    public void test_20() {
        transform(//
        select(ORG1).as("L1").where().exists( //
        select(ORG2).as("L2").where().prop("parent").eq().extProp("L1.id").and().exists( //
        select(ORG3).as("L3").where().prop("parent").eq().extProp("L2.id").and().exists( // 
        select(ORG4).as("L4").where().prop("parent").eq().extProp("L3.id").and().exists( //
        select(ORG5).as("L5").where().prop("parent").eq().extProp("L4").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()). //
        model());
    }


    @Test
    public void test_21() {
        transform(//
        select(ORG1).as("L1").where().exists( //
        select(ORG2).as("L2").where().prop("parent").eq().extProp("L1.id").and().exists( //
        select(ORG3).as("L3").where().prop("parent").eq().extProp("L1.id").and().exists( // 
        select(ORG4).as("L4").where().prop("parent").eq().extProp("L1.id").and().exists( //
        select(ORG5).as("L5").where().prop("parent").eq().extProp("L1.id").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()). //
        model());
    }

    @Test
    @Ignore
    public void test_that_prop_name_is_without_alias_at_stage2() {
        final EntityResultQueryModel<TgWorkshop> qry = select(TgWorkshop.class).as("w").where().prop("w.key").isNotNull().model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgWorkshop.class, metadata.get(WORKSHOP), "w", "0");
        
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        // TODO make utility method for easy creation of Conditions2 with only one condition
        firstAndConditionsGroup.add(new NullTest2(new EntProp2(source, asList(pi(WORKSHOP, "key"))), true));
        allConditions.add(firstAndConditionsGroup);

        final Conditions2 conditions = new Conditions2(false, allConditions);
        final EntQueryBlocks2 parts = new EntQueryBlocks2(new Sources2(source, emptyList()), conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 exp = new EntQuery2(parts, TgWorkshop.class, RESULT_QUERY);
        assertEquals(qry2.item, exp);
    }

    @Test
    @Ignore
    public void test_q2() {
        final EntityResultQueryModel<TgAuthor> qry = select(AUTHOR).where().prop("surname").isNotNull().model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(AUTHOR, metadata.get(AUTHOR), null, "0");
        final Sources2 sources = new Sources2(source, emptyList());
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2(source, asList(pi(AUTHOR, "surname"))), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 exp = new EntQuery2(parts, AUTHOR, RESULT_QUERY);

        System.out.println(qry2.item.conditions.equals(exp.conditions));
        System.out.println(qry2.item.groups.equals(exp.groups));
        System.out.println(qry2.item.yields.equals(exp.yields));
        System.out.println(qry2.item.sources.equals(exp.sources));
        System.out.println(qry2.item.orderings.equals(exp.orderings));
        assertEquals(qry2.item, exp);
    }

    
    @Test
    @Ignore
    public void test_q3() {
        final EntityResultQueryModel<TgAuthor> qry = select(AUTHOR).as("a").where().prop("a.surname").isNotNull().model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(AUTHOR, metadata.get(AUTHOR), "a", "0");
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2(source, asList(pi(AUTHOR, "surname"))), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 exp = new EntQuery2(parts, AUTHOR, RESULT_QUERY);

        assertEquals(qry2.item, exp);
    }

    @Test
    @Ignore
    public void test_q4() {
        final EntityResultQueryModel<TgAuthor> qry = select(AUTHOR).where().prop("surname").isNotNull().and().prop("name").eq().iVal(null).model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(AUTHOR, metadata.get(AUTHOR), null, "0");
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2(source, asList(pi(AUTHOR, "surname"))), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 exp = new EntQuery2(parts, AUTHOR, RESULT_QUERY);

        assertEquals(qry2.item, exp);
    }

    @Test
    @Ignore
    public void test_q5() {
        final EntityResultQueryModel<TgAuthor> qry = select(AUTHOR).where().prop("surname").isNotNull().and().prop("name").eq().iParam("param").model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(AUTHOR, metadata.get(AUTHOR), null, "0");
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2(source, asList(pi(AUTHOR, "surname"))), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 exp = new EntQuery2(parts, AUTHOR, RESULT_QUERY);

        assertEquals(qry2.item, exp);
    }

    @Test
    @Ignore
    public void test_q6() {
        final EntityResultQueryModel<TgAuthor> qry = select(AUTHOR).where().prop("surname").eq().param("param").model();
        final Map<String, Object> params = new HashMap<>();
        params.put("param", 1);
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(AUTHOR, metadata.get(AUTHOR), null, "0");
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new ComparisonTest2(new EntProp2(source, asList(pi(AUTHOR, "surname"))), EQ, new EntValue2(1)));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 exp = new EntQuery2(parts, AUTHOR, RESULT_QUERY);

        assertEquals(exp, qry2.item);
    }

    @Test
    @Ignore
    public void test_q7() {
        final EntityResultQueryModel<TgAuthor> qry = select(AUTHOR).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn.id"). //
        where().prop("surname").eq().val(1).model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(AUTHOR, metadata.get(AUTHOR), null, "0");
        final QrySource2BasedOnPersistentType source2 = new QrySource2BasedOnPersistentType(TgPersonName.class, metadata.get(TgPersonName.class), "pn", "0");

        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new ComparisonTest2(new EntProp2(source, asList(pi(AUTHOR, "name"))), //
                EQ, // 
                new EntProp2(source2, asList(metadata.get(TgPersonName.class).getProps().get("id")))));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 joinConditions = new Conditions2(false, allConditions);

        final CompoundSource2 compound = new CompoundSource2(source2, JoinType.LJ, joinConditions);
        final List<CompoundSource2> compounds = new ArrayList<>();
        compounds.add(compound);
        final Sources2 sources = new Sources2(source, compounds);
        final List<List<? extends ICondition2<?>>> allConditions2 = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup2 = new ArrayList<>();
        firstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2(source, asList(pi(AUTHOR, "surname"))), EQ, new EntValue2(1)));
        allConditions2.add(firstAndConditionsGroup2);
        final Conditions2 conditions = new Conditions2(false, allConditions2);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 exp = new EntQuery2(parts, AUTHOR, RESULT_QUERY);

        assertEquals(exp, qry2.item);
    }

    @Test
    @Ignore
    public void test_q8() {
        final EntityResultQueryModel<TgAuthor> qry = select(AUTHOR).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn.id"). //
        where().prop("lastRoyalty").eq().val(1).model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType sourceAuthor = new QrySource2BasedOnPersistentType(AUTHOR, metadata.get(AUTHOR), null, "0");

        final QrySource2BasedOnPersistentType sourceAuthorRoyalty = new QrySource2BasedOnPersistentType(TgAuthorRoyalty.class, metadata.get(TgAuthorRoyalty.class), null, "0");

        final List<List<? extends ICondition2<?>>> lrAllConditions2 = new ArrayList<>();
        final List<ICondition2<?>> lrFirstAndConditionsGroup2 = new ArrayList<>();
        lrFirstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2(sourceAuthorRoyalty, asList(null)), EQ,
                new EntProp2(sourceAuthor, asList(null))));
        lrAllConditions2.add(lrFirstAndConditionsGroup2);
        final Conditions2 lrConditions = new Conditions2(false, lrAllConditions2);

        final EntQueryBlocks2 lastRoyaltyParts = new EntQueryBlocks2(new Sources2(sourceAuthorRoyalty, emptyList()), lrConditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 lastRoyaltySubqry = new EntQuery2(lastRoyaltyParts, TgAuthorRoyalty.class, SUB_QUERY);

        final QrySource2BasedOnPersistentType sourcePersonName = new QrySource2BasedOnPersistentType(TgPersonName.class, metadata.get(TgPersonName.class), "pn", "0");

        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new ComparisonTest2(new EntProp2(sourceAuthor, asList(null)), EQ, new EntProp2(sourcePersonName, asList(null))));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 joinConditions = new Conditions2(false, allConditions);

        final CompoundSource2 compound = new CompoundSource2(sourcePersonName, LJ, joinConditions);
        final List<CompoundSource2> compounds = new ArrayList<>();
        compounds.add(compound);
        final Sources2 sources = new Sources2(sourceAuthor, compounds);
        final List<List<? extends ICondition2<?>>> allConditions2 = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup2 = new ArrayList<>();

        final Expression2 lrExpr = new Expression2(lastRoyaltySubqry, emptyList());
        firstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2(sourceAuthor, asList(null)), EQ, new EntValue2(1)));
        allConditions2.add(firstAndConditionsGroup2);
        final Conditions2 conditions = new Conditions2(false, allConditions2);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 exp = new EntQuery2(parts, AUTHOR, RESULT_QUERY);

        assertEquals(exp, qry2.item);
    }

}