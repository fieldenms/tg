package ua.com.fielden.platform.web_api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;
import static ua.com.fielden.platform.web_api.WebApiUtils.result;

import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * Test for GraphQL Web API implementation for standard @include & @skip directives.
 * 
 * @author TG Team
 *
 */
public class WebApiStandardDirectivesTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private void createEntities() {
        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleModel model = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(model));
        save(new_(TgWebApiEntity.class, "VEH2", "veh2 desc").setModel(model));
    }
    
    @Test
    public void skip_directive_works_with_argument_literal() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key desc(like:\"veh2%desc\") @skip(if:true) }}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void include_directive_works_with_argument_variable() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("query ($val: Boolean!) {tgWebApiEntity{key desc(like:\"veh2%desc\") @include(if:$val) }}", linkedMapOf(t2("val", false))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
}