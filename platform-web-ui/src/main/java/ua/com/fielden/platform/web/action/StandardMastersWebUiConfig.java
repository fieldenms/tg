package ua.com.fielden.platform.web.action;

import com.google.inject.Injector;
import ua.com.fielden.platform.attachment.AttachmentPreviewEntityAction;
import ua.com.fielden.platform.attachment.AttachmentsUploadAction;
import ua.com.fielden.platform.attachment.producers.AttachmentPreviewEntityActionProducer;
import ua.com.fielden.platform.attachment.producers.AttachmentsUploadActionProducer;
import ua.com.fielden.platform.entity.*;
import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.menu.MenuProducer;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;
import ua.com.fielden.platform.web.menu.impl.MainMenuBuilder;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.EntityEditMaster;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.EntityManipulationMasterBuilder;
import ua.com.fielden.platform.web.view.master.attachments.AttachmentPreviewEntityMaster;
import ua.com.fielden.platform.web.view.master.attachments.AttachmentsUploadActionMaster;

import java.util.Optional;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.EntityExportAction.*;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.*;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.*;

///
/// A set of factory methods for various standard platform-level entity masters such as Export to Excel.
///
public class StandardMastersWebUiConfig {
    public static final int MASTER_ACTION_DEFAULT_WIDTH = 80;
    public static final String MASTER_ACTION_CUSTOM_SPECIFICATION = "'margin: 10px', 'width: %spx'";
    public static final String MASTER_ACTION_SPECIFICATION = format(MASTER_ACTION_CUSTOM_SPECIFICATION, MASTER_ACTION_DEFAULT_WIDTH);

    private StandardMastersWebUiConfig() {}

    public static EntityMaster<EntityNewAction> createEntityNewMaster(final Injector injector) {
        return new EntityMaster<>(EntityNewAction.class,
                EntityNewActionProducer.class,
                new EntityManipulationMasterBuilder<EntityNewAction>()
                /*  */.forEntityWithSaveOnActivate(EntityNewAction.class)
                /*  */.withMaster(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from selection criteria context
                /*  */.done(),
                injector);
    }

    public static EntityMaster<EntityEditAction> createEntityEditMaster(final Injector injector) {
        return new EntityMaster<>(EntityEditAction.class,
                EntityEditActionProducer.class,
                new EntityEditMaster(EntityEditAction.class, true),
                injector);
    }

    public static EntityMaster<AttachmentPreviewEntityAction> createAttachmentPreviewMaster(final Injector injector) {
        return new EntityMaster<>(AttachmentPreviewEntityAction.class,
                AttachmentPreviewEntityActionProducer.class,
                new AttachmentPreviewEntityMaster(),
                injector);
    }

    public static EntityMaster<EntityExportAction> createExportMaster(final Injector injector) {
        final FlexLayoutConfig CELL_LAYOUT = layout().flex().end();

        final String layout = cell(
                cell(cell(CELL_LAYOUT))
               .cell(cell(CELL_LAYOUT))
               .cell(cell(CELL_LAYOUT))
               .cell(cell(CELL_LAYOUT), layout().withStyle("padding-left", "32px").end()),
               layout().withStyle("padding", "20px").end()).toString();

        final String MASTER_ACTION_LAYOUT_SPECIFICATION = "'horizontal', 'padding: 10px', 'wrap', 'justify-content: center'";
        final String buttonPanelLayout = format("[%s, [%s], [%s]]", MASTER_ACTION_LAYOUT_SPECIFICATION, MASTER_ACTION_SPECIFICATION, MASTER_ACTION_SPECIFICATION);
        final IMaster<EntityExportAction> masterConfig = new SimpleMasterBuilder<EntityExportAction>()
                .forEntity(EntityExportAction.class)
                .addProp(PROP_EXPORT_ALL).asCheckbox().also()
                .addProp(PROP_EXPORT_SELECTED).asCheckbox().also()
                .addProp(PROP_EXPORT_TOP).asCheckbox().also()
                .addProp(PROP_NUMBER).asInteger().also()
                .addAction(MasterActions.REFRESH)
                /*      */.shortDesc("CANCEL")
                /*      */.longDesc("Cancel action.")
                .addAction(MasterActions.SAVE)
                /*      */.shortDesc("EXPORT")
                /*      */.longDesc("Export data.")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), buttonPanelLayout)
                .setLayoutFor(DESKTOP, Optional.empty(), layout)
                .setLayoutFor(TABLET, Optional.empty(), layout)
                .setLayoutFor(MOBILE, Optional.empty(), layout)
                .withDimensions(mkDim(420, 370, Unit.PX))
                .done();

        return new EntityMaster<>(EntityExportAction.class, EntityExportActionProducer.class, masterConfig, injector);
    }

    public static EntityMaster<UserDefinableHelp> createUserDefinableHelpMaster(final Injector injector) {
        final String layout = cell(cell(CELL_LAYOUT),layout().withStyle("padding", MARGIN_PIX).end()).toString();

        final IMaster<UserDefinableHelp> masterConfig = new SimpleMasterBuilder<UserDefinableHelp>()
                .forEntity(UserDefinableHelp.class)
                .addProp("help").asHyperlink()
                .also()
                .addAction(MasterActions.REFRESH)
                /*      */.shortDesc("CANCEL").longDesc("Cancel changes or refresh.")
                .addAction(MasterActions.SAVE)
                /*      */.shortDesc("SAVE").longDesc("Save changes.")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), mkActionLayoutForMaster())
                .setLayoutFor(DESKTOP, Optional.empty(), layout)
                .setLayoutFor(TABLET, Optional.empty(), layout)
                .setLayoutFor(MOBILE, Optional.empty(), layout)
                .withDimensions(mkDim(640, 180, Unit.PX))
                .done();

        return new EntityMaster<>(UserDefinableHelp.class, UserDefinableHelpProducer.class, masterConfig, injector);
    }

    public static EntityMaster<AttachmentsUploadAction> createAttachmentsUploadMaster(
            final Injector injector,
            final PrefDim dims,
            final int fileSizeLimitKb,
            final String mimeType,
            final String... moreMimeTypes) {
        final IMaster<AttachmentsUploadAction> masterConfig = new AttachmentsUploadActionMaster(dims, fileSizeLimitKb, mimeType, moreMimeTypes);
        return new EntityMaster<>(AttachmentsUploadAction.class, AttachmentsUploadActionProducer.class, masterConfig, injector);
    }

    ///
    /// Creates an entity master configuration for {@link PersistentEntityInfo}.
    ///
    /// @param injector
    /// @return
    ///
    public static EntityMaster<PersistentEntityInfo> createPersistentEntityInfoMaster(final Injector injector) {
        final String desktopLayout = cell(cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN)).repeat(3),layout().withStyle("padding", MARGIN_PIX).end()).toString();
        final String mobileLayout = cell(cell().repeat(6),layout().withStyle("padding", MARGIN_PIX).end()).toString();

        final IMaster<PersistentEntityInfo> masterConfig = new SimpleMasterBuilder<PersistentEntityInfo>()
                .forEntity(PersistentEntityInfo.class)
                .addProp("lastUpdatedBy").asAutocompleter().also()
                .addProp("lastUpdatedDate").asDateTimePicker().also()
                .addProp("createdBy").asAutocompleter().also()
                .addProp("createdDate").asDateTimePicker().also()
                .addProp("entityId").asSinglelineText().also()
                .addProp("entityVersion").asInteger()
                .also()
                .addAction(MasterActions.REFRESH)
                    .shortDesc("Close")
                    .longDesc("Closes the dialog.")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), mkActionLayoutForMaster(1, MASTER_ACTION_DEFAULT_WIDTH))
                .setLayoutFor(DESKTOP, Optional.empty(), desktopLayout)
                .setLayoutFor(TABLET, Optional.empty(), desktopLayout)
                .setLayoutFor(MOBILE, Optional.empty(), mobileLayout)
                .withDimensions(mkDim(590, 305, Unit.PX))
                .done();

        return new EntityMaster<>(PersistentEntityInfo.class, PersistentEntityInfoProducer.class, masterConfig, injector);
    }

    public static EntityMaster<Menu> createMenuMaster(final Injector injector, final MainMenuBuilder desktopMenuBuilder, final MainMenuBuilder mobileMenuBuilder) {
        return new EntityMaster<Menu>(Menu.class, MenuProducer.class, null, injector) {
            @Override
            public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
                final IDeviceProvider deviceProvider = injector.getInstance(IDeviceProvider.class);
                final MainMenuBuilder menuBuilder = deviceProvider.getDeviceProfile() == DeviceProfile.DESKTOP ? desktopMenuBuilder  : mobileMenuBuilder;
                return menuBuilder.getActionConfig(actionNumber, actionKind);
            }
        };
    }

    // TODO once it will be necessary, uncomment this code to implement generic EDIT / NEW actions with 'no parent centre refresh' capability:
//    public static EntityMaster<EntityNewActionWithNoParentCentreRefresh> createEntityNewMasterWithNoParentCentreRefresh(final Injector injector) {
//        return new EntityMaster<EntityNewAction>(EntityNewActionWithNoParentCentreRefresh.class,
//                EntityNewActionWithNoParentCentreRefreshProducer.class,
//                new EntityManipulationMasterBuilder<EntityNewActionWithNoParentCentreRefresh>()
//                /*  */.forEntityWithSaveOnActivate(EntityNewActionWithNoParentCentreRefresh.class)
//                /*  */.withMasterAndWithNoParentCentreRefresh(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from selection criteria context
//                /*  */.done(),
//                injector);
//    }
//
//    public static EntityMaster<EntityEditActionWithNoParentCentreRefresh> createEntityEditMasterWithNoParentCentreRefresh(final Injector injector) {
//        return new EntityMaster<EntityEditActionWithNoParentCentreRefresh>(EntityEditActionWithNoParentCentreRefresh.class,
//                EntityEditActionWithNoParentCentreRefreshProducer.class,
//                new EntityManipulationMasterBuilder<EntityEditActionWithNoParentCentreRefresh>()
//                /*  */.forEntityWithSaveOnActivate(EntityEditActionWithNoParentCentreRefresh.class)
//                /*  */.withMasterAndWithNoParentCentreRefresh(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from currentEntity context
//                /*  */.done(),
//                injector);
//    }
}
