package ua.com.fielden.platform.web_api.fields;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;
import static ua.com.fielden.platform.web_api.WebApiUtils.result;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Test for GraphQL Web API implementation for Long ID / version fields, their arguments with literals and variables.
 * 
 * @author TG Team
 *
 */
public class WebApiLongFieldIDAndVersionTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    // we take the most comprehensive examples for querying Long properties and these must be sufficient to test 'id' and 'version' support
    
    private List<Long> createIdAndVersionEntities() {
        final TgWebApiEntity veh1 = save(new_(TgWebApiEntity.class, "VEH1.0")); // version == 0
        final TgWebApiEntity veh2 = save(new_(TgWebApiEntity.class, "VEH2.0"));
        save(veh2.setKey("VEH2.1")); // version == 1
        final TgWebApiEntity veh3 = save(new_(TgWebApiEntity.class, "VEH3.0"));
        final TgWebApiEntity veh3_1 = save(veh3.setKey("VEH3.1"));
        save(veh3_1.setKey("VEH3.2")); // version == 2
        return asList(veh1.getId(), veh2.getId(), veh3.getId());
    }
    
    @Test
    public void id_prop_returns() {
        final List<Long> ids = createIdAndVersionEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Long){tgWebApiEntity{key id(from:$val)}}", linkedMapOf(t2("val", 0L))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1.0"), t2("id", ids.get(0))),
                linkedMapOf(t2("key", "VEH2.1"), t2("id", ids.get(1))),
                linkedMapOf(t2("key", "VEH3.2"), t2("id", ids.get(2)))
            ))
        )), result);
    }
    
    @Test
    public void version_prop_returns() {
        createIdAndVersionEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Long){tgWebApiEntity{key version(to:$val)}}", linkedMapOf(t2("val", 1L))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1.0"), t2("version", 0L)),
                linkedMapOf(t2("key", "VEH2.1"), t2("version", 1L))
            ))
        )), result);
    }
    
}