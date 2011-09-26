package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.transformation.SqlEntQuery;
import ua.com.fielden.platform.entity.query.model.transformation.Table;
import ua.com.fielden.platform.entity.query.model.transformation.TableColumn;
import ua.com.fielden.platform.entity.query.model.transformation.YieldedProp;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;


public class SqlQueryCompositionTest {

    @Test
    public void test_qry_sql1() {
	final Table eqdetTable = new Table("EQDET", TgVehicle.class);
	final TableColumn idCol = new TableColumn("id", "_ID", eqdetTable);
	final TableColumn versionCol = new TableColumn("version", "_VERSION", eqdetTable);
	final TableColumn keyCol = new TableColumn("key", "KEY_", eqdetTable);
	final TableColumn descCol = new TableColumn("desc", "DESC_", eqdetTable);

	final SqlEntQuery qry = new SqlEntQuery(eqdetTable);
	qry
	.yield(new YieldedProp(qry, idCol, "mvId"))
	.yield(new YieldedProp(qry, versionCol, "mvVersion"))
	.yield(new YieldedProp(qry, keyCol, "mvKey"))
	.yield(new YieldedProp(qry, descCol, "mvDesc"));

	assertEquals("Incorrect sql", "SELECT T1.DESC_ AS C1, T1._ID AS C2, T1.KEY_ AS C3, T1._VERSION AS C4 FROM EQDET AS T1", qry.querySql());
    }

    @Test
    public void test_qry_sql2() {
	final Table eqdetTable = new Table("EQDET", TgVehicle.class);
	final TableColumn idCol = new TableColumn("id", "_ID", eqdetTable);
	final TableColumn versionCol = new TableColumn("version", "_VERSION", eqdetTable);
	final TableColumn keyCol = new TableColumn("key", "KEY_", eqdetTable);
	final TableColumn descCol = new TableColumn("desc", "DESC_", eqdetTable);

	final SqlEntQuery qry = new SqlEntQuery(eqdetTable);
	qry
	.yield(new YieldedProp(qry, idCol, "mvId"))
	.yield(new YieldedProp(qry, versionCol, "mvVersion"))
	.yield(new YieldedProp(qry, keyCol, "mvKey"))
	.yield(new YieldedProp(qry, descCol, "mvDesc"));

	final SqlEntQuery qry2 = new SqlEntQuery(qry);
	qry2
	.yield(new YieldedProp(qry2, qry.getYields().get("mvId"), "mv2Id"))
	.yield(new YieldedProp(qry2, qry.getYields().get("mvVersion"), "mv2Version"))
	.yield(new YieldedProp(qry2, qry.getYields().get("mvKey"), "mv2Key"))
	.yield(new YieldedProp(qry2, qry.getYields().get("mvDesc"), "mv2Desc"));

	assertEquals("Incorrect sql", "SELECT Q1.C1 AS C1, Q1.C2 AS C2, Q1.C3 AS C3, Q1.C4 AS C4 FROM (SELECT T1.DESC_ AS C1, T1._ID AS C2, T1.KEY_ AS C3, T1._VERSION AS C4 FROM EQDET AS T1) AS Q1", qry2.querySql());
    }
}