package ua.com.fielden.platform.web_api.fields;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web_api.RootEntityUtils.ERR_EQ_AND_LIKE_ARE_MUTUALLY_EXCLUSIVE;
import static ua.com.fielden.platform.web_api.RootEntityUtils.ERR_EQ_DOES_NOT_PERMIT_WILDCARDS;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;
import static ua.com.fielden.platform.web_api.WebApiUtils.result;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Tests for GraphQL Web API implementation for entity-typed fields.
 * 
 * @author TG Team
 *
 */
public class WebApiEntityFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    @Test
    public void entity_prop_with_no_conditions_is_supported() {
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
    public void nested_entity_prop_is_supported() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model{key make{key}}}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("model", linkedMapOf(t2("key", "316"), t2("make", linkedMapOf(t2("key", "MERC")))))),
                linkedMapOf(t2("key", "VEH2"), t2("model", linkedMapOf(t2("key", "316"), t2("make", linkedMapOf(t2("key", "MERC"))))))
            ))
        )), result);
    }
    
    @Test
    public void like_supports_null_argument_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(like:null){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void like_supports_non_empty_argument_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(like:\"*1\"){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"))
            ))
        )), result);
    }
    
    @Test
    public void like_supports_missing_value_for_argument_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(like:$val){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void like_supports_null_argument_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(like:$val){key}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void like_supports_non_null_argument_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(like:$val){key}}", linkedMapOf(t2("val", "*1"))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"))
            ))
        )), result);
    }

    @Test
    public void like_does_not_match_anywhere_when_using_argument_with_no_wildcard_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(like:\"VEH\"){key}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf())
        )), result);
    }

    @Test
    public void like_does_not_match_anywhere_when_using_argument_with_no_wildcard_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(like:$val){key}}", linkedMapOf(t2("val", "VEH"))));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf())
        )), result);
    }

    @Test
    public void like_supports_comma_separated_values_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(like:\"VEH1,VEH2\"){key}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf(
                        linkedMapOf(t2("key", "VEH1")),
                        linkedMapOf(t2("key", "VEH2"))
                ))
        )), result);
    }

    @Test
    public void like_supports_comma_separated_values_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(like:$val){key}}", linkedMapOf(t2("val", "VEH1,VEH2"))));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf(
                        linkedMapOf(t2("key", "VEH1")),
                        linkedMapOf(t2("key", "VEH2"))
                ))
        )), result);
    }

    @Test
    public void eq_does_not_support_wildcards_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(eq:\"VEH*\"){key}}"));
        final List<Object> errors = errors(result);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertTrue(errors.getFirst().toString().contains(ERR_EQ_DOES_NOT_PERMIT_WILDCARDS));
    }

    @Test
    public void eq_does_not_support_wildcards_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(eq:$val){key}}", linkedMapOf(t2("val", "VEH*"))));
        final List<Object> errors = errors(result);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertTrue(errors.getFirst().toString().contains(ERR_EQ_DOES_NOT_PERMIT_WILDCARDS));
    }

    @Test
    public void eq_matches_exactly_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(eq:\"VEH1\"){key}}"));
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf(
                        linkedMapOf(t2("key", "VEH1"))
                ))
        )), result);
    }

    @Test
    public void eq_matches_exactly_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(eq:$val){key}}", linkedMapOf(t2("val", "VEH1"))));
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf(
                        linkedMapOf(t2("key", "VEH1"))
                ))
        )), result);
    }

    @Test
    public void eq_and_like_are_mutually_exclusive() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(eq:\"VEH1\", like: \"VEH2\" ){key}}"));
        final List<Object> errors = errors(result);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertTrue(errors.getFirst().toString().contains(ERR_EQ_AND_LIKE_ARE_MUTUALLY_EXCLUSIVE));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleModel model = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(model));
        save(new_(TgWebApiEntity.class, "VEH2", "veh2 desc").setModel(model));

    }
}