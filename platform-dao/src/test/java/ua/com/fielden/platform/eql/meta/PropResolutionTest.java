package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.eql.meta.QueryCategory.RESULT_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SOURCE_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SUB_QUERY;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

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
import ua.com.fielden.platform.eql.stage1.elements.EntQueryBlocks1;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.conditions.ComparisonTest1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.NullTest1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.SetTest1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntProp1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntQuery1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntValue1;
import ua.com.fielden.platform.eql.stage1.elements.operands.OperandsBasedSet1;
import ua.com.fielden.platform.eql.stage1.elements.sources.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.sources.QrySource1BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage1.elements.sources.Sources1;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBy2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBy2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ComparisonTest2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.NullTest2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.SetTest2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntValue2;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.elements.operands.OperandsBasedSet2;
import ua.com.fielden.platform.eql.stage2.elements.sources.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;
import ua.com.fielden.platform.eql.stage3.elements.Column;
import ua.com.fielden.platform.eql.stage3.elements.Table;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorRoyalty;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgEntityWithLoopedCalcProps;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModelWithCalc;
import ua.com.fielden.platform.sample.domain.TgWorkshop;

public class PropResolutionTest extends BaseEntQueryTCase1 {
    
    private static GroupBys2 emptyGroupBys2 = new GroupBys2(emptyList());
    private static OrderBys2 emptyOrderBys2 = new OrderBys2(emptyList());
    private static Yields2 emptyYields2 = new Yields2(emptyList());
    private static Conditions2 emptyConditions2 = new Conditions2();
    
    private static TransformationResult<EntQuery2> transform(final EntityResultQueryModel qry) {
        return entResultQry2(qry, new PropsResolutionContext(metadata));
    }

    private static TransformationResult<EntQuery2> transform(final AggregatedResultQueryModel qry) {
        return entResultQry2(qry, new PropsResolutionContext(metadata));
    }

    @Test
    public void test_q21_s1s3() {
        final AggregatedResultQueryModel qry = select(TgVehicle.class).as("veh").leftJoin(TgVehicle.class).as("rbv").
                on().prop("veh.replacedBy").eq().prop("rbv.id").or().prop("veh.replacedBy").ne().prop("rbv.id").
                yield().prop("veh.key").as("vehicle-key").
                yield().prop("rbv.key").as("replacedByVehicle-key").
                modelAsAggregate();
        
        final Map<String, Table> tables = new HashMap<>();
        
        final Map<String, Column> columns = new HashMap<>();
        columns.put("id", new Column("_ID"));
        columns.put("key", new Column("KEY_"));
        columns.put("replacedBy", new Column("REPLACED_BY_"));
        final Table vehT = new Table("EQDET", columns);
        tables.put(TgVehicle.class.getName(), vehT);
        final ua.com.fielden.platform.eql.stage2.elements.TransformationResult<EntQuery3> qry3 = entResultQry3(qry,  new PropsResolutionContext(metadata), new TransformationContext(tables));
       
        System.out.println(qry3.getItem().sql());
    }
    
    @Test
    public void test_q21_s1s2() {
        final EntityResultQueryModel<TgVehicleModel> qry = select(TgVehicleModel.class).where().prop("make").isNotNull().model();
        
        
        final Sources1 sources1 = new Sources1(new QrySource1BasedOnPersistentType(TgVehicleModel.class, 1));
        final Conditions1 conditions1 = new Conditions1(false, new NullTest1(new EntProp1("make", 2), true));

        final EntQueryBlocks1 parts1 = new EntQueryBlocks1(sources1, conditions1);
        final EntQuery1 expQry1 = new EntQuery1(parts1, TgVehicleModel.class, RESULT_QUERY, 3);

        assertEquals(expQry1, entResultQry(qry));
        
        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgVehicleModel.class, metadata.get(TgVehicleModel.class), 1);
        final Sources2 sources = new Sources2(source);
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        final EntProp2 makeProp2 = new EntProp2("make", source, TgVehicleMake.class, 2);
        firstAndConditionsGroup.add(new NullTest2(makeProp2, true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 expQry2 = new EntQuery2(parts, TgVehicleModel.class, RESULT_QUERY);

        final TransformationResult<EntQuery2> trQry2 = expQry1.transform(new PropsResolutionContext(metadata));
        assertEquals(expQry2, trQry2.getItem());
        assertEquals(setOf(makeProp2), trQry2.getUpdatedContext().getResolvedProps());
    }
    
    @Test
    public void test_12() {
        final EntityResultQueryModel<TgVehicleModel> sourceModel1Qry = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
        
        final Sources1 sm1Sources1 = new Sources1(new QrySource1BasedOnPersistentType(TgVehicleModel.class, 1));
        final Conditions1 sm1Conditions1 = new Conditions1(false, new ComparisonTest1(new EntProp1("key", 2), EQ, new EntValue1("316")));

        final EntQueryBlocks1 sm1Parts1 = new EntQueryBlocks1(sm1Sources1, sm1Conditions1);
        final EntQuery1 sm1ExpQry1 = new EntQuery1(sm1Parts1, TgVehicleModel.class, SOURCE_QUERY, 3);

//        assertEquals(sm1ExpQry1, entSourceQry(sourceModel1Qry));

        final QrySource2BasedOnPersistentType sm1Source2 = new QrySource2BasedOnPersistentType(TgVehicleModel.class, metadata.get(TgVehicleModel.class), 1);
        final Sources2 sm1Sources2 = new Sources2(sm1Source2);
        final List<List<? extends ICondition2<?>>> sm1AllConditions2 = new ArrayList<>();
        final List<ICondition2<?>> sm1FirstAndConditionsGroup2 = new ArrayList<>();
        final EntProp2 sm1Prop2 = new EntProp2("key", sm1Source2, String.class, 2);
        sm1FirstAndConditionsGroup2.add(new ComparisonTest2(sm1Prop2, EQ, new EntValue2("316")));
        sm1AllConditions2.add(sm1FirstAndConditionsGroup2);
        final Conditions2 sm1Conditions2 = new Conditions2(false, sm1AllConditions2);

        final EntQueryBlocks2 sm1Parts2 = new EntQueryBlocks2(sm1Sources2, sm1Conditions2, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 sm1ExpQry2 = new EntQuery2(sm1Parts2, TgVehicleModel.class, SOURCE_QUERY);
        
//        TransformationResult<EntQuery2> sm1TrQry2 = sm1ExpQry1.transform(new PropsResolutionContext(metadata));
//        assertEquals(sm1ExpQry2, sm1TrQry2.getItem());

        
        
        final EntityResultQueryModel<TgVehicleModel> sourceModel2Qry = select(TgVehicleModel.class).where().prop("key").eq().val("317").model();

        final Sources1 sm2Sources1 = new Sources1(new QrySource1BasedOnPersistentType(TgVehicleModel.class, 4));
        final Conditions1 sm2Conditions1 = new Conditions1(false, new ComparisonTest1(new EntProp1("key", 5), EQ, new EntValue1("317")));

        final EntQueryBlocks1 sm2Parts1 = new EntQueryBlocks1(sm2Sources1, sm2Conditions1);
        final EntQuery1 sm2ExpQry1 = new EntQuery1(sm2Parts1, TgVehicleModel.class, SOURCE_QUERY, 6);

//        assertEquals(sm2ExpQry1, entSourceQry(sourceModel2Qry));
        
        final QrySource2BasedOnPersistentType sm2Source2 = new QrySource2BasedOnPersistentType(TgVehicleModel.class, metadata.get(TgVehicleModel.class), 4);
        final Sources2 sm2Sources2 = new Sources2(sm2Source2);
        final List<List<? extends ICondition2<?>>> sm2AllConditions2 = new ArrayList<>();
        final List<ICondition2<?>> sm2FirstAndConditionsGroup2 = new ArrayList<>();
        final EntProp2 sm2Prop2 = new EntProp2("key", sm2Source2, String.class, 5);
        sm2FirstAndConditionsGroup2.add(new ComparisonTest2(sm2Prop2, EQ, new EntValue2("317")));
        sm2AllConditions2.add(sm2FirstAndConditionsGroup2);
        final Conditions2 sm2Conditions2 = new Conditions2(false, sm2AllConditions2);

        final EntQueryBlocks2 sm2Parts2 = new EntQueryBlocks2(sm2Sources2, sm2Conditions2, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 sm2ExpQry2 = new EntQuery2(sm2Parts2, TgVehicleModel.class, SOURCE_QUERY);
        
//        TransformationResult<EntQuery2> sm2TrQry2 = sm2ExpQry1.transform(new PropsResolutionContext(metadata));
//        assertEquals(sm2ExpQry2, sm2TrQry2.getItem());
        
        
        

        final EntityResultQueryModel<TgVehicleModel> qry = select(sourceModel1Qry, sourceModel2Qry).where().prop("key").in().values("316", "317").model();
        
        final Sources1 sources1 = new Sources1(new QrySource1BasedOnSubqueries(null, listOf(sm1ExpQry1, sm2ExpQry1), 7));
        final Conditions1 conditions1 = new Conditions1(false, new SetTest1(new EntProp1("key", 8), false, new OperandsBasedSet1(listOf(new EntValue1("316"), new EntValue1("317")))));

        final EntQueryBlocks1 parts1 = new EntQueryBlocks1(sources1, conditions1);
        final EntQuery1 expQry1 = new EntQuery1(parts1, TgVehicleModel.class, RESULT_QUERY, 9);

        
        final EntQuery1 actQry1 = entResultQry(qry);
        assertEquals(expQry1, actQry1);
        
        
        final QrySource2BasedOnSubqueries source2 = new QrySource2BasedOnSubqueries(listOf(sm1ExpQry2, sm2ExpQry2), null,  metadata, 7);
        
        final Sources2 sources2 = new Sources2(source2);
        final List<List<? extends ICondition2<?>>> allConditions2 = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup2 = new ArrayList<>();
        final EntProp2 prop2 = new EntProp2("key", source2, String.class, 8);
        firstAndConditionsGroup2.add(new SetTest2(prop2, false, new OperandsBasedSet2(listOf(new EntValue2("316"), new EntValue2("317")))));
        allConditions2.add(firstAndConditionsGroup2);
        final Conditions2 conditions2 = new Conditions2(false, allConditions2);

        final EntQueryBlocks2 parts2 = new EntQueryBlocks2(sources2, conditions2, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 expQry2 = new EntQuery2(parts2, TgVehicleModel.class, RESULT_QUERY);
        
        final TransformationResult<EntQuery2> trQry2 = expQry1.transform(new PropsResolutionContext(metadata));
        assertEquals(expQry2, trQry2.getItem());
        assertEquals(setOf(prop2, sm1Prop2, sm2Prop2), trQry2.getUpdatedContext().getResolvedProps());
    }


    @Test
    public void test_18_1() {
        final EntityResultQueryModel<TgOrgUnit1> qry = select(TgOrgUnit1.class).where().exists( //
                select(TgOrgUnit2.class).where().prop("parent").eq().extProp("id"). //
                model()). //
                model();  
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));
        System.out.println(qry2.getUpdatedContext().getResolvedProps());
    }


    @Test
    public void test_18() {
        transform(//
        select(TgOrgUnit1.class).where().exists( //
        select(TgOrgUnit2.class).where().prop("parent").eq().extProp("id").and().exists( //
        select(TgOrgUnit3.class).where().prop("parent").eq().extProp("id").and().exists( // 
        select(TgOrgUnit4.class).where().prop("parent").eq().extProp("id").and().exists( //
        select(TgOrgUnit5.class).where().prop("parent").eq().extProp("id").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()). //
        model());
    }

    @Test
    public void test_19() {
        transform(//
        select(TgOrgUnit1.class).as("L1").where().exists( //
        select(TgOrgUnit2.class).as("L2").where().prop("parent").eq().extProp("L1.id").and().exists( //
        select(TgOrgUnit3.class).as("L3").where().prop("parent").eq().extProp("L2.id").and().exists( // 
        select(TgOrgUnit4.class).as("L4").where().prop("parent").eq().extProp("L3.id").and().exists( //
        select(TgOrgUnit5.class).as("L5").where().prop("parent").eq().extProp("L4.id").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()). //
        model());
    }

    @Test
    @Ignore
    public void test_20() {
        transform(//
        select(TgOrgUnit1.class).as("L1").where().exists( //
        select(TgOrgUnit2.class).as("L2").where().prop("parent").eq().extProp("L1.id").and().exists( //
        select(TgOrgUnit3.class).as("L3").where().prop("parent").eq().extProp("L2.id").and().exists( // 
        select(TgOrgUnit4.class).as("L4").where().prop("parent").eq().extProp("L3.id").and().exists( //
        select(TgOrgUnit5.class).as("L5").where().prop("parent").eq().extProp("L4").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()). //
        model());
    }


    @Test
    public void test_21() {
        transform(//
        select(TgOrgUnit1.class).as("L1").where().exists( //
        select(TgOrgUnit2.class).as("L2").where().prop("parent").eq().extProp("L1.id").and().exists( //
        select(TgOrgUnit3.class).as("L3").where().prop("parent").eq().extProp("L1.id").and().exists( // 
        select(TgOrgUnit4.class).as("L4").where().prop("parent").eq().extProp("L1.id").and().exists( //
        select(TgOrgUnit5.class).as("L5").where().prop("parent").eq().extProp("L1.id").and().prop("key").isNotNull(). //
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

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgWorkshop.class, metadata.get(WORKSHOP), "w", 0);
        
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        // TODO make utility method for easy creation of Conditions2 with only one condition
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("key", source, String.class, 1), true));
        allConditions.add(firstAndConditionsGroup);

        final Conditions2 conditions = new Conditions2(false, allConditions);
        final EntQueryBlocks2 parts = new EntQueryBlocks2(new Sources2(source), conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
        final EntQuery2 exp = new EntQuery2(parts, TgWorkshop.class, RESULT_QUERY);
        assertEquals(qry2.getItem(), exp);
    }

//    @Test
//    public void test_q1() {
//        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().or().prop("surname").eq().iVal(null).model();
//        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata));
//
//        final EntQuery2 authorSourceQry = entResultQry2(MetadataGenerator.createYieldAllQueryModel(TgAuthor.class), new TransformatorToS2(metadata));
//        final QrySource2BasedOnPersistentTypeWithCalcProps source = new QrySource2BasedOnPersistentTypeWithCalcProps(TgAuthor.class, authorSourceQry);
//        final Sources2 sources = new Sources2(source);
//
//        final List<List<? extends ICondition2>> allConditions1 = new ArrayList<>();
//        final List<ICondition2> firstAndConditionsGroup1 = new ArrayList<>();
//        firstAndConditionsGroup1.add(new NullTest2(new EntProp2("surname", source, String.class), true));
//        allConditions1.add(firstAndConditionsGroup1);
//
//        final List<List<? extends ICondition2>> allConditions2 = new ArrayList<>();
//        final List<ICondition2> firstAndConditionsGroup2 = new ArrayList<>();
//        firstAndConditionsGroup2.add(new NullTest2(new EntProp2("key", source, String.class), true));
//        allConditions2.add(firstAndConditionsGroup2);
//
//        final List<List<? extends ICondition2>> allConditions = new ArrayList<>();
//        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
//        firstAndConditionsGroup.add(new Conditions2(false, allConditions2));
//        firstAndConditionsGroup.add(new Conditions2(false, allConditions1));
//        allConditions.add(firstAndConditionsGroup);
//
//        final Conditions2 conditions = new Conditions2(false, allConditions);
//
//        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(emptyList()), new OrderBys2(null));
//        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, RESULT_QUERY, null);
//        System.out.println(qry2.getConditions().equals(exp.getConditions()));
//        System.out.println(qry2.getGroups().equals(exp.getGroups()));
//        System.out.println(qry2.getYields().equals(exp.getYields()));
//        System.out.println(qry2.getSources().equals(exp.getSources()));
//        System.out.println(qry2.getOrderings().equals(exp.getOrderings()));
//        assertEquals(qry2, exp);
//    }

    @Test
    @Ignore
    public void test_q2() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgAuthor.class, metadata.get(TgAuthor.class), null, 0);
        final Sources2 sources = new Sources2(source);
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("surname", source, String.class, 1), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, RESULT_QUERY);

        System.out.println(qry2.getItem().conditions.equals(exp.conditions));
        System.out.println(qry2.getItem().groups.equals(exp.groups));
        System.out.println(qry2.getItem().yields.equals(exp.yields));
        System.out.println(qry2.getItem().sources.equals(exp.sources));
        System.out.println(qry2.getItem().orderings.equals(exp.orderings));
        assertEquals(qry2.getItem(), exp);
    }


//    @Test
//    public void test_q212() {
//        final EntityResultQueryModel<TgVehicleModelWithCalc> qry = select(TgVehicleModelWithCalc.class).where().prop("make").isNotNull().model();
//        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata));
//
//
//        
////        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgVehicleModel.class);
////        final Sources2 sources = new Sources2(source);
////        final List<List<? extends ICondition2>> allConditions = new ArrayList<>();
////        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
////        final Conditions2 conditions = new Conditions2(false, allConditions);
//
////        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(Collections.<OrderBy2> emptyList()));
////        final EntQuery2 exp = new EntQuery2(parts, TgVehicleModelWithCalc.class, RESULT_QUERY, null);
//
//        final EntQuery2 vehicleModelSourceQry = entSourceQry(MetadataGenerator.createYieldAllQueryModel(TgVehicleModelWithCalc.class), new TransformatorToS2(metadata));
//
//        final QrySource2BasedOnPersistentTypeWithCalcProps source = new QrySource2BasedOnPersistentTypeWithCalcProps(TgVehicleModelWithCalc.class, vehicleModelSourceQry);
//        final Sources2 sources = new Sources2(source);
//        final List<List<? extends ICondition2>> allConditions = new ArrayList<>();
//        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
//        firstAndConditionsGroup.add(new NullTest2(new EntProp2("make", source, TgVehicleMake.class), true));
//        allConditions.add(firstAndConditionsGroup);
//        final Conditions2 conditions = new Conditions2(false, allConditions);
//
//        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(Collections.<OrderBy2> emptyList()));
//        final EntQuery2 exp = new EntQuery2(parts, TgVehicleModelWithCalc.class, RESULT_QUERY, null);
//        System.out.println(qry2.getSources().getMain().sourceType().equals(exp.getSources().getMain().sourceType()));
//        System.out.println(qry2.getConditions().equals(exp.getConditions()));
//        System.out.println(qry2.getGroups().equals(exp.getGroups()));
//        System.out.println(qry2.getYields().equals(exp.getYields()));
//        System.out.println(qry2.getSources().equals(exp.getSources()));
//        System.out.println(qry2.getOrderings().equals(exp.getOrderings()));
//        assertEquals(qry2, exp);
//    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    @Test
    @Ignore
    public void test_q212_copy() {
        final EntityResultQueryModel<TgVehicleModelWithCalc> qry = select(TgVehicleModelWithCalc.class).where().prop("make").isNotNull().model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgVehicleModelWithCalc.class, metadata.get(TgVehicleModelWithCalc.class), null, 0);
        final Sources2 sources = new Sources2(source);
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("make", source, TgVehicleMake.class, 1), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(Collections.<OrderBy2> emptyList()));
        final EntQuery2 exp = new EntQuery2(parts, TgVehicleModelWithCalc.class, RESULT_QUERY);

        System.out.println(qry2.getItem().conditions.equals(exp.conditions));
        System.out.println(qry2.getItem().groups.equals(exp.groups));
        System.out.println(qry2.getItem().yields.equals(exp.yields));
        System.out.println(qry2.getItem().sources.equals(exp.sources));
        System.out.println(qry2.getItem().orderings.equals(exp.orderings));
        assertEquals(qry2.getItem(), exp);
    }

    @Test
    @Ignore
    public void test_q3() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).as("a").where().prop("a.surname").isNotNull().model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgAuthor.class, metadata.get(TgAuthor.class), "a", 0);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("surname", source, String.class, 1), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, RESULT_QUERY);

        assertEquals(qry2.getItem(), exp);
    }

    @Test
    @Ignore
    public void test_q4() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().and().prop("name").eq().iVal(null).model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgAuthor.class, metadata.get(TgAuthor.class), null, 0);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("surname", source, String.class, 1), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, RESULT_QUERY);

        assertEquals(qry2.getItem(), exp);
    }

    @Test
    @Ignore
    public void test_q5() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().and().prop("name").eq().iParam("param").model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgAuthor.class, metadata.get(TgAuthor.class), null, 0);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("surname", source, String.class, 1), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, RESULT_QUERY);

        assertEquals(qry2.getItem(), exp);
    }

    @Test
    @Ignore
    public void test_q6() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").eq().param("param").model();
        final Map<String, Object> params = new HashMap<>();
        params.put("param", 1);
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgAuthor.class, metadata.get(TgAuthor.class), null, 0);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new ComparisonTest2(new EntProp2("surname", source, String.class, 1), EQ, new EntValue2(1)));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, RESULT_QUERY);

        assertEquals(exp, qry2.getItem());
    }

    @Test
    @Ignore
    public void test_q7() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn.id"). //
        where().prop("surname").eq().val(1).model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(TgAuthor.class, metadata.get(TgAuthor.class), null, 0);
        final QrySource2BasedOnPersistentType source2 = new QrySource2BasedOnPersistentType(TgPersonName.class, metadata.get(TgPersonName.class), "pn", 0);

        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new ComparisonTest2(new EntProp2("name", source, TgPersonName.class, 1), EQ, new EntProp2("id", source2, Long.class, 1)));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 joinConditions = new Conditions2(false, allConditions);

        final CompoundSource2 compound = new CompoundSource2(source2, JoinType.LJ, joinConditions);
        final List<CompoundSource2> compounds = new ArrayList<>();
        compounds.add(compound);
        final Sources2 sources = new Sources2(source, compounds);
        final List<List<? extends ICondition2<?>>> allConditions2 = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup2 = new ArrayList<>();
        firstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2("surname", source, String.class, 1), EQ, new EntValue2(1)));
        allConditions2.add(firstAndConditionsGroup2);
        final Conditions2 conditions = new Conditions2(false, allConditions2);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, RESULT_QUERY);

        assertEquals(exp, qry2.getItem());
    }

    @Test
    @Ignore
    public void test_q8() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn.id"). //
        where().prop("lastRoyalty").eq().val(1).model();
        final TransformationResult<EntQuery2> qry2 = entResultQry2(qry, new PropsResolutionContext(metadata));

        final QrySource2BasedOnPersistentType sourceAuthor = new QrySource2BasedOnPersistentType(TgAuthor.class, metadata.get(TgAuthor.class), null, 0);

        final QrySource2BasedOnPersistentType sourceAuthorRoyalty = new QrySource2BasedOnPersistentType(TgAuthorRoyalty.class, metadata.get(TgAuthorRoyalty.class), null, 0);

        final List<List<? extends ICondition2<?>>> lrAllConditions2 = new ArrayList<>();
        final List<ICondition2<?>> lrFirstAndConditionsGroup2 = new ArrayList<>();
        lrFirstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2("authorship.author", sourceAuthorRoyalty, TgAuthor.class, 1), EQ,
                new EntProp2("id", sourceAuthor, Long.class, 1)));
        lrAllConditions2.add(lrFirstAndConditionsGroup2);
        final Conditions2 lrConditions = new Conditions2(false, lrAllConditions2);

        final EntQueryBlocks2 lastRoyaltyParts = new EntQueryBlocks2(new Sources2(sourceAuthorRoyalty), lrConditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 lastRoyaltySubqry = new EntQuery2(lastRoyaltyParts, TgAuthorRoyalty.class, SUB_QUERY);

        final QrySource2BasedOnPersistentType sourcePersonName = new QrySource2BasedOnPersistentType(TgPersonName.class, metadata.get(TgPersonName.class), "pn", 0);

        final List<List<? extends ICondition2<?>>> allConditions = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new ComparisonTest2(new EntProp2("name", sourceAuthor, TgPersonName.class, 1), EQ, new EntProp2("id", sourcePersonName, Long.class, 1)));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 joinConditions = new Conditions2(false, allConditions);

        final CompoundSource2 compound = new CompoundSource2(sourcePersonName, LJ, joinConditions);
        final List<CompoundSource2> compounds = new ArrayList<>();
        compounds.add(compound);
        final Sources2 sources = new Sources2(sourceAuthor, compounds);
        final List<List<? extends ICondition2<?>>> allConditions2 = new ArrayList<>();
        final List<ICondition2<?>> firstAndConditionsGroup2 = new ArrayList<>();

        final Expression2 lrExpr = new Expression2(lastRoyaltySubqry);
        firstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2("lastRoyalty", sourceAuthor, TgAuthorRoyalty.class, 1), EQ, new EntValue2(1)));
        allConditions2.add(firstAndConditionsGroup2);
        final Conditions2 conditions = new Conditions2(false, allConditions2);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, emptyYields2, new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, RESULT_QUERY);

        assertEquals(exp, qry2.getItem());
    }

    @Test
    public void test_00() {
        transform(select(TgAuthor.class).where().prop("lastRoyalty").isNotNull().model());
    }

    @Test
    public void test_01() {
        transform(select(TgAuthor.class).as("aa").where().prop("lastRoyalty").isNotNull().and().prop("aa.surname").isNull().model());
    }

    @Test
    @Ignore
    public void test_02() {
        transform(select(TgAuthorship.class).where().exists(select(TgAuthor.class).where().prop("lastRoyalty").isNotNull().model()).model());
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
        transform(select(TgAuthor.class).where().exists(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("id").model()).model());
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
        transform(select(TgAuthorship.class).where().beginExpr().val(100).mult().model(select(TgAuthor.class).yield().countAll().modelAsPrimitive()).endExpr().ge().val(1000).model());
    }


    @Test
    @Ignore
    public void test_13() {
        transform(select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2").model());
    }

    @Test
    public void test_14() {
        transform(select(TgAuthor.class).where().prop("hasMultiplePublications").eq().val(true).model());
    }

    @Test
    @Ignore
    public void test_15() {
        // TODO EQL.3
        transform(select(TgEntityWithLoopedCalcProps.class).where().prop("calc1").gt().val(25).model());
    }

    @Test
    @Ignore
    public void test_16() {
        // TODO EQL.3
        transform(select(TgAuthor.class).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn").where().prop("lastRoyalty").eq().val(1).model());
    }

    @Test
    public void test_17() {
        transform(select(TgAuthor.class).where().prop("name").isNotNull().groupBy().prop("name").yield().prop("name").modelAsEntity(TgPersonName.class));
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
        transform(select(select(TgVehicle.class).yield().prop("key").as("key").yield().prop("desc").as("desc").yield().prop("model.make").as("model-make").modelAsAggregate()).where().prop("model-make").isNotNull().model());
    }

    
    //TODO EQL transfer to S2 tests
    //  @Test
    //  public void test_user_data_filtering() {
    //  assertModelsEqualsAccordingUserDataFiltering(//
    //      select(VEHICLE). //
    //      where().prop("model.make.key").eq().val("MERC").model(),
    //
    //      select(VEHICLE). //
    //      where().begin().prop("key").notLike().val("A%").end().and().begin().prop("model.make.key").eq().val("MERC").end().model());
    //  }
}