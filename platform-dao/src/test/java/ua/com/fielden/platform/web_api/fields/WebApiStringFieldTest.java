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

/// Tests for GraphQL Web API implementation for String fields, and conditions on them with literals and variables.
///
public class WebApiStringFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    @Test
    public void string_prop_with_no_conditions_is_supported() {
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
    public void like_supports_null_argument_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(where: {cond: {key: {like: null}}}){key}}"));
        
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
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(where: {cond: {key: {like: \"*1\"}}}){key}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"))
            ))
        )), result);
    }
    
    @Test
    public void like_supports_missing_value_for_argument_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(where: {cond: {key: {like: $val}}}){key}}"));
        
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
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(where: {cond: {key: {like: $val}}}){key}}", linkedMapOf(t2("val", null))));
        
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
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(where: {cond: {key: {like: $val}}}){key}}", linkedMapOf(t2("val", "*1"))));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(t2("key", "VEH1"))
            ))
        )), result);
    }

    @Test
    public void eq_matches_exactly_as_literal() {
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(where: {cond: {key: {eq: \"VEH1\"}}}){key}}"));
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf(
                        linkedMapOf(t2("key", "VEH1"))
                ))
        )), result);
    }

    @Test
    public void eq_matches_exactly_as_variable() {
        final Map<String, Object> result = webApi.execute(input("query($val:String){tgWebApiEntity(where: {cond: {key: {eq: $val}}}){key}}", linkedMapOf(t2("val", "VEH1"))));
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
                t2("tgWebApiEntity", listOf(
                        linkedMapOf(t2("key", "VEH1"))
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
        final TgVehicleModel model = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(model));
        save(new_(TgWebApiEntity.class, "VEH2", "veh2 desc").setModel(model));
    }

}
