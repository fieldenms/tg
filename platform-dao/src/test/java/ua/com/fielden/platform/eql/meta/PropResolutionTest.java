package ua.com.fielden.platform.eql.meta;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase1;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorRoyalty;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieClass;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class PropResolutionTest extends BaseEntQueryTCase1 {

    @Test
    public void test() {
	final EntityInfo tgPersonName = new EntityInfo(TgPersonName.class);
	final EntityInfo tgAuthor = new EntityInfo(TgAuthor.class);
	final EntityInfo tgAuthorship = new EntityInfo(TgAuthorship.class);
	final EntityInfo tgAuthorRoyalty = new EntityInfo(TgAuthorRoyalty.class);
	final EntityInfo tgBogie = new EntityInfo(TgBogie.class);
	final EntityInfo tgBogieClass = new EntityInfo(TgBogieClass.class);
	final EntityInfo tgWagon = new EntityInfo(TgWagon.class);
	final EntityInfo tgWagonSlot = new EntityInfo(TgWagonSlot.class);
	final EntityInfo tgWagonClass = new EntityInfo(TgWagonClass.class);
	final EntityInfo tgWagonClassCompatibility = new EntityInfo(TgWagonClassCompatibility.class);
	final EntityInfo tgWorkshop = new EntityInfo(TgWorkshop.class);
	final EntityInfo tgTimesheet = new EntityInfo(TgTimesheet.class);
	final EntityInfo tgVehicle = new EntityInfo(TgVehicle.class);
	final EntityInfo tgVehicleFinDetails = new EntityInfo(TgVehicleFinDetails.class);
	final EntityInfo tgVehicleModel = new EntityInfo(TgVehicleModel.class);
	final EntityInfo tgVehicleMake = new EntityInfo(TgVehicleMake.class);
	final EntityInfo tgOrgUnit1 = new EntityInfo(TgOrgUnit1.class);
	final EntityInfo tgOrgUnit2 = new EntityInfo(TgOrgUnit2.class);
	final EntityInfo tgOrgUnit3 = new EntityInfo(TgOrgUnit3.class);
	final EntityInfo tgOrgUnit4 = new EntityInfo(TgOrgUnit4.class);
	final EntityInfo tgOrgUnit5 = new EntityInfo(TgOrgUnit5.class);
	final EntityInfo tgWorkOrder = new EntityInfo(TgWorkOrder.class);
	final EntityInfo tgFuelUsage = new EntityInfo(TgFuelUsage.class);
	final EntityInfo tgFuelType = new EntityInfo(TgFuelType.class);
	final EntityInfo tgModelCount = new EntityInfo(TgModelCount.class);
	final EntityInfo tgModelYearCount = new EntityInfo(TgModelYearCount.class);
	final EntityInfo tgAverageFuelUsage = new EntityInfo(TgAverageFuelUsage.class);

	tgPersonName.getProps().put("id", new PrimTypePropInfo("id", tgPersonName, Long.class));
	tgPersonName.getProps().put("key", new PrimTypePropInfo("key", tgPersonName, String.class));
	tgAuthor.getProps().put("id", new PrimTypePropInfo("id", tgAuthor, Long.class));
	tgAuthor.getProps().put("key", new PrimTypePropInfo("key", tgAuthor, String.class));
	tgAuthor.getProps().put("name", new EntityTypePropInfo("name", tgAuthor, tgPersonName));
	tgAuthor.getProps().put("surname", new PrimTypePropInfo("surname", tgAuthor, String.class));
	tgAuthorship.getProps().put("id", new PrimTypePropInfo("id", tgAuthorship, Long.class));
	tgAuthorship.getProps().put("key", new PrimTypePropInfo("key", tgAuthorship, String.class));
	tgAuthorship.getProps().put("author", new EntityTypePropInfo("author", tgAuthorship, tgAuthor));
	tgAuthorship.getProps().put("bookTitle", new PrimTypePropInfo("bookTitle", tgAuthorship, String.class));
	tgAuthorRoyalty.getProps().put("id", new PrimTypePropInfo("id", tgAuthorRoyalty, Long.class));
	tgAuthorRoyalty.getProps().put("authorship", new EntityTypePropInfo("authorship", tgAuthorRoyalty, tgAuthorship));
	tgAuthorRoyalty.getProps().put("paymentDate", new PrimTypePropInfo("paymentDate", tgAuthorRoyalty, Date.class));

	final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata = new HashMap<>();
	metadata.put(TgPersonName.class, tgPersonName);
	metadata.put(TgAuthor.class, tgAuthor);
	metadata.put(TgAuthorship.class, tgAuthorship);
	metadata.put(TgAuthorRoyalty.class, tgAuthorRoyalty);

//	final EntityResultQueryModel<TgAuthorship> qry = select(TgAuthorship.class).
//	where().beginExpr().val(100).mult().model(
//		select(FUEL_USAGE).yield().sumOf().prop("qty").modelAsPrimitive()
//		).endExpr().ge().val(1000).model();

	System.out.println(tgAuthorship.resolve("author1"));
	System.out.println(tgAuthorship.resolve("author1.name"));
	System.out.println(tgAuthorship.resolve("author.name.key1"));
	System.out.println(tgAuthorship.resolve("author.name1"));
	System.out.println("-----------------------------------");
	System.out.println(tgAuthorship.resolve("key"));
	System.out.println(tgAuthorship.resolve("author"));
	System.out.println(tgAuthorship.resolve("author.name"));
	System.out.println(tgAuthorship.resolve("author.name.key"));
	System.out.println(tgAuthorship.resolve("author.surname"));
	System.out.println(tgAuthorship.resolve("bookTitle"));
	System.out.println("-----------------------------------");
	System.out.println(tgAuthor.resolve("key"));
	System.out.println(tgAuthor.resolve("name"));
	System.out.println(tgAuthor.resolve("name.key"));
	System.out.println(tgAuthor.resolve("surname"));

	System.out.println(tgAuthorRoyalty.resolve("authorship.bookTitle"));
	System.out.println(tgAuthorRoyalty.resolve("authorship.author.surname"));
	System.out.println(tgAuthorRoyalty.resolve("authorship.author.name.key"));
	System.out.println(tgAuthorRoyalty.resolve("paymentDate"));

	System.out.println("-----------------------------------");
	final EntityResultQueryModel<TgAuthorship> qry = select(TgAuthorship.class).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model();
	entResultQry2(qry, new TransformatorToS2(metadata));
	System.out.println("-----------------------------------");
	final EntityResultQueryModel<TgAuthorRoyalty> qry2 = select(TgAuthorRoyalty.class).as("ar").where().prop("authorship.author.surname").eq().val("Date").or().prop("ar.authorship.author.name.key").eq().val("Chris").model();
	entResultQry2(qry2, new TransformatorToS2(metadata));
	System.out.println("-----------------------------------");
	final EntityResultQueryModel<TgAuthorRoyalty> qry3 = select(TgAuthorRoyalty.class).as("ar").where().exists(
		select(TgAuthorship.class).where().prop("id").eq().extProp("authorship").and().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model()).model();
	entResultQry2(qry3, new TransformatorToS2(metadata));

    }
}