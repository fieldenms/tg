package ua.com.fielden.platform.web_api.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;
import static ua.com.fielden.platform.web_api.WebApiUtils.result;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Test for GraphQL Web API implementation for Money fields, their arguments with literals and variables.
 * 
 * @author TG Team
 *
 */
public class WebApiMoneyFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private final BigDecimal zero = new BigDecimal("0.0000");
    private final BigDecimal five = new BigDecimal("5.0000");
    private final BigDecimal ten = new BigDecimal("10.0000");
    
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
    
}