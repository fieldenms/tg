package ua.com.fielden.platform.web.centre.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;
import static ua.com.fielden.platform.web.centre.api.resultset.PropDef.mkProp;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.TABLET;
import static ua.com.fielden.platform.web.interfaces.ILayout.Orientation.LANDSCAPE;

import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.impl.helpers.CustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.impl.helpers.FunctionalEntity;
import ua.com.fielden.platform.web.centre.api.impl.helpers.ResultSetRenderingCustomiser;
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

    @Test
    public void ordering_through_dsl_should_produce_a_correct_sequential_list_of_properties_to_order_by() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key").order(3).asc()
                .also()
                .addProp("vehicle")
                .also()
                .addProp("desc").order(1).desc()
                .also()
                .addProp("vehicle.key").order(2).asc()
                .build();

        assertEquals(4, config.getResultSetProperties().get().size());
        assertTrue(config.getResultSetOrdering().isPresent());
        assertEquals(3, config.getResultSetOrdering().get().size());
        assertEquals("desc", config.getResultSetOrdering().get().keySet().toArray()[0]);
        assertEquals(EntityCentreConfig.OrderDirection.DESC, config.getResultSetOrdering().get().values().toArray()[0]);
        assertEquals("vehicle.key", config.getResultSetOrdering().get().keySet().toArray()[1]);
        assertEquals(EntityCentreConfig.OrderDirection.ASC, config.getResultSetOrdering().get().values().toArray()[1]);
        assertEquals("key", config.getResultSetOrdering().get().keySet().toArray()[2]);
        assertEquals(EntityCentreConfig.OrderDirection.ASC, config.getResultSetOrdering().get().values().toArray()[2]);
    }

    @Test
    public void ordering_through_dsl_should_produce_a_correct_sequential_list_of_properties_to_order_by_even_if_sequential_numbers_have_gaps() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key").order(33).asc()
                .also()
                .addProp("vehicle")
                .also()
                .addProp("desc").order(-10).desc()
                .also()
                .addProp("vehicle.key").order(-2).asc()
                .build();

        assertEquals(4, config.getResultSetProperties().get().size());
        assertTrue(config.getResultSetOrdering().isPresent());
        assertEquals(3, config.getResultSetOrdering().get().size());
        assertEquals("desc", config.getResultSetOrdering().get().keySet().toArray()[0]);
        assertEquals(EntityCentreConfig.OrderDirection.DESC, config.getResultSetOrdering().get().values().toArray()[0]);
        assertEquals("vehicle.key", config.getResultSetOrdering().get().keySet().toArray()[1]);
        assertEquals(EntityCentreConfig.OrderDirection.ASC, config.getResultSetOrdering().get().values().toArray()[1]);
        assertEquals("key", config.getResultSetOrdering().get().keySet().toArray()[2]);
        assertEquals(EntityCentreConfig.OrderDirection.ASC, config.getResultSetOrdering().get().values().toArray()[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void attempt_to_order_by_the_same_sequence_should_be_prevented() {
        centreFor(TgWorkOrder.class)
                .addProp("key").order(1).asc()
                .also()
                .addProp("vehicle")
                .also()
                .addProp("desc").order(2).desc()
                .also()
                .addProp("vehicle.key").order(1).asc()
                .build();
    }

    @Test
    public void specified_primary_and_secondary_actions_should_be_present_in_config() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key")
                .addPrimaryAction(action(FunctionalEntity.class).withContext(context().withCurrentEntity().build()).build())
                .also()
                .addSecondaryAction(action(FunctionalEntity.class).withContext(context().withCurrentEntity().build()).build())
                .also()
                .addSecondaryAction(action(FunctionalEntity.class).withContext(context().withCurrentEntity().build()).build())
                .build();
        assertTrue(config.getResultSetPrimaryEntityAction().isPresent());
        assertTrue(config.getResultSetSecondaryEntityActions().isPresent());
        assertEquals(2, config.getResultSetSecondaryEntityActions().get().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_primary_action_should_be_prevented() {
        centreFor(TgWorkOrder.class)
                .addProp("key")
                .addPrimaryAction(null)
                .also()
                .addSecondaryAction(action(FunctionalEntity.class).withContext(context().withCurrentEntity().build()).build())
                .also()
                .addSecondaryAction(action(FunctionalEntity.class).withContext(context().withCurrentEntity().build()).build())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_secondary_action_should_be_prevented() {
        centreFor(TgWorkOrder.class)
                .addProp("key")
                .addPrimaryAction(action(FunctionalEntity.class).withContext(context().withCurrentEntity().build()).build())
                .also()
                .addSecondaryAction(null)
                .build();
    }

    @Test
    public void should_be_able_to_set_custom_prop_assignment_handler_for_result_set_with_custom_properties_without_default_vaues() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp(mkProp("OF", "Defect OFF road", "OF"))
                .also()
                .addProp(mkProp("ON", "Defect ON road", String.class))
                .setCustomPropsValueAssignmentHandler(CustomPropsAssignmentHandler.class)
                .build();

        assertTrue(config.getResultSetCustomPropAssignmentHandlerType().isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_permit_setting_custom_value_assignment_handler_for_result_set_where_all_custom_props_hav_default_vaues() {
        centreFor(TgWorkOrder.class)
                .addProp(mkProp("OF", "Defect OFF road", "OF"))
                .also()
                .addProp(mkProp("ON", "Defect ON road", "ON"))
                .setCustomPropsValueAssignmentHandler(CustomPropsAssignmentHandler.class)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_permit_setting_null_custom_value_assignment_handler() {
        centreFor(TgWorkOrder.class)
                .addProp(mkProp("OF", "Defect OFF road", "OF"))
                .also()
                .addProp(mkProp("ON", "Defect ON road", String.class))
                .setCustomPropsValueAssignmentHandler(null)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void should_require_custom_value_assignment_handler_for_result_set_with_custom_props_without_default_values() {
        centreFor(TgWorkOrder.class)
                .addProp(mkProp("OF", "Defect OFF road", "OF"))
                .also()
                .addProp(mkProp("ON", "Defect ON road", String.class))
                .build();
    }

    @Test
    public void specified_through_dsl_result_set_rendering_customiser_should_be_present() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key")
                .setRenderingCustomiser(ResultSetRenderingCustomiser.class)
                .build();

        assertTrue(config.getResultSetRenderingCustomiserType().isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void result_set_rendering_customiser_should_not_be_null() {
        centreFor(TgWorkOrder.class)
                .addProp(mkProp("OF", "Defect OFF road", "OF"))
                .setRenderingCustomiser(null)
                .build();
    }

    @Test
    public void this_keyword_should_be_supported_as_a_result_set_property() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("this")
                .build();

        assertEquals(1, config.getResultSetProperties().get().size());
        assertEquals("this", config.getResultSetProperties().get().get(0).propName.get());
    }

    @Test
    public void resultset_card_layouting_should_result_in_association_of_layouts_with_specified_devices() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("desc")
                .also()
                .addProp("yearlyCost")
                .setCollapsedCardLayoutFor(DESKTOP, Optional.empty(), "[[][]]")
                .setCollapsedCardLayoutFor(TABLET, Optional.of(LANDSCAPE), "[select='propName='yearlyCost'']")
                .withExpansionLayout("[select='propName='desc'']")
                .build();

        assertNotNull(config.getResultsetCollapsedCardLayout());
        assertTrue(config.getResultsetCollapsedCardLayout().hasLayoutFor(DESKTOP, null));
        assertTrue(config.getResultsetCollapsedCardLayout().hasLayoutFor(TABLET, LANDSCAPE));

        assertNotNull(config.getResultsetExpansionCardLayout());
        assertFalse(config.getResultsetExpansionCardLayout().hasLayoutFor(DESKTOP, null));
        assertTrue(config.getResultsetExpansionCardLayout().hasLayoutFor(TABLET, LANDSCAPE));
    }

}
