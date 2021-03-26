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
 * Test for GraphQL Web API implementation for Long fields, their arguments with literals and variables.
 * 
 * @author TG Team
 *
 */
public class WebApiLongFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private void createLongEntities() {
        save(new_(TgWebApiEntity.class, "VEH1").setLongProp(0L));
        save(new_(TgWebApiEntity.class, "VEH2").setLongProp(5L));
        save(new_(TgWebApiEntity.class, "VEH3").setLongProp(10L));
    }
    
    @Test
    public void long_prop_returns() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key longProp}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", 0L)),
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L)),
                linkedMapOf(t2("key", "VEH3"), t2("longProp", 10L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_left_null_argument_literal() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key longProp(from:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", 0L)),
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L)),
                linkedMapOf(t2("key", "VEH3"), t2("longProp", 10L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_right_null_argument_literal() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key longProp(to:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", 0L)),
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L)),
                linkedMapOf(t2("key", "VEH3"), t2("longProp", 10L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_left_non_empty_argument_literal() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key longProp(from:2)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L)),
                linkedMapOf(t2("key", "VEH3"), t2("longProp", 10L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_right_non_empty_argument_literal() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key longProp(to:8)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", 0L)),
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_left_argument_variable() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Long){tgWebApiEntity{key longProp(from:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", 0L)),
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L)),
                linkedMapOf(t2("key", "VEH3"), t2("longProp", 10L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_right_argument_variable() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Long){tgWebApiEntity{key longProp(to:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", 0L)),
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L)),
                linkedMapOf(t2("key", "VEH3"), t2("longProp", 10L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_left_null_argument_variable() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Long){tgWebApiEntity{key longProp(from:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", 0L)),
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L)),
                linkedMapOf(t2("key", "VEH3"), t2("longProp", 10L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_right_null_argument_variable() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Long){tgWebApiEntity{key longProp(to:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", 0L)),
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L)),
                linkedMapOf(t2("key", "VEH3"), t2("longProp", 10L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_left_non_empty_argument_variable() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Long){tgWebApiEntity{key longProp(from:$val)}}", linkedMapOf(t2("val", 2))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L)),
                linkedMapOf(t2("key", "VEH3"), t2("longProp", 10L))
            ))
        )), result);
    }
    
    @Test
    public void long_prop_returns_with_right_non_empty_argument_variable() {
        createLongEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Long){tgWebApiEntity{key longProp(to:$val)}}", linkedMapOf(t2("val", 8))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", 0L)),
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5L))
            ))
        )), result);
    }
    
}