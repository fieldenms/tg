package ua.com.fielden.platform.web.centre.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.helpers.FunctionalEntity;

/**
 * A test case for Entity Centre DSL generated top level actions.
 * @author TG Team
 *
 */
public class EntityCentreBuilderTopLevelActionsTest {

    @Test
    public void top_level_actions_should_be_present_in_configuration_with_appropriate_groups_and_order() {
        final EntityActionConfig topActionStub = action(FunctionalEntity.class).withContext(context().withCurrentEntity().withSelectionCrit().build()).build();
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

}
