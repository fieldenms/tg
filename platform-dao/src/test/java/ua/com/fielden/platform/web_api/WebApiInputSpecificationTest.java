package ua.com.fielden.platform.web_api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web_api.WebApiUtils.DATA;
import static ua.com.fielden.platform.web_api.WebApiUtils.QUERY;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;
import static ua.com.fielden.platform.web_api.WebApiUtils.inputMult;
import static ua.com.fielden.platform.web_api.WebApiUtils.result;

import java.util.Map;

import org.junit.Test;

import graphql.AssertException;
import graphql.execution.UnknownOperationException;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * Test for GraphQL Web API implementation for different inputs: complete and incomplete queries, variables, variable definitions and multiple queries.
 * 
 * @author TG Team
 *
 */
public class WebApiInputSpecificationTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    //////////////////////// GraphiQL unnatural cases, for completeness ////////////////////////
    
    @Test(expected = AssertException.class)
    public void null_query_results_in_exception() {
        webApi.execute(input(null));
    }
    
    @Test
    public void query_without_variables_entry_executes_successfully() {
        final Map<String, Object> result = webApi.execute(linkedMapOf(t2(QUERY, "{tgWebApiEntity{key}}")));
        assertTrue(errors(result).isEmpty());
        assertTrue(result.containsKey(DATA));
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf())
        )), result);
    }
    
    @Test(expected = UnknownOperationException.class)
    public void multiple_valid_queries_with_different_names_results_in_exception() {
        webApi.execute(input("query test1($val:String){tgWebApiEntity{key(like:$val)}}query test2($val:String){tgWebApiEntity{key(like:$val)}}", linkedMapOf(t2("val", "CA"))));
    }
    
    //////////////////////// GraphiQL unnatural cases, for completeness [END] ////////////////////////
    
    //////////////////////// Testing different input cases ////////////////////////
    
    @Test
    public void empty_query_results_in_errors_and_no_data() {
        final Map<String, Object> result = webApi.execute(input(""));
        assertFalse(errors(result).isEmpty());
        assertFalse(result.containsKey(DATA));
    }
    
    @Test
    public void empty_query_with_curly_braces_results_in_errors_and_no_data() {
        final Map<String, Object> result = webApi.execute(input("{}"));
        assertFalse(errors(result).isEmpty());
        assertFalse(result.containsKey(DATA));
    }
    
    @Test
    public void incomplete_query_with_root_field_selection_results_in_errors_and_no_data() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity}"));
        assertFalse(errors(result).isEmpty());
        assertFalse(result.containsKey(DATA));
    }
    
    @Test
    public void valid_query_executes_successfully() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key}}"));
        assertTrue(errors(result).isEmpty());
        assertTrue(result.containsKey(DATA));
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf())
        )), result);
    }
    
    @Test
    public void valid_query_with_empty_variables_executes_successfully() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key}}", linkedMapOf()));
        assertTrue(errors(result).isEmpty());
        assertTrue(result.containsKey(DATA));
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf())
        )), result);
    }
    
    @Test
    public void valid_query_with_non_empty_variables_executes_successfully() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key}}", linkedMapOf(t2("val", true))));
        assertTrue(errors(result).isEmpty());
        assertTrue(result.containsKey(DATA));
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf())
        )), result);
    }
    
    @Test
    public void valid_query_with_non_empty_variables_and_query_keyword_executes_successfully() {
        final Map<String, Object> result = webApi.execute(input("query{tgWebApiEntity{key}}", linkedMapOf(t2("val", true))));
        assertTrue(errors(result).isEmpty());
        assertTrue(result.containsKey(DATA));
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf())
        )), result);
    }
    
    @Test
    public void valid_query_with_non_empty_unused_variable_and_its_definition_executes_with_errors() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity{key}}", linkedMapOf(t2("val", "CA"))));
        assertFalse(errors(result).isEmpty());
        assertFalse(result.containsKey(DATA));
    }
    
    @Test
    public void valid_query_with_non_empty_variable_and_its_definition_executes_successfully() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity{key(like:$val)}}", linkedMapOf(t2("val", "CA"))));
        assertTrue(errors(result).isEmpty());
        assertTrue(result.containsKey(DATA));
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf())
        )), result);
    }
    
    @Test
    public void valid_query_with_name_and_non_empty_variable_and_its_definition_executes_successfully() {
        final Map<String, Object> result = webApi.execute(input("query test($val:String){tgWebApiEntity{key(like:$val)}}", linkedMapOf(t2("val", "CA"))));
        assertTrue(errors(result).isEmpty());
        assertTrue(result.containsKey(DATA));
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf())
        )), result);
    }
    
    @Test
    public void multiple_queries_with_different_names_and_variables_included_executes_successfully_iff_operationName_was_specified() {
        final Map<String, Object> result = webApi.execute(input("query test1{tgWebApiEntity{key}}query test2{tgWebApiEntity{key}}", linkedMapOf(t2("val", "CA")), "test2"));
        assertTrue(errors(result).isEmpty());
        assertTrue(result.containsKey(DATA));
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf())
        )), result);
    }
    
    @Test
    public void multiple_queries_with_different_names_executes_successfully_iff_operationName_was_specified() {
        final Map<String, Object> result = webApi.execute(inputMult("query test1{tgWebApiEntity{key}}query test2{tgWebApiEntity{key}}", "test1"));
        assertTrue(errors(result).isEmpty());
        assertTrue(result.containsKey(DATA));
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf())
        )), result);
    }
    
    //////////////////////// Testing different input cases [END] ////////////////////////
    
}