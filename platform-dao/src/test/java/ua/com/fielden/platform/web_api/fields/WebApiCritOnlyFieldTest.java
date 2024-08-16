package ua.com.fielden.platform.web_api.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web_api.GraphQLScalars.createDateRepr;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;
import static ua.com.fielden.platform.web_api.WebApiUtils.result;

import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Test for GraphQL Web API implementation for {@link CritOnly} properties.
 * 
 * @author TG Team
 *
 */
public class WebApiCritOnlyFieldTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private final DateTime date1Instant = new DateTime(2020, 2, 10, 0, 0);
    private final DateTime date2Instant = new DateTime(2020, 2, 15, 0, 0);
    private final DateTime date3Instant = new DateTime(2020, 2, 20, 0, 0);
    private final Map<String, Object> date2 = createDateRepr(date2Instant.toDate());
    private final Map<String, Object> date3 = createDateRepr(date3Instant.toDate());
    
    private void createDateEntities() {
        save(new_(TgWebApiEntity.class, "VEH1").setDateProp(date1Instant.toDate()));
        save(new_(TgWebApiEntity.class, "VEH2").setDateProp(date2Instant.toDate()));
        save(new_(TgWebApiEntity.class, "VEH3").setDateProp(date3Instant.toDate()));
    }
    
    @Test
    public void critOnly_single_criteria_works() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntitySyntheticSingle{key date(from:\"2020-02-12\") @skip(if:true) dateProp}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntitySyntheticSingle", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2)),
                linkedMapOf(t2("key", "VEH3"), t2("dateProp", date3))
            ))
        )), result);
    }
    
    @Test
    public void critOnly_multi_criteria_works() {
        createDateEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntitySyntheticMulti{key datePeriod(from:\"2020-02-12\",to:\"2020-02-18\") @skip(if:true) dateProp}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntitySyntheticMulti", listOf(
                linkedMapOf(t2("key", "VEH2"), t2("dateProp", date2))
            ))
        )), result);
    }
    
}