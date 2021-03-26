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
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Test for GraphQL Web API implementation for boolean fields, their arguments with literals and variables.
 * 
 * @author TG Team
 *
 */
public class WebApiBooleanFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private void createBoolEntities() {
        save(new_(TgWebApiEntity.class, "VEH1").setActive(true));
        save(new_(TgWebApiEntity.class, "VEH2").setActive(false));
    }
    
    @Test
    public void boolean_prop_returns() {
        createBoolEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key active}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("active", true)),
                linkedMapOf(t2("key", "VEH2"), t2("active", false))
            ))
        )), result);
    }
    
    @Test
    public void boolean_prop_returns_with_null_argument_literal() {
        createBoolEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key active(value:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("active", true)),
                linkedMapOf(t2("key", "VEH2"), t2("active", false))
            ))
        )), result);
    }
    
    @Test
    public void boolean_prop_returns_with_non_empty_argument_literal() {
        createBoolEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key active(value:true)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("active", true))
            ))
        )), result);
    }
    
    @Test
    public void boolean_prop_returns_with_argument_variable() {
        createBoolEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Boolean){tgWebApiEntity{key active(value:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("active", true)),
                linkedMapOf(t2("key", "VEH2"), t2("active", false))
            ))
        )), result);
    }
    
    @Test
    public void boolean_prop_returns_with_null_argument_variable() {
        createBoolEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Boolean){tgWebApiEntity{key active(value:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("active", true)),
                linkedMapOf(t2("key", "VEH2"), t2("active", false))
            ))
        )), result);
    }
    
    @Test
    public void boolean_prop_returns_with_non_empty_argument_variable() {
        createBoolEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Boolean){tgWebApiEntity{key active(value:$val)}}", linkedMapOf(t2("val", true))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("active", true))
            ))
        )), result);
    }
    
}