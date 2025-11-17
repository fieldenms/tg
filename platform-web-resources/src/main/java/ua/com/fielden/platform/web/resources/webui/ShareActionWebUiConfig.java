package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Injector;
import ua.com.fielden.platform.tiny.ShareAction;
import ua.com.fielden.platform.tiny.ShareActionProducer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import java.util.Optional;

import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.StandardMastersWebUiConfig.MASTER_ACTION_DEFAULT_WIDTH;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.*;

public class ShareActionWebUiConfig {

    public final EntityMaster<ShareAction> master;

    public static ShareActionWebUiConfig register(final Injector injector) {
        return new ShareActionWebUiConfig(injector);
    }

    private ShareActionWebUiConfig(final Injector injector) {
        master = createMaster(injector);
    }

    private static EntityMaster<ShareAction> createMaster(final Injector injector) {
        final var layout = cell(
                cell(cell(CELL_LAYOUT).withGapBetweenCells(MARGIN))
                // Embed the QR code.
                .html("<img style='height: auto; width: 75%' src=data:image/png;base64,{{qrCode}} />",
                      layout().withStyle("padding-top", MARGIN_PIX)
                              .withStyle("display", "flex")
                              .withStyle("justify-content", "center")
                              .end())
                , FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final var masterConfig = new SimpleMasterBuilder<ShareAction>().forEntity(ShareAction.class)
                .addProp(ShareAction.HYPERLINK).asHyperlink().also()
                .addCancelAction().excludeNew().shortDesc("Close").longDesc("Close")
                .setActionBarLayoutFor(DESKTOP, Optional.empty(), mkActionLayoutForMaster(1, MASTER_ACTION_DEFAULT_WIDTH))
                .setLayoutFor(DESKTOP, Optional.empty(), layout)
                .withDimensions(mkDim(540, 700))
                .done();

        return new EntityMaster<>(ShareAction.class, ShareActionProducer.class, masterConfig, injector);
    }

}
