package ua.com.fielden.platform.web.centre.api.impl;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;
import static ua.com.fielden.platform.web.centre.api.resultset.PropDef.mkProp;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.impl.helpers.FunctionalEntity;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;

/**
 * A test case for Entity Centre DSL produced result sets.
 *
 * @author TG Team
 *
 */
public class EntityCentreBuilderResultSetTest {

    @Test
    public void order_of_properties_and_custom_properties_should_be_the_same_as_during_centre_definition() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key")
                .also()
                .addProp(mkProp("OF", "Defect OFF road", "OF"))
                .also()
                .addProp(mkProp("ON", "Defect ON road", "ON"))
                .also()
                .addProp("desc")
                .build();

        assertFalse(config.getSelectionCriteria().isPresent());
        assertTrue(config.getResultSetProperties().isPresent());
        assertEquals(4, config.getResultSetProperties().get().size());

        assertTrue(config.getResultSetProperties().get().get(0).propName.isPresent());
        assertFalse(config.getResultSetProperties().get().get(0).propDef.isPresent());
        assertEquals("key", config.getResultSetProperties().get().get(0).propName.get());

        assertFalse(config.getResultSetProperties().get().get(1).propName.isPresent());
        assertTrue(config.getResultSetProperties().get().get(1).propDef.isPresent());
        assertEquals("OF", config.getResultSetProperties().get().get(1).propDef.get().title);

        assertFalse(config.getResultSetProperties().get().get(2).propName.isPresent());
        assertTrue(config.getResultSetProperties().get().get(2).propDef.isPresent());
        assertEquals("ON", config.getResultSetProperties().get().get(2).propDef.get().title);

        assertTrue(config.getResultSetProperties().get().get(3).propName.isPresent());
        assertFalse(config.getResultSetProperties().get().get(3).propDef.isPresent());
        assertEquals("desc", config.getResultSetProperties().get().get(3).propName.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void adding_non_exitsting_properties_to_result_set_should_be_prevented() {
        centreFor(TgWorkOrder.class).addProp("non.existing.prop").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void adding_null_as_property_to_result_set_should_be_prevented() {
        centreFor(TgWorkOrder.class).addProp((String) null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void adding_null_as_custom_property_to_result_set_should_be_prevented() {
        centreFor(TgWorkOrder.class).addProp((PropDef<?>) null).build();
    }

    @Test
    public void properties_and_custom_properties_should_support_custom_actions() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key").withAction(action(FunctionalEntity.class).withContext(context().withCurrentEntity().build()).build())
                .also()
                .addProp(mkProp("OF", "Defect OFF road", "OF")).withAction(action(FunctionalEntity.class).withContext(context().withCurrentEntity().withSelectionCrit().build()).longDesc("Changes vehicle status").build())
                .build();

        assertEquals(2, config.getResultSetProperties().get().size());
        assertTrue(config.getResultSetProperties().get().get(0).propAction.isPresent());
        assertFalse(config.getResultSetProperties().get().get(0).propAction.get().longDesc.isPresent());
        assertTrue(config.getResultSetProperties().get().get(1).propAction.isPresent());
        assertTrue(config.getResultSetProperties().get().get(1).propAction.get().longDesc.isPresent());
        assertEquals("Changes vehicle status", config.getResultSetProperties().get().get(1).propAction.get().longDesc.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void adding_null_as_custom_property_action_should_be_prevented() {
        centreFor(TgWorkOrder.class).addProp(mkProp("OF", "Defect OFF road", "OF")).withAction(null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void adding_null_as_property_action_should_be_prevented() {
        centreFor(TgWorkOrder.class).addProp("key").withAction(null).build();
    }

}
