package ua.com.fielden.platform.web_api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web_api.RootEntityUtils.WARN_ORDER_PRIORITIES_ARE_NOT_DISTINCT;
import static ua.com.fielden.platform.web_api.GraphQLScalars.createDateRepr;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;
import static ua.com.fielden.platform.web_api.WebApiUtils.result;

import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * Test for GraphQL Web API implementation for data ordering.
 * 
 * @author TG Team
 *
 */
public class WebApiOrderingTest extends AbstractDaoTestCase {
    private static final Map<String, Object> VEH1 = linkedMapOf(t2("key", "VEH1"), t2("model", linkedMapOf(t2("key", "117"), t2("make", linkedMapOf(t2("key", "TOYOTA"))))));
    private static final Map<String, Object> VEH2 = linkedMapOf(t2("key", "VEH2"), t2("model", linkedMapOf(t2("key", "316"), t2("make", linkedMapOf(t2("key", "MERC"))))));
    private static final Map<String, Object> VEH3 = linkedMapOf(t2("key", "VEH3"), t2("model", linkedMapOf(t2("key", "116"), t2("make", linkedMapOf(t2("key", "TOYOTA"))))));
    private static final Map<String, Object> VEH4 = linkedMapOf(t2("key", "VEH4"), t2("model", linkedMapOf(t2("key", "317"), t2("make", linkedMapOf(t2("key", "MERC"))))));
    private final IWebApi webApi = getInstance(IWebApi.class);
    
    private void createEntities() {
        final TgVehicleMake toyota = save(new_(TgVehicleMake.class, "TOYOTA", "Toyota"));
        final TgVehicleModel toyota117 = save(new_(TgVehicleModel.class, "117", "117").setMake(toyota));
        final TgVehicleModel toyota116 = save(new_(TgVehicleModel.class, "116", "116").setMake(toyota));
        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleModel merc317 = save(new_(TgVehicleModel.class, "317", "317").setMake(merc));
        final TgVehicleModel merc316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgWebApiEntity.class, "VEH2", "veh2 desc").setModel(merc316));
        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(toyota117));
        save(new_(TgWebApiEntity.class, "VEH3", "veh3 desc").setModel(toyota116));
        save(new_(TgWebApiEntity.class, "VEH4", "veh4 desc").setModel(merc317));
    }
    
    @Test
    public void default_ordering_is_by_key_ascending() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model{key make{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH1,
                VEH2,
                VEH3,
                VEH4
            ))
        )), result);
    }
    
    @Test
    public void root_ascending_order_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(order: ASC_1){key model{key make{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH1,
                VEH2,
                VEH3,
                VEH4
            ))
        )), result);
    }
    
    @Test
    public void root_descending_order_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(order: DESC_1){key model{key make{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH4,
                VEH3,
                VEH2,
                VEH1
            ))
        )), result);
    }
    
    @Test
    public void first_level_simple_property_ascending_order_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key(order: ASC_1) model{key make{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH1,
                VEH2,
                VEH3,
                VEH4
            ))
        )), result);
    }
    
    @Test
    public void first_level_simple_property_descending_order_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key(order: DESC_1) model{key make{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH4,
                VEH3,
                VEH2,
                VEH1
            ))
        )), result);
    }
    
    @Test
    public void first_level_entity_property_ascending_order_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model(order: ASC_1){key make{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH3,
                VEH1,
                VEH2,
                VEH4
            ))
        )), result);
    }
    
    @Test
    public void first_level_entity_property_descending_order_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model(order: DESC_1){key make{key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH4,
                VEH2,
                VEH1,
                VEH3
            ))
        )), result);
    }
    
    @Test
    public void second_level_entity_property_ascending_order_in_combination_with_first_level_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model(order: ASC_2){key make(order: ASC_1){key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH2,
                VEH4,
                VEH3,
                VEH1
            ))
        )), result);
    }
    
    @Test
    public void second_level_entity_property_descending_order_in_combination_with_first_level_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{key model(order: DESC_2){key make(order: DESC_1){key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH1,
                VEH3,
                VEH4,
                VEH2
            ))
        )), result);
    }
    
    @Test
    public void second_level_entity_property_ascending_order_in_combination_with_root_level_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(order: ASC_2){key model{key make(order: ASC_1){key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH2,
                VEH4,
                VEH1,
                VEH3
            ))
        )), result);
    }
    
    @Test
    public void second_level_entity_property_descending_order_in_combination_with_root_level_works() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(order: DESC_2){key model{key make(order: DESC_1){key}}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH3,
                VEH1,
                VEH4,
                VEH2
            ))
        )), result);
    }
    
    // all numbers
    
    @Test
    public void ascending_order_with_all_9_numbers_works() {
        createEntities();
        
        //TODO Should be thought through how to better handle such cases of duplication
        final Map<String, Object> result = webApi.execute(input("{"
                + "tgWebApiEntity(order: ASC_1){"
                + "  key"
                + "  bigDecimalProp(order: ASC_2) @skip(if:true)"
                + "  desc(order: ASC_3) @skip(if:true)"
                + "  model(order: ASC_4){"
                + "    key"
                + "    desc(order: ASC_5) @skip(if:true)"
                + "    make(order: ASC_6){"
                + "      key"
                + "      desc(order: ASC_7) @skip(if:true)"
                + "    }"
                + "  }"
                + "  intProp(order: ASC_8) @skip(if:true)"
                + "  longProp(order: ASC_9) @skip(if:true)"
                + "}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH1,
                VEH2,
                VEH3,
                VEH4
            ))
        )), result);
    }
    
    @Test
    public void descending_order_with_all_9_numbers_works() {
        createEntities();
        
        //TODO Should be thought through how to better handle such cases of duplication
        final Map<String, Object> result = webApi.execute(input("{"
                + "tgWebApiEntity(order: DESC_1){"
                + "  bigDecimalProp(order: DESC_2) @skip(if:true)"
                + "  moneyProp(order: DESC_3) @skip(if:true)"
                + "  dateProp(order: DESC_4) @skip(if:true)"
                + "  hyperlinkProp(order: DESC_5) @skip(if:true)"
                + "  colourProp(order: DESC_6) @skip(if:true)"
                + "  key"
                + "  desc(order: DESC_8) @skip(if:true)"
                + "  active(order: DESC_9) @skip(if:true)"
                + "  model(order: DESC_7){"
                + "    key"
                + "    make{"
                + "      key"
                + "    }"
                + "  }"
                + "}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                VEH4,
                VEH3,
                VEH2,
                VEH1
            ))
        )), result);
    }
    
    // default order -- composite and entity-typed keys
    
    @Test
    public void default_ordering_is_by_key_ascending_for_one_2_one_associations() {
        save(new_(TgCompoundEntity.class, "M1", "Master 1")); // TgCompoundEntityDetail saves automatically -- see TgCompoundEntityDao
        save(new_(TgCompoundEntity.class, "M2", "Master 2"));
       
        final Map<String, Object> result = webApi.execute(input("{tgCompoundEntityDetail{key{key}}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgCompoundEntityDetail", listOf(
                linkedMapOf(t2("key", linkedMapOf(t2("key", "M1")))),
                linkedMapOf(t2("key", linkedMapOf(t2("key", "M2"))))
            ))
        )), result);
    }
    
    @Test
    public void default_ordering_is_by_keys_ascending_for_one_2_many_associations() {
        final DateTime date1Instant = new DateTime(2020, 2, 10, 0, 0);
        final DateTime date2Instant = new DateTime(2020, 2, 15, 0, 0);
        final DateTime date3Instant = new DateTime(2020, 2, 20, 0, 0);
        final DateTime date4Instant = new DateTime(2020, 2, 25, 0, 0);
        final Map<String, Object> date1 = createDateRepr(date1Instant.toDate());
        final Map<String, Object> date2 = createDateRepr(date2Instant.toDate());
        final Map<String, Object> date3 = createDateRepr(date3Instant.toDate());
        final Map<String, Object> date4 = createDateRepr(date4Instant.toDate());
        final TgCompoundEntity master1 = save(new_(TgCompoundEntity.class, "M1", "Master 1"));
        final TgCompoundEntity master2 = save(new_(TgCompoundEntity.class, "M2", "Master 2"));
        save(new_composite(TgCompoundEntityChild.class, master2, date4Instant.toDate()).setDesc("master2 date4"));
        save(new_composite(TgCompoundEntityChild.class, master2, date3Instant.toDate()).setDesc("master2 date3"));
        save(new_composite(TgCompoundEntityChild.class, master1, date1Instant.toDate()).setDesc("master1 date1"));
        save(new_composite(TgCompoundEntityChild.class, master1, date2Instant.toDate()).setDesc("master1 date2"));
       
        final Map<String, Object> result = webApi.execute(input("{tgCompoundEntityChild{tgCompoundEntity{key} date}}"));
        
        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgCompoundEntityChild", listOf(
                linkedMapOf(t2("tgCompoundEntity", linkedMapOf(t2("key", "M1"))), t2("date", date1)),
                linkedMapOf(t2("tgCompoundEntity", linkedMapOf(t2("key", "M1"))), t2("date", date2)),
                linkedMapOf(t2("tgCompoundEntity", linkedMapOf(t2("key", "M2"))), t2("date", date3)),
                linkedMapOf(t2("tgCompoundEntity", linkedMapOf(t2("key", "M2"))), t2("date", date4))
            ))
        )), result);
    }
    
    // validation for ordering priorities
    
    @Test
    public void non_distinct_ordering_priorities_returns_data_with_error() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity(order: DESC_1){key model{key make(order: DESC_1){key}}}}"));
        
        assertFalse(errors(result).isEmpty());
        assertEquals(result(
            listOf(linkedMapOf(
                t2("message", WARN_ORDER_PRIORITIES_ARE_NOT_DISTINCT),
                t2("locations", listOf(linkedMapOf(t2("line", "1"), t2("column", "2")))),
                t2("path", listOf("tgWebApiEntity")),
                t2("extensions", linkedMapOf(t2("classification", "DataFetchingException")))
            )),
            linkedMapOf(
                t2("tgWebApiEntity", listOf(
                    VEH4,
                    VEH3,
                    VEH2,
                    VEH1
                ))
            )
        ).toString(), result.toString());
    }
    
    @Test
    public void non_distinct_ordering_priorities_returns_data_with_errors_for_multiple_queries() {
        createEntities();
        
        final Map<String, Object> result = webApi.execute(input("{q1:tgWebApiEntity(order: DESC_1){key model{key make(order: DESC_1){key}}}q2:tgWebApiEntity(order: ASC_1){key model{key make(order: DESC_1){key}}}}"));
        
        assertFalse(errors(result).isEmpty());
        assertEquals(result(
            listOf(linkedMapOf(
                t2("message", WARN_ORDER_PRIORITIES_ARE_NOT_DISTINCT),
                t2("locations", listOf(linkedMapOf(t2("line", "1"), t2("column", "2")))),
                t2("path", listOf("q1")),
                t2("extensions", linkedMapOf(t2("classification", "DataFetchingException")))
            ), linkedMapOf(
                t2("message", WARN_ORDER_PRIORITIES_ARE_NOT_DISTINCT),
                t2("locations", listOf(linkedMapOf(t2("line", "1"), t2("column", "75")))),
                t2("path", listOf("q2")),
                t2("extensions", linkedMapOf(t2("classification", "DataFetchingException")))
            )),
            linkedMapOf(
                t2("q1", listOf(
                    VEH4,
                    VEH3,
                    VEH2,
                    VEH1
                )),
                t2("q2", listOf(
                    VEH1,
                    VEH2,
                    VEH3,
                    VEH4
                ))
            )
        ).toString(), result.toString());
    }
    
}