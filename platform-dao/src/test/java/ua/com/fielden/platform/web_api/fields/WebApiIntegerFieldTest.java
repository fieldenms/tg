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
 * Test for GraphQL Web API implementation for Integer fields, their arguments with literals and variables.
 * 
 * @author TG Team
 *
 */
public class WebApiIntegerFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private void createIntEntities() {
        save(new_(TgWebApiEntity.class, "VEH1").setIntProp(0));
        save(new_(TgWebApiEntity.class, "VEH2").setIntProp(5));
        save(new_(TgWebApiEntity.class, "VEH3").setIntProp(10));
    }
    
    @Test
    public void integer_prop_returns() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key intProp}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("intProp", 0)),
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5)),
                linkedMapOf(t2("key", "VEH3"), t2("intProp", 10))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_left_null_argument_literal() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key intProp(from:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("intProp", 0)),
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5)),
                linkedMapOf(t2("key", "VEH3"), t2("intProp", 10))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_right_null_argument_literal() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key intProp(to:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("intProp", 0)),
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5)),
                linkedMapOf(t2("key", "VEH3"), t2("intProp", 10))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_left_non_empty_argument_literal() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key intProp(from:2)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5)),
                linkedMapOf(t2("key", "VEH3"), t2("intProp", 10))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_right_non_empty_argument_literal() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key intProp(to:8)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("intProp", 0)),
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_left_argument_variable() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Int){tgWebApiEntity{key intProp(from:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("intProp", 0)),
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5)),
                linkedMapOf(t2("key", "VEH3"), t2("intProp", 10))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_right_argument_variable() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Int){tgWebApiEntity{key intProp(to:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("intProp", 0)),
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5)),
                linkedMapOf(t2("key", "VEH3"), t2("intProp", 10))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_left_null_argument_variable() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Int){tgWebApiEntity{key intProp(from:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("intProp", 0)),
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5)),
                linkedMapOf(t2("key", "VEH3"), t2("intProp", 10))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_right_null_argument_variable() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Int){tgWebApiEntity{key intProp(to:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("intProp", 0)),
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5)),
                linkedMapOf(t2("key", "VEH3"), t2("intProp", 10))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_left_non_empty_argument_variable() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Int){tgWebApiEntity{key intProp(from:$val)}}", linkedMapOf(t2("val", 2))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5)),
                linkedMapOf(t2("key", "VEH3"), t2("intProp", 10))
            ))
        )), result);
    }
    
    @Test
    public void integer_prop_returns_with_right_non_empty_argument_variable() {
        createIntEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Int){tgWebApiEntity{key intProp(to:$val)}}", linkedMapOf(t2("val", 8))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("intProp", 0)),
                linkedMapOf(t2("key", "VEH2"), t2("intProp", 5))
            ))
        )), result);
    }
    
}