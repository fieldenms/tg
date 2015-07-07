package ua.com.fielden.platform.eql.meta;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.s2.elements.ComparisonTest2;
import ua.com.fielden.platform.eql.s2.elements.CompoundSource2;
import ua.com.fielden.platform.eql.s2.elements.Conditions2;
import ua.com.fielden.platform.eql.s2.elements.EntProp2;
import ua.com.fielden.platform.eql.s2.elements.EntQuery2;
import ua.com.fielden.platform.eql.s2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.s2.elements.EntValue2;
import ua.com.fielden.platform.eql.s2.elements.Expression2;
import ua.com.fielden.platform.eql.s2.elements.GroupBy2;
import ua.com.fielden.platform.eql.s2.elements.GroupBys2;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;
import ua.com.fielden.platform.eql.s2.elements.NullTest2;
import ua.com.fielden.platform.eql.s2.elements.OrderBys2;
import ua.com.fielden.platform.eql.s2.elements.Sources2;
import ua.com.fielden.platform.eql.s2.elements.TypeBasedSource2;
import ua.com.fielden.platform.eql.s2.elements.Yields2;
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
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

public class PropResolutionTest extends BaseEntQueryTCase1 {

    private EntQuery2 transform(final EntityResultQueryModel qry) {
        return entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    private EntQuery2 transform(final AggregatedResultQueryModel qry) {
        return entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    @Ignore
    public void test_q1() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().or().prop("surname").eq().iVal(null).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));

        final TypeBasedSource2 source = new TypeBasedSource2(TgAuthor.class);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());

        final List<List<ICondition2>> allConditions1 = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup1 = new ArrayList<>();
        firstAndConditionsGroup1.add(new NullTest2(new EntProp2("surname", source, metadata.get(TgAuthor.class).resolve("surname"), null), true));
        allConditions1.add(firstAndConditionsGroup1);

        final List<List<ICondition2>> allConditions2 = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup2 = new ArrayList<>();
        firstAndConditionsGroup2.add(new NullTest2(new EntProp2("key", source, metadata.get(TgAuthor.class).resolve("key"), null), true));
        allConditions2.add(firstAndConditionsGroup2);

        final List<List<ICondition2>> allConditions = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new Conditions2(false, allConditions2));
        firstAndConditionsGroup.add(new Conditions2(false, allConditions1));
        allConditions.add(firstAndConditionsGroup);

        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, QueryCategory.RESULT_QUERY, null);

        assertEquals(qry2, exp);
    }

    @Test
    public void test_q2() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));

        final TypeBasedSource2 source = new TypeBasedSource2(TgAuthor.class);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<ICondition2>> allConditions = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("surname", source, metadata.get(TgAuthor.class).resolve("surname"), null), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, QueryCategory.RESULT_QUERY, null);

        assertEquals(qry2, exp);
    }

    @Test
    public void test_q3() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).as("a").where().prop("a.surname").isNotNull().model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));

        final TypeBasedSource2 source = new TypeBasedSource2(TgAuthor.class);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<ICondition2>> allConditions = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("a.surname", source, metadata.get(TgAuthor.class).resolve("surname"), null), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, QueryCategory.RESULT_QUERY, null);

        assertEquals(qry2, exp);
    }

    @Test
    public void test_q4() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().and().prop("name").eq().iVal(null).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));

        final TypeBasedSource2 source = new TypeBasedSource2(TgAuthor.class);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<ICondition2>> allConditions = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("surname", source, metadata.get(TgAuthor.class).resolve("surname"), null), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, QueryCategory.RESULT_QUERY, null);

        assertEquals(qry2, exp);
    }

    @Test
    public void test_q5() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().and().prop("name").eq().iParam("param").model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));

        final TypeBasedSource2 source = new TypeBasedSource2(TgAuthor.class);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<ICondition2>> allConditions = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new NullTest2(new EntProp2("surname", source, metadata.get(TgAuthor.class).resolve("surname"), null), true));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, QueryCategory.RESULT_QUERY, null);

        assertEquals(qry2, exp);
    }

    @Test
    public void test_q6() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").eq().param("param").model();
        final Map<String, Object> params = new HashMap<>();
        params.put("param", 1);
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, params, DOMAIN_METADATA));

        final TypeBasedSource2 source = new TypeBasedSource2(TgAuthor.class);
        final Sources2 sources = new Sources2(source, Collections.<CompoundSource2> emptyList());
        final List<List<ICondition2>> allConditions = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new ComparisonTest2(new EntProp2("surname", source, metadata.get(TgAuthor.class).resolve("surname"), null), ComparisonOperator.EQ, new EntValue2(1)));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 conditions = new Conditions2(false, allConditions);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, QueryCategory.RESULT_QUERY, null);

        assertEquals(exp, qry2);
    }

    @Test
    public void test_q7() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn.id"). //
        where().prop("surname").eq().val(1).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));

        final TypeBasedSource2 source = new TypeBasedSource2(TgAuthor.class);
        final TypeBasedSource2 source2 = new TypeBasedSource2(TgPersonName.class);

        final List<List<ICondition2>> allConditions = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new ComparisonTest2(new EntProp2("name", source, metadata.get(TgAuthor.class).resolve("name"), null), ComparisonOperator.EQ, new EntProp2("pn.id", source2, metadata.get(TgPersonName.class).resolve("id"), null)));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 joinConditions = new Conditions2(false, allConditions);

        final CompoundSource2 compound = new CompoundSource2(source2, JoinType.LJ, joinConditions);
        final List<CompoundSource2> compounds = new ArrayList<>();
        compounds.add(compound);
        final Sources2 sources = new Sources2(source, compounds);
        final List<List<ICondition2>> allConditions2 = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup2 = new ArrayList<>();
        firstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2("surname", source, metadata.get(TgAuthor.class).resolve("surname"), null), ComparisonOperator.EQ, new EntValue2(1)));
        allConditions2.add(firstAndConditionsGroup2);
        final Conditions2 conditions = new Conditions2(false, allConditions2);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, QueryCategory.RESULT_QUERY, null);

        assertEquals(exp, qry2);
    }

    @Test
    public void test_q8() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn.id"). //
        where().prop("lastRoyalty").eq().val(1).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));

        final TypeBasedSource2 sourceAuthor = new TypeBasedSource2(TgAuthor.class);

        final TypeBasedSource2 sourceAuthorRoyalty = new TypeBasedSource2(TgAuthorRoyalty.class);

        final List<List<ICondition2>> lrAllConditions2 = new ArrayList<>();
        final List<ICondition2> lrFirstAndConditionsGroup2 = new ArrayList<>();
        lrFirstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2("authorship.author", sourceAuthorRoyalty, metadata.get(TgAuthorRoyalty.class).resolve("authorship.author"), null), ComparisonOperator.EQ,
                new EntProp2("id", sourceAuthor, metadata.get(TgAuthor.class).resolve("id"), null)));
        lrAllConditions2.add(lrFirstAndConditionsGroup2);
        final Conditions2 lrConditions = new Conditions2(false, lrAllConditions2);

        final EntQueryBlocks2 lastRoyaltyParts = new EntQueryBlocks2(new Sources2(sourceAuthorRoyalty), lrConditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 lastRoyaltySubqry = new EntQuery2(lastRoyaltyParts, TgAuthorRoyalty.class, QueryCategory.SUB_QUERY, null);

        final TypeBasedSource2 sourcePersonName = new TypeBasedSource2(TgPersonName.class);

        final List<List<ICondition2>> allConditions = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup = new ArrayList<>();
        firstAndConditionsGroup.add(new ComparisonTest2(new EntProp2("name", sourceAuthor, metadata.get(TgAuthor.class).resolve("name"), null), ComparisonOperator.EQ, new EntProp2("pn.id", sourcePersonName, metadata.get(TgPersonName.class).resolve("id"), null)));
        allConditions.add(firstAndConditionsGroup);
        final Conditions2 joinConditions = new Conditions2(false, allConditions);

        final CompoundSource2 compound = new CompoundSource2(sourcePersonName, JoinType.LJ, joinConditions);
        final List<CompoundSource2> compounds = new ArrayList<>();
        compounds.add(compound);
        final Sources2 sources = new Sources2(sourceAuthor, compounds);
        final List<List<ICondition2>> allConditions2 = new ArrayList<>();
        final List<ICondition2> firstAndConditionsGroup2 = new ArrayList<>();

        final Expression2 lrExpr = new Expression2(lastRoyaltySubqry);
        firstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2("lastRoyalty", sourceAuthor, metadata.get(TgAuthor.class).resolve("lastRoyalty"), lrExpr), ComparisonOperator.EQ, new EntValue2(1)));
        allConditions2.add(firstAndConditionsGroup2);
        final Conditions2 conditions = new Conditions2(false, allConditions2);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, QueryCategory.RESULT_QUERY, null);

        assertEquals(exp, qry2);
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
        EntQuery2 a = transform(select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().model()).as("ar").where().prop("authorship.author.surname").eq().val("Date").or().prop("ar.authorship.author.name.key").eq().val("Chris").model());
        System.out.println(a.getSources().getMain().props());
    }

    @Test
    public void test09() {
        EntQuery2 a = transform(select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().yield().prop("authorship.author").modelAsEntity(TgAuthorship.class)).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model());
        System.out.println(a.getSources().getMain().props());
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
    public void test_12() {
        final EntityResultQueryModel<TgVehicleModel> sourceModel1 = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
        final EntityResultQueryModel<TgVehicleModel> sourceModel2 = select(TgVehicleModel.class).where().prop("key").eq().val("317").model();
        transform(select(sourceModel1, sourceModel2).where().prop("key").in().values("316", "317").model());
    }

    @Test
    public void test_13() {
        EntQuery2 a = transform(select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2").model());
        System.out.println(a.getSources().getMain().props());
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
        transform(select(TgOrgUnit1.class).as("L1").where().exists(
                select(TgOrgUnit2.class).as("L2").where().prop("parent").eq().extProp("L1.id").and().exists(
                        select(TgOrgUnit3.class).as("L3").where().prop("parent").eq().extProp("L1.id").and().exists(
                                select(TgOrgUnit4.class).as("L4").where().prop("parent").eq().extProp("L1.id").and().exists(
                                        select(TgOrgUnit5.class).as("L5").where().prop("parent").eq().extProp("L1.id").and().prop("key").isNotNull().
                                                model()).
                                        model()).
                                model()).
                        model()).
                model());
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