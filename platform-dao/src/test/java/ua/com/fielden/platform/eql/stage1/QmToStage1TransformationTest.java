package ua.com.fielden.platform.eql.stage1;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlStage1TestCase;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.queries.ResultQuery1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;

public class QmToStage1TransformationTest extends EqlStage1TestCase {
    
    @Test
    public void test01() {
        final EntityResultQueryModel<TeVehicleModel> qry = select(MODEL).where().prop("make.key").isNotNull().model();
        
        final IJoinNode1<? extends IJoinNode2<?>> sources1 = sources(MODEL);
        final Conditions1 conditions1 = conditions(isNotNull(prop("make.key")));
        final QueryComponents1 parts1 = qc1(sources1, conditions1);
        final ResultQuery1 expQry1 = new ResultQuery1(parts1, MODEL, null);

        assertEquals(expQry1, resultQry(qry));
    }
    
    @Test
    public void test02() {
        final EntityResultQueryModel<TeVehicleModel> qry = select(MODEL).where().prop("make").isNotNull().model();
        
        final IJoinNode1<? extends IJoinNode2<?>> sources1 = sources(MODEL);
        final Conditions1 conditions1 = conditions(isNotNull(prop("make")));
        final QueryComponents1 parts1 = qc1(sources1, conditions1);
        final ResultQuery1 expQry1 = new ResultQuery1(parts1, MODEL, null);

        assertEquals(expQry1, resultQry(qry));
    }
}