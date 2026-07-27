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

import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web_api.IWebApi;

/// Test for GraphQL Web API implementation for collectional fields.
///
public class WebApiCollectionalFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    @Test
    public void collectional_associations_works() {
        final Map<String, Object> result = webApi.execute(input("{user{key activeRoles{userRole{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("user", listOf(
                linkedMapOf(
                    t2("key", "UNIT_TEST_USER"),
                    t2("activeRoles", listOf(
                        linkedMapOf(t2("userRole", 
                            linkedMapOf(t2("key", "UNIT_TEST_ROLE"))
                        ))
                    ))
                )
            ))
        )), result);
    }
    
}