package ua.com.fielden.platform.web_api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web_api.TgScalars.createDateRepr;
import static ua.com.fielden.platform.web_api.WebApiUtils.DATA;
import static ua.com.fielden.platform.web_api.WebApiUtils.QUERY;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;
import static ua.com.fielden.platform.web_api.WebApiUtils.inputMult;
import static ua.com.fielden.platform.web_api.WebApiUtils.result;

import java.math.BigDecimal;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import graphql.execution.UnknownOperationException;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

/**
 * Test for GraphQL Web API implementation.
 * 
 * @author TG Team
 *
 */
public class WebApiTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    //////////////////////// GraphiQL unnatural cases, for completeness ////////////////////////
    
    @Test(expected = NullPointerException.class)
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
    
    //////////////////////// Different property types, their arguments with literals and variables ////////////////////////
    
    //////////////////////// String ////////////////////////
    private void createEntities() {
        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleModel model = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(model));
        save(new_(TgWebApiEntity.class, "VEH2", "veh2 desc").setModel(model));
    }
    
    @Test
    public void string_prop_returns() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void string_prop_returns_with_null_argument_literal() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key(like:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void string_prop_returns_with_non_empty_argument_literal() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key(like:\"1\")}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"))
            ))
        )), result);
    }
    
    @Test
    public void string_prop_returns_with_argument_variable() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity{key(like:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void string_prop_returns_with_null_argument_variable() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity{key(like:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void string_prop_returns_with_non_empty_argument_variable() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity{key(like:$val)}}", linkedMapOf(t2("val", "1"))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"))
            ))
        )), result);
    }
    
    //////////////////////// boolean ////////////////////////
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
    
    //////////////////////// Integer ////////////////////////
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
    
    //////////////////////// Long ////////////////////////
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
    
    //////////////////////// BigDecimal ////////////////////////
    private final BigDecimal zero = new BigDecimal("0.0000");
    private final BigDecimal five = new BigDecimal("5.0000");
    private final BigDecimal ten = new BigDecimal("10.0000");
    
    private void createBigDecimalEntities() {
        save(new_(TgWebApiEntity.class, "VEH1").setBigDecimalProp(zero));
        save(new_(TgWebApiEntity.class, "VEH2").setBigDecimalProp(five));
        save(new_(TgWebApiEntity.class, "VEH3").setBigDecimalProp(ten));
    }
    
    @Test
    public void bigDecimal_prop_returns() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key bigDecimalProp}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("bigDecimalProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("bigDecimalProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_left_null_argument_literal() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key bigDecimalProp(from:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("bigDecimalProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("bigDecimalProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_right_null_argument_literal() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key bigDecimalProp(to:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("bigDecimalProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("bigDecimalProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_left_non_empty_argument_literal() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key bigDecimalProp(from:2.5)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("bigDecimalProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_right_non_empty_argument_literal() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key bigDecimalProp(to:7.5)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("bigDecimalProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_left_argument_variable() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:BigDecimal){tgWebApiEntity{key bigDecimalProp(from:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("bigDecimalProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("bigDecimalProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_right_argument_variable() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:BigDecimal){tgWebApiEntity{key bigDecimalProp(to:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("bigDecimalProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("bigDecimalProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_left_null_argument_variable() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:BigDecimal){tgWebApiEntity{key bigDecimalProp(from:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("bigDecimalProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("bigDecimalProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_right_null_argument_variable() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:BigDecimal){tgWebApiEntity{key bigDecimalProp(to:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("bigDecimalProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("bigDecimalProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_left_non_empty_argument_variable() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:BigDecimal){tgWebApiEntity{key bigDecimalProp(from:$val)}}", linkedMapOf(t2("val", 2.5))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("bigDecimalProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void bigDecimal_prop_returns_with_right_non_empty_argument_variable() {
        createBigDecimalEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:BigDecimal){tgWebApiEntity{key bigDecimalProp(to:$val)}}", linkedMapOf(t2("val", 7.5))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("bigDecimalProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("bigDecimalProp", five))
            ))
        )), result);
    }
    
    //////////////////////// Money ////////////////////////
    private void createMoneyEntities() {
        save(new_(TgWebApiEntity.class, "VEH1").setMoneyProp(new Money("0.0000")));
        save(new_(TgWebApiEntity.class, "VEH2").setMoneyProp(new Money("5.0000")));
        save(new_(TgWebApiEntity.class, "VEH3").setMoneyProp(new Money("10.0000")));
    }
    
    @Test
    public void money_prop_returns() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key moneyProp}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("moneyProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("moneyProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_left_null_argument_literal() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key moneyProp(from:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("moneyProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("moneyProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_right_null_argument_literal() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key moneyProp(to:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("moneyProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("moneyProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_left_non_empty_argument_literal() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key moneyProp(from:2.5)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("moneyProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_right_non_empty_argument_literal() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key moneyProp(to:7.5)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("moneyProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_left_argument_variable() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Money){tgWebApiEntity{key moneyProp(from:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("moneyProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("moneyProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_right_argument_variable() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Money){tgWebApiEntity{key moneyProp(to:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("moneyProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("moneyProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_left_null_argument_variable() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Money){tgWebApiEntity{key moneyProp(from:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("moneyProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("moneyProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_right_null_argument_variable() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Money){tgWebApiEntity{key moneyProp(to:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("moneyProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("moneyProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_left_non_empty_argument_variable() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Money){tgWebApiEntity{key moneyProp(from:$val)}}", linkedMapOf(t2("val", 2.5))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five)),
                linkedMapOf(t2("key", "VEH3"), t2("moneyProp", ten))
            ))
        )), result);
    }
    
    @Test
    public void money_prop_returns_with_right_non_empty_argument_variable() {
        createMoneyEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Money){tgWebApiEntity{key moneyProp(to:$val)}}", linkedMapOf(t2("val", 7.5))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("moneyProp", zero)),
                linkedMapOf(t2("key", "VEH2"), t2("moneyProp", five))
            ))
        )), result);
    }
    
    //////////////////////// Entity ////////////////////////
    
    @Test
    public void entity_prop_returns() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model{key}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("model", linkedMapOf(t2("key", "316")))),
                linkedMapOf(t2("key", "VEH2"), t2("model", linkedMapOf(t2("key", "316"))))
            ))
        )), result);
    }
    
    @Test
    public void dot_notated_entity_prop_returns() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model{key make{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("model", linkedMapOf(t2("key", "316"), t2("make", linkedMapOf(t2("key", "MERC")))))),
                linkedMapOf(t2("key", "VEH2"), t2("model", linkedMapOf(t2("key", "316"), t2("make", linkedMapOf(t2("key", "MERC"))))))
            ))
        )), result);
    }
    
    //////////////////////// Date ////////////////////////
    private final DateTime date1Instant = new DateTime(2020, 2, 10, 0, 0);
    private final DateTime date2Instant = new DateTime(2020, 2, 15, 0, 0);
    private final DateTime date3Instant = new DateTime(2020, 2, 20, 0, 0);
    private final Map<String, Object> date1 = createDateRepr(date1Instant.toDate());
    private final Map<String, Object> date2 = createDateRepr(date2Instant.toDate());
    private final Map<String, Object> date3 = createDateRepr(date3Instant.toDate());
    
    private void createDateEntities() {
        save(new_(TgWebApiEntity.class, "VEH1").setDateProp(date1Instant.toDate()));
        save(new_(TgWebApiEntity.class, "VEH2").setDateProp(date2Instant.toDate()));
        save(new_(TgWebApiEntity.class, "VEH3").setDateProp(date3Instant.toDate()));
    }
    
    @Test
    public void date_prop_returns() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key dateProp}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("dateProp", date1)),
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_left_null_argument_literal() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key dateProp(from:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("dateProp", date1)),
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_right_null_argument_literal() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key dateProp(to:null)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("dateProp", date1)),
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_left_non_empty_argument_literal() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key dateProp(from:\"2020-02-12\")}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_right_non_empty_argument_literal() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key dateProp(to:20200218)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("dateProp", date1)),
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_left_argument_variable() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Date){tgWebApiEntity{key dateProp(from:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("dateProp", date1)),
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_right_argument_variable() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Date){tgWebApiEntity{key dateProp(to:$val)}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("dateProp", date1)),
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_left_null_argument_variable() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Date){tgWebApiEntity{key dateProp(from:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("dateProp", date1)),
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_right_null_argument_variable() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Date){tgWebApiEntity{key dateProp(to:$val)}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("dateProp", date1)),
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_left_non_empty_argument_variable() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Date){tgWebApiEntity{key dateProp(from:$val)}}", linkedMapOf(t2("val", "2020-02-12"))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void date_prop_returns_with_right_non_empty_argument_variable() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("query($val:Date){tgWebApiEntity{key dateProp(to:$val)}}", linkedMapOf(t2("val", 20200218))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("dateProp", date1)),
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2))
            ))
        )), result);
    }
    
    //////////////////////// Hyperlink ////////////////////////
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
    
    //////////////////////// Colour ////////////////////////
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
    
    //////////////////////// Different property types, their arguments with literals and variables [END] ////////////////////////
    
    //////////////////////// Fragments & Aliases ////////////////////////
    
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
    
    //////////////////////// Fragments & Aliases [END] ////////////////////////
    
    //////////////////////// Standard @include & @skip Directives ////////////////////////
    
    @Test
    public void skip_directive_works_with_argument_literal() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key desc(like:\"veh2%desc\") @skip(if:true) }}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void include_directive_works_with_argument_variable() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("query ($val: Boolean!) {tgWebApiEntity{key desc(like:\"veh2%desc\") @include(if:$val) }}", linkedMapOf(t2("val", false))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    //////////////////////// Standard @include & @skip Directives [END] ////////////////////////
    
    //////////////////////// Collectional Associations ////////////////////////
    
    @Test
    public void collectional_associations_works() {
        final Map<String, Object> result = webApi.execute(input("{user{key roles{userRole{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("user", listOf(
                linkedMapOf(
                    t2("key", "UNIT_TEST_USER"),
                    t2("roles", listOf(
                        linkedMapOf(t2("userRole", 
                            linkedMapOf(t2("key", "UNIT_TEST_ROLE"))
                        ))
                    ))
                )
            ))
        )), result);
    }
    
    //////////////////////// Collectional Associations [END] ////////////////////////
    
}