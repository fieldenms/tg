package ua.com.fielden.platform.web.centre.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

public class EntityCentreBuilderTest {

    @Test
    public void top_level_actions_should_be_present_in_configuration_with_appropriate_groups_and_order() {
        final EntityActionConfig topActionStub = action(null).withContext(context().withCurrentEntity().withSelectionCrit().build()).build();
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addTopAction(topActionStub)
                .also()
                .beginTopActionsGroup("group1")
                    .addGroupAction(topActionStub)
                    .addGroupAction(topActionStub)
                .endTopActionsGroup()
                .also()
                .addTopAction(topActionStub)
                .also()
                .beginTopActionsGroup("group2")
                    .addGroupAction(topActionStub)
                    .addGroupAction(topActionStub)
                .endTopActionsGroup()
                .addProp("desc").build();

        assertTrue(config.getTopLevelActions().isPresent());
        assertEquals(6, config.getTopLevelActions().get().size());
        assertFalse(config.getTopLevelActions().get().get(0).getValue().isPresent());
        assertEquals("group1", config.getTopLevelActions().get().get(1).getValue().get());
        assertEquals("group1", config.getTopLevelActions().get().get(2).getValue().get());
        assertFalse(config.getTopLevelActions().get().get(3).getValue().isPresent());
        assertEquals("group2", config.getTopLevelActions().get().get(4).getValue().get());
        assertEquals("group2", config.getTopLevelActions().get().get(5).getValue().get());
    }

    @Test
    public void top_level_actions_may_not_exists() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class).addProp("desc").build();

        assertFalse(config.getTopLevelActions().isPresent());
    }

    @Test
    public void selection_criteria_should_support_custom_autcompleters_with_custom_context() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("vehicle").asMulti().autocompleter(TgVehicle.class).withMatcher(CustomVehicleMatcher.class)
                .also()
                .addCrit("orgUnit1").asMulti().autocompleter(TgOrgUnit1.class).withMatcher(CustomOrgUnit1Matcher.class, context().withCurrentEntity().withSelectionCrit().build())
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "['vertical', 'justified', 'margin:20px', [][]")
                .addProp("desc").build();

        assertTrue(config.getSelectionCriteria().isPresent());
        assertEquals("vehicle", config.getSelectionCriteria().get().get(0));
        assertEquals("orgUnit1", config.getSelectionCriteria().get().get(1));
        assertTrue(config.getValueMatchersForSelectionCriteria().isPresent());
        assertEquals(2, config.getValueMatchersForSelectionCriteria().get().size());
        assertEquals(CustomVehicleMatcher.class, config.getValueMatchersForSelectionCriteria().get().get("vehicle").getKey());
        assertFalse(config.getValueMatchersForSelectionCriteria().get().get("vehicle").getValue().isPresent());
        assertEquals(CustomOrgUnit1Matcher.class, config.getValueMatchersForSelectionCriteria().get().get("orgUnit1").getKey());
        assertTrue(config.getValueMatchersForSelectionCriteria().get().get("orgUnit1").getValue().isPresent());
    }

    @Test
    public void selection_criteria_should_not_permit_invalid_property_expressions() {
        try {
            centreFor(TgWorkOrder.class)
                .addCrit("non.existing.property").asMulti().autocompleter(TgVehicle.class)
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "['vertical', 'justified', 'margin:20px', []")
                .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Provided value '%s' is not a valid property expression for entity '%s'", "non.existing.property", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_non_critonly_as_single_valued_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                .addCrit("vehicle").asSingle().autocompleter(TgVehicle.class)
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "['vertical', 'justified', 'margin:20px', []")
                .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used as a single-valued criterion due to missing @CritOnly(SINGLE) in its definition.", "vehicle", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_range_critonly_as_single_valued_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                .addCrit("orgunitCritOnly").asSingle().autocompleter(TgOrgUnit1.class)
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "['vertical', 'justified', 'margin:20px', []")
                .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used as a single-valued criterion due to its definition as @CritOnly(RANGE).", "orgunitCritOnly", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_critonly_as_multi_valued_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                .addCrit("orgunitCritOnlySingle").asMulti().autocompleter(TgOrgUnit1.class)
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "['vertical', 'justified', 'margin:20px', []")
                .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used as a multi-valued criterion due to its definition as @CritOnly(SINGLE).", "orgunitCritOnlySingle", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_not_permit_single_critonly_as_range_valued_criteria() {
        try {
            centreFor(TgWorkOrder.class)
                .addCrit("intSingle").asRange().integer()
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "['vertical', 'justified', 'margin:20px', []")
                .addProp("desc").build();
            fail();
        } catch (final IllegalArgumentException ex) {
            assertEquals(String.format("Property '%s'@'%s' cannot be used as a range-valued criterion due to its definition as @CritOnly(SINGLE).", "intSingle", TgWorkOrder.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void selection_criteria_should_permit_single_critonly_as_single_valued_criteria() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("intSingle").asSingle().integer()
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "['vertical', 'justified', 'margin:20px', []")
                .addProp("desc").build();

            assertTrue(config.getSelectionCriteria().isPresent());
            assertEquals("intSingle", config.getSelectionCriteria().get().get(0));
    }

    @Test
    public void selection_criteria_should_permit_range_critonly_as_range_valued_criteria() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addCrit("intRange").asRange().integer()
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "['vertical', 'justified', 'margin:20px', []")
                .addProp("desc").build();

            assertTrue(config.getSelectionCriteria().isPresent());
            assertEquals("intRange", config.getSelectionCriteria().get().get(0));
    }

}
