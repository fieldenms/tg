package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Injector;
import ua.com.fielden.platform.tiny.EntityShareAction;
import ua.com.fielden.platform.tiny.EntityShareActionProducer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import java.util.Optional;

import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.StandardMastersWebUiConfig.MASTER_ACTION_DEFAULT_WIDTH;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.*;

public class EntityShareActionWebUiConfig {

    public final EntityMaster<EntityShareAction> master;

    public static EntityShareActionWebUiConfig register(final Injector injector) {
        return new EntityShareActionWebUiConfig(injector);
    }

    private EntityShareActionWebUiConfig(final Injector injector) {
        master = createMaster(injector);
    }

    private static EntityMaster<EntityShareAction> createMaster(final Injector injector) {
        final var layout = cell(
                cell(cell(CELL_LAYOUT).withGapBetweenCells(MARGIN))
                , FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final var masterConfig = new SimpleMasterBuilder<EntityShareAction>().forEntity(EntityShareAction.class)
                .addProp(EntityShareAction.HYPERLINK).asHyperlink().also()
                .addCancelAction().excludeNew().shortDesc("Close").longDesc("Close")
                .setActionBarLayoutFor(DESKTOP, Optional.empty(), mkActionLayoutForMaster(1, MASTER_ACTION_DEFAULT_WIDTH))
                .setLayoutFor(DESKTOP, Optional.empty(), layout)
                .withDimensions(mkDim(SIMPLE_ONE_COLUMN_MASTER_DIM_WIDTH, 200))
                .done();

        return new EntityMaster<>(EntityShareAction.class, EntityShareActionProducer.class, masterConfig, injector);
    }

}
