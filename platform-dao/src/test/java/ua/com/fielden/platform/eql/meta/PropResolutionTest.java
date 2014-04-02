package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.s1.elements.Expression1;
import ua.com.fielden.platform.eql.s2.elements.ComparisonTest2;
import ua.com.fielden.platform.eql.s2.elements.CompoundSource2;
import ua.com.fielden.platform.eql.s2.elements.Conditions2;
import ua.com.fielden.platform.eql.s2.elements.EntProp2;
import ua.com.fielden.platform.eql.s2.elements.EntQuery2;
import ua.com.fielden.platform.eql.s2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.s2.elements.EntValue2;
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
import ua.com.fielden.platform.sample.domain.TgPersonName;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class PropResolutionTest extends BaseEntQueryTCase1 {

    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata = new HashMap<>();

    @Before
    public void setUp() {
        final EntityInfo tgPersonName = new EntityInfo(TgPersonName.class);
        final EntityInfo tgAuthor = new EntityInfo(TgAuthor.class);
        final EntityInfo tgAuthorship = new EntityInfo(TgAuthorship.class);
        final EntityInfo tgAuthorRoyalty = new EntityInfo(TgAuthorRoyalty.class);

        new PrimTypePropInfo("id", tgPersonName, Long.class, null);
        new PrimTypePropInfo("key", tgPersonName, String.class, null);
        new PrimTypePropInfo("id", tgAuthor, Long.class, null);

        final Expression1 expr = entQryExpression(expr().model(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("id").model()).model());
        // TODO EQL final Expression1 expr = entQryExpression(expr().model(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("author").model()).model());
        new EntityTypePropInfo("lastRoyalty", tgAuthor, tgAuthorRoyalty, expr);
        new PrimTypePropInfo("key", tgAuthor, String.class, null);
        new EntityTypePropInfo("name", tgAuthor, tgPersonName, null);
        new PrimTypePropInfo("surname", tgAuthor, String.class, null);

        new PrimTypePropInfo("id", tgAuthorship, Long.class, null);
        new PrimTypePropInfo("key", tgAuthorship, String.class, null);
        new EntityTypePropInfo("author", tgAuthorship, tgAuthor, null);
        new PrimTypePropInfo("bookTitle", tgAuthorship, String.class, null);

        new PrimTypePropInfo("id", tgAuthorRoyalty, Long.class, null);
        new EntityTypePropInfo("authorship", tgAuthorRoyalty, tgAuthorship, null);
        new PrimTypePropInfo("paymentDate", tgAuthorRoyalty, Date.class, null);

        metadata.put(TgPersonName.class, tgPersonName);
        metadata.put(TgAuthor.class, tgAuthor);
        metadata.put(TgAuthorship.class, tgAuthorship);
        metadata.put(TgAuthorRoyalty.class, tgAuthorRoyalty);

        //	final Set<Class<? extends AbstractEntity<?>>> entities = new HashSet<>();
        //	entities.add(TgPersonName.class);
        //	entities.add(TgAuthor.class);
        //	entities.add(TgAuthorship.class);
        //	entities.add(TgAuthorRoyalty.class);
        //
        //
        //	metadata.putAll(MetadataGenerator.generate(entities));

        System.out.println("--------------------------------------------------- START ---------------------------------------------------");
    }

    @Test
    @Ignore
    public void test() {
        //	final EntityInfo tgBogie = new EntityInfo(TgBogie.class);
        //	final EntityInfo tgBogieClass = new EntityInfo(TgBogieClass.class);
        //	final EntityInfo tgWagon = new EntityInfo(TgWagon.class);
        //	final EntityInfo tgWagonSlot = new EntityInfo(TgWagonSlot.class);
        //	final EntityInfo tgWagonClass = new EntityInfo(TgWagonClass.class);
        //	final EntityInfo tgWagonClassCompatibility = new EntityInfo(TgWagonClassCompatibility.class);
        //	final EntityInfo tgWorkshop = new EntityInfo(TgWorkshop.class);
        //	final EntityInfo tgTimesheet = new EntityInfo(TgTimesheet.class);
        //	final EntityInfo tgVehicle = new EntityInfo(TgVehicle.class);
        //	final EntityInfo tgVehicleFinDetails = new EntityInfo(TgVehicleFinDetails.class);
        //	final EntityInfo tgVehicleModel = new EntityInfo(TgVehicleModel.class);
        //	final EntityInfo tgVehicleMake = new EntityInfo(TgVehicleMake.class);
        //	final EntityInfo tgOrgUnit1 = new EntityInfo(TgOrgUnit1.class);
        //	final EntityInfo tgOrgUnit2 = new EntityInfo(TgOrgUnit2.class);
        //	final EntityInfo tgOrgUnit3 = new EntityInfo(TgOrgUnit3.class);
        //	final EntityInfo tgOrgUnit4 = new EntityInfo(TgOrgUnit4.class);
        //	final EntityInfo tgOrgUnit5 = new EntityInfo(TgOrgUnit5.class);
        //	final EntityInfo tgWorkOrder = new EntityInfo(TgWorkOrder.class);
        //	final EntityInfo tgFuelUsage = new EntityInfo(TgFuelUsage.class);
        //	final EntityInfo tgFuelType = new EntityInfo(TgFuelType.class);
        //	final EntityInfo tgModelCount = new EntityInfo(TgModelCount.class);
        //	final EntityInfo tgModelYearCount = new EntityInfo(TgModelYearCount.class);
        //	final EntityInfo tgAverageFuelUsage = new EntityInfo(TgAverageFuelUsage.class);

        //	final EntityResultQueryModel<TgAuthorship> qry = select(TgAuthorship.class).
        //	where().beginExpr().val(100).mult().model(
        //		select(FUEL_USAGE).yield().sumOf().prop("qty").modelAsPrimitive()
        //		).endExpr().ge().val(1000).model();

        //	System.out.println(tgAuthorship.resolve("author1"));
        //	System.out.println(tgAuthorship.resolve("author1.name"));
        //	System.out.println(tgAuthorship.resolve("author.name.key1"));
        //	System.out.println(tgAuthorship.resolve("author.name1"));
        //	System.out.println("-----------------------------------");
        //	System.out.println(tgAuthorship.resolve("key"));
        //	System.out.println(tgAuthorship.resolve("author"));
        //	System.out.println(tgAuthorship.resolve("author.name"));
        //	System.out.println(tgAuthorship.resolve("author.name.key"));
        //	System.out.println(tgAuthorship.resolve("author.surname"));
        //	System.out.println(tgAuthorship.resolve("bookTitle"));
        //	System.out.println("-----------------------------------");
        //	System.out.println(tgAuthor.resolve("key"));
        //	System.out.println(tgAuthor.resolve("name"));
        //	System.out.println(tgAuthor.resolve("name.key"));
        //	System.out.println(tgAuthor.resolve("surname"));
        //
        //	System.out.println(tgAuthorRoyalty.resolve("authorship.bookTitle"));
        //	System.out.println(tgAuthorRoyalty.resolve("authorship.author.surname"));
        //	System.out.println(tgAuthorRoyalty.resolve("authorship.author.name.key"));
        //	System.out.println(tgAuthorRoyalty.resolve("paymentDate"));
    }

    //TODO EQL transfer to S2 tests
    //  @Test
    //  public void test_user_data_filtering() {
    //	assertModelsEqualsAccordingUserDataFiltering(//
    //		select(VEHICLE). //
    //		where().prop("model.make.key").eq().val("MERC").model(),
    //
    //		select(VEHICLE). //
    //		where().begin().prop("key").notLike().val("A%").end().and().begin().prop("model.make.key").eq().val("MERC").end().model());
    //  }

    @Test
    public void test0() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("lastRoyalty").isNotNull().model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
    }

    @Test
    public void test_1() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().or().prop("surname").eq().iVal(null).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, new SimpleUserFilter(), null, qb));

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
        //System.out.println(qry2.getConditions());
        assertEquals(qry2, exp);
    }

    @Test
    public void test_q1() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));

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
    public void test_q2() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).as("a").where().prop("a.surname").isNotNull().model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));

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
    public void test_q3() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().and().prop("name").eq().iVal(null).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));

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
    public void test_q4() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").isNotNull().and().prop("name").eq().iParam("param").model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));

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
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("surname").eq().param("param").model();
        final Map<String, Object> params = new HashMap<>();
        params.put("param", 1);
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, params, DOMAIN_METADATA, null, null, qb));

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
    public void test0b() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn.id"). //
        where().prop("surname").eq().val(1).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));

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
    @Ignore
    public void test0b1() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn.id"). //
        where().prop("lastRoyalty").eq().val(1).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));

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
        firstAndConditionsGroup2.add(new ComparisonTest2(new EntProp2("lastRoyalty", source, metadata.get(TgAuthor.class).resolve("lastRoyalty"), null), ComparisonOperator.EQ, new EntValue2(1)));
        allConditions2.add(firstAndConditionsGroup2);
        final Conditions2 conditions = new Conditions2(false, allConditions2);

        final EntQueryBlocks2 parts = new EntQueryBlocks2(sources, conditions, new Yields2(), new GroupBys2(Collections.<GroupBy2> emptyList()), new OrderBys2(null));
        final EntQuery2 exp = new EntQuery2(parts, TgAuthor.class, QueryCategory.RESULT_QUERY, null);
        assertEquals(exp, qry2);

    }

    @Test
    //@Ignore
    public void test0c() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).as("pn").where().prop("lastRoyalty").isNotNull().model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
    }

    @Test
    @Ignore
    public void test0a() {
        final EntityResultQueryModel<TgAuthorship> qry = select(TgAuthorship.class).where().exists(select(TgAuthor.class).where().prop("lastRoyalty").isNotNull().model()).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
    }

    @Test
    public void test1() {
        final EntityResultQueryModel<TgAuthorship> qry = select(TgAuthorship.class).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
    }

    @Test
    public void test2() {
        final EntityResultQueryModel<TgAuthorRoyalty> qry = select(TgAuthorRoyalty.class).as("ar").where().prop("authorship.author.surname").eq().val("Date").or().prop("ar.authorship.author.name.key").eq().val("Chris").model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
    }

    @Test
    public void test2a() {
        final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().exists(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("id").model()).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
    }

    @Test
    public void test3() {
        final EntityResultQueryModel<TgAuthorRoyalty> qry = select(TgAuthorRoyalty.class).as("ar").where().exists(select(TgAuthorship.class).where().prop("id").eq().extProp("authorship").and().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model()).model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
    }

    @Test
    public void test4() {
        final EntityResultQueryModel<TgAuthorship> qry = select(select(TgAuthorship.class).where().prop("bookTitle").isNotNull().model()).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
    }

    @Test
    public void test5() {
        final EntityResultQueryModel<TgAuthorRoyalty> qry = select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().model()).as("ar").where().prop("authorship.author.surname").eq().val("Date").or().prop("ar.authorship.author.name.key").eq().val("Chris").model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
    }

    @Test
    public void test6() {
        final EntityResultQueryModel<TgAuthorship> qry = select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().yield().prop("authorship.author").modelAsEntity(TgAuthorship.class)).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
        for (final EntProp2 prop : qry2.getSources().getMain().props()) {
            System.out.println("---: " + prop);
        }
    }

    @Test
    public void test7() {
        final AggregatedResultQueryModel qry = select(select(TgAuthorship.class).where().prop("bookTitle").isNotNull().yield().prop("author").as("author").yield().prop("bookTitle").as("title").modelAsAggregate()).where().prop("title").isNotNull().or().begin().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").end().yield().prop("author.name.key").as("name").yield().prop("title").as("titel").modelAsAggregate();
        final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA, null, null, qb));
        for (final EntProp2 prop : qry2.getSources().getMain().props()) {
            System.out.println("---: " + prop);
        }
    }
}