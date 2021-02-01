package ua.com.fielden.platform.web_api.fields;

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
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Test for GraphQL Web API implementation for entity-typed fields, their arguments with literals and variables.
 * 
 * @author TG Team
 *
 */
public class WebApiEntityFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private void createEntities() {
        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleModel model = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(model));
        save(new_(TgWebApiEntity.class, "VEH2", "veh2 desc").setModel(model));
    }
    
    @Test
    public void entity_prop_returns() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model{key}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("model", linkedMapOf(t2("key", "316")))),
                linkedMapOf(t2("key", "VEH2"), t2("model", linkedMapOf(t2("key", "316"))))
            ))
        )), result);
    }
    
    @Test
    public void dot_notated_entity_prop_returns() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model{key make{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("model", linkedMapOf(t2("key", "316"), t2("make", linkedMapOf(t2("key", "MERC")))))),
                linkedMapOf(t2("key", "VEH2"), t2("model", linkedMapOf(t2("key", "316"), t2("make", linkedMapOf(t2("key", "MERC"))))))
            ))
        )), result);
    }
    
}