package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Injector;
import ua.com.fielden.platform.tiny.TinyHyperlink;
import ua.com.fielden.platform.web.interfaces.ILayout;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import java.util.Optional;

import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.StandardMastersWebUiConfig.MASTER_ACTION_DEFAULT_WIDTH;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.*;

public class TinyHyperlinkWebUiConfig {

    public final EntityMaster<TinyHyperlink> master;

    public static TinyHyperlinkWebUiConfig register(final Injector injector) {
        return new TinyHyperlinkWebUiConfig(injector);
    }

    private TinyHyperlinkWebUiConfig(final Injector injector) {
        master = createMaster(injector);
    }

    private static EntityMaster<TinyHyperlink> createMaster(final Injector injector) {
        final var layout = cell(
                cell(cell(CELL_LAYOUT).withGapBetweenCells(MARGIN))
                , FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final var masterConfig = new SimpleMasterBuilder<TinyHyperlink>().forEntity(TinyHyperlink.class)
                // TODO Calculated property representing the URL.
                .addProp(TinyHyperlink.ENTITY_TYPE_NAME).asSinglelineText().also()
                // TODO There should be an action to display a QR code and an action to copy the URL.
                .addAction(MasterActions.REFRESH).shortDesc("REFRESH").longDesc("Refresh")
                .setActionBarLayoutFor(ILayout.Device.DESKTOP, Optional.empty(), mkActionLayoutForMaster(1, MASTER_ACTION_DEFAULT_WIDTH))
                .setLayoutFor(ILayout.Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(ILayout.Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(ILayout.Device.MOBILE, Optional.empty(), layout)
                .withDimensions(mkDim(SIMPLE_ONE_COLUMN_MASTER_DIM_WIDTH, 320))
                .done();

        return new EntityMaster<>(TinyHyperlink.class, masterConfig, injector);
    }

}
