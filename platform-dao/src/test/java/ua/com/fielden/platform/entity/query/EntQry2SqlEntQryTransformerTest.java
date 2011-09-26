package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.transformation.SqlEntQuery;
import ua.com.fielden.platform.entity.query.model.transformation.Table;
import ua.com.fielden.platform.entity.query.model.transformation.TableColumn;
import ua.com.fielden.platform.entity.query.model.transformation.YieldedProp;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;

public class EntQry2SqlEntQryTransformerTest {
    @Test
    public void test_qry_sql1() {
	final EntityResultQueryModel<AbstractEntity> entQry = query.select(TgVehicle.class).model();
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

}
