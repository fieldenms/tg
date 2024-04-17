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

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Test for GraphQL Web API implementation for default property data fetching (resolving) logic, aka how non-root field data gets resolved from fetched TG entities and / or records.
 * 
 * @author TG Team
 *
 */
public class WebApiPropertyDataFetcherTest extends AbstractDaoTestCase {
    private static final String KEY = "KEY";
    private static final String VAL = "VAL";
    private static final String OTHER = "OTHER";
    private final IWebApi webApi = getInstance(IWebApi.class);

    ////////////////////////////////////////// Boolean prop tests because of 'is/get' accessor specifics //////////////////////////////////////////

    @Test
    public void boolean_property_gets_resolved_from_GetProperty_getter_if_IsProperty_getter_is_also_present_in_proxied_entity() {
        save(new_(TgWebApiEntityWithBooleanPropWithIsAndGetGetters.class, KEY).setProp(true).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithBooleanPropWithIsAndGetGetters{prop}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithBooleanPropWithIsAndGetGetters", listOf(
                linkedMapOf(t2("prop", true))
            ))
        )), result);
    }

    @Test
    public void boolean_property_gets_resolved_from_GetProperty_getter_if_IsProperty_getter_is_also_present_in_full_entity() {
        save(new_(TgWebApiEntityWithBooleanPropWithIsAndGetGetters.class, KEY).setProp(true).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithBooleanPropWithIsAndGetGetters{prop key otherProp}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithBooleanPropWithIsAndGetGetters", listOf(
                linkedMapOf(t2("prop", true), t2("key", KEY), t2("otherProp", OTHER))
            ))
        )), result);
    }

    @Test
    public void boolean_property_gets_resolved_from_IsProperty_getter_if_Property_accessor_is_also_present_in_proxied_entity() {
        save(new_(TgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessor.class, KEY).setProp(true).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessor{prop}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessor", listOf(
                linkedMapOf(t2("prop", true))
            ))
        )), result);
    }

    @Test
    public void boolean_property_gets_resolved_from_IsProperty_getter_if_Property_accessor_is_also_present_in_full_entity() {
        save(new_(TgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessor.class, KEY).setProp(true).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessor{prop key otherProp}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithBooleanPropWithIsGetterAndRecordLikeAccessor", listOf(
                linkedMapOf(t2("prop", true), t2("key", KEY), t2("otherProp", OTHER))
            ))
        )), result);
    }

    @Test
    public void boolean_property_gets_resolved_from_GetProperty_getter_if_Property_accessor_is_also_present_in_proxied_entity() {
        save(new_(TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor.class, KEY).setProp(true).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor{prop}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor", listOf(
                linkedMapOf(t2("prop", true))
            ))
        )), result);
    }

    @Test
    public void boolean_property_gets_resolved_from_GetProperty_getter_if_Property_accessor_is_also_present_in_full_entity() {
        save(new_(TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor.class, KEY).setProp(true).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor{prop key otherProp}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor", listOf(
                linkedMapOf(t2("prop", true), t2("key", KEY), t2("otherProp", OTHER))
            ))
        )), result);
    }

    @Test
    @Ignore(value = "Throws NoSuchMethodException, because record-like accessors are not supported now; may be supported in future; then TgWebApiEntityWithRecordLikeAccessor and corresponding tests should be added too.")
    public void boolean_property_gets_resolved_from_Property_accessor_if_no_getter_is_present_in_proxied_entity() {
        save(new_(TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly.class, KEY).setProp(true).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly{prop}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly", listOf(
                linkedMapOf(t2("prop", true))
            ))
        )), result);
    }

    @Test
    @Ignore(value = "Throws NoSuchMethodException, because record-like accessors are not supported now; may be supported in future; then TgWebApiEntityWithRecordLikeAccessor and corresponding tests should be added too.")
    public void boolean_property_gets_resolved_from_Property_accessor_if_no_getter_is_present_in_full_entity() {
        save(new_(TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly.class, KEY).setProp(true).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly{prop key otherProp}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly", listOf(
                linkedMapOf(t2("prop", true), t2("key", KEY), t2("otherProp", OTHER))
            ))
        )), result);
    }

    ////////////////////////////////////////////////// Simple prop tests //////////////////////////////////////////////////

    @Test
    public void property_gets_resolved_from_GetProperty_getter_if_Property_accessor_is_also_present_in_proxied_entity() {
        save(new_(TgWebApiEntityWithGetGetterAndRecordLikeAccessor.class, KEY).setProp(VAL).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithGetGetterAndRecordLikeAccessor{prop}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithGetGetterAndRecordLikeAccessor", listOf(
                linkedMapOf(t2("prop", VAL))
            ))
        )), result);
    }

    @Test
    public void property_gets_resolved_from_GetProperty_getter_if_Property_accessor_is_also_present_in_full_entity() {
        save(new_(TgWebApiEntityWithGetGetterAndRecordLikeAccessor.class, KEY).setProp(VAL).setOtherProp(OTHER));

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntityWithGetGetterAndRecordLikeAccessor{prop key otherProp}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntityWithGetGetterAndRecordLikeAccessor", listOf(
                linkedMapOf(t2("prop", VAL), t2("key", KEY), t2("otherProp", OTHER))
            ))
        )), result);
    }

    ////////////////////////////////////////////////// Future record-typed prop tests //////////////////////////////////////////////////
    // property_gets_resolved_from_GetProperty_getter_if_it_is_also_present_in_record_besides_implicit_Property_accessor
    // boolean_property_gets_resolved_from_IsProperty_getter_if_it_is_also_present_in_record_besides_implicit_Property_accessor

}