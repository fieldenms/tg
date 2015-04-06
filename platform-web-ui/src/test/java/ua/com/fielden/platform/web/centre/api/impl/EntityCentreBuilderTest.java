package ua.com.fielden.platform.web.centre.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;

public class EntityCentreBuilderTest {

    @Ignore
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
                .beginTopActionsGroup("group1")
                    .addGroupAction(topActionStub)
                    .addGroupAction(topActionStub)
                .endTopActionsGroup()
                .addCrit("key").asMulti().autocompleter()
                .setLayoutFor(Device.DESKTOP, null, ("['vertical', 'justified', 'margin:20px', "
                        + "[[mr], [mr], [mr], [mr], [mr]], "
                        + "[[mr], [mr], [mr], [mr], [mr]]]"))
                .addProp("desc").build();

        assertTrue(config.getTopLevelActions().isPresent());
        assertEquals(6, config.getTopLevelActions().get().size());
    }

}
