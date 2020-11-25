package ua.com.fielden.platform.eql.stage2;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.HashMap;

import org.junit.Test;

import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.sources.QrySources2;

public class IgnoringConditionsWithNullArgumentsTest extends EqlStage2TestCase {

    @Test
    public void condition_is_correctly_ignored_01() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().prop(KEY).eq().iVal(null));
        final String model1 = "1";

        final QrySource2BasedOnPersistentType model = source(model1, MODEL);

        final QrySources2 sources = sources(model);
        final ResultQuery2 expQry = qryCountAll(sources);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void condition_is_correctly_ignored_02() {
        final HashMap<String,Object> paramValues = new HashMap<String, Object>();
        paramValues.put(KEY, null);
        
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().prop(KEY).eq().iParam("keyValue"), paramValues);
        final String model1 = "1";

        final QrySource2BasedOnPersistentType model = source(model1, MODEL);

        final QrySources2 sources = sources(model);
        final ResultQuery2 expQry = qryCountAll(sources);
        
        assertEquals(expQry, actQry);
    }
}