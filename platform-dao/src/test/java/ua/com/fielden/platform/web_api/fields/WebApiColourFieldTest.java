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

import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Test for GraphQL Web API implementation for Colour fields.
 * 
 * @author TG Team
 *
 */
public class WebApiColourFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private final String colour1 = "FFCC01";
    private final String colour2 = "AABBCC";
    
    private void createColourEntities() {
        save(new_(TgWebApiEntity.class, "VEH1").setColourProp(new Colour(colour1)));
        save(new_(TgWebApiEntity.class, "VEH2").setColourProp(new Colour(colour2)));
    }
    
    @Test
    public void colour_prop_returns() {
        createColourEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key colourProp}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("colourProp", "#" + colour1)),
                linkedMapOf(t2("key", "VEH2"), t2("colourProp", "#" + colour2))
            ))
        )), result);
    }
    
}