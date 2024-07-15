package ua.com.fielden.platform.web_api;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web_api.FieldSchema.DEFAULT_PAGE_CAPACITY;
import static ua.com.fielden.platform.web_api.WebApiUtils.data;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;
import static ua.com.fielden.platform.web_api.WebApiUtils.result;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * Test for GraphQL Web API implementation for pagination.
 * 
 * @author TG Team
 *
 */
public class WebApiPaginationTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private void createEntities(final int number) {
        for (int i = 0; i < number; i++) {
            save(new_(TgWebApiEntity.class, format("VEH%04d", i)));
        }
    }
    
    // PAGE CAPACITY
    
    @Test
    public void default_page_capacity_returns_if_no_explicit_page_capacity_was_specified_or_if_zero_negative_or_empty_page_capacity_was_specified() {
        createEntities(DEFAULT_PAGE_CAPACITY + 1);
        
        Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(DEFAULT_PAGE_CAPACITY, ((List<?>) data(result).get("tgWebApiEntity")).size());
        
        result = webApi.execute(input("{tgWebApiEntity(pageCapacity:null){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(DEFAULT_PAGE_CAPACITY, ((List<?>) data(result).get("tgWebApiEntity")).size());
        
        result = webApi.execute(input("{tgWebApiEntity(pageCapacity:0){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(DEFAULT_PAGE_CAPACITY, ((List<?>) data(result).get("tgWebApiEntity")).size());
        
        result = webApi.execute(input("{tgWebApiEntity(pageCapacity:-5){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(DEFAULT_PAGE_CAPACITY, ((List<?>) data(result).get("tgWebApiEntity")).size());
    }
    
    @Test
    public void explicitly_specified_page_capacity_works() {
        createEntities(11);
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(pageCapacity:10){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(10, ((List<?>) data(result).get("tgWebApiEntity")).size());
    }
    
    @Test
    public void explicitly_specified_page_capacity_works_with_non_empty_argument_variable() {
        createEntities(11);
        
        final Map<String, Object> result = webApi.execute(input("query($val:Int){tgWebApiEntity(pageCapacity:$val){key}}", linkedMapOf(t2("val", 10))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(10, ((List<?>) data(result).get("tgWebApiEntity")).size());
    }
    
    // PAGE NUMBER
    
    @Test
    public void default_page_number_returns_if_no_explicit_page_number_was_specified_or_if_negative_or_empty_page_number_was_specified() {
        createEntities(2);
        
        Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(pageCapacity:1){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH0000"))
            ))
        )), result);
        
        result = webApi.execute(input("{tgWebApiEntity(pageCapacity:1, pageNumber:-1){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH0000"))
            ))
        )), result);
        
        result = webApi.execute(input("{tgWebApiEntity(pageCapacity:1, pageNumber:null){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH0000"))
            ))
        )), result);
    }
    
    @Test
    public void explicitly_specified_page_number_works() {
        createEntities(2);
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(pageCapacity:1, pageNumber:1){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH0001"))
            ))
        )), result);
    }
    
    @Test
    public void explicitly_specified_page_number_works_with_non_empty_argument_variable() {
        createEntities(2);
        
        final Map<String, Object> result = webApi.execute(input("query($val:Int){tgWebApiEntity(pageCapacity:1, pageNumber:$val){key}}", linkedMapOf(t2("val", 1))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH0001"))
            ))
        )), result);
    }
    
}