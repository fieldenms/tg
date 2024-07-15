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
 * Test for GraphQL Web API implementation for fragments and aliases.
 * 
 * @author TG Team
 *
 */
public class WebApiFragmentsAndAliasesTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private void createEntities() {
        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleModel model = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(model));
        save(new_(TgWebApiEntity.class, "VEH2", "veh2 desc").setModel(model));
    }
    
    @Test
    public void named_fragments_and_aliases_work() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{vehs1:tgWebApiEntity{...keyAndDesc}vehs2:tgWebApiEntity{...keyAndDesc}}fragment keyAndDesc on TgWebApiEntity{key desc}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("vehs1", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("desc", "veh1 desc")),
                linkedMapOf(t2("key", "VEH2"), t2("desc", "veh2 desc"))
            )),
            t2("vehs2", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("desc", "veh1 desc")),
                linkedMapOf(t2("key", "VEH2"), t2("desc", "veh2 desc"))
            ))
        )), result);
    }
    
    @Test
    public void inline_fragments_work() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{... on TgWebApiEntity{key desc}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("desc", "veh1 desc")),
                linkedMapOf(t2("key", "VEH2"), t2("desc", "veh2 desc"))
            ))
        )), result);
    }
    
}