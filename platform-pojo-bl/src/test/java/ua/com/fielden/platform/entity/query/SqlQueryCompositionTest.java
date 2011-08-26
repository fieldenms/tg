package ua.com.fielden.platform.entity.query;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.domain.TgVehicle;
import ua.com.fielden.platform.entity.query.model.transformation.SqlEntQuery;
import ua.com.fielden.platform.entity.query.model.transformation.Table;
import ua.com.fielden.platform.entity.query.model.transformation.TableColumn;
import ua.com.fielden.platform.entity.query.model.transformation.YieldedProp;
import static org.junit.Assert.assertEquals;


public class SqlQueryCompositionTest {
    @Ignore
    @Test
    public void test_qry_sql1() {
	final Table eqdetTable = new Table("EQDET", TgVehicle.class);
	final TableColumn idCol = new TableColumn("id", "_ID", eqdetTable);
	final TableColumn versionCol = new TableColumn("version", "_VERSION", eqdetTable);
	final TableColumn keyCol = new TableColumn("key", "KEY_", eqdetTable);
	final TableColumn descCol = new TableColumn("desc", "DESC_", eqdetTable);

	final SqlEntQuery qry = new SqlEntQuery(eqdetTable);
	qry.getYields().put("mvId", new YieldedProp(qry, idCol, "mvId"));
	qry.getYields().put("mvVersion", new YieldedProp(qry, versionCol, "mvVersion"));
	qry.getYields().put("mvKey", new YieldedProp(qry, keyCol, "mvKey"));
	qry.getYields().put("mvDesc", new YieldedProp(qry, descCol, "mvDesc"));

	assertEquals("Incorrect sql", "SELECT T1.DESC_ AS C1, T1._ID AS C2, T1.KEY_ AS C3, T1._VERSION AS C4 FROM EQDET AS T1", qry.querySql());
    }

    @Ignore
    @Test
    public void test_qry_sql2() {
	final Table eqdetTable = new Table("EQDET", TgVehicle.class);
	final TableColumn idCol = new TableColumn("id", "_ID", eqdetTable);
	final TableColumn versionCol = new TableColumn("version", "_VERSION", eqdetTable);
	final TableColumn keyCol = new TableColumn("key", "KEY_", eqdetTable);
	final TableColumn descCol = new TableColumn("desc", "DESC_", eqdetTable);

	final SqlEntQuery qry = new SqlEntQuery(eqdetTable );
	qry.getYields().put("mvId", new YieldedProp(qry, idCol, "mvId"));
	qry.getYields().put("mvVersion", new YieldedProp(qry, versionCol, "mvVersion"));
	qry.getYields().put("mvKey", new YieldedProp(qry, keyCol, "mvKey"));
	qry.getYields().put("mvDesc", new YieldedProp(qry, descCol, "mvDesc"));

	final SqlEntQuery qry2 = new SqlEntQuery(qry);
	qry2.getYields().put("mv2Id", new YieldedProp(qry2, qry.getYields().get("mvId"), "mv2Id"));
	qry2.getYields().put("mv2Version", new YieldedProp(qry2, qry.getYields().get("mvVersion"), "mv2Version"));
	qry2.getYields().put("mv2Key", new YieldedProp(qry2, qry.getYields().get("mvKey"), "mv2Key"));
	qry2.getYields().put("mv2Desc", new YieldedProp(qry2, qry.getYields().get("mvDesc"), "mv2Desc"));

	assertEquals("Incorrect sql", "SELECT Q1.DESC_ AS C1, Q1._ID AS C2, Q1.KEY_ AS C3, Q1._VERSION AS C4 FROM (SELECT T1.DESC_ AS C1, T1._ID AS C2, T1.KEY_ AS C3, T1._VERSION AS C4 FROM EQDET AS T1) AS Q1", qry2.querySql());
    }

}
