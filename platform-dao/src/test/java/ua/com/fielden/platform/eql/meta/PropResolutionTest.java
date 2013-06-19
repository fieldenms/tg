package ua.com.fielden.platform.eql.meta;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.s1.elements.Expression1;
import ua.com.fielden.platform.eql.s2.elements.EntProp2;
import ua.com.fielden.platform.eql.s2.elements.EntQuery2;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorRoyalty;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgPersonName;
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
	tgPersonName.getProps().put("id", new PrimTypePropInfo("id", tgPersonName, Long.class, null));
	tgPersonName.getProps().put("key", new PrimTypePropInfo("key", tgPersonName, String.class, null));
	tgAuthor.getProps().put("id", new PrimTypePropInfo("id", tgAuthor, Long.class, null));

	final Expression1 expr = entQryExpression(expr().model(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("id").model()).model());
	// TODO EQL
	//final Expression1 expr = entQryExpression(expr().model(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("author").model()).model());
	tgAuthor.getProps().put("lastRoyalty", new EntityTypePropInfo("lastRoyalty", tgAuthor, tgAuthorRoyalty, expr));
	tgAuthor.getProps().put("key", new PrimTypePropInfo("key", tgAuthor, String.class, null));
	tgAuthor.getProps().put("name", new EntityTypePropInfo("name", tgAuthor, tgPersonName, null));
	tgAuthor.getProps().put("surname", new PrimTypePropInfo("surname", tgAuthor, String.class, null));
	tgAuthorship.getProps().put("id", new PrimTypePropInfo("id", tgAuthorship, Long.class, null));
	tgAuthorship.getProps().put("key", new PrimTypePropInfo("key", tgAuthorship, String.class, null));
	tgAuthorship.getProps().put("author", new EntityTypePropInfo("author", tgAuthorship, tgAuthor, null));
	tgAuthorship.getProps().put("bookTitle", new PrimTypePropInfo("bookTitle", tgAuthorship, String.class, null));
	tgAuthorRoyalty.getProps().put("id", new PrimTypePropInfo("id", tgAuthorRoyalty, Long.class, null));
	tgAuthorRoyalty.getProps().put("authorship", new EntityTypePropInfo("authorship", tgAuthorRoyalty, tgAuthorship, null));
	tgAuthorRoyalty.getProps().put("paymentDate", new PrimTypePropInfo("paymentDate", tgAuthorRoyalty, Date.class, null));
	metadata.put(TgPersonName.class, tgPersonName);
	metadata.put(TgAuthor.class, tgAuthor);
	metadata.put(TgAuthorship.class, tgAuthorship);
	metadata.put(TgAuthorRoyalty.class, tgAuthorRoyalty);
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

    @Test
    public void test0() {
	final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().prop("lastRoyalty").isNotNull().model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));

	//final EntQuery2 exp = new EntQuery2(false, null, TgAuthor.class, null, null, null, null, null, null, null);



    }

    @Test
    public void test0b() {
	final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn.id").where().prop("lastRoyalty").isNotNull().model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    @Ignore
    public void test0c() {
	final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).as("pn").where().prop("lastRoyalty").isNotNull().model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    @Ignore
    public void test0a() {
	final EntityResultQueryModel<TgAuthorship> qry = select(TgAuthorship.class).where().exists(select(TgAuthor.class).where().prop("lastRoyalty").isNotNull().model()).model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    public void test1() {
	final EntityResultQueryModel<TgAuthorship> qry = select(TgAuthorship.class).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    public void test2() {
	final EntityResultQueryModel<TgAuthorRoyalty> qry = select(TgAuthorRoyalty.class).as("ar").where().prop("authorship.author.surname").eq().val("Date").or().prop("ar.authorship.author.name.key").eq().val("Chris").model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    public void test2a() {
	final EntityResultQueryModel<TgAuthor> qry = select(TgAuthor.class).where().exists(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("id").model()).model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    public void test3() {
	final EntityResultQueryModel<TgAuthorRoyalty> qry = select(TgAuthorRoyalty.class).as("ar").where().exists(
		select(TgAuthorship.class).where().prop("id").eq().extProp("authorship").and().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model()).model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    public void test4() {
	final EntityResultQueryModel<TgAuthorship> qry = select(select(TgAuthorship.class).where().prop("bookTitle").isNotNull().model()).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    public void test5() {
	final EntityResultQueryModel<TgAuthorRoyalty> qry = select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().model()).as("ar").where().prop("authorship.author.surname").eq().val("Date").or().prop("ar.authorship.author.name.key").eq().val("Chris").model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
    }

    @Test
    public void test6() {
	final EntityResultQueryModel<TgAuthorship> qry = select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().yield().prop("authorship.author").modelAsEntity(TgAuthorship.class)).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
	System.out.println("---------");
	for (final EntProp2 prop : qry2.getSources().getMain().props()) {
	    System.out.println("---: " + prop);
	}
    }

    @Test
    public void test7() {
	final AggregatedResultQueryModel qry = select(select(TgAuthorship.class).where().prop("bookTitle").isNotNull().yield().prop("author").as("author").yield().prop("bookTitle").as("title").modelAsAggregate()).where().prop("title").isNotNull().or().begin().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").end().yield().prop("author.name.key").as("name").yield().prop("title").as("titel").modelAsAggregate();
	final EntQuery2 qry2 = entResultQry2(qry, new TransformatorToS2(metadata, Collections.EMPTY_MAP, DOMAIN_METADATA));
	System.out.println("---------");
	for (final EntProp2 prop : qry2.getSources().getMain().props()) {
	    System.out.println("---: " + prop);
	}
    }

    @Test
    @Ignore
    public void test8() {
    }
}