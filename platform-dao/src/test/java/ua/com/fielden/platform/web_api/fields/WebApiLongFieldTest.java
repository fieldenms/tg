package ua.com.fielden.platform.web_api.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    @Test
    public void long_prop_supports_values_beyond_the_32_bit_integer_range() {
        // 3_000_000_000 exceeds Integer.MAX_VALUE (2_147_483_647); supporting such values is the whole point of Long over Int.
        final long beyondInt = 3_000_000_000L;
        save(new_(TgWebApiEntity.class, "VEH1").setLongProp(beyondInt));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key longProp}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("longProp", beyondInt))
            ))
        )), result);
    }

    @Test
    public void long_prop_filters_by_a_literal_bound_beyond_the_32_bit_integer_range() {
        save(new_(TgWebApiEntity.class, "VEH1").setLongProp(1_000_000_000L)); // below the bound
        save(new_(TgWebApiEntity.class, "VEH2").setLongProp(5_000_000_000L)); // above the bound, and beyond Integer.MAX_VALUE

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key longProp(from:3000000000)}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("longProp", 5_000_000_000L))
            ))
        )), result);
    }

    @Test
    public void long_prop_with_a_literal_bound_beyond_the_64_bit_range_results_in_errors() {
        createLongEntities();
        // 9223372036854775808 == 2^63 == Long.MAX_VALUE + 1, so it cannot be represented as a Long and must be rejected.
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key longProp(from:9223372036854775808)}}"));

        assertFalse(errors(result).isEmpty());
    }

    @Test
    public void long_prop_with_a_fractional_literal_bound_results_in_errors() {
        createLongEntities();
        // A fractional literal is a FloatValue, which is not a valid Long argument.
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key longProp(from:2.5)}}"));

        assertFalse(errors(result).isEmpty());
    }

}