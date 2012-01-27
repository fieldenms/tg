package ua.com.fielden.platform.entity.query.model.elements;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.BaseEntQueryTCase;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QuerySourcesEnhancementTest extends BaseEntQueryTCase {

    @Test
    public void test_prop_to_source_association13() {
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(VEHICLE). //
	where().prop("model.make.key").eq().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(VEHICLE). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association14() {
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(VEHICLE). //
	where().prop("station.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(VEHICLE). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	leftJoin(ORG5).as("station").on().prop("station").eq().prop("station.id"). //
	where().prop("station.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association15() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("station.key").eq().val("AA").model();
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(sourceQry). //
	where().prop("model.make.key").eq().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(sourceQry). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association16() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("station.key").eq().val("AA").model();
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(sourceQry). //
	leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
	where().prop("s.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(sourceQry). //
	leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("s.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association17() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("station.key").eq().val("AA").model();
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(sourceQry). //
	leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
	where().prop("s.parent.key").like().val("AA%").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(sourceQry). //
	leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
	leftJoin(ORG4).as("s.parent").on().prop("s.parent").eq().prop("s.parent.id"). //
	where().prop("s.parent.key").like().val("AA%").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }


    @Test
    public void test_prop_to_source_association18() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("station.key").eq().val("AA").model();
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(sourceQry). //
	leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
	where().prop("s.parent.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(sourceQry). //
	leftJoin(ORG5).as("s").on().prop("station").eq().prop("s"). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	leftJoin(ORG4).as("s.parent").on().prop("s.parent").eq().prop("s.parent.id"). //
	where().prop("s.parent.key").like().val("AA%").and().prop("model.make.key").eq().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association19() {
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(VEHICLE). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	where().prop("model.make.key").eq().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(VEHICLE). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association20() {
	// demonstrate how user can misuse explicit joining and get incorrect result (used leftJoin instead of innerJoin) in case of vice-verse the result will be even worse - incomplete result set
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(VEHICLE). //
	leftJoin(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	where().prop("model.make.key").eq().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(VEHICLE). //
	leftJoin(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association21() {
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(VEHICLE). //
	leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
	where().prop("model.make.key").eq().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(VEHICLE). //
	leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association22() {
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(VEHICLE). //
	leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
	where().prop("model.make.key").eq().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(VEHICLE). //
	leftJoin(VEHICLE).as("replacedBy").on().prop("replacedBy").eq().prop("replacedBy.id"). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association23() {
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(VEHICLE). //
	leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
	where().prop("model.make.key").eq().val("MERC").and().prop("rv.model.make.key").ne().val("MERC").model();

	final EntityResultQueryModel<TgVehicle> explicitQry = select(VEHICLE). //
	leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	leftJoin(MODEL).as("rv.model").on().prop("rv.model").eq().prop("rv.model.id"). //
	leftJoin(MAKE).as("rv.model.make").on().prop("rv.model.make").eq().prop("rv.model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").and().prop("rv.model.make.key").ne().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }

    @Test
    public void test_prop_to_source_association24() {
	final EntityResultQueryModel<TgVehicle> shortcutQry = select(VEHICLE). //
	leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
	join(VEHICLE).as("rv2").on().prop("rv.replacedBy").eq().prop("rv2"). //
	where().prop("model.make.key").eq().val("MERC").and().prop("rv2.model.make.key").ne().val("MERC").model();
	// this illustrates the case of records being ate by explicit IJ after explicit LJ and how implicit joins are generated for (sub)properties of explicit IJ (aliased as rv2)
	final EntityResultQueryModel<TgVehicle> explicitQry = select(VEHICLE). //
	leftJoin(VEHICLE).as("rv").on().prop("replacedBy").eq().prop("rv"). //
	join(VEHICLE).as("rv2").on().prop("rv.replacedBy").eq().prop("rv2"). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	leftJoin(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	join(MODEL).as("rv2.model").on().prop("rv2.model").eq().prop("rv2.model.id"). //
	leftJoin(MAKE).as("rv2.model.make").on().prop("rv2.model.make").eq().prop("rv2.model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").and().prop("rv2.model.make.key").ne().val("MERC").model();

	assertEquals("Incorrect list of unresolved props", entQuery1(explicitQry), entQuery1(shortcutQry));
    }
}