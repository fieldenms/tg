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

import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web_api.IWebApi;

/// Tests for GraphQL Web API implementation for entity-typed fields, and conditions on them with literals and variables.
///
public class WebApiEntityFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    @Test
    public void entity_prop_with_no_conditions_is_supported() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model{key}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"), t2("model", linkedMapOf(t2("key", "316")))),
                linkedMapOf(t2("key", "VEH2"), t2("model", linkedMapOf(t2("key", "A4"))))
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
                linkedMapOf(t2("key", "VEH2"), t2("model", linkedMapOf(t2("key", "A4"), t2("make", linkedMapOf(t2("key", "AUDI"))))))
            ))
        )), result);
    }
    
    @Test
    public void like_on_entity_prop_supports_null_value_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(where: {cond: {model: {cond: {key: {like: null}}}}}){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void like_on_entity_prop_supports_non_empty_value_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(where: {cond: {model: {cond: {key: {like: \"3*\"}}}}}){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"))
            ))
        )), result);
    }
    
    @Test
    public void like_on_entity_prop_supports_missing_value_for_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(where: {cond: {model: {cond: {key: {like: $val}}}}}){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void like_on_entity_prop_supports_null_value_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(where: {cond: {model: {cond: {key: {like: $val}}}}}){key}}", linkedMapOf(t2("val", null))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1")),
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }
    
    @Test
    public void like_on_entity_prop_supports_non_null_value_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(where: {cond: {model: {cond: {key: {like: $val}}}}}){key}}", linkedMapOf(t2("val", "A*"))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH2"))
            ))
        )), result);
    }

    @Test
    public void eq_on_entity_prop_matches_exactly_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(where: {cond: {model: {cond: {key: {eq: \"316\"}}}}}){key}}"));
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf(
                        linkedMapOf(t2("key", "VEH1"))
                ))
        )), result);
    }

    @Test
    public void eq_on_entity_prop_matches_exactly_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(where: {cond: {model: {cond: {key: {eq: $val}}}}}){key}}", linkedMapOf(t2("val", "A4"))));
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf(
                        linkedMapOf(t2("key", "VEH2"))
                ))
        )), result);
    }

    @Test
    public void condition_on_nested_entity_prop_is_supported() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(where: {cond: {model: {cond: {make: {cond: {key: {eq: \"AUDI\"}}}}}}}){key}}"));
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf(
                        linkedMapOf(t2("key", "VEH2"))
                ))
        )), result);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleMake audi = save(new_(TgVehicleMake.class, "AUDI", "Audi"));
        final TgVehicleModel m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        final TgVehicleModel a4 = save(new_(TgVehicleModel.class, "A4", "A4").setMake(audi));
        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(m316));
        save(new_(TgWebApiEntity.class, "VEH2", "veh2 desc").setModel(a4));
    }

}
