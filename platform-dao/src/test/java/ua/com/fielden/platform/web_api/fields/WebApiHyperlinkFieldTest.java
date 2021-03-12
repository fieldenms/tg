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
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Test for GraphQL Web API implementation for Hyperlink fields.
 * 
 * @author TG Team
 *
 */
public class WebApiHyperlinkFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private final String hyperlink1 = "http://www.example.com";
    private final String hyperlink2 = "https://www.example.org";
    
    private void createHyperlinkEntities() {
        save(new_(TgWebApiEntity.class, "VEH1").setHyperlinkProp(new Hyperlink(hyperlink1)));
        save(new_(TgWebApiEntity.class, "VEH2").setHyperlinkProp(new Hyperlink(hyperlink2)));
    }
    
    @Test
    public void hyperlink_prop_returns() {
        createHyperlinkEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key hyperlinkProp}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("hyperlinkProp", hyperlink1)),
                linkedMapOf(t2("key", "VEH2"), t2("hyperlinkProp", hyperlink2))
            ))
        )), result);
    }
    
}